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

import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.util.ExecutorServiceBuilder;

import com.intellij.openapi.project.Project;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * Xms event consumer
 *
 * @since 2020-03-09
 */
@Slf4j
@Getter
public class XmsEventConsumer {
    public static final int CONSUMER_IDLE = 0;

    public static final int CONSUMER_GENERATING = 1;

    private static final String THREAD_NAME_FORMAT = "convertor-xms-event-consumer-%d";

    private String pluginJarPath;

    private ExecutorService eventExecutor = ExecutorServiceBuilder.newSingleThreadExecutor(THREAD_NAME_FORMAT);

    private XmsEventQueue xmsEventQueue;

    private XmsEventHandler handler;

    private boolean isRunning = false;

    private String eventStr;

    private int status; // 0 idle 1 generating

    public XmsEventConsumer(Project project, XmsEventQueue xmsEventQueue) {
        this.xmsEventQueue = xmsEventQueue;
        pluginJarPath = System.getProperty(XmsConstants.KEY_XMS_JAR);
        handler = new XmsEventHandler(project, pluginJarPath);
    }

    /**
     * Startup event consumer
     */
    public final void startup() {
        log.info("startup enter {} {}", isRunning, eventExecutor.hashCode());

        isRunning = true;
        eventExecutor.execute(() -> {
            while (isRunning) {
                try {
                    log.info("wait event");
                    XmsEvent xmsEvent = consumeEvent();
                    if (xmsEvent.isValidEvent()) {
                        log.info("get event{}", xmsEvent.toString());
                        refreshStatusAndEvent(CONSUMER_GENERATING, xmsEvent.getHmsKitItemsStirng());
                        log.info("handle Event");
                        handleEvent(xmsEvent);
                        refreshStatusAndEvent(CONSUMER_IDLE, null);
                    }
                } catch (Exception e) {
                    log.error("{} thread interrupted", e.getMessage(), e);
                }
            }
        });

        log.info("startup finish {} {}", isRunning, eventExecutor.hashCode());
    }

    /**
     * Shutdown event executor
     */
    public final void shutdown() {
        isRunning = false;
        eventExecutor.shutdownNow();
        log.info("shut down now");
    }

    /**
     * Consume event
     *
     * @return event
     */
    private XmsEvent consumeEvent() {
        return consume();
    }

    private void handleEvent(XmsEvent event) {
        handler.handleEvent(event);
    }

    XmsEvent consume() {
        try {
            log.info("xmsEvents take");
            XmsEvent event = xmsEventQueue.getXmsEvents().take();
            log.info("Consume event: {}", event.toString());

            return event;
        } catch (InterruptedException e) {
            log.error("Take event from event queue failed", e);
            return new XmsEvent(false);
        }
    }

    private void refreshStatusAndEvent(int statusIn, String event) {
        eventStr = event;
        status = statusIn;
    }
}
