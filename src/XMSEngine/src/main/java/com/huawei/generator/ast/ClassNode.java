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

package com.huawei.generator.ast;

import com.huawei.generator.ast.custom.CustomContentNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.utils.GlobalMapping;
import com.huawei.generator.utils.Modifier;
import com.huawei.generator.utils.XMSUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node representing a java class.
 *
 * @since 2019-11-12
 */
public class ClassNode extends AstNode {
    /**
     * String constants of class types.
     */
    public interface Types {
        String INTERFACE = "interface";

        String ENUM = "enum";

        String ANNOTATION = "annotation";
    }

    private static final String CONSTANT_XMS = "xms";

    private static final String CONSTANT_DOT = ".";

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassNode.class);

    private String packageName;

    private ModifierNode modifiers;

    private String classType;

    private String superName;

    private TypeNode xType;

    private TypeNode gType;

    private TypeNode hType;

    private String fullName;

    private List<String> interfaces;

    private List<FieldNode> fields;

    private List<MethodNode> methods;

    private List<ClassNode> innerClasses;

    private ClassNode outerClass;

    private boolean isInner = false;

    private boolean isSupported = true;

    private CustomContentNode customContentNode;

    public ClassNode() {
    }

    public boolean isSupported() {
        return isSupported;
    }

    public void setSupported(boolean supported) {
        isSupported = supported;
    }

    public void setXType(TypeNode xType) {
        if (xType == null) {
            throw new IllegalStateException("classDeclaredName should not be null");
        }

        this.xType = xType;
        this.fullName = xType.toString();

        String className = xType.getTypeName();
        if (className.contains(CONSTANT_DOT)) {
            this.packageName = className.substring(0, className.lastIndexOf(CONSTANT_DOT));
        }
    }

    public TypeNode getGType() {
        return gType;
    }

    /**
     * UNSAFE: hType may be null.
     * Make sure you know what you are doing before calling this method.
     * 
     * @return Hms type for a class node.
     */
    public TypeNode getHType() {
        return hType;
    }

    public TypeNode getXType() {
        return xType;
    }

    public void setHType(TypeNode hType) {
        if (hType == null) {
            LOGGER.debug("hType is null for class {}, using Object instead of this.", xType.getTypeName());
            this.hType = TypeNode.OBJECT_TYPE;
            return;
        }
        this.hType = hType;
    }

    public void setGType(TypeNode gType) {
        this.gType = gType;
    }

    public void setPackageName(String packageName) {
        if (!packageName.contains(CONSTANT_DOT)) {
            LOGGER.error("Not an inner class: {}", fullName);
        }
        this.packageName = packageName;
        innerClasses().forEach(c -> c.setPackageName(packageName));
    }

    public String packageName() {
        return packageName;
    }

    public ModifierNode modifiers() {
        return modifiers;
    }

    public String classType() {
        return classType;
    }

    /**
     * For org.xms.utils.Map<T>, shortName() returns "Map"
     * 
     * @return type name without package name, without generic info.
     */
    public String shortName() {
        return getXType().getTypeNameWithoutPackage();
    }

    /**
     * For org.xms.utils.Map<T>, longName() returns "org.xms.utils.Map"
     * 
     * @return type name with package name, without generic info.
     */
    public String longName() {
        return xType.getTypeName();
    }

    public String superName() {
        return superName;
    }

    /**
     * For org.xms.utils.Map<T>, fullName() returns "org.xms.utils.Map<T>"
     * 
     * @return type name with package name, with generic info.
     */
    public String fullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> interfaces() {
        return interfaces;
    }

    public List<FieldNode> fields() {
        return fields;
    }

    public List<MethodNode> methods() {
        return methods;
    }

    public List<ClassNode> innerClasses() {
        return innerClasses;
    }

    public boolean isInner() {
        return isInner;
    }

    public List<TypeNode> generics() {
        return xType.getGenericType();
    }

    public String genericsString() {
        return TypeNode.typeListToString(generics());
    }

    /**
     * Checks whether a given type is a generic type in the scope of this class.
     *
     * @param t a given type
     * @param isXType whether the given type is from X world
     * @return whether it's a generic type
     */
    boolean isGeneric(TypeNode t, boolean isXType) {
        String type = adaptTypeName(t, isXType);
        if ((generics() != null) && generics().stream().anyMatch(it -> type.equals(it.getTypeName()))) {
            return true;
        }
        return (outerClass != null) && outerClass.isGeneric(t, isXType);
    }

    /**
     * Adapt the type name of a given generic type, to ensure that the given type and this class are from the same world
     * (X world or Z world). Generic types from the X world are all prefixed with a "X" character, which should be
     * removed before comparing with type names defined in Z classes.
     * 
     * @param t a given generic type
     * @param isXType whether the type represents a X type
     * @return the type name of the given type, with generic prefix removed as necessary.
     */
    protected String adaptTypeName(TypeNode t, boolean isXType) {
        if (isXType) {
            // remove generic prefix
            return t.getTypeName().substring(1);
        } else {
            return t.getTypeName();
        }
    }

    public boolean hasTodo() {
        for (MethodNode methodNode : methods) {
            if (methodNode.hasTodo()) {
                return true;
            }
        }
        return false;
    }

    public CustomContentNode customContentNode() {
        return customContentNode;
    }

    public void setCustomContentNode(CustomContentNode node) {
        this.customContentNode = node;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public void setFields(List<FieldNode> fields) {
        this.fields = fields;
    }

    public void setMethods(List<MethodNode> methods) {
        this.methods = methods;
    }

    public boolean isInterface() {
        return Types.INTERFACE.equals(classType);
    }

    public boolean isEnum() {
        return Types.ENUM.equals(classType);
    }

    public boolean isAnnotation() {
        return Types.ANNOTATION.equals(classType);
    }

    public boolean isXObject() {
        return (this.fullName.contains(CONSTANT_XMS) && this.superName.contains(CONSTANT_XMS));
    }

    public boolean isAbstract() {
        return modifiers.contains(Modifier.ABSTRACT.getName());
    }

    public boolean isFinal() {
        return modifiers.contains(Modifier.FINAL.getName());
    }

    public void setInnerClasses(List<ClassNode> list) {
        this.innerClasses = list;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = ModifierNode.create(modifiers);
    }

    public void setInner(boolean isInner) {
        this.isInner = isInner;
    }

    public void addInnerClass(ClassNode innerClass) {
        this.innerClasses.add(innerClass);
    }

    public ClassNode normalize() {
        if (this.isInterface()) {
            modifiers().remove(Modifier.ABSTRACT.getName());
        }
        if (isAnnotation()) {
            modifiers().remove(Modifier.ABSTRACT.getName());
            interfaces().remove("java.lang.annotation.Annotation");
        }
        if (isEnum() && !modifiers().contains(Modifier.FINAL.getName())) {
            modifiers().add(Modifier.FINAL.getName());
        }
        // Interface's inner is static by default
        if (!modifiers().contains(Modifier.STATIC.getName()) && getOuterClass() != JClass.J_CLASS
            && getOuterClass().isInterface()) {
            modifiers().add(Modifier.STATIC.getName());
        }
        // If an interface is inner,then it's static by default
        if (isInner() && isInterface() && !modifiers().contains(Modifier.STATIC.getName())) {
            modifiers().add(Modifier.STATIC.getName());
        }
        return this;
    }

    /**
     * @return The JClass corresponding to this ClassNode
     */
    public JClass getJClass() {
        return GlobalMapping.getDegenerigyMap().get(XMSUtils.degenerify(fullName()));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Whether the class node is inheritable.
     *
     * @return if inheritable return true, otherwise false
     */
    public boolean isInheritable() {
        return (!isInner && !isFinal()) || isAbstract();
    }

    public ClassNode outerClass() {
        return outerClass;
    }

    public void setOuterClass(ClassNode outerClass) {
        this.outerClass = outerClass;
    }

    private JClass getOuterClass() {
        if (!isInner()) {
            return JClass.J_CLASS;
        }
        String className = XMSUtils.degenerify(fullName);
        String outer = className.substring(0, className.lastIndexOf(CONSTANT_DOT));
        JClass jClass = GlobalMapping.getDegenerigyMap().get(outer);
        if (jClass == null) {
            throw new IllegalStateException("Missing outer class " + outer);
        }
        return jClass;
    }

    public static class Builder {
        private boolean isSupported;

        private boolean isInner;

        private String type;

        private TypeNode xType;

        private List<String> modifiers;

        private String superName;

        private List<String> interfaces;

        public Builder setSupported(boolean isSupported) {
            this.isSupported = isSupported;
            return this;
        }

        public Builder setInner(boolean isInner) {
            this.isInner = isInner;
            return this;
        }

        public Builder setClassType(String type) {
            this.type = type;
            return this;
        }

        public Builder setXType(TypeNode type) {
            this.xType = type;
            return this;
        }

        public Builder setModifiers(List<String> modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder setSuperName(String superName) {
            this.superName = superName;
            return this;
        }

        public Builder setInterfaces(List<String> interfaces) {
            this.interfaces = interfaces;
            return this;
        }

        protected ClassNode getClassNode() {
            return new ClassNode();
        }

        public ClassNode build() {
            ClassNode node = getClassNode();
            node.setSupported(this.isSupported);
            node.setInner(this.isInner);
            node.setClassType(this.type);
            node.setXType(this.xType);
            node.setModifiers(this.modifiers);
            node.setSuperName(this.superName);
            node.setInterfaces(this.interfaces);
            node.setMethods(new ArrayList<>());
            node.setFields(new ArrayList<>());
            node.setInnerClasses(new ArrayList<>());
            node.normalize();
            return node;
        }
    }
}
