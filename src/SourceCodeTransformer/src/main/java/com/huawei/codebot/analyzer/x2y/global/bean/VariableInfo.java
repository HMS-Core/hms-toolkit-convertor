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

/**
 * Data structure that contains the type information of variables.
 *
 * @since 2019-07-14
 */
public class VariableInfo extends EntityInfo {
    private static final long serialVersionUID = -1968510780867567359L;

    private Object declaration;

    private TypeInfo type;

    public VariableInfo() {}

    public VariableInfo(FieldInfo fieldInfo) {
        this.setName(fieldInfo.getName());
        this.setOwnerClasses(fieldInfo.getOwnerClasses());
        this.setPackageName(fieldInfo.getPackageName());
        this.setType(fieldInfo.getType());
    }

    public VariableInfo(TypeInfo typeInfo, Object declaration) {
        this.type = typeInfo;
        this.declaration = declaration;
    }

    public Object getDeclaration() {
        return declaration;
    }

    public void setDeclaration(Object declaration) {
        this.declaration = declaration;
    }

    public TypeInfo getType() {
        return type;
    }

    public void setType(TypeInfo type) {
        this.type = type;
    }
}
