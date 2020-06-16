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

package com.huawei.generator.json;

import com.google.gson.annotations.SerializedName;

/**
 * JParameter class
 * 
 * @since 2019-11-14
 */
public class JParameter {
    @SerializedName("pos")
    private int pos;

    @SerializedName("type")
    private String type;

    public int pos() {
        return pos;
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JParameter deepCopy() {
        JParameter jParameter = new JParameter();
        jParameter.pos = this.pos;
        jParameter.type = this.type;
        return jParameter;
    }
}
