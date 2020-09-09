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

package com.huawei.codebot.analyzer.x2y.io.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.xml.CommonOperation;
import com.huawei.codebot.analyzer.x2y.xml.LabelType;
import com.huawei.codebot.analyzer.x2y.xml.LayoutAtrrValueOperation;
import com.huawei.codebot.analyzer.x2y.xml.LayoutAtrributeOperation;
import com.huawei.codebot.analyzer.x2y.xml.LayoutOperation;
import com.huawei.codebot.analyzer.x2y.xml.XmlJsonPattern;
import com.huawei.codebot.framework.AbstractJSONConfig;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huawei.codebot.analyzer.x2y.xml.LabelType.ACTIVITY;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.METADATA;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.PERMISSION;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.PROVIDER;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.RECEIVER;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.SERVICE;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.USES_PERMISSION;

class XMLJSONConfig extends AbstractJSONConfig {
    XMLJSONConfig(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
    }

    @Override
    public String getFileName() {
        if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
            return "wisehub-xml-hms";
        }
        if (DefectFixerType.WISEHUB.toString().equals(getFixerTypeString())) {
            return "wisehub-xml";
        }
        return DefectFixerType.LIBADAPTION_XMLMODIFICATION.toString();
    }

    XmlJsonPattern getXmlPatterns() {
        Map<String, CommonOperation> xmlChangerJsonTargets = new HashMap<>();
        Map<LabelType, Map<String, CommonOperation>> xmlChangerCategoryJsonTargets = new HashMap<>();
        Map<String, LayoutOperation> layoutOprationJsonTargets = new HashMap<>();
        Map<String, List<LayoutAtrributeOperation>> layoutAtrributeOperationJsonTargets = new HashMap<>();
        Map<String, List<LayoutAtrrValueOperation>> layoutAtrrValueOperationJsonTargets = new HashMap<>();
        XmlJsonPattern xmlJsonPattern = new XmlJsonPattern();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj != null) {
            JSONArray commonOperation = (JSONArray) this.getJSON().get("commonOperation");
            for (int i = 0; i < commonOperation.length(); i++) {
                JSONObject operationObject = (JSONObject) commonOperation.get(i);
                if (operationObject != null) {
                    CommonOperation commonOperationObject = new CommonOperation();
                    commonOperationObject.androidName = operationObject.getString("androidName");
                    commonOperationObject.operation = operationObject.getString("operation");
                    commonOperationObject.labelName = operationObject.getString("labelName");
                    if (StringUtils.isEmpty(commonOperationObject.androidName)
                            || StringUtils.isEmpty(commonOperationObject.operation)
                            || StringUtils.isEmpty(commonOperationObject.labelName)) {
                        continue;
                    }
                    commonOperationObject.desc = getDescription(operationObject, "desc");
                    commonOperationObject.newContent = operationObject.getString("newContent");
                    String key = commonOperationObject.labelName + commonOperationObject.androidName;
                    xmlChangerJsonTargets.put(key, commonOperationObject);
                    if (commonOperationObject.labelName.equals(USES_PERMISSION.toString())) {
                        generateJsonMap(USES_PERMISSION, key, commonOperationObject, xmlChangerCategoryJsonTargets);
                    } else if (commonOperationObject.labelName.equals(PERMISSION.toString())) {
                        generateJsonMap(PERMISSION, key, commonOperationObject, xmlChangerCategoryJsonTargets);
                    } else if (commonOperationObject.labelName.equals(METADATA.toString())) {
                        generateJsonMap(METADATA, key, commonOperationObject, xmlChangerCategoryJsonTargets);
                    } else if (commonOperationObject.labelName.equals(SERVICE.toString())) {
                        generateJsonMap(SERVICE, key, commonOperationObject, xmlChangerCategoryJsonTargets);
                    } else if (commonOperationObject.labelName.equals(ACTIVITY.toString())) {
                        generateJsonMap(ACTIVITY, key, commonOperationObject, xmlChangerCategoryJsonTargets);
                    } else if (commonOperationObject.labelName.equals(PROVIDER.toString())) {
                        generateJsonMap(PROVIDER, key, commonOperationObject, xmlChangerCategoryJsonTargets);
                    } else if (commonOperationObject.labelName.equals(RECEIVER.toString())) {
                        generateJsonMap(RECEIVER, key, commonOperationObject, xmlChangerCategoryJsonTargets);
                    }
                }
            }

            JSONArray layoutOperation = (JSONArray) this.getJSON().get("layoutOperation");
            for (int j = 0; j < layoutOperation.length(); j++) {
                JSONObject operationObject = (JSONObject) layoutOperation.get(j);
                LayoutOperation layoutOperationObject = new LayoutOperation();
                layoutOperationObject.oldClassName = operationObject.getString("oldClassName");
                layoutOperationObject.newClassName = operationObject.getString("newClassName");
                layoutOperationObject.desc = getDescription(operationObject, "desc");
                if (StringUtils.isEmpty(layoutOperationObject.oldClassName)) {
                    continue;
                }
                layoutOprationJsonTargets.put(layoutOperationObject.oldClassName, layoutOperationObject);
            }

            if (this.getJSON().has("layoutAtrributeOperation")) {
                JSONArray layoutAtrributeOperation = (JSONArray) this.getJSON().get("layoutAtrributeOperation");
                List<LayoutAtrributeOperation> layoutAtrributeOperationList = new ArrayList<>();
                for (int j = 0; j < layoutAtrributeOperation.length(); j++) {
                    JSONObject operationObject = (JSONObject) layoutAtrributeOperation.get(j);
                    LayoutAtrributeOperation layoutAtrributeOperationObject = new LayoutAtrributeOperation();
                    layoutAtrributeOperationObject.oldClassName = operationObject.getString("oldClassName");
                    layoutAtrributeOperationObject.oldAtrributeName = operationObject.getString("oldAtrributeName");
                    layoutAtrributeOperationObject.newAtrributeName = operationObject.getString("newAtrributeName");
                    layoutAtrributeOperationObject.desc = getDescription(operationObject, "desc");
                    if (StringUtils.isEmpty(layoutAtrributeOperationObject.oldClassName)) {
                        continue;
                    }
                    if (StringUtils.isEmpty(layoutAtrributeOperationObject.oldAtrributeName)) {
                        continue;
                    }
                    layoutAtrributeOperationList.add(layoutAtrributeOperationObject);
                    layoutAtrributeOperationJsonTargets.put(layoutAtrributeOperationObject.oldClassName,
                            layoutAtrributeOperationList);
                }
            }

            if (this.getJSON().has("layoutAtrrValueOperation")) {
                JSONArray layoutAtrrValueOperation = (JSONArray) this.getJSON().get("layoutAtrrValueOperation");
                List<LayoutAtrrValueOperation> layoutAtrrValueOperationList = new ArrayList<>();
                for (int j = 0; j < layoutAtrrValueOperation.length(); j++) {
                    JSONObject operationObject = (JSONObject) layoutAtrrValueOperation.get(j);
                    LayoutAtrrValueOperation layoutAtrrValueOperationObject = new LayoutAtrrValueOperation();
                    layoutAtrrValueOperationObject.oldClassName = operationObject.getString("oldClassName");
                    layoutAtrrValueOperationObject.oldAtrributeName = operationObject.getString("oldAtrributeName");
                    layoutAtrrValueOperationObject.oldAtrrValue = operationObject.getString("oldAtrrValue");
                    layoutAtrrValueOperationObject.newAtrrValue = operationObject.getString("newAtrrValue");
                    layoutAtrrValueOperationObject.desc = getDescription(operationObject, "desc");
                    if (StringUtils.isEmpty(layoutAtrrValueOperationObject.oldClassName)) {
                        continue;
                    }
                    if (StringUtils.isEmpty(layoutAtrrValueOperationObject.oldAtrributeName)) {
                        continue;
                    }
                    if (StringUtils.isEmpty(layoutAtrrValueOperationObject.oldAtrrValue)) {
                        continue;
                    }
                    layoutAtrrValueOperationList.add(layoutAtrrValueOperationObject);
                    layoutAtrrValueOperationJsonTargets.put(layoutAtrrValueOperationObject.oldClassName,
                            layoutAtrrValueOperationList);
                }
            }

            xmlJsonPattern.setXmlChangerJsonTargets(xmlChangerJsonTargets)
                    .setXmlChangerCategoryJsonTargets(xmlChangerCategoryJsonTargets)
                    .setLayoutOperationJsonTargets(layoutOprationJsonTargets)
                    .setLayoutAtrributeOperationJsonTargets(layoutAtrributeOperationJsonTargets)
                    .setLayoutAtrrValueOperationJsonTargets(layoutAtrrValueOperationJsonTargets);

            if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
                JSONObject operationObject = (JSONObject)this.getJSON().get("SpecialConversionResourceDesc");
                String specialConversionResourceDesc = getDescription(operationObject,"desc");
                xmlJsonPattern.setSpecialConversionResourceDesc(specialConversionResourceDesc);
            }
            return xmlJsonPattern;
        }
        return null;
    }

    private void generateJsonMap(LabelType labelType, String key, CommonOperation commonOperationObject,
        Map<LabelType, Map<String, CommonOperation>> xmlChangerCategoryJsonTargets) {
        xmlChangerCategoryJsonTargets.computeIfAbsent(labelType, k -> new HashMap()).put(key, commonOperationObject);
    }

    private String getDescription(JSONObject jsonObject, String key) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map desc = JsonUtil.toMap(jsonObject.getJSONObject(key));
        return gson.toJson(desc);
    }
}
