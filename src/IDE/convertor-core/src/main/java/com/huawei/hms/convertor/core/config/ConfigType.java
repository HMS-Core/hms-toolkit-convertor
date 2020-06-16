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

package com.huawei.hms.convertor.core.config;

import lombok.Getter;

/**
 * Config type
 *
 * @since 2020-04-03
 */
@Getter
public enum ConfigType {
    PLUGIN(1, "Plugin global configuration"),

    PROJECT(2, "project configuration");

    private int code;

    private String name;

    ConfigType(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
