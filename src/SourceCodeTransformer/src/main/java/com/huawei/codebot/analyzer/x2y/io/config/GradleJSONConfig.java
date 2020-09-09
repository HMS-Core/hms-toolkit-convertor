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

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAddClassPathInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAddMavenInReposotories;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddApplyPlugin;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddIndirectDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddMessage;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAidl;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppDeleteGmsApplyPlugin;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppDeleteInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppReplace;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructDeleteClasspathInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructDeleteClasspathInRepositories;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructGradleHeadquarter;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructSettingGradle;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructSpecialAdd;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructVersionWarnings;
import com.huawei.codebot.framework.AbstractJSONConfig;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class GradleJSONConfig extends AbstractJSONConfig {
    StructGradleHeadquarter changePattern = new StructGradleHeadquarter();

    GradleJSONConfig(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
    }

    @Override
    public String getFileName() {
        if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
            return "wisehub-gradle-hms";
        }
        if (DefectFixerType.WISEHUB.toString().equals(getFixerTypeString())) {
            return "wisehub-gradle";
        }
        return DefectFixerType.LIBADAPTION_GRADLEMODIFICATION.toString();
    }

    StructGradleHeadquarter getGradleModificationPatterns() {
        JSONObject jsonObject = this.getJSON();
        // read project change from json
        if (jsonObject.keySet().contains("projectBuildGradle")) {
            JSONObject projectBuildGradleTemp = jsonObject.getJSONObject("projectBuildGradle");
            // read project - addMavenInRepositoriesTemp
            if (projectBuildGradleTemp.keySet().contains("addMavenInRepositories")) {
                getAddMavenInRepositories(projectBuildGradleTemp);
            }
            // read project - deleteClasspathInDependencies
            if (projectBuildGradleTemp.keySet().contains("deleteClasspathInDependencies")) {
                getDeleteClasspathInDependencies(projectBuildGradleTemp);
            }
            // read project - deleteClasspathInRepositories
            if (projectBuildGradleTemp.keySet().contains("deleteClasspathInRepositories")) {
                getDeleteClasspathInRepositories(projectBuildGradleTemp);
            }
            // read project - addClassPathInDependencies
            if (projectBuildGradleTemp.keySet().contains("addClassPathInDependencies")) {
                getAddClassPathInDependencies(projectBuildGradleTemp);
            }
        }

        // read App
        if (jsonObject.keySet().contains("AppBuildGradle")) {
            JSONObject appBuildGradleTemp = jsonObject.getJSONObject("AppBuildGradle");
            // read App - addApplyPlugin
            if (appBuildGradleTemp.keySet().contains("addApplyPlugin")) {
                getAddApplyPlugin(appBuildGradleTemp);
            }

            // read App - addMessage
            if (appBuildGradleTemp.keySet().contains("addMessage")) {
                getAddMessage(appBuildGradleTemp);
            }

            // read App - deleteGmsApplyPlugin
            if (appBuildGradleTemp.keySet().contains("deleteGmsApplyPlugin")) {
                getDeleteGmsApplyPlugin(appBuildGradleTemp);
            }

            // read app - replace
            if (appBuildGradleTemp.keySet().contains("replace")) {
                getReplace(appBuildGradleTemp);
            }

            // read app - deleteInDependencies
            if (appBuildGradleTemp.keySet().contains("deleteInDependencies")) {
                getDeleteInDependencies(appBuildGradleTemp);
            }

            // read app - Aidl
            if (appBuildGradleTemp.keySet().contains("Aidl")) {
                getAidl(appBuildGradleTemp);
            }

            // read app - addIndirectDependencies
            if (appBuildGradleTemp.keySet().contains("addIndirectDependencies")) {
                getAddIndirectDependencies(appBuildGradleTemp);
            }

            // read App - addInDependencies
            if (appBuildGradleTemp.keySet().contains("addInDependencies")) {
                getAddInDependencies(appBuildGradleTemp);
            }
        }

        // read specific Json -> add new String in Settings.gradle
        // special demand : add one String in Settings.gradle in the first line
        if (jsonObject.keySet().contains("SettingsGradle")) {
            JSONObject settingsGradleJson = jsonObject.getJSONObject("SettingsGradle");
            if (settingsGradleJson.keySet().contains("addInSettingsGradle")) {
                getAddInSettingsGradle(settingsGradleJson);
            }
        }

        // read specific Json -> add new String in app build.gradle
        // special demand : add one String in dependency in app build.gradle
        if (jsonObject.keySet().contains("SpecialAddInDependency")) {
            JSONObject specialAddInDependencyJson = jsonObject.getJSONObject("SpecialAddInDependency");
            if (specialAddInDependencyJson.keySet().contains("addSpecialInDependency")) {
                getAddSpecialInDependency(specialAddInDependencyJson);
            }
        }

        if(jsonObject.keySet().contains("SpecialAddInApp")){
            JSONObject specialAddInAppJson = jsonObject.getJSONObject("SpecialAddInApp");
            if (specialAddInAppJson.keySet().contains("addSpecialInApply")) {
                getAddSpecialInApp(specialAddInAppJson);
            }
        }

        // read Version Warnings
        // if version continue alphabet like '17.0.0-beta', we need to generate an alarm.
        if (jsonObject.keySet().contains("VersionWarnings")) {
            JSONObject versionWarningsJson = jsonObject.getJSONObject("VersionWarnings");
            StructVersionWarnings versionWarnings = new StructVersionWarnings();
            if (versionWarningsJson.keySet().contains("desc")) {
                versionWarnings.setDesc(JsonUtil.toMap(versionWarningsJson.getJSONObject("desc")));
            }
            changePattern.setVersionWarnings(versionWarnings);
        }

        // read AllProjectsInfo
        if (jsonObject.keySet().contains("AllProjectsInfo")){
            StructSpecialAdd allProjectInfo = new StructSpecialAdd();
            JSONObject allProjectInfoJson = jsonObject.getJSONObject("AllProjectsInfo");
            if (allProjectInfoJson.keySet().contains("addString")) {
                allProjectInfo.setAddString((String) allProjectInfoJson.get("addString"));
            }
            if (allProjectInfoJson.keySet().contains("desc")) {
                allProjectInfo.setDesc(JsonUtil.toMap(allProjectInfoJson.getJSONObject("desc")));
            }
            changePattern.setAllProjectsInfo(allProjectInfo);
        }

        return changePattern;
    }


    /**
     * read project Build Gradle into StructAddMavenInReposotories from wisehub-gradle/wisehub-gradle-hms
     *
     * @param projectBuildGradleTemp to store data from wisehub-gradle project part
     */
    private void getAddMavenInRepositories(JSONObject projectBuildGradleTemp) {
        JSONArray addMavenInRepositoriesTemp =
                projectBuildGradleTemp.getJSONArray("addMavenInRepositories");
        List<StructAddMavenInReposotories> addMavenInRepositories = new ArrayList<>();
        for (int i = 0; i < addMavenInRepositoriesTemp.length(); i++) {
            if (addMavenInRepositoriesTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) addMavenInRepositoriesTemp.get(i);
                StructAddMavenInReposotories addTemp = new StructAddMavenInReposotories();
                if (jsonObjectTemp.keySet().contains("addMavenInRepositoriesName")) {
                    if (jsonObjectTemp.get("addMavenInRepositoriesName") instanceof String) {
                        addTemp.setAddMavenInRepositoriesName(
                                (String) jsonObjectTemp.get("addMavenInRepositoriesName"));
                    }
                    if (StringUtils.isNotEmpty(addTemp.getAddMavenInRepositoriesName())) {
                        addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                        addMavenInRepositories.add(addTemp);
                    }
                }
            }
        }
        changePattern.setProjectAddMavenInRepositories(addMavenInRepositories);
    }

    /**
     * read project Build Gradle deleteClasspathInDependencies into StructDeleteClasspathInDependencies
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param projectBuildGradleTemp to store data from wisehub-gradle project part
     */
    private void getDeleteClasspathInDependencies(JSONObject projectBuildGradleTemp) {
        JSONArray deleteClasspathInDependenciesTemp =
                projectBuildGradleTemp.getJSONArray("deleteClasspathInDependencies");
        List<StructDeleteClasspathInDependencies> deleteClasspathInDependencies = new ArrayList<>();
        for (int i = 0; i < deleteClasspathInDependenciesTemp.length(); i++) {
            if (deleteClasspathInDependenciesTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) deleteClasspathInDependenciesTemp.get(i);
                StructDeleteClasspathInDependencies addTemp = new StructDeleteClasspathInDependencies();
                if (jsonObjectTemp.keySet().contains("deleteClasspathInDependenciesName")) {
                    if (jsonObjectTemp.get("deleteClasspathInDependenciesName") instanceof String) {
                        addTemp.setDeleteClasspathInDependenciesName(
                                (String) jsonObjectTemp.get("deleteClasspathInDependenciesName"));
                        if (StringUtils.isNotEmpty(addTemp.getDeleteClasspathInDependenciesName())) {
                            addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                            deleteClasspathInDependencies.add(addTemp);
                        }
                    }
                }
            }
        }
        changePattern.setProjectDeleteClasspathInDependencies(deleteClasspathInDependencies);
    }

    /**
     * read project Build Gradle deleteClasspathInRepositories into StructDeleteClasspathInRepositories
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param projectBuildGradleTemp to store data from wisehub-gradle project part
     */
    private void getDeleteClasspathInRepositories(JSONObject projectBuildGradleTemp) {
        if (projectBuildGradleTemp.get("deleteClasspathInRepositories") instanceof List) {
            List deleteClasspathInRepositoriesTemp =
                    (List) projectBuildGradleTemp.get("deleteClasspathInRepositories");
            List<StructDeleteClasspathInRepositories> deleteClasspathInRepositories = new ArrayList<>();
            for (Object deleteClassPathOne : deleteClasspathInRepositoriesTemp) {
                if (deleteClassPathOne instanceof JSONObject) {
                    JSONObject jsonObjectTemp = (JSONObject) deleteClassPathOne;
                    StructDeleteClasspathInRepositories addTemp =
                            new StructDeleteClasspathInRepositories();
                    if (jsonObjectTemp.keySet().contains("deleteClasspathInRepositoriesName")) {
                        if (jsonObjectTemp.get("addMavenInRepositoriesName") instanceof String) {
                            addTemp.setDeleteClasspathInRepositoriesName(
                                    (String) jsonObjectTemp.get("addMavenInRepositoriesName"));
                            if (StringUtils.isNotEmpty(addTemp.getDeleteClasspathInRepositoriesName())) {
                                addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                                deleteClasspathInRepositories.add(addTemp);
                            }
                        }
                    }
                }
            }
            changePattern.setProjectDeleteClasspathInRepositories(deleteClasspathInRepositories);
        }
    }

    /**
     * read project Build Gradle addClassPathInDependencies into StructAddClassPathInDependencies
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param projectBuildGradleTemp to store data from wisehub-gradle project part
     */
    private void getAddClassPathInDependencies(JSONObject projectBuildGradleTemp) {
        JSONArray addClassPathInDependenciesTemp =
                projectBuildGradleTemp.getJSONArray("addClassPathInDependencies");
        List<StructAddClassPathInDependencies> addClassPathInDependencies = new ArrayList<>();
        for (int i = 0; i < addClassPathInDependenciesTemp.length(); i++) {
            if (addClassPathInDependenciesTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) addClassPathInDependenciesTemp.get(i);
                StructAddClassPathInDependencies addTemp = new StructAddClassPathInDependencies();
                if (jsonObjectTemp.keySet().contains("addClassPathInDependenciesName")) {
                    Object jsonObjectTempGet = jsonObjectTemp.get("addClassPathInDependenciesName");
                    if (jsonObjectTempGet instanceof String) {
                        addTemp.setAddClassPathInDependenciesName((String) jsonObjectTempGet);
                        if (StringUtils.isNotEmpty(addTemp.getAddClassPathInDependenciesName())) {
                            addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                            addClassPathInDependencies.add(addTemp);
                        }
                    }
                }
            }
        }
        changePattern.setProjectAddClassPathInDependencies(addClassPathInDependencies);
    }

    /**
     * read app Build Gradle addApplyPlugin into StructAppAddApplyPlugin
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param appBuildGradleTemp to store data from wisehub-gradle app part
     */
    private void getAddApplyPlugin(JSONObject appBuildGradleTemp) {
        JSONArray addApplyPluginTemp = appBuildGradleTemp.getJSONArray("addApplyPlugin");
        List<StructAppAddApplyPlugin> addApplyPlugin = new ArrayList<>();
        for (int i = 0; i < addApplyPluginTemp.length(); i++) {
            Object addApplyPluginTempGet = addApplyPluginTemp.get(i);
            if (addApplyPluginTempGet instanceof JSONObject) {
                if (addApplyPluginTemp.get(i) instanceof JSONObject) {
                    JSONObject jsonObjectTemp = (JSONObject) addApplyPluginTemp.get(i);
                    StructAppAddApplyPlugin addTemp = new StructAppAddApplyPlugin();
                    if (jsonObjectTemp.keySet().contains("AddApplyPluginInApp")) {
                        Object jsonObjectTempGet = jsonObjectTemp.get("AddApplyPluginInApp");
                        if (jsonObjectTempGet instanceof String) {
                            Object jsonObjectTempGetApp = jsonObjectTemp.get("AddApplyPluginInApp");
                            if (jsonObjectTempGetApp instanceof String) {
                                addTemp.setAddApplyPluginInApp((String) jsonObjectTempGetApp);
                                if (StringUtils.isNotEmpty(addTemp.getAddApplyPluginInApp())) {
                                    addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                                    addApplyPlugin.add(addTemp);
                                }
                            }
                        }
                    }
                }
            }
        }
        changePattern.setAppAddApplyPlugin(addApplyPlugin);
    }

    /**
     * read app Build Gradle addMessage into StructAppAddMessage
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param appBuildGradleTemp to store data from wisehub-gradle app part
     */
    private void getAddMessage(JSONObject appBuildGradleTemp) {
        JSONArray addMessageTemp = appBuildGradleTemp.getJSONArray("addMessage");
        List<StructAppAddMessage> addMessage = new ArrayList<>();
        for (int i = 0; i < addMessageTemp.length(); i++) {
            if (addMessageTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) addMessageTemp.get(i);
                StructAppAddMessage addTemp = new StructAppAddMessage();
                if (jsonObjectTemp.keySet().contains("addMessageInDependenciesImplementation")) {
                    Object jsonObjectTempGet =
                            jsonObjectTemp.get("addMessageInDependenciesImplementation");
                    if (jsonObjectTempGet instanceof String) {
                        addTemp.setAddMessageInDependenciesImplementation((String) jsonObjectTempGet);
                        if (StringUtils.isNotEmpty(addTemp.getAddMessageInDependenciesImplementation())) {
                            addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                            addMessage.add(addTemp);
                        }
                    }
                }
            }
        }
        changePattern.setAppBuildGradleAddMessage(addMessage);
    }

    /**
     * read app Build Gradle deleteGmsApplyPlugin into StructAppDeleteGmsApplyPlugin
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param appBuildGradleTemp to store data from wisehub-gradle app part
     */
    private void getDeleteGmsApplyPlugin(JSONObject appBuildGradleTemp) {
        JSONArray deleteGmsApplyPluginTemp = appBuildGradleTemp.getJSONArray("deleteGmsApplyPlugin");
        List<StructAppDeleteGmsApplyPlugin> deleteGmsApplyPlugin = new ArrayList<>();
        for (int i = 0; i < deleteGmsApplyPluginTemp.length(); i++) {
            if (deleteGmsApplyPluginTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) deleteGmsApplyPluginTemp.get(i);
                StructAppDeleteGmsApplyPlugin addTemp = new StructAppDeleteGmsApplyPlugin();
                if (jsonObjectTemp.keySet().contains("deleteGmsApplyPluginName")) {
                    Object jsonObjectTempGet = jsonObjectTemp.get("deleteGmsApplyPluginName");
                    if (jsonObjectTempGet instanceof String) {
                        addTemp.setDeleteApplyPluginInApp((String) jsonObjectTempGet);
                        if (StringUtils.isNotEmpty(addTemp.getDeleteApplyPluginInApp())) {
                            addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                            deleteGmsApplyPlugin.add(addTemp);
                        }
                    }
                }
            }
        }
        changePattern.setAppDeleteGmsApplyPlugin(deleteGmsApplyPlugin);
    }

    /**
     * read app Build Gradle replace into StructAppReplace
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param appBuildGradleTemp to store data from wisehub-gradle app part
     */
    private void getReplace(JSONObject appBuildGradleTemp) {
        JSONArray appReplace = appBuildGradleTemp.getJSONArray("replace");
        List<StructAppReplace> appReplaces = new ArrayList<>();
        for (int i = 0; i < appReplace.length(); i++) {
            if (appReplace.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) appReplace.get(i);
                StructAppReplace addTemp = new StructAppReplace();
                if (jsonObjectTemp.keySet().contains("originGoogleName")) {
                    addTemp.setOriginGoogleName((String) jsonObjectTemp.get("originGoogleName"));
                    addTemp.setVersion((String) jsonObjectTemp.get("version"));
                    if (StringUtils.isNotEmpty(addTemp.getOriginGoogleName())) {
                        addTemp.setDescAuto(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc_auto")));
                        addTemp.setDescManual(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc_manual")));
                        List<String> listStringTemp = new ArrayList<>();
                        JSONArray listObjectTemp = jsonObjectTemp.getJSONArray("replaceHmsName");
                        for (int j = 0; j < listObjectTemp.length(); j++) {
                            Object listObjectTempGet = listObjectTemp.get(j);
                            if (listObjectTempGet instanceof String) {
                                listStringTemp.add((String) listObjectTempGet);
                            }
                        }
                        addTemp.setReplaceHmsName(listStringTemp);
                        appReplaces.add(addTemp);
                    }
                }
            }
        }
        changePattern.setAppBuildGradleReplace(appReplaces);
    }

    /**
     * read app Build Gradle deleteInDependencies into StructAppDeleteInDependencies
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param appBuildGradleTemp to store data from wisehub-gradle app part
     */
    private void getDeleteInDependencies(JSONObject appBuildGradleTemp) {
        JSONArray deleteInDependenciesTemp = appBuildGradleTemp.getJSONArray("deleteInDependencies");
        List<StructAppDeleteInDependencies> deleteInDependencies = new ArrayList<>();
        for (int i = 0; i < deleteInDependenciesTemp.length(); i++) {
            if (deleteInDependenciesTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) deleteInDependenciesTemp.get(i);
                StructAppDeleteInDependencies addTemp = new StructAppDeleteInDependencies();
                if (jsonObjectTemp.keySet().contains("deleteInDependenciesName")) {
                    Object deleteInDependenciesNameTemp =
                            jsonObjectTemp.get("deleteInDependenciesName");
                    if (deleteInDependenciesNameTemp instanceof String) {
                        addTemp.setDeleteClasspathInDependenciesName((String) deleteInDependenciesNameTemp);
                        if (StringUtils.isNotEmpty(addTemp.getDeleteClasspathInDependenciesName())) {
                            addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                            deleteInDependencies.add(addTemp);
                        }
                    }
                }
            }
        }
        changePattern.setAppDeleteInDependencies(deleteInDependencies);
    }

    /**
     * read app Build Gradle Aidl into StructAppAidl
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param appBuildGradleTemp to store data from wisehub-gradle app part
     */
    private void getAidl(JSONObject appBuildGradleTemp) {
        JSONArray appAidlTemp = appBuildGradleTemp.getJSONArray("Aidl");
        List<StructAppAidl> aidl = new ArrayList<>();
        for (int i = 0; i < appAidlTemp.length(); i++) {
            if (appAidlTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) appAidlTemp.get(i);
                StructAppAidl addTemp = new StructAppAidl();
                if (jsonObjectTemp.keySet().contains("AidlName")) {
                    if (jsonObjectTemp.get("AidlName") instanceof String) {
                        addTemp.setAidlName((String) jsonObjectTemp.get("AidlName"));
                        if (StringUtils.isNotEmpty(addTemp.getAidlName())) {
                            addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                            List<String> listStringTemp = new ArrayList<>();
                            JSONArray listObjectTemp =
                                    jsonObjectTemp.getJSONArray("AddImplementationInDependencies");
                            for (int j = 0; j < listObjectTemp.length(); j++) {
                                if (listObjectTemp.get(j) instanceof String) {
                                    Object listObjectTempGetJTemp = listObjectTemp.get(j);
                                    if (listObjectTempGetJTemp instanceof String) {
                                        listStringTemp.add((String) listObjectTempGetJTemp);
                                    }
                                }
                            }
                            addTemp.setAddImplementationInDependencies(listStringTemp);
                            aidl.add(addTemp);
                        }
                    }
                }
            }
        }
        changePattern.setAppAidls(aidl);
    }

    /**
     * read app Build Gradle addIndirectDependencies into StructAppAddIndirectDependencies
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param appBuildGradleTemp to store data from wisehub-gradle app part
     */
    private void getAddIndirectDependencies(JSONObject appBuildGradleTemp) {
        JSONArray addIndirectDependenciesTemp = appBuildGradleTemp.getJSONArray("addIndirectDependencies");
        List<StructAppAddIndirectDependencies> addIndirectDependencies = new ArrayList<>();
        for (int i = 0; i < addIndirectDependenciesTemp.length(); i++) {
            if (addIndirectDependenciesTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) addIndirectDependenciesTemp.get(i);
                StructAppAddIndirectDependencies addTemp = new StructAppAddIndirectDependencies();
                if (jsonObjectTemp.keySet().contains("originGoogleName")) {
                    addTemp.setOriginGoogleName((String) jsonObjectTemp.get("originGoogleName"));
                    if (StringUtils.isNotEmpty(addTemp.getOriginGoogleName())) {
                        addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                        addTemp.setDependencies(JsonUtil.toList(jsonObjectTemp.getJSONArray("dependencies")));
                        addIndirectDependencies.add(addTemp);
                    }
                }
            }
        }
        changePattern.setAppAddIndirectDependencies(addIndirectDependencies);
    }

    /**
     * read app Build Gradle addInDependencies into StructAppAddInDependencies
     * from wisehub-gradle/wisehub-gradle-hms
     *
     * @param appBuildGradleTemp to store data from wisehub-gradle app part
     */
    private void getAddInDependencies(JSONObject appBuildGradleTemp) {
        JSONArray addInDependenciesTemp = appBuildGradleTemp.getJSONArray("addInDependencies");
        List<StructAppAddInDependencies> addInDependencies = new ArrayList<>();
        for (int i = 0; i < addInDependenciesTemp.length(); i++) {
            if (addInDependenciesTemp.get(i) instanceof JSONObject) {
                JSONObject jsonObjectTemp = (JSONObject) addInDependenciesTemp.get(i);
                StructAppAddInDependencies addTemp = new StructAppAddInDependencies();
                if (jsonObjectTemp.keySet().contains("originGoogleName")) {
                    if (jsonObjectTemp.get("version") instanceof String) {
                        addTemp.setVersion((String) jsonObjectTemp.get("version"));
                        if (jsonObjectTemp.get("originGoogleName") instanceof String) {
                            addTemp.setOriginGoogleName((String) jsonObjectTemp.get("originGoogleName"));
                            if (StringUtils.isNotEmpty(addTemp.getOriginGoogleName())) {
                                addTemp.setDescAuto(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc_auto")));
                                addTemp.setDescManual(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc_manual")));
                                addTemp.setDependencies(JsonUtil.toList(
                                        jsonObjectTemp.getJSONArray("addDependenciesName")));
                                addInDependencies.add(addTemp);
                            }
                        }
                    }
                }
            }
        }
        changePattern.setAppAddInDependencies(addInDependencies);
    }

    /**
     * read addInSettingsGradle part into StructSettingGradle from wisehub-gradle/wisehub-gradle-hms
     *
     * @param settingsGradleJson to store data from wisehub-gradle settings gradle part
     */
    private void getAddInSettingsGradle(JSONObject settingsGradleJson) {
        StructSettingGradle addLineStructNew = new StructSettingGradle();
        JSONArray settingsGradleJsonArray = settingsGradleJson.getJSONArray("addInSettingsGradle");
        JSONObject addLineInSettingsGradleObject = (JSONObject) settingsGradleJsonArray.get(0);
        if (addLineInSettingsGradleObject.keySet().contains("addString")) {
            addLineStructNew.setAddString((String) addLineInSettingsGradleObject.get("addString"));
        }
        if (addLineInSettingsGradleObject.keySet().contains("desc")) {
            addLineStructNew.setDesc(JsonUtil.toMap(addLineInSettingsGradleObject.getJSONObject("desc")));
        }
        changePattern.setSettingGradle(addLineStructNew);
    }

    /**
     * read addSpecialInDependency part into StructSpecialAddInDependency from wisehub-gradle/wisehub-gradle-hms
     *
     * @param specialAddInDependencyJson to store data from wisehub-gradle settings gradle part
     */
    private void getAddSpecialInDependency(JSONObject specialAddInDependencyJson) {
        StructSpecialAdd addLineStructNew = new StructSpecialAdd();
        JSONArray settingsGradleJsonArray = specialAddInDependencyJson.getJSONArray("addSpecialInDependency");
        JSONObject addSpecialInGradleObject = (JSONObject) settingsGradleJsonArray.get(0);
        if (addSpecialInGradleObject.keySet().contains("addString")) {
            addLineStructNew.setAddString((String) addSpecialInGradleObject.get("addString"));
        }
        if (addSpecialInGradleObject.keySet().contains("desc")) {
            addLineStructNew.setDesc(JsonUtil.toMap(addSpecialInGradleObject.getJSONObject("desc")));
        }
        changePattern.setSpecialAddInDependency(addLineStructNew);
    }

    /**
     * read addSpecialInDependency part into StructSpecialAddInDependency from wisehub-gradle/wisehub-gradle-hms
     *
     * @param specialAddInDependencyJson to store data from wisehub-gradle settings gradle part
     */
    private void getAddSpecialInApp(JSONObject specialAddInDependencyJson) {
        StructSpecialAdd addLineStructNew = new StructSpecialAdd();
        JSONArray settingsGradleJsonArray = specialAddInDependencyJson.getJSONArray("addSpecialInApply");
        JSONObject addSpecialInGradleObject = (JSONObject) settingsGradleJsonArray.get(0);
        if (addSpecialInGradleObject.keySet().contains("addString")) {
            addLineStructNew.setAddString((String) addSpecialInGradleObject.get("addString"));
        }
        if (addSpecialInGradleObject.keySet().contains("desc")) {
            addLineStructNew.setDesc(JsonUtil.toMap(addSpecialInGradleObject.getJSONObject("desc")));
        }
        changePattern.setSpecialAddInApp(addLineStructNew);
    }
}
