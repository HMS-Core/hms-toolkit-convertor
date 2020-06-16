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

package com.huawei.generator.gen.classes;

/**
 * Method
 *
 * @since 2020-01-10
 */
class Method {
    final String className;

    final String methodName;

    Method(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    boolean isMatch(String cls, String method) {
        return className.equals(cls) && methodName.equals(method);
    }
}
