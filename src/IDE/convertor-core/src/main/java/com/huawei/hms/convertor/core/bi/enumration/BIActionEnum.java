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
 * bi action type enum
 *
 * @since 2020-03-27
 */
public enum BIActionEnum {
    /**
     * Trace menu selection.
     */
    MENU_SELECTION("menuSelection"),

    /**
     * Trace project source info.
     */
    SOURCE_INFO("sourceInfo"),

    /**
     * Trace operation of processing the analysis result.
     */
    CONVERT_OPERATION("convertOperation"),

    /**
     * Trace Analyze Result.
     */
    ANALYZE_RESULT("analyzeResult"),

    /**
     * Trace usage of some function options in the tool.
     */
    FUNCTION_SELECTION("functionSelection"),

    /**
     * Trace Cancellation of page operations.
     */
    CANCEL_LISTENER("cancelListener"),

    /**
     * Trace help click.
     */
    HELP_CLICK("helpClick"),

    EXPORT_CLICK("exportClick"),

    /**
     * Trace analysis time cost.
     */
    TIME_ANALYZE_COST("timeAnalyzeCost"),

    /**
     * Trace java doc selection.
     */
    JAVADOC_SELECTION("javadocSelection"),

    /**
     * Trace java doc selection.
     */
    JAVADOC_SEARCH("javadocSearch");

    private String value;

    BIActionEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
