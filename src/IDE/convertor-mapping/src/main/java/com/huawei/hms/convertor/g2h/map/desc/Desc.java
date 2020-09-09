/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.g2h.map.desc;

import com.google.gson.annotations.Expose;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class Desc {
    @Expose()
    private String text;

    @Expose()
    private String url;

    @Expose()
    private String kit;

    @Expose()
    private String dependencyName;

    @Expose()
    private String gmsVersion;

    @Expose()
    private String hmsVersion;

    @Expose()
    private String status;

    @Expose()
    private boolean support;

    @Expose()
    private String type;

    public String getType() {
        return type;
    }

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

    public boolean isSupport() {
        return support;
    }

    public void setKit(String kit) {
        this.kit = kit;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
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

    public String isAutoConvert() {
        return status;
    }

    /**
     * set real name for a class, method or field
     *
     * @param name real name for a class, method or field，like com.xxx.ggg.Foo.foo
     */
    public abstract void setName(String name);

    /**
     * get real name for a class, method or field
     *
     * @return real name for a class, method or field
     */
    public abstract String getName();
}
