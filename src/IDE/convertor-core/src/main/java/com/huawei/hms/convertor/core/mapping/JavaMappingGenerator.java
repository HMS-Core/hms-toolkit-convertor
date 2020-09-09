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

import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.g2h.processor.XMSRouterService;
import com.huawei.hms.convertor.openapi.XmsGenerateService;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class JavaMappingGenerator {
    public boolean generate(String pluginPackagePath, Map<String, String> dependencyVersionMap) {
        boolean isGenerate4GaddhSuccess = generate4GaddH(pluginPackagePath, dependencyVersionMap);
        if (!isGenerate4GaddhSuccess) {
            return false;
        }

        return generate4G2H(pluginPackagePath, dependencyVersionMap);
    }

    private boolean generate4GaddH(String pluginPackagePath, Map<String, String> dependencyVersionMap) {
        log.info("begin generate java mapping for G+H, dependencyVersionMap: {}.", dependencyVersionMap);
        String pluginJarPath = System.getProperty(XmsConstants.KEY_XMS_JAR);
        String outPath = pluginPackagePath + PluginConstant.PluginPackageDir.CONFIG_DIR;

        GeneratorResult generateXmsResult = XmsGenerateService.generateXmsConfig(pluginJarPath, outPath,
            PluginConstant.PluginDataDir.PLUGIN_LOG_PATH, dependencyVersionMap);
        if (generateXmsResult.getKey() != 0) {
            log.info("generate java mapping for G+H failed, dependencyVersionMap: {}, errorMsg: {}.",
                dependencyVersionMap, generateXmsResult.getMessage());
            return false;
        }

        log.info("end generate java mapping for G+H, dependencyVersionMap: {}.", dependencyVersionMap);
        return true;
    }

    private boolean generate4G2H(String pluginPackagePath, Map<String, String> dependencyVersionMap) {
        log.info("begin generate java mapping for G2H, dependencyVersionMap: {}.", dependencyVersionMap);

        String pluginJarPath = System.getProperty(MappingConstant.Mapping4G2hJar.KEY_MAPPING_4_G2H_JAR);
        String outPath = pluginPackagePath + PluginConstant.PluginPackageDir.CONFIG_DIR;

        com.huawei.hms.convertor.g2h.processor.GeneratorResult generatorResult = XMSRouterService.generateHmsConfig(
            pluginJarPath, outPath, PluginConstant.PluginDataDir.PLUGIN_LOG_PATH, dependencyVersionMap);
        if (generatorResult.getKey() != 0) {
            log.info("generate java mapping for G2H failed, dependencyVersionMap: {}, errorMsg: {}.",
                dependencyVersionMap, generatorResult.getMessage());
            return false;
        }

        log.info("end generate java mapping for G2H, dependencyVersionMap: {}.", dependencyVersionMap);
        return true;
    }
}
