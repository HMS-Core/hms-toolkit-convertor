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

package com.huawei.generator.g2x.processor;

/**
 * Enum generator result
 *
 * @since 2019-11-24
 */
public enum GeneratorResult {
    SUCCESS(0, "success"),

    INNER_CRASH(1, "xms-engine.jar crash"),

    MISSING_PLUGIN(2, "missing xms-engine.jar"), // check description

    INVALID_OUTPATH(3, "invalid output path"),

    INVALID_KIT_NAME(4, "invalid kit name"), // check description

    INVALID_KIT_VERSION(5, "invalid kit version"), // check description

    INVALID_KIT_STRATEGY(6, "invalid kit strategy"), // check description

    INVALID_LOGPATH(7, "invalid log path"), // check description

    INVALID_JSON_FORMAT(8, "invalid json format"),

    INVALID_OUTTER_BLACKLIST(9, "invalid outer blacklist"),

    MISSING_DEPENDENCY(10, "there is no dependency list"),

    INVALID_SUMMARYPATH(11, "invalid Summary path"),

    INVALID_INPUT(12, "invalid input path"),

    ERROR_CREATE_MODULE(13, "failed to create module"),

    ERROR_CREATE_MODULE_XML_FILE(14, "failed to create xml file"),

    ERROR_CREATE_MODULE_CODE(15, "failed to generate code"),

    ERROR_CREATE_MODULE_GRADLE_FILE(16, "failed to create gradle file"),

    ERROR_CREATE_MODULE_README_FILE(17, "failed to create readme file"),

    ERROR_CREATE_MODULE_SUMMARY_FILE(18, "failed to create summary file");

    private int key;

    private String message;

    private String description; // for detail failure reasons

    GeneratorResult(int key, String message) {
        this.key = key;
        this.message = message;
        this.description = "";
    }

    public int getKey() {
        return this.key;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDescription() {
        return this.description;
    }
}
