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

import static com.huawei.generator.gen.AstConstants.COLLECTION;
import static com.huawei.generator.gen.AstConstants.JAVA_LANG_CLASS;
import static com.huawei.generator.gen.AstConstants.LIST;

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
 * @since 2020-02-24
 */
public final class X2ZValueConverter extends ValueConverter {
    public X2ZValueConverter(MethodNode methodNode, Component component) {
        super(methodNode, component);
    }

    @Override
    protected StatementNode handleNonSdkContainerValue(TypeNode node, String valueName, StatementNode rawNode) {
        return processNonSdkContainerValue(node, valueName, rawNode);
    }

    @Override
    protected StatementNode handleNonWrapperValue(TypeNode node, String valueName, StatementNode rawNode) {
        if (Arrays.asList(COLLECTION, LIST).contains(node.getTypeName())) {
            String type = node.getTypeName().substring("java.util.".length());
            String func = "org.xms.g.utils.Utils.map" + type + "2GH";
            return CallNode.create(func, Arrays.asList(rawNode, VarNode.create(String.valueOf(component.isH()))));
        } else if (methodNode.isGeneric(node, true)) {
            setTargetType(AstConstants.OBJECT);
            return processGenericValue(methodNode, node, valueName);
        } else if (node.getTypeName().equals(AstConstants.JAVA_LANG_CLASS)) {
            VarNode classNode = VarNode.create(AstConstants.XMS_UTILS);
            List<StatementNode> parameters = new ArrayList<>();
            parameters.add(VarNode.create(valueName));
            String methodName = component.getZClassWithXClass();
            setTargetType(JAVA_LANG_CLASS);
            return CallNode.create(classNode, methodName, parameters);
        } else {
            return rawNode;
        }
    }

    @Override
    protected StatementNode handleWrapperValue(TypeNode node, String valueName) {
        if (isXVarArray(node) || isXArray(node)) {
            return processArrayValue(node, valueName);
        }
        if (TypeUtils.isInterface(node.getTypeName())) {
            // Cast is unnecessary here because we call the exact getter instead of getGInstance.
            String interfaceName = node.getTypeNameWithoutPackage();
            return NotNullTernaryNode.create(VarNode.create(valueName), CallNode.create(VarNode.create(valueName),
                component.getInstancePrefix() + interfaceName, Collections.emptyList()));
        } else {
            TypeNode zType = TypeNode.create(component.x2Z(node.getTypeName()));
            return CastExprNode.create(zType, NotNullTernaryNode.create(VarNode.create(valueName),
                CallNode.create(VarNode.create(valueName), component.getZInstance(), Collections.emptyList())));
        }
    }

    /**
     * @param typeNode, a TypeNode
     * @return typeNode is a XVarArray
     */
    private boolean isXVarArray(TypeNode typeNode) {
        return typeNode.isVarArg() && XMSUtils.isX(typeNode.getTypeName());
    }

    /**
     * @param typeNode, a TypeNode
     * @return typeNode is a XArray
     */
    private boolean isXArray(TypeNode typeNode) {
        return typeNode.isArray() && XMSUtils.isX(typeNode.getTypeName());
    }

    private StatementNode processArrayValue(TypeNode type, String param) {
        String zType = component.x2Z(type.getTypeName());
        // take care of index i, maybe outOfBound
        return CastExprNode.create(TypeNode.create(zType).setDimension(type.isVarArg() ? 1 : type.dimension()),
            CallNode.create(VarNode.create(AstConstants.XMS_UTILS), AstConstants.GENERIC_ARRAY_COPY,
                Arrays.asList(VarNode.create(param), VarNode.create(zType + ".class"),
                    VarNode.create("x -> (" + zType + ")x." + component.getZInstance() + "()"))));
    }

    private StatementNode processNonSdkContainerValue(TypeNode valueType, String param, StatementNode rawNode) {
        if (TypeUtils.needRemap(valueType)) {
            return mapContainer(valueType, param, rawNode);
        }
        return VarNode.create(param);
    }

    /**
     * @param valueType may be List, Iterable
     * @param param name of param
     * @return mapped container data structure
     */
    private StatementNode mapContainer(TypeNode valueType, String param, StatementNode rawNode) {
        String varName = methodNode.isGeneric(valueType, true) ? String.valueOf(component.isH()) : instanceGetter();
        if (TypeUtils.isIterable(valueType)) {
            return mapIterableContainer(param);
        } else if (TypeUtils.isList(valueType)) {
            return mapListContainer(valueType, rawNode);
        } else if (TypeUtils.isMap(valueType)) {
            return mapMapContainer(param, varName);
        } else if (TypeUtils.isSet(valueType)) {
            return mapSetContainer(valueType, param, varName);
        } else if (TypeUtils.isSparseArray(valueType) || TypeUtils.isIterator(valueType)) {
            return mapSparseArrayOrIterator(valueType, rawNode);
        } else if (valueType.isArray()) {
            return mapGenericArrayContainer(valueType, param, varName);
        } else {
            throw new IllegalStateException(valueType.getTypeName() + " is not supported now");
        }
    }

    /**
     * @param valueType the value type node
     * @param param param name
     * @return remapped statement
     */
    private StatementNode mapGenericArrayContainer(TypeNode valueType, String param, String varName) {
        String mapperName = AstConstants.MAP_ARRAY_TO_GH;
        List<TypeNode> defTypes = methodNode.returnType().getDefTypes();
        String upperType = TypeUtils.getUpperBound(valueType, defTypes);
        String zType = TypeNode.create(component.x2Z(upperType)).getTypeName();
        return CallNode.create(VarNode.create(AstConstants.XMS_UTILS), mapperName,
            Arrays.asList(VarNode.create(param), VarNode.create(zType + ".class"), VarNode.create(varName)));
    }

