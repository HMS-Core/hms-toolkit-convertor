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

import com.huawei.generator.g2x.po.summary.Summary;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.g2x.processor.XmsConstants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * utils for summary path related
 *
 * @since 2020-02-28
 */
public class SummaryPathUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancerUtils.class);

    /**
     * parse summary from summary file according to strategy
     * 
     * @param rootPath summary root folder
     * @param strategyKind strategy
     * @return result, null for invalid summary path or broken summary file
     */
    public static Summary parseSummary(String rootPath, GeneratorStrategyKind strategyKind) {
        File root = new File(rootPath);
        if (!(root.exists() && root.isDirectory())) {
            return null;
        }
        // walk this folder to parse summary file
        File[] files = root.listFiles();
        if (files == null) {
            LOGGER.info("Can not find summary files");
            return null;
        }

        String targetSummaryName = getSummaryFileName(strategyKind);
        if (null == targetSummaryName) {
            LOGGER.info("{} strategy do not support summary", strategyKind);
            return null;
        }

        targetSummaryName += ".json";

        for (File f : files) {
            if (f.getName().equals(targetSummaryName)) {
                return buildSummaryFromJson(f);
            }
        }

        LOGGER.info("Can not find {} summary files", strategyKind);
        return null;
    }

    /**
     * achieve summary file name according to strategy, return null for exception
     * 
     * @param strategyKind strategy
     * @return name of the summary file
     */
    public static String getSummaryFileName(GeneratorStrategyKind strategyKind) {
        switch (strategyKind) {
            case G:
                return XmsConstants.G_SUMMARY_FILE_NAME;
            case GOrH:
                return XmsConstants.GH_SUMMARY_FILE_NAME;
            case HOrG:
                return XmsConstants.HG_SUMMARY_FILE_NAME;
        }
        return null;
    }

    /**
     * parse summary from json
     * 
     * @param f summary file
     * @return parse result, null for failure
     */
    public static Summary buildSummaryFromJson(File f) {
        try (InputStreamReader ins = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(ins, Summary.class);
        } catch (IOException e) {
            LOGGER.error("Invalid summary file");
            return new Summary();
        }
    }
}
