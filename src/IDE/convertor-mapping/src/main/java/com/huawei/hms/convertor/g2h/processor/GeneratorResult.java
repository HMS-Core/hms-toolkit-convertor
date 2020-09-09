/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.g2h.processor;

public enum GeneratorResult {
    SUCCESS(0, "success"),

    MISSING_PLUGIN(2, "missing xms-generator.jar"), // check description

    INVALID_OUTPATH(3, "invalid output path"),

    INVALID_LOGPATH(7, "invalid log path"); // check description

    private int key;

    private String message;

    private String description; // for detail failure reasons

    GeneratorResult(int key, String message) {
        this.key = key;
        this.message = message;
        description = "";
    }

    public int getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
