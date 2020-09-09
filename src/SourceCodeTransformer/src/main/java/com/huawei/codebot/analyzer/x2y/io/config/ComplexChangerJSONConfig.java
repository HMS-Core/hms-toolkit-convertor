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
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppDeleteInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructGradleHeadquarter;
import com.huawei.codebot.analyzer.x2y.java.other.complexchanger.FixAction;
import com.huawei.codebot.analyzer.x2y.xml.CommonOperation;
import com.huawei.codebot.analyzer.x2y.xml.LabelType;
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

/**
 * complex changer config to read from json
 *
 * @since 2020-04-01
 */
class ComplexChangerJSONConfig extends AbstractJSONConfig {
    ComplexChangerJSONConfig(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
    }

    @Override
    public String getFileName() {
        if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
            return "wisehub-complex-hms";
        }
        if (DefectFixerType.WISEHUB.toString().equals(getFixerTypeString())) {
            return "wisehub-complex";
        }
        return DefectFixerType.LIBADAPTION_COMPLEXMODIFICATION.toString();
    }

    /**
     * get complex gradle modification from wisehub-complex or wisehub-complex-hms json in AppBuildGradle
     * this used to warn users which should to be delete in app dependencies
     */
    StructGradleHeadquarter getComplexGradleModificationPatterns() {
        StructGradleHeadquarter changePattern = new StructGradleHeadquarter();
        JSONObject jsonObject = this.getJSON();
        if (!jsonObject.keySet().contains("AppBuildGradle")) {
            return changePattern;
        }
        JSONObject appBuildGradleTemp = jsonObject.getJSONObject("AppBuildGradle");
        if (!appBuildGradleTemp.keySet().contains("deleteInDependencies")) {
            return changePattern;
        }
        JSONArray deleteInDependenciesTemp = appBuildGradleTemp.getJSONArray("deleteInDependencies");
        List<StructAppDeleteInDependencies> deleteInDependencies = new ArrayList<>();
        for (int i = 0; i < deleteInDependenciesTemp.length(); i++) {
            if (deleteInDependenciesTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) deleteInDependenciesTemp.get(i);
                StructAppDeleteInDependencies addTemp = new StructAppDeleteInDependencies();
                if (!jsonObjectTemp.keySet().contains("deleteInDependenciesName")) {
                    continue;
                }
                if (jsonObjectTemp.get("deleteInDependenciesName") instanceof String) {
                    addTemp.setDeleteClasspathInDependenciesName((String) jsonObjectTemp.get("deleteInDependenciesName"));
                    if (StringUtils.isNotEmpty(addTemp.getDeleteClasspathInDependenciesName())) {
                        addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                        deleteInDependencies.add(addTemp);
                    }
                }
            }
        }
        changePattern.setAppDeleteInDependencies(deleteInDependencies);
        return changePattern;
    }

    /**
     * get complex gradle modification from wisehub-complex or wisehub-complex-hms json in AppBuildGradle
     * this used to get commonOperation and to detect in xml
     */
    XmlJsonPattern getComplexXmlPatterns() {
        Map<String, CommonOperation> xmlChangerJsonTargets = new HashMap<>();
        Map<LabelType, Map<String, CommonOperation>> xmlChangerCategoryJsonTargets = new HashMap<>();
        XmlJsonPattern xmlJsonPattern = new XmlJsonPattern();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null) {
            return null;
        }
        Object objectTemp = this.getJSON().get("commonOperation");
        if (objectTemp instanceof JSONArray) {
            JSONArray commonOperation = (JSONArray) objectTemp;
            for (int i = 0; i < commonOperation.length(); i++) {
                if (commonOperation.get(i) instanceof JSONObject) {
                    JSONObject operationObject = (JSONObject) commonOperation.get(i);
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
                }
            }
        }
        xmlJsonPattern
                .setXmlChangerJsonTargets(xmlChangerJsonTargets)
                .setXmlChangerCategoryJsonTargets(xmlChangerCategoryJsonTargets);
        return xmlJsonPattern;
    }

    /**
     * get complex gradle modification from wisehub-complex or wisehub-complex-hms json in AppBuildGradle
     * if gradle and xml both exist it can add fixType
     */
    Map<String, FixAction> getComplexSpecificPatterns() {
        Map<String, FixAction> fixActions = new HashMap<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null) {
            return fixActions;
        }
        Object objectTemp = this.getJSON().get("fixOperation");
        if (objectTemp instanceof JSONArray) {
            JSONArray fixOperation = (JSONArray) objectTemp;
            for (int i = 0; i < fixOperation.length(); i++) {
                if (fixOperation.get(i) instanceof JSONObject) {
                    JSONObject operationObject = (JSONObject) fixOperation.get(i);
                    FixAction fixAction = new FixAction();
                    fixAction.setFixType(operationObject.getString("fixType"));
                    fixAction.setOperation(operationObject.getString("operation"));
                    fixAction.setNewContent(operationObject.getString("newContent"));
                    fixAction.setFixPosition(operationObject.getString("fixPosition"));
                    fixAction.setDesc(getDescription(operationObject, "desc"));
                    if (StringUtils.isEmpty(fixAction.getFixType()) || StringUtils.isEmpty(fixAction.getOperation())
                            || StringUtils.isEmpty(fixAction.getFixPosition())) {
                        continue;
                    }
                    String key = fixAction.getFixType() + fixAction.getOperation() + fixAction.getFixPosition();
                    fixActions.put(key, fixAction);
                }
            }
        }
        return fixActions;
    }

    /**
     * get complex gradle modification desc from wisehub-complex or wisehub-complex-hms
     */
    String getDescription(JSONObject jsonObject, String key) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map desc = JsonUtil.toMap(jsonObject.getJSONObject(key));
        return gson.toJson(desc);
    }
}
