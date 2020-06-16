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

package com.huawei.generator.ast.custom;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.gen.AstConstants;

/**
 * A kind of class node representing classes from X world, such as X adapter classes and XImpl classes.
 *
 * @since 2020-03-06
 */
public abstract class XClassNode extends ClassNode {
    @Override
    protected String adaptTypeName(TypeNode t, boolean isXType) {
        if (isXType) {
            return t.getTypeName();
        } else {
            // add prefix
            return AstConstants.GENERIC_PREFIX + t.getTypeName();
        }
    }
}
