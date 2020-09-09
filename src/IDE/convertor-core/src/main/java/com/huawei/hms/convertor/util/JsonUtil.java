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

package com.huawei.hms.convertor.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.ParserConfig;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Json util
 *
 * @since 2019-07-01
 */
@Slf4j
public final class JsonUtil {
    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(false);
    }

    private JsonUtil() {
    }

    public static <T> T getResultList(String resultFilePath, Class<T> clazz) throws IOException, JSONException {
        if (resultFilePath == null || !new File(resultFilePath).exists()) {
            throw new FileNotFoundException("No result file generated! resultFilePath = " + resultFilePath);
        }

        String resultString = FileUtil.readToString(resultFilePath, StandardCharsets.UTF_8.toString());
        T wiseHubs = JSON.parseObject(resultString, clazz);
        return wiseHubs;
    }

    /**
     * Create json file
     */
    public static void createJsonFile(String jsonString, String filePath, String fileName) {
        String fullPath = filePath + Constant.UNIX_FILE_SEPARATOR + fileName + ".json";
        File file = new File(fullPath);

        if (!file.getParentFile().exists()) {
            boolean mkResult = file.getParentFile().mkdirs();
            if (!mkResult) {
                log.error("mkdirs failed");
                return;
            }
        }

        try (Writer write = new OutputStreamWriter(FileUtils.openOutputStream(file), StandardCharsets.UTF_8)) {
            String formattedJsonStr = jsonString;
            if (formattedJsonStr.contains("'")) {
                // Escape the single quote, because the string type in a JSON string can be enclosed in single quotes.
                formattedJsonStr = formattedJsonStr.replaceAll("'", "\\'");
            }
            if (formattedJsonStr.contains("\"")) {
                // Escape the double quote, because the string type in a JSON string can be enclosed in double quotes.
                formattedJsonStr = formattedJsonStr.replaceAll("\"", "\\\"");
            }

            if (formattedJsonStr.contains(System.lineSeparator())) {
                // Transform the carriage return, because the JSON string cannot contain explicit carriage return.
                formattedJsonStr = formattedJsonStr.replaceAll(System.lineSeparator(), "\\u000d\\u000a");
            }

            write.write(formattedJsonStr);
            write.flush();
        } catch (IOException e) {
            log.error("write file failed: ", e);
        }
    }
}
