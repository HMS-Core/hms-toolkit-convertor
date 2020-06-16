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

import com.huawei.codebot.analyzer.x2y.java.method.MethodChangePattern;
import com.huawei.codebot.analyzer.x2y.java.method.replace.ReplacedMethod;
import com.huawei.codebot.framework.AbstractJSONConfig;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.utils.JsonUtil;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class X2YAutoJSONConfig extends AbstractJSONConfig {
    private List<ReplacedMethod> autoMethods = new ArrayList<>();

    X2YAutoJSONConfig(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
    }

    @Override
    public String getFileName() {
        if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
            return "wisehub-auto-hms";
        }
        if (DefectFixerType.WISEHUB.toString().equals(getFixerTypeString())) {
            return "wisehub-auto";
        }
        return DefectFixerType.LIBADAPTION_CLASSRENAME.toString();
    }

    HashMap<String, String> getClassRenamePatterns() {
        HashMap<String, String> changePatterns = new HashMap<String, String>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj != null) {
            JSONArray renamedClasses = (JSONArray) this.getJSON().get("autoClasses");
            for (int i = 0; i < renamedClasses.length(); i++) {
                JSONObject renamedClass = (JSONObject) renamedClasses.get(i);
                String oldClassName = renamedClass.getString("oldClassName");
                String newClassName = renamedClass.getString("newClassName");
                changePatterns.put(oldClassName, newClassName);
            }
        }
        return changePatterns;
    }

    Map<String, Map> getClassRenameDescription() {
        HashMap<String, Map> descriptions = new HashMap<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj != null) {
            if (this.getJSON().get("autoClasses") instanceof JSONArray) {
                JSONArray renamedClasses = (JSONArray) this.getJSON().get("autoClasses");
                for (int i = 0; i < renamedClasses.length(); i++) {
                    if (renamedClasses.get(i) instanceof JSONObject) {
                        JSONObject renamedClass = (JSONObject) renamedClasses.get(i);
                        String oldClassName = renamedClass.getString("oldClassName");
                        Map desc = JsonUtil.toMap(renamedClass.getJSONObject("desc"));
                        descriptions.put(oldClassName, desc);
                    }
                }
            }
        }
        return descriptions;
    }

    HashMap<String, String> getFieldRenamePatterns() {
        HashMap<String, String> changePatterns = new HashMap<String, String>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj != null) {
            JSONArray renamedFields = (JSONArray) this.getJSON().get("autoFields");
            for (int i = 0; i < renamedFields.length(); i++) {
                JSONObject renamedField = (JSONObject) renamedFields.get(i);
                String oldFieldName = renamedField.getString("oldFieldName");
                String newFieldName = renamedField.getString("newFieldName");
                changePatterns.put(oldFieldName, newFieldName);
            }
        }
        return changePatterns;
    }

    Map<String, Map> getFieldDescription() {
        HashMap<String, Map> descriptions = new HashMap<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj != null) {
            JSONArray renamedClasses = (JSONArray) this.getJSON().get("autoFields");
            for (int i = 0; i < renamedClasses.length(); i++) {
                JSONObject renamedClass = (JSONObject) renamedClasses.get(i);
                String oldClassName = renamedClass.getString("oldFieldName");
                Map desc = JsonUtil.toMap(renamedClass.getJSONObject("desc"));
                descriptions.put(oldClassName, desc);
            }
        }
        return descriptions;
    }

    /**
     * Get the Mapping before and after Package modification in Json file
     */
    HashMap<String, String> getPackageRenamePatterns() {
        HashMap<String, String> changePatterns = new HashMap<String, String>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj != null && this.getJSON().keySet().contains("autoPackages")) {
            JSONArray renamedPackage = (JSONArray) this.getJSON().get("autoPackages");
            if (renamedPackage != null) {
                for (int i = 0; i < renamedPackage.length(); i++) {
                    if (renamedPackage.get(i) instanceof JSONObject) {
                        JSONObject renamedPackages = (JSONObject) renamedPackage.get(i);
                        String oldPackageName = renamedPackages.getString("oldPackageName");
                        String newPackageName = renamedPackages.getString("newPackageName");
                        changePatterns.put(oldPackageName, newPackageName);
                    }
                }
            }
        }
        return changePatterns;
    }

    /**
     * Get the desc from Json file
     * for package rename
     */
    Map<String, Map> getPackageRenameDescription() {
        HashMap<String, Map> descriptions = new HashMap<>();
        JSONObject jsonObj = this.getJSON();
        if (jsonObj == null || !this.getJSON().keySet().contains("autoPackages")) {
            return descriptions;
        }
        if (this.getJSON().get("autoPackages") instanceof JSONArray) {
            JSONArray renamedPackages = (JSONArray) this.getJSON().get("autoPackages");
            if (renamedPackages != null) {
                for (int i = 0; i < renamedPackages.length(); i++) {
                    if (renamedPackages.get(i) instanceof JSONObject) {
                        JSONObject renamedClass = (JSONObject) renamedPackages.get(i);
                        String oldClassName = renamedClass.getString("oldPackageName");
                        Map desc = JsonUtil.toMap(renamedClass.getJSONObject("desc"));
                        descriptions.put(oldClassName, desc);
                    }
                }
            }
        }
        return descriptions;
    }

    HashMap<String, List<MethodChangePattern>> getMethodReplacePatterns() {
        HashMap<String, List<MethodChangePattern>> changePatterns = new HashMap<>();
        Gson gson = new Gson();
        X2YAutoJSONConfig configJson = gson.fromJson(this.getJSON().toString(),
            new SubTypeToken().getType());
        if (configJson != null) {
            for (ReplacedMethod replacedMethod : configJson.autoMethods) {
                if (changePatterns.containsKey(replacedMethod.getOldMethodName())) {
                    changePatterns.get(replacedMethod.getOldMethodName()).add(replacedMethod);
                } else {
                    List<MethodChangePattern> list = new ArrayList<>();
                    list.add(replacedMethod);
                    changePatterns.put(replacedMethod.getOldMethodName(), list);
                }
            }
        }
        return changePatterns;
    }

    public void setAutoMethods(List<ReplacedMethod> autoMethods) {
        this.autoMethods = autoMethods;
    }

    private static class SubTypeToken extends TypeToken<X2YAutoJSONConfig> {
        private static final long serialVersionUID = 1L;
    }
}
