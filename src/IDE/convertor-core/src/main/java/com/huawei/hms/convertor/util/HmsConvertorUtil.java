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

package com.huawei.hms.convertor.util;

import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.core.result.diff.Dependency;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Function Description
 *
 * @since 2020-03-23
 */
public final class HmsConvertorUtil {
    public static Map<String, Set<String>> parseGradle(String configPath) throws IOException {
        Map<String, Set<String>> kit2Dependency = new HashMap<>();
        JSONObject gradle = getResultList(configPath);
        Object appBuildGradle = gradle.get("AppBuildGradle");
        Map<String, Object> map = JSON.parseObject(appBuildGradle.toString(), Map.class);
        Object addInDependencies = map.get("addInDependencies");
        List<Object> dependencies = JSON.parseObject(addInDependencies.toString(), List.class);
        for (Object dependency : dependencies) {
            Dependency de = JSON.parseObject(dependency.toString(), Dependency.class);
            List<String> dependencyNames = de.getAddDependenciesName();
            String originGoogleName = de.getOriginGoogleName();
            String g = originGoogleName + ":" + de.getVersion();
            ConversionPointDesc desc = de.getDescAuto();
            if (kit2Dependency.containsKey(desc.getKit())) {
                kit2Dependency.get(desc.getKit()).add(g);
            } else {
                Set<String> dependencySet = new HashSet<>();
                dependencySet.add(g);
                dependencySet.addAll(dependencyNames);
                if (desc.getKit().equals(KitsConstants.ML)) {
                    if (originGoogleName.equals(KitsConstants.ML_GMS_NAME)
                        && !kit2Dependency.containsKey(KitsConstants.ML_GMS)) {
                        kit2Dependency.put(KitsConstants.ML_GMS, dependencySet);
                        continue;
                    } else if (originGoogleName.equals(KitsConstants.ML_FIREBASE_NAME)
                        && !kit2Dependency.containsKey(KitsConstants.ML_FIREBASE)) {
                        kit2Dependency.put(KitsConstants.ML_FIREBASE, dependencySet);
                        continue;
                    } else {
                        kit2Dependency.get(desc.getKit()).addAll(dependencySet);
                    }
                } else {
                    kit2Dependency.put(desc.getKit(), dependencySet);
                }
            }
            kit2Dependency.get(desc.getKit()).addAll(dependencyNames);
        }
        // ML special
        List<String> mlList = new ArrayList<>();
        mlList.add("com.google.firebase:firebase-ml-natural-language:21.0.2");
        mlList.add("com.google.firebase:firebase-ml-natural-language-translate-model:20.0.5");
        mlList.add("com.google.firebase:firebase-ml-model-interpreter:22.0.1");
        kit2Dependency.get(KitsConstants.ML_FIREBASE).addAll(mlList);
        return kit2Dependency;
    }

    public static <T> JSONObject getResultList(String resultFilePath) throws IOException, JSONException {
        if (null == resultFilePath || !new File(resultFilePath).exists()) {
            throw new NoSuchFileException("No result file generated! resultFilePath = " + resultFilePath);
        }

        String resultString = FileUtil.readToString(resultFilePath, Constant.GBK);
        JSONObject wiseHubs = JSON.parseObject(resultString);
        return wiseHubs;
    }

}
