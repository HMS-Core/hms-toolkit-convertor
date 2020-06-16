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

package com.huawei.generator.classes;

import static com.huawei.generator.gen.classes.GHImplMethodSupplement.getAdditionalMethods;
import static com.huawei.generator.mirror.KClassUtils.getConstructorList;
import static com.huawei.generator.utils.XMSUtils.listMap;

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.FieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.ZImplClassNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.builder.CallSuperMethodBuilder;
import com.huawei.generator.method.builder.ZImplMethodBuilder;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.mirror.KClass;
import com.huawei.generator.mirror.SupersVisitor;
import com.huawei.generator.utils.MappingUtils;
import com.huawei.generator.utils.Modifier;
import com.huawei.generator.utils.TodoManager;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Factory for  creating ZImpl class nodes.
 *
 * @since 2020-02-28
 */
class ZImplFactory {
    void createZImplClass(MethodGeneratorFactory factory, ClassNode outerClass,
        Map<JMapping<JMethod>, MethodNode> xMethodMapping, Component component) {
        JClass def = outerClass.getJClass();
        String zName = component.zName(def);
        if (zName.isEmpty() || zName.equals(AstConstants.OBJECT)) {
            return;
        }
        List<JMethod> constructorList = getConstructorList(component.zName(def), component.getZClassList());
        if (constructorList.isEmpty()) {
            return;
        }
        ZImplClassNode zImpl = getEmptyInnerClass();
        zImpl.setXType(TypeNode.create(component.zImpl()).setGenericType(TypeNode.create(zName).getGenericType()));
        zImpl.setSuperName(TypeNode.create(zName).getInstanceName());
        zImpl.setOuterClass(outerClass);

        // filter constructors and methods that are not overrided
        List<JMapping<JMethod>> overridedMethods = new ArrayList<>();
        List<JMapping<JMethod>> abstractMethods = new ArrayList<>();
        List<JMapping<JMethod>> wholeMapping = component.wholeMapping(outerClass);
        for (JMapping<JMethod> mapping : wholeMapping) {
            if (!component.isZImplMethod(mapping, def)) {
                continue;
            }
            JMethod jMethod = component.jMethod(mapping);
            if (MappingUtils.isConstructor(def, mapping)) {
                continue;
            }
            if (!(jMethod.modifiers().contains(Modifier.FINAL.getName()))
                && !(jMethod.modifiers().contains(Modifier.ABSTRACT.getName()))
                && !(jMethod.modifiers().contains(Modifier.STATIC.getName()))) {
                overridedMethods.add(mapping);
            }
            if (jMethod.modifiers().contains(Modifier.ABSTRACT.getName())) {
                abstractMethods.add(mapping);
            }
        }
        processAdditionalMethods(abstractMethods, overridedMethods, outerClass);
        List<MethodNode> methods = new ArrayList<>();
        listMap(overridedMethods,
            mapping -> ZImplMethodBuilder.getBuilder(factory, xMethodMapping, component).build(def, zImpl, mapping))
                .stream()
                .filter(Objects::nonNull)
                .forEach(methods::add);

        listMap(overridedMethods, mapping -> new CallSuperMethodBuilder(factory, component).build(def, zImpl, mapping))
            .stream()
            .filter(Objects::nonNull)
            .forEach(methods::add);

        listMap(abstractMethods,
            mapping -> ZImplMethodBuilder.getBuilder(factory, xMethodMapping, component).build(def, zImpl, mapping))
                .stream()
                .filter(Objects::nonNull)
                .forEach(methods::add);
        zImpl.setMethods(methods);
        // add constructor to zNode's methods.
        zImpl.methods().addAll(createConstructorInZImpl(zImpl, constructorList));

        // Add to do block in zImpl
        addToDoBlockInZImpl(wholeMapping, zImpl, component);

        // add creator to zNode's fields.
        addCreatorField(zImpl, component.getZClassList(), zName, outerClass);

        outerClass.addInnerClass(zImpl);
    }

