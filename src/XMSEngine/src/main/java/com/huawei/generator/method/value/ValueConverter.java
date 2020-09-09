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

package com.huawei.generator.method.value;

import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.TypeUtils;
import com.huawei.generator.utils.XMSUtils;

/**
 * Converter for Some special Values
 *
 * @since 2020-02-14
 */
public abstract class ValueConverter {
    protected MethodNode methodNode;

    protected Component component;

    private boolean needToAssign = false;

    private String targetType;

    protected ValueConverter(MethodNode methodNode, Component component) {
        this.methodNode = methodNode;
        this.component = component;
    }

    static String getMapperName(TypeNode node) {
        String containerType = node.getTypeName();
        String mapperName;
        if (TypeUtils.isMap(node)) {
            mapperName = AstConstants.CONVERT_MAP;
        } else if (TypeUtils.isSparseArray(containerType) || node.isArray()) {
            mapperName = AstConstants.GENERIC_ARRAY_COPY;
        } else if (TypeUtils.isIterable(node)) {
            mapperName = AstConstants.TRANSFORM_ITERABLE;
        } else if (TypeUtils.isIterator(node)) {
            mapperName = AstConstants.TRANSFORM_ITERATOR;
        } else {
            mapperName = AstConstants.MAP_COLLECTION;
        }
        return mapperName;
    }

    /**
     * Get type of element
     *
     * @param node Represents array, vararg, List, Set, Map, SparseArray and so on
     * @return the type of element
     */
    static String extractGenericType(TypeNode node) {
        if (node.isArray()) {
            return node.getTypeName();
        }
        if (node.isVarArg()) {
            return node.getTypeName();
        }
        if (TypeUtils.isMap(node)) {
            // this is a map container
            return node.getGenericType().get(1).getTypeName();
        } else {
            // this is a collection container
            TypeNode genericType = node.getGenericType().get(0);
            if (genericType.getSuperClass() == null) {
                // no generic extends keyword
                // java.util.List<org.xms.g.xxx> pattern, return org.xms.g.XXX
                return genericType.getTypeName();
            } else {
                // java.util.List< ? extends org.xms.g.XXX> pattern, return org.xms.g.XXX
                return genericType.getSuperClass().get(0).getTypeName();
            }
        }
    }

    /**
     * Handle value of non sdk container
     * 
     * @param node type node
     * @param paramName parameter's name
     * @param rawNode raw node
     * @return handled value
     */
    protected abstract StatementNode handleNonSdkContainerValue(TypeNode node, String paramName, StatementNode rawNode);

    /**
     * Handle value with non wrapper
     * 
     * @param node type node
     * @param paramName value name
     * @param rawNode raw node
     * @return handled value
     */
    protected abstract StatementNode handleNonWrapperValue(TypeNode node, String paramName, StatementNode rawNode);

    /**
     * Handle value with wrapper
     * 
     * @param node type node
     * @param paramName parameter's name
     * @return handled value
     */
    protected abstract StatementNode handleWrapperValue(TypeNode node, String paramName);

    public StatementNode convertValue(TypeNode node, String paramName, StatementNode rawNode) {
        if (isValueNeedWrap(node)) {
            return handleWrapperValue(node, paramName);
        } else if (TypeUtils.isNonSdkContainer(node)) {
            return handleNonSdkContainerValue(node, paramName, rawNode);
        } else {
            return handleNonWrapperValue(node, paramName, rawNode);
        }
    }

    /**
     * When the type is g, h or x type, it needs to be wrapped.
     *
     * @param type type node
     * @return return true if need, otherwise false
     */
    private boolean isValueNeedWrap(TypeNode type) {
        String typeName = type.getTypeName();
        return XMSUtils.isX(typeName);
    }

    void setTargetType(String type) {
        needToAssign = true;
        this.targetType = type;
    }

    public boolean isNeedToAssign() {
        return needToAssign;
    }

    public String getTargetType() {
        return targetType;
    }
}
