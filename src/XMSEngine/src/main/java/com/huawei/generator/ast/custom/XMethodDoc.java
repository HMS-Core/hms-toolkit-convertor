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
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.inquiry.docs.Struct;
import com.huawei.inquiry.docs.XDocs;
import com.huawei.inquiry.utils.MethodDocUtil;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Model of javadoc methods for json deserialization.
 *
 * @since 2020-06-07
 */
public class XMethodDoc extends AstNode implements XDocs {
    @SerializedName("methodName")
    private String xMethodName;

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

    @SerializedName("params")
    private List<Map<String, String>> params;

    @SerializedName("retType")
    private String retType;

    @SerializedName("retDescription")
    private String retDescription;

    @SerializedName("throwExceptions")
    private List<Map<String, String>> exceptions;

    private List<String> displayInfoList;

    private String signature;

    private Map<String, Struct> paramsForIDE; // key is paramName like "param0"

    private List<Struct> exceptionsForIDE;

    private Struct returnForIDE;

    public String getRetType() {
        return retType == null ? "" : retType.trim();
    }

    public void setRetType(String retType) {
        this.retType = retType;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public void setxMethodName(String xMethodName) {
        this.xMethodName = xMethodName;
    }

    public void setParams(List<Map<String, String>> params) {
        this.params = params;
    }

    public void setRetDescription(String retDescription) {
        this.retDescription = retDescription;
    }

    public void setExceptions(List<Map<String, String>> exceptions) {
        this.exceptions = exceptions;
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

    public String getRetDescription() {
        return retDescription == null ? "" : retDescription.trim();
    }

    public List<Map<String, String>> getExceptions() {
        return exceptions;
    }

    public String getXMethodName() {
        return xMethodName;
    }

    public String getGName() {
        return gName == null ? "" : gName.trim();
    }

    public String getHName() {
        return hName == null ? "" : hName.trim();
    }

    public String getDescriptions() {
        return descriptions == null ? "" : descriptions.trim();
    }

    public String getHmsInfo() {
        return hmsInfo == null ? "" : hmsInfo.trim();
    }

    public String getGmsInfo() {
        return gmsInfo == null ? "" : gmsInfo.trim();
    }

    public List<Map<String, String>> getParams() {
        return params;
    }

    public void setDisplayInfoList(List<String> displayInfoList) {
        this.displayInfoList = displayInfoList;
    }

    public List<String> getDisplayInfoList() {
        return displayInfoList;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public void settingsForIDE() {
        setParamsForIDE();
        setReturnForIDE();
        setExceptionsForIDE();
    }

    public Map<String, Struct> getParamsForIDE() {
        return paramsForIDE;
    }

    public List<Struct> getExceptionsForIDE() {
        return exceptionsForIDE;
    }

    public Struct getReturnForIDE() {
        return returnForIDE;
    }

    private void setParamsForIDE() {
        Map<String, String> paramsNameAndDescription = getParamsNameAndDescription();
        paramsForIDE = MethodDocUtil.getParamsForIDE(paramsNameAndDescription, xMethodName);
    }

    private void setExceptionsForIDE() {
        Map<String, String> exceptionNameAndDescription = getExceptionNameAndDescription();
        exceptionsForIDE = MethodDocUtil.getExceptionsForIDE(exceptionNameAndDescription);
    }

    private void setReturnForIDE() {
        returnForIDE = MethodDocUtil.getReturnForIDE(retType, retDescription);
    }

    private Map<String, String> getParamsNameAndDescription() {
        if (params == null || params.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        Map<String, String> paramsNameAndDescription = new LinkedHashMap<>();
        for (Map<String, String> param : params) {
            paramsNameAndDescription.put(param.get("name"), param.get("descriptions"));
        }
        return paramsNameAndDescription;
    }

    private Map<String, String> getExceptionNameAndDescription() {
        if (exceptions == null || exceptions.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        Map<String, String> exceptionNameAndDescription = new LinkedHashMap<>();
        for (Map<String, String> param : exceptions) {
            exceptionNameAndDescription.put(param.get("name"), param.get("descriptions"));
        }
        return exceptionNameAndDescription;
    }

    // judge whether two methods are the same
    public boolean isSameMethod(MethodNode methodNode) {
        return methodNameIsSame(methodNode) && paramsTypeIsSame(methodNode);
    }

    // judge whether names of methodDoc and methodNode are the same
    private boolean methodNameIsSame(MethodNode methodNode) {
        if (xMethodName == null || xMethodName.isEmpty()) {
            return false;
        }
        String noParams = xMethodName.substring(0, xMethodName.indexOf("("));
        int start = noParams.lastIndexOf(".");
        String fullClassName = noParams.substring(0, start);
        String simpleMethodName = noParams.substring(start + 1);

        ClassNode classNode = methodNode.parent();
        ClassNode outerClass = classNode.outerClass();
        String fullName = TypeNode.create(classNode.fullName()).getTypeName();
        if (classNode.isInner() && outerClass != null) {
            fullName = TypeNode.create(outerClass.fullName()).getTypeName() + "." + classNode.shortName();
        }

        if (!fullClassName.equals(fullName)) {
            return false;
        }
        return simpleMethodName.equals(methodNode.name());
    }

    // judge whether two list namely docMethodParams and methodNodeParams are the same
    private boolean paramsTypeIsSame(MethodNode methodNode) {
        List<String> docMethodParams = MethodDocUtil.getParamsType(xMethodName);
        List<TypeNode> methodNodeParams = methodNode.parameters();

        if (docMethodParams.size() != methodNodeParams.size()) {
            return false;
        }
        for (int i = 0; i < docMethodParams.size(); i++) {
            TypeNode nodeParam = methodNodeParams.get(i);
            String nodeParamStr = methodNodeParamName(nodeParam);
            if (!docMethodParams.get(i).equals(nodeParamStr)) {
                return false;
            }
        }
        return true;
    }

    // get name info of the parameter in the methodNode
    private String methodNodeParamName(TypeNode typeNode) {
        if (typeNode.isArray()) {
            return typeNode.toString();
        } else {
            return typeNode.getTypeName();
        }
    }

    @Override
    public Class getTypeClass() {
        return XMethodDoc.class;
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