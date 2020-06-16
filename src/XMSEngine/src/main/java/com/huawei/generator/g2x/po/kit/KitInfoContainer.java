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

package com.huawei.generator.g2x.po.kit;

import com.google.gson.annotations.Expose;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * container of all kinds of kit-related values
 * 1. supported kits: used for kit validator and provide xms-code generation capability to IDE
 * 2. display-info: key is used for generator, value for ide display as well as mapping-file
 * 3. dependency: kit dependencies for xms generation
 *
 * @since 2020-02-27
 */
public class KitInfoContainer {
    @Expose
    private List<String> supportedList = new LinkedList<>();

    @Expose
    private Map<String, String> displayMap = new TreeMap<>();

    @Expose
    private Map<String, List<String>> dependencyMap = new TreeMap<>();

    @Expose
    private Map<String, Map<String, Integer>> defaultSdkVersion = new TreeMap<>();

    public KitInfoContainer() {
    }

    public List<String> getSupported() {
        return supportedList;
    }

    public void setSupported(List<String> supportedList) {
        this.supportedList = supportedList;
    }

    public Map<String, String> getDisplay() {
        return displayMap;
    }

    public void setDisplay(Map<String, String> display) {
        this.displayMap = display;
    }

    public Map<String, List<String>> getDependency() {
        return dependencyMap;
    }

    public void setDependency(Map<String, List<String>> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    public Map<String, Map<String, Integer>> getDefaultSdkVersion() {
        return defaultSdkVersion;
    }
}
