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

package com.huawei.hms.convertor.idea.ui.analysis;

/**
 * Help link enum
 *
 * @since 2020-04-09
 */
public enum HelpLinkType {
    /**
     * link to explain the conversion strategy of Add HMS.
     */
    ADD_HMS("add_hms_url"),

    /**
     * link to explain the conversion strategy of To HMS.
     */
    TO_HMS("to_hms_url"),

    /**
     * link to explain how to set the path of XMS adapter code.
     */
    XMS_PATH("xms_path_url"),

    /**
     * link to learn about the Support Status.
     */
    NOT_SUPPORT_API("api_url"),

    /**
     * link to learn about the Support Status.
     */
    NOT_SUPPORT_METHOD("method_url");

    private String value;

    HelpLinkType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
