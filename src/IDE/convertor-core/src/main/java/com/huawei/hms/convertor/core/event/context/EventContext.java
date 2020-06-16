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

package com.huawei.hms.convertor.core.event.context;

import com.huawei.hms.convertor.core.event.context.project.ProjectEvent;
import com.huawei.hms.convertor.core.event.context.project.ProjectEventContext;
import com.huawei.hms.convertor.openapi.result.Result;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Event context
 *
 * @since 2020-02-29
 */
@Slf4j
public final class EventContext {
    private static final EventContext EVENT_CONTEXT = new EventContext();

    private Map<String, ProjectEventContext> contexts;

    private EventContext() {
        contexts = new HashMap<>();
    }

    /**
     * Get singleton instance of {@code EventContext}
     *
     * @return The singleton instance of {@code EventContext}
     */
    public static EventContext getInstance() {
        return EVENT_CONTEXT;
    }

    /**
     * Push event to project level event queue
     *
     * @param event Project level event
     * @return Push result
     */
    public Result pushProjectEvent(ProjectEvent event) {
        String projectPath = event.getProjectPath();
        if (!contexts.containsKey(projectPath)) {
            return Result.failed("Project event context not found");
        }
        ProjectEventContext projectEventContext = contexts.get(projectPath);
        return projectEventContext.pushEvent(event);
    }

    /**
     * Startup project level event context
     *
     * @param projectPath Project base path
     */
    public void startupProjectContext(String projectPath) {
        if (contexts.containsKey(projectPath)) {
            log.warn("Project[{}] event context had been started", projectPath);
            return;
        }
        ProjectEventContext projectEventContext = new ProjectEventContext(projectPath);
        contexts.put(projectPath, projectEventContext);
        projectEventContext.startup();
    }

    /**
     * Shutdown project level event context
     *
     * @param projectPath Project base path
     */
    public void shutdownProjectContext(String projectPath) {
        if (!contexts.containsKey(projectPath)) {
            log.warn("Project[{}] event context had been stopped", projectPath);
            return;
        }
        ProjectEventContext projectEventContext = contexts.get(projectPath);
        projectEventContext.shutdown();
        contexts.remove(projectPath);
    }
}
