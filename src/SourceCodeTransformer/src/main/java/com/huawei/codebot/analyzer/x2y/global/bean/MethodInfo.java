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

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure of basic information for code methods
 *
 * @since 2019-07-14
 */
public class MethodInfo extends EntityInfo {
    private static final long serialVersionUID = -4767379401522906987L;
    private TypeInfo returnType;
    private List<TypeInfo> paramTypes = new ArrayList<>();
    private String accessibility;

    public TypeInfo getReturnType() {
        return returnType;
    }

    public void setReturnType(TypeInfo returnType) {
        this.returnType = returnType;
    }

    public List<TypeInfo> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<TypeInfo> paramTypes) {
        this.paramTypes = paramTypes;
    }

    public String getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(String accessibility) {
        this.accessibility = accessibility;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.getQualifiedName().hashCode();
        result = 31 * result + this.getReturnType().hashCode();
        for (TypeInfo paramType : getParamTypes()) {
            result = 31 * result + paramType.getQualifiedName().hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodInfo)) {
            return false;
        }
        MethodInfo mi = (MethodInfo) obj;

        if (!mi.getReturnType().equals(this.getReturnType())) {
            return false;
        }

        if (mi.getParamTypes().size() != this.getParamTypes().size()) {
            return false;
        }

        for (int i = 0; i < this.getParamTypes().size(); i++) {
            if (!mi.getParamTypes().get(i).equals(this.getParamTypes().get(i))) {
                return false;
            }
        }

        if (mi.getQualifiedName() == null) {
            return this.getQualifiedName() == null;
        }
        return mi.getQualifiedName().equals(this.getQualifiedName());
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getReturnType());
        buffer.append(" ");
        buffer.append(this.getQualifiedName());
        buffer.append("(");
        for (int i = 0; i < this.getParamTypes().size(); i++) {
            buffer.append(this.getParamTypes().get(i));
            if (i != this.getParamTypes().size() - 1) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
        return buffer.toString();
    }
}
