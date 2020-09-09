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

package com.huawei.inquiry.docs;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;

/**
 * Model of javadoc class for gms or hms json deserialization.
 *
 * @since 2020-07-25
 */
public class ZClassDoc implements ZDocs {
    @SerializedName("classDes")
    private String classDes;

    @SerializedName("classUrl")
    private String classUrl;

    @SerializedName("methods")
    private Map<String, ZMethodDoc> methods; // key is methodSignature

    @SerializedName("fields")
    private Map<String, ZFieldDoc> fields;

    private String signature;

    public Map<String, ZFieldDoc> getFields() {
        if (fields == null || fields.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        return fields;
    }

    public Map<String, ZMethodDoc> getMethods() {
        if (methods == null || methods.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        return methods;
    }

    public String getClassDes() {
        return classDes;
    }

    public String getClassUrl() {
        return classUrl;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public Class getTypeClass() {
        return ZClassDoc.class;
    }

    @Override
    public String toString() {
        return signature;
    }
}