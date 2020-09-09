/*
 *
 *  * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.huawei.codebot.analyzer.x2y.global;

import com.huawei.codebot.framework.exception.CodeBotException;

/**
 * Holds qualified name of kotlin basic type info.
 *
 * @author sirnple
 * @since 2020/5/26
 */
public enum KotlinBasicType {
    ANY("kotlin.Any"),
    BOOLEAN("kotlin.Boolean"),
    BYTE("kotlin.Byte"),
    CHAR("kotlin.Char"),
    SHORT("kotlin.Short"),
    INT("kotlin.Int"),
    LONG("kotlin.Long"),
    FLOAT("kotlin.Float"),
    DOUBLE("kotlin.Double"),
    STRING("java.lang.String"),
    ARRAY("kotlin.Array"),
    BOOLEAN_ARRAY("kotlin.BooleanArray"),
    BYTE_ARRAY("kotlin.ByteArray"),
    CHAR_ARRAY("kotlin.CharArray"),
    SHORT_ARRAY("kotlin.ShortArray"),
    INT_ARRAY("kotlin.IntArray"),
    LONG_ARRAY("kotlin.LongArray"),
    FLOAT_ARRAY("kotlin.FloatArray"),
    DOUBLE_ARRAY("kotlin.DoubleArray"),
    LIST("kotlin.collections.List"),
    ARRAY_LIST("kotlin.collections.ArrayList"),
    MUTABLE_LIST("kotlin.collections.MutableList"),
    MAP("kotlin.collections.Map"),
    HASH_MAP("kotlin.collections.HashMap"),
    LINKED_HASH_MAP("kotlin.collections.LinkedHashMap"),
    MUTABLE_MAP("kotlin.collections.MutableMap"),
    SORTED_MAP("kotlin.collections.SortedMap"),
    SET("kotlin.collections.Set"),
    HASH_SET("kotlin.collections.HashSet"),
    LINKED_HASH_SET("kotlin.collections.LinkedHashSet"),
    MUTABLE_SET("kotlin.collections.MutableSet"),
    TREE_SET("kotlin.collections.TreeSet");

    private String qualifiedName;

    KotlinBasicType(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    /**
     * Convert a value of qualifiedName to a enum.
     *
     * @param qualifiedName QualifiedName.
     * @return A {@link KotlinBasicType} enum.
     */
    public static KotlinBasicType fromValue(String qualifiedName) throws CodeBotException {
        for (KotlinBasicType value : KotlinBasicType.values()) {
            if (value.getQualifiedName().equals(qualifiedName)) {
                return value;
            }
        }
        throw new CodeBotException("Failed to parse, arg " + qualifiedName + "is invalid value");
    }

    /**
     * Type qualified name.
     */
    public String getQualifiedName() {
        return qualifiedName;
    }
}
