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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LambdaNodeTest {
    @Test
    public void lambdaWithSingleArg() {
        VarNode var = VarNode.create("it");
        CallNode call = CallNode.create("get", Arrays.asList(var));
        BlockNode block = BlockNode.create(Arrays.asList(call));
        List<VarNode> vars = Arrays.asList(var);
        LambdaNode lambda1 = LambdaNode.create(vars, block);
        Assert.assertEquals("it -> {\n    get(it);\n}".replace("\n", System.lineSeparator()), lambda1.toString());

        LambdaNode lambda2 = LambdaNode.create(vars, call);
        Assert.assertEquals("it -> get(it)", lambda2.toString());
    }

    @Test
    public void lambdaWithTwoArgs() {
        VarNode var1 = VarNode.create("e1");
        VarNode var2 = VarNode.create("e2");
        CallNode call = CallNode.create("f", Arrays.asList(var1, var2));
        List<VarNode> vars = Arrays.asList(var1, var2);
        LambdaNode lambda = LambdaNode.create(vars, call);
        Assert.assertEquals("(e1, e2) -> f(e1, e2)", lambda.toString());
    }
}