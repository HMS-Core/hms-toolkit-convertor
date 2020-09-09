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
 * Class for Dependency Infos
 *
 * @since 2019-02-20
 */
public class DependencyInfo {
    @Expose
    public String dependencyName;

    @Expose
    public String hmsVersion;

    @Expose
    public String gmsVersion;

    @Expose
    public TreeMap<String, FileInfo> fileInfoMap = new TreeMap<>();

    public DependencyInfo(String dependencyName, String hmsVersion, String gmsVersion) {
        this.dependencyName = dependencyName;
        this.hmsVersion = hmsVersion;
        this.gmsVersion = gmsVersion;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
    }

    public String getHmsVersion() {
        return hmsVersion;
    }

    public void setHmsVersion(String hmsVersion) {
        this.hmsVersion = hmsVersion;
    }

    public String getGmsVersion() {
        return gmsVersion;
    }

    public void setGmsVersion(String gmsVersion) {
        this.gmsVersion = gmsVersion;
    }

    public TreeMap<String, FileInfo> getFileInfoMap() {
        return fileInfoMap;
    }

    public void setFileInfoMap(TreeMap<String, FileInfo> fileInfoMap) {
        this.fileInfoMap = fileInfoMap;
    }

}
