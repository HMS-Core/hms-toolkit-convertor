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

import static com.huawei.generator.gen.AstConstants.XMS_GETTABLE;
import static com.huawei.generator.gen.AstConstants.XMS_INTERFACE;
import static com.huawei.generator.utils.SpecialClasses.isOnlyForWrapping;

import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.FieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.custom.XAdapterClassNode;
import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.gen.ParcelableDecorator;
import com.huawei.generator.gen.XEnumCreator;
import com.huawei.generator.json.DocSources;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.builder.ConstructorBuilder;
import com.huawei.generator.method.builder.DynamicCastBuilder;
import com.huawei.generator.method.builder.FieldAccessorBuilder;
import com.huawei.generator.method.builder.GetZInstanceBuilder;
import com.huawei.generator.method.builder.GetZInterfaceInstanceBuilder;
import com.huawei.generator.method.builder.InstWrapperBuilder;
import com.huawei.generator.method.builder.InterfaceInstanceGetterBuilder;
import com.huawei.generator.method.builder.IsInstanceBuilder;
import com.huawei.generator.method.builder.RoutingMethodBuilder;
import com.huawei.generator.method.builder.SetMethodBuilder;
import com.huawei.generator.method.builder.WrapperConstructorBuilder;
import com.huawei.generator.method.component.StubComponent;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.mirror.KClassUtils;
import com.huawei.generator.utils.G2HTables;
import com.huawei.generator.utils.MappingUtils;
import com.huawei.generator.utils.TypeUtils;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Factory for creating class nodes from json definitions.
 *
 * @since 2020-02-25
 */
public class XClassFactory {
    private MethodGeneratorFactory factory;

    private Map<JMapping<JMethod>, MethodNode> xMethodMapping = new HashMap<>();

    private DocSources docSources;

    public XClassFactory(MethodGeneratorFactory factory) {
        this.factory = factory;
        this.docSources = new DocSources();
    }

    public XClassFactory(MethodGeneratorFactory factory, String pluginPath) {
        this.factory = factory;
        this.docSources = new DocSources(pluginPath);
    }

    private static boolean isSupportedClass(JClass def) {
        List<JMapping<JMethod>> methods = def.methods();
        if (methods.isEmpty() && def.fields().isEmpty()) {
            return true;
        }
        for (JMapping<JMethod> methodJMapping : methods) {
            if (!methodJMapping.isUnsupported()) {
                return true;
            }
        }
        for (JMapping<JFieldOrMethod> fieldJMapping : def.fields()) {
            if (!fieldJMapping.isUnsupported()) {
                return true;
            }
        }
        return false;
    }

    public XAdapterClassNode from(JClass def) {
        XAdapterClassNode node = new XAdapterClassNode();
        node.setDef(def);
        node.setSupported(isSupportedClass(def));
        node.setGType(TypeNode.create(def.gName(), false));
        node.setXType(TypeNode.create(def.gName(), false).toX().renameGenericDefinitions());
        node.setHType(TypeNode.create(def.hName(), false));
        node.setSuperName(def.superClass());
        node.setModifiers(def.modifiers());
        node.setClassType(def.type());
        node.setInterfaces(def.interfaces());
        node.setInnerClasses(new ArrayList<>());
        node.setInner(def.isInnerClass());
        return node.normalize();
    }

    public ClassNode populate(XAdapterClassNode node) {
        // add classDoc information into classNode
        XClassDoc classDoc = docSources.getClassDoc(node);

        // hacker for javadoc json
        factory.createClassDoc(classDoc, node);

        List<FieldNode> fields = new ArrayList<>();
        node.setFields(fields);
        List<MethodNode> methods = new ArrayList<>();
        node.setMethods(methods);

        JClass def = node.getDefinition();
        if (node.isAnnotation()) {
            // After some analyse, we decide to change all annotation class to interface and then deal with it
            node.setClassType("interface");
        }

        // <init>(XBox)
        createWrapperConstructor(def, node);

        // <init>
        createConstructors(def, node);

        // getXXX()
        createFieldGetters(def, node);

        createRoutingMethods(def, node);

        // gInst, hInst, getG(), getH(), setG(), setH()
        if (node.interfaces().contains(XMS_GETTABLE)) {
            createXGettableMethods(def, node);
        }

        if (node.isInterface()) {
            node.interfaces().add(0, XMS_INTERFACE);
            node.addInnerClass(XImplFactory.create(factory, def, node));
            methods.add(createGetZInterfaceInstanceMethod(node));
            methods.addAll(createGetInterfaceInstanceMethods(node));
        }

        // XImpl for abstract classes
        if (node.isAbstract()) {
            node.addInnerClass(XImplFactory.create(factory, def, node));
        }

        methods.add(DynamicCastBuilder.getBuilder(factory).build(def, node));
        methods.add(IsInstanceBuilder.getBuilder(factory).build(def, node));

        // add CREATOR field
        ParcelableDecorator.with(factory).decorate(node);

        // Enum.values(), Enum.valueOf
        if (node.isEnum()) {
            XEnumCreator.populateEnum(def, node, factory);
        }

        // set view
        setView(def, node);

        if (classDoc != null) {
            factory.createFieldDoc(classDoc, node);
        }

        // add GImpl, HImpl, field and assignment
        return WrapperDecorator.with(factory, xMethodMapping).decorate(node);
    }

