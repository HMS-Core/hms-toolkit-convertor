/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.core.event.context.project;

import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.core.event.handler.project.ProjectEventHandler;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.ExecutorServiceBuilder;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Project level event Listener
 *
 * @since 2020-02-24
 */
@Slf4j
final class ProjectEventListener {
    private static final String THREAD_NAME_PREFIX = "project-";

    private static final String THREAD_NAME_SUFFIX = "-event-listener-%d";

    /**
     * The period for checking if has events int the edit event queue, unit: ms
     */
    private static final int EDIT_EVENT_QUEUE_CHECK_PERIOD = 500;

    private ExecutorService eventExecutor;

    private boolean isRunning = true;

    private ProjectEventQueue eventQueue;

    private ProjectEventHandler eventHandler;

    /**
     * Create project level event listener
     *
     * @param projectName Root project folder name
     * @param eventQueue Project level event queue
     * @param eventHandler Project level event handler
     */
    ProjectEventListener(String projectName, ProjectEventQueue eventQueue, ProjectEventHandler eventHandler) {
        this.eventQueue = eventQueue;
        this.eventHandler = eventHandler;

        eventExecutor =
            ExecutorServiceBuilder.newSingleThreadExecutor(THREAD_NAME_PREFIX + projectName + THREAD_NAME_SUFFIX);
    }

    /**
     * Startup event listener
     */
    void startup() {
        isRunning = true;

        eventExecutor.execute(() -> {
            while (isRunning) {
                try {
                    if (hasEditEvent()) {
                        suspendOtherEvent();
                        handleEditEvent();
                        resumeOtherEvent();
                    } else {
                        waitForNextCheck();
                    }
                } catch (Exception e) {
                    log.error("{} thread interrupted", getClass().getName(), e);
                }
            }
        });
    }

    private boolean hasEditEvent() {
        return !eventQueue.isEmpty(EventType.EDIT_EVENT);
    }

    private void suspendOtherEvent() {
        eventHandler.suspendGeneralEvent();
    }

    private void resumeOtherEvent() {
        eventHandler.resumeGeneralEvent();
    }

    private void handleEditEvent() {
        // Handle event one by one until none event in the edit event queue
        Result<ProjectEvent> result = eventQueue.consume(EventType.EDIT_EVENT);
        while (result.isOk()) {
            try {
                eventHandler.handleEditEvent(result.getData());
            } catch (RuntimeException e) {
                log.error("Handle event failed", e);
            }
            result = eventQueue.consume(EventType.EDIT_EVENT);
        }
    }

    private void waitForNextCheck() {
        try {
            TimeUnit.MILLISECONDS.sleep(EDIT_EVENT_QUEUE_CHECK_PERIOD);
        } catch (InterruptedException e) {
            log.error("Wait for next check error", e);
        }
    }

    /**
     * Shutdown event listener
     */
    void shutdown() {
        isRunning = false;
        eventExecutor.shutdown();
    }
}
