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

import com.huawei.generator.gen.JavaCodeGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Base interface of AST nodes.
 *
 * @since 2019-11-12
 */
public abstract class AstNode {
    /**
     * accept base method.
     *
     * @param visitor AstVisitor
     */
    public abstract void accept(AstVisitor visitor);

    @Override
    public String toString() {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            JavaCodeGenerator.from(this).to(stream);
            return stream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
