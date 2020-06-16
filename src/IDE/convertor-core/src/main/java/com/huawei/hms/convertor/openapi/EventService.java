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

package com.huawei.hms.convertor.openapi;

import com.huawei.hms.convertor.core.event.context.EventContext;
import com.huawei.hms.convertor.core.event.context.project.ProjectEvent;
import com.huawei.hms.convertor.openapi.result.Result;

/**
 * Event service
 *
 * @since 2020-02-24
 */
public final class EventService {
    private static final EventService EVENT_SUBMIT_SERVICE = new EventService();

    private EventService() {
    }

    /**
     * Get singleton instance of {@code EventService}
     *
     * @return The singleton instance of {@code EventService}
     */
    public static EventService getInstance() {
        return EVENT_SUBMIT_SERVICE;
    }

    /**
     * Startup project level event context
     *
     * @param projectPath Project base path
     */
    public void startupProjectEventContext(String projectPath) {
        EventContext.getInstance().startupProjectContext(projectPath);
    }

    /**
     * Shutdown project level event context
     *
     * @param projectPath Project base path
     */
    public void shutdownProjectEventContext(String projectPath) {
        EventContext.getInstance().shutdownProjectContext(projectPath);
    }

    /**
     * Submit project level event
     *
     * @param event Project level event
     * @return Submit result
     */
    public Result submitProjectEvent(ProjectEvent event) {
        return EventContext.getInstance().pushProjectEvent(event);
    }
}
