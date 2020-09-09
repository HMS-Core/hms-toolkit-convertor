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
import org.dom4j.Element;
import org.dom4j.Node;

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
    private List<Integer> labelEndLines = new ArrayList<>();
    private Map<String, XmlEntity> labelContents = new HashMap<>();
    private List<String> annotations;
    private List<String> labelList = new ArrayList<>();

    public XmlEntitiesAnalyzer(
            List<String> fileContent,
            List<String> annotations) {
        this.fileContent = fileContent;
        this.annotations = annotations;
    }

    public void parseXMLEntities(Element root) {
        List<Node> allNodes = root.content();
        Node prevNode;
        Node nextNode;
        for (int i = 0; i < allNodes.size(); i++) {
            Node node = allNodes.get(i);
            int nextIndex = i + 1;
            if (node instanceof CodeNetText && (nextIndex <= allNodes.size() - 1)) {
                nextNode = allNodes.get(nextIndex);
                if (nextNode instanceof CodeNetElement) {
                    labelStartLines.add(((CodeNetText) node).getLineNumber());
                }
            } else if (node instanceof CodeNetElement) {
                if (!((CodeNetElement) node).content().isEmpty()) {
                    if ("application".equals(node.getName())) {
                        if (allNodes.get(nextIndex) instanceof CodeNetText) {
                            labelEndLines.add(((CodeNetElement) node).getLineNumber());
                            labelList.add(node.getName());
                        }
                        // recursive call parseXMLEntities
                        parseXMLEntities((CodeNetElement) node);
                        continue;
                    }
                    List<Node> innerNodes = ((CodeNetElement)node).content();
                    Node n = innerNodes.get(innerNodes.size() - 1);
                    if (n instanceof CodeNetElement) {
                        labelEndLines.add(((CodeNetElement) n).getLineNumber());
                        labelList.add(node.getName());
                    } else if (n instanceof CodeNetText) {
                        labelEndLines.add(((CodeNetText) n).getLineNumber());
                        labelList.add(node.getName());
                    } else if (n instanceof  CodeNetComment) {
                        labelEndLines.add(((CodeNetComment) n).getLineNumber());
                        labelList.add(node.getName());
                    }
                } else {
                    prevNode = allNodes.get(i - 1);
                    nextNode = allNodes.get(nextIndex);
                    if (prevNode instanceof CodeNetText && nextNode instanceof CodeNetText) {
                        // CodeNetElement between two CodeNetTexts
                        labelEndLines.add(((CodeNetElement) node).getLineNumber());
                        labelList.add(node.getName());
                    }
                }
            }
            // other conditions no need to handle
        }
    }

    private void getManifestLabelInfo(Element root) {
        if (root instanceof CodeNetElement) {
            labelEndLines.add(((CodeNetElement) root).getLineNumber());
            labelList.add(root.getName());
            String target = "<" + root.getName();
            for (int i = 0; i < fileContent.size(); i++) {
                if ((fileContent.get(i).contains(target) && (annotations == null))
                        || (fileContent.get(i).contains(target)
                        && (annotations != null)
                        && !annotations.toString().contains(fileContent.get(i)))) {
                    labelStartLines.add(i + 1);
                    break;
                }
            }
        }
    }

    private void extractXMLLabelContent() {
        for (int i = 0; i < labelEndLines.size(); i++) {
            int startLine = labelStartLines.get(i);
            int endLine = labelEndLines.get(i);
            StringBuilder content = new StringBuilder();
            for (int j = startLine - 1; j <= endLine - 1; j++) {
                content.append(fileContent.get(j));
            }
            XmlEntity xmlEntity = new XmlEntity();
            xmlEntity.setLabelName(labelList.get(i));
            xmlEntity.setNameIdentifier(getAndroidName(content.toString()));
            xmlEntity.setLabelStartLine(startLine);
            xmlEntity.setLabelEndLine(endLine);
            xmlEntity.setLabelContent(content.toString());
            String key = labelList.get(i) + xmlEntity.getNameIdentifier();
            if (!labelContents.containsKey(key)) {
                labelContents.put(key, xmlEntity);
            }
        }
    }

    public Map<String, XmlEntity> getLabelContents(Element root) {
        getManifestLabelInfo(root);
        parseXMLEntities(root);
        extractXMLLabelContent();
        return labelContents;
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
        return subStr.substring((lineNumbers.get(0) + 1), lineNumbers.get(1));
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

    /**
     * Match XML file content by given regex and return a matched list of each file line.
     * <br/>
     * We use this method to find the specific line we need, like an XML annotation.
     *
     * @param fileContent A XML file content.
     * @return A list of string that matched the regex.
     */
    public static List<String> getXmlCommentLines(List<String> fileContent) {
        String content = getLineContents(fileContent, 0, fileContent.size() - 1);
        List<String> targets = new ArrayList<>();
        Pattern pattern = Pattern.compile("^<!--[\\s\\S\\n]*?-->$");
        Matcher matcher = pattern.matcher(content);
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
     * @param node XML node we want to traverse.
     * @param nodeInfo A list used to store all element we get by traversing the {@code node}.
     */
    public static boolean traverseXmlNode(CodeNetElement node, List<XmlEntity> nodeInfo) {
        boolean effectFlag = false;
        if (node == null) {
            return effectFlag;
        }
        XmlEntity xmlEntity = new XmlEntity();
        xmlEntity.setLabelName(node.getName());
        xmlEntity.setNameIdentifier(node.attribute("name") != null ? node.attribute("name").getValue() : "");
        xmlEntity.setParentLabelName(node.getParent() != null ? node.getParent().getName() : "");
        nodeInfo.add(xmlEntity);
        if (StringUtils.isNotEmpty(xmlEntity.getParentLabelName())
                && "application".equals(xmlEntity.getParentLabelName())
                && StringUtils.isNotEmpty(xmlEntity.getNameIdentifier())) {
            effectFlag = true;
        }
        List<CodeNetElement> listElement = node.getElements();
        for (CodeNetElement e : listElement) {
            effectFlag = traverseXmlNode(e, nodeInfo) || effectFlag;
        }
        return effectFlag;
    }
}
