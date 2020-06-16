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

package com.huawei.codebot.analyzer.x2y.java.method;

import java.util.List;
import java.util.Map;

/**
 * a base pattern class that holds information of changed method needs
 *
 * @since 2020-04-14
 */
public abstract class MethodChangePattern {
    private Map desc;

    private List<String> paramTypes;

    private List<String> weakTypes;

    private List<String> paramValues;

    /**
     * need to be implement in concrete class cause different types of change may have different implementation
     *
     * @return qualified name of method before change
     */
    public abstract String getOldMethodName();

    /**
     * need to be implement in concrete class cause different types of change may have different implementation
     *
     * @return qualified name of method after change
     */
    public abstract String getNewMethodName();

    /**
     * define desc
     */
    public Map getDesc() {
        return desc;
    }

    public void setDesc(Map desc) {
        this.desc = desc;
    }

    /**
     * define paramTypes
     */
    public List<String> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<String> paramTypes) {
        this.paramTypes = paramTypes;
    }

    /**
     * define weakTypes
     */
    public List<String> getWeakTypes() {
        return weakTypes;
    }

    public void setWeakTypes(List<String> weakTypes) {
        this.weakTypes = weakTypes;
    }

    /**
     * define paramValues
     */
    public List<String> getParamValues() {
        return paramValues;
    }

    public void setParamValues(List<String> paramValues) {
        this.paramValues = paramValues;
    }
}
