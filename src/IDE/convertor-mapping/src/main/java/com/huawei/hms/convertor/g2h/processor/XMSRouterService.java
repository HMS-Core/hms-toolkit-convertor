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

package com.huawei.hms.convertor.g2h.processor;

import com.huawei.hms.convertor.utils.KitMapping;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * generate mapping file
 *
 * @since 2020-07-03
 */
@Slf4j
public class XMSRouterService {
    /**
     * generate full Gms -> Xms mapping
     *
     * @param jarFilePath, file path of xms_generator_1.0.0.0-SNAPSHOT.jar
     * @param outputPath, output path
     * @param logPath, log output path
     * @param gmsVersion, gms version
     * @return hmsVersion，hms version
     */
    public static GeneratorResult generateHmsConfig(String jarFilePath, String outputPath, String logPath,
        Map<String, String> gmsVersion) {
        log.info("begin to generate mapping json.");
        GeneratorResult ret = KitValidator.paramValidator(outputPath, logPath, jarFilePath);
        if (ret != GeneratorResult.SUCCESS) {
            return ret;
        }

        Map<String, String> currentVersion = KitMapping.processGmsVersion(gmsVersion);
        MapProcessor creator = new MapProcessor(currentVersion, outputPath, logPath, jarFilePath);
        return creator.processAllTargetConfig();
    }
}
