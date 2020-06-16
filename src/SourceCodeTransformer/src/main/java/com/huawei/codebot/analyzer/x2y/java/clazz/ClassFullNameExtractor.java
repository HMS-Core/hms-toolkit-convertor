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

package com.huawei.codebot.analyzer.x2y.java.clazz;

import com.huawei.codebot.analyzer.x2y.global.TypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;

/**
 * Extract the full class name from a node in AST.
 *
 * @since 2020-04-16
 */
public class ClassFullNameExtractor {
    /**
     * @param ctx Kotlin ASTNode contains contains simple class name or full class name
     * @return full class name from ASTNode
     */
    public String extractFullClassName(ParserRuleContext ctx) {
        if (ctx instanceof KotlinParser.IdentifierContext) {
            return ctx.getText();
        }
        String[] fullType = TypeInferencer.getFullType(ctx.getText(), KotlinASTUtils.getPackageName(ctx),
                KotlinASTUtils.getImportNames(ctx));
        if (fullType.length != 0) {
            return fullType[0] + "." + fullType[1];
        }
        return ctx.getText();
    }

    /**
     * @param node Java ASTNode contains contains simple class name or full class name
     * @return full class name from ASTNode
     */
    public String extractFullClassName(ASTNode node) {
        if (node instanceof ImportDeclaration) {
            return ((ImportDeclaration) node).getName().toString();
        }
        String name = node.toString();
        if (node instanceof MarkerAnnotation) {
            name = ((MarkerAnnotation) node).getTypeName().toString();
        }
        if (node.getRoot() instanceof CompilationUnit) {
            String[] fullType = JavaTypeInferencer.getFullType(name, (CompilationUnit) node.getRoot());
            if (fullType.length != 0) {
                return fullType[0] + "." + fullType[1];
            }
        }
        return "";
    }
}
