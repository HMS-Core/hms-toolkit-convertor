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

package com.huawei.generator.gen;

import com.huawei.generator.g2x.processor.XmsConstants;
import com.huawei.generator.g2x.processor.map.Validator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * GeneratorBuilder class
 *
 * @since 2020-05-12
 */
public class GeneratorBuilder {
    File outPath;

    String pluginPath; // the absolute path of generator

    Map<String, String> kitVersionMap;

    List<String> staticDirs = new ArrayList<>();

    // kitlist with path information
    List<String> realkitList;

    // standard kitlist
    List<String> standardKitList;

    GeneratorConfiguration configuration;

    public GeneratorBuilder(String pluginPath, String outPath) {
        this.pluginPath = pluginPath;
        this.outPath = new File(outPath);
    }

    public GeneratorBuilder strategy(List<String> originKitList, GeneratorConfiguration configuration) {
        this.realkitList = Validator.generateEssentialDependency(originKitList);
        this.standardKitList = buildModuleKitList(new LinkedList<>(realkitList));
        // handle firebase & gms
        Map<String, String> map = new HashMap<>();
        map.put("mlfirebase", XmsConstants.XMS_ML_FIREBASE_PATH);
        map.put("mlgms", XmsConstants.XMS_ML_GMS_PATH);

        // mlfirebase => ml\\firebase
        realkitList.replaceAll(x -> map.getOrDefault(x, x));
        staticDirs.addAll(realkitList);
        this.configuration = configuration;

        return this;
    }

    public GeneratorBuilder version(Map<String, String> kitVersionMap) {
        this.kitVersionMap = kitVersionMap;
        return this;
    }

    public Generator build() {
        return new Generator(this);
    }

    public List<String> getStandardKitList() {
        return standardKitList;
    }

    // map in util contains extra className. either firebase or gms are included in each kit
    private List<String> buildModuleKitList(List<String> kitStrategyMap) {
        // copy map
        List<String> result = new LinkedList<>(kitStrategyMap);
        Map<String, String> map = new HashMap<>();
        map.put("mlfirebase", "ml");
        map.put("mlgms", "ml");

        // mlfirebase => ml
        result.replaceAll(x -> map.getOrDefault(x, x));
        return result;
    }
}
