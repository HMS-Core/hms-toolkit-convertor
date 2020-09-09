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

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Struct Delete Classpath In Repositories
 *
 * @since 2020-04-01
 */
public class StructDeleteClasspathInRepositories {
    private String deleteClasspathInRepositoriesName;

    private Map desc;

    public StructDeleteClasspathInRepositories() {
        setDesc(new HashMap());
    }

    /**
     * define String store delete classpath in repositoriesName
     */
    public String getDeleteClasspathInRepositoriesName() {
        return deleteClasspathInRepositoriesName;
    }

    public void setDeleteClasspathInRepositoriesName(String deleteClasspathInRepositoriesName) {
        this.deleteClasspathInRepositoriesName = deleteClasspathInRepositoriesName;
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
