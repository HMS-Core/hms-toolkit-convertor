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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.generator.g2x.po.summary.Summary;

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
            LOGGER.info("Invalid summary file");
            return null;
        }
    }
}
