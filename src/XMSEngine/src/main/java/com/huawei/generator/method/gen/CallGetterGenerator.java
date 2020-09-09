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

package com.huawei.generator.method.gen;

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.method.component.Component;

import java.util.Collections;
import java.util.List;

/**
 * Collection of body generators that just generate a callee by getZInstanceXName in g+h or g+obj.
 *
 * @since 2020-03-04
 */
public final class CallGetterGenerator implements BodyGenerator {
    private MethodNode methodNode;

    private Component component;

    public CallGetterGenerator(MethodNode methodNode, Component component) {
        this.methodNode = methodNode;
        this.component = component;
    }

    @Override
    public List<StatementNode> generate() {
        String calleeName = component.getInstancePrefix() + methodNode.parent().getXType().getTypeNameWithoutPackage();
        ReturnNode rtNode = ReturnNode.create(CallNode.create(calleeName, Collections.emptyList()));
        return Collections.singletonList(rtNode);
    }
}
