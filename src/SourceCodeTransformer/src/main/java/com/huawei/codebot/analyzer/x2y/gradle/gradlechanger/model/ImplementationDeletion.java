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

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model;

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppDeleteInDependencies;

/**
 * deal with low version implementation to delete
 *
 * @since 3.0.2
 */
public class ImplementationDeletion {
    String tagName;
    int startLineNumber;
    String oldStr;
    StructAppDeleteInDependencies deletion;

    public ImplementationDeletion(String tagName, int startLineNumber, String oldStr,
            StructAppDeleteInDependencies deletion) {
        this.tagName = tagName;
        this.startLineNumber = startLineNumber;
        this.oldStr = oldStr;
        this.deletion = deletion;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getStartLineNumber() {
        return startLineNumber;
    }

    public void setStartLineNumber(int startLineNumber) {
        this.startLineNumber = startLineNumber;
    }

    public String getOldStr() {
        return oldStr;
    }

    public void setOldStr(String oldStr) {
        this.oldStr = oldStr;
    }

    public StructAppDeleteInDependencies getDeletion() {
        return deletion;
    }

    public void setDeletion(StructAppDeleteInDependencies deletion) {
        this.deletion = deletion;
    }
}
