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

package com.huawei.generator.method.exception;

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.CatchNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.ThrowNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception handler for wrapping X instances around exceptions throw by Z call.
 *
 * @since 2020-03-04
 */
public class Z2XExceptionHandler extends ExceptionHandler {
    private JMethod methodDef;

    public Z2XExceptionHandler(MethodNode methodNode, JMapping<JMethod> mapping, Component component) {
        super(methodNode, component);
        methodDef = component.jMethod(mapping);
    }

    @Override
    protected List<TypeNode> findXExceptions() {
        return exceptions.stream()
            .filter(t -> XMSUtils.isX(t.getTypeName()))
            // z method may only throw a subset of the exceptions defined in x method.
            .filter(this::thrownByZ)
            .collect(Collectors.toList());
    }

    @Override
    protected CatchNode catchException(TypeNode t) {
        // catch (zE e) 
        // throw new XE()
        NewNode newNode = NewNode.create(t, component.xWrapperParams("e"));
        String zType = component.x2Z(t.getTypeName());
        return CatchNode.create(zType, Collections.singletonList(ThrowNode.create(newNode)));
    }

    private boolean thrownByZ(TypeNode e) {
        return methodDef != null && methodDef.exceptions().contains(component.x2Z(e.getTypeName()));
    }

    @Override
    protected CatchNode catchGeneric(TypeNode eType) {
        List<StatementNode> catchBlock = new ArrayList<>();

        // throw ((XT) XObj2ZObj(e))
        StatementNode zObj = CallNode.create(VarNode.create(AstConstants.XMS_UTILS), component.getXMethodName(),
            Collections.singletonList(VarNode.create("e")));
        catchBlock.add(ThrowNode.create(CastExprNode.create(eType, zObj)));

        return CatchNode.create(methodNode.upperBoundOf(eType).getTypeName(), catchBlock);
    }
}
