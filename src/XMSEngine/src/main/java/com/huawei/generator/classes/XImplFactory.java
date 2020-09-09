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

import static com.huawei.generator.gen.AstConstants.CREATOR;
import static com.huawei.generator.gen.AstConstants.INNER_CLASS_NAME;
import static com.huawei.generator.gen.AstConstants.PARCELABLE_INTERFACE;
import static com.huawei.generator.gen.AstConstants.XMS_OBJECT;
import static com.huawei.generator.utils.XMSUtils.degenerify;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.FieldNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XImplClassNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.builder.RoutingMethodBuilder;
import com.huawei.generator.method.builder.XImplConstructorBuilder;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.mirror.KClass;
import com.huawei.generator.mirror.KClassReader;
import com.huawei.generator.mirror.KClassUtils;
import com.huawei.generator.mirror.SupersVisitor;
import com.huawei.generator.utils.GlobalMapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Factory for creating XImpl class nodes.
 *
 * @since 2020-01-19
 */
public class XImplFactory {
    /**
     * Creates a class node representing XImpl with information provided by its outer class definition and its outer
     * class node
     *
     * @param factory The instance of method generator factory
     * @param def JClass representing the outer class
     * @param outerClass class node representing the outer class
     * @return a class node representing XImpl
     */
    public static ClassNode create(MethodGeneratorFactory factory, JClass def, ClassNode outerClass) {
        if (outerClass.isInterface()) {
            return new InterfaceImpl(factory, def, outerClass).createXImpl();
        } else if (outerClass.isAbstract()) {
            return new AbstractImpl(factory, def, outerClass).createXImpl();
        } else {
            throw new IllegalArgumentException(outerClass.fullName() + " is not abstract");
        }
    }

    /**
     * XImpl creator implementation.
     */
    private abstract static class CreatorImpl {
        MethodGeneratorFactory factory;

        JClass def;

        ClassNode outerClass;

        ClassNode xImpl;

        CreatorImpl(MethodGeneratorFactory factory, JClass def, ClassNode outerClass) {
            // Fatal error.
            if (def == null) {
                throw new IllegalStateException("jClass is null:" + outerClass.fullName());
            }

            // Fatal error.
            if (def.gName().isEmpty()) {
                throw new IllegalStateException("gName is null:" + outerClass.fullName());
            }
            this.factory = factory;
            this.def = def;
            this.outerClass = outerClass;
            xImpl = new XImplClassNode.Builder().setSupported(outerClass.isSupported())
                .setInner(true)
                .setClassType("class")
                .setXType(TypeNode.create(INNER_CLASS_NAME).setGenericType(outerClass.generics()))
                .setModifiers(Collections.singletonList("static"))
                .setSuperName(superName())
                .setInterfaces(interfaces())
                .setOuterClass(outerClass)
                .build();
            XClassDoc classDoc = factory.getClassDoc();
            if (classDoc != null) {
                XClassDoc xImplClassDoc = classDoc.getXImplClassDoc();
                factory.createClassDoc(xImplClassDoc, xImpl);
                xImpl.setClassDoc(xImplClassDoc);
            }
        }

        /**
         * Creates a class node representing XImpl.
         *
         * @return a class node representing XImpl, with the corresponding methods been filled.
         */
        final ClassNode createXImpl() {
            insertCreatorField();

            // add wrapper constructor
            xImpl.methods().add(XImplConstructorBuilder.getBuilder(factory).build(def, xImpl));

            // methods need to be implemented in inheritance chain
            List<JMapping<JMethod>> methods = KClassUtils.getXImplMethods(def, outerClass);
            methods.stream()
                .filter(methodJMapping -> methodJMapping.g() != null)
                .forEach(methodJMapping ->
                    xImpl.methods().add(new RoutingMethodBuilder(factory).build(def, xImpl, methodJMapping)));

            return xImpl;
        }

        /**
         * Inserts creator field to XImpl if superClass doesn't have creator field.
         */
        private void insertCreatorField() {
            if (!needCreatorField()) {
                return;
            }
            // public static final android.os.Parcelable.Creator CREATOR = null;
            FieldNode fieldNode = FieldNode.create(xImpl, Arrays.asList("public", "static", "final"),
                TypeNode.create(PARCELABLE_INTERFACE + ".Creator"), CREATOR, VarNode.create("null"));
            xImpl.fields().add(fieldNode);
        }

        private boolean needCreatorField() {
            KClass superKClass = KClassReader.INSTANCE.getAndroidClassList().get(PARCELABLE_INTERFACE);
            KClass kClass = KClassReader.INSTANCE.getGClassList().get(degenerify(def.gName()));
            List<KClass> classList = new SupersVisitor(kClass, KClassReader.INSTANCE.getGClassList()).visit();

            for (KClass cls : classList) {
                JClass cur = GlobalMapping.getDegenerigyMap().get(cls.getClassName());
                if (cur == null) {
                    continue;
                }
                List<JMapping<JFieldOrMethod>> fields = cur.fields();
                for (JMapping<JFieldOrMethod> field : fields) {
                    if (field.g().asJField().name().equals(CREATOR)) {
                        return false;
                    }
                }
            }
            return classList.contains(superKClass);
        }

        /**
         * Super class name of this XImpl class.
         *
         * @return super class name of this XImpl class.
         */
        abstract String superName();

        /**
         * Interfaces implemented by this XImpl class.
         *
         * @return interfaces implemented by this XImpl class.
         */
        abstract List<String> interfaces();
    }

    /**
     * Factory for creating XImpl of interface classes.
     */
    private static class InterfaceImpl extends CreatorImpl {
        InterfaceImpl(MethodGeneratorFactory factory, JClass def, ClassNode outerClass) {
            super(factory, def, outerClass);
        }

        @Override
        String superName() {
            return XMS_OBJECT;
        }

        @Override
        List<String> interfaces() {
            return Collections.singletonList(outerClass.getXType().getInstanceName());
        }
    }

    /**
     * Factory for creating XImpl of abstract classes.
     */
    private static class AbstractImpl extends CreatorImpl {
        AbstractImpl(MethodGeneratorFactory factory, JClass def, ClassNode outerClass) {
            super(factory, def, outerClass);
        }

        @Override
        String superName() {
            return outerClass.getXType().getInstanceName();
        }

        @Override
        List<String> interfaces() {
            return Collections.emptyList();
        }
    }

}
