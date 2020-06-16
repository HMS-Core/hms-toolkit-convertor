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

package com.huawei.generator.g2x.processor.map;

/**
 * Enum for valid result
 *
 * @since 2019-12-03
 */
public enum ValidResult {
    AUTO(0, "auto"),

    MANUAL_SUPPORT(1, "manual support"),

    MANUAL_NOTSUPPORT(2, "manual not support"),

    IGNORE(3, "ignore"),

    AUTO_DUMMY(4, "dummy");

    private int key;

    private String message;

    private String description;

    ValidResult(int key, String message) {
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
