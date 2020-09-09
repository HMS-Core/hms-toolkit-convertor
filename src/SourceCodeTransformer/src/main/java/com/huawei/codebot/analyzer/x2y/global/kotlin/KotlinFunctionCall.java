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

package com.huawei.codebot.analyzer.x2y.global.kotlin;

import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Data structure that contains several AST Nodes represents a function call
 *
 * @since 2019-12-27
 */
public class KotlinFunctionCall {
    private static final Logger LOGGER = LoggerFactory.getLogger(KotlinFunctionCall.class);
    private KotlinParser.PrimaryExpressionContext primaryExpressionContext;
    private List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContextList;
    private KotlinParser.PostfixUnarySuffixContext lastPostfixUnarySuffixContext;

    KotlinFunctionCall(KotlinParser.PostfixUnaryExpressionContext postfixUnaryExpressionContext) {
        this(postfixUnaryExpressionContext.primaryExpression(), postfixUnaryExpressionContext.postfixUnarySuffix());
    }

    public KotlinFunctionCall(
            KotlinParser.PrimaryExpressionContext primaryExpressionContext,
            List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContextList) {
        if (isFunctionCall(postfixUnarySuffixContextList)) {
            this.setPrimaryExpressionContext(primaryExpressionContext);
            this.setPostfixUnarySuffixContextList(postfixUnarySuffixContextList);
            this.setLastPostfixUnarySuffixContext(
                    postfixUnarySuffixContextList.get(postfixUnarySuffixContextList.size() - 1));
        }
    }

    /**
     * @param postfixUnarySuffixContexts node in Kotlin AST, which may represent field access, function and so on.
     * @return true if postfixUnarySuffixContexts can represent the function call.
     */
    public static boolean isFunctionCall(List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContexts) {
        if (CollectionUtils.isEmpty(postfixUnarySuffixContexts)) {
            return false;
        }

        for (KotlinParser.PostfixUnarySuffixContext postfixUnarySuffix : postfixUnarySuffixContexts) {
            if (postfixUnarySuffix.navigationSuffix() == null
                    && postfixUnarySuffix.callSuffix() == null
                    && postfixUnarySuffix.postfixUnaryOperator() == null
                    && postfixUnarySuffix.typeArguments() == null) {
                return false;
            }
        }
        return postfixUnarySuffixContexts.get(postfixUnarySuffixContexts.size() - 1).callSuffix() != null;
    }

    /**
     * @param ctx node in Kotlin AST, which may represent field access, function and so on.
     * @return true if ctx can represent the function call.
     */
    static boolean isFunctionCall(KotlinParser.PostfixUnaryExpressionContext ctx) {
        return isFunctionCall(ctx.postfixUnarySuffix());
    }

    boolean containsLambdaExpression() {
        return getLastPostfixUnarySuffixContext().callSuffix().annotatedLambda() != null;
    }

    /**
     * @return the simple name of the function.
     */
    public String getFunctionSimpleName() {
        for (KotlinParser.PostfixUnarySuffixContext postfixUnarySuffix :
                Lists.reverse(getPostfixUnarySuffixContextList())) {
            if (postfixUnarySuffix.navigationSuffix() != null
                    && postfixUnarySuffix.navigationSuffix().simpleIdentifier() != null) {
                return postfixUnarySuffix.navigationSuffix().simpleIdentifier().getText();
            }
        }
        if (getPrimaryExpressionContext().simpleIdentifier() != null) {
            return getPrimaryExpressionContext().simpleIdentifier().getText();
        } else {
            LOGGER.error("primaryExpressionContext.simpleIdentifier() is null. primaryExpressionContext is {}. ",
                    getPrimaryExpressionContext().getText());
            StringBuilder sb = new StringBuilder();
            for (KotlinParser.PostfixUnarySuffixContext postfixUnarySuffix : getPostfixUnarySuffixContextList()) {
                sb.append(postfixUnarySuffix.getText()).append("\t");
            }
            LOGGER.error("postfixUnarySuffixContextList is {}", sb.toString());
            return getPrimaryExpressionContext().getText();
        }
    }

    /**
     * @return return the node that contains and only contains the simpleName of the function
     */
    public ParserRuleContext getFunctionSimpleNameNode() {
        for (KotlinParser.PostfixUnarySuffixContext postfixUnarySuffix :
                Lists.reverse(getPostfixUnarySuffixContextList())) {
            if (postfixUnarySuffix.navigationSuffix() != null
                    && postfixUnarySuffix.navigationSuffix().simpleIdentifier() != null) {
                return postfixUnarySuffix.navigationSuffix().simpleIdentifier();
            }
        }
        return getPrimaryExpressionContext().simpleIdentifier();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getPrimaryExpressionContext().getText());
        for (KotlinParser.PostfixUnarySuffixContext ctx : this.getPostfixUnarySuffixContextList()) {
            sb.append(ctx.getText());
        }
        return sb.toString();
    }

    public KotlinParser.PostfixUnarySuffixContext getLastPostfixUnarySuffixContext() {
        return lastPostfixUnarySuffixContext;
    }

    private void setLastPostfixUnarySuffixContext(
            KotlinParser.PostfixUnarySuffixContext lastPostfixUnarySuffixContext) {
        this.lastPostfixUnarySuffixContext = lastPostfixUnarySuffixContext;
    }

    public KotlinParser.PrimaryExpressionContext getPrimaryExpressionContext() {
        return primaryExpressionContext;
    }

    private void setPrimaryExpressionContext(KotlinParser.PrimaryExpressionContext primaryExpressionContext) {
        this.primaryExpressionContext = primaryExpressionContext;
    }

    public List<KotlinParser.PostfixUnarySuffixContext> getPostfixUnarySuffixContextList() {
        return postfixUnarySuffixContextList;
    }

    private void setPostfixUnarySuffixContextList(
            List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContextList) {
        this.postfixUnarySuffixContextList = postfixUnarySuffixContextList;
    }
}
