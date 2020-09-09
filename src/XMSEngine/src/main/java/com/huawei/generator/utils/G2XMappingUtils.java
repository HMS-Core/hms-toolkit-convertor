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

import com.huawei.generator.g2x.po.map.MethodDesc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utils for G2XMapping
 *
 * @since 2019-11-27
 */
public class G2XMappingUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(G2XMappingUtils.class);

    // Prefixes basic types for use with ide
    public static String enhancePrimaryType(String type) {
        String result = type;
        if (TypeUtils.isPrimitiveType(type)) {
            result = "#BUILT_IN." + type;
        }
        return result;
    }

    // Erase all contents in angle brackets
    public static String degenerifyContains(String context) {
        if (context == null || context.isEmpty()) {
            return "";
        }
        int head = context.indexOf('<'); // Mark the position of the first left parenthesis
        if (head == -1) {
            return context;
        } else {
            int next = head + 1; // Check each character starting from head+1.
            int count = 1;
            do {
                if (context.charAt(next) == '<') {
                    count++;
                }
                if (context.charAt(next) == '>') {
                    count--;
                }
                next++;
                if (count == 0) {
                    String temp = context.substring(head, next);
                    context = context.replace(temp, "");
                    head = context.indexOf('<');
                    next = head + 1;
                    count = 1;
                }
            } while (head != -1);
        }
        return context.trim();
    }

    public static String dot2Dollar(String name) {
        String[] tmpStrs = name.split("\\.");
        String regex = "^[A-Z].*";
        StringBuilder result = new StringBuilder();
        for (String tmpStr : tmpStrs) {
            if (Pattern.matches(regex, tmpStr)) {
                result.append(tmpStr).append("$");
            } else {
                result.append(tmpStr).append(".");
            }
        }
        result.deleteCharAt(result.length() - 1);

        // Processing Variable Parameter Formats
        if (name.endsWith("...")) {
            result.append("[]");
        }
        return result.toString();
    }

    public static String normalizeKitName(String name) {
        String str = name;
        if (KitInfoRes.INSTANCE.getNormalizeKitMap().containsKey(str)) {
            str = KitInfoRes.INSTANCE.getNormalizeKitMap().get(str);
        } else {
            // upperCase first char
            char[] cs = str.toCharArray();
            if (cs[0] >= 'a' && cs[0] <= 'z') {
                cs[0] -= 32;
            }
            str = String.valueOf(cs);
        }
        return str;
    }

    public static String unNormalizeKitName(String name) {
        String str = name;
        if (KitInfoRes.INSTANCE.getUnnormalizeKitMap().containsKey(str)) {
            str = KitInfoRes.INSTANCE.getUnnormalizeKitMap().get(str);
        } else {
            str = str.toLowerCase(Locale.ENGLISH);
        }
        return str;
    }

    /**
     * Input method signature, which is output after the return field is deleted.
     * 
     * @param signature  method signature before processing
     * @return method signature after processing
     */
    public static String simplifySignature(String signature) {
        String[] strs = signature.split(" ");
        if (strs.length == 2) {
            return strs[1];
        }
        if (strs.length > 2) {
            throw new IllegalArgumentException("signature for convertor error occurred: " + signature);
        }
        return signature;
    }

    /**
     * erase weakTypes, replace each element by "*"
     * 
     * @param weakTypes weakTypes for erase
     * @return erased list of weakTypes
     */
    public static List<String> eraseWeakTypes(List<String> weakTypes) {
        List<String> list = new ArrayList<>(weakTypes.size());
        for (String weakType : weakTypes) {
            if (weakType.endsWith("...")) {
                list.add("*...");
            } else {
                list.add("*");
            }
        }
        return list;
    }

    /**
     * erase MDesc in AutoMethod, replace text and methodName by param#
     * 
     * @param desc target desc
     * @param size number of params
     * @param gName gName
     * @param xName xName
     */
    public static void eraseMDesc(MethodDesc desc, int size, String gName, String xName) {
        String oldMethodName = desc.getMethodName();
        String newMethodName = buildMDescMethodName(oldMethodName, size);
        desc.setMethodName(newMethodName);
        String newText = "\"" + buildMDescMethodName(gName, size) + "\"" + " will be replaced by " + "\""
            + buildMDescMethodName(xName, size) + "\"";
        desc.setText(newText);
    }

    /**
     * erase MDesc in AutoMethod, replace methodName by param#
     * 
     * @param desc target desc
     * @param size number of params
     */
    public static void eraseMDesc(MethodDesc desc, int size) {
        // replace methodName
        String oldMethodName = desc.getMethodName();
        String newName = buildMDescMethodName(oldMethodName, size);
        desc.setMethodName(newName);
    }

    /**
     * replace paramType by "*"
     * 
     * @param oldMethodName oldName
     * @param size size of params, it must be larger than 0
     * @return result
     */
    public static String buildMDescMethodName(String oldMethodName, int size) {
        if (size < 1) {
            return oldMethodName;
        }
        StringBuilder builder = new StringBuilder();
        String r = oldMethodName.split("\\(")[0] + "(";
        builder.append(r);
        int index = 0;
        for (; index < size - 1; index++) {
            builder.append("param").append(index).append(",");
        }
        builder.append("param").append(index).append(")");
        return builder.toString();
    }
}
