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

import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;

import com.google.common.base.Throwables;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


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
     * General XML file path.
     */
    public static final String XML_PATH =
            "src" + File.separator + "main" + File.separator + "res" + File.separator + "layout";
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
                List<String> annotations = XmlEntitiesAnalyzer.getTargetBasedOnRegex(fileContent, "<!--(\\s|.)*?-->");

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
                Map<String, XmlEntity> allLabelContents = new HashMap<>();
                Map<LabelType, Map<String, XmlEntity>> labelTypeContents = new HashMap<>();
                for (LabelType labelType : analyzedBranchLabels) {
                    XmlEntitiesAnalyzer xmlEntitiesAnalyzer =
                            new XmlEntitiesAnalyzer(fileContent, annotations, labelType, null);
                    Map<String, XmlEntity> labelContent = xmlEntitiesAnalyzer.getLabelContents();
                    labelTypeContents.put(labelType, labelContent);
                    allLabelContents.putAll(labelContent);
                }

                List<LabelType> analyzedLeafLabels = LabelType.getLeafLabels();
                for (LabelType labelType : analyzedLeafLabels) {
                    XmlEntitiesAnalyzer xmlEntitiesAnalyzer =
                            new XmlEntitiesAnalyzer(fileContent, annotations, labelType, allLabelContents);
                    Map<String, XmlEntity> labelContent = xmlEntitiesAnalyzer.getLabelContents();
                    labelTypeContents.put(labelType, labelContent);
                    allLabelContents.putAll(labelContent);
                }

                List<LabelType> specificLabels = LabelType.getPositionLabels();

                Map<LabelType, Map<String, XmlEntity>> specificTypeLabelPositionInfo = new HashMap<>();
                for (LabelType labelType : specificLabels) {
                    XmlEntitiesAnalyzer xmlEntitiesAnalyzer =
                            new XmlEntitiesAnalyzer(fileContent, annotations, labelType, null);
                    Map<String, XmlEntity> labelStartContent = xmlEntitiesAnalyzer.getStartLabelContents();
                    specificTypeLabelPositionInfo.put(labelType, labelStartContent);
                }
                // Traverse XML tree, do delete and modify operations
                operationInXmlNodes((CodeNetElement) root, buggyFilePath, allLabelContents, defectInstances);
                // Do add operation at specific locations
                addOperation(buggyFilePath, labelTypeContents, specificTypeLabelPositionInfo, defectInstances);

                return defectInstances;
            } catch (IOException | DocumentException e) {
                return null;
            }
        }
        if (buggyFilePath.contains(XML_PATH)) {
            generateGenericXmlDefectInstance(buggyFilePath, defectInstances);
            return defectInstances;
        }
        return null;
    }

    private void generateGenericXmlDefectInstance(String buggyFilePath, List<DefectInstance> defectInstances) {
        String codeContent = null;
        try {
            codeContent = FileUtils.getFileContent(buggyFilePath);
        } catch (IOException e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
        }
        if (codeContent == null) {
            return;
        }
        List<String> lines = FileUtils.cutStringToList(codeContent);
        for (Entry<String, LayoutOperation> entry : layoutOperationJsonTargets.entrySet()) {
            String oldClassName = entry.getKey();
            String newClassName = entry.getValue().newClassName;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.trim().startsWith("<!--") || line.trim().endsWith("-->")) {
                    continue;
                }
                if (line.contains(oldClassName)) {
                    String fixedLine = line.replace(oldClassName, newClassName);
                    int startLineNumber = i + 1;
                    DefectInstance defectInstance = createDefectInstance(buggyFilePath, startLineNumber, line,
                            fixedLine);
                    defectInstance.setMessage(entry.getValue().desc);
                    defectInstance.isFixed = true;
                    defectInstances.add(defectInstance);
                }
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
        if (node.getParent() != null && node.attribute("name") != null) {
            String nodeName = node.getName();
            String androidName = node.attribute("name").getValue();
            if (node.getParent().getName().equals(LabelType.MANIFEST.toString())) {
                if (nodeName.equals(LabelType.USES_PERMISSION.toString())
                        || nodeName.equals(LabelType.PERMISSION.toString())) {
                    deleteOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                    replaceOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                }
            } else if (node.getParent().getName().equals(LabelType.APPLICATION.toString())) {
                if (nodeName.equals(LabelType.METADATA.toString()) || nodeName.equals(LabelType.SERVICE.toString())
                        || nodeName.equals(LabelType.ACTIVITY.toString())
                        || nodeName.equals(LabelType.PROVIDER.toString())
                        || nodeName.equals(LabelType.RECEIVER.toString())) {
                    deleteOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                    replaceOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                    insertOperation(buggyFilePath, nodeName, androidName, labelContents, defectInstances);
                }
            }
        }
        List<CodeNetElement> listElement = node.getElements();
        for (CodeNetElement e : listElement) {
            operationInXmlNodes(e, buggyFilePath, labelContents, defectInstances);
        }
    }

    /**
     * Remove {@code content}'s line separator at tail.
     *
     * @param content Unformatted string.
     * @return Formatted string.
     */
    public static String reformatFixedString(String content) {
        if (content.length() > 1 && content.endsWith("\n")) {
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
        if (commonOperation != null && xmlEntity != null && commonOperation.operation.equals("delete")) {
            DefectInstance defectInstance =
                    createWarningDefectInstance(
                            buggyFilePath,
                            xmlEntity.labelStartLine,
                            reformatFixedString(xmlEntity.labelContent),
                            commonOperation.desc);
            defectInstances.add(defectInstance);
        } else if (xmlEntity != null
                && XmlEntitiesAnalyzer.getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets) != null) {
            String jsonKey = XmlEntitiesAnalyzer.getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets);
            CommonOperation newCommonOperation = xmlChangerJsonTargets.get(jsonKey);
            if (newCommonOperation != null && newCommonOperation.operation.equals("delete")) {
                DefectInstance defectInstance =
                        createWarningDefectInstance(
                                buggyFilePath,
                                xmlEntity.labelStartLine,
                                reformatFixedString(xmlEntity.labelContent),
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
        String jsonKey = XmlEntitiesAnalyzer.getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets);
        if (xmlEntity == null) {
            return;
        }
        if (commonOperation != null && commonOperation.operation.equals("replace")) {
            DefectInstance defectInstance;
            String fixedLineContent;
            if (nodeName.equals(LabelType.METADATA.toString())) {
                fixedLineContent = reformatFixedString(xmlEntity.labelContent).replace(androidName,
                        commonOperation.newContent);
            } else {
                fixedLineContent = reformatFixedString(commonOperation.newContent);
            }
            defectInstance =
                    createDefectInstance(
                            buggyFilePath,
                            xmlEntity.labelStartLine,
                            reformatFixedString(xmlEntity.labelContent),
                            fixedLineContent);
            defectInstance.setMessage(commonOperation.desc);
            defectInstance.isFixed = true;
            defectInstances.add(defectInstance);
        } else if (jsonKey != null) {
            CommonOperation newCommonOperation = xmlChangerJsonTargets.get(jsonKey);
            if (newCommonOperation != null && newCommonOperation.operation.equals("replace")) {
                String newContent;
                if (newCommonOperation.newContent.contains("%subclass")) {
                    newContent = newCommonOperation.newContent.replaceFirst("%subclass", androidName);
                } else {
                    newContent = newCommonOperation.newContent;
                }
                DefectInstance defectInstance =
                        createDefectInstance(
                                buggyFilePath,
                                xmlEntity.labelStartLine,
                                reformatFixedString(xmlEntity.labelContent),
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
        if (commonOperation != null && xmlEntity != null && commonOperation.operation.equals("insert")) {
            DefectInstance defectInstance =
                    createDefectInstance(
                            buggyFilePath,
                            -(xmlEntity.labelStartLine),
                            null,
                            reformatFixedString(xmlEntity.labelContent)
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
            if (addContents.length() <= 0) {
                continue;
            }
            if (labelType.equals(LabelType.USES_PERMISSION) || labelType.equals(LabelType.PERMISSION)) {
                // Add after "manifest" label.
                XmlEntity headXmlEntity =
                        XmlModificationChanger.getHead(
                                specificLabelPositionInfo.get(LabelType.MANIFEST))
                                .getValue();
                DefectInstance defectInstance =
                        createDefectInstance(
                                buggyFilePath,
                                -(headXmlEntity.labelStartLinesEndPosition + 1),
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
                                -(headXmlEntity.labelStartLinesEndPosition + 1),
                                null,
                                addContents.toString());
                defectInstance.setMessage(addHints.toString());
                defectInstance.isFixed = true;
                defectInstances.add(defectInstance);
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
            if (operation.equals("add") && labelNodes.get(key) == null) {
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
