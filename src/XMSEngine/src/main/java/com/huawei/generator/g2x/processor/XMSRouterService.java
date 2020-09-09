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

package com.huawei.generator.g2x.processor;

import com.huawei.generator.g2x.po.kit.KitMapping;
import com.huawei.generator.g2x.processor.map.MapProcessor;
import com.huawei.generator.g2x.processor.map.Validator;
import com.huawei.generator.gen.InvalidJsonException;

import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.InvalidPathException;
import java.util.Map;

/**
 * Class for XMSRouterService
 *
 * @since 2019-11-24
 */
public class XMSRouterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XMSRouterService.class);

    /**
     * generate full Gms to Xms mapping
     *
     * @param pluginPath location of XMSRouterService.jar
     * @param outputPath location of output
     * @param logPath location of log, reserved
     * @param gmsVersion version reserved
     * @param hmsVersion version reserved
     * @return the result of generating xms configuration
     */
    public static GeneratorResult generateXmsConfig(String pluginPath, String outputPath, String logPath,
        Map<String, String> gmsVersion, Map<String, String> hmsVersion) {
        // blocklist
        System.setProperty("enable_block_list", "true");
        GeneratorResult r = Validator.validateParam(outputPath, logPath, pluginPath, gmsVersion);
        if (r != GeneratorResult.SUCCESS) {
            return r;
        }
        Map<String, String> currentVersion = KitMapping.processGmsVersion(gmsVersion);
        try {
            MapProcessor creator = new MapProcessor.MapProcessorBuilder(pluginPath, outputPath, currentVersion).build();
            return creator.processAllTarget();
        } catch (InvalidJsonException e) {
            LOGGER.error("Invalid input json when generating XmsConfig!");
            return GeneratorResult.INVALID_JSON_FORMAT;
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid content exists in input Json!");
            return GeneratorResult.INVALID_JSON_FORMAT;
        } catch (InvalidPathException e) {
            LOGGER.error("Invalid output path when generating XmsConfig!!");
            return GeneratorResult.INVALID_OUTPATH;
        }
    }

    /**
     * get current jar location
     * 
     * @return current jar location
     */
    public static String whereAmI() {
        String path = "/mirror/gms.json";
        String tempPath = XMSRouterService.class.getResource(path).getPath();

        // use the designated jar
        if (tempPath == null || !tempPath.contains("!") && !tempPath.endsWith(".jar")) {
            return "";
        }
        if (tempPath.startsWith("file:/")) {
            tempPath = tempPath.substring(6);
        }
        if (tempPath.endsWith("!/mirror/gms.json")) {
            tempPath = tempPath.replace("!/mirror/gms.json", "");
        }
        LOGGER.info("pluginpath for convertor {}", tempPath);
        return tempPath;
    }
}
