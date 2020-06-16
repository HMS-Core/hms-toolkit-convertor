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

package com.huawei.codebot.analyzer.x2y.xml;

import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that help us get information of XML label we need conveniently.
 *
 * @since 2020-04-22
 */
public class XmlEntitiesAnalyzer {
    private List<String> fileContent;
    private List<Integer> labelStartLines = new ArrayList<>();
    private List<Integer> labelStartLinesEndPosition = new ArrayList<>();
    private List<Integer> labelEndLines = new ArrayList<>();
    private Map<String, XmlEntity> labelContents = new HashMap<>();
    private Map<String, XmlEntity> startLabelContents = new HashMap<>();
    private String startLabelSymbol;
    private String endLabelSymbol;
    private List<String> annotations;
    private LabelType labelType;

    private Map<String, XmlEntity> componentLabelContents;

    public XmlEntitiesAnalyzer(
            List<String> fileContent,
            List<String> annotations,
            LabelType labelType,
            Map<String, XmlEntity> componentLabelContents) {
        this.fileContent = fileContent;
        this.annotations = annotations;
        this.labelType = labelType;
        this.startLabelSymbol = "<" + labelType.toString();
        this.endLabelSymbol = "</" + labelType.toString() + ">";
        this.componentLabelContents = componentLabelContents;
    }

    private void extractXmlLabelLineNumbers() {
        String symbol = "";
        boolean labelFlag = false;
        int num = -1;
        for (int i = 0; i < fileContent.size(); i++) {
            String fileContentLine = fileContent.get(i);
            if (checkFileContentLine(fileContentLine)) {
                labelStartLines.add(i);
                String partialContens = getLineContents(fileContent, i, fileContent.size() - 1);
                symbol = getServiceSymbolValue(partialContens);
                num++;
                labelFlag = true;
            }
            if (!labelFlag || i < labelStartLines.get(num)) {
                continue;
            }
            if ((symbol.equals("nonclosed") && fileContentLine.contains(endLabelSymbol))
                    || (symbol.equals("closure") && fileContentLine.contains("/>"))) {
                labelEndLines.add(i);
                labelFlag = false;
            }
        }
    }

    private void extractXmlStartLabelLineNumbers() {
        boolean labelFlag = false;
        int num = -1;
        for (int i = 0; i < fileContent.size(); i++) {
            String fileContentLine = fileContent.get(i);
            if (checkFileContentLine(fileContentLine)) {
                labelStartLines.add(i);
                num++;
                labelFlag = true;
            }
            if (labelFlag && (i >= labelStartLines.get(num)) && (fileContentLine.contains(">"))) {
                labelStartLinesEndPosition.add(i);
                labelFlag = false;
            }
        }
    }

    private boolean checkFileContentLine(String fileContentLine) {
        return fileContentLine.contains(startLabelSymbol)
                && (annotations == null || !annotations.toString().contains(fileContentLine));
    }

    private void extractXmlLabelContent() {
        for (int i = 0; i < labelEndLines.size(); i++) {
            int startLine = labelStartLines.get(i);
            int endLine = labelEndLines.get(i);
            StringBuilder content = new StringBuilder();
            for (int j = 0; j < fileContent.size(); j++) {
                if (j >= startLine && j <= endLine) {
                    content.append(fileContent.get(j));
                }
            }
            XmlEntity xmlEntity = new XmlEntity();
            xmlEntity.labelName = labelType.toString();
            xmlEntity.nameIdentifier = getAndroidName(content.toString());
            xmlEntity.labelStartLine = startLine + 1;
            xmlEntity.labelEndLine = endLine + 1;
            xmlEntity.labelContent = content.toString();
            String key = labelType.toString() + xmlEntity.nameIdentifier;
            if (isFilteredLabel(xmlEntity, componentLabelContents)) {
                continue;
            }
            labelContents.put(key, xmlEntity);
        }
    }

    private void extractXmlStartLabelContent() {
        for (int i = 0; i < labelStartLinesEndPosition.size(); i++) {
            int startLine = labelStartLines.get(i);
            int endLine = labelStartLinesEndPosition.get(i);
            StringBuilder content = new StringBuilder();
            for (int j = 0; j < fileContent.size(); j++) {
                if (j >= startLine && j <= endLine) {
                    content.append(fileContent.get(j));
                }
            }
            XmlEntity xmlEntity = new XmlEntity();
            xmlEntity.labelName = labelType.toString();
            xmlEntity.nameIdentifier = getAndroidName(content.toString());
            xmlEntity.labelStartLine = startLine + 1;
            xmlEntity.labelStartLinesEndPosition = endLine + 1;
            xmlEntity.labelContent = content.toString();
            String key = labelType.toString() + xmlEntity.nameIdentifier;
            startLabelContents.put(key, xmlEntity);
        }
    }

    public Map<String, XmlEntity> getLabelContents() {
        extractXmlLabelLineNumbers();
        extractXmlLabelContent();
        return labelContents;
    }

    Map<String, XmlEntity> getStartLabelContents() {
        extractXmlStartLabelLineNumbers();
        extractXmlStartLabelContent();
        return startLabelContents;
    }

