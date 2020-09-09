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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);
    private static Properties properties = new Properties();

    static {
        try (InputStream ins = PropertyUtils.class.getResourceAsStream("/generator.properties")) {
            properties.load(ins);
        } catch (IOException e) {
            LOGGER.error("Load generator.properties failed");
        }
    }

    public static String getProperty(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        return properties.getProperty(key);
    }
}
