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

import com.google.gson.JsonSyntaxException;
import com.huawei.generator.g2x.po.map.convertor.GSummaryMap;
import com.huawei.generator.g2x.processor.map.ConvertorProcessor;
import com.huawei.generator.g2x.processor.map.MapProcessor;
import com.huawei.generator.gen.InvalidJsonException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.InvalidPathException;
import java.util.Map;

/**
 * class for XMSRouterService
 *
 * @since 2019-11-24
 */
public class XMSRouterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XMSRouterService.class);

    /**
     * generate full Gms -> Xms mapping
     *
     * @param pluginPath, path for XMSRouterService.jar，used to read resource file and generate code
     * @param outputPath, path for output
     * @param logPath, path for log files（reserve: status will be preserve in return value）
     * @param gmsVersion, value is null if we can not ascertain which gmsVersion
     * @return hmsVersion，value is null if we can not ascertain which hmsVersion
     */
    public static GeneratorResult generateXmsConfig(String pluginPath, String outputPath, String logPath,
        Map<String, String> gmsVersion, Map<String, String> hmsVersion) {
        // back-list
        System.setProperty("enable_black_list", "true");
        try {
            MapProcessor creator = new MapProcessor.MapProcessorBuilder(pluginPath, outputPath).build();
            return creator.processAllTarget();
        } catch (InvalidJsonException | JsonSyntaxException e) {
            LOGGER.error(e.getMessage());
            return GeneratorResult.INVALID_JSON_FORMAT;
        } catch (InvalidPathException e) {
            LOGGER.error(e.getMessage());
            return GeneratorResult.INVALID_OUTPATH;
        }
    }

    /**
     * generate summary info for convertor
     */
    public static GSummaryMap generateSummaryInfo() {
        return generateSummaryInfo(null);
    }

    /**
     * generate summary info for convertor with plugin
     */
    public static GSummaryMap generateSummaryInfo(String pluginPath) {
        String path = pluginPath;
        if (path == null) {
            path = whereAmI();
        }
        ConvertorProcessor processor = new ConvertorProcessor.ConvertorProcessorBuilder(path).dollar(true).build();
        return processor.processAllForConvertorSummary();
    }

    /**
     * function as follow two conditions:
     * 1.file '/mirror/gms.json' exits and is in jar package
     * 2.file 'gms.json' in jar package is loaded earlier than another files having same name with it
     */
    private static String whereAmI() {
        String path = "/mirror/gms.json";
        String tempPath = XMSRouterService.class.getResource(path).getPath();
        // use the jar
        if (tempPath == null || !tempPath.contains("!") && !tempPath.endsWith(".jar")) {
            return "";
        }
        if (tempPath.startsWith("/")) {
            tempPath = tempPath.substring(1);
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
