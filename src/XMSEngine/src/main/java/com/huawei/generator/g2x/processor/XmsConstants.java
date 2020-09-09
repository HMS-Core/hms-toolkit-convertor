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
 * Constants used in full scope of g2x, including:
 * 1. all kinds of pre-defined file names
 * 2. all kinds of keys of maps
 *
 * @since 2020-02-26
 */
public class XmsConstants {
    // summary file name
    public static final String GH_SUMMARY_FILE_NAME = "gh_summary";

    public static final String HG_SUMMARY_FILE_NAME = "hg_summary";

    public static final String G_SUMMARY_FILE_NAME = "g_summary";

    // single module name
    public static final String XMS_MODULE_NAME = "xmsadapter";

    public static final String XMS_SUBMODULE_NAME = "xmsaux";

    public static final String XG_MODULE_NAME = "xg";

    public static final String XH_MODULE_NAME = "xh";

    // xms adapter code location
    public static final String XMS_TEMP_PATH = "xmstemp";

    public static final String G2X_MANUAL_EXTENSION = "xms/g2x_config/g2x_manual_extension.json";

    public static final String KIT_INFO = "xms/g2x_config/kit_info.json";

    public static final String KIT_MAPPING = "xms/g2x_config/kit_mapping.json";

    // constants in diff and readme
    public static final String NULL_SIGN = "N/A";

    public static final String VERSION = "4.0.2-300";

    // file line break length
    public static final int LINE_SPACE = 4;

    public static final String XMS_NEED_REPLACE_FOLDER = "/xms_temp_folder";

    // file name
    public static final String GLOBAL_ENV_SETTING = "GlobalEnvSetting.java";

    public static final String SERIALIZATION_KIT_NAME = "Common";

    public static final String AUTO = "Auto";

    public static final String MANUAL = "Manual";

    public static final String DUMMY = "Dummy";
}
