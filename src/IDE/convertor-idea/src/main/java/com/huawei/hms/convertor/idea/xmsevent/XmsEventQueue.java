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

package com.huawei.hms.convertor.idea.xmsevent;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Xms event queue
 *
 * @since 2020-03-09
 */
@Slf4j
@Getter
@Setter
public class XmsEventQueue {
    /**
     * General event, lower priority than code edit event
     */
    private LinkedBlockingQueue<XmsEvent> xmsEvents;

    public XmsEventQueue() {
        this.xmsEvents = new LinkedBlockingQueue<XmsEvent>();
    }

    public int getSize() {
        return xmsEvents.size();
    }

    /**
     * Push event to the queue
     *
     * @param eventStr hmsKitItems stirng
     */
    public void push(String eventStr) {
        log.info("Receive event: {}", eventStr);

        try {
            XmsEvent event = new XmsEvent(eventStr);
            if (event.isValidEvent()) {
                log.info("put event: {}", eventStr);
                xmsEvents.put(event);
            } else {
                log.warn("Invalid event[{}].", eventStr);
            }
        } catch (InterruptedException e) {
            log.error("Put event[{}] to event queue failed.", eventStr, e);
        }
    }

    /**
     * Check if the specified event queue is empty
     *
     * @return {@code true} if the event queue is empty, {@code false} otherwise
     */
    boolean isEmpty() {
        return xmsEvents.isEmpty();
    }
}
