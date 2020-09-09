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

package com.huawei.generator.gen;

import static com.huawei.generator.gen.AstConstants.CREATOR;
import static com.huawei.generator.gen.AstConstants.CREATOR_TYPE;
import static com.huawei.generator.gen.AstConstants.PARCELABLE_INTERFACE;
import static com.huawei.generator.json.JMapping.STATUS_MANUALLY_ADAPT;
import static com.huawei.generator.json.JMapping.STATUS_MATCHING;
import static com.huawei.generator.mirror.KClassUtils.hasInheritance;

import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.FieldNode;
import com.huawei.generator.ast.GetFieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewArrayNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.XAdapterClassNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.builder.AbstractMethodBuilder;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.exception.UnExpectedProcessException;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Decorator for Parcelable
 *
 * @since 2020-03-20
 */
public final class ParcelableDecorator {
    private static final class CreateFromParcelBuilder extends AbstractMethodBuilder<JMethod> {
        private CreateFromParcelBuilder(MethodGeneratorFactory factory) {
            super(factory);
        }

        @Override
        public MethodNode build(JClass jClass, ClassNode classNode) {
            MethodNode method = new MethodNode();
            method.setParent(classNode);
            method.setModifiers(new ArrayList<>());
            method.setName("createFromParcel");
            method.setExceptions(new ArrayList<>());
            method.setReturnType(TypeNode.create(classNode.outerClass().fullName(), false));
            method.setParameters(Collections.singletonList(TypeNode.create("android.os.Parcel")));
            method.setBody(factory.createFromParcelGenerator(method, jClass).generate());
            return method;
        }

        @Override
        public MethodNode build(JClass jClass, ClassNode classNode, JMapping<JMethod> mapping) {
            throw new UnExpectedProcessException();
        }
    }

    public static final class CreateFromParcelGenerator implements BodyGenerator {
        private MethodNode methodNode;

        private JClass def;

        private Component component;

        public CreateFromParcelGenerator(MethodNode methodNode, JClass def, Component component) {
            this.methodNode = methodNode;
            this.def = def;
            this.component = component;
        }

        @Override
        public List<StatementNode> generate() {
            List<StatementNode> body = new ArrayList<>();
            body.add(
                AssignNode.create(DeclareNode.create(TypeNode.create(component.zName(def)), component.retVarName()),
                    CallNode.create(GetFieldNode.create(VarNode.create(component.zName(def)), CREATOR),
                        "createFromParcel", Collections.singletonList(VarNode.create(methodNode.paramAt(0))))));
            body.add(
                ReturnNode.create(NewNode.create(TypeNode.create(methodNode.parent().outerClass().fullName(), false),
                    component.xWrapperParams(component.retVarName()))));
            return body;
        }
    }

    public static final class NewArrayGenerator implements BodyGenerator {
        private MethodNode methodNode;

        public NewArrayGenerator(MethodNode methodNode) {
            this.methodNode = methodNode;
        }

        @Override
        public List<StatementNode> generate() {
            return Collections.singletonList(ReturnNode.create(NewArrayNode
                .create(TypeNode.create(methodNode.parent().outerClass().fullName()), methodNode.paramAt(0))));
        }
    }

    // Generating a CREATOR field for these classes will cause syntax error.
    private static final List<String> NO_CREATOR_CLASSES =
        Arrays.asList("org.xms.g.maps.model.Dot", "org.xms.g.maps.model.Dash", "org.xms.g.maps.model.Gap");

    private MethodGeneratorFactory factory;

    private ParcelableDecorator(MethodGeneratorFactory factory) {
        this.factory = factory;
    }

    public static ParcelableDecorator with(MethodGeneratorFactory factory) {
        return new ParcelableDecorator(factory);
    }

    public void decorate(ClassNode classNode) {
        if (!(classNode instanceof XAdapterClassNode)) {
            return;
        }
        XAdapterClassNode xClass = (XAdapterClassNode) classNode;
        if (!shouldCreateParcelableField(xClass.getDefinition())) {
            return;
        }
        classNode.fields().add(createParcelableField(xClass));
    }

