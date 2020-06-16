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

import static com.huawei.generator.utils.XMSUtils.degenerify;
import static com.huawei.generator.utils.XMSUtils.removePackage;

import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;

/**
 * Function description
 *
 * @since 2020-03-23
 */
public class MappingUtils {
    public static boolean hasNonWrapperConstructor(JClass jClass) {
        return jClass.methods()
            .stream()
            .anyMatch(mapping -> isConstructor(jClass, mapping) && !mapping.isUnsupported());
    }

    public static boolean isConstructor(JClass jClass, JMapping<JMethod> mapping) {
        return mapping.g() != null && mapping.g().name().equals(removePackage(degenerify(jClass.gName())))
            && mapping.g().returnType().isEmpty();
    }

    /**
     * x method name
     *
     * @param mapping JMapping
     * @return x method name
     */
    public static String xMethodName(JMapping<JMethod> mapping) {
        return mapping.g().name();
    }
}
