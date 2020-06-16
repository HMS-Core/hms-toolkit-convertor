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

package com.huawei.generator.method.call;

import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;

import java.util.List;

/**
 * Generates the router call for a router method.
 *
 * @since 2020-03-09
 */
public abstract class RouterCallHandler {
    protected MethodNode methodNode;

    protected JClass def;

    protected JMapping<JMethod> mapping;

    protected Component component;

    protected RouterCallHandler(MethodNode methodNode, JClass def, JMapping<JMethod> mapping, Component component) {
        this.methodNode = methodNode;
        this.def = def;
        this.mapping = mapping;
        this.component = component;
    }

    /**
     * Receiver of the routing call.
     *
     * @return the receiver
     */
    abstract StatementNode receiver();

    /**
     * Handles the routing call.
     *
     * @param body contains partially generated code, contents of the body may be modified in this method
     * @param args argument list of the routing call.
     * @return a statement representing the routing call.
     */
    public abstract StatementNode handleRouterCall(List<StatementNode> body, List<StatementNode> args);
}
