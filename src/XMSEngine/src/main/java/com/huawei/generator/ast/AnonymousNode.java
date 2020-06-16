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

import java.util.ArrayList;
import java.util.List;

/**
 * Anonymous node
 *
 * @since 2019-11-18
 */
public class AnonymousNode extends ClassNode {
    private TypeNode type;

    private List<MethodNode> methods;

    private List<StatementNode> parameters;

    AnonymousNode() {
    }

    public String type() {
        return type.toString();
    }

    public List<MethodNode> methods() {
        return methods;
    }

    public List<StatementNode> parameters() {
        return parameters;
    }

    @Override
    public boolean isInner() {
        return true;
    }

    @Override
    public List<TypeNode> generics() {
        return type.getGenericType();
    }

    // This is a hack for MethodNode.generics() to work properly.
    @Override
    public TypeNode getXType() {
        return type;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static AnonymousNode create(String type, List<StatementNode> parameters, ClassNode outerClass) {
        AnonymousNode node = new AnonymousNode();
        node.setOuterClass(outerClass);
        node.parameters = parameters;
        node.type = TypeNode.create(type, false);
        node.methods = new ArrayList<>();
        return node;
    }
}
