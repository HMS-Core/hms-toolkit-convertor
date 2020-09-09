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

import static com.huawei.generator.gen.AstConstants.WRAP_INST;

import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.NotNullTernaryNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.TypeUtils;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Converter for X2Z Value
 *
 * @since 2020-02-21
 */
public class Z2XValueConverter extends ValueConverter {
    public Z2XValueConverter(MethodNode methodNode, Component component) {
        super(methodNode, component);
    }

    @Override
    protected StatementNode handleNonSdkContainerValue(TypeNode node, String paramName, StatementNode rawNode) {
        return wrapZContainer(node, paramName);
    }

    @Override
    protected StatementNode handleNonWrapperValue(TypeNode node, String paramName, StatementNode rawNode) {
        if (methodNode.isGeneric(node, true)) {
            return convertGenericValue(rawNode, node);
        } else {
            return rawNode;
        }
    }

    @Override
    protected StatementNode handleWrapperValue(TypeNode node, String paramName) {
        return processWrapperValue(node, paramName);
    }

    /**
     * Process wrapper value
     *
     * @param type target to be converted
     */
    private StatementNode processWrapperValue(TypeNode type, String paramName) {
        if (TypeUtils.isNonSdkContainer(type)) {
            return wrapZContainer(type, paramName);
        }
        List<StatementNode> paramList = component.xWrapperParams(paramName);
        String gType = XMSUtils.xtoG(type.getTypeName());
        StatementNode node;
        if (TypeUtils.isGmsInterface(gType) || TypeUtils.isGmsAbstract(gType)) {
            node = NewNode.create(TypeNode.create(XMSUtils.getImplConstructor(type.getTypeName())), paramList);
        } else if (TypeUtils.isViewSubClass(TypeNode.create(gType), true)) {
            // new XView(getContext()).getXInstance(gReturn, hReturn) or
            // new XView(getContext()).getXInstance(param, 0)
            node =
                CallNode
                    .create(
                        NewNode.create(type,
                            Collections
                                .singletonList(CallNode.create(AstConstants.GET_CONTEXT, Collections.emptyList()))),
                        WRAP_INST, paramList);
        } else {
            node = NewNode.create(type, paramList);
        }
        return NotNullTernaryNode.create(VarNode.create(paramName), node);
    }

    private StatementNode wrapZContainer(TypeNode type, String paramName) {
        MethodNode mapper = createWrapMapper(type);
        String xType = extractGenericType(type);
        boolean isGenericIdentifier = TypeUtils.isGenericIdentifier(TypeNode.create(xType));
        String zType = component.toZMethodName(xType);
        zType = isGenericIdentifier ? "Object" : zType;
        String mapperName = getMapperName(type);
        AnonymousNode mapperNode =
            AnonymousNode.create(AstConstants.XMS_PACKAGE + ".Function<" + zType + ", " + xType + ">",
                Collections.emptyList(), methodNode.parent());
        mapperNode.methods().add(mapper);
        if (TypeUtils.isSparseArray(type)) {
            return CallNode.create(VarNode.create(AstConstants.XMS_UTILS), mapperName,
                Arrays.asList(VarNode.create(paramName), NewNode.create(mapperNode)));
        } else if (type.isArray()) {
            return CallNode.create(VarNode.create(AstConstants.XMS_UTILS), mapperName, Arrays.asList(
                VarNode.create(paramName), VarNode.create(type.getTypeName() + ".class"), NewNode.create(mapperNode)));
        } else {
            String containerType = TypeUtils.isNonSdkContainer(type) ? type.getTypeName() : null;
            return CastExprNode.create(TypeNode.create(containerType),
                CallNode.create(VarNode.create(AstConstants.XMS_UTILS), mapperName,
                    Arrays.asList(VarNode.create(paramName), NewNode.create(mapperNode))));
        }
    }

    /**
     * creates H,G -> X anonymous method
     */
    private MethodNode createWrapMapper(TypeNode node) {
        String xType = extractGenericType(node);
        MethodNode mapper = new MethodNode();
        ClassNode dummy = new ClassNode();
        dummy.setClassType("class");
        mapper.setParent(dummy);
        mapper.setModifiers(Collections.singletonList("public"));
        mapper.setReturnType(TypeNode.create(xType));
        mapper.setBody(new ArrayList<>());
        mapper.setName("apply");
        boolean isGenericIdentifier = TypeUtils.isGenericIdentifier(TypeNode.create(xType));
        if (isGenericIdentifier) {
            mapper.setParameters(Collections.singletonList(TypeNode.create(AstConstants.OBJECT)));
            mapper.body()
                .getStatements()
                .add(ReturnNode.create(
                    CastExprNode.create(TypeNode.create(xType), CallNode.create(VarNode.create(AstConstants.XMS_UTILS),
                        component.getXMethodName(), Collections.singletonList(VarNode.create(mapper.paramAt(0)))))));
        } else {
            String gTypeName = XMSUtils.xtoG(xType);
            boolean isAbstract = TypeUtils.isInterface(gTypeName) || TypeUtils.isGmsAbstract(gTypeName);
            String zType = component.toZMethodName(xType);
            List<StatementNode> varList = component.xWrapperParams(mapper.paramAt(0));
            mapper.setParameters(Collections.singletonList(TypeNode.create(zType)));
            mapper.body()
                .getStatements()
                .add(ReturnNode.create(NewNode.create(TypeNode.create(xType + (isAbstract ? ".XImpl" : "")), varList)));
        }
        return mapper;
    }

    private StatementNode convertGenericValue(StatementNode callNode, TypeNode target) {
        return CastExprNode.create(target, CallNode.create(VarNode.create(AstConstants.XMS_UTILS),
            component.getXMethodName(), Collections.singletonList(callNode)));
    }
}
