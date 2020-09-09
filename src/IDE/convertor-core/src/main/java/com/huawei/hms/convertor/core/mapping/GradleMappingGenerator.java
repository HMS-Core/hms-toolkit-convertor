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

import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class GradleMappingGenerator {
    public boolean generate(String pluginPackagePath, Map<String, String> dependencyVersionMap) {
        String configDirPath = pluginPackagePath + PluginConstant.PluginPackageDir.CONFIG_DIR;

        try {
            generate4GaddH(configDirPath, dependencyVersionMap);
            generate4G2H(configDirPath, dependencyVersionMap);
        } catch (IOException e) {
            log.error("generate gradle mapping fail, dependencyVersionMap: {}.", dependencyVersionMap);
            return false;
        }
        return true;
    }

    private void generate4GaddH(String configFilePath, Map<String, String> dependencyVersionMap) throws IOException {
        log.info("begin generate gradle mapping for G+H, dependencyVersionMap: {}.", dependencyVersionMap);
        String middleJsonStr = FileUtil.readToString(
            configFilePath + MappingConstant.MappingFile.ADD_HMS_GRADLE_AUTO_MIDDLE_JSON_FILE_NAME,
            StandardCharsets.UTF_8.name());
        String mappingJson = GradleMiddleJsonParser.parseMiddleJson2Mapping4GaddH(middleJsonStr, dependencyVersionMap);
        FileUtil.writeFile(configFilePath + MappingConstant.MappingFile.ADD_HMS_GRADLE_AUTO_JSON_FILE_NAME,
            mappingJson);
        log.info("end generate gradle mapping for G+H, dependencyVersionMap: {}.", dependencyVersionMap);
    }

    private void generate4G2H(String configFilePath, Map<String, String> dependencyVersionMap) throws IOException {
        log.info("begin generate gradle mapping for G2H, dependencyVersionMap: {}.", dependencyVersionMap);
        String middleJsonStr =
            FileUtil.readToString(configFilePath + MappingConstant.MappingFile.TO_HMS_GRADLE_AUTO_MIDDLE_JSON_FILE_NAME,
                StandardCharsets.UTF_8.name());
        String mappingJson = GradleMiddleJsonParser.parseMiddleJson2Mapping4G2H(middleJsonStr, dependencyVersionMap);
        FileUtil.writeFile(configFilePath + MappingConstant.MappingFile.TO_HMS_GRADLE_AUTO_JSON_FILE_NAME, mappingJson);
        log.info("end generate gradle mapping for G2H, dependencyVersionMap: {}.", dependencyVersionMap);
    }
}
