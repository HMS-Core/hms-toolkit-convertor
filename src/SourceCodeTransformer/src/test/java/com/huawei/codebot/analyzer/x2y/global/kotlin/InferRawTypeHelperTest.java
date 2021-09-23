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

public class InferRawTypeHelperTest {


    @org.junit.Test
    public void getRawType() {


    }

    private String inferRawType(String funcName) {
        switch (funcName) {
            case "Array":
            case "arrayOf":
            case "arrayOfNulls":
            case "emptyArray":
                return KotlinBasicType.ARRAY.getQualifiedName();
            case "booleanArrayOf":
                return KotlinBasicType.BOOLEAN_ARRAY.getQualifiedName();
            case "byteArrayOf":
                return KotlinBasicType.BYTE_ARRAY.getQualifiedName();
            case "charArrayOf":
                return KotlinBasicType.CHAR_ARRAY.getQualifiedName();
            case "doubleArrayOf":
                return KotlinBasicType.DOUBLE_ARRAY.getQualifiedName();
            case "floatArrayOf":
                return KotlinBasicType.FLOAT_ARRAY.getQualifiedName();
            case "intArrayOf":
                return KotlinBasicType.INT_ARRAY.getQualifiedName();
            case "longArrayOf":
                return KotlinBasicType.LONG_ARRAY.getQualifiedName();
            case "shortArrayOf":
                return KotlinBasicType.SHORT_ARRAY.getQualifiedName();
            case "List":
            case "listOf":
            case "listOfNotNull":
            case "emptyList":
                return KotlinBasicType.LIST.getQualifiedName();
            case "arrayListOf":
                return KotlinBasicType.ARRAY_LIST.getQualifiedName();
            case "mutableListOf":
                return KotlinBasicType.MUTABLE_LIST.getQualifiedName();
            case "Map":
            case "mapOf":
                return KotlinBasicType.MAP.getQualifiedName();
            case "hashMapOf":
                return KotlinBasicType.HASH_MAP.getQualifiedName();
            case "linkedMapOf":
                return KotlinBasicType.LINKED_HASH_MAP.getQualifiedName();
            case "mutableMapOf":
                return KotlinBasicType.MUTABLE_MAP.getQualifiedName();
            case "sortedMapOf":
                return KotlinBasicType.SORTED_MAP.getQualifiedName();
            case "Set":
            case "setOf":
                return KotlinBasicType.SET.getQualifiedName();
            case "hashSetOf":
                return KotlinBasicType.HASH_SET.getQualifiedName();
            case "linkedSetOf":
                return KotlinBasicType.LINKED_HASH_SET.getQualifiedName();
            case "mutableSetOf":
                return KotlinBasicType.MUTABLE_SET.getQualifiedName();
            case "sortedSetOf":
                return KotlinBasicType.TREE_SET.getQualifiedName();
            default:
                return null;
        }
    }
}