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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model of javadoc class for xms json deserialization.
 *
 * @since 2020-06-07
 */
public class XClassDoc extends AstNode implements XDocs {
    @SerializedName("gClassName")
    private String gClassName;

    @SerializedName("hClassName")
    private String hClassName;

    @SerializedName("xClassInfo")
    private String xClassInfo;

    @SerializedName("hClassInfo")
    private String hClassInfo;

    @SerializedName("gClassInfo")
    private String gClassInfo;

    @SerializedName("hClassUrl")
    private String hClassUrl;

    @SerializedName("gClassUrl")
    private String gClassUrl;

    @SerializedName("methods")
    private List<XMethodDoc> methods;

    @SerializedName("fields")
    private List<XFieldDoc> fields;

    private XClassDoc xImplClassDoc;

    private List<String> displayInfoList;

    private String signature; // used in IDE

    public Map<String, XMethodDoc> getMethods() {
        if (methods == null || methods.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        Map<String, XMethodDoc> map = new HashMap<>();
        for (XMethodDoc xMethodDoc : methods) {
            map.put(xMethodDoc.getXMethodName(), xMethodDoc);
        }
        return map;
    }

    public Map<String, XFieldDoc> getFields() {
        if (fields == null || fields.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        Map<String, XFieldDoc> map = new HashMap<>();
        for (XFieldDoc xFieldDoc : fields) {
            map.put(xFieldDoc.getSignature(), xFieldDoc);
        }
        return map;
    }

    public String getXClassInfo() {
        return xClassInfo;
    }

    public String getGClassInfo() {
        return gClassInfo;
    }

    public String getHClassInfo() {
        return hClassInfo == null ? "" : hClassInfo.trim();
    }

    public String getGClassName() {
        return gClassName == null ? "" : gClassName.trim();
    }

    public String getHClassName() {
        return hClassName == null ? "" : hClassName.trim();
    }

    public String getHClassUrl() {
        return hClassUrl == null ? "" : hClassUrl.trim();
    }

    public void setHClassUrl(String hClassUrl) {
        this.hClassUrl = hClassUrl;
    }

    public String getGClassUrl() {
        return gClassUrl == null ? "" : gClassUrl.trim();
    }

    public void setGClassUrl(String gClassUrl) {
        this.gClassUrl = gClassUrl;
    }

    public void setMethods(List<XMethodDoc> methods) {
        this.methods = methods;
    }

    public void setDisplayInfoList(List<String> displayInfoList) {
        this.displayInfoList = displayInfoList;
    }

    public List<String> getDisplayInfoList() {
        return displayInfoList;
    }

    public void setXImplClassDoc(XClassDoc xImplClassDoc) {
        this.xImplClassDoc = xImplClassDoc;
    }

    public XClassDoc getXImplClassDoc() {
        return xImplClassDoc;
    }

    public void setGClassName(String gClassName) {
        this.gClassName = gClassName;
    }

    public void setHClassName(String hClassName) {
        this.hClassName = hClassName;
    }

    public void setXClassInfo(String xClassInfo) {
        this.xClassInfo = xClassInfo;
    }

    public void setHClassInfo(String hClassInfo) {
        this.hClassInfo = hClassInfo;
    }

    public void setGClassInfo(String gClassInfo) {
        this.gClassInfo = gClassInfo;
    }

    public void setFields(List<XFieldDoc> fields) {
        this.fields = fields;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public Class getTypeClass() {
        return XClassDoc.class;
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