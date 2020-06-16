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

package com.huawei.codebot.analyzer.x2y.global.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data structure that contains the type information of an expression
 *
 * @since 2019-07-14
 */
public class TypeInfo implements Serializable {
    private static final long serialVersionUID = 8351975446738277421L;
    private String qualifiedName = null;
    private List<String> generics = new ArrayList<>();

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public List<String> getGenerics() {
        return generics;
    }

    public void setGenerics(List<String> generics) {
        this.generics = generics;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(qualifiedName);
        if (!generics.isEmpty()) {
            buffer.append("<");
            buffer.append(String.join(", ", generics));
            buffer.append(">");
        }
        return buffer.toString();
    }
}
