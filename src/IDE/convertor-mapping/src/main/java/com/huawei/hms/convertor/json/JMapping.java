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

/**
 * Model of G<->H mapping for json deserialization.
 *
 * @since 2019-11-12
 */

public class JMapping<T> {

    private static final String STATUS_TO_BE_IMPLEMENTED = "toBeImplemented";

    private static final String STATUS_COMPLEX = "complex";

    private static final String STATUS_MANUALLY_ADAPT = "manuallyAdapt";

    private static final String STATUS_MATCHING = "matching";

    private static final String STATUS_REDUNDANT = "redundant";

    private static final String STATUS_NOT_SUPPORT = "notSupport";

    private static final String STATUS_ONE_TO_MANY = "oneToMany";

    private static final String STATUS_SPECIAL = "SpecialStatus";

    @SerializedName("h")
    private T h;

    @SerializedName("g")
    private T g;

    @SerializedName("correspondingStatus")
    private String status;

    @SerializedName("hName")
    private String hName;

    @SerializedName("text")
    private String text;

    @SerializedName("url")
    private String url;

    @SerializedName("dependencyName")
    private String dependencyName;

    @SerializedName("newParams")
    private String[] newParams;

    @SerializedName("oldIndex")
    private String[] oldIndex;

    public T h() {
        return h;
    }

    public T g() {
        return g;
    }

    public String status() {
        return status;
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

    public String dependencyName() {
        return dependencyName;
    }

    public String[] newParams() {
        return newParams;
    }

    public String[] oldIndex() {
        return oldIndex;
    }

    public boolean isMatching() {
        return lowerCaseEqual(JMapping.STATUS_MATCHING, status);
    }

    public boolean isManuallyAdapt() {
        return lowerCaseEqual(JMapping.STATUS_MANUALLY_ADAPT, status);
    }

    public boolean isRedundant() {
        return lowerCaseEqual(JMapping.STATUS_REDUNDANT, status);
    }

    public boolean isComplex() {
        return lowerCaseEqual(JMapping.STATUS_COMPLEX, status);
    }

    public boolean isNotSupport() {
        return lowerCaseEqual(JMapping.STATUS_NOT_SUPPORT, status);
    }

    public boolean isSpecial() {
        return lowerCaseEqual(JMapping.STATUS_SPECIAL, status);
    }

    public void setH(T h) {
        this.h = h;
    }

    public void setG(T g) {
        this.g = g;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
    }

    private boolean lowerCaseEqual(String str1, String str2) {
        return str1.toLowerCase().equals(str2.toLowerCase());
    }
}
