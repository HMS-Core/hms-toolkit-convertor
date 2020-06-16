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

import static com.huawei.generator.gen.AstConstants.GENERIC_PREFIX;

import com.huawei.generator.utils.Modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * MethodNode class
 *
 * @since 2019-11-12
 */
public class MethodNode extends AstNode {
    private static final String CONSTANT_VOID = "void";

    private ClassNode parent;

    private String name;

    private TypeNode returnType;

    private ModifierNode modifiers;

    private List<TypeNode> parameters;

    private List<String> parameterNames;

    private BlockNode body;

    private List<TypeNode> exceptions;

    private boolean hasTodo;

    private List<TypeNode> localGenerics = new ArrayList<>();

    public MethodNode() {
    }

    public String paramAt(int n) {
        if (parameterNames == null) {
            return "param" + n;
        }
        if (n < 0 || n >= parameterNames.size()) {
            throw new IllegalArgumentException("Illegal parameter index.");
        }
        return parameterNames.get(n);
    }

    public ClassNode parent() {
        return parent;
    }

    public String name() {
        return name;
    }

    public TypeNode returnType() {
        return returnType;
    }

    public ModifierNode modifiers() {
        return modifiers;
    }

    public List<TypeNode> parameters() {
        return parameters;
    }

    public BlockNode body() {
        return body;
    }

    public boolean shouldHasNoBody() {
        return (parent().isInterface() && !isStatic() && !isDefault()) || isAbstract();
    }

    public boolean shouldHasBody() {
        return !shouldHasNoBody();
    }

    public void setParent(ClassNode parent) {
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReturnType(TypeNode returnType) {
        if (returnType == null) {
            return;
        }

        this.returnType = returnType.deepClone();
        localGenerics = this.returnType.getDefTypes();
        if (localGenerics == null) {
            localGenerics = Collections.emptyList();
        }
    }

    public void setParameterNames(List<String> names) {
        this.parameterNames = names;
    }

    public String localGenericsAsString() {
        if (localGenerics == null || localGenerics.isEmpty()) {
            return "";
        }
        return TypeNode.typeListToString(localGenerics);
    }

    public List<TypeNode> getMethodGenerics() {
        return localGenerics;
    }

    boolean hasTodo() {
        return hasTodo;
    }

    /**
     * Generic defines for method. This will change a method to
     * generic methods.
     *
     * @param genericDefines, generic definitions in method.
     */
    public void setGenericDefines(List<TypeNode> genericDefines) {
        this.localGenerics = genericDefines;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = ModifierNode.create(modifiers);
    }

    public void setModifiers(ModifierNode modifiers) {
        this.modifiers = modifiers;
    }

    public void setParameters(List<TypeNode> parameters) {
        this.parameters = parameters;
    }

    public void setBody(List<StatementNode> body) {
        this.body = BlockNode.create(body);
    }

    public boolean isAbstract() {
        return isModify(Modifier.ABSTRACT.getName());
    }

    public boolean isStatic() {
        return isModify(Modifier.STATIC.getName());
    }

    public boolean isDefault() {
        return isModify(Modifier.DEFAULT.getName());
    }

    public boolean isProtected() {
        return isModify(Modifier.PROTECTED.getName());
    }

    public boolean isPrivate() {
        return isModify(Modifier.PRIVATE.getName());
    }

    /**
     * Whether the method node is final.
     * 
     * @return if final return true, otherwise false
     */
    public boolean isFinal() {
        return isModify(Modifier.FINAL.getName());
    }

    private boolean isModify(String accFlag) {
        if (accFlag.equals(Modifier.PUBLIC.getName())) {
            return isPublic();
        }
        return modifiers.contains(accFlag);
    }

    public boolean isPublic() {
        return !isDefault() && !isProtected() && !isPrivate();
    }

    public boolean isReturnNeedWrap() {
        if (returnType == null) {
            return false;
        }
        return returnType.getTypeName().contains("org.xms.");
    }

    public List<TypeNode> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<TypeNode> exceptions) {
        this.exceptions = exceptions;
    }

    public boolean isReturnVoid() {
        if (returnType == null) {
            return false;
        }
        return returnType.getTypeName().equals(CONSTANT_VOID);
    }

    public void setHasTodo(boolean hasTodo) {
        this.hasTodo = hasTodo;
    }

    /**
     * Checks whether a give type represents a generic type in the scope of this method.
     * 
     * @param t the given type
     * @param isXType whether the given type represents a X type
     * @return whether the given type is a generic type
     */
    public boolean isGeneric(TypeNode t, boolean isXType) {
        if (t == null) {
            return false;
        }
        String type = adaptTypeName(t, isXType);
        if ((localGenerics != null) && localGenerics.stream().anyMatch(it -> it.getTypeName().equals(type))) {
            return true;
        }
        return parent().isGeneric(t, isXType);
    }

    /**
     * Adapt the type name of a given generic type, to ensure that the given type and this method are from the same
     * world
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

    public TypeNode upperBoundOf(TypeNode given) {
        if (given == null) {
            throw new IllegalStateException();
        }

        for (TypeNode tn : localGenerics) {
            if (given.getTypeName().equals(tn.getTypeName()) && (tn.getSuperClass() != null)
                && (!tn.getSuperClass().isEmpty())) {
                return tn.getSuperClass().get(0);
            }
        }

        return TypeNode.OBJECT_TYPE;
    }

    /**
     * Rename a given generic type to X. If it's a generic type defined in the scope of this method, then it's renamed,
     * otherwise not.
     *
     * @param t a given type
     */
    public void renameGeneric(TypeNode t) {
        List<TypeNode> queue = new LinkedList<>();
        queue.add(t);

        while (!queue.isEmpty()) {
            TypeNode n = queue.remove(0);
            if (isGeneric(n, false)) {
                n.addPrefix(GENERIC_PREFIX);
            }

            if (n.getGenericType() != null) {
                queue.addAll(n.getGenericType());
            }

            if (n.getSuperClass() != null) {
                queue.addAll(n.getSuperClass());
            }

            if (n.getInfClass() != null) {
                queue.addAll(n.getInfClass());
            }
        }
    }

    /**
     * Normalize this method node.
     *
     * @return this
     */
    public MethodNode normalize() {
        return this;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }
}
