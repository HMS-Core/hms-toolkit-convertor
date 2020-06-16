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

import static com.huawei.generator.gen.AstConstants.REF_GET_INST;

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.CatchNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.ThrowNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.method.component.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception handler for extracting Z instances from exceptions throw by X call.
 *
 * @since 2020-03-04
 */
public class X2ZExceptionHandler extends ExceptionHandler {
    public X2ZExceptionHandler(MethodNode methodNode, Component component) {
        super(methodNode, component);
    }

    @Override
    protected List<TypeNode> findXExceptions() {
        return exceptions.stream()
            .filter(t -> component.isZType(t.getTypeName()))
            .map(t -> component.toX(t))
            .collect(Collectors.toList());
    }

    @Override
    protected CatchNode catchException(TypeNode t) {
        // catch (XException e)
        // throw e.getZInstance()
        CallNode ze = CallNode.create(VarNode.create("e"), component.getZInstance(), Collections.emptyList());
        return CatchNode.create(t.getTypeName(), Collections.singletonList(ThrowNode.create(ze)));
    }

    @Override
    protected CatchNode catchGeneric(TypeNode t) {
        // catch (TopType x) {}
        StatementNode getInst = CastExprNode.create(t, CallNode.create(VarNode.create(AstConstants.XMS_UTILS),
            REF_GET_INST, Arrays.asList(VarNode.create("e"), VarNode.create(String.valueOf(component.isH())))));
        return CatchNode.create(methodNode.upperBoundOf(t).getTypeName(),
            Collections.singletonList(ThrowNode.create(getInst)));
    }
}
