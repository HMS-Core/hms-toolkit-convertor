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

package com.huawei.hms.convertor.core.event.handler.project;

import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.core.event.context.project.ProjectEvent;
import com.huawei.hms.convertor.core.event.handler.CallbackExecuteHandler;
import com.huawei.hms.convertor.core.event.handler.EventHandler;
import com.huawei.hms.convertor.core.result.conversion.ChangedCode;
import com.huawei.hms.convertor.util.ExecutorServiceBuilder;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Event handler
 *
 * @since 2020-02-25
 */
@Slf4j
public final class ProjectEventHandler {
    private static final String THREAD_NAME_PREFIX = "project-";

    private static final String THREAD_NAME_SUFFIX = "-event-callback-%d";

    /**
     * The period for checking if the general event suspended
     */
    private static final int GENERAL_EVENT_SUSPEND_CHECK_PERIOD = 1;

    private AtomicBoolean isGeneralEventSuspended = new AtomicBoolean(false);

    private ExecutorService eventExecutor;

    private Map<EventType, EventHandler> handlerMap;

    /**
     * Create project level event handler
     */
    public ProjectEventHandler(String projectName) {
        eventExecutor =
            ExecutorServiceBuilder.newSingleThreadExecutor(THREAD_NAME_PREFIX + projectName + THREAD_NAME_SUFFIX);

        handlerMap = new HashMap<>();
    }

    /**
     * Register event handler
     *
     * @param type Event type
     * @param eventHandler Event handler
     */
    public void registerHandler(EventType type, EventHandler eventHandler) {
        if (handlerMap.containsKey(type)) {
            throw new ProjectEventHandler.DuplicateRegisterException("Duplicate registered. Event type: "
                + type.getName() + " , handler: " + eventHandler.getClass().getName());
        }

        handlerMap.put(type, eventHandler);
    }

    /**
     * Clear event handler
     */
    public void shutdown() {
        eventExecutor.shutdown();
        handlerMap.clear();
    }

    /**
     * Handle event
     *
     * @param event Event Object
     */
    public void handleGeneralEvent(ProjectEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Handle event: {}", event);
        }

        EventHandler eventHandler = getEventHandler(event.getType());
        String projectPath = event.getProjectPath();

        Object eventData = event.getData();
        if (eventData instanceof Collection) {
            ((Collection) eventData).forEach((data) -> {
                handleEvent(eventHandler, projectPath, data);
            });
        } else {
            handleEvent(eventHandler, projectPath, event.getData());
        }

        if (eventHandler instanceof CallbackExecuteHandler) {
            executeCallback((CallbackExecuteHandler) eventHandler, event);
        }
    }

    /**
     * Handle event
     *
     * @param event Project event
     * @param <D> Event data type
     * @param <M> Callback message type
     */
    public <D, M> void handleEditEvent(ProjectEvent<D, M> event) {
        if (log.isDebugEnabled()) {
            log.debug("Handle event: {}", event);
        }

        if (!(event.getData() instanceof ChangedCode)) {
            log.error("Illegal event data type, expected: {}, actual: {}", ChangedCode.class.getName(),
                event.getData().getClass().getName());
        }

        EventHandler eventHandler = getEventHandler(EventType.EDIT_EVENT);
        String projectPath = event.getProjectPath();
        if (eventHandler instanceof EditEventHandler) {
            ((EditEventHandler) eventHandler).handleEvent(projectPath, event.getData());
        }

        if (eventHandler instanceof CallbackExecuteHandler) {
            executeCallback((CallbackExecuteHandler) eventHandler, event);
        }
    }

    /**
     * Suspend to handle general event
     */
    public void suspendGeneralEvent() {
        isGeneralEventSuspended.set(true);
    }

    /**
     * Resume to handle general event
     */
    public void resumeGeneralEvent() {
        isGeneralEventSuspended.set(false);
    }

    /**
     * Get specified event handler
     *
     * @param type Event type
     * @return Event handler
     */
    private EventHandler getEventHandler(EventType type) {
        if (!handlerMap.containsKey(type)) {
            throw new ProjectEventHandler.HandlerNotFoundException(
                "Event handler not found. Event type: " + type.getName());
        }

        return handlerMap.get(type);
    }

    private <T> void handleEvent(EventHandler eventHandler, String projectPath, T data) {
        checkOrWaitGeneralEventResume();

        if (eventHandler instanceof GeneralEventHandler) {
            ((GeneralEventHandler) eventHandler).handleEvent(projectPath, data);
        } else {
            throw new UnsupportedOperationException(
                "Unsupported event handler: " + eventHandler.getClass().getSimpleName());
        }
    }

    private void executeCallback(CallbackExecuteHandler handler, ProjectEvent event) {
        // Async execute callback
        eventExecutor.execute(() -> handler.executeCallback(event));
    }

    private void checkOrWaitGeneralEventResume() {
        while (isGeneralEventSuspended.get()) {
            log.info("Handling code edit event, general event handler suspended");

            try {
                TimeUnit.SECONDS.sleep(GENERAL_EVENT_SUSPEND_CHECK_PERIOD);
            } catch (InterruptedException e) {
                log.error("Wait for finishing all edit-events in the queue error", e);
            }
        }
    }

    private static class HandlerNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 2612972031662684115L;

        HandlerNotFoundException(String message) {
            super(message);
        }
    }

    private static class DuplicateRegisterException extends RuntimeException {
        private static final long serialVersionUID = 2612972031662684115L;

        DuplicateRegisterException(String message) {
            super(message);
        }
    }
}
