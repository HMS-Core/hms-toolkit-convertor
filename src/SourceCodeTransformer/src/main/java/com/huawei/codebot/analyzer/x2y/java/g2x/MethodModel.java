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

package com.huawei.codebot.analyzer.x2y.java.g2x;

import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class MethodModel {

    private boolean javaFile;

    private CompilationUnit unit;

    private TypeDeclaration typeDeclaration;

    private String buggyFilePath;

    private KotlinParser.ClassDeclarationContext classDeclarationContext;

    public boolean isJavaFile() {
        return javaFile;
    }

    public void setJavaFile(boolean javaFile) {
        this.javaFile = javaFile;
    }

    public CompilationUnit getUnit() {
        return unit;
    }

    public void setUnit(CompilationUnit unit) {
        this.unit = unit;
    }

    public TypeDeclaration getTypeDeclaration() {
        return typeDeclaration;
    }

    public void setTypeDeclaration(TypeDeclaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
    }

    public String getBuggyFilePath() {
        return buggyFilePath;
    }

    public void setBuggyFilePath(String buggyFilePath) {
        this.buggyFilePath = buggyFilePath;
    }

    public KotlinParser.ClassDeclarationContext getClassDeclarationContext() {
        return classDeclarationContext;
    }

    public void setClassDeclarationContext(KotlinParser.ClassDeclarationContext classDeclarationContext) {
        this.classDeclarationContext = classDeclarationContext;
    }
}
