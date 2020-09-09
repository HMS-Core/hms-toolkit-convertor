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

package com.huawei.hms.convertor.core.project.base;

/**
 * Project constants
 *
 * @since 2020-04-03
 */
public final class ProjectConstants {
    /**
     * Project type
     */
    public interface Type {
        String SDK = "SDK";

        String APP = "APP";
    }

    public interface SourceDir {
        String SRC_DIR = "/src/main";

        String JAVA_SRC_DIR = SRC_DIR + "/java";
    }

    public interface KitSdkVersionConfig {
        /**
         * if all sdkVersion is legal, this contains the Kit.
         * if any sdkVersion is N/A, this not contains the Kit.
         */
        String KIT_SDK_VERSION_JSON_FILE = "/kitSdkVersion.json";
    }

    /**
     * Converted-result files
     */
    public interface Result {
        String DEFECT_INSTANCES_JSON = "DefectInstances.json";

        String DEFECT_FILES_JSON = "DefectFiles.json";

        String LAST_XMSDIFF_JSON = "LastXmsDiff.json";

        String LAST_SUMMARY_JSON = "LastSummary.json";

        String LAST_CONVERSION_JSON = "LastConversion.json";

        String PROJECT_INFO_JSON = "ProjectInfo.json";

        String XMS_SETTING_JSON = "XmsSetting.json";
    }

    /**
     * Project common config
     */
    public interface Common {
        String COMMENT_SUFFIX = "-comment";

        String CONFIG_SUFFIX = "-config";

        String BACKUP_SUFFIX = "-xmsBackup";

        String PERCENT = "0%";
    }
}
