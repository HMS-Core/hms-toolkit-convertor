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

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Struct add in direct dependencies in app
 *
 * @since 2020-04-01
 */
public class StructAppAddIndirectDependencies {
    private String originGoogleName;

    private List<String> dependencies;

    private Map desc;

    public StructAppAddIndirectDependencies() {
        setDependencies(new ArrayList<>());
        setDesc(new HashMap());
    }

    /**
     * define String to store origin GoogleName
     */
    public String getOriginGoogleName() {
        return originGoogleName;
    }

    public void setOriginGoogleName(String originGoogleName) {
        this.originGoogleName = originGoogleName;
    }

    /**
     * define List to store dependencies from json
     */
    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public Map getDesc() {
        return desc;
    }

    public void setDesc(Map desc) {
        this.desc = desc;
    }
}
