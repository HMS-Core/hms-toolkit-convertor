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

package com.huawei.generator.ast.custom;

import com.huawei.generator.ast.AstNode;
import com.huawei.generator.ast.AstVisitor;
import com.huawei.inquiry.docs.XDocs;

import com.google.gson.annotations.SerializedName;

/**
 * Model of javadoc field for json deserialization.
 *
 * @since 2020-06-07
 */

public class XFieldDoc extends AstNode implements XDocs {
    @SerializedName("fieldName")
    private String fieldInfo;

    @SerializedName("descriptions")
    private String descriptions;

    @SerializedName("hName")
    private String hName;

    @SerializedName("hmsInfo")
    private String hmsInfo;

    @SerializedName("gName")
    private String gName;

    @SerializedName("gmsInfo")
    private String gmsInfo;

    private String displayInfo;

    private String signature; // used in IDE

    public void setFieldName(String fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public void setHName(String hName) {
        this.hName = hName;
    }

    public void setHmsInfo(String hmsInfo) {
        this.hmsInfo = hmsInfo;
    }

    public void setGName(String gName) {
        this.gName = gName;
    }

    public void setGmsInfo(String gmsInfo) {
        this.gmsInfo = gmsInfo;
    }

    public String getFieldInfo() {
        if (fieldInfo == null || fieldInfo.isEmpty()) {
            return "";
        }
        return fieldInfo;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public String getHName() {
        return hName;
    }

    public String getHmsInfo() {
        return hmsInfo;
    }

    public String getGName() {
        return gName;
    }

    public String getGmsInfo() {
        return gmsInfo;
    }

    public void setDisplayInfo(String displayInfo) {
        this.displayInfo = displayInfo;
    }

    public String getDisplayInfo() {
        return displayInfo;
    }

    public String getDisplayHInfo() {
        return getHName() == null || getHName().isEmpty() ? ""
            : " " + getHName() + ": <a href=\"" + getHmsInfo() + "\">" + getHmsInfo() + "</a><br/>\n     *";
    }

    public String getDisplayGInfo() {
        return getGName() == null || getGName().isEmpty() ? ""
            : " " + getGName() + ": <a href=\"" + getGmsInfo() + "\">" + getGmsInfo() + "</a><br/>\n     *";
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public Class getTypeClass() {
        return XFieldDoc.class;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return signature;
    }
}
