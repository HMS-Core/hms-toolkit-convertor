/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Get the system configuration of the scan project, such as the JDK version
 * 
 * @since 3.0.0
 */
public class GradleProjectInfo {
    private static final List<String> PROJECT_INFO_RANGE = new ArrayList<>(Arrays.asList("sourceCompatibility"));
    private Map<String, String> projectInfoMap;

    public GradleProjectInfo() {
        projectInfoMap = new HashMap<>();
        for (String info : PROJECT_INFO_RANGE) {
            projectInfoMap.put(info, "");
        }
    }

    public Map<String, String> getProjectInfoMap() {
        return projectInfoMap;
    }

    public void addProjectInfo(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        if (PROJECT_INFO_RANGE.contains(key)) {
            projectInfoMap.put(key, value);
        }
    }
}
