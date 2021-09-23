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
    private static Map<String, KotlinBasicType> map = new HashMap<>();

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


    }


}
