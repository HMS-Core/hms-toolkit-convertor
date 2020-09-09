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

import com.huawei.inquiry.utils.MethodDocUtil;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class ZMethodDoc implements ZDocs {
    @SerializedName("methodUrl")
    private String methodUrl;

    @SerializedName("methodDes")
    private String methodDes;

    @SerializedName("params")
    private Map<String, String> params;

    @SerializedName("returns")
    private String returns;

    @SerializedName("Exceptions")
    private Map<String, String> exceptions;

    private String signature;

    private Map<String, Struct> paramsForIDE; // key is paramName like "param0"

    private List<Struct> exceptionsForIDE;

    private Struct returnForIDE;

    public Map<String, String> getExceptions() {
        return exceptions;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getMethodDes() {
        return methodDes;
    }

    public String getMethodUrl() {
        return methodUrl;
    }

    public String getReturns() {
        return returns;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public void settingForIDE() {
        setParamsForIDE();
        setExceptionsForIDE();
        setReturnForIDE();
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
        paramsForIDE = MethodDocUtil.getParamsForIDE(params, signature);
    }

    private void setExceptionsForIDE() {
        exceptionsForIDE = MethodDocUtil.getExceptionsForIDE(exceptions);
    }

    private void setReturnForIDE() {
        if (returns == null) {
            return;
        }
        if (returns.equals("void") || returns.equals("")) {
            returnForIDE = new Struct(returns, "", false);
            return;
        }
        if (!returns.contains(":")) {
            return;
        }
        int index = returns.indexOf(":");
        String retType = returns.substring(0, index);
        String retDescription = returns.substring(index + 1);
        returnForIDE = MethodDocUtil.getReturnForIDE(retType, retDescription);
    }

    @Override
    public Class getTypeClass() {
        return ZMethodDoc.class;
    }

    @Override
    public String toString() {
        return signature;
    }
}
