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

package com.huawei.codebot.analyzer.x2y.java.other.complexchanger;

import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.xml.CodeNetElement;
import com.huawei.codebot.analyzer.x2y.xml.CommonOperation;
import com.huawei.codebot.analyzer.x2y.xml.XmlEntity;
import com.huawei.codebot.analyzer.x2y.xml.XmlModificationChanger;
import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;

import java.util.List;
import java.util.Map;

import static com.huawei.codebot.analyzer.x2y.xml.LabelType.ACTION;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.ACTIVITY;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.CATEGORY;
import static com.huawei.codebot.analyzer.x2y.xml.XmlEntitiesAnalyzer.getJsonTargetKey;
import static com.huawei.codebot.framework.FixStatus.NONEFIX;

/**
 * A changer used to process Complex Xml Modification
 *
 * @since 2020-04-17
 */
public class ComplexXmlModificationChanger extends XmlModificationChanger {
    public ComplexXmlModificationChanger(String fixerType) throws CodeBotRuntimeException {
        super(ConfigService.getInstance(fixerType).getComplexXmlPatterns());
    }

    @Override
    protected void operationInXmlNodes(
            CodeNetElement node,
            String buggyFilePath,
            Map<String, XmlEntity> labelContents,
            List<DefectInstance> defectInstances) {
        if (node == null) {
            return;
        }

        if (node.getParent() != null && node.getParent().getParent() != null) {
            String nodeName = node.getName();
            if (node.attribute("name") != null) {
                String androidName = node.attribute("name").getValue();
                String grandNodeName = node.getParent().getParent().getName();
                if (grandNodeName.equals(ACTIVITY.toString())) {
                    if (nodeName.equals(ACTION.toString()) || nodeName.equals(CATEGORY.toString())) {
                        if (node.getParent().getParent().attribute("name") != null) {
                            String grandAndroidName = node.getParent().getParent().attribute("name").getValue();
                            detectOperation(
                                    buggyFilePath,
                                    nodeName,
                                    androidName,
                                    grandNodeName,
                                    grandAndroidName,
                                    labelContents,
                                    defectInstances);
                        }
                    }
                }
            }
        }
        List<CodeNetElement> listElement = node.getElements();
        for (CodeNetElement e : listElement) {
            operationInXmlNodes(e, buggyFilePath, labelContents, defectInstances);
        }
    }

    private void detectOperation(
            String buggyFilePath,
            String nodeName,
            String androidName,
            String grandNodeName,
            String grandAndroidName,
            Map<String, XmlEntity> labelContents,
            List<DefectInstance> defectInstances) {
        String nodekey = nodeName + androidName;
        CommonOperation commonOperation = xmlChangerJsonTargets.get(nodekey);
        String grandNodeKey = grandNodeName + grandAndroidName;
        XmlEntity xmlEntity = labelContents.get(grandNodeKey);
        if (xmlEntity == null || isExistedDefectInstance(xmlEntity.getLabelStartLine(), defectInstances)) {
            return;
        }
        if (commonOperation != null && "delete".equals(commonOperation.operation)) {
            DefectInstance defectInstance =
                    createDefectInstance(
                            buggyFilePath, xmlEntity.getLabelStartLine(), reformatFixedString(xmlEntity.getLabelContent()), null);
            defectInstance.setMessage(commonOperation.desc);
            defectInstance.setStatus(NONEFIX.toString());
            defectInstance.isFixed = false;
            defectInstance.context.add("Activity", grandAndroidName);
            defectInstances.add(defectInstance);
        } else {
            if (getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets) != null) {
                String jsonKey = getJsonTargetKey(nodeName, androidName, xmlChangerJsonTargets);
                CommonOperation newCommonOperation = xmlChangerJsonTargets.get(jsonKey);
                if (newCommonOperation != null && "delete".equals(newCommonOperation.operation)) {
                    DefectInstance defectInstance =
                            createDefectInstance(
                                    buggyFilePath,
                                    xmlEntity.getLabelStartLine(),
                                    reformatFixedString(xmlEntity.getLabelContent()),
                                    null);
                    defectInstance.setMessage(newCommonOperation.desc);
                    defectInstance.setStatus(NONEFIX.toString());
                    defectInstance.isFixed = false;
                    defectInstance.context.add("Activity", grandAndroidName);
                    defectInstances.add(defectInstance);
                }
            }
        }
    }

    private static Boolean isExistedDefectInstance(int lineNumber, List<DefectInstance> defectInstances) {
        for (DefectInstance defectInstance : defectInstances) {
            if (lineNumber == defectInstance.mainBuggyLineNumber) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DefectInstance createDefectInstance(String filePath, int buggyLineNumber, String buggyLineContent,
            String fixedLineContent) {
        return ComplexChangerUtils.createDefectInstance(filePath, buggyLineNumber, buggyLineContent, fixedLineContent,
                this.getFixerInfo());
    }
}
