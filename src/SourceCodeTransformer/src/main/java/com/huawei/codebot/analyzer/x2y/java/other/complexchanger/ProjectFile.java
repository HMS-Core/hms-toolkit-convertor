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

import java.util.List;


/**
 * struct ProjectFile
 *
 * @since 2020-04-17
 */
public class ProjectFile {
    private String projectPath = null;

    private String modulePath = null;

    private String gradlePath = null;

    private String xmlPath = null;

    private String codePaths = null;

    private List<String> codeFilePaths = null;

    /** store projectPath */
    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    /** store modulePath */
    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    /** store gradlePath */
    public String getGradlePath() {
        return gradlePath;
    }

    public void setGradlePath(String gradlePath) {
        this.gradlePath = gradlePath;
    }

    /** store xmlPath */
    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    /** store codePaths */
    public String getCodePaths() {
        return codePaths;
    }

    public void setCodePaths(String codePaths) {
        this.codePaths = codePaths;
    }

    /** store codeFilePaths */
    public List<String> getCodeFilePaths() {
        return codeFilePaths;
    }

    public void setCodeFilePaths(List<String> codeFilePaths) {
        this.codeFilePaths = codeFilePaths;
    }
}
