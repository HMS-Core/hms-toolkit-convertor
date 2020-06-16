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
import com.huawei.hms.convertor.core.event.handler.project.ConvertEventHandler;
import com.huawei.hms.convertor.core.event.handler.project.EditEventHandler;
import com.huawei.hms.convertor.core.event.handler.project.ProjectEventHandler;
import com.huawei.hms.convertor.core.event.handler.project.RecoveryEventHandler;
import com.huawei.hms.convertor.core.event.handler.project.RevertEventHandler;
import com.huawei.hms.convertor.core.event.handler.project.SaveAllEventHandler;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

/**
 * Event context
 *
 * @since 2020-02-29
 */
@Slf4j
public final class ProjectEventContext {
    /**
     * Root project folder name
     */
    private String projectName;

    private String projectPath;

    private ProjectEventQueue projectEventQueue;

    private ProjectEventHandler projectEventHandler;

    private ProjectEventListener projectEventListener;

    private ProjectEventConsumer projectEventConsumer;

    /**
     * Create project level event context
     *
     * @param projectPath Project base path
     */
    public ProjectEventContext(String projectPath) {
        this.projectPath = projectPath;
        projectName = StringUtils.substring(projectPath, StringUtils.lastIndexOf(projectPath, Constant.SEPARATOR) + 1);
        projectEventQueue = new ProjectEventQueue();
        projectEventHandler = new ProjectEventHandler(projectName);
        projectEventListener = new ProjectEventListener(projectName, projectEventQueue, projectEventHandler);
        projectEventConsumer = new ProjectEventConsumer(projectName, projectEventQueue, projectEventHandler);
    }

    /**
     * Push event to project level event queue
     *
     * @param event Project level event
     * @return Push result
     */
    public Result pushEvent(ProjectEvent event) {
        return projectEventQueue.push(event);
    }

    /**
     * Register event handler and startup event context
     */
    public void startup() {
        // Register project level event handler
        projectEventHandler.registerHandler(EventType.EDIT_EVENT, new EditEventHandler(projectPath));
        projectEventHandler.registerHandler(EventType.CONVERT_EVENT, new ConvertEventHandler(projectPath));
        projectEventHandler.registerHandler(EventType.REVERT_EVENT, new RevertEventHandler(projectPath));
        projectEventHandler.registerHandler(EventType.SAVE_ALL_EVENT, new SaveAllEventHandler());
        projectEventHandler.registerHandler(EventType.RECOVERY_EVENT, new RecoveryEventHandler());
        // Startup project level event listener
        projectEventListener.startup();

        // Startup project level event consumer
        projectEventConsumer.startup();

        log.info("Project[{}] event context started successfully", projectName);
    }

    /**
     * Clear event handler and shutdown event context
     */
    public void shutdown() {
        // Shutdown project level event listener
        projectEventListener.shutdown();

        // Shutdown project level event consumer
        projectEventConsumer.shutdown();

        // Shutdown project level event handler
        projectEventHandler.shutdown();

        log.info("Project[{}] event context stopped successfully", projectName);
    }
}
