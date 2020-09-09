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

package com.huawei.generator.g2x.po.summary;

import com.google.gson.annotations.Expose;

import java.util.TreeMap;

/**
 * Class for kit infos
 *
 * @since 2019-02-20
 */
public class KitInfo {
    @Expose
    private String kitName;

    @Expose
    private TreeMap<String, DependencyInfo> dependencyInfos = new TreeMap<>();

    public KitInfo(String kitName) {
        this.kitName = kitName;
    }

    String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    public TreeMap<String, DependencyInfo> getDependencyInfos() {
        return dependencyInfos;
    }

    public void setDependencyInfos(TreeMap<String, DependencyInfo> dependencyInfos) {
        this.dependencyInfos = dependencyInfos;
    }
}
