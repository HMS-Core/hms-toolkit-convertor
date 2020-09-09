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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextUtil {
    /**
     * delete all context from brackets
     *
     * @param context, class name, field name or method name
     * @return context after convert
     */
    public static String degenerifyContains(String context) {
        int head = context.indexOf('<'); // mark the first index of left bracket
        if (head == -1) {
            return context;
        } else {
            int next = head + 1; // check every char from the head+1 index
            int count = 1;
            do {
                if (context.charAt(next) == '<') {
                    count++;
                } else if (context.charAt(next) == '>') {
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
        return context;
    }
}