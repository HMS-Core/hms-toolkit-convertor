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

package com.huawei.codebot.analyzer.x2y.java.method.replace;

/**
 * an entity used to represent new parameters of changed method
 *
 * @since 2020-04-13
 */
public class NewParam {
    private String newParamValue;
    private String newParamType;
    private String oldParamIndex;

    /**
     * value of this parameter
     */
    public String getNewParamValue() {
        return newParamValue;
    }

    void setNewParamValue(String newParamValue) {
        this.newParamValue = newParamValue;
    }

    /**
     * type of this parameter
     */
    public String getNewParamType() {
        return newParamType;
    }

    public void setNewParamType(String newParamType) {
        this.newParamType = newParamType;
    }

    /**
     * index of this parameter
     */
    public String getOldParamIndex() {
        return oldParamIndex;
    }

    public void setOldParamIndex(String oldParamIndex) {
        this.oldParamIndex = oldParamIndex;
    }
}
