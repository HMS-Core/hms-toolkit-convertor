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

import com.huawei.generator.ast.IfNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.gen.AstConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Generator for DualZBody
 *
 * @since 2020-03-03
 */
public final class DualZBodyGenerator implements BodyGenerator {
    private final BodyGenerator gGenerator;

    private final BodyGenerator hGenerator;

    public DualZBodyGenerator(BodyGenerator gGenerator, BodyGenerator hGenerator) {
        this.gGenerator = gGenerator;
        this.hGenerator = hGenerator;
    }

    @Override
    public List<StatementNode> generate() {
        return Collections
            .singletonList(IfNode.create(AstConstructor.gOrH(), hGenerator.generate(), gGenerator.generate()));
    }
}