    private void createWrapperConstructor(JClass def, ClassNode node) {
        if (G2HTables.inBlockList(node.fullName(), node.shortName())
            || TypeUtils.isViewSubClass(node.getGType(), true)) {
            return;
        }
        if (!node.isInterface()) {
            node.methods().add(WrapperConstructorBuilder.getBuilder(factory).build(def, node));
        }
    }

    private void createFieldGetters(JClass def, ClassNode node) {
        def.fields()
            .stream()
            .filter(mapping -> (mapping.isMatching() || mapping.isDisMatchMethodNeedToBeCreated()
                || mapping.isUnsupported()) && mapping.g().isJField())
            // check if this field in block list
            .filter(fieldOrMethodJMapping ->
                !G2HTables.inBlockList(node.fullName(), fieldOrMethodJMapping.g().asJField().name()))
            // check if this field in trust list
            .filter(fieldOrMethodJMapping ->
                G2HTables.inTrustList(node.fullName(), fieldOrMethodJMapping.g().asJField().name()))
            .filter(fieldOrMethodJMapping -> !XMSUtils.isCreatorField(fieldOrMethodJMapping.g().asJField()))
            .forEach(mapping -> {
                MethodNode method = FieldAccessorBuilder.getBuilder(factory).build(def, node, mapping);
                factory.createMethodDoc(method);
                node.methods().add(method);
            });
    }

    private void createRoutingMethods(JClass def, ClassNode node) {
        List<JMapping<JMethod>> wholeMapping;
        if (node.isAbstract() || node.isInterface()) {
            wholeMapping = def.methods();
        } else {
            wholeMapping = KClassUtils.getHierarchicalMethodMappings(node);
        }
        wholeMapping.stream()
            .filter(mapping -> (mapping.isMatching() || mapping.isDisMatchMethodNeedToBeCreated()
                || mapping.isUnsupported()))
            .filter(mapping -> !Objects.equals(mapping.g().returnType(), ""))
            // check if this method in block list
            .filter(methodJMapping -> !G2HTables.inBlockList(node.fullName(), methodJMapping.g().name()))
            // check if this method in trust list
            .filter(methodJMapping -> G2HTables.inTrustList(node.fullName(), methodJMapping.g().name()))
            .forEach(mapping -> {
                MethodNode methodNode = new RoutingMethodBuilder(factory).build(def, node, mapping);
                node.methods().add(methodNode);
                xMethodMapping.put(mapping, methodNode);
            });
    }

    private void createXGettableMethods(JClass def, ClassNode node) {
        // zInstance field, getZInstance() and setZInstance() method
        factory.componentContainer().components().forEach(component -> {
            node.fields()
                .add(FieldNode.create(node, Collections.singletonList("public"), TypeNode.OBJECT_TYPE,
                    component.zInstanceFieldName(), null));
            node.methods().add(SetMethodBuilder.getBuilder(factory, component).build(def, node));
        });
        factory.componentContainer().components().forEach(component ->
            node.methods().add(GetZInstanceBuilder.getBuilder(factory, component).build(def, node)));
        // this is a hack for xapi generation, we should generate a getZInstance method to stop the compiler from
        // complaining
        if (factory.componentContainer().components().isEmpty()) {
            node.methods().add(GetZInstanceBuilder.getBuilder(factory, StubComponent.INSTANCE).build(def, node));
        }
    }

    private void createConstructors(JClass def, ClassNode node) {
        if (G2HTables.inBlockList(node.fullName(), node.shortName())) {
            return;
        }
        if (isOnlyForWrapping(TypeNode.create(def.gName()).toX().getTypeName())) {
            return;
        }

        def.methods()
            .stream()
            .filter(
                mapping -> mapping.isMatching() || mapping.isDisMatchMethodNeedToBeCreated() || mapping.isUnsupported())
            .filter(mapping -> !mapping.g().modifiers().contains("private"))
            .filter(mapping -> MappingUtils.isConstructor(def, mapping))
            .forEach(mapping -> {
                node.methods().add(ConstructorBuilder.getBuilder(factory).build(def, node, mapping));
            });
    }

    private void setView(JClass def, ClassNode node) {
        if (!TypeUtils.isViewSubClass(node.getGType(), true)) {
            return;
        }
        setViewSuper(node);

        node.methods().add(InstWrapperBuilder.getBuilder(factory).build(def, node));
    }

    private void setViewSuper(ClassNode node) {
        if (node.superName().contains("org.xms")) {
            return;
        }
        node.setSuperName("android.widget.FrameLayout");
    }

    private MethodNode createGetZInterfaceInstanceMethod(ClassNode classNode) {
        return GetZInterfaceInstanceBuilder.getBuilder(factory).build(null, classNode);
    }

    private List<MethodNode> createGetInterfaceInstanceMethods(ClassNode classNode) {
        return factory.componentContainer().components().stream().map(it -> {
            AnonymousNode anonymous =
                new AnonymousZImplFactory().createZImplClass(factory, classNode, xMethodMapping, it);
            return InterfaceInstanceGetterBuilder.getBuilder(factory, anonymous, it).build(null, classNode);
        }).collect(Collectors.toList());
    }
}
