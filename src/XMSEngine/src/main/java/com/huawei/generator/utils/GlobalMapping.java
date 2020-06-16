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

import com.huawei.generator.json.JClass;

import java.util.HashMap;
import java.util.Map;

/**
 * The collection of all of the used xClass, gClass and hClass.
 *
 * @since 2019-11-16
 */
public class GlobalMapping {
    private static final Map<String, GlobalMapping> XMAPPINGS = new HashMap<>();

    private static final Map<String, GlobalMapping> HMAPPINGS = new HashMap<>();

    private static final Map<String, JClass> DEGENERIGY_MAP = new HashMap<>();

    private String g;

    private String h;

    private String x;

    public static Map<String, GlobalMapping> getXmappings() {
        return XMAPPINGS;
    }

    public static Map<String, GlobalMapping> getHmappings() {
        return HMAPPINGS;
    }

    public static Map<String, JClass> getDegenerigyMap() {
        return DEGENERIGY_MAP;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }
}
