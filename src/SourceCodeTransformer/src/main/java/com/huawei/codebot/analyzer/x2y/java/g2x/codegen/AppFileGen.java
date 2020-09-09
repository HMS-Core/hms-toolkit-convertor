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

package com.huawei.codebot.analyzer.x2y.java.g2x.codegen;

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;

import com.huawei.codebot.framework.DefectFixerType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * Use jdt to generate a new Class inherit android.app.Application
 *
 * @since 2020-07-08
 */
public class AppFileGen {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppFileGen.class);

    private static void createImport(AST ast, CompilationUnit unit) {
        ImportDeclaration appImportDeclaration = ast.newImportDeclaration();
        appImportDeclaration.setName(ast.newName("android.app.Application"));
        unit.imports().add(appImportDeclaration);

        ImportDeclaration envImportDeclaration = ast.newImportDeclaration();
        envImportDeclaration.setName(ast.newName("org.xms.g.utils.GlobalEnvSetting"));
        unit.imports().add(envImportDeclaration);
    }

    private static void createMethod(AST ast, CompilationUnit unit, TypeDeclaration typeDeclaration) {
        MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
        MarkerAnnotation markerAnnotation = ast.newMarkerAnnotation();
        markerAnnotation.setTypeName(ast.newSimpleName("Override"));
        methodDeclaration.modifiers().add(markerAnnotation);
        methodDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        methodDeclaration.setName(ast.newSimpleName("onCreate"));

        methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

        Block block = ast.newBlock();

        SuperMethodInvocation superMethodInvocation = ast.newSuperMethodInvocation();
        superMethodInvocation.setName(ast.newSimpleName("onCreate"));
        ExpressionStatement onCreateExpressionStatement = ast.newExpressionStatement(superMethodInvocation);
        block.statements().add(onCreateExpressionStatement);

        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newSimpleName("GlobalEnvSetting"));
        methodInvocation.setName(ast.newSimpleName("init"));
        methodInvocation.arguments().add(ast.newThisExpression());
        methodInvocation.arguments().add(ast.newNullLiteral());
        ExpressionStatement initExpressionStatement = ast.newExpressionStatement(methodInvocation);
        block.statements().add(initExpressionStatement);

        if (GlobalSettings.isNeedClassloader()) {
            ImportDeclaration xloaderImportDeclaration = ast.newImportDeclaration();
            xloaderImportDeclaration.setName(ast.newName("org.xms.adapter.utils.XLoader"));
            unit.imports().add(xloaderImportDeclaration);
            MethodInvocation classLoaderInvocation = ast.newMethodInvocation();
            classLoaderInvocation.setExpression(ast.newSimpleName("XLoader"));
            classLoaderInvocation.setName(ast.newSimpleName("init"));
            classLoaderInvocation.arguments().add(ast.newThisExpression());
            block.statements().add(ast.newExpressionStatement(classLoaderInvocation));
        }

        methodDeclaration.setBody(block);
        typeDeclaration.bodyDeclarations().add(methodDeclaration);
    }

    private static void saveFile(CompilationUnit unit, String appFilePath) {
        String sourceCode = unit.toString();
        Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
        DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
        Document doc = new Document(sourceCode);
        Map compilerOptions =  DefaultCodeFormatterOptions.getEclipseDefaultSettings().getMap();
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
            LOGGER.error("Failed to format code");
        }

        File file = new File(appFilePath);
        if (!file.exists()) {
            try {
                File dir = file.getParentFile();
                if(dir.mkdirs()){
                    LOGGER.info("The file already exists.");
                }
                if (!file.createNewFile()) {
                    LOGGER.info("The file create fail. Path: {}", appFilePath);
                }
            } catch (IOException e) {
                LOGGER.error("Create new App file failed");
            }
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            writer.write(doc.get());
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Failed to write code to file");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close file");
                }
            }
        }
    }

    /**
     * Create Application class
     *
     * @param appFilePath Application class path
     * @param packageName Package name of Application class
     */
    public static void createAppClass(String appFilePath, String packageName) {
        AST ast = AST.newAST(AST.JLS11);
        CompilationUnit unit = ast.newCompilationUnit();
        PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
        packageDeclaration.setName(ast.newName(packageName));
        unit.setPackage(packageDeclaration);

        createImport(ast, unit);

        TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
        typeDeclaration.setName(ast.newSimpleName("MyApp"));

        typeDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

        unit.types().add(typeDeclaration);
        typeDeclaration.setSuperclassType(ast.newSimpleType(ast.newSimpleName("Application")));

        createMethod(ast, unit, typeDeclaration);
        saveFile(unit, appFilePath);
    }
}
