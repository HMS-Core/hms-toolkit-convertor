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

package com.huawei.codebot.analyzer.x2y.java.other.specificchanger.bean;

import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.utils.FileUtils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An assistant that help us analyze java source code by jdt.
 *
 * @since 2020-04-20
 */
public class JavaCodeAnalyzer extends JavaFileAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaCodeAnalyzer.class);
    // Represent line separator with oct code: \n is \012, \r is \015
    private static final String WIN_LINE_SEPARATOR = "\015\012";
    private static final String LINUX_LINE_SEPARATOR = "\012";

    private List<String> importList = new ArrayList<>();

    /**
     * Profile a java file to a {@link JavaClass} instance.
     *
     * @param filePath The path of a single code file.
     * @return A {@link JavaClass} instance.
     */
    public JavaClass extractJavaClassInfo(String filePath) {
        JavaClass javaClassObj = new JavaClass();
        try {
            String codeContent = FileUtils.getFileContent(filePath);
            javaClassObj.setFilePath(filePath);

            final CompilationUnit cu = generateAST(codeContent);

            // Get filename without extension.
            String fileName = getFileName(filePath);
            // for single code file.
            String packageName = "default";
            if (cu.getPackage() != null) {
                packageName = cu.getPackage().getName().toString();
                fileName = packageName + "." + fileName;
            }

            javaClassObj.setFileName(fileName);
            javaClassObj.setPackageName(packageName);
            for (Object im : cu.imports()) {
                ImportDeclaration imd = (ImportDeclaration) im;

                if (imd.isOnDemand()) {
                    importList.add(imd.getName() + ".*");
                    javaClassObj.addImport(imd.getName() + ".*");
                } else {
                    String importName = imd.getName().toString();
                    importList.add(importName);
                    javaClassObj.addImport(importName);
                }
            }

            List types = cu.types();
            // if the java file is a enum file, types is null;
            if (types == null || types.size() == 0) {
                return javaClassObj;
            }

            if (!(types.get(0) instanceof TypeDeclaration)) {
                return javaClassObj;
            }

            TypeDeclaration typeDec = (TypeDeclaration) types.get(0);
            javaClassObj.setClassName(typeDec.getName().toString());

            JavaClass parentClass = getParentClass(javaClassObj, packageName, typeDec);
            javaClassObj.setParentClass(parentClass);
            analyzeMethods(typeDec, cu, javaClassObj);

        } catch (IOException e) {
            for (StackTraceElement elem : e.getStackTrace()) {
                LOGGER.error("{}", elem, e);
            }
            LOGGER.error("extract Java Class Info failed, file path is:" + filePath);
        }
        return javaClassObj;
    }

    private JavaClass getParentClass(JavaClass javaClassObj, String packageName, TypeDeclaration typeDec) {
        String[] getPackageName = getParentPackageName(typeDec, javaClassObj);
        String parentPackageName = getPackageName[0];
        String parentName = getPackageName[1];

        List<String> potentialPackageNames = new ArrayList<String>();
        if (parentPackageName == null) {
            // extract potential package name
            for (String importName : javaClassObj.imports) {
                if (importName.contains(".*")) {
                    String candidateParentPackage = importName.substring(0, importName.lastIndexOf('.'));
                    potentialPackageNames.add(candidateParentPackage);
                }
            }
        }

        JavaClass parentClass = null;
        if (parentPackageName != null) {
            parentClass = new JavaClass(parentName, parentPackageName);
        } else if (potentialPackageNames.size() > 0) {
            potentialPackageNames.add(packageName);
            parentClass = new JavaClass(parentName, potentialPackageNames);
        } else {
            parentClass = new JavaClass(parentName, packageName);
        }
        return parentClass;
    }

    private String[] getParentPackageName(TypeDeclaration typeDec, JavaClass javaClassObj) {
        String parentPackageName = "";
        String parentName = "";
        Type supperClassType = typeDec.getSuperclassType();
        if (supperClassType != null) {
            parentName = supperClassType.toString();
            for (String importName : javaClassObj.imports) {
                if (importName.contains(parentName)) {
                    int index = importName.lastIndexOf(parentName);
                    if (index == 0) {
                        parentPackageName = importName.substring(0, importName.lastIndexOf("."));
                    } else {
                        parentPackageName = importName.substring(0, index - 1);
                    }
                    break;
                }
            }
        }
        return new String[]{parentPackageName, parentName};
    }

    private void analyzeMethods(TypeDeclaration typeDec, CompilationUnit cu, JavaClass javaClassObj) {
        MethodDeclaration[] methodDecs = typeDec.getMethods();
        for (MethodDeclaration method : methodDecs) {
            final JavaMethod javaMethod = new JavaMethod();
            // get method name
            SimpleName methodName = method.getName();
            javaMethod.setName(methodName.toString());

            // get method parameters
            List params = method.parameters();
            for (Object param : params) {
                SingleVariableDeclaration svd = (SingleVariableDeclaration) param;
                javaMethod.addInputParam(
                        new GenericVariableDeclaration(svd.getName().toString(), svd.getType().toString()));
            }

            // get method return type
            Type returnType = method.getReturnType2();
            // the return type of Constructor is null
            if (returnType != null) {
                javaMethod.setReturnType(returnType.toString());
            }

            int startLineNumber = cu.getLineNumber(method.getStartPosition());
            int endLineNumber = cu.getLineNumber(method.getStartPosition() + method.getLength());

            javaMethod.setBeginLineNumber(startLineNumber);
            javaMethod.setEndLineNumber(endLineNumber);

            // interface has no body
            if (method.getBody() == null) {
                continue;
            }

            acceptVisitor(cu, javaClassObj, method, javaMethod);

            // set javaClassObj
            javaClassObj.addMethod(javaMethod);
        }
    }

    private void acceptVisitor(CompilationUnit cu, JavaClass javaClassObj, MethodDeclaration method,
        JavaMethod javaMethod) {
        method.getBody().accept(new ASTVisitor() {
            @Override
            public boolean visit(VariableDeclarationFragment node) {
                SimpleName name = node.getName();
                if (node.getParent() instanceof VariableDeclarationStatement) {
                    VariableDeclarationStatement vds =
                        (VariableDeclarationStatement) node.getParent();
                    javaMethod.declaredVariables.put(
                        name.getIdentifier(),
                        new GenericVariableDeclaration(
                            name.getIdentifier(), vds.getType().toString()));
                } else if (node.getParent() instanceof FieldDeclaration) {
                    FieldDeclaration vds = (FieldDeclaration) node.getParent();
                    javaMethod.declaredVariables.put(
                        name.getIdentifier(),
                        new GenericVariableDeclaration(
                            name.getIdentifier(), vds.getType().toString()));
                }
                return true; // do not continue
            }

            @Override
            public boolean visit(SimpleName node) {
                if (javaMethod.declaredVariables.get(node.getIdentifier()) != null) {
                    javaMethod.usedVariables.put(
                        node.getIdentifier(),
                        javaMethod.declaredVariables.get(node.getIdentifier()));
                }
                return true;
            }

            @Override
            public boolean visit(ClassInstanceCreation classInstanceCreation) {
                JavaClassCreationInstance classInstance = new JavaClassCreationInstance();
                classInstance.setTypeName(classInstanceCreation.getType().toString());
                classInstance.setStartPosition(classInstanceCreation.getStartPosition());
                classInstance.setEndPosition(
                    classInstance.getStartPosition() + classInstanceCreation.getLength());
                classInstance.setStartLine(cu.getLineNumber(classInstance.getStartPosition()));
                classInstance.setEndLine(cu.getLineNumber(classInstance.getEndPosition()));
                classInstance.setEndLine(cu.getLineNumber(classInstance.getEndPosition()));
                for (Object arg : classInstanceCreation.arguments()) {
                    classInstance.addArgument(String.valueOf(arg));
                }
                if (classInstanceCreation.getParent() != null) {
                    ASTNode parentAstNode = classInstanceCreation.getParent();
                    if ((parentAstNode instanceof VariableDeclarationFragment)
                        && parentAstNode.getParent() != null) {
                        ASTNode grandAstNode = parentAstNode.getParent();
                        if (grandAstNode instanceof VariableDeclarationStatement) {
                            analyzeVarDeclStmt(classInstance, grandAstNode);
                        }
                    } else if (parentAstNode instanceof ExpressionStatement) {
                        analyzeVarDeclStmt(classInstance, parentAstNode);
                    }
                }
                if (classInstance.getVariableDeclarationStatement().equals("")) {
                    classInstance.setVariableDeclarationStatement(classInstanceCreation.toString());
                }

                javaMethod.addClassInstance(classInstance);

                return true;
            }

            @Override
            public boolean visit(MethodInvocation calleeNode) {
                JavaMethod calleeMethod = new JavaMethod();

                int startPosition = calleeNode.getStartPosition();
                int endPosition = startPosition + calleeNode.getLength();
                int startLine = cu.getLineNumber(startPosition);
                int endLine = cu.getLineNumber(endPosition);
                calleeMethod.setBeginLineNumber(startLine);
                calleeMethod.setEndLineNumber(endLine);
                calleeMethod.setBeginPosition(startPosition);
                calleeMethod.setEndPosition(endPosition);
                calleeMethod.setName(calleeNode.getName().toString());

                String expression = calleeNode.toString();
                calleeMethod.setOriginalSignature(expression);

                String ownerName = null;
                if (calleeNode.getExpression() != null) {
                    ownerName = calleeNode.getExpression().toString();
                }

                if (StringUtils.isBlank(ownerName) || ownerName.equals("this")) {
                    calleeMethod.setOwnerName("this");
                    calleeMethod.setOwnerClassType(
                        javaClassObj.getPackageName() + "." + javaClassObj.getClassName());
                } else {
                    calleeMethod.setOwnerName(ownerName);
                    if (javaMethod.declaredVariables.get(ownerName) != null) {
                        String ownerType = javaMethod.declaredVariables.get(ownerName).getType();
                        calleeMethod.setOwnerClassType(ownerType);
                    }
                }

                javaMethod.addMethodCallee(calleeMethod);
                return true;
            }
        });
    }

    private void analyzeVarDeclStmt(JavaClassCreationInstance classInstance, ASTNode grandAstNode) {
        String rawData = grandAstNode.toString();
        if (rawData.endsWith(WIN_LINE_SEPARATOR)) {
            int lastIndex = rawData.lastIndexOf(WIN_LINE_SEPARATOR);
            rawData = rawData.substring(0, lastIndex);
        } else {
            if (rawData.endsWith(LINUX_LINE_SEPARATOR)) {
                int lastIndex = rawData.lastIndexOf(LINUX_LINE_SEPARATOR);
                rawData = rawData.substring(0, lastIndex);
            }
        }
        classInstance.setVariableDeclarationStatement(rawData);
        classInstance.setFixFlag(true);
    }

    /**
     * Return the filename that the path string identified.
     *
     * @param filePath A path string.
     * @return Filename that the path string identified.
     */
    public String getFileName(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            return fileName;
        }
        return null;
    }
}
