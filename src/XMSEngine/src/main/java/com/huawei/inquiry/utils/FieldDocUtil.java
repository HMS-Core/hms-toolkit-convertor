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

package com.huawei.inquiry.utils;

/**
 * util for fieldDoc.
 *
 * @since 2020-07-28
 */
public class FieldDocUtil {
    /**
     * get the full package class name for field signature.
     *
     * @param fieldSignature field's signature
     * @return full package class name
     */
    protected static String getParentName(String fieldSignature) {
        // field signature formed like com.xx.xxx.Xxx.FIELD
        // so remove the '.FIELD'
        if (!fieldSignature.contains(".")) {
            return "";
        }
        int endIndex = fieldSignature.lastIndexOf(".");
        return fieldSignature.substring(0, endIndex);
    }

    /**
     * get the simple name of field, namely no package classname.
     *
     * @param fieldSignature field's signature
     * @return the simple name of field
     */
    public static String simpleName(String fieldSignature) {
        if (fieldSignature != null && fieldSignature.contains(".")) {
            int index = fieldSignature.lastIndexOf(".");
            return fieldSignature.substring(index + 1);
        }
        return fieldSignature;
    }
}