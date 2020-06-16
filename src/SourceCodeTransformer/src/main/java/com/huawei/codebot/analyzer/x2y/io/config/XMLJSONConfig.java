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

import static com.huawei.codebot.analyzer.x2y.xml.LabelType.ACTIVITY;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.METADATA;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.PERMISSION;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.PROVIDER;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.RECEIVER;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.SERVICE;
import static com.huawei.codebot.analyzer.x2y.xml.LabelType.USES_PERMISSION;

import com.huawei.codebot.analyzer.x2y.xml.CommonOperation;
import com.huawei.codebot.analyzer.x2y.xml.LabelType;
import com.huawei.codebot.analyzer.x2y.xml.LayoutOperation;
import com.huawei.codebot.analyzer.x2y.xml.XmlJsonPattern;
import com.huawei.codebot.framework.AbstractJSONConfig;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.utils.JsonUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
                    commonOperationObject.newContent = operationObject.getString("newContent");
                    commonOperationObject.labelName = operationObject.getString("labelName");
                    commonOperationObject.desc = getDescription(operationObject, "desc");
                    if (StringUtils.isEmpty(commonOperationObject.androidName)
                        || StringUtils.isEmpty(commonOperationObject.operation)
                        || StringUtils.isEmpty(commonOperationObject.labelName)) {
                        continue;
                    }

                    String key = commonOperationObject.labelName + commonOperationObject.androidName;
                    xmlChangerJsonTargets.put(key, commonOperationObject);
                    if (commonOperationObject.labelName.equals(USES_PERMISSION.toString())
                        || commonOperationObject.labelName.equals(PERMISSION.toString())
                        || commonOperationObject.labelName.equals(METADATA.toString())
                        || commonOperationObject.labelName.equals(SERVICE.toString())
                        || commonOperationObject.labelName.equals(ACTIVITY.toString())
                        || commonOperationObject.labelName.equals(PROVIDER.toString())
                        || commonOperationObject.labelName.equals(RECEIVER.toString())) {
                        generateJsonMap(LabelType.fromValue(commonOperationObject.labelName), key,
                            commonOperationObject, xmlChangerCategoryJsonTargets);
                    }
                }
            }

            return setXmlJsonPattern(xmlChangerJsonTargets, xmlChangerCategoryJsonTargets, layoutOprationJsonTargets,
                xmlJsonPattern);
        }
        return null;
    }

    private XmlJsonPattern setXmlJsonPattern(Map<String, CommonOperation> xmlChangerJsonTargets,
        Map<LabelType, Map<String, CommonOperation>> xmlChangerCategoryJsonTargets,
        Map<String, LayoutOperation> layoutOprationJsonTargets, XmlJsonPattern xmlJsonPattern) {
        JSONArray layoutOperation = (JSONArray) this.getJSON().get("layoutOperation");
        for (int j = 0; j < layoutOperation.length(); j++) {
            JSONObject operationObject = (JSONObject) layoutOperation.get(j);
            LayoutOperation layoutOperationObject = new LayoutOperation();
            layoutOperationObject.oldClassName = operationObject.getString("oldClassName");
            layoutOperationObject.newClassName = operationObject.getString("newClassName");
            layoutOperationObject.desc = getDescription(operationObject, "desc");
            if (layoutOperationObject.oldClassName == null || layoutOperationObject.oldClassName.trim().equals("")) {
                continue;
            }
            layoutOprationJsonTargets.put(layoutOperationObject.oldClassName, layoutOperationObject);
        }
        xmlJsonPattern.setXmlChangerJsonTargets(xmlChangerJsonTargets)
                .setXmlChangerCategoryJsonTargets(xmlChangerCategoryJsonTargets)
                .setLayoutOperationJsonTargets(layoutOprationJsonTargets);
        return xmlJsonPattern;
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
