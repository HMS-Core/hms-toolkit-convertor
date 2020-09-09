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

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Used to store gradle version
 *
 * @since 2020-04-13
 */
public class GradleVersionService {
    /**
     * DOUBLE_QUOTES to store double quotes
     */
    public static final Pattern DOUBLE_QUOTES = Pattern.compile("\"(.*?)\"");

    /**
     * DOUBLE_QUOTES to store single quotes
     */
    public static final Pattern SINGLE_QUOTES = Pattern.compile("\'(.*?)\'");

    static Map<String, String> package_version = new HashMap<>();
    static Map<String, String> variable_version = new HashMap<>();

    private static boolean packageVersionChanged = false;
    private static boolean variableVersionChanged = false;
    /**
     * getValue from variable_version
     */
    public static String getValue(String var) {
        return variable_version.get(var);
    }

    public GradleVersionService() {
    }

    public static boolean isPackageVersionChanged() {
        return packageVersionChanged;
    }

    public static void setPackageVersionChanged(boolean changed) {
        packageVersionChanged = changed;
    }

    public static boolean isVariableVersionChanged() {
        return variableVersionChanged;
    }

    public static void setVariableVersionChanged(boolean changed) {
        variableVersionChanged = changed;
    }

    /**
     * initial version-info containers
     */
    public static void initAllVersionInfo() {
        package_version.clear();
        variable_version.clear();
    }

}
