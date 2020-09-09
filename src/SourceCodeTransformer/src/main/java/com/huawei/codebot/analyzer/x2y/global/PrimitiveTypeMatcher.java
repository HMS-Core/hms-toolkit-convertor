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

package com.huawei.codebot.analyzer.x2y.global;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * store Java 8 kinds of types into primitive2Wrapper map and wrapper2Primitive map
 *
 * @since 3.0.1
 */
public class PrimitiveTypeMatcher {
    private final Map<String, String> primitive2Wrapper;
    private final Map<String, String> wrapper2Primitive;

    public PrimitiveTypeMatcher() {
        primitive2Wrapper = new HashMap<>();
        wrapper2Primitive = new HashMap<>();
        JavaPrimitiveType[] javaPrimitiveType = JavaPrimitiveType.values();
        for (JavaPrimitiveType primitiveType : javaPrimitiveType) {
            primitive2Wrapper.put(primitiveType.primitiveString, primitiveType.wrapperString);
            wrapper2Primitive.put(primitiveType.wrapperString, primitiveType.primitiveString);
        }
    }

    /**
     * determine weather typeName is java primitive type
     *
     * @param typeName type name
     * @return is java primitive type or not
     */
    public boolean isJavaPrimitiveType(String typeName) {
        return primitive2Wrapper.containsKey(typeName);
    }

    /**
     * determine weather typeName is java wrapper type
     *
     * @param typeName type name
     * @return is java wrapper type or not
     */
    public boolean isJavaWrapperType(String typeName) {
        return wrapper2Primitive.containsKey(typeName);
    }

    /**
     * find wrapper type from primitive type
     *
     * @param typeName primitive type
     * @return wrapper type
     */
    public String getWrapperType(String typeName) {
        return primitive2Wrapper.get(typeName);
    }

    /**
     * find primitive type from wrapper type
     *
     * @param typeName wrapper type
     * @return primitive type
     */
    public String getPrimitiveType(String typeName) {
        return wrapper2Primitive.get(typeName);
    }

    /**
     * determine weather primitive arg type and primitive param type are matched
     *
     * @param argTypeName arg type name
     * @param paramTypeName param type name
     * @return true if arg type and param type are matched
     */
    public boolean primitiveTypeMathch(String argTypeName, String paramTypeName) {
        if (StringUtils.isEmpty(argTypeName) && StringUtils.isEmpty(paramTypeName)) {
            return true;
        }
        if (StringUtils.isEmpty(argTypeName) || StringUtils.isEmpty(paramTypeName)) {
            return false;
        }

        if (argTypeName.equals(paramTypeName)) {
            return true;
        }

        if (argTypeName.equals(JavaPrimitiveType.BYTE.primitiveString)
                && !paramTypeName.equals(JavaPrimitiveType.BOOLEAN.primitiveString)) {
            return true;
        }

        if (argTypeName.equals(JavaPrimitiveType.SHORT.primitiveString)
                && !paramTypeName.equals(JavaPrimitiveType.BOOLEAN.primitiveString)
                && !paramTypeName.equals(JavaPrimitiveType.BYTE.primitiveString)
                && !paramTypeName.equals(JavaPrimitiveType.CHARACTER.primitiveString)) {
            return true;
        }

        if (argTypeName.equals(JavaPrimitiveType.CHARACTER.primitiveString)
                && !paramTypeName.equals(JavaPrimitiveType.BOOLEAN.primitiveString)
                && !paramTypeName.equals(JavaPrimitiveType.BYTE.primitiveString)
                && !paramTypeName.equals(JavaPrimitiveType.SHORT.primitiveString)) {
            return true;
        }

        if (argTypeName.equals(JavaPrimitiveType.INTEGER.primitiveString)
                && (paramTypeName.equals(JavaPrimitiveType.LONG.primitiveString)
                || paramTypeName.equals(JavaPrimitiveType.FLOAT.primitiveString)
                || paramTypeName.equals(JavaPrimitiveType.DOUBLE.primitiveString))) {
            return true;
        }

        if (argTypeName.equals(JavaPrimitiveType.LONG.primitiveString)
                && (paramTypeName.equals(JavaPrimitiveType.FLOAT.primitiveString)
                || paramTypeName.equals(JavaPrimitiveType.DOUBLE.primitiveString))) {
            return true;
        }

        return argTypeName.equals(JavaPrimitiveType.FLOAT.primitiveString)
                && paramTypeName.equals(JavaPrimitiveType.DOUBLE.primitiveString);
    }
}
