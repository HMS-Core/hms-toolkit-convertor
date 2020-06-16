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

package com.huawei.codebot.analyzer.x2y.global.commonvisitor;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.VariableInfo;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.huawei.codebot.framework.parser.kotlin.KotlinParserBaseVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * ASTVisitor which provides variable type information in each block according to variable's scope.
 * Sub visitor class can inherit this class and get variable type information in any blocks if it needs.
 *
 * @since 2019-07-14
 */
public class KotlinLocalVariablesVisitor extends KotlinParserBaseVisitor<Boolean> {
    // store local variable info
    private Stack<Map<String, VariableInfo>> varMaps = new Stack<>();

    private KotlinTypeInferencer typeInferencer;

    private VisitorIterator iterator;

    public KotlinLocalVariablesVisitor() {
        super();
        typeInferencer = new KotlinTypeInferencer(this);
        iterator = new VisitorIterator();
    }

    @Override
    public Boolean visitTopLevelObject(KotlinParser.TopLevelObjectContext ctx) {
        Map<String, VariableInfo> varMap = new HashMap<>();
        varMaps.push(varMap);
        if (ctx.declaration() != null && ctx.declaration().propertyDeclaration() != null) {
            KotlinParser.PropertyDeclarationContext propertyDeclaration = ctx.declaration().propertyDeclaration();
            if (propertyDeclaration.multiVariableDeclaration() != null) {
                KotlinParser.MultiVariableDeclarationContext multiVAriableDeclaration =
                        propertyDeclaration.multiVariableDeclaration();
                List<KotlinParser.VariableDeclarationContext> variableDeclarations =
                        multiVAriableDeclaration.variableDeclaration();
                for (KotlinParser.VariableDeclarationContext variableDeclaration : variableDeclarations) {
                    VariableInfo varInfo = new VariableInfo();
                    varInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(ctx));
                    varInfo.setPackageName(KotlinASTUtils.getPackageName(ctx));
                    varInfo.setName(variableDeclaration.simpleIdentifier().getText());
                    varInfo.setType(KotlinTypeInferencer.getTypeInfo(variableDeclaration.type()));
                    varInfo.setDeclaration(variableDeclaration);
                    varMap.put(varInfo.getName(), varInfo);
                }
            } else if (propertyDeclaration.variableDeclaration() != null) {
                KotlinParser.VariableDeclarationContext variableDeclaration = propertyDeclaration.variableDeclaration();
                VariableInfo varInfo = new VariableInfo();
                varInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(ctx));
                varInfo.setPackageName(KotlinASTUtils.getPackageName(ctx));
                varInfo.setName(variableDeclaration.simpleIdentifier().getText());
                varInfo.setType(KotlinTypeInferencer.getTypeInfo(variableDeclaration.type()));
                varInfo.setDeclaration(variableDeclaration);
                varMap.put(varInfo.getName(), varInfo);
            }
        }
        Boolean result = super.visitTopLevelObject(ctx);
        varMaps.pop();
        return result;
    }

    @Override
    public Boolean visitClassDeclaration(KotlinParser.ClassDeclarationContext ctx) {
        String className = ctx.simpleIdentifier().getText();
        String[] fullType = JavaTypeInferencer.getFullType(
                className, KotlinASTUtils.getPackageName(ctx), KotlinASTUtils.getImportNames(ctx));
        if (fullType.length != 0) {
            Map<String, VariableInfo> thisMap = new HashMap<>();
            varMaps.push(thisMap);
            VariableInfo varInfo = new VariableInfo();
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
            varInfo.setType(typeInfo);
            varInfo.setName("this");
            varInfo.setPackageName(KotlinASTUtils.getPackageName(ctx));
            varInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(ctx));
            varInfo.setDeclaration(ctx);
            thisMap.put("this", varInfo);
            iterator.loadSuperField(varMaps, fullType[0] + "." + fullType[1]);
        }

        Map<String, VariableInfo> fieldMap = new HashMap<>();
        varMaps.push(fieldMap);
        if (ctx.classBody() != null) {
            KotlinParser.ClassMemberDeclarationsContext memberDeclarationsContext =
                    ctx.classBody().classMemberDeclarations();
            List<KotlinParser.ClassMemberDeclarationContext> memberDeclarations =
                    memberDeclarationsContext.classMemberDeclaration();
            for (KotlinParser.ClassMemberDeclarationContext memberDeclaration : memberDeclarations) {
                if (memberDeclaration.declaration() != null
                        && memberDeclaration.declaration().propertyDeclaration() != null) {
                    KotlinParser.PropertyDeclarationContext propertyDeclaration =
                            memberDeclaration.declaration().propertyDeclaration();
                    getVarInfoFromPropertyDeclaration(fieldMap, propertyDeclaration);
                }
            }
        }
        Boolean result = super.visitClassDeclaration(ctx);
        varMaps.pop();
        return result;
    }

    private void getVarInfoFromPropertyDeclaration(
            Map<String, VariableInfo> fieldMap, KotlinParser.PropertyDeclarationContext propertyDeclaration) {
        if (propertyDeclaration.multiVariableDeclaration() != null) {
            KotlinParser.MultiVariableDeclarationContext multiVAriableDeclaration =
                    propertyDeclaration.multiVariableDeclaration();
            List<KotlinParser.VariableDeclarationContext> variableDeclarations =
                    multiVAriableDeclaration.variableDeclaration();
            for (KotlinParser.VariableDeclarationContext variableDeclaration : variableDeclarations) {
                VariableInfo varInfo = new VariableInfo();
                varInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(variableDeclaration));
                varInfo.setPackageName(KotlinASTUtils.getPackageName(variableDeclaration));
                varInfo.setName(variableDeclaration.simpleIdentifier().getText());
                if (variableDeclaration.type() != null) {
                    varInfo.setType(KotlinTypeInferencer.getTypeInfo(variableDeclaration.type()));
                } else {
                    varInfo.setType(typeInferencer.getExpressionType(propertyDeclaration.expression()));
                }
                varInfo.setDeclaration(propertyDeclaration);
                fieldMap.put(varInfo.getName(), varInfo);
            }
        } else if (propertyDeclaration.variableDeclaration() != null) {
            KotlinParser.VariableDeclarationContext variableDeclaration = propertyDeclaration.variableDeclaration();
            VariableInfo varInfo = new VariableInfo();
            varInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(propertyDeclaration.getParent().getParent()));
            varInfo.setPackageName(KotlinASTUtils.getPackageName(propertyDeclaration.getParent().getParent()));
            varInfo.setName(variableDeclaration.simpleIdentifier().getText());
            if (variableDeclaration.type() != null) {
                varInfo.setType(KotlinTypeInferencer.getTypeInfo(variableDeclaration.type()));
            } else {
                varInfo.setType(typeInferencer.getExpressionType(propertyDeclaration.expression()));
            }
            varInfo.setDeclaration(propertyDeclaration);
            fieldMap.put(varInfo.getName(), varInfo);
        }
    }

    @Override
    public Boolean visitFunctionDeclaration(KotlinParser.FunctionDeclarationContext ctx) {
        Map<String, VariableInfo> varMap = new HashMap<>();
        varMaps.push(varMap);
        List<KotlinParser.FunctionValueParameterContext> parameters =
                ctx.functionValueParameters().functionValueParameter();
        for (KotlinParser.FunctionValueParameterContext valueParameterContext : parameters) {
            KotlinParser.ParameterContext parameter = valueParameterContext.parameter();
            VariableInfo varInfo = new VariableInfo();
            varInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(ctx));
            varInfo.setPackageName(KotlinASTUtils.getPackageName(ctx));
            varInfo.setName(parameter.simpleIdentifier().getText());
            varInfo.setDeclaration(valueParameterContext);
            varInfo.setType(KotlinTypeInferencer.getTypeInfo(parameter.type()));
            varMap.put(varInfo.getName(), varInfo);
        }
        Boolean result = super.visitFunctionDeclaration(ctx);
        varMaps.pop();
        return result;
    }

    @Override
    public Boolean visitBlock(KotlinParser.BlockContext ctx) {
        varMaps.push(new HashMap<>());
        Boolean result = super.visitBlock(ctx);
        varMaps.pop();
        return result;
    }

    @Override
    public Boolean visitForStatement(KotlinParser.ForStatementContext ctx) {
        Map<String, VariableInfo> varMap = new HashMap<>();
        varMaps.push(varMap);
        if (ctx.multiVariableDeclaration() != null) {
            KotlinParser.MultiVariableDeclarationContext multiVariableDeclaration = ctx.multiVariableDeclaration();
            List<KotlinParser.VariableDeclarationContext> variableDeclarations =
                    multiVariableDeclaration.variableDeclaration();
            for (KotlinParser.VariableDeclarationContext variableDeclaration : variableDeclarations) {
                VariableInfo varInfo = new VariableInfo();
                varInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(ctx));
                varInfo.setPackageName(KotlinASTUtils.getPackageName(ctx));
                varInfo.setName(variableDeclaration.simpleIdentifier().getText());
                varInfo.setType(KotlinTypeInferencer.getTypeInfo(variableDeclaration.type()));
                varInfo.setDeclaration(variableDeclaration);
                varMap.put(varInfo.getName(), varInfo);
            }
        } else if (ctx.variableDeclaration() != null) {
            KotlinParser.VariableDeclarationContext variableDeclaration = ctx.variableDeclaration();
            VariableInfo varInfo = new VariableInfo();
            varInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(ctx));
            varInfo.setPackageName(KotlinASTUtils.getPackageName(ctx));
            varInfo.setName(variableDeclaration.simpleIdentifier().getText());
            varInfo.setType(KotlinTypeInferencer.getTypeInfo(variableDeclaration.type()));
            varInfo.setDeclaration(variableDeclaration);
            varMap.put(varInfo.getName(), varInfo);
        }
        Boolean result = super.visitForStatement(ctx);
        varMaps.pop();
        return result;
    }

    @Override
    public Boolean visitPropertyDeclaration(KotlinParser.PropertyDeclarationContext ctx) {
        Map<String, VariableInfo> varMap = new HashMap<>();
        varMaps.push(varMap);
        getVarInfoFromPropertyDeclaration(varMap, ctx);
        return super.visitPropertyDeclaration(ctx);
    }

    /**
     * get VariableInfo by name
     *
     * @param name variable name
     * @return VariableInfo
     */
    public VariableInfo getVarInfo(String name) {
        return iterator.getVarInfo(this.varMaps, name);
    }
}
