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

package com.huawei.hms.convertor.g2h.map.manual;

import com.huawei.hms.convertor.g2h.map.desc.Desc;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ManualMethod {
    @Expose
    private String methodName;

    @Expose
    private List<String> paramTypes;

    @Expose
    private List<String> weakTypes;

    @Expose
    private Desc desc;

    public ManualMethod(String methodName, List<String> paramTypes, List<String> weakParamTypes, Desc desc) {
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.desc = desc;
        weakTypes = weakParamTypes;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public Desc getDesc() {
        return desc;
    }

}
