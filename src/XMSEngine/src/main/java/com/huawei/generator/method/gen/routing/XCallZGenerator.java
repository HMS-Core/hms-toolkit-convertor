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

package com.huawei.generator.method.gen.routing;

import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.call.XCallZRouter;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.exception.Z2XExceptionHandler;
import com.huawei.generator.method.param.X2ZMethodParamHandler;
import com.huawei.generator.method.returns.Z2XMethodReturnHandler;

/**
 * XNormalGenerator
 *
 * @since 2020-03-11
 */
public class XCallZGenerator extends RoutingMethodGenerator {
    public XCallZGenerator(MethodNode methodNode, JClass def, JMapping<JMethod> mapping, Component component) {
        super(methodNode);
        paramHandler = new X2ZMethodParamHandler(methodNode, component);
        routerCallHandler = new XCallZRouter(methodNode, def, mapping, component);
        returnHandler = new Z2XMethodReturnHandler(methodNode, mapping, component);
        exceptionHandler = new Z2XExceptionHandler(methodNode, mapping, component);
    }
}
