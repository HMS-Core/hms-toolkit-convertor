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
 * data struct used to store condition type and dependency
 *
 * @since 2020-04-13
 */
public class StructCondition {
    /**
     * used to store dependency type
     */
    public String type;

    /**
     * used to store dependency
     */
    public String dependency;

    boolean isSatisfied = false;

    public StructCondition() {
        this.type = "";
        this.dependency = "";
    }
}
