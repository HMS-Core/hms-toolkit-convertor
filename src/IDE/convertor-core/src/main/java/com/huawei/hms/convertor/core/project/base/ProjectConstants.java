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

    /**
     * Mapping config files
     */
    public interface Mapping {
        String ADD_HMS_AUTO_JSON_FILE = "wisehub-auto.json";

        String TO_HMS_AUTO_JSON_FILE = "wisehub-auto-hms.json";

        String ADD_HMS_MANUAL_JSON_FILE = "wisehub-manual.json";

        String TO_HMS_MANUAL_JSON_FILE = "wisehub-manual-hms.json";

        String ADD_HMS_GRADLE_JSON_FILE = "wisehub-gradle.json";
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
