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

package com.huawei.generator.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.huawei.generator.g2x.po.kit.KitInfoContainer;
import com.huawei.generator.g2x.processor.XmsConstants;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * reconstruct inputs of KitValidator
 *
 * @since 2020-03-02
 */
public enum KitInfoRes {
    INSTANCE;

    private Map<String, List<String>> KIT_DEPENDENCY_MAP;

    private Map<String, String> NORMALIZE_KIT_MAP;

    private Map<String, String> UNNORMALIZE_KIT_MAP;

    private List<String> SUPPORT_LIST;

    private Map<String, Map<String, Integer>> DEFAULT_SDK_VERSION_MAP;

    private static final String MIN_SDK_VERSION = "minSdkVersion";
    private static final String TARGET_SDK_VERSION = "targetSdkVersion";
    private static final String COMPILE_SDK_VERSION = "compileSdkVersion";

    KitInfoRes() {
        InputStream inputStream = KitInfoRes.class.getResourceAsStream("/" + XmsConstants.KIT_INFO);
        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Gson gson = new GsonBuilder().create();
        KitInfoContainer kitInfoContainer = gson.fromJson(isr, new TypeToken<KitInfoContainer>() {
        }.getType());
        KIT_DEPENDENCY_MAP = kitInfoContainer.getDependency();
        NORMALIZE_KIT_MAP = kitInfoContainer.getDisplay();
        SUPPORT_LIST = kitInfoContainer.getSupported();
        DEFAULT_SDK_VERSION_MAP = kitInfoContainer.getDefaultSdkVersion();
        HashMap<String, String> unNormalizeKitMap = new HashMap<>();
        for (Map.Entry<String, String> mapEntry : kitInfoContainer.getDisplay().entrySet()) {
            unNormalizeKitMap.put(mapEntry.getValue(), mapEntry.getKey());
        }
        UNNORMALIZE_KIT_MAP = unNormalizeKitMap;
    }

    public Map<String, List<String>> getKitDependencyMap() {
        return KIT_DEPENDENCY_MAP;
    }

    public Map<String, String> getNormalizeKitMap() {
        return NORMALIZE_KIT_MAP;
    }

    public Map<String, String> getUnnormalizeKitMap() {
        return UNNORMALIZE_KIT_MAP;
    }

    public List<String> getSupportList() {
        return SUPPORT_LIST;
    }

    public Map<String, Integer> getDefaultSdkVersion(List<String> allKits) {
        int compileSDKVersion = 0;
        int minSDKVersion = 0;
        int targetSDKVersion = 0;
        for (String kit : allKits) {
            kit = kit.toLowerCase();
            if (DEFAULT_SDK_VERSION_MAP.get(kit) == null) {
                continue;
            }
            compileSDKVersion = DEFAULT_SDK_VERSION_MAP.get(kit).get(COMPILE_SDK_VERSION) > compileSDKVersion ?
                    DEFAULT_SDK_VERSION_MAP.get(kit).get(COMPILE_SDK_VERSION) : compileSDKVersion;
            minSDKVersion = DEFAULT_SDK_VERSION_MAP.get(kit).get(MIN_SDK_VERSION) > minSDKVersion ?
                    DEFAULT_SDK_VERSION_MAP.get(kit).get(MIN_SDK_VERSION) : minSDKVersion;
            targetSDKVersion = DEFAULT_SDK_VERSION_MAP.get(kit).get(TARGET_SDK_VERSION) > targetSDKVersion ?
                    DEFAULT_SDK_VERSION_MAP.get(kit).get(TARGET_SDK_VERSION) : targetSDKVersion;
        }
        Map<String, Integer> result = new HashMap<>();
        result.put(COMPILE_SDK_VERSION, compileSDKVersion);
        result.put(MIN_SDK_VERSION, minSDKVersion);
        result.put(TARGET_SDK_VERSION, targetSDKVersion);
        return result;
    }
}
