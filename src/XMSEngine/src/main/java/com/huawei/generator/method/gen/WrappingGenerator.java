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

import static com.huawei.generator.gen.AstConstants.WRAPPER_FIELD;

import com.huawei.generator.ast.IfNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.VarNode;

import java.util.Collections;
import java.util.List;

/**
 * WrappingGenerator class
 *
 * @since 2020-05-12
 */
public class WrappingGenerator implements BodyGenerator {
    private BodyGenerator callZGenerator;

    private BodyGenerator callSuperGenerator;

    public WrappingGenerator(BodyGenerator callZGenerator, BodyGenerator callSuperGenerator) {
        this.callZGenerator = callZGenerator;
        this.callSuperGenerator = callSuperGenerator;
    }

    @Override
    public List<StatementNode> generate() {
        return Collections.singletonList(
            IfNode.create(VarNode.create(WRAPPER_FIELD), callZGenerator.generate(), callSuperGenerator.generate()));
    }
}
