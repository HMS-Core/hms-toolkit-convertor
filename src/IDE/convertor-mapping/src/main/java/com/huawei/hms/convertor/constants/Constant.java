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

package com.huawei.hms.convertor.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Constant
 *
 * @since 2020-07-09
 */
public class Constant {
    public static final String KIT_MAPPING = "/kitMapping.json";

    public static final String CONFIG_PROPERTIES = "/config.properties";

    public static final String DEVELOPER_EN_URL = "developer.en.url";

    public static final int KIT_NAME_INDEX = 1;

    private static final String ML_FIREBASE = "com.google.firebase:firebase-ml-vision";

    private static final String ML_GMS =  "com.google.android.gms:play-services-vision";

    private static final String ML_GMS_VERSION =  "19.0.0";

    public static final Map<String, Map<String, String>> FORCE_BIND_DEP;

    static {
        Map<String, Map<String, String>> tempMap = new HashMap<>();
        Map<String, String> firebaseDep = new HashMap<>();
        firebaseDep.put(ML_GMS, ML_GMS_VERSION);
        tempMap.put(ML_FIREBASE, firebaseDep);
        FORCE_BIND_DEP = Collections.unmodifiableMap(tempMap);
    }
}
