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

package com.huawei.hms.convertor.core.bi.enumration;

/**
 * HMS-Convertor-sub menu enum
 *
 * @since 2020-03-30
 */
public enum MenuEnum {
    /**
     * New Conversion menu
     */
    NEW_CONVERSION("newConversion"),

    /**
     * Open Last Conversion menu
     */
    OPEN_LAST("openLastConversion"),

    /**
     * Save All menu
     */
    SAVE("saveAll"),

    /**
     * Restore Project menu
     */
    RESTORE("restoreProject");

    private String value;

    MenuEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
