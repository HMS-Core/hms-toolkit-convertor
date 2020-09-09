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
import com.huawei.hms.convertor.openapi.result.Result;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Event Queue
 *
 * @since 2020-02-24
 */
@Slf4j
public class ProjectEventQueue {
    /**
     * General event, lower priority than code edit event
     */
    private LinkedBlockingQueue<ProjectEvent> generalEvents = new LinkedBlockingQueue<ProjectEvent>();

    /**
     * Code edit event, handle this event queue with highest priority
     */
    private LinkedBlockingQueue<ProjectEvent> editEvents = new LinkedBlockingQueue<ProjectEvent>();

    /**
     * Push event to the queue
     *
     * @param event Project level event
     */
    Result push(ProjectEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Receive event: {}", event);
        }

        try {
            if (event.isEditEvent()) {
                editEvents.put(event);
            } else {
                generalEvents.put(event);
            }
            return Result.ok();
        } catch (InterruptedException e) {
            log.error("Put event[{}] to event queue failed.", event, e);
            return Result.failed("Push event to the queue failed");
        }
    }

    /**
     * Consume common event, blocked if the queue is empty
     *
     * @return Common event
     */
    Result<ProjectEvent> consume() {
        try {
            ProjectEvent event = generalEvents.take();

            if (log.isDebugEnabled()) {
                log.debug("Consume event: {}", event);
            }

            return Result.ok(event);
        } catch (InterruptedException e) {
            log.error("Take event from event queue failed", e);
            return Result.failed("Consume event from the queue failed");
        }
    }

    /**
     * Consume specified event
     *
     * @param eventType Event type
     * @return Event
     */
    Result<ProjectEvent> consume(EventType eventType) {
        if (eventType == EventType.EDIT_EVENT) {
            if (editEvents.isEmpty()) {
                return Result.failed("None edit event in the queue");
            }

            ProjectEvent event = editEvents.poll();

            if (log.isDebugEnabled()) {
                log.debug("Consume event: {}", event);
            }

            return Result.ok(event);
        } else {
            throw new IllegalArgumentException("Unsupported event: " + eventType.getName());
        }
    }

    /**
     * Check if the specified event queue is empty
     *
     * @param eventType Event type
     * @return {@code true} if the specified event queue is empty, {@code false} otherwise
     */
    boolean isEmpty(EventType eventType) {
        if (eventType == EventType.EDIT_EVENT) {
            return editEvents.isEmpty();
        } else {
            return generalEvents.isEmpty();
        }
    }
}
