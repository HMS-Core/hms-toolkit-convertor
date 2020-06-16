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

import com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger.StructGradleXml;
import com.huawei.codebot.framework.AbstractJSONConfig;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.utils.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * read wisehub-crossfile and wisehub-crossfile-hms
 * put all data into List<StructGradleXml>
 *
 * @since 2020-04-01
 */
class CrossfileChangerJSONConfig extends AbstractJSONConfig {
    CrossfileChangerJSONConfig(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
    }

    @Override
    public String getFileName() {
        if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
            return "wisehub-crossfile-hms";
        }
        if (DefectFixerType.WISEHUB.toString().equals(getFixerTypeString())) {
            return "wisehub-crossfile";
        }
        return DefectFixerType.LIBADAPTION_CLASSRENAME.toString();
    }

    List<StructGradleXml> getGradleXMLPatterns() {
        List<StructGradleXml> changePattern = new ArrayList<>();
        JSONObject jsonObject = this.getJSON();
        if (jsonObject.keySet().contains("GradleXml")) {
            JSONArray gradleXmlList = jsonObject.getJSONArray("GradleXml");
            for (int i = 0; i < gradleXmlList.length(); i++) {
                StructGradleXml gradleXmlTemp = new StructGradleXml();
                JSONObject jsonObjectGradleXml = (JSONObject) gradleXmlList.get(i);
                JSONObject structConditionTemp = (JSONObject) jsonObjectGradleXml.get("condition");
                gradleXmlTemp.condition.type = (String) structConditionTemp.get("type");
                gradleXmlTemp.condition.dependency = (String) structConditionTemp.get("dependency");
                JSONObject structOperationTemp = (JSONObject) jsonObjectGradleXml.get("operation");
                gradleXmlTemp.operation.setType((String) structOperationTemp.get("type"));
                gradleXmlTemp.operation.setOperationType((String) structOperationTemp.get("operationType"));
                gradleXmlTemp.operation.setLabelName((String) structOperationTemp.get("labelName"));
                gradleXmlTemp.operation.setAndroidName((String) structOperationTemp.get("androidName"));
                gradleXmlTemp.operation.setUnconditional((Boolean) structOperationTemp.get("unconditional"));
                gradleXmlTemp.operation.setAddContent((String) structOperationTemp.get("addContent"));
                gradleXmlTemp.operation.setDesc(JsonUtil.toMap(structOperationTemp.getJSONObject("desc")));
                changePattern.add(gradleXmlTemp);
            }
        }
        return changePattern;
    }
}
