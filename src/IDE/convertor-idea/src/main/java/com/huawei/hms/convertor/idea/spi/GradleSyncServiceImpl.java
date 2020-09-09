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

package com.huawei.hms.convertor.idea.spi;

import com.huawei.hms.convertor.core.project.convert.GradleSyncService;

import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Gradle conversion to achieve automatic synchronization project
 *
 * @since 2020-07-03
 */
@Slf4j
public class GradleSyncServiceImpl implements GradleSyncService {

    private Map<String, Project> projectMap = new HashMap<>();

    @Override
    public void sync(String projectPath) {
        Project project = projectMap.get(projectPath);
        log.info("Project[{}] start sync", project.getName());
        ExternalSystemUtil.refreshProject(project, new ProjectSystemId("GRADLE"), projectPath, false,
            ProgressExecutionMode.IN_BACKGROUND_ASYNC);
        log.info("Project[{}] sync completed", project.getName());
    }

    @Override
    public void init(String projectPath) {
        getProject(projectPath);
    }

    private void getProject(String projectPath) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects == null) {
            log.error("Can't get current project");
            return;
        }

        Arrays.stream(projects).filter(item -> projectPath.equals(item.getBasePath())).forEach(item -> {
            projectMap.put(projectPath, item);
            log.info("Get project, name: {}.", item.getName());
        });
    }
}
