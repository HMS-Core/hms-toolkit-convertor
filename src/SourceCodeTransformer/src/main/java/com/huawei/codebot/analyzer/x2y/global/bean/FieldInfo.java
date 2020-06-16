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
 * Data structure of basic information for code fields
 *
 * @since 2019-07-14
 */
public class FieldInfo extends EntityInfo {
    private static final long serialVersionUID = -1898763736930691052L;
    private TypeInfo type;
    private String accessibility;
    private String initValue;

    public TypeInfo getType() {
        return type;
    }

    public void setType(TypeInfo type) {
        this.type = type;
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
        result = 31 * result + this.getType().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FieldInfo)) {
            return false;
        }
        FieldInfo fi = (FieldInfo) obj;

        if (!fi.getType().equals(this.getType())) {
            return false;
        }

        if (fi.getQualifiedName() == null) {
            return this.getQualifiedName() == null;
        }
        return fi.getQualifiedName().equals(this.getQualifiedName());
    }

    public String getInitValue() {
        return initValue;
    }

    public void setInitValue(String initValue) {
        this.initValue = initValue;
    }

    @Override
    public String toString() {
        return this.getQualifiedName();
    }
}
