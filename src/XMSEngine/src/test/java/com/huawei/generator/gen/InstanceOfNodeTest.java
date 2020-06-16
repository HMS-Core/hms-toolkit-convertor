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

package com.huawei.generator.gen;

import com.huawei.generator.ast.InstanceOfNode;
import com.huawei.generator.ast.VarNode;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for Instance Of Node
 *
 * @since 2019-11-20
 */
public class InstanceOfNodeTest implements TestVisitor<InstanceOfNode> {
    private static final String E = "com.google.android.gms.nearby.DeviceNotFoundException";

    private static final String EXPECTED = "e instanceof " + E;

    @Test
    public void testVisit() {
        VarNode exception = VarNode.create(E);
        InstanceOfNode instanceOfNode = InstanceOfNode.create(exception);
        visit(instanceOfNode);
    }

    @Override
    public void visit(InstanceOfNode node) {
        String builder = "e" + " instanceof " + node.getException().value();
        Assert.assertEquals(builder, EXPECTED);
    }
}