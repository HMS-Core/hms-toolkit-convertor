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

package com.huawei.codebot.analyzer.x2y.java.field;

import org.apache.commons.lang3.StringUtils;

/**
 * An entity that identified a unique field
 *
 * @since 2020-04-15
 */
public class FieldName {
    private String qualifier;
    private String simpleName;

    public FieldName(String qualifier, String simpleName) {
        this.qualifier = qualifier;
        this.simpleName = simpleName;
    }

    public FieldName(String fullName) {
        if (StringUtils.isNotEmpty(fullName)) {
            int index = fullName.lastIndexOf(".");
            if (index > 0) {
                this.simpleName = fullName.substring(fullName.lastIndexOf(".") + 1);
                this.qualifier = fullName.substring(0, fullName.lastIndexOf("."));
            } else {
                this.qualifier = "";
                this.simpleName = fullName;
            }
        }
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    /**
     * Get the {@link FieldName} instance's full qualified name like <b>qualifier.simpleName</b>
     *
     * @return a qualified name of field
     */
    public String getFullName() {
        return new StringBuilder(qualifier).append(".").append(simpleName).toString();
    }
}
