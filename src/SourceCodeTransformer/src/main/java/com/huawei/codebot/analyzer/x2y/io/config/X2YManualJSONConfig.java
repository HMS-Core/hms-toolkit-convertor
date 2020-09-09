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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.huawei.codebot.analyzer.x2y.java.method.MethodChangePattern;
import com.huawei.codebot.analyzer.x2y.java.method.delete.DeleteMethod;
import com.huawei.codebot.framework.AbstractJSONConfig;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.utils.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class X2YManualJSONConfig extends AbstractJSONConfig {
    X2YManualJSONConfig(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
    }

    private List<DeleteMethod> manualMethods = new ArrayList<>();

    @Override
    public String getFileName() {
        if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
            return "wisehub-manual-hms";
        }
        if (DefectFixerType.WISEHUB.toString().equals(getFixerTypeString())) {
            return "wisehub-manual";
        }
        return DefectFixerType.LIBADAPTION_CLASSDELETE.toString();
    }

    Set<String> getClassDeletePatterns() {
        Set<String> changePatterns = new HashSet<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null) {
            return changePatterns;
        }
        if (this.getJSON().get("manualClasses") instanceof JSONArray) {
            JSONArray deletedClass = (JSONArray) this.getJSON().get("manualClasses");
            if (deletedClass != null) {
                for (int i = 0; i < deletedClass.length(); i++) {
                    if (deletedClass.get(i) instanceof JSONObject) {
                        JSONObject deletedClasses = (JSONObject) deletedClass.get(i);
                        String className = deletedClasses.getString("className");
                        changePatterns.add(className);
                    }
                }
            }
        }
        return changePatterns;
    }

    Map<String, Map> getClassDeleteDescription() {
        HashMap<String, Map> descriptions = new HashMap<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null) {
            return descriptions;
        }
        Object objectTemp = this.getJSON().get("manualClasses");
        if (objectTemp instanceof JSONArray) {
            JSONArray deletedClasses = (JSONArray) objectTemp;
            for (int i = 0; i < deletedClasses.length(); i++) {
                if (deletedClasses.get(i) instanceof JSONObject) {
                    JSONObject deleteClass = (JSONObject) deletedClasses.get(i);
                    String className = deleteClass.getString("className");
                    Map desc = JsonUtil.toMap(deleteClass.getJSONObject("desc"));
                    descriptions.put(className, desc);
                }
            }
        }
        return descriptions;
    }

    List<String> getFieldDeletePatterns() {
        List<String> changePatterns = new ArrayList<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null) {
            return changePatterns;
        }
        if (this.getJSON().get("manualFields") instanceof JSONArray) {
            JSONArray deletedField = (JSONArray) this.getJSON().get("manualFields");
            if (deletedField != null) {
                for (int i = 0; i < deletedField.length(); i++) {
                    if (deletedField.get(i) instanceof JSONObject) {
                        JSONObject deletedFields = (JSONObject) deletedField.get(i);
                        String fieldName = deletedFields.getString("fieldName");
                        changePatterns.add(fieldName);
                    }
                }
            }
        }
        return changePatterns;
    }

    Map<String, Map> getFieldDeleteDescriptions() {
        HashMap<String, Map> descriptions = new HashMap<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null) {
            return descriptions;
        }
        Object objectTemp = this.getJSON().get("manualFields");
        if (objectTemp instanceof JSONArray) {
            JSONArray deletedFields = (JSONArray) this.getJSON().get("manualFields");
            for (int i = 0; i < deletedFields.length(); i++) {
                if (deletedFields.get(i) instanceof JSONObject) {
                    JSONObject deleteField = (JSONObject) deletedFields.get(i);
                    String fieldName = deleteField.getString("fieldName");
                    Map desc = JsonUtil.toMap(deleteField.getJSONObject("desc"));
                    descriptions.put(fieldName, desc);
                }
            }
        }
        return descriptions;
    }

    List<String> getPackageDeletePatterns() {
        List<String> changePatterns = new ArrayList<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null) {
            return changePatterns;
        }
        Object manualPackages = this.getJSON().get("manualPackages");
        if (manualPackages instanceof JSONArray) {
            JSONArray deletedPackages = (JSONArray) manualPackages;
            for (int i = 0; i < deletedPackages.length(); i++) {
                if (deletedPackages.get(i) instanceof JSONObject) {
                    JSONObject deletedPackage = (JSONObject) deletedPackages.get(i);
                    String packageName = deletedPackage.getString("deletedPackageName");
                    changePatterns.add(packageName);
                }
            }
        }
        return changePatterns;
    }

    Map<String, Map> getPackageDeleteDescriptions() {
        HashMap<String, Map> descriptions = new HashMap<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null) {
            return descriptions;
        }
        Object objectTemp = this.getJSON().get("manualPackages");
        if (!(objectTemp instanceof JSONArray)) {
            return descriptions;
        }

        JSONArray deletedPackages = (JSONArray) objectTemp;
        for (int i = 0; i < deletedPackages.length(); i++) {
            if (deletedPackages.get(i) instanceof JSONObject) {
                JSONObject deletePackage = (JSONObject) deletedPackages.get(i);
                String packageName = deletePackage.getString("deletedPackageName");
                Map desc = JsonUtil.toMap(deletePackage.getJSONObject("desc"));
                descriptions.put(packageName, desc);
            }
        }
        return descriptions;
    }

    HashMap<String, List<MethodChangePattern>> getDeleteMethodPatterns() {
        HashMap<String, List<MethodChangePattern>> changePatterns = new HashMap<>();
        Gson gson = new Gson();
        X2YManualJSONConfig configJson = gson.fromJson(this.getJSON().toString(),
                new SubTypeToken().getType());
        if(configJson == null){
            return changePatterns;
        }
        configJson.manualMethods.stream()
                .filter(method -> method != null)
                .forEach(method -> changePatterns.computeIfAbsent(method.getMethodName(),
                        k -> new ArrayList<>()).add(method));
        return changePatterns;
    }

    public void setManualMethods(List<DeleteMethod> manualMethods) {
        this.manualMethods = manualMethods;
    }

    private static class SubTypeToken extends TypeToken<X2YManualJSONConfig>{
        private static final long serialVersionUID = 1L;
    }
}