    private ZImplClassNode getEmptyInnerClass() {
        ZImplClassNode node = new ZImplClassNode();
        node.setModifiers(Collections.singletonList(Modifier.PRIVATE.getName()));
        node.setClassType("class");
        node.setInterfaces(Collections.emptyList());
        node.setInner(true);
        node.setFields(Collections.emptyList());
        node.setInnerClasses(Collections.emptyList());
        return node;
    }

    private void addCreatorField(ClassNode node, Map<String, KClass> world, String name, ClassNode xNode) {
        if (!xNode.isAbstract()) {
            return;
        }

        List<KClass> classList = new SupersVisitor(world.get(XMSUtils.degenerify(name)), world).visit();
        for (KClass cls : classList) {
            if (cls.getClassName().contains(AstConstants.PARCELABLE_INTERFACE)) {
                List<FieldNode> fields = new ArrayList<>();
                FieldNode fieldNode = FieldNode.create(xNode,
                    Arrays.asList(Modifier.PUBLIC.getName(), Modifier.FINAL.getName()),
                    TypeNode.create(AstConstants.PARCELABLE_INTERFACE + ".Creator"), AstConstants.CREATOR,
                    VarNode.create("null"));
                fields.add(fieldNode);
                node.setFields(fields);
                break;
            }
        }
    }

    private void processAdditionalMethods(List<JMapping<JMethod>> abstractMethods,
        List<JMapping<JMethod>> overridedMethods, ClassNode classNode) {
        List<JMapping<JMethod>> additionalMethods = getAdditionalMethods(classNode);
        if (additionalMethods.isEmpty()) {
            return;
        }
        abstractMethods.removeAll(additionalMethods);
        additionalMethods.forEach(mapping -> {
            JMethod gMethod = mapping.g();
            gMethod.modifiers().remove(Modifier.ABSTRACT.getName());
            JMethod hMethod = mapping.h();
            if (hMethod != null) {
                hMethod.modifiers().remove(Modifier.ABSTRACT.getName());
            }
            JMapping<JMethod> newMapping = JMapping.create(gMethod, hMethod, mapping.status());
            overridedMethods.add(newMapping);
        });
    }

    private List<MethodNode> createConstructorInZImpl(ClassNode parent, List<JMethod> ctors) {
        List<MethodNode> constructorMethods = new ArrayList<>();
        for (JMethod jMethod : ctors) {
            MethodNode node = new MethodNode();
            node.setName(parent.getXType().getTypeName());
            List<TypeNode> paraTypes = new ArrayList<>();
            jMethod.parameterTypes().forEach(t -> paraTypes.add(TypeNode.create(t.type(), false)));
            node.setParameters(paraTypes);
            node.setReturnType(TypeNode.create(""));
            node.setModifiers(jMethod.modifiers());
            List<TypeNode> exceptionTypes = new ArrayList<>();
            jMethod.exceptions().forEach(e -> exceptionTypes.add(TypeNode.create(e, false)));
            node.setExceptions(exceptionTypes);
            node.setParent(parent);

            // gathering parameters, prepare to call super
            List<StatementNode> params = new ArrayList<>();
            for (int i = 0; i < node.parameters().size(); i++) {
                params.add(VarNode.create(node.paramAt(i)));
            }

            // generator body : super(param0, param1)
            node.setBody(Collections.singletonList(CallNode.create("super", params)));
            constructorMethods.add(node);
        }
        return constructorMethods;
    }

    private void addToDoBlockInZImpl(List<JMapping<JMethod>> wholeMappings, ClassNode zImpl, Component component) {
        if (!component.needToDoBlockInZImpl()) {
            return;
        }
        boolean needToDoBlock = wholeMappings.stream().anyMatch(JMapping::isDisMatchMethodNeedToBeCreated);
        if (needToDoBlock) {
            TodoManager.createTodoBlockFor(zImpl);
        }
    }
}
