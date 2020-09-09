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

package com.huawei.codebot.analyzer.x2y.java.other.complexchanger;

/**
 * A model used to describe what the fix involved
 *
 * @since 2020-04-17
 */
public class FixAction {
    private String fixType = null;

    private String operation = null;


    private String newContent = null;

    private String fixPosition = null;

    private String desc = null;

    /** initialize fixType */
    public String getFixType() {
        return fixType;
    }

    public void setFixType(String fixType) {
        this.fixType = fixType;
    }

    /** initialize operation */
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    /** initialize newContent */
    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }

    /** initialize fixPosition */
    public String getFixPosition() {
        return fixPosition;
    }

    public void setFixPosition(String fixPosition) {
        this.fixPosition = fixPosition;
    }

    /** initialize desc */
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
