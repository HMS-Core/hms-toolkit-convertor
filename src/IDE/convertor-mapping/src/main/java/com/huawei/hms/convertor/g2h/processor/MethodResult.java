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

public enum MethodResult {
    AUTO(0, "auto"),

    MANUAL(1, "manual"),

    IGNORE(2, "ignore");

    private int key;

    private String message;

    private String description;

    MethodResult(int key, String message) {
        this.key = key;
        this.message = message;
        description = "";
    }

}
