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

package com.huawei.hms.convertor.core.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MappingConstant {
    public interface MappingDir {
        String MAPPING_DIR = "/mapping";
    }

    public interface MappingFile {
        String MAPPING_FILE_EXT = ".json";

        String ADD_HMS_AUTO_JSON_FILE_NAME = "wisehub-auto.json";

        String TO_HMS_AUTO_JSON_FILE_NAME = "wisehub-auto-hms.json";

        String ADD_HMS_MANUAL_JSON_FILE_NAME = "wisehub-manual.json";

        String TO_HMS_MANUAL_JSON_FILE_NAME = "wisehub-manual-hms.json";

        String ADD_HMS_GRADLE_AUTO_JSON_FILE_NAME = "wisehub-gradle.json";

        String TO_HMS_GRADLE_AUTO_JSON_FILE_NAME = "wisehub-gradle-hms.json";

        String ADD_HMS_GRADLE_AUTO_MIDDLE_JSON_FILE_NAME = "wisehub-gradle-middle.json";

        String TO_HMS_GRADLE_AUTO_MIDDLE_JSON_FILE_NAME = "wisehub-gradle-hms-middle.json";
    }

    public interface DependencyApiMetadataFile {
        String DEPENDENCY_API_DATA_DIR = "gms";

        String DEPENDENCY_API_DATA_FILE_EXT = ".api";

        String DEPENDENCY_API_METADATA_FILE_NAME = "dependency.txt";
    }

    public interface MappingFileKey {
        String AUTO_METHODS = "autoMethods";

        String MANUAL_METHODS = "manualMethods";

        String AUTO_CLASSES = "autoClasses";

        String MANUAL_CLASSES = "manualClasses";

        String AUTO_FIELDS = "autoFields";

        String MANUAL_FIELDS = "manualFields";
    }

    public interface Mapping4G2hJar {
        String KEY_MAPPING_4_G2H_JAR = "KEY_MAPPING_4_G2H_JAR";

        Pattern MAPPING_4_G2H_JAR_PATTERN = Pattern.compile("^convertor-mapping-[\\d+\\.]*(.)*\\.jar$");
    }

    public interface MappingVersionFile {
        String VERSION_FILE = "/version.properties";

        String KEY_VERSION_NAME = "versionName";
    }

    private static final String GMS_PLAY_SERVICE_BASEMENT = "com.google.android.gms:play-services-basement";

    private static final String GMS_PLAY_SERVICE_TASKS = "com.google.android.gms:play-services-tasks";

    private static final String GMS_PLAY_SERVICE_BASE = "com.google.android.gms:play-services-base";

    private static final String GMS_PLAY_SERVICE_AUTH = "com.google.android.gms:play-services-auth";

    public static final List<String> PREV_DEP;


    static {
        List<String> tempList = new ArrayList<>();
        tempList.add(GMS_PLAY_SERVICE_BASEMENT);
        tempList.add(GMS_PLAY_SERVICE_TASKS);
        tempList.add(GMS_PLAY_SERVICE_BASE);
        tempList.add(GMS_PLAY_SERVICE_AUTH);
        PREV_DEP = Collections.unmodifiableList(tempList);

    }
}