    private StatementNode mapSetContainer(TypeNode valueType, String param, String varName) {
        String mapperName = methodNode.isGeneric(valueType, true) ? AstConstants.MAP_SET_TO_GH : "mapSet";
        return CallNode.create(VarNode.create(AstConstants.XMS_UTILS), mapperName,
            Arrays.asList(VarNode.create(param), VarNode.create(varName)));
    }

    private StatementNode mapMapContainer(String param, String varName) {
        String mapperName = AstConstants.CONVERT_MAP;
        return CallNode.create(VarNode.create(AstConstants.XMS_UTILS), mapperName,
            Arrays.asList(VarNode.create(param), VarNode.create(varName)));
    }

    private StatementNode mapListContainer(TypeNode valueType, StatementNode rawNode) {
        String mapperName = AstConstants.MAP_LIST_TO_GH;
        return CastExprNode.create(TypeNode.create(valueType.getTypeName()),
            CallNode.create(VarNode.create(AstConstants.XMS_UTILS), mapperName,
                Arrays.asList(rawNode, VarNode.create(String.valueOf(component.isH())))));
    }

    private StatementNode mapIterableContainer(String param) {
        String mapperName = AstConstants.TRANSFORM_ITERABLE;
        return CallNode.create(VarNode.create(AstConstants.XMS_UTILS), mapperName,
            Arrays.asList(VarNode.create(param), VarNode.create(instanceGetterForGeneric())));
    }

    private String instanceGetter() {
        return "e -> " + AstConstants.XMS_UTILS + "." + component.getZInstance() + "()";
    }

    private String instanceGetterForGeneric() {
        return "e -> " + AstConstants.XMS_UTILS + "." + AstConstants.GET_INSTANCE_INTERFACE + "(e, " + component.isH()
            + ")";
    }

    private StatementNode processGenericValue(MethodNode method, TypeNode node, String valueName) {
        CallNode callNode = CallNode.create(VarNode.create(AstConstants.XMS_UTILS), AstConstants.GET_INSTANCE_INTERFACE,
            Arrays.asList(VarNode.create(valueName), VarNode.create(String.valueOf(component.isH()))));
        if (method.returnType() == null || method.returnType().getDefTypes() == null
            || method.returnType().getDefTypes().isEmpty()) {
            return genObj(method, callNode);
        }
        for (TypeNode typeNode : method.returnType().getDefTypes()) {
            if (typeNode.getTypeName().equals(node.getTypeName())) {
                TypeNode targetType;
                if (typeNode.getSuperClass() == null || typeNode.getSuperClass().isEmpty()) {
                    targetType = node;
                } else {
                    targetType = TypeNode.create(component.x2Z(typeNode.getSuperClass().get(0).getTypeName()));
                }
                setTargetType(targetType.getTypeName());
                return callNode;
            }
        }
        return genObj(method, callNode);
    }

    private StatementNode genObj(MethodNode method, StatementNode callNode) {
        if (null != method && method.parent() != null && method.parent().generics() != null
            && !method.parent().generics().isEmpty() && method.parent().generics().get(0) != null
            && method.parent().generics().get(0).getSuperClass() != null) {
            TypeNode typeNode = method.parent().generics().get(0);
            String type = typeNode.getSuperClass().get(0).getTypeName();
            TypeNode targetType = TypeNode.create(component.x2Z(type));
            setTargetType(targetType.getTypeName());
            return callNode;
        } else {
            setTargetType(AstConstants.OBJECT);
            return callNode;
        }
    }

    /**
     * creates G/H -> X mapper method with Utils' method
     */
    private StatementNode mapSparseArrayOrIterator(TypeNode xType, StatementNode callNode) {
        String zType = extractGenericType(methodNode.returnType());
        String xTypeName = xType.getGenericType().get(0).getTypeName();
        String containerType = xType.getTypeName();
        MethodNode mapper = createWrapMapper(methodNode, xTypeName);
        String mapperName = getMapperName(xType);
        AnonymousNode mapperNode =
            AnonymousNode.create(AstConstants.XMS_PACKAGE + ".Function<" + xTypeName + ", " + zType + ">",
                Collections.emptyList(), methodNode.parent());
        mapperNode.methods().add(mapper);
        return CastExprNode.create(TypeNode.create(containerType), CallNode.create(
            VarNode.create(AstConstants.XMS_UTILS), mapperName,
            Arrays.asList(CastExprNode.create(TypeNode.create(containerType), callNode), NewNode.create(mapperNode))));
    }

    /**
     * creates G/H -> X anonymous function method
     */
    private MethodNode createWrapMapper(MethodNode node, String xTypeName) {
        String zType = extractGenericType(node.returnType());
        MethodNode mapper = new MethodNode();
        ClassNode dummy = new ClassNode();
        dummy.setClassType("class");
        mapper.setParent(dummy);
        mapper.setModifiers(Collections.singletonList("public"));
        mapper.setReturnType(TypeNode.create(zType));
        mapper.setBody(new ArrayList<>());
        mapper.setName("apply");
        mapper.setParameters(Collections.singletonList(TypeNode.create(xTypeName)));
        mapper.body()
            .getStatements()
            .add(ReturnNode.create(CastExprNode.create(TypeNode.create(zType),
                CallNode.create(VarNode.create(AstConstants.XMS_UTILS), AstConstants.GET_INSTANCE_INTERFACE, Arrays
                    .asList(VarNode.create(methodNode.paramAt(0)), VarNode.create(String.valueOf(component.isH())))))));
        return mapper;
    }
}
