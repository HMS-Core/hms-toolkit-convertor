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

import java.util.HashMap;
import java.util.Map;

/**
 * struct delete classpath in dependencies
 *
 * @since 2020-04-01
 */
public class StructDeleteClasspathInDependencies {
    private String deleteClasspathInDependenciesName;

    private Map desc;

    public StructDeleteClasspathInDependencies() {
        setDesc(new HashMap());
    }

    /**
     * define String store delete classpath in dependenciesName
     */
    public String getDeleteClasspathInDependenciesName() {
        return deleteClasspathInDependenciesName;
    }

    public void setDeleteClasspathInDependenciesName(String deleteClasspathInDependenciesName) {
        this.deleteClasspathInDependenciesName = deleteClasspathInDependenciesName;
    }

    /**
     * define Map store desc from json
     */
    public Map getDesc() {
        return desc;
    }

    public void setDesc(Map desc) {
        this.desc = desc;
    }
}
