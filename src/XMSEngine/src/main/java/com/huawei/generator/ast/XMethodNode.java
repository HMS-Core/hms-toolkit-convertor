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

import com.huawei.generator.gen.AstConstants;

/**
 * Method node that represents a X method.
 *
 * @since 2020-03-06
 */
public class XMethodNode extends MethodNode {
    @Override
    protected String adaptTypeName(TypeNode t, boolean isXType) {
        if (isXType) {
            return t.getTypeName();
        } else {
            // add prefix
            return AstConstants.GENERIC_PREFIX + t.getTypeName();
        }
    }

    @Override
    public MethodNode normalize() {
        super.normalize();
        renameGenerics();
        return this;
    }

    private void renameGenerics() {
        // rename generics in return type & parameter types
        if (returnType() != null) {
            renameGeneric(returnType());
        }
        if (getMethodGenerics() != null) {
            getMethodGenerics().forEach(this::renameGeneric);
        }
        parameters().forEach(this::renameGeneric);
        if (getExceptions() != null) {
            getExceptions().forEach(this::renameGeneric);
        }
    }
}
