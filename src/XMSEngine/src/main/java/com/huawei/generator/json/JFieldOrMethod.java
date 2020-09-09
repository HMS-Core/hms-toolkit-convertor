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

import java.util.List;

/**
 * Model of field or method for json deserialization.
 *
 * @since 2019-11-15
 */
public class JFieldOrMethod {
    @SerializedName("name")
    private String name;

    @SerializedName("modifiers")
    private List<String> modifiers;

    // JMethod only
    @SerializedName("exceptions")
    private List<String> exceptions;

    // JMethod only
    @SerializedName("paramTypes")
    private List<JParameter> parameterTypes;

    // JMethod only
    @SerializedName("retType")
    private String returnType;

    // JField only
    @SerializedName("type")
    private String type;

    // JField only
    @SerializedName("value")
    private String value;

    /**
     * JFieldOrMethod constructor
     *
     * @param name JFieldOrMethod name
     * @param type JFieldOrMethod type
     * @param value JFieldOrMethod value
     * @param modifiers list of JFieldOrMethod modifiers
     */
    public JFieldOrMethod(String name, String type, String value, List<String> modifiers) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.modifiers = modifiers;
    }

    public boolean isJField() {
        return type != null;
    }

    public boolean isJMethod() {
        return !isJField();
    }

    public JField asJField() {
        if (!isJField()) {
            throw new IllegalStateException(name + "  type cast error: not a JField");
        }
        JField jField = new JField();
        jField.name = name;
        jField.modifiers = modifiers;
        jField.type = type;
        jField.value = value;
        return jField;
    }

    public JMethod asJMethod() {
        if (!isJMethod()) {
            throw new IllegalStateException("type cast error: not a isJMethod");
        }
        JMethod jMethod = new JMethod();
        jMethod.name = name;
        jMethod.modifiers = modifiers;
        jMethod.exceptions = exceptions;
        jMethod.parameterTypes = parameterTypes;
        jMethod.returnType = returnType;
        return jMethod;
    }
}
