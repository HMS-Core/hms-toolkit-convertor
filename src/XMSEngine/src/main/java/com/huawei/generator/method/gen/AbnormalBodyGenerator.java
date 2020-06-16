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

import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.ThrowNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;

import java.util.Collections;
import java.util.List;

/**
 * Collection of body generators that just generate a throw expression.
 *
 * @since 2020-03-04
 */
public abstract class AbnormalBodyGenerator implements BodyGenerator {
    public static final BodyGenerator UNSUPPORTED = new AbnormalBodyGenerator("Not Supported") {};

    public static final BodyGenerator REDUNDANT = new AbnormalBodyGenerator("Stub") {};

    public static final BodyGenerator NOT_FOR_INHERITING = new AbnormalBodyGenerator("Not for inheriting") {};

    public static final BodyGenerator UNSUPPORTED_BRANCH = new AbnormalBodyGenerator("HMS is not supported") {};

    public static final BodyGenerator TO_DO_PLACEHOLDER = new AbnormalBodyGenerator("TODO block must be filled") {};

    private String info;

    // Forbids instantiation from outside world.
    private AbnormalBodyGenerator(String info) {
        // surround the given text with double quotes
        this.info = "\"" + info + "\"";
    }

    @Override
    public List<StatementNode> generate() {
        // throw new RuntimeException("...")
        return Collections.singletonList(ThrowNode.create(NewNode
            .create(TypeNode.create(AstConstants.RUNTIME_EXCEPTION), Collections.singletonList(VarNode.create(info)))));
    }
}
