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

package com.huawei.generator.method.factory;

import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.custom.XWrapperConstructorNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.component.ComponentContainer;
import com.huawei.generator.method.gen.BodyGenerator;

/**
 * Factory for method generator.
 *
 * @since 2020-03-13
 */
public interface MethodGeneratorFactory {
    /**
     * Gets the component container used in this factory.
     *
     * @return component container
     */
    ComponentContainer componentContainer();

    /**
     * Creates a generator for a normal X constructor.
     *
     * @param methodNode the method node to be filled
     * @param mapping the mapping for specifying this constructor.
     * @return body generator
     */
    BodyGenerator createConstructorGenerator(MethodNode methodNode, JMapping<JMethod> mapping);

    /**
     * Creates a generator for method dynamicCast.
     *
     * @param methodNode the method node to be filled
     * @param def the json definition of the class
     * @return body generator
     */
    BodyGenerator createDynamicCastGenerator(MethodNode methodNode, JClass def);

    /**
     * Creates a generator for method isInstance.
     *
     * @param methodNode the method node to be filled
     * @param def the json definition of the class
     * @return body generator
     */
    BodyGenerator createIsInstanceGenerator(MethodNode methodNode, JClass def);

    /**
     * Creates a generator for a ZImpl method.
     *
     * @param methodNode the method node to be filled
     * @param xMethodNode xms method node
     * @param def the json definition of the class
     * @param mapping the mapping for specifying this method
     * @param component the component used for Z
     * @return body generator
     */
    BodyGenerator createZImplMethodGenerator(MethodNode methodNode, MethodNode xMethodNode, JClass def,
        JMapping<JMethod> mapping, Component component);

    /**
     * Creates a generator for getZInstanceXName for interfaces.
     *
     * @param methodNode the method node to be filled
     * @return body generator
     */
    BodyGenerator createGetZInstanceGenerator(MethodNode methodNode);

    /**
     * Creates a generator for method getGInstance or getHInstance for interfaces.
     *
     * @param methodNode the method node to be filled
     * @param anonymousZImpl the anonymous ZImpl to be returned in this method node
     * @param component the component used for Z
     * @return body generator
     */
    BodyGenerator createGetInterfaceInstanceGenerator(MethodNode methodNode, AnonymousNode anonymousZImpl,
        Component component);

    /**
     * Creates a generator for field getter
     *
     * @param methodNode the method node to be filled
     * @param def the json definition of the class
     * @param mapping the mapping for specifying this method
     * @return body generator
     */
    BodyGenerator createFieldGetterGenerator(MethodNode methodNode, JClass def, JMapping<JFieldOrMethod> mapping);

    /**
     * Creates a generator for routing method.
     *
     * @param methodNode the method node to be filled
     * @param def the json definition of the class
     * @param mapping the mapping for specifying this method
     * @return body generator
     */
    BodyGenerator createRoutingMethodGenerator(MethodNode methodNode, JClass def, JMapping<JMethod> mapping);

    /**
     * Creates a generator for wrapper constructor.
     *
     * @param node the constructor node to be filled.
     * @return body generator
     */
    BodyGenerator createWrapperCtorGenerator(XWrapperConstructorNode node);

    /**
     * Creates a generator for createFromParcel.
     *
     * @param methodNode the method node to be filled
     * @param def the json definition of the class
     * @return body generator
     */
    BodyGenerator createFromParcelGenerator(MethodNode methodNode, JClass def);

    /**
     * Creates a generator for newArray in Creators.
     *
     * @param methodNode the method node to be filled
     * @param def the json definition of the class
     * @return body generator
     */
    BodyGenerator createNewArrayGenerator(MethodNode methodNode, JClass def);

    /**
     * Creates a generator for getZInstance for XGettables.
     *
     * @param component the component used for Z
     * @return body generator
     */
    BodyGenerator createGetZInstanceGenerator(Component component);
}
