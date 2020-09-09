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

import com.huawei.hms.convertor.core.plugin.PluginConstant;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * load properties
 *
 * @since 2019-06-14
 */
@Slf4j
public final class PropertyUtil {
    private static final String DEFAULT_PROPERTIES_FILENAME = "/convertor.properties";

    private static final String CUSTOM_PROPERTIES_FILENAME = "customConvertor.properties";

    private static final Properties DEFAULT_PROPERTIES = new Properties();

    private static final Properties CUSTOM_PROPERTIES = new Properties();

    public static String readProperty(String key) {
        if (!loadProperties()) {
            log.error("load properties fail.");
            return "";
        }

        if (CUSTOM_PROPERTIES.containsKey(key)) {
            return CUSTOM_PROPERTIES.getProperty(key);
        }
        if (!DEFAULT_PROPERTIES.containsKey(key)) {
            log.error("{} not found", key);
            return "";
        }
        return DEFAULT_PROPERTIES.getProperty(key);
    }

    private static boolean loadProperties() {
        if (DEFAULT_PROPERTIES.isEmpty()) {
            try (InputStream inputStream = PropertyUtil.class.getResourceAsStream(DEFAULT_PROPERTIES_FILENAME)) {
                DEFAULT_PROPERTIES.load(inputStream);
            } catch (IOException e) {
                log.error("load {} failed", DEFAULT_PROPERTIES_FILENAME);
                return false;
            }
        }

        return loadCustomProperties();
    }

    private static boolean loadCustomProperties() {
        if (!CUSTOM_PROPERTIES.isEmpty()) {
            return true;
        }

        Path customPropertiesFilePath =
            Paths.get(PluginConstant.PluginDataDir.CONFIG_CACHE_PATH, CUSTOM_PROPERTIES_FILENAME);
        if (!Files.exists(customPropertiesFilePath)) {
            log.info("custom properties not config.");
            return true;
        }

        try (InputStream inputStream = Files.newInputStream(customPropertiesFilePath)) {
            CUSTOM_PROPERTIES.load(inputStream);
        } catch (IOException e) {
            log.error("load {} failed", CUSTOM_PROPERTIES_FILENAME);
            return false;
        }

        return true;
    }
}
