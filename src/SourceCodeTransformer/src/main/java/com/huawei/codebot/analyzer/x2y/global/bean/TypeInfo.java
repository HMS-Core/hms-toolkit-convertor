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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        if (StringUtils.isNotEmpty(qualifiedName)) {
            buffer.append(qualifiedName);
        }
        if (!generics.isEmpty()) {
            buffer.append("<");
            buffer.append(String.join(", ", generics));
            buffer.append(">");
        }
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        List<Object> hashArr = new ArrayList<>();
        hashArr.add(qualifiedName);
        if (CollectionUtils.isNotEmpty(generics)) {
            hashArr.addAll(generics);
        }
        return Objects.hash(hashArr.toArray());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeInfo)) {
            return false;
        }
        TypeInfo ti = (TypeInfo) obj;
        if (ti.generics.size() != this.generics.size()) {
            return false;
        }
        for (int i = 0; i < ti.generics.size(); i++) {
            if (!ti.generics.get(i).equals(this.generics.get(i))) {
                return false;
            }
        }
        if (ti.getQualifiedName() == null) {
            return this.getQualifiedName() == null;
        }
        return ti.getQualifiedName().equals(this.getQualifiedName());
    }
}
