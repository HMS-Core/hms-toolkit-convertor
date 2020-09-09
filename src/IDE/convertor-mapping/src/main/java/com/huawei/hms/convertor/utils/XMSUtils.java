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

package com.huawei.hms.convertor.utils;

import com.huawei.hms.convertor.constants.Constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @since 2019-11-12
 */

public class XMSUtils {
    private static final String BOOLEAN_TYPE = "boolean";

    private static final String BYTE_TYPE = "byte";

    private static final String CHAR_TYPE = "char";

    private static final String SHORT_TYPE = "short";

    private static final String INT_TYPE = "int";

    private static final String FLOAT_TYPE = "float";

    private static final String DOUBLE_TYPE = "double";

    private static final String LONG_TYPE = "long";

    private static final String VARARGS = "...";

    private static final List<String> PRIMITIVE_TYPES = Arrays.asList(BOOLEAN_TYPE, BYTE_TYPE, CHAR_TYPE, SHORT_TYPE,
        INT_TYPE, FLOAT_TYPE, DOUBLE_TYPE, LONG_TYPE, BOOLEAN_TYPE + VARARGS, BYTE_TYPE + VARARGS, CHAR_TYPE + VARARGS,
        SHORT_TYPE + VARARGS, INT_TYPE + VARARGS, FLOAT_TYPE + VARARGS, DOUBLE_TYPE + VARARGS, LONG_TYPE + VARARGS);

    public static boolean isPrimitiveType(String type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    public static void specializedProcess(Map<String, String> dependencyVersion) {
        for (Map.Entry<String, Map<String, String>> entry :  Constant.FORCE_BIND_DEP.entrySet()) {
            if (dependencyVersion.containsKey(entry.getKey())) {
                for (Map.Entry<String, String> entryDep :
                    Constant.FORCE_BIND_DEP.get(entry.getKey()).entrySet()) {
                    dependencyVersion.put(entryDep.getKey(), entryDep.getValue());
                }
            }
        }
    }
}
