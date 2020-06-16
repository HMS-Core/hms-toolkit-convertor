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

package com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.xml.CodeNetDocumentLocator;
import com.huawei.codebot.analyzer.x2y.xml.CodeNetElement;
import com.huawei.codebot.analyzer.x2y.xml.CodeNetSaxReader;
import com.huawei.codebot.analyzer.x2y.xml.LabelType;
import com.huawei.codebot.analyzer.x2y.xml.XmlEntitiesAnalyzer;
import com.huawei.codebot.analyzer.x2y.xml.XmlEntity;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
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

import static com.huawei.codebot.analyzer.x2y.xml.LabelType.APPLICATION;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.METADATA;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.getBranchLabels;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.getLeafLabels;
import static com.huawei.codebot.analyzer.x2y.xml.XmlEntitiesAnalyzer.getTargetBasedOnRegex;

class XmlConditionalChanger extends AndroidAppFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlConditionalChanger.class);

    private static final String MANIFEST_PATH = "src" + File.separator + "main" + File.separator
        + "AndroidManifest.xml";
    private Map<String, StructGradleXml> xmlChangerJsonTargets = new HashMap<>();
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    XmlConditionalChanger(List<StructGradleXml> config) {
        for (StructGradleXml gx : config) {
            xmlChangerJsonTargets.put(gx.operation.getLabelName() + gx.operation.getAndroidName(), gx);
        }
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        if (buggyFilePath.endsWith(MANIFEST_PATH)) {
            try {
                List<String> fileContent = FileUtils
                        .getOriginalFileLines(buggyFilePath, FileUtils.detectCharset(buggyFilePath));
                List<String> annotations = getTargetBasedOnRegex(fileContent, "<!--(\\s|.)*?-->");
                Locator locator = new LocatorImpl();
                DocumentFactory docFactory = new CodeNetDocumentLocator(locator);
                SAXReader reader = new CodeNetSaxReader(docFactory, locator);
                Document document = reader.read(new File(buggyFilePath));
                List<LabelType> analyzedBranchLabels = getBranchLabels();
                Map<String, XmlEntity> allLabelContents = new HashMap<String, XmlEntity>();
                for (LabelType labelType : analyzedBranchLabels) {
                    try {
                        XmlEntitiesAnalyzer xmlEntitiesAnalyzer = new XmlEntitiesAnalyzer(fileContent, annotations,
                                labelType, null);
                        Map<String, XmlEntity> labelContent = xmlEntitiesAnalyzer.getLabelContents();
                        allLabelContents.putAll(labelContent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                List<LabelType> analyzedLeafLabels = getLeafLabels();
                for (LabelType labelType : analyzedLeafLabels) {
                    try {
                        XmlEntitiesAnalyzer xmlEntitiesAnalyzer = new XmlEntitiesAnalyzer(fileContent, annotations,
                                labelType, allLabelContents);
                        Map<String, XmlEntity> labelContent = xmlEntitiesAnalyzer.getLabelContents();
                        allLabelContents.putAll(labelContent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Element root = document.getRootElement();
                operationInXmlNodes((CodeNetElement) root, buggyFilePath, allLabelContents, defectInstances);
                return defectInstances;
            } catch (IOException | DocumentException e) {
                LOGGER.error("", e);
            }
        }
        return defectInstances;
    }

    private void operationInXmlNodes(
            CodeNetElement node,
            String buggyFilePath, Map<String, XmlEntity> labelContents, List<DefectInstance> defectInstances) {
        if (node == null) {
            return;
        }
        List<CodeNetElement> listElement = node.getElements();
        if (node.getName().equals(APPLICATION.toString())) {
            StringBuilder sb = new StringBuilder();
            StringBuilder descs = new StringBuilder();
            for (Map.Entry<String, StructGradleXml> entry : xmlChangerJsonTargets.entrySet()) {
                if (entry.getValue().condition.isSatisfied && !entry.getValue().operation.isUnconditional()) {
                    boolean hasFound = false;
                    for (CodeNetElement e : listElement) {
                        if (METADATA.toString().equals(e.getName())
                                && e.attribute("name") != null
                                && e.attribute("name").getValue().equals(
                                entry.getValue().operation.getAndroidName())) {
                            hasFound = true;
                            break;
                        }
                    }
                    if (!hasFound) {
                        sb.append(entry.getValue().operation.getAddContent()).append(System.lineSeparator());
                        descs.append(gson.toJson(entry.getValue().operation.getDesc())).append(",");
                    }
                } else if (entry.getValue().condition.isSatisfied) {
                    sb.append(entry.getValue().operation.getAddContent()).append(System.lineSeparator());
                    descs.append(gson.toJson(entry.getValue().operation.getDesc())).append(",");
                }
            }
            if (sb.length() > 0) {
                for (CodeNetElement e : Lists.reverse(listElement)) {
                    if (e.attribute("name") != null) {
                        String androidName = e.attribute("name").getValue();
                        String nodeName = e.getName();
                        String key = nodeName + androidName;
                        XmlEntity xmlEntity = labelContents.get(key);
                        DefectInstance defectInstance = createDefectInstance(buggyFilePath,
                                -(xmlEntity.labelEndLine + 1), null,
                                sb.toString().substring(0, sb.toString().length() - System.lineSeparator().length()));
                        defectInstance.setMessage(descs.toString().substring(0, descs.toString().length() - 1));
                        defectInstance.isFixed = true;
                        defectInstances.add(defectInstance);
                        break;
                    }
                }
            }
        }
        for (CodeNetElement e : listElement) {
            operationInXmlNodes(e, buggyFilePath, labelContents, defectInstances);
        }
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
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_CROSSGRADLEXML;
            info.description = "GradleXml CrossFileChanger";
            this.info = info;
        }
        return this.info;
    }
}
