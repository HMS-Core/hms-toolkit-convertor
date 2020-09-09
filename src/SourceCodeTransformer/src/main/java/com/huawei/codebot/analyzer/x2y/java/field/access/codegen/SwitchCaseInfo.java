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
package com.huawei.codebot.analyzer.x2y.java.field.access.codegen;

import java.util.Map;

/**
 * Switch Case Info
 *
 * @since 3.0.0.300
 */
public class SwitchCaseInfo {

    private String importName;

    private String gmsFieldName;

    private String hmsFieldName;

    private String replaceName;

    private Map<String, Object> desc;

    public String getImportName() {
        return importName;
    }

    public void setImportName(String importName) {
        this.importName = importName;
    }

    public String getGmsFieldName() {
        return gmsFieldName;
    }

    public void setGmsFieldName(String gmsFieldName) {
        this.gmsFieldName = gmsFieldName;
    }

    public String getHmsFieldName() {
        return hmsFieldName;
    }

    public void setHmsFieldName(String hmsFieldName) {
        this.hmsFieldName = hmsFieldName;
    }

    public String getReplaceName() {
        return replaceName;
    }

    public void setReplaceName(String replaceName) {
        this.replaceName = replaceName;
    }

    public Map<String, Object> getDesc() {
        return desc;
    }

    public void setDesc(Map<String, Object> desc) {
        this.desc = desc;
    }

}
