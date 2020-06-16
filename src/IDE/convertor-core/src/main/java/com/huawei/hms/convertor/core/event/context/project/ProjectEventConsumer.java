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

import com.huawei.hms.convertor.core.event.handler.project.ProjectEventHandler;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.ExecutorServiceBuilder;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * Project level event consumer
 *
 * @since 2020-02-24
 */
@Slf4j
class ProjectEventConsumer {
    private static final String THREAD_NAME_PREFIX = "project-";

    private static final String THREAD_NAME_SUFFIX = "-event-consumer-%d";

    private ExecutorService eventExecutor;

    private boolean isRunning = true;

    private ProjectEventQueue eventQueue;

    private ProjectEventHandler eventHandler;

    /**
     * Create project level event consumer
     *
     * @param projectName Root project folder name
     * @param eventQueue Project level event queue
     * @param eventHandler Project level event handler
     */
    ProjectEventConsumer(String projectName, ProjectEventQueue eventQueue, ProjectEventHandler eventHandler) {
        this.eventQueue = eventQueue;
        this.eventHandler = eventHandler;

        eventExecutor =
            ExecutorServiceBuilder.newSingleThreadExecutor(THREAD_NAME_PREFIX + projectName + THREAD_NAME_SUFFIX);
    }

    /**
     * Consume project level event
     *
     * @return Project level event
     */
    private Result<ProjectEvent> consumeEvent() {
        return eventQueue.consume();
    }

    private void handleEvent(ProjectEvent event) {
        eventHandler.handleGeneralEvent(event);
    }

    /**
     * Startup event consumer
     */
    final void startup() {
        isRunning = true;

        eventExecutor.execute(() -> {
            while (isRunning) {
                try {
                    Result<ProjectEvent> result = consumeEvent();
                    if (result.isOk()) {
                        handleEvent(result.getData());
                    }
                } catch (Exception e) {
                    log.error("{} thread interrupted", getClass().getName(), e);
                }
            }
        });
    }

    /**
     * Shutdown event executor
     */
    final void shutdown() {
        isRunning = false;

        eventExecutor.shutdown();
    }
}
