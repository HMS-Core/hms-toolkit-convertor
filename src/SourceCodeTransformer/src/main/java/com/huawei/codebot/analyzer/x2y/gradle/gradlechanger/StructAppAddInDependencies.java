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
 * Struct app add in dependencies
 *
 * @since 2020-04-01
 */
public class StructAppAddInDependencies {
    private String version;

    private String originGoogleName;

    private List<String> dependencies;

    private Map descAuto;

    private Map descManual;

    public StructAppAddInDependencies() {
        setDependencies(new ArrayList<>());
        this.setDescAuto(new HashMap());
        this.setDescManual(new HashMap());
    }

    /**
     * define String to store version
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
     * define List to store dependencies
     */
    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * define Map store desc from json
     * descAuto is autoFix
     */
    public Map getDescAuto() {
        return descAuto;
    }

    public void setDescAuto(Map desc_auto) {
        this.descAuto = desc_auto;
    }

    /**
     * define Map store desc from json
     * descManual is delete
     */
    public Map getDescManual() {
        return descManual;
    }

    public void setDescManual(Map desc_manual) {
        this.descManual = desc_manual;
    }
}
