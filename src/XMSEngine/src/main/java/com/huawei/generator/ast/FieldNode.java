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

import com.huawei.generator.ast.custom.XFieldDoc;

import java.util.List;

/**
 * Field Node
 *
 * @since 2019-11-12
 */
public final class FieldNode extends AstNode {
    private ClassNode parent;

    private ModifierNode modifiers;

    private TypeNode type;

    private String name;

    private AstNode value;

    private XFieldDoc fieldDoc;

    private FieldNode() {
    }

    public ClassNode parent() {
        return parent;
    }

    public void setParent(ClassNode parent) {
        this.parent = parent;
    }

    public ModifierNode modifiers() {
        return modifiers;
    }

    public TypeNode type() {
        return type;
    }

    public String name() {
        return name;
    }

    public AstNode value() {
        return value;
    }

    public XFieldDoc getFieldDoc() {
        return fieldDoc;
    }

    public void setFieldDoc(XFieldDoc fieldDoc) {
        this.fieldDoc = fieldDoc;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static FieldNode create(ClassNode parent, List<String> modifiers, TypeNode type, String name,
        AstNode value) {
        FieldNode node = new FieldNode();
        node.parent = parent;
        node.modifiers = ModifierNode.create(modifiers);
        node.type = type;
        node.name = name;
        node.value = value;
        return node;
    }
}
