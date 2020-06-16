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

package com.huawei.codebot.analyzer.x2y.java.method;

/**
 * an entity that represents a unique method call in the whole java package
 *
 * @since 2020-04-14
 */
class MethodCall {
    /**
     * a qualified name string of this method
     */
    private String qualifier;
    /**
     * a simple name string of this method
     */
    private String simpleName;

    MethodCall(String qualifier, String simpleName) {
        this.qualifier = qualifier;
        this.simpleName = simpleName;
    }

    /**
     * @return qualified name of this method
     */
    String getFullName() {
        return qualifier + "." + simpleName;
    }

    /**
     * a getter of {@link #qualifier}
     *
     * @return the qualified name string of this method
     */
    String getQualifier() {
        return qualifier;
    }

    /**
     * a setter of {@link #qualifier}
     *
     * @param qualifier qualified name string
     */
    void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    /**
     * a getter of {@link #simpleName}
     *
     * @return the simple name of this method
     */
    String getSimpleName() {
        return simpleName;
    }

    /**
     * a setter of {@link #simpleName}
     *
     * @param simpleName simple name string
     */
    void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }
}
