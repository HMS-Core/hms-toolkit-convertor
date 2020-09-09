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
package com.huawei.codebot.analyzer.x2y.java.field.access.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Switch Case Adapter
 *
 * @since 3.0.0.300
 */
public class SwitchCaseAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SwitchCaseAdapter.class);

    private static final String GET_GMS_VALUE = "getGmsValue";

    private static final String GET_HMS_VALUE = "getHmsValue";

    private static final String IS_HMS = "isHms";

    private static final String GLOBAL_SETTING = "org.xms.g.utils.GlobalEnvSetting";

    private static final String TRANSLATE_VALUE = "translateValue";

    private static final String PARAM = "param";

    private static final String VAL = "val";

    private static final String VALUES = "values";

    private static final String GMS_VALUE = "gmsValue";

    private static final String HMS_VALUE = "hmsValue";

    public void generate(SwitchCaseNode node) {
        AST ast = AST.newAST(AST.JLS11);

        CompilationUnit unit = ast.newCompilationUnit();
        setPackage(ast, unit, node.packageName());
        setImports(ast, unit, node);

        EnumDeclaration anEnum = createEnum(ast, node);
        unit.types().add(anEnum);

        printResult(unit, node.filePath(), node.className());
    }

    private EnumDeclaration createEnum(AST ast, SwitchCaseNode node) {
        // declare enum
        EnumDeclaration declaration = ast.newEnumDeclaration();
        declaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        String className = node.className();
        declaration.setName(ast.newSimpleName(className));

        // declare enum constants
        Map<String, SwitchCaseNode.Pair> instances = node.instances();
        instances.keySet().forEach(instance -> declaration.enumConstants().add(createEnumConstant(ast, instance, instances)));

        // declare field
        declaration.bodyDeclarations().add(createField(ast, GMS_VALUE, node.type().gmsType()));
        declaration.bodyDeclarations().add(createField(ast, HMS_VALUE, node.type().hmsType()));

        // declare constructor
        declaration.bodyDeclarations().add(createConstructor(ast, node));

        // declare translateValue method
        declaration.bodyDeclarations().add(createTranslateValue(ast, node));

        // declare getValue method
        declaration.bodyDeclarations().add(createValueGetter(ast, GET_GMS_VALUE, GMS_VALUE, node.type().gmsType()));
        declaration.bodyDeclarations().add(createValueGetter(ast, GET_HMS_VALUE, HMS_VALUE, node.type().hmsType()));

        return declaration;
    }

    private EnumConstantDeclaration createEnumConstant(AST ast, String constantName, Map<String, SwitchCaseNode.Pair> instances) {
        EnumConstantDeclaration constant = ast.newEnumConstantDeclaration();
        constant.setName(ast.newSimpleName(constantName));
        SwitchCaseNode.Pair pair = instances.get(constantName);
        constant.arguments().addAll(Arrays.asList(ast.newName(pair.gmsType()), ast.newName(pair.hmsType())));
        return constant;
    }

    private Type getType(AST ast, String type) {
        if ("int".equals(type)) {
            return ast.newPrimitiveType(PrimitiveType.INT);
        } else {
            return ast.newSimpleType(ast.newName(type));
        }
    }

    private FieldDeclaration createField(AST ast, String fieldName, String type) {
        VariableDeclarationFragment value = ast.newVariableDeclarationFragment();
        value.setName(ast.newSimpleName(fieldName));
        FieldDeclaration field = ast.newFieldDeclaration(value);
        field.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        field.setType(getType(ast, type));
        return field;
    }

    private MethodDeclaration createConstructor(AST ast, SwitchCaseNode node) {
        MethodDeclaration constructor = ast.newMethodDeclaration();
        constructor.setConstructor(true);
        List modifiers = constructor.modifiers();
        modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        constructor.setName(ast.newSimpleName(node.className()));
        SingleVariableDeclaration gArg = variableDeclaration(ast, GMS_VALUE, getType(ast, node.type().gmsType()));
        SingleVariableDeclaration hArg = variableDeclaration(ast, HMS_VALUE, getType(ast, node.type().hmsType()));
        constructor.parameters().addAll(Arrays.asList(gArg, hArg));

        Block block = ast.newBlock();

        block.statements().add(ast.newExpressionStatement(valueAssignment(ast, GMS_VALUE)));
        block.statements().add(ast.newExpressionStatement(valueAssignment(ast, HMS_VALUE)));
        constructor.setBody(block);
        return constructor;
    }

    private SingleVariableDeclaration variableDeclaration(AST ast, String valueName, Type type) {
        SingleVariableDeclaration value = ast.newSingleVariableDeclaration();
        value.setType(type);
        value.setName(ast.newSimpleName(valueName));
        return value;
    }

    private Assignment valueAssignment(AST ast, String valueName) {
        Assignment assignment = ast.newAssignment();
        FieldAccess fieldAccess = ast.newFieldAccess();
        fieldAccess.setExpression(ast.newThisExpression());
        fieldAccess.setName(ast.newSimpleName(valueName));
        assignment.setLeftHandSide(fieldAccess);
        assignment.setRightHandSide(ast.newSimpleName(valueName));
        return assignment;
    }

    private MethodDeclaration createTranslateValue(AST ast, SwitchCaseNode node) {
        MethodDeclaration translateValue = ast.newMethodDeclaration();
        translateValue.setName(ast.newSimpleName(TRANSLATE_VALUE));
        translateValue.setConstructor(false);
        translateValue.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        translateValue.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        String className = node.className();
        translateValue.setReturnType2(ast.newSimpleType(ast.newSimpleName(className)));
        SingleVariableDeclaration arg = ast.newSingleVariableDeclaration();
        arg.setType(getType(ast, node.type().gmsType()));
        arg.setName(ast.newSimpleName(PARAM));
        translateValue.parameters().add(arg);

        Block translateBody = ast.newBlock();
        translateBody.statements().addAll(createTranslateValueBody(ast, node));
        translateValue.setBody(translateBody);
        return translateValue;
    }

    private List<Statement> createTranslateValueBody(AST ast, SwitchCaseNode node) {
        MethodInvocation isHms = ast.newMethodInvocation();
        isHms.setName(ast.newSimpleName(IS_HMS));
        isHms.setExpression(ast.newName(GLOBAL_SETTING));
        IfStatement ifStatement = ast.newIfStatement();
        ifStatement.setExpression(isHms);
        String className = node.className();
        Block thenBlock = ast.newBlock();
        thenBlock.statements().add(forEachValue(ast, GET_HMS_VALUE, className, node));
        ifStatement.setThenStatement(thenBlock);
        Block elseBlock = ast.newBlock();
        elseBlock.statements().add(forEachValue(ast, GET_GMS_VALUE, className, node));
        ifStatement.setElseStatement(elseBlock);

        ReturnStatement ret = ast.newReturnStatement();
        NullLiteral nullLiteral = ast.newNullLiteral();
        ret.setExpression(nullLiteral);

        return Arrays.asList(ifStatement, ret);
    }

    private MethodDeclaration createValueGetter(AST ast, String methodName, String valueName, String type) {
        MethodDeclaration getter = ast.newMethodDeclaration();
        getter.setName(ast.newSimpleName(methodName));
        getter.setConstructor(false);
        getter.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        getter.setReturnType2(getType(ast, type));
        ReturnStatement ret = ast.newReturnStatement();
        ret.setExpression(ast.newSimpleName(valueName));
        Block block = ast.newBlock();
        block.statements().add(ret);
        getter.setBody(block);
        return getter;
    }

    private void setPackage(AST ast, CompilationUnit unit, String packageName) {
        PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
        packageDeclaration.setName(ast.newName(packageName));
        unit.setPackage(packageDeclaration);
    }

    private void setImports(AST ast, CompilationUnit unit, SwitchCaseNode node) {
        List<ImportDeclaration> importDeclarations = new ArrayList<>();
        node.imports().forEach(info -> importDeclarations.add(createImportDeclaration(ast, info)));
        unit.imports().addAll(importDeclarations);
    }

    private ImportDeclaration createImportDeclaration(AST ast, String importInfo) {
        ImportDeclaration declaration = ast.newImportDeclaration();
        declaration.setName(ast.newName(importInfo));
        declaration.setStatic(false);
        return declaration;
    }

    private IfStatement invokeValueEquals(AST ast, String methodName, SwitchCaseNode node) {
        IfStatement equals = ast.newIfStatement();
        if ("int".equals(node.type().gmsType())) {
            InfixExpression infixExpression = ast.newInfixExpression();
            infixExpression.setOperator(InfixExpression.Operator.EQUALS);
            MethodInvocation invokeGetValue = ast.newMethodInvocation();
            invokeGetValue.setName(ast.newSimpleName(methodName));
            invokeGetValue.setExpression(ast.newSimpleName(VAL));
            infixExpression.setLeftOperand(invokeGetValue);
            infixExpression.setRightOperand(ast.newSimpleName(PARAM));
            equals.setExpression(infixExpression);
        } else {
            MethodInvocation invokeArguments = ast.newMethodInvocation();
            invokeArguments.setName(ast.newSimpleName(methodName));
            invokeArguments.setExpression(ast.newSimpleName(VAL));
            MethodInvocation invokeParamEquals = ast.newMethodInvocation();
            invokeParamEquals.setName(ast.newSimpleName("equals"));
            invokeParamEquals.setExpression(ast.newSimpleName(PARAM));
            invokeParamEquals.arguments().add(invokeArguments);
            equals.setExpression(invokeParamEquals);
        }
        ReturnStatement ret = ast.newReturnStatement();
        ret.setExpression(ast.newSimpleName(VAL));
        Block thenBlock = ast.newBlock();
        thenBlock.statements().add(ret);
        equals.setThenStatement(thenBlock);
        return equals;
    }

    private EnhancedForStatement forEachValue(AST ast, String methodName, String className, SwitchCaseNode node) {
        EnhancedForStatement enhancedForStatement = ast.newEnhancedForStatement();
        SingleVariableDeclaration val = ast.newSingleVariableDeclaration();
        val.setName(ast.newSimpleName(VAL));
        val.setType(ast.newSimpleType(ast.newName(className)));
        enhancedForStatement.setParameter(val);
        MethodInvocation invokeValues = ast.newMethodInvocation();
        invokeValues.setName(ast.newSimpleName(VALUES));
        invokeValues.setExpression(ast.newName(className));
        enhancedForStatement.setExpression(invokeValues);
        IfStatement equalsHmsValue = invokeValueEquals(ast, methodName, node);
        Block block = ast.newBlock();
        block.statements().add(equalsHmsValue);
        enhancedForStatement.setBody(block);
        return enhancedForStatement;
    }

    private void printResult(CompilationUnit unit, String filePath, String className) {
        String sourceCode = unit.toString();
        Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
        DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
        Document doc = new Document(sourceCode);
        Map compilerOptions = new HashMap();
        //confirm java source base on java 1.8
        compilerOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
        compilerOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
        compilerOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
        DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences, compilerOptions);
        //format
        TextEdit edits = codeFormatter.format(org.eclipse.jdt.core.formatter.CodeFormatter.K_COMPILATION_UNIT,
                sourceCode, 0, sourceCode.length(), 0, null);

        try {
            edits.apply(doc);
        } catch (BadLocationException e) {
            logger.error("Failed to format code");
        }

        File dir = new File(filePath);
        if (dir.mkdirs()) {
            logger.info("The file already exists.");
        }
        File file = new File(dir, className + ".java");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            writer.write(doc.get());
            writer.flush();
        } catch (IOException e) {
            logger.error("Failed to write code to file");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.error("Failed to close file");
                }
            }
        }
    }
}
