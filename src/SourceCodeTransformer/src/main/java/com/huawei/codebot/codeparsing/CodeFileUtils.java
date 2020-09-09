package com.huawei.codebot.codeparsing;

import com.huawei.codebot.utils.StringUtil;

import java.util.List;

public class CodeFileUtils {
    /**
     *
     * @param startLineNumber   start line number
     * @param startColumnNumber start column number
     * @param endLineNumber     end line number
     * @param endColumnNumber   end column number
     * @param fileLines         file lines in Kotlin or java
     * @param fileContent       file content in Kotlin or java
     * @return the raw text of the node.
     */
    public static String getRawSignature(int startLineNumber, int startColumnNumber, int endLineNumber, int endColumnNumber,
            List<String> fileLines, String fileContent) {
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
