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

package com.huawei.generator.g2x.po.map;

import com.google.gson.annotations.Expose;

/**
 * Desc: used as a description container for fields and classes,
 * including:
 * 1. dependencyName -> dependency used in build.gradle as well as jar name
 * 2. gmsVersion -> version of gms
 * 3. hmsVersion -> version of hms
 * Note: when autoConvert is true, support must be true.
 *
 * @since 2019-11-27
 */
public class Desc extends ExDesc {
    @Expose
    public String dependencyName;

    @Expose
    public String gmsVersion;

    @Expose
    public String hmsVersion;

    public String getGmsVersion() {
        return gmsVersion;
    }

    public void setGmsVersion(String gmsVersion) {
        this.gmsVersion = gmsVersion;
    }

    public String getHmsVersion() {
        return hmsVersion;
    }

    public void setHmsVersion(String hmsVersion) {
        this.hmsVersion = hmsVersion;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
    }
}
