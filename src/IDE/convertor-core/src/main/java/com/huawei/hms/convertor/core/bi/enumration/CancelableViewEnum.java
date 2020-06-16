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
 * Cancelable view enum
 *
 * @since 2020-03-30
 */
public enum CancelableViewEnum {
    /**
     * A new scan is performed, and a message is displayed indicating that the scan result already exists.
     */
    NEW_CONVERSION("newConversionConfirm"),

    /**
     * Base path Setting view.
     */
    PATH_SETTING("pathSettingView"),

    /**
     * First analyze loading window.
     */
    FIRST_ANALYZE("firstAnalyzeLoading"),

    /**
     * PreAnalyze view.
     */
    PRE_ANALYZE("preAnalyzeView"),

    /**
     * Message indicating that the current conversion policy is different from the last one.
     */
    POLICY_CHANGE("policyChangeConfirm"),

    /**
     * Second analyze loading window.
     */
    SECOND_ANALYZE("secondAnalyzeLoading"),

    /**
     * Restore project view.
     */
    RESTORE_PROJECT("restoreProjectView"),

    /**
     * Restore project confirm.
     */
    RESTORE_PROJECT_CONFIRM("restoreProjectConfirm"),

    /**
     * Privacy view.
     */
    PRIVACY("privacyView");

    private String value;

    CancelableViewEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