    private FieldNode createParcelableField(XAdapterClassNode classNode) {
        List<String> mod = Arrays.asList("public", "final", "static");
        StatementNode value = createAnonymousNode(classNode);
        return FieldNode.create(classNode, mod, TypeNode.create(CREATOR_TYPE), CREATOR, value);
    }

    private StatementNode createAnonymousNode(XAdapterClassNode classNode) {
        AnonymousNode anonymous = AnonymousNode.create(CREATOR_TYPE, Collections.emptyList(), classNode);
        anonymous.setFullName(classNode.fullName() + "." + CREATOR_TYPE);
        anonymous.setSupported(classNode.isSupported());
        anonymous.setMethods(new ArrayList<>());
        anonymous.methods().add(createFromParcel(anonymous, classNode.getDefinition()));
        anonymous.methods().add(createNewArray(anonymous, classNode.getDefinition()));
        return NewNode.create(anonymous);
    }

    private MethodNode createFromParcel(ClassNode classNode, JClass def) {
        return new CreateFromParcelBuilder(factory).build(def, classNode);
    }

    private MethodNode createNewArray(ClassNode classNode, JClass def) {
        MethodNode method = new MethodNode();
        method.setParent(classNode);
        method.setModifiers(new ArrayList<>());
        method.setName("newArray");
        method.setExceptions(new ArrayList<>());
        method.setReturnType(TypeNode.create(classNode.outerClass().fullName(), false).setDimension(1));
        method.setParameters(Collections.singletonList(TypeNode.create("int")));
        method.setBody(factory.createNewArrayGenerator(method, def).generate());
        return method;
    }

    /**
     * Checks if CREATOR field is supported in a class definition.
     *
     * @param def the given JClass definition
     * @return whether CREATOR field is supported
     */
    public static boolean isCreatorSupported(JClass def) {
        JMapping<JFieldOrMethod> mapping = findCreatorMapping(def);
        return mapping != null && !mapping.isUnsupported();
    }

    /**
     * @param def class to be processed
     * @return true if the class contains the CREATOR field
     */
    private static boolean shouldCreateParcelableField(JClass def) {
        if (findCreatorMapping(def) != null) {
            return true;
        }
        if (NO_CREATOR_CLASSES.contains(TypeNode.create(def.gName()).toX().getTypeName())) {
            return false;
        }
        return insertCreatorFieldToDef(def);
    }

    private static JMapping<JFieldOrMethod> findCreatorMapping(JClass def) {
        return def.fields()
            .stream()
            .filter(fieldOrMethodJMapping -> fieldOrMethodJMapping.g() != null
                && XMSUtils.isCreatorField(fieldOrMethodJMapping.g().asJField()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Determine whether needs to insert creator field to class
     *
     * @param def target JClass
     * @return if needs to insert return true, otherwise false
     */
    private static boolean insertCreatorFieldToDef(JClass def) {
        if (def.isInterface() || def.isAbstract()) {
            return false;
        }
        if (!hasInheritance(def, PARCELABLE_INTERFACE, true)) {
            return false;
        }
        def.fields().add(generateCreatorMapping(def));
        return true;
    }

    private static JMapping<JFieldOrMethod> generateCreatorMapping(JClass def) {
        JMapping<JFieldOrMethod> fieldMapping = new JMapping<>();
        JFieldOrMethod jField = new JFieldOrMethod(CREATOR, CREATOR_TYPE, "", Arrays.asList("static", "final"));
        fieldMapping.setG(jField);
        if (hasInheritance(def, PARCELABLE_INTERFACE, false)) {
            fieldMapping.setH(jField);
            fieldMapping.setStatus(STATUS_MATCHING);
        } else {
            fieldMapping.setH(null);
            fieldMapping.setStatus(STATUS_MANUALLY_ADAPT);
        }
        return fieldMapping;
    }
}
