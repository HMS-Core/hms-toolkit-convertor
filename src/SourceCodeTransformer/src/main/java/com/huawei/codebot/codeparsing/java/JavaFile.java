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

import com.huawei.codebot.utils.StringUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.List;

/**
 * This class includes basic attributes of the file besides root node of AST.
 *
 * @since 2020-03-01
 */
public class JavaFile {
    /**
     * file name
     */
    public String name;

    /**
     * abstract file path
     */
    public String filePath;

    /**
     * raw text in the file
     */
    public String fileContent = null;

    /**
     * split text by line separator
     */
    public List<String> fileLines = null;

    /**
     * line separator
     */
    public String lineBreak = null;

    /**
     * root of the AST
     */
    public CompilationUnit compilationUnit = null;

    JavaFile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String className) {
        this.name = className;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    /**
     * @param node A node of the AST of the java file
     * @return the raw text of the node.
     */
    public String getRawSignature(ASTNode node) {
        int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition());
        int startColumnNumber = compilationUnit.getColumnNumber(node.getStartPosition());
        int endLineNumber = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
        int endColumnNumber = compilationUnit.getColumnNumber(node.getStartPosition() + node.getLength());

        if (startLineNumber == endLineNumber) {
            return fileLines.get(startLineNumber - 1).substring(startColumnNumber, endColumnNumber);
        }

        List<String> lines = fileLines.subList(startLineNumber, endLineNumber - 1);
        String startLine = fileLines.get(startLineNumber - 1).substring(startColumnNumber);
        String endLine = fileLines.get(endLineNumber - 1).substring(0, endColumnNumber);

        StringBuilder buffer = new StringBuilder();
        buffer.append(startLine);
        String lineSeparator = StringUtil.getLineBreak(fileContent);
        buffer.append(lineSeparator);
        if (!lines.isEmpty()) {
            buffer.append(String.join(lineSeparator, lines));
            buffer.append(lineSeparator);
        }
        buffer.append(endLine);

        return buffer.toString();
    }
}
