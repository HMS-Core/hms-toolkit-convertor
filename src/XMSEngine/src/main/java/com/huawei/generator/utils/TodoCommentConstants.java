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

/**
 * TodoCommentConstants
 *
 * @since 2019-12-15
 */
public class TodoCommentConstants {
    public static final String EMPTY_H_NAME_TODO_WHOLE_METHOD_REWRITE =
        "// TODO: The whole method need to be " + "rewritten, including the parameter list.";

    public static final String EMPTY_H_NAME_TODO_USE_OBJECT_TEMPORARY = "// TODO: As HMS does not have corresponding"
        + " class, we temporary assume HMS class is java.lang.Object type.";

    public static final String EMPTY_H_NAME_TODO_REWRITE_CODE_ACCORDING_REQUIREMENT =
        "// TODO: You should modify the " + "parameter list and/or method body according to requirement";

    public static final String X_IMPL_INNER_CLASS_TODO_HANDLE_COMPILE_ISSUE = "// TODO: As HMS does not have "
        + "corresponding abstract class or interface, the inner class XImpl was created to handle compiling errors";

    public static final String PARCELABLE_EMPTY_H_NAME_TODO = "// TODO: As HMS does not have corresponding abstract "
        + "class or interface, you should write your own code according to requirement";

    public static final String TODO_START_PREFIX = "// TODO start ";

    public static final String TODO_END_PREFIX = "// TODO end ";

    public static final String OUTPUT_TODO_COMMENT = "OUTPUT_TODO_COMMENT";

    public static final String USER_CUSTOM_CODE_IN_CLASS_BODY = "USER_CUSTOM_CODE_IN_CLASS_BODY_";

    public static final String USER_CUSTOM_CODE_OUT_CLASS_BODY = "USER_CUSTOM_CODE_OUT_CLASS_BODY_";

    public static final String USER_CUSTOM_CODE_FOR_METHOD_HEAD = "USER_CUSTOM_CODE_FOR_METHOD_HEAD_";

    public static final String USER_CUSTOM_CODE_IN_MEIHOD_BODY_WRAPPER = "USER_CUSTOM_CODE_IN_METHOD_BODY_WRAPPER_";
}
