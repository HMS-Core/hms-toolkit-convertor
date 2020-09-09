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

import com.huawei.generator.g2x.processor.map.Validator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builder for Generator
 *
 * @since 2019-12-27
 */
public class GeneratorBuilder {
    private File outPath;

    private String pluginPath; // the absolute path of generator

    private Map<String, String> kitVersionMap;

    private List<String> staticDirs = new ArrayList<>();

    // kitlist with path information
    private List<String> realkitList;

    // standard kitlist
    private List<String> standardKitList;

    private List<String> originKitList;

    private GeneratorConfiguration configuration;

    public GeneratorBuilder(String pluginPath, String outPath) {
        this.pluginPath = pluginPath;
        this.outPath = new File(outPath);
    }

    public GeneratorBuilder strategy(List<String> originKitList, GeneratorConfiguration configuration) {
        this.realkitList = Validator.generateEssentialDependency(originKitList);
        this.standardKitList = new LinkedList<>(realkitList);
        staticDirs.addAll(realkitList);
        this.configuration = configuration;
        this.originKitList = Validator.generateEssentialDependency(originKitList);
        return this;
    }

    public GeneratorBuilder version(Map<String, String> kitVersionMap) {
        this.kitVersionMap = kitVersionMap;
        return this;
    }

    public Generator build() {
        return new Generator(this);
    }

    public File getOutPath() {
        return outPath;
    }

    public GeneratorConfiguration getConfiguration() {
        return configuration;
    }

    public List<String> getStaticDirs() {
        return staticDirs;
    }

    public Map<String, String> getKitVersionMap() {
        return kitVersionMap;
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public List<String> getOriginKitList() {
        return originKitList;
    }

    public List<String> getRealkitList() {
        return realkitList;
    }

    public List<String> getStandardKitList() {
        return standardKitList;
    }
}
