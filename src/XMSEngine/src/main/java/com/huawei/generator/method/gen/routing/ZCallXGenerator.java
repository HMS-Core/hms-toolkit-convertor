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
import com.huawei.generator.method.call.ZCallXRouter;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.exception.X2ZExceptionHandler;
import com.huawei.generator.method.param.Z2XMethodParamHandler;
import com.huawei.generator.method.returns.X2ZMethodReturnHandler;

/**
 * Method
 *
 * @since 2019-12-02
 */
public class ZCallXGenerator extends RoutingMethodGenerator {
    public ZCallXGenerator(MethodNode methodNode, MethodNode xMethodNode, JClass def, JMapping<JMethod> mapping,
        Component component) {
        super(methodNode);
        paramHandler = new Z2XMethodParamHandler(methodNode, xMethodNode, component);
        routerCallHandler = new ZCallXRouter(methodNode, def, mapping, component);
        returnHandler = new X2ZMethodReturnHandler(methodNode, xMethodNode, component);
        exceptionHandler = new X2ZExceptionHandler(methodNode, component);
    }
}
