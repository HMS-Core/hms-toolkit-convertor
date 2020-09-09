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
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.utils.KitMapping;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class DependencyApiMetadataGenerator {
    public boolean generate(String pluginPackagePath, Map<String, String> dependencyVersionMap) {
        log.info("begin generate dependency api metadata, dependencyVersionMap: {}.", dependencyVersionMap);
        String configDirPath = pluginPackagePath + PluginConstant.PluginPackageDir.CONFIG_DIR;
        Map<String, String> apiVersionMap = KitMapping.processGmsVersion(dependencyVersionMap);
        String dependencyApiMetadata = buildDependencyApiMetadata(apiVersionMap);

        try {
            FileUtil.writeFile(
                configDirPath + MappingConstant.DependencyApiMetadataFile.DEPENDENCY_API_METADATA_FILE_NAME,
                dependencyApiMetadata);
        } catch (IOException e) {
            log.error("write dependency api metadata to file fail, exception: {}.", e.getMessage());
            return false;
        }
        log.info("end generate dependency api metadata, dependencyVersionMap: {}.", dependencyVersionMap);
        return true;
    }

    private String buildDependencyApiMetadata(Map<String, String> apiVersionMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> apiVersion : apiVersionMap.entrySet()) {
            builder.append(MappingConstant.DependencyApiMetadataFile.DEPENDENCY_API_DATA_DIR)
                .append(Constant.UNIX_FILE_SEPARATOR)
                .append(apiVersion.getKey())
                .append(Constant.UNIX_FILE_SEPARATOR)
                .append(apiVersion.getValue())
                .append(MappingConstant.DependencyApiMetadataFile.DEPENDENCY_API_DATA_FILE_EXT)
                .append(Constant.LINE_SEPARATOR);
        }
        return builder.toString();
    }
}
