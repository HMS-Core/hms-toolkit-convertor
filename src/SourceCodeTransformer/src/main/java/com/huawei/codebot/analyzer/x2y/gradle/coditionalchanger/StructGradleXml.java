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

package com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger;

/**
 * StructGradleXml contains datastruct Condition and Operation
 *
 * @since 2020-04-13
 */
public class StructGradleXml {
    /**
     * condition struct is used to determine whether satisfy conditions
     */
    public StructCondition condition;

    /**
     * if satisfy conditions and then use operation struct to change
     */
    public StructOperation operation;

    public StructGradleXml() {
        condition = new StructCondition();
        operation = new StructOperation();
    }
}
