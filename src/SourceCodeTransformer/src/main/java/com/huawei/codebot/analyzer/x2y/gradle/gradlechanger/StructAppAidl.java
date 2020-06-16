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
 * struct app aidl
 *
 * @since 2020-04-01
 */
public class StructAppAidl {
    private String aidlName;

    private List<String> addImplementationInDependencies;

    private Map desc;

    public StructAppAidl() {
        setAddImplementationInDependencies(new ArrayList<>());
        setDesc(new HashMap());
    }

    /**
     * define String to store delete classpath in dependenciesName
     */
    public String getAidlName() {
        return aidlName;
    }

    public void setAidlName(String aidlName) {
        this.aidlName = aidlName;
    }

    /**
     * define List to store implementation in dependencies in app
     */
    public List<String> getAddImplementationInDependencies() {
        return addImplementationInDependencies;
    }

    public void setAddImplementationInDependencies(List<String> addImplementationInDependencies) {
        this.addImplementationInDependencies = addImplementationInDependencies;
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
