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

package com.huawei.generator.g2x.po.map.convertor;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;

/**
 * class, method, field map
 *
 * @since 2019-1-3
 */
public class GSummaryMap {
    @Expose
    private Map<String, JDesc> class2DescMap = new HashMap<>();

    @Expose
    private Map<String, JDesc> method2DescMap = new HashMap<>();

    @Expose
    private Map<String, JDesc> field2DescMap = new HashMap<>();

    public GSummaryMap() {
    }

    public Map<String, JDesc> getClass2DescMap() {
        return class2DescMap;
    }

    public Map<String, JDesc> getMethod2DescMap() {
        return method2DescMap;
    }

    public Map<String, JDesc> getField2DescMap() {
        return field2DescMap;
    }
}
