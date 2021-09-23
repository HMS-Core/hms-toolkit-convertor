/*
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
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
package com.huawei.codebot.analyzer.x2y.global.kotlin;

import com.huawei.codebot.analyzer.x2y.global.KotlinBasicType;

import java.util.HashMap;
import java.util.Map;

/**
 * InferRawTypeHelper,
 * inferRawType kotlinBasicType mapping
 *
 * @since 2021-09-23
 */
public class InferRawTypeHelper {
    private static final Map<String, KotlinBasicType> map = new HashMap<>();

    static {
        map.put("Array", KotlinBasicType.ARRAY);
        map.put("arrayOf", KotlinBasicType.ARRAY);
        map.put("arrayOfNulls", KotlinBasicType.ARRAY);
        map.put("emptyArray", KotlinBasicType.ARRAY);

        map.put("booleanArrayOf", KotlinBasicType.BOOLEAN_ARRAY);
        map.put("byteArrayOf", KotlinBasicType.BYTE_ARRAY);
        map.put("charArrayOf", KotlinBasicType.CHAR_ARRAY);
        map.put("doubleArrayOf", KotlinBasicType.DOUBLE_ARRAY);
        map.put("floatArrayOf", KotlinBasicType.FLOAT_ARRAY);
        map.put("intArrayOf", KotlinBasicType.INT_ARRAY);
        map.put("longArrayOf", KotlinBasicType.LONG_ARRAY);
        map.put("shortArrayOf", KotlinBasicType.SHORT_ARRAY);

        map.put("List", KotlinBasicType.LIST);
        map.put("listOf", KotlinBasicType.LIST);
        map.put("listOfNotNull", KotlinBasicType.LIST);
        map.put("emptyList", KotlinBasicType.LIST);

        map.put("arrayListOf", KotlinBasicType.ARRAY_LIST);
        map.put("mutableListOf", KotlinBasicType.MUTABLE_LIST);

        map.put("Map", KotlinBasicType.MAP);
        map.put("mapOf", KotlinBasicType.MAP);
        map.put("hashMapOf", KotlinBasicType.HASH_MAP);
        map.put("linkedMapOf", KotlinBasicType.LINKED_HASH_MAP);
        map.put("mutableMapOf", KotlinBasicType.MUTABLE_MAP);
        map.put("sortedMapOf", KotlinBasicType.SORTED_MAP);

        map.put("Set", KotlinBasicType.SET);
        map.put("setOf", KotlinBasicType.SET);
        map.put("hashSetOf", KotlinBasicType.HASH_SET);
        map.put("linkedSetOf", KotlinBasicType.LINKED_HASH_SET);
        map.put("mutableSetOf", KotlinBasicType.MUTABLE_SET);
        map.put("sortedSetOf", KotlinBasicType.TREE_SET);
    }

    private InferRawTypeHelper() {
    }

    public static KotlinBasicType getRawType(String funcName) {
        return map.getOrDefault(funcName, null);
    }
}
