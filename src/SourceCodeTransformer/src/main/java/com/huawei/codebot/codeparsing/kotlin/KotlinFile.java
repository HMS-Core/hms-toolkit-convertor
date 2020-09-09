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

package com.huawei.codebot.codeparsing.kotlin;

import com.huawei.codebot.codeparsing.CodeFileUtils;
import com.huawei.codebot.codeparsing.Shielder;
import com.huawei.codebot.framework.parser.kotlin.KotlinLexer;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * This class includes basic attributes of the file besides root node of AST.
 *
 * @since 2020-03-01
 */
public class KotlinFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(KotlinFile.class);

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
    public String fileContent;

    /**
     * split text by line separator
     */
    public List<String> fileLines;

    /**
     * line separator
     */
    public String lineBreak;

    /**
     * root of the AST
     */
    public ParseTree tree;

    /**
     * ignore code blocks
     */
    public Shielder shielder;

    public KotlinFile(String filePath) {
        this.filePath = filePath;
        this.name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
        try {
            this.fileContent = FileUtils.getFileContent(filePath);
            this.lineBreak = StringUtil.getLineBreak(this.fileContent);
            this.fileLines = FileUtils.cutStringToList(this.fileContent);
            this.shielder = new Shielder(this.fileLines);
            KotlinLexer kotlinLexer = new KotlinLexer(CharStreams.fromStream(new FileInputStream(filePath)));
            CommonTokenStream commonTokenStream = new CommonTokenStream(kotlinLexer);
            KotlinParser kotlinParser = new KotlinParser(commonTokenStream);
            this.tree = kotlinParser.kotlinFile();
        } catch (IOException e) {
            LOGGER.error("An exception occurred during the processing:", e);
        }
    }

    /**
     * @param ctx A node of the AST of the Kotlin file
     * @return the raw text of the node.
     */
    public String getRawSignature(ParserRuleContext ctx) {
        int startLineNumber = ctx.getStart().getLine();
        int startColumnNumber = ctx.getStart().getCharPositionInLine();
        int endLineNumber = ctx.getStop().getLine();
        int endColumnNumber = ctx.getStop().getCharPositionInLine() + ctx.getStop().getText().length();

        return CodeFileUtils.getRawSignature(startLineNumber, startColumnNumber, endLineNumber, endColumnNumber,
                fileLines, fileContent);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public List<String> getFileLines() {
        return fileLines;
    }

    public void setFileLines(List<String> fileLines) {
        this.fileLines = fileLines;
    }

    public String getLineBreak() {
        return lineBreak;
    }

    public void setLineBreak(String lineBreak) {
        this.lineBreak = lineBreak;
    }
}
