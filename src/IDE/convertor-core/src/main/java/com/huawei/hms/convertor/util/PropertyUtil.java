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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * load properties
 *
 * @since 2019-06-14
 */
@Slf4j
public final class PropertyUtil {
    private static final String PATH = "/convertor.properties";

    private static Properties properties = new Properties();

    public static String readProperty(String key) {
        if (properties.isEmpty()) {
            try (InputStream inputStream = PropertyUtil.class.getResourceAsStream(PATH)) {
                properties.load(inputStream);
            } catch (IOException e) {
                log.error("load {} failed", PATH);
                return "";
            }
        }

        if (!properties.containsKey(key)) {
            log.error("{} not found", key);
            return "";
        }
        return properties.getProperty(key);
    }
}
