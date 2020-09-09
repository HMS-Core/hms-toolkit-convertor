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

package com.huawei.hms.convertor.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model of classes for json deserialization.
 *
 * @since 2019-11-12
 */

public class JClass {
    @SerializedName("gName")
    private String gName;

    @SerializedName("hName")
    private String hName;

    @SerializedName("modifiers")
    private List<String> modifiers;

    @SerializedName("type")
    private String type;

    @SerializedName("interfaces")
    private List<String> interfaces;

    @SerializedName("superClass")
    private String superClass;

    @SerializedName("innerClass")
    private boolean innerClass;

    @SerializedName("methods")
    private List<JMapping<JMethod>> methods;

    @SerializedName("fields")
    private List<JMapping<JFieldOrMethod>> fields;

    @SerializedName("text")
    private String text;

    @SerializedName("url")
    private String url;

    @SerializedName("dependencyName")
    private String dependencyName;

    @SerializedName("newParams")
    private String[] newParams;

    @SerializedName("status")
    private String status;

    @SerializedName("kitName")
    private String kitName;

    public String getStatus() {
        return status;
    }

    public String gName() {
        return gName;
    }

    public String hName() {
        return hName;
    }

    public String text() {
        return text;
    }

    public String url() {
        return url;
    }

    public String[] newParams() {
        return newParams;
    }

    public String dependencyName() {
        return dependencyName;
    }

    public List<String> modifiers() {
        return modifiers;
    }

    public String type() {
        return type;
    }

    public List<String> interfaces() {
        return interfaces;
    }

    public String superClass() {
        return superClass;
    }

    public boolean isInnerClass() {
        return innerClass;
    }

    public List<JMapping<JMethod>> methods() {
        return methods;
    }

    public List<JMapping<JFieldOrMethod>> fields() {
        return fields;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }
}