    private static Boolean isFilteredLabel(XmlEntity xmlEntity, Map<String, XmlEntity> componentLabelContents) {
        if (componentLabelContents == null) {
            return false;
        }
        for (Entry<String, XmlEntity> componentLabelEntry : componentLabelContents.entrySet()) {
            XmlEntity labelXmlEntity = componentLabelEntry.getValue();
            if (labelXmlEntity.labelContent.contains(xmlEntity.labelName)
                    && labelXmlEntity.labelContent.contains(xmlEntity.nameIdentifier)
                    && labelXmlEntity.labelStartLine < xmlEntity.labelStartLine
                    && labelXmlEntity.labelEndLine > xmlEntity.labelEndLine) {
                return true;
            }
        }
        return false;
    }

    private static String getAndroidName(String label) {
        int nameIndex = label.indexOf("android:name=");
        if (nameIndex == -1) {
            return null;
        }
        String subStr = label.substring(nameIndex);
        List<Integer> lineNumbers = new ArrayList<>();
        for (int i = 0; i < subStr.length(); i++) {
            if (subStr.charAt(i) == '\"') {
                lineNumbers.add(i);
            }
        }
        return subStr.substring(lineNumbers.get(0) + 1, lineNumbers.get(1));
    }

    private static String getLineContents(List<String> fileContents, Integer startIndex, Integer endIndex) {
        List<String> partialContents = new ArrayList<>();
        for (int i = 0; i < fileContents.size(); i++) {
            if (i >= startIndex && i <= endIndex) {
                partialContents.add(fileContents.get(i));
            }
        }
        return StringUtils.join(partialContents, "");
    }

    private static String getServiceSymbolValue(String partialContents) {
        String symbol = null;
        int symbolPosition = partialContents.indexOf(">");
        int otherSymbolPosition = partialContents.indexOf("/>");
        if (symbolPosition != -1 && otherSymbolPosition != -1) {
            // If partialContents contains both ">" and "/>"
            if (symbolPosition < otherSymbolPosition) {
                // If ">" appears before "/>", it's nonclosed
                symbol = "nonclosed";
            } else {
                // If ">" don't appear before "/>", it means closure.
                symbol = "closure";
            }
        } else if (symbolPosition != -1) {
            // If partialContents just contains ">", it's also nonclosed.
            symbol = "nonclosed";
        }
        return symbol;
    }

    /**
     * Match XML file content by given regex and return a matched list of each file line.
     * <br/>
     * We use this method to find the specific line we need, like an XML annotation.
     *
     * @param fileContent A XML file content.
     * @param pattern     A pattern we want to match.
     * @return A list of string that matched the pattern.
     */
    public static List<String> getTargetBasedOnRegex(List<String> fileContent, String pattern) {
        String content = getLineContents(fileContent, 0, fileContent.size() - 1);
        List<String> targets = new ArrayList<>();
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(content);
        while (matcher.find()) {
            targets.add(matcher.group());
        }
        if (targets.size() == 0) {
            return null;
        } else {
            return targets;
        }
    }

    /**
     * Return the key of {@code xmlChangerJsonTargets} when we matched a {@code CommonOperation}
     * by given {@code nodeName} and {@code androidName}
     *
     * @param nodeName XML node name you are visiting, usually a labelName.
     * @param androidName XML label property.
     * @param xmlChangerJsonTargets A map that key is {@code CommonOperation}'s identifier
     *                              and value is {@code CommonOperation}.
     * @return The key of {@code nodeName} and {@code androidName} corresponding to.
     */
    public static String getJsonTargetKey(
            String nodeName, String androidName, Map<String, CommonOperation> xmlChangerJsonTargets) {
        for (Entry<String, CommonOperation> entry : xmlChangerJsonTargets.entrySet()) {
            String jsonKey = entry.getKey();
            CommonOperation commonOperation = entry.getValue();
            if (nodeName.equals(commonOperation.labelName)
                    && new InheritanceService()
                    .isSubClassConsiderIndirectInheritance(androidName, commonOperation.androidName)) {
                return jsonKey;
            }
            String newString = commonOperation.androidName.replace(".*", "");
            if (nodeName.equals(commonOperation.labelName) && androidName.startsWith(newString)) {
                return jsonKey;
            }
        }
        return null;
    }

    /**
     * Traverse {@code node} recursively and put all element into {@code nodeInfo}.
     *
     * @param node     XML node we want to traverse.
     * @param nodeInfo A list used to store all element we get by traversing the {@code node}.
     */
    static boolean traverseXmlNode(CodeNetElement node, List<XmlEntity> nodeInfo) {
        boolean effectFlag = false;
        if (node == null) {
            return effectFlag;
        }
        XmlEntity xmlEntity = new XmlEntity();
        xmlEntity.labelName = node.getName();
        xmlEntity.nameIdentifier = (node.attribute("name") != null) ? node.attribute("name").getValue() : null;
        xmlEntity.parentLabelName = (node.getParent() != null) ? node.getParent().getName() : null;
        nodeInfo.add(xmlEntity);
        if (xmlEntity.parentLabelName != null
                && xmlEntity.parentLabelName.equals("application")
                && xmlEntity.nameIdentifier != null) {
            effectFlag = true;
        }
        List<CodeNetElement> listElement = node.getElements();
        for (CodeNetElement e : listElement) {
            effectFlag = traverseXmlNode(e, nodeInfo) || effectFlag;
        }
        return effectFlag;
    }
}
