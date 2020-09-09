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

package com.huawei.codebot.codeparsing.java;

import com.huawei.codebot.codeparsing.Shielder;
import com.huawei.codebot.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.huawei.codebot.utils.StringUtil.getLineBreak;

/**
 * Analyze java file and get set all fields of the JavaFile object
 *
 * @since 2020-03-01
 */
public class JavaFileAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaFileAnalyzer.class);

    /**
     * @param filePath : the path of a single code file
     * @return javaFile which contains the root of AST and information about the java file.
     */
    public JavaFile extractJavaFileInfo(String filePath) {
        String codeContent = null;
        JavaFile javaFileObj = new JavaFile();

        try {
            if (StringUtils.isNotEmpty(filePath)) {
                codeContent = FileUtils.getFileContent(filePath);
            }
        } catch (IOException e) {
            LOGGER.error("An exception occurred during the processing:", e);
        }
        javaFileObj.setFilePath(filePath);
        if (codeContent != null) {
            javaFileObj.fileContent = codeContent;
            javaFileObj.lineBreak = getLineBreak(codeContent);
            javaFileObj.fileLines = FileUtils.cutStringToList(codeContent);
            javaFileObj.shielder = new Shielder(javaFileObj.fileLines);
            final CompilationUnit cu = generateAST(codeContent);
            javaFileObj.compilationUnit = cu;
            List types = cu.types();
            // if the java file is a enum file, types is null;
            if (types == null || types.size() == 0) {
                return javaFileObj;
            }
            if (!(types.get(0) instanceof TypeDeclaration)) {
                return javaFileObj;
            }
            TypeDeclaration typeDec = (TypeDeclaration) types.get(0);
            javaFileObj.setName(typeDec.getName().toString());
        }
        return javaFileObj;
    }

    /**
     * Generate AST
     *
     * @param codeContent source code
     * @return the root node of the AST
     */
    public static CompilationUnit generateAST(String codeContent) {
        ASTParser parser = ASTParser.newParser(AST.JLS11);
        parser.setSource(codeContent.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        ConcurrentHashMap<String, String> options = new ConcurrentHashMap<>(JavaCore.getOptions());
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        parser.setCompilerOptions(options);
        return (CompilationUnit) parser.createAST(null);
    }
}
