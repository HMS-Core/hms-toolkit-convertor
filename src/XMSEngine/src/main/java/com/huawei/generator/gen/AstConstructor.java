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

import static com.huawei.generator.gen.AstConstants.XMS_PACKAGE;

import com.huawei.generator.ast.AstNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.StmtStringNode;

import java.util.Arrays;
import java.util.Collections;

/**
 * AstConstructor class
 *
 * @since 2019-11-12
 */
public class AstConstructor {
    public static StatementNode gOrH() {
        return CallNode.create(VarNode.create(XMS_PACKAGE + ".GlobalEnvSetting"), "isHms", Collections.emptyList());
    }

    public static StatementNode log(AstNode node) {
        return CallNode.create(VarNode.create(XMS_PACKAGE + ".XmsLog"), "d",
            Arrays.asList(VarNode.create("\"XMSRouter\""), StmtStringNode.create(node)));
    }
}
