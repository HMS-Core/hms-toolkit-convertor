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

package com.huawei.generator.method.builder;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.factory.MethodGeneratorFactory;

/**
 * Some common methods definitions.
 *
 * @since 2019-11-26
 */

public abstract class AbstractMethodBuilder<T> {
    protected MethodGeneratorFactory factory;

    protected AbstractMethodBuilder(MethodGeneratorFactory factory) {
        this.factory = factory;
    }

    /**
     * Build a method node from JClass and ClassNode.
     *
     * @param jClass the json definition
     * @param classNode the classNode containing this method
     * @return methodNode
     */
    public abstract MethodNode build(JClass jClass, ClassNode classNode);

    /**
     * Build a method node from JClass, ClassNode and a mapping.
     *
     * @param jClass the json definition
     * @param classNode the classNode containing this method
     * @param mapping mapping
     * @return methodNode
     */
    public abstract MethodNode build(JClass jClass, ClassNode classNode, JMapping<T> mapping);
}
