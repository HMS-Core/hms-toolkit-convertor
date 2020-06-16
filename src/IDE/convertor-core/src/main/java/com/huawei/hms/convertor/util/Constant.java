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

import java.time.format.DateTimeFormatter;

/**
 * Global constant
 *
 * @since 2019-06-10
 */
public final class Constant {
    public static final String PLUGIN_NAME = "HMS Convertor";

    public static final String GBK = "GBK";
    public static final String UTF8 = "UTF-8";
    public static final String LINE_SEPARATOR = "\n";
    public static final String SEPARATOR = "/";

    public static final DateTimeFormatter BASIC_ISO_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final String EXTENSION_ZIP = ".zip";
    public static final String NA = "N/A";
    public static final String ALL = "All...";
    public static final int FIRST_INDEX = 0;

    public static final String CONFIG_CACHE_PATH = System.getProperty("user.home") + "/.hmstoolkit/convertor/config/";
    public static final String PLUGIN_CACHE_PATH = System.getProperty("user.home") + "/.hmstoolkit/convertor/";
    public static final String PLUGIN_LOG_PATH = System.getProperty("user.home") + "/.hmstoolkit/logs";
}
