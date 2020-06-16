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
 * Scenario where a user clicks Help
 *
 * @since 2020-03-30
 */
public enum ConversionHelpEnum {
    /**
     * Base path setting view - Help in the lower left corner
     */
    PATH_SETTING("basePathSetting"),

    /**
     * PreAnalyze view - Add HMS policy question mark help
     */
    PRE_ANALYZE_GH("preAnalyzeGH"),

    /**
     * PreAnalyze view - To HMS policy question mark help
     */
    PRE_ANALYZE_G2H("preAnalyzeG2H"),

    /**
     * PreAnalyze view - UnConvertible Api question mark help
     */
    PRE_ANALYZE_NOT_SUPPORT_API("preAnalyzeUnConvertibleApi"),

    /**
     * PreAnalyze view - UnConvertible Method question mark help
     */
    PRE_ANALYZE_NOT_SUPPORT_METHOD("preAnalyzeUnConvertibleMethod"),

    /**
     * PreAnalyze view - XMS Adapter Generating Path question mark help
     */
    PRE_ANALYZE_XMS_PATH("preAnalyzeXMSPath"),

    /**
     * PreAnalyze view - Help in the lower left corner
     */
    PRE_ANALYZE("preAnalyze"),

    /**
     * PreAnalyze view - Jdk Check
     */
    PRE_ANALYZE_JDK("preAnalyzeJdk"),

    /**
     * PreAnalyze view - AndroidX Check
     */
    PRE_ANALYZE_ANDROID_X("preAnalyzeAndroidX"),

    /**
     * PreAnalyze view - SDK Version Check
     */
    PRE_ANALYZE_SDK_VERSION("preAnalyzeSdkVersion"),

    /**
     * PreAnalyze view - Variant Function question mark help
     */
    PRE_ANALYZE_VARIANT("preAnalyzeVariant"),

    /**
     * PreAnalyze view - UnSupport dependency Check
     */
    PRE_ANALYZE_NOT_SUPPORT_DEPENDENCY("preAnalyzeUnSupportDependency"),

    /**
     * Restore project view - help in right bottom corner.
     */
    RESTORE_PROJECT("restoreProject"),

    /**
     * Pre analyze view - third-party library detail help.
     */
    PRE_ANALYZE_THIRD_PARTY_LIBRARY("preAnalyzeThirdPartyLibrary");

    private String value;

    ConversionHelpEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
