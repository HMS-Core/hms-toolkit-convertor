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
import java.util.Set;

/**
 * Switch Statement Info
 *
 * @since 3.0.0.300
 */
public class SwitchStatementInfo {

    private String typeInfo;

    private String moduleName;

    private String className;

    private String packageName;

    private String filePath;

    private Set<String> declareNames;

    private Set<String> clazzNameSet;

    private Map<String, Object> desc;

    private boolean isChildSet;

    public String getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(String typeInfo) {
        this.typeInfo = typeInfo;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Set<String> getDeclareNames() {
        return declareNames;
    }

    public void setDeclareNames(Set<String> declareNames) {
        this.declareNames = declareNames;
    }

    public Set<String> getClazzNameSet() {
        return clazzNameSet;
    }

    public void setClazzNameSet(Set<String> clazzNameSet) {
        this.clazzNameSet = clazzNameSet;
    }

    public Map<String, Object> getDesc() {
        return desc;
    }

    public void setDesc(Map<String, Object> desc) {
        this.desc = desc;
    }

    public boolean isChildSet() {
        return isChildSet;
    }

    public void setChildSet(boolean childSet) {
        isChildSet = childSet;
    }
}
