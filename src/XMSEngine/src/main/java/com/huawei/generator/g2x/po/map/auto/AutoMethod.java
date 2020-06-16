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

package com.huawei.generator.g2x.po.map.auto;

import com.google.gson.annotations.Expose;
import com.huawei.generator.g2x.po.map.MDesc;

import java.util.List;

/**
 * AutoField: description of auto-methods, indicating wishhub plugins can modified them directly.
 * Such as G.ClassG.MethodG can be replaced by X.ClassX.MethodX, directly.
 * These fields will be put into wisehub-auto.json#autoMethods.
 * including:
 * 1. oldMethodName -> old method name - g
 * 2. newMethodName -> new method name - x
 * 3. paramTypes -> canonical types of parameters
 * 4. weakTypes -> weakTypes for overloaded methods with same size of params,
 * we will fulfill with "*" for source code engine. Others will be same with paramTypes
 * 5. desc -> details of this method, see {@link MDesc}
 * Note: For auto method, the following fields of desc should be filled by us according to a default value:
 * 1. desc'text: which will be replaced by which
 * 2. desc'methodName: should be build by ourselves in format of canonical types. But for methods
 *
 * @since 2019-11-27
 */
public class AutoMethod {
    @Expose
    private String oldMethodName;

    @Expose
    private String newMethodName;

    @Expose
    private List<String> paramTypes;

    @Expose
    private List<String> weakTypes;

    @Expose
    private MDesc desc;

    // preserved fields for matching methods pair, which have different parameter-length.
    // currently, g and x have the same length
    public AutoMethod(String oldMethodName, String newMethodName, MDesc desc, List<String> paramTypes,
        List<String> weakTypes) {
        this.oldMethodName = oldMethodName;
        this.newMethodName = newMethodName;
        this.paramTypes = paramTypes;
        this.weakTypes = weakTypes;
        this.desc = desc;
    }

    public AutoMethod() {
    }

    public String getOldMethodName() {
        return oldMethodName;
    }

    public String getNewMethodName() {
        return newMethodName;
    }

    public MDesc getDesc() {
        return desc;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public void setOldMethodName(String oldMethodName) {
        this.oldMethodName = oldMethodName;
    }

    public void setNewMethodName(String newMethodName) {
        this.newMethodName = newMethodName;
    }

    public void setParamTypes(List<String> paramTypes) {
        this.paramTypes = paramTypes;
    }

    public void setMDesc(MDesc desc) {
        this.desc = desc;
    }

    public List<String> getWeakTypes() {
        return weakTypes;
    }

    public void setWeakTypes(List<String> weakTypes) {
        this.weakTypes = weakTypes;
    }
}
