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

package com.huawei.generator.method.returns;

import com.huawei.generator.ast.StatementNode;

import java.util.List;

/**
 * Return handler.
 *
 * @since 2020-03-11
 */
public interface ReturnHandler {
    /**
     * Handles method return value.
     *
     * @param body method body.
     * @param rawValue the raw value.
     */
    void handleReturnValue(List<StatementNode> body, StatementNode rawValue);
}
