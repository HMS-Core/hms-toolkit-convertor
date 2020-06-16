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
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.ReplaceData;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.SpecificJsonPattern;
import com.huawei.codebot.framework.AbstractJSONConfig;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.utils.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SpecificChangerJSONConfig extends AbstractJSONConfig {
    SpecificChangerJSONConfig(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
    }

    @Override
    public String getFileName() {
        if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
            return "wisehub-specific-hms";
        }
        if (DefectFixerType.WISEHUB.toString().equals(getFixerTypeString())) {
            return "wisehub-specific";
        }
        return DefectFixerType.LIBADAPTION_SPECIFICMODIFICATION.toString();
    }

    SpecificJsonPattern getSpecificPatterns() {
        JSONObject jsonObj = this.getJSON();
        SpecificJsonPattern specificJsonPattern = new SpecificJsonPattern();
        if (jsonObj != null) {
            List<ReplaceData> replaceBuilderPatterns = (List<ReplaceData>) replaceObject("replaceBuilder",
                    "builderName", "parameterContainsIdentifier", "newContent");
            Map<String, String> deleteUrlPatterns = (Map<String, String>) deleteObject("deleteUrl", "url");
            Map<String, String> deleteFilePatterns = (Map<String, String>) deleteObject("deleteFile", "fileName");
            List<ReplaceData> replaceScopePatterns = (List<ReplaceData>) replaceObject("replaceScope", "scopeName",
                    "parameterContainsIdentifier", "newContent");
            List<ReplaceData> deleteScopePatterns = (List<ReplaceData>) replaceObject("deleteScope", "scopeName",
                    "noneParameterContainsIdentifier", null);
            specificJsonPattern.setReplaceBuilderPatterns(replaceBuilderPatterns)
                    .setDeleteUrlPatterns(deleteUrlPatterns)
                    .setDeleteFilePatterns(deleteFilePatterns).setReplaceScopePatterns(replaceScopePatterns)
                    .setDeleteScopePatterns(deleteScopePatterns);
            return specificJsonPattern;
        }
        return null;
    }

    String getDescription(JSONObject jsonObject, String key) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Map desc = JsonUtil.toMap(jsonObject.getJSONObject(key));
        return gson.toJson(desc);
    }

    private Object replaceObject(String jsonKey, String name, String identifier, String newContent) {
        List<ReplaceData> replacePatterns = new ArrayList<ReplaceData>();
        JSONArray replaceClasses = (JSONArray) this.getJSON().get(jsonKey);
        for (int i = 0; i < replaceClasses.length(); i++) {
            JSONObject replaceBuilderLabel = (JSONObject) replaceClasses.get(i);
            if (replaceBuilderLabel != null) {
                ReplaceData replaceBuilderData = new ReplaceData();
                if (name != null && replaceBuilderLabel.keySet().contains(name)) {
                    replaceBuilderData.name = (String) replaceBuilderLabel.get(name);
                }
                if (identifier != null && replaceBuilderLabel.keySet().contains(identifier)) {
                    replaceBuilderData.parameterContainsIdentifier = (String) replaceBuilderLabel.get(identifier);
                }
                if (newContent != null && replaceBuilderLabel.keySet().contains(newContent)) {
                    replaceBuilderData.newContent = (String) replaceBuilderLabel.get(newContent);
                }
                if (replaceBuilderLabel.keySet().contains("desc")) {
                    replaceBuilderData.description = getDescription(replaceBuilderLabel, "desc");
                }
                if (replaceBuilderData.name == null || replaceBuilderData.name.equals("")) {
                    continue;
                }
                replacePatterns.add(replaceBuilderData);
            }
        }
        return replacePatterns;
    }

    private Object deleteObject(String jsonKey, String name) {
        Map<String, String> deletePatterns = new HashMap<>();
        JSONArray deleteClasses = (JSONArray) this.getJSON().get(jsonKey);
        for (int i = 0; i < deleteClasses.length(); i++) {
            if (deleteClasses.get(i) instanceof JSONObject) {
                JSONObject deleteLabel = (JSONObject) deleteClasses.get(i);
                if (deleteLabel.getString(name) == null || deleteLabel.getString(name).equals("")) {
                    continue;
                }
                deletePatterns.put(deleteLabel.getString(name), getDescription(deleteLabel, "desc"));
            }
        }
        return deletePatterns;
    }
}
