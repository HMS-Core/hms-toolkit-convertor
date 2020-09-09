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

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

/**
 * Changer for XML files. There are usually two type of modification --- CommonOperation and LayoutOperation.
 *
 * @since 2020-04-22
 */
public class XmlModificationChanger extends AndroidAppFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlModificationChanger.class);
    /**
     * AndroidManifest file path.
     */
    public static final String MANIFEST_PATH = "src" + File.separator + "main" + File.separator + "AndroidManifest.xml";

    /**
     * RemoteConfig resource node info
     */
    private static final String DEFAULTS_MAP_START_NODE = "<defaultsMap>";
    private static final String DEFAULTS_MAP_END_NODE = "</defaultsMap>";
    private static final String ENTRY_START_NODE = "<entry>";
    private static final String ENTRY_END_NODE = "</entry>";
    private static final String KEY_START_NODE = "<key>";
    private static final String KEY_END_NODE = "</key>";
    private static final String VALUE_START_NODE = "<value>";
    private static final String VALUE_END_NODE = "</value>";

    /**
     * General XML file path.
     */
    public static final String XML_PATH =
            "src" + File.separator + "main" + File.separator + "res" + File.separator + "layout";
    /**
     * Strings XML file path.
     */
    public static final String STRINGS_PATH = "src" + File.separator + "main" + File.separator + "res" + File.separator + "values" + File.separator + "strings.xml";
    /**
     * Pattern for CommonOperation.
     */
    protected Map<String, CommonOperation> xmlChangerJsonTargets = new HashMap<>();
    /**
     * A map that groups CommonOperation by label type.
     */
    protected Map<LabelType, Map<String, CommonOperation>> xmlChangerCategoryJsonTargets = new HashMap<>();
    /**
     * Pattern for LayoutOperation.
     */
    protected Map<String, LayoutOperation> layoutOperationJsonTargets = new HashMap<>();
    /**
     * Pattern for LayoutAtrributeOperation.
     */
    protected Map<String, List<LayoutAtrributeOperation>> layoutAtrributeOperationJsonTargets = new HashMap<>();
    /**
     * Pattern for LayoutAtrrValueOperation.
     */
    protected Map<String, List<LayoutAtrrValueOperation>> layoutAtrrValueOperationJsonTargets = new HashMap<>();
    /**
     * the desc of 'RemoteConfig' resource file conversion
     */
    protected String specialConversionResourceDesc;

    public XmlModificationChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        XmlJsonPattern xmlJsonPattern = configService.getXmlJsonPattern();
        initConfig(xmlJsonPattern);
    }

    protected XmlModificationChanger(XmlJsonPattern xmlJsonPattern) {
        initConfig(xmlJsonPattern);
    }

    /**
     * Init this changer.
     *
     * @param xmlJsonPattern A {@link XmlJsonPattern} instance.
     */
    public void initConfig(XmlJsonPattern xmlJsonPattern) {
        if (xmlJsonPattern != null) {
            xmlChangerJsonTargets = xmlJsonPattern.getXmlChangerJsonTargets();
            xmlChangerCategoryJsonTargets = xmlJsonPattern.getXmlChangerCategoryJsonTargets();
            layoutOperationJsonTargets = xmlJsonPattern.getLayoutOperationJsonTargets();
            layoutAtrributeOperationJsonTargets = xmlJsonPattern.getLayoutAtrributeOperationJsonTargets();
            layoutAtrrValueOperationJsonTargets = xmlJsonPattern.getLayoutAtrrValueOperationJsonTargets();
            specialConversionResourceDesc = xmlJsonPattern.getSpecialConversionResourceDesc();
        }
        this.basicFormatAfterFix = true;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        if (buggyFilePath.endsWith(MANIFEST_PATH)) {
            try {
                List<String> fileContent =
                        FileUtils.getOriginalFileLines(buggyFilePath, FileUtils.detectCharset(buggyFilePath));
                // XML annotations.
                List<String> annotations = XmlEntitiesAnalyzer.getXmlCommentLines(fileContent);

                // Get the root node of the XML tree.
                Locator locator = new LocatorImpl();
                DocumentFactory docFactory = new CodeNetDocumentLocator(locator);
                SAXReader reader = new CodeNetSaxReader(docFactory, locator);
                Document document = reader.read(new File(buggyFilePath));
                Element root = document.getRootElement();

                // Determine whether it is a Manifest XML file that needs to be processed
                List<XmlEntity> nodeInfo = new ArrayList<>();
                boolean effectFlag = XmlEntitiesAnalyzer.traverseXmlNode((CodeNetElement) root, nodeInfo);
                if (!effectFlag) {
                    return null;
                }

                // Get the original content of each label based on text analysis
                List<LabelType> analyzedBranchLabels = LabelType.getBranchLabels();
                List<LabelType> analyzedLeafLabels = LabelType.getLeafLabels();
                analyzedBranchLabels.addAll(analyzedLeafLabels);
                XmlEntitiesAnalyzer xmlEntitiesAnalyzer = new XmlEntitiesAnalyzer(fileContent, annotations);
                Map<String, XmlEntity> allLabelContents = xmlEntitiesAnalyzer.getLabelContents(root);

                // group by each label
                Map<LabelType, Map<String, XmlEntity>> labelTypeContents = new HashMap<>();
                for (LabelType labelType : analyzedBranchLabels) {
                    Map<String, XmlEntity> currentLabelMap = new HashMap<>();
                    for (Map.Entry<String, XmlEntity> entry : allLabelContents.entrySet()) {
                        if (entry.getKey().startsWith(labelType.toString())) {
                            currentLabelMap.put(entry.getKey(), entry.getValue());
                        }
                    }
                    labelTypeContents.put(labelType, currentLabelMap);
                }
                // handle manifest and application label
                List<LabelType> specificLabels = LabelType.getPositionLabels();
                Map<LabelType, Map<String, XmlEntity>> specificTypeLabelPositionInfo = new HashMap<>();
                for (LabelType labelType : specificLabels) {
                    Map<String, XmlEntity> labelStartContent = new HashMap<>();
                    for (Map.Entry<String, XmlEntity> entry : allLabelContents.entrySet()) {
                        if (entry.getKey().contains(labelType.toString())) {
                            labelStartContent.put(entry.getKey(), entry.getValue());
                        }
                    }
                    specificTypeLabelPositionInfo.put(labelType, labelStartContent);
                }
                // Traverse XML tree, do delete and modify operations
                operationInXmlNodes((CodeNetElement) root, buggyFilePath, allLabelContents, defectInstances);
                // Do add operation at specific locations
                addOperation(buggyFilePath, labelTypeContents, specificTypeLabelPositionInfo, defectInstances);

                return defectInstances;
            } catch (IOException | DocumentException e) {
                return null;
            } catch (Exception e) {
                LOGGER.error("XML File should be format!", e);
                return null;
            }
        } else {
            boolean otherXmlFlag = true;
            if (otherXmlFlag && (buggyFilePath.contains(XML_PATH)) || buggyFilePath.contains(STRINGS_PATH)) {
                generateGenericXmlDefectInstance(buggyFilePath, defectInstances);
                return defectInstances;
            } else {
                // Only for G2H
                if (StringUtils.isNotEmpty(specialConversionResourceDesc)) {
                    List<DefectInstance> agcDefectInstances = generateAgcDefectInstance(buggyFilePath);
                    if (CollectionUtils.isNotEmpty(agcDefectInstances)) {
                        return agcDefectInstances;
                    }
                }
                return null;
            }
        }
    }

    /**
     * Special handling of AGC resource files
     *
     * @param buggyFilePath buggy file path
     * @return defectInstance list
     */
    private List<DefectInstance> generateAgcDefectInstance(String buggyFilePath) {
        if (StringUtils.isEmpty(buggyFilePath)) {
            return null;
        }
        Stack<String> fileStack = new Stack<>();
        Stack<String> entry = new Stack<>();
        List<String> resultFileContent = new ArrayList<>();
        List<DefectInstance> result = new ArrayList<>();
        int startLine = 0;
        try {
            String fileContent = FileUtils.getFileContent(buggyFilePath);
            List<String> fileContents = FileUtils.getOriginalFileLines(buggyFilePath,
                    FileUtils.detectCharset(buggyFilePath));

            for (int i = 0; i < fileContents.size(); i++) {
                if (fileContents.get(i).contains(DEFAULTS_MAP_START_NODE)) {
                    // Add start defectInstance
                    resultFileContent
                            .add(reformatFixedString(fileContents.get(i).replace(DEFAULTS_MAP_START_NODE, "<resources>")));
                    startLine = i;
                }
                if (fileContents.get(i).contains(DEFAULTS_MAP_END_NODE)) {
                    // Completed the end line of the deleted defectInstance
                    StringBuffer resultBuffer = new StringBuffer();
                    StringBuffer fileBuffer = new StringBuffer();
                    String lineBreak = StringUtil.getLineBreak(fileContent);

                    for (int j = startLine; j < i; j++) {
                        fileBuffer.append(fileContents.get(j));
                    }
                    fileBuffer.append(reformatFixedString(fileContents.get(i)));

                    for (String row : resultFileContent) {
                        resultBuffer.append(row).append(lineBreak);
                    }
                    resultBuffer.append(reformatFixedString(fileContents.get(i).replace(DEFAULTS_MAP_END_NODE, "</resources>")));
                    // Replace the resource
                    DefectInstance changeDefectInstance = createDefectInstance(buggyFilePath, startLine + 1,
                            fileBuffer.toString(), resultBuffer.toString());
                    changeDefectInstance.setMessage(specialConversionResourceDesc);
                    result.add(changeDefectInstance);
                    return result;
                }
                if (fileContents.get(i).contains(ENTRY_END_NODE)) {
                    entry.clear();
                    String nextRow;
                    do {
                        nextRow = fileStack.pop();
                        entry.push(nextRow);
                    } while (!fileStack.isEmpty() && !nextRow.contains(ENTRY_START_NODE));
                    if (CollectionUtils.isEmpty(entry)) {
                        continue;
                    }
                    resultFileContent.add(generateEntry(entry));
                    continue;
                }
                fileStack.push(fileContents.get(i));
            }
        } catch (IOException e) {
            logger.error(Throwables.getStackTraceAsString(e));
        }
        return result;
    }

    private String generateEntry(Stack<String> entry) {
        List<String> keyNode = new ArrayList<>();
        List<String> valueNode = new ArrayList<>();
        String key = "";
        String value = "";
        do {
            String nextRow = entry.pop();
            if (nextRow.contains(KEY_START_NODE)) {
                while (!entry.isEmpty() && !nextRow.contains(KEY_END_NODE)) {
                    keyNode.add(nextRow);
                    nextRow = entry.pop();
                }
                keyNode.add(nextRow);
                key = getStringFromXml(keyNode, KEY_START_NODE, KEY_END_NODE);
            } else if (nextRow.contains(VALUE_START_NODE)) {
                while (!entry.isEmpty() && !nextRow.contains(VALUE_END_NODE)) {
                    valueNode.add(nextRow);
                    nextRow = entry.pop();
                }
                valueNode.add(nextRow);
                value = getStringFromXml(valueNode, VALUE_START_NODE, VALUE_END_NODE);
            }

        } while (!entry.isEmpty());
        return "    <value key=\"" + key + "\">" + value + "</value>";
    }

    private String getStringFromXml(List<String> keyNode, String startNode, String endNode) {
        if (keyNode.size() == 1) {
            String key = keyNode.get(0);
            int start = key.indexOf(startNode);
            int end = key.indexOf(endNode);
            return key.substring(start + startNode.length(), end).trim();
        } else {
            for (int i = 0; i < keyNode.size(); i++) {
                String key = keyNode.get(i);
                if (StringUtils.isNotEmpty(key) && !key.contains(startNode) && !key.contains(endNode)) {
                    return key.trim();
                }
            }
        }
        return "";
    }

    private void generateGenericXmlDefectInstance(String buggyFilePath, List<DefectInstance> defectInstances) {
        String codeContent = null;
        try {
            codeContent = FileUtils.getFileContent(buggyFilePath);
        } catch (IOException e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
        }
        if (codeContent != null) {
            List<String> lines = FileUtils.cutStringToList(codeContent);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                for (Entry<String, LayoutOperation> entry : layoutOperationJsonTargets.entrySet()) {
                    String oldClassName = entry.getKey();
                    String newClassName = entry.getValue().newClassName;
                    if (line != null && !line.trim().startsWith("<!--") && !line.trim().endsWith("-->")) {
                        if (line.contains(oldClassName)) {
                            String fixedLine = line.replace(oldClassName, newClassName);
                            int startLineNumber = i + 1;
                            DefectInstance defectInstance = createDefectInstance(buggyFilePath, startLineNumber, line,
                                    fixedLine);
                            defectInstance.setMessage(entry.getValue().desc);
                            defectInstance.isFixed = true;
                            defectInstances.add(defectInstance);
                            // Replace signinbutton and signinbutton tag properties according to rules
                            if (layoutAtrributeOperationJsonTargets.get(oldClassName) != null) {
                                generateGenericXmlDefectInstanceBySignInButton(buggyFilePath, defectInstances,
                                        oldClassName, i, lines);
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateGenericXmlDefectInstanceBySignInButton(String buggyFilePath,
            List<DefectInstance> defectInstances, String oldClassName, int lineNumber, List<String> lines) {
        List<LayoutAtrributeOperation> layoutAtrributeOperationList = layoutAtrributeOperationJsonTargets
                .get(oldClassName);
        List<LayoutAtrrValueOperation> layoutAtrrValueOperationList = layoutAtrrValueOperationJsonTargets
                .get(oldClassName);
        for (int i = lineNumber; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().startsWith("<!--") || line.trim().endsWith("-->")) {
                continue;
            }
            outterLoop: for (LayoutAtrributeOperation layoutAtrribute : layoutAtrributeOperationList) {
                for (LayoutAtrrValueOperation layoutAtrrValue : layoutAtrrValueOperationList) {
                    if (!line.contains(layoutAtrribute.oldAtrributeName)
                            || !line.contains(layoutAtrrValue.oldAtrrValue)) {
                        continue;
                    }
                    String atrributeNameFixedLine = line.replace(layoutAtrribute.oldAtrributeName,
                            layoutAtrribute.newAtrributeName);
                    String atrrValueFixedLine = atrributeNameFixedLine.replace(layoutAtrrValue.oldAtrrValue,
                            layoutAtrrValue.newAtrrValue);
                    int startLineNumber = i + 1;
                    DefectInstance atrributeNameDefectInstance = createDefectInstance(buggyFilePath, startLineNumber,
                            line, atrributeNameFixedLine);
                    atrributeNameDefectInstance.setMessage(layoutAtrribute.desc);
                    atrributeNameDefectInstance.isFixed = true;
                    defectInstances.add(atrributeNameDefectInstance);
                    DefectInstance atrrValueDefectInstance = createDefectInstance(buggyFilePath, startLineNumber, line,
                            atrrValueFixedLine);
                    atrrValueDefectInstance.setMessage(layoutAtrrValue.desc);
                    atrrValueDefectInstance.isFixed = true;
                    defectInstances.add(atrrValueDefectInstance);
                    break outterLoop;
                }
            }
            if (line.trim().endsWith(">")) {
                break;
            }
        }
    }

    /**
     * Traverse Xml nodes, and handle delete and replace operations.
     *
     * @param node XML node you want to traverse.
     * @param buggyFilePath The XML file path you want to process.
     * @param labelContents This node's raw string content.
     * @param defectInstances A container to store DefectInstance produced by this node.
     */
    protected void operationInXmlNodes(
            CodeNetElement node,
            String buggyFilePath,
            Map<String, XmlEntity> labelContents,
            List<DefectInstance> defectInstances) {
        if (node == null) {
            return;
        }

        if (node.getParent() != null) {
            String nodeName = node.getName();
            if (node.attribute("name") != null) {
                String androidName = node.attribute("name").getValue();
                if (node.getParent().getName().equals(LabelType.MANIFEST.toString())) {
                    if (nodeName.equals(LabelType.USES_PERMISSION.toString())
                            || nodeName.equals(LabelType.PERMISSION.toString())) {
                        deleteOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                        replaceOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                    }
                } else if (node.getParent().getName().equals(LabelType.APPLICATION.toString())) {
                    if (nodeName.equals(LabelType.METADATA.toString())
                            || nodeName.equals(LabelType.SERVICE.toString())
                            || nodeName.equals(LabelType.ACTIVITY.toString())
                            || nodeName.equals(LabelType.PROVIDER.toString())
                            || nodeName.equals(LabelType.RECEIVER.toString())) {
                        deleteOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                        replaceOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                        insertOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                    }
                }
            } else if ("application".equals(nodeName)) {
                if (!GlobalSettings.isHasApplication() && !GlobalSettings.isIsSDK()) {
                    File xmlFile = new File(buggyFilePath);
                    if (GlobalSettings.getMainModuleName().equals(xmlFile.getParentFile()
                            .getParentFile().getParentFile().getName())) {
                        XmlEntity xmlEntity = labelContents.get("applicationnull");
                        DefectInstance defectInstance =
                                createDefectInstance(
                                        buggyFilePath,
                                        -(xmlEntity.getLabelStartLine() + 1),
                                        null,
                                        "android:name=\".MyApp\"");

                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        defectInstance.setMessage(gson.toJson(getMessage()));
                        defectInstance.isFixed = true;
                        defectInstances.add(defectInstance);
                    }
                }
            }
        }
        List<CodeNetElement> listElement = node.getElements();
        for (CodeNetElement e : listElement) {
            operationInXmlNodes(e, buggyFilePath, labelContents, defectInstances);
        }
    }

    private Map<String, Object> getMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("fieldName", "");
        message.put("hmsVersion", "");
        message.put("dependencyName", "Common");
        message.put("kit", "Common");
        message.put("text", "Generate new class MyApp inheriting android.app.Application");
        message.put("support", true);
        message.put("url", "");
        message.put("type", "");
        message.put("gmsVersion", "");
        message.put("status", "AUTO");
        message.put("extraPath", String.join("#",
                Collections.singleton(GlobalSettings.getAppFilePath())));
        return message;
    }

    /**
     * Remove {@code content}'s line separator at tail.
     *
     * @param content Unformatted string.
     * @return Formatted string.
     */
    public static String reformatFixedString(String content) {
        if (StringUtils.isNotEmpty(content) && content.length() > 1 && content.endsWith("\n")) {
            return content.substring(0, content.length() - 1);
        } else {
            return content;
        }
    }

    private void deleteOperation(
            String buggyFilePath,
            String nodeName,
            String androidName,
            Map<String, XmlEntity> labelContents,
            List<DefectInstance> defectInstances) {
        String key = nodeName + androidName;
        CommonOperation commonOperation = xmlChangerJsonTargets.get(key);
        XmlEntity xmlEntity = labelContents.get(key);
        if (commonOperation != null && xmlEntity != null && "delete".equals(commonOperation.operation)) {
            DefectInstance defectInstance =
                    createWarningDefectInstance(
                            buggyFilePath,
                            xmlEntity.getLabelStartLine(),
                            reformatFixedString(xmlEntity.getLabelContent()),
                            commonOperation.desc);
            defectInstances.add(defectInstance);
        } else if (xmlEntity != null
                && XmlEntitiesAnalyzer.getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets) != null) {
            String jsonKey = XmlEntitiesAnalyzer.getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets);
            CommonOperation newCommonOperation = xmlChangerJsonTargets.get(jsonKey);
            if (newCommonOperation != null && "delete".equals(newCommonOperation.operation)) {
                DefectInstance defectInstance =
                        createWarningDefectInstance(
                                buggyFilePath,
                                xmlEntity.getLabelStartLine(),
                                reformatFixedString(xmlEntity.getLabelContent()),
                                newCommonOperation.desc);
                defectInstances.add(defectInstance);
            }
        }
    }

    private void replaceOperation(
            String buggyFilePath,
            String nodeName,
            String androidName,
            Map<String, XmlEntity> labelContents,
            List<DefectInstance> defectInstances) {
        String key = nodeName + androidName;
        CommonOperation commonOperation = xmlChangerJsonTargets.get(key);
        XmlEntity xmlEntity = labelContents.get(key);
        if (commonOperation != null && xmlEntity != null && "replace".equals(commonOperation.operation)) {
            DefectInstance defectInstance;
            if (nodeName.equals(LabelType.METADATA.toString())) {
                defectInstance =
                        createDefectInstance(
                                buggyFilePath,
                                xmlEntity.getLabelStartLine(),
                                reformatFixedString(xmlEntity.getLabelContent()),
                                reformatFixedString(xmlEntity.getLabelContent())
                                        .replace(androidName, commonOperation.newContent));
            } else {
                defectInstance =
                        createDefectInstance(
                                buggyFilePath,
                                xmlEntity.getLabelStartLine(),
                                reformatFixedString(xmlEntity.getLabelContent()),
                                reformatFixedString(commonOperation.newContent));
            }
            defectInstance.setMessage(commonOperation.desc);
            defectInstance.isFixed = true;
            defectInstances.add(defectInstance);
        } else if (xmlEntity != null
                && XmlEntitiesAnalyzer.getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets) != null) {
            String jsonKey = XmlEntitiesAnalyzer.getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets);
            CommonOperation newCommonOperation = xmlChangerJsonTargets.get(jsonKey);
            if (newCommonOperation != null && "replace".equals(newCommonOperation.operation)) {
                String newContent;
                if (newCommonOperation.newContent.contains("%subclass")) {
                    newContent = newCommonOperation.newContent.replaceFirst("%subclass", androidName);
                } else {
                    newContent = newCommonOperation.newContent;
                }
                DefectInstance defectInstance =
                        createDefectInstance(
                                buggyFilePath,
                                xmlEntity.getLabelStartLine(),
                                reformatFixedString(xmlEntity.getLabelContent()),
                                newContent);
                defectInstance.setMessage(newCommonOperation.desc);
                defectInstance.isFixed = true;
                defectInstances.add(defectInstance);
            }
        }
    }

    private void insertOperation(
            String buggyFilePath,
            String nodeName,
            String androidName,
            Map<String, XmlEntity> labelContents,
            List<DefectInstance> defectInstances) {
        String key = nodeName + androidName;
        CommonOperation commonOperation = xmlChangerJsonTargets.get(key);
        XmlEntity xmlEntity = labelContents.get(key);
        if (commonOperation != null && xmlEntity != null && "insert".equals(commonOperation.operation)) {
            DefectInstance defectInstance =
                    createDefectInstance(
                            buggyFilePath,
                            -(xmlEntity.getLabelStartLine()),
                            null,
                            reformatFixedString(xmlEntity.getLabelContent())
                                    .replace(androidName, commonOperation.newContent));
            defectInstance.setMessage(commonOperation.desc);
            defectInstance.isFixed = true;
            defectInstances.add(defectInstance);
        }
    }

    private void addOperation(
            String buggyFilePath,
            Map<LabelType, Map<String, XmlEntity>> labelContents,
            Map<LabelType, Map<String, XmlEntity>> specificLabelPositionInfo,
            List<DefectInstance> defectInstances) {
        List<LabelType> processLabels = LabelType.getProcessLabels();
        for (LabelType labelType : processLabels) {
            Map<String, CommonOperation> jsonTargets = xmlChangerCategoryJsonTargets.get(labelType);
            Map<String, XmlEntity> labelNodes = labelContents.get(labelType);
            if (jsonTargets == null) {
                continue;
            }
            StringBuilder addContents = new StringBuilder();
            StringBuilder addHints = new StringBuilder();
            extractAddingContents(jsonTargets, labelNodes, addContents, addHints);
            if (addContents.length() > 0) {
                if (labelType.equals(LabelType.USES_PERMISSION) || labelType.equals(LabelType.PERMISSION)) {
                    // Add after "manifest" label.
                    XmlEntity headXmlEntity =
                            XmlModificationChanger.getHead(
                                    specificLabelPositionInfo.get(LabelType.MANIFEST))
                                    .getValue();
                    DefectInstance defectInstance =
                            createDefectInstance(
                                    buggyFilePath,
                                    -(headXmlEntity.getLabelStartLinesEndPosition() + 1),
                                    null,
                                    addContents.toString());
                    defectInstance.setMessage(addHints.toString());
                    defectInstance.isFixed = true;
                    defectInstances.add(defectInstance);

                } else if (labelType.equals(LabelType.METADATA)
                        || labelType.equals(LabelType.SERVICE)
                        || labelType.equals(LabelType.ACTIVITY)
                        || labelType.equals(LabelType.PROVIDER)
                        || labelType.equals(LabelType.RECEIVER)) {
                    // Add after "application" label.
                    XmlEntity headXmlEntity =
                            XmlModificationChanger.getHead(
                                    specificLabelPositionInfo.get(LabelType.APPLICATION))
                                    .getValue();
                    DefectInstance defectInstance =
                            createDefectInstance(
                                    buggyFilePath,
                                    -(headXmlEntity.getLabelStartLinesEndPosition() + 1),
                                    null,
                                    addContents.toString());
                    defectInstance.setMessage(addHints.toString());
                    defectInstance.isFixed = true;
                    defectInstances.add(defectInstance);
                }
            }
        }
    }

    private void extractAddingContents(
            Map<String, CommonOperation> jsonTargets,
            Map<String, XmlEntity> labelNodes,
            StringBuilder addContents,
            StringBuilder addHints) {
        for (Entry<String, CommonOperation> entry : jsonTargets.entrySet()) {
            String key = entry.getKey();
            String operation = entry.getValue().operation;
            if ("add".equals(operation) && labelNodes.get(key) == null) {
                addContents.append(entry.getValue().newContent).append(System.lineSeparator());
                addHints.append(entry.getValue().desc).append(",");
            }
        }
        if (addContents != null && addContents.length() > 1) {
            addContents.deleteCharAt(addContents.length() - 1);
        }
        if (addHints != null && addHints.length() > 1) {
            addHints.deleteCharAt(addHints.length() - 1);
        }
    }

    private static <K, V> Entry<K, V> getHead(Map<K, V> map) {
        return map.entrySet().iterator().next();
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    protected void mergeDuplicateFixedLines(List<DefectInstance> defectInstances) {
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_XMLMODIFICATION;
            info.description = "Google GMS AndroidManifest needs to be rewritten corresponding name in Huawei HMS";
            this.info = info;
        }
        return this.info;
    }
}
