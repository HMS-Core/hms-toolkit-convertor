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

package com.huawei.generator.g2x.po.map.manual;

import com.huawei.generator.g2x.po.map.MethodDesc;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * ManualField: description of manual-methods, indicating wishhub plugins can not modify them directly.
 * Therefore app client developers should change them manually.
 * <p>
 * including:
 * 1. methodName means method name
 * 2. desc means details of this method, see {@link MethodDesc}
 * 3. paramTypes means canonical types of parameters
 * 4. weakTypes means weakTypes for overloaded methods with same size of params,
 * we will fulfill with "*" for source code engine. Others will be same with paramTypes
 * </p>
 * <p>
 * Note: for manual field, desc is very important, because it contains the user manual and url to guide
 * developers.
 * </p>
 *
 * @since 2019-11-27
 */
public class ManualMethod {
    @Expose
    private String methodName;

    @Expose
    private List<String> paramTypes;

    @Expose
    private List<String> weakTypes;

    @Expose
    private MethodDesc desc;

    public ManualMethod(String methodName, List<String> paramTypes, List<String> weakTypes, MethodDesc desc) {
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.weakTypes = weakTypes;
        this.desc = desc;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public MethodDesc getDesc() {
        return desc;
    }

    public List<String> getWeakTypes() {
        return weakTypes;
    }

    public void setWeakTypes(List<String> weakTypes) {
        this.weakTypes = weakTypes;
    }

    public void setMDesc(MethodDesc desc) {
        this.desc = desc;
    }
}
