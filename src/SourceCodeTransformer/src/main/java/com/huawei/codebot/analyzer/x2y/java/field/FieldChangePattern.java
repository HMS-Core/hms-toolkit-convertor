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

/**
 * A model used to represent how to change a field
 *
 * @since 2020-04-16
 */
public class FieldChangePattern {
    private FieldName oldFieldName;
    private FieldName newFieldName;

    private FieldName actualFieldName;

    public FieldChangePattern(FieldName oldFieldName, FieldName newFieldName) {
        this.oldFieldName = oldFieldName;
        this.newFieldName = newFieldName;
        this.actualFieldName = oldFieldName;
    }

    public FieldName getOldFieldName() {
        return oldFieldName;
    }

    public void setOldFieldName(FieldName oldFieldName) {
        this.oldFieldName = oldFieldName;
    }

    public FieldName getNewFieldName() {
        return newFieldName;
    }

    public void setNewFieldName(FieldName newFieldName) {
        this.newFieldName = newFieldName;
    }

    public FieldName getActualFieldName() {
        return actualFieldName;
    }

    public void setActualFieldName(FieldName actualFieldName) {
        this.actualFieldName = actualFieldName;
    }
}
