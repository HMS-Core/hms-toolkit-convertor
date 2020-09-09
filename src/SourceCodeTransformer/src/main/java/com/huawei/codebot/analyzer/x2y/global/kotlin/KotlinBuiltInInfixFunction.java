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

package com.huawei.codebot.analyzer.x2y.global.kotlin;

/**
 * Holds Kotlin's Built-in InfixFunction.
 *
 * @author sirnple
 * @since 2020/5/27
 */
public enum KotlinBuiltInInfixFunction {
    SHL("shl", "signed shift left"),
    SHR("shr", "signed shift right"),
    USHR("ushr", "unsigned shift right"),
    AND("and", "bitwise and"),
    OR("or", "bitwise or"),
    XOR("xor", "bitwise xor"),
    INV("inv", "bitwise inversion");

    private String functionName;

    private String desc;

    KotlinBuiltInInfixFunction(String functionName, String desc) {
        this.functionName = functionName;
        this.desc = desc;
    }

    /**
     * Function's name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * A simple description of this function
     */
    public String getDesc() {
        return desc;
    }
}
