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

/**
 * Holds Java primitive type info.
 *
 * @author sirnple
 * @since 2020/5/25
 */
public enum JavaPrimitiveType {
    BOOLEAN("#BUILT_IN.boolean", "java.lang.Boolean"),
    BYTE("#BUILT_IN.byte", "java.lang.Byte"),
    CHARACTER("#BUILT_IN.char", "java.lang.Character"),
    SHORT("#BUILT_IN.short", "java.lang.Short"),
    INTEGER("#BUILT_IN.int", "java.lang.Integer"),
    LONG("#BUILT_IN.long", "java.lang.Long"),
    FLOAT("#BUILT_IN.float", "java.lang.Float"),
    DOUBLE("#BUILT_IN.double", "java.lang.Double");

    /**
     * A specified name of primitive type
     */
    public final String primitiveString;
    /**
     * The primitive type wrapper's qualified name.
     */
    public final String wrapperString;

    JavaPrimitiveType(String primitive, String wrapper) {
        this.primitiveString = primitive;
        this.wrapperString = wrapper;
    }
}
