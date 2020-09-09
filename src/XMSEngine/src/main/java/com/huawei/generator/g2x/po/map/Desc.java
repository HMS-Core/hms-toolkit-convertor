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
 * <p>
 * including:
 * 1. text means literal description for this field or class
 * 2. url means url of the reference
 * 3. kit means kit name, which is designed by us, such as Basement, Push, Location
 * 4. dependencyName means dependency used in build.gradle as well as jar name
 * 5. gmsVersion means version of gms
 * 6. hmsVersion means version of hms
 * 7. autoConvert means that it can be auto modified by wisehub plugins, true for auto, false for manual
 * 8. support means that in manual, whether hms provides the functionality that app developer can build by themselves
 * </p>
 * <p>
 * Note: when autoConvert is true, support must be true.
 * </p>
 *
 * @since 2019-11-27
 */
public abstract class Desc {
    @Expose()
    public String text;

    @Expose()
    public String url;

    @Expose()
    public String kit;

    @Expose()
    public String dependencyName;

    @Expose()
    public String gmsVersion;

    @Expose()
    public String hmsVersion;

    @Expose()
    public String status;

    @Expose()
    public boolean support;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKit() {
        return kit;
    }

    public void setKit(String kit) {
        this.kit = kit;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSupport() {
        return support;
    }

    public void setSupport(boolean support) {
        this.support = support;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
    }

    /**
     * set real name for a class, method or field
     * 
     * @param name real name for a class, method or field，like com.xxx.ggg.Foo.foo
     */
    public abstract void setName(String name);
}
