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

package com.huawei.hms.convertor.utils;

import com.huawei.hms.convertor.constants.Constant;
import com.huawei.hms.convertor.g2h.processor.GeneratorResult;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Slf4j
public class FileUtil {
    /**
     * generate g2h mapping json
     *
     * @param jsonString input string
     * @param filePath output mapping json file dir
     * @param fileName output mapping json file name
     * @return result code
     */
    public static GeneratorResult createJsonFile(String jsonString, String filePath, String fileName) {
        String fullPath = filePath + File.separator + fileName + ".json";

        try {
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            Writer write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            write.write(jsonString);
            write.flush();
            write.close();
        } catch (IOException e) {
            log.error("create json file fail.");
            return GeneratorResult.INVALID_OUTPATH;
        }

        return GeneratorResult.SUCCESS;
    }

    public static boolean delFile(String path) {
        boolean isDeleteSuccess = true;
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            isDeleteSuccess = file.delete();
        }
        return isDeleteSuccess;
    }

    public static void outPutString(String content, String filePath) {
        try (OutputStreamWriter fwriter =
            new OutputStreamWriter(new FileOutputStream(filePath, true), StandardCharsets.UTF_8)) {
            // true refer to addition, not cover
            fwriter.write(content);
            log.info("out put log in {}", filePath);
        } catch (IOException ex) {
            log.error("out put log error in {}.", filePath);
        }
    }

    /**
     * output log file
     *
     * @param filePath output log file path
     * @param message log file content
     */
    public static void outPutLog(String filePath, GeneratorResult message) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName = sf.format(new Date());
        filePath += "/" + fileName + ".log";
        delFile(filePath);
        outPutString(message.getDescription(), filePath);
    }

    public static String getProperty(String property) {
        Properties prop = new Properties();

        try {
            InputStream inputStream = FileUtil.class.getResourceAsStream(Constant.CONFIG_PROPERTIES);
            prop.load(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return prop.getProperty(property);
    }
}