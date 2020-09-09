/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.core.engine.fixbot;

import com.huawei.codebot.framework.dispatch.model.DefectFile;
import com.huawei.codebot.framework.dispatch.model.UIDefectInstance;
import com.huawei.codebot.framework.model.DefectBlockIndex;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.ProjectInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.fixbot.model.XmsSetting;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.ApiKey;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.FixbotApiInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.MappingApiInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.clazz.MappingClassJson4Auto;
import com.huawei.hms.convertor.core.engine.fixbot.model.clazz.MappingClassJson4Manual;
import com.huawei.hms.convertor.core.engine.fixbot.model.field.MappingFieldJson4Auto;
import com.huawei.hms.convertor.core.engine.fixbot.model.field.MappingFieldJson4Manual;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitSdkVersion;
import com.huawei.hms.convertor.core.engine.fixbot.model.method.MappingMethodJson4Auto;
import com.huawei.hms.convertor.core.engine.fixbot.model.method.MappingMethodJson4Manual;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.mapping.MappingConstant;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.plugin.PluginPathObtainService;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.util.PropertyUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This is the Fixbot result file parser that parse the
 * Fixbot result files (DefectFiles.json/DefectInstances.json)
 * to generate the conversion list and summary information.
 *
 * @since 2020-03-20
 */
@Getter
@Setter
@Slf4j
public final class FixbotResultParser {
    private static final String RECOMMENDED_GMS_SDK_VERSION = "The recommended GMS SDK version is ";

    private static final String POINTER = ".";

    private static final String JDK_COMPILE_VERSION = "sourceCompatibility";

    private String projectBasePath;

    private String allianceDomain;

    private String defaultDomain;

    private RoutePolicy routePolicy;

    private ProjectInfo projectInfo;

    private XmsSetting xmsSetting;

    private String type;

    private int fontSize;

    private String folderName;

    private String resultPath;

    private ServiceLoader<PluginPathObtainService> pluginPathObtainService;

    private List<DefectFile> defectFiles;

    private Map<ApiKey, MappingApiInfo> methodKey2MappingMethodMap4AutoGaddH;

    private Map<ApiKey, MappingApiInfo> methodKey2MappingMethodMap4AutoG2H;

    private Map<ApiKey, MappingApiInfo> methodKey2MappingMethodMap4ManualGaddH;

    private Map<ApiKey, MappingApiInfo> methodKey2MappingMethodMap4ManualG2H;

    private Map<ApiKey, MappingApiInfo> classKey2MappingClassMap4AutoGaddH;

    private Map<ApiKey, MappingApiInfo> classKey2MappingClassMap4AutoG2H;

    private Map<ApiKey, MappingApiInfo> classKey2MappingClassMap4ManualGaddH;

    private Map<ApiKey, MappingApiInfo> classKey2MappingClassMap4ManualG2H;

    private Map<ApiKey, MappingApiInfo> fieldKey2MappingFieldMap4AutoGaddH;

    private Map<ApiKey, MappingApiInfo> fieldKey2MappingFieldMap4AutoG2H;

    private Map<ApiKey, MappingApiInfo> fieldKey2MappingFieldMap4ManualGaddH;

    private Map<ApiKey, MappingApiInfo> fieldKey2MappingFieldMap4ManualG2H;

    /**
     * Kit not included: any sdkVersion illegal
     */
    private Map<String, KitSdkVersion> kitSdkVersionMap;

    private List<String> allKits;

    private List<String> allDependencies;

    /**
     * Kit not included: Common, Other
     */
    private TreeMap<String, List<FixbotApiInfo>> kit2FixbotMethodsMap;

    /**
     * Kit not included: Common, Other
     */
    private TreeMap<String, List<FixbotApiInfo>> kit2FixbotClassesMap;

    /**
     * Kit not included: Common, Other
     */
    private TreeMap<String, List<FixbotApiInfo>> kit2FixbotFieldsMap;

    private Map<String, List<ConversionPointDesc>> blockId2DescriptionsMap;

    public FixbotResultParser(String projectBasePath, String allianceDomain, RoutePolicy routePolicy, String type,
        int fontSize) {
        this.allianceDomain = allianceDomain;
        defaultDomain = PropertyUtil.readProperty("default_alliance_domain");
        this.routePolicy = routePolicy;
        this.projectBasePath = projectBasePath;
        this.type = type;
        this.fontSize = fontSize;
        methodKey2MappingMethodMap4AutoGaddH = new HashMap<>();
        methodKey2MappingMethodMap4AutoG2H = new HashMap<>();
        methodKey2MappingMethodMap4ManualGaddH = new HashMap<>();
        methodKey2MappingMethodMap4ManualG2H = new HashMap<>();
        classKey2MappingClassMap4AutoGaddH = new HashMap<>();
        classKey2MappingClassMap4AutoG2H = new HashMap<>();
        classKey2MappingClassMap4ManualGaddH = new HashMap<>();
        classKey2MappingClassMap4ManualG2H = new HashMap<>();
        fieldKey2MappingFieldMap4AutoGaddH = new HashMap<>();
        fieldKey2MappingFieldMap4AutoG2H = new HashMap<>();
        fieldKey2MappingFieldMap4ManualGaddH = new HashMap<>();
        fieldKey2MappingFieldMap4ManualG2H = new HashMap<>();
        kitSdkVersionMap = new HashMap<>();
        allKits = new ArrayList<>();
        allDependencies = new ArrayList<>();
        kit2FixbotMethodsMap = new TreeMap<>();
        kit2FixbotClassesMap = new TreeMap<>();
        kit2FixbotFieldsMap = new TreeMap<>();
        blockId2DescriptionsMap = new HashMap<>();
        folderName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");

        pluginPathObtainService = ServiceLoader.load(PluginPathObtainService.class, getClass().getClassLoader());
    }

    private static class KitSdkVersionType extends TypeReference<HashMap<String, KitSdkVersion>> {
    }

    public boolean parseFixbotResult() {
        try {
            resultPath = FileUtil.unifyToUnixFileSeparator(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + folderName);
            String defectFilesPath =
                resultPath + Constant.UNIX_FILE_SEPARATOR + ProjectConstants.Result.DEFECT_FILES_JSON;
            String defectInstancesPath =
                resultPath + Constant.UNIX_FILE_SEPARATOR + ProjectConstants.Result.DEFECT_INSTANCES_JSON;
            String projectInfoPath =
                resultPath + Constant.UNIX_FILE_SEPARATOR + ProjectConstants.Result.PROJECT_INFO_JSON;
            String xmsSettingPath =
                resultPath + Constant.UNIX_FILE_SEPARATOR + ProjectConstants.Result.XMS_SETTING_JSON;

            // Extract DefectFile and UIDefectInstance form engine result files.
            defectFiles = getResultList(defectFilesPath, DefectFile.class);
            List<UIDefectInstance> defectInstances = getResultList(defectInstancesPath, UIDefectInstance.class);

            if (projectInfoFileExists(projectInfoPath)) {
                projectInfo = getResult(projectInfoPath, ProjectInfo.class);
            } else {
                projectInfo = new ProjectInfo(System.getProperty("java.version"));
            }

            xmsSetting = getResult(xmsSettingPath, XmsSetting.class);

            // Parse defect descriptions.
            parseDefectInstances(defectInstances);

            JSONObject mappingJson4AutoGaddH =
                getMappingJsonFromMappingFile(MappingConstant.MappingFile.ADD_HMS_AUTO_JSON_FILE_NAME);
            JSONObject mappingJson4AutoG2H =
                getMappingJsonFromMappingFile(MappingConstant.MappingFile.TO_HMS_AUTO_JSON_FILE_NAME);
            JSONObject mappingJson4ManualGaddH =
                getMappingJsonFromMappingFile(MappingConstant.MappingFile.ADD_HMS_MANUAL_JSON_FILE_NAME);
            JSONObject mappingJson4ManualG2H =
                getMappingJsonFromMappingFile(MappingConstant.MappingFile.TO_HMS_MANUAL_JSON_FILE_NAME);

            getAutoMethods(mappingJson4AutoGaddH, methodKey2MappingMethodMap4AutoGaddH);
            getAutoMethods(mappingJson4AutoG2H, methodKey2MappingMethodMap4AutoG2H);
            getManualMethods(mappingJson4ManualGaddH, methodKey2MappingMethodMap4ManualGaddH);
            getManualMethods(mappingJson4ManualG2H, methodKey2MappingMethodMap4ManualG2H);
            log.info(
                "methodKey2MappingMethodMap4AutoGaddH size: {}, methodKey2MappingMethodMap4AutoG2H size: {}, methodKey2MappingMethodMap4ManualGaddH size: {}, methodKey2MappingMethodMap4ManualG2H: {}.",
                methodKey2MappingMethodMap4AutoGaddH.size(), methodKey2MappingMethodMap4AutoG2H.size(),
                methodKey2MappingMethodMap4ManualGaddH.size(), methodKey2MappingMethodMap4ManualG2H.size());

            getAutoClasses(mappingJson4AutoGaddH, classKey2MappingClassMap4AutoGaddH);
            getAutoClasses(mappingJson4AutoG2H, classKey2MappingClassMap4AutoG2H);
            getManualClasses(mappingJson4ManualGaddH, classKey2MappingClassMap4ManualGaddH);
            getManualClasses(mappingJson4ManualG2H, classKey2MappingClassMap4ManualG2H);
            log.info(
                "classKey2MappingClassMap4AutoGaddH size: {}, classKey2MappingClassMap4AutoG2H size: {}, classKey2MappingClassMap4ManualGaddH size: {}, classKey2MappingClassMap4ManualG2H: {}.",
                classKey2MappingClassMap4AutoGaddH.size(), classKey2MappingClassMap4AutoG2H.size(),
                classKey2MappingClassMap4ManualGaddH.size(), classKey2MappingClassMap4ManualG2H.size());

            getAutoFields(mappingJson4AutoGaddH, fieldKey2MappingFieldMap4AutoGaddH);
            getAutoFields(mappingJson4AutoG2H, fieldKey2MappingFieldMap4AutoG2H);
            getManualFields(mappingJson4ManualGaddH, fieldKey2MappingFieldMap4ManualGaddH);
            getManualFields(mappingJson4ManualG2H, fieldKey2MappingFieldMap4ManualG2H);

            log.info(
                "fieldKey2MappingFieldMap4AutoGaddH size: {}, fieldKey2MappingFieldMap4AutoG2H size: {}, fieldKey2MappingFieldMap4ManualGaddH size: {}, fieldKey2MappingFieldMap4ManualG2H: {}.",
                fieldKey2MappingFieldMap4AutoGaddH.size(), fieldKey2MappingFieldMap4AutoG2H.size(),
                fieldKey2MappingFieldMap4ManualGaddH.size(), fieldKey2MappingFieldMap4ManualG2H.size());

            buildKitSdkVersions();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private boolean projectInfoFileExists(String projectInfoPath) {
        if (!new File(projectInfoPath).exists()) {
            return false;
        }
        return true;
    }

    private void parseDefectInstances(List<UIDefectInstance> defectInstances) throws JSONException {
        if (CollectionUtils.isEmpty(defectInstances)) {
            log.warn("The defect instances are empty.");
            return;
        }

        TreeMap<String, Set<FixbotApiInfo>> kit2FixbotMethodSetMap = new TreeMap<>();
        TreeMap<String, Set<FixbotApiInfo>> kit2FixbotClassSetMap = new TreeMap<>();
        TreeMap<String, Set<FixbotApiInfo>> kit2FixbotFieldSetMap = new TreeMap<>();
        for (UIDefectInstance defectInstance : defectInstances) {
            // Get the string of one defect instance.
            String defectDescriptionString = defectInstance.getDefectDescription();
            if (StringUtils.isEmpty(defectDescriptionString)) {
                continue;
            }

            // Converts description string to 'ConversionPointDesc' object.
            List<ConversionPointDesc> descriptions =
                JSONArray.parseArray(defectDescriptionString, ConversionPointDesc.class);
            for (DefectBlockIndex block : defectInstance.getDefectBlocks()) {
                blockId2DescriptionsMap.put(block.getDefectBlockId(), descriptions);
            }

            for (ConversionPointDesc description : descriptions) {
                // Generate a description indicating that the GMS needs to be upgraded.
                if (description.isUpdate()) {
                    description.setText(
                        description.getText() + RECOMMENDED_GMS_SDK_VERSION + description.getVersion() + POINTER);
                }

                // Generate a complete URL.
                String url = replaceGrsAllianceDomain(description.getUrl());
                description.setUrl(url);

                // Get the GMS API information.
                String kit = description.getKit();
                if (StringUtils.isEmpty(kit)) {
                    continue;
                }
                if (!allKits.contains(kit)) {
                    allKits.add(kit);
                }

                extractKit2FixbotApiInfo(description.getMethodName(), description, kit, kit2FixbotMethodSetMap);
                extractKit2FixbotApiInfo(description.getClassName(), description, kit, kit2FixbotClassSetMap);
                extractKit2FixbotApiInfo(description.getFieldName(), description, kit, kit2FixbotFieldSetMap);

                // Get the GMS dependency information.
                String dependency = description.getDependencyName();
                if (StringUtils.isEmpty(dependency)) {
                    continue;
                }
                if (!allDependencies.contains(dependency)) {
                    allDependencies.add(dependency);
                }
            }
        }
        log.info("allKits size is {}, allDependencies size is {}.", allKits.size(), allDependencies.size());

        buildKit2FixbotApisMap(kit2FixbotMethodSetMap, kit2FixbotMethodsMap);
        buildKit2FixbotApisMap(kit2FixbotClassSetMap, kit2FixbotClassesMap);
        buildKit2FixbotApisMap(kit2FixbotFieldSetMap, kit2FixbotFieldsMap);
        log.info("kit2FixbotMethodsMap size: {}, kit2FixbotClassesMap size: {}, kit2FixbotFieldsMap size: {}.",
            kit2FixbotMethodsMap.size(), kit2FixbotClassesMap.size(), kit2FixbotFieldsMap.size());
    }

    private void extractKit2FixbotApiInfo(String apiName, ConversionPointDesc description, String kit,
        TreeMap<String, Set<FixbotApiInfo>> kit2FixbotApiSetMap) {
        if (kit.equals(KitsConstants.COMMON) || kit.equals(KitsConstants.OTHER)) {
            return;
        }
        kit2FixbotApiSetMap.putIfAbsent(kit, new LinkedHashSet<>());

        if (StringUtils.isEmpty(apiName)) {
            return;
        }
        // trim GMS name in desc
        String oldNameInDesc = apiName.trim();
        FixbotApiInfo fixbotApi = new FixbotApiInfo(oldNameInDesc, description.getStatus(), description.isSupport());
        kit2FixbotApiSetMap.get(kit).add(fixbotApi);
    }

    private void getAutoMethods(JSONObject mappingJson, Map<ApiKey, MappingApiInfo> methodKey2MappingMethodMap)
        throws JSONException {
        Object methodObjs = mappingJson.get(MappingConstant.MappingFileKey.AUTO_METHODS);
        List<MappingApiInfo> mappingMethods = new ArrayList<>();
        List<MappingMethodJson4Auto> mappingMethodJsons =
            JSON.parseArray(methodObjs.toString(), MappingMethodJson4Auto.class);
        for (MappingMethodJson4Auto mappingMethodJson : mappingMethodJsons) {
            ConversionPointDesc desc = mappingMethodJson.getDesc();
            if (StringUtils.isEmpty(desc.getMethodName())) {
                continue;
            }

            MappingApiInfo mappingMethod = new MappingApiInfo();
            // trim GMS method name in desc
            mappingMethod.setOldNameInDesc(desc.getMethodName().trim());
            mappingMethod.setUrl(replaceGrsAllianceDomain(desc.getUrl()));
            mappingMethod.setKit(desc.getKit());
            mappingMethod.setConvertStatus(desc.getStatus());
            mappingMethod.setSupport(desc.isSupport());
            mappingMethod.setDependencyName(desc.getDependencyName());
            mappingMethod.setOldName(mappingMethodJson.getOldMethodName());
            mappingMethod.setNewName(mappingMethodJson.getNewMethodName());
            mappingMethods.add(mappingMethod);
        }

        buildApiKey2MappingApiMap(mappingMethods, methodKey2MappingMethodMap);
    }

    private void getManualMethods(JSONObject mappingJson, Map<ApiKey, MappingApiInfo> methodKey2MappingMethodMap)
        throws JSONException {
        Object methodObjs = mappingJson.get(MappingConstant.MappingFileKey.MANUAL_METHODS);
        List<MappingApiInfo> mappingMethods = new ArrayList<>();
        List<MappingMethodJson4Manual> mappingMethodJsons =
            JSON.parseArray(methodObjs.toString(), MappingMethodJson4Manual.class);
        for (MappingMethodJson4Manual mappingMethodJson : mappingMethodJsons) {
            ConversionPointDesc desc = mappingMethodJson.getDesc();
            if (StringUtils.isEmpty(desc.getMethodName())) {
                continue;
            }

            MappingApiInfo mappingMethod = new MappingApiInfo();
            // trim GMS method name in desc
            mappingMethod.setOldNameInDesc(desc.getMethodName().trim());
            mappingMethod.setUrl(replaceGrsAllianceDomain(desc.getUrl()));
            mappingMethod.setKit(desc.getKit());
            mappingMethod.setConvertStatus(desc.getStatus());
            mappingMethod.setSupport(desc.isSupport());
            mappingMethod.setOldName(mappingMethodJson.getMethodName());
            mappingMethods.add(mappingMethod);
        }

        buildApiKey2MappingApiMap(mappingMethods, methodKey2MappingMethodMap);
    }

    private void getAutoClasses(JSONObject mappingJson, Map<ApiKey, MappingApiInfo> classKey2MappingClassMap)
        throws JSONException {
        Object classObjs = mappingJson.get(MappingConstant.MappingFileKey.AUTO_CLASSES);
        List<MappingApiInfo> mappingClasses = new ArrayList<>();
        List<MappingClassJson4Auto> mappingClassJsons =
            JSON.parseArray(classObjs.toString(), MappingClassJson4Auto.class);
        for (MappingClassJson4Auto mappingClassJson : mappingClassJsons) {
            ConversionPointDesc desc = mappingClassJson.getDesc();
            if (StringUtils.isEmpty(desc.getClassName())) {
                continue;
            }

            MappingApiInfo mappingClass = new MappingApiInfo();
            // trim GMS class name in desc
            mappingClass.setOldNameInDesc(desc.getClassName().trim());
            mappingClass.setUrl(replaceGrsAllianceDomain(desc.getUrl()));
            mappingClass.setKit(desc.getKit());
            mappingClass.setConvertStatus(desc.getStatus());
            mappingClass.setSupport(desc.isSupport());
            mappingClass.setDependencyName(desc.getDependencyName());
            mappingClass.setOldName(mappingClassJson.getOldClassName());
            mappingClass.setNewName(mappingClassJson.getNewClassName());
            mappingClasses.add(mappingClass);
        }

        buildApiKey2MappingApiMap(mappingClasses, classKey2MappingClassMap);
    }

    private void getManualClasses(JSONObject mappingJson, Map<ApiKey, MappingApiInfo> classKey2MappingClassMap)
        throws JSONException {
        Object classObjs = mappingJson.get(MappingConstant.MappingFileKey.MANUAL_CLASSES);
        List<MappingApiInfo> mappingClasses = new ArrayList<>();
        List<MappingClassJson4Manual> mappingClassJsons =
            JSON.parseArray(classObjs.toString(), MappingClassJson4Manual.class);
        for (MappingClassJson4Manual mappingClassJson : mappingClassJsons) {
            ConversionPointDesc desc = mappingClassJson.getDesc();
            if (StringUtils.isEmpty(desc.getClassName())) {
                continue;
            }

            MappingApiInfo mappingClass = new MappingApiInfo();
            // trim GMS class name in desc
            mappingClass.setOldNameInDesc(desc.getClassName().trim());
            mappingClass.setUrl(replaceGrsAllianceDomain(desc.getUrl()));
            mappingClass.setKit(desc.getKit());
            mappingClass.setConvertStatus(desc.getStatus());
            mappingClass.setSupport(desc.isSupport());
            mappingClass.setOldName(mappingClassJson.getClassName());
            mappingClasses.add(mappingClass);
        }

        buildApiKey2MappingApiMap(mappingClasses, classKey2MappingClassMap);
    }

    private void getAutoFields(JSONObject mappingJson, Map<ApiKey, MappingApiInfo> fieldKey2MappingFieldMap)
        throws JSONException {
        Object fieldObjs = mappingJson.get(MappingConstant.MappingFileKey.AUTO_FIELDS);
        List<MappingApiInfo> mappingFields = new ArrayList<>();
        List<MappingFieldJson4Auto> mappingFieldJsons =
            JSON.parseArray(fieldObjs.toString(), MappingFieldJson4Auto.class);
        for (MappingFieldJson4Auto mappingFieldJson : mappingFieldJsons) {
            ConversionPointDesc desc = mappingFieldJson.getDesc();
            if (StringUtils.isEmpty(desc.getFieldName())) {
                continue;
            }

            MappingApiInfo mappingField = new MappingApiInfo();
            // trim GMS field name in desc
            mappingField.setOldNameInDesc(desc.getFieldName().trim());
            mappingField.setUrl(replaceGrsAllianceDomain(desc.getUrl()));
            mappingField.setKit(desc.getKit());
            mappingField.setConvertStatus(desc.getStatus());
            mappingField.setSupport(desc.isSupport());
            mappingField.setDependencyName(desc.getDependencyName());
            mappingField.setOldName(mappingFieldJson.getOldFieldName());
            mappingField.setNewName(mappingFieldJson.getNewFieldName());
            mappingFields.add(mappingField);
        }

        buildApiKey2MappingApiMap(mappingFields, fieldKey2MappingFieldMap);
    }

    private void getManualFields(JSONObject mappingJson, Map<ApiKey, MappingApiInfo> fieldKey2MappingFieldMap)
        throws JSONException {
        Object fieldObjs = mappingJson.get(MappingConstant.MappingFileKey.MANUAL_FIELDS);
        List<MappingApiInfo> mappingFields = new ArrayList<>();
        List<MappingFieldJson4Manual> mappingFieldJsons =
            JSON.parseArray(fieldObjs.toString(), MappingFieldJson4Manual.class);
        for (MappingFieldJson4Manual mappingFieldJson : mappingFieldJsons) {
            ConversionPointDesc desc = mappingFieldJson.getDesc();
            if (StringUtils.isEmpty(desc.getFieldName())) {
                continue;
            }

            MappingApiInfo mappingField = new MappingApiInfo();
            // trim GMS field name in desc
            mappingField.setOldNameInDesc(desc.getFieldName().trim());
            mappingField.setUrl(replaceGrsAllianceDomain(desc.getUrl()));
            mappingField.setKit(desc.getKit());
            mappingField.setConvertStatus(desc.getStatus());
            mappingField.setSupport(desc.isSupport());
            mappingField.setOldName(mappingFieldJson.getFieldName());
            mappingFields.add(mappingField);
        }

        buildApiKey2MappingApiMap(mappingFields, fieldKey2MappingFieldMap);
    }

    private void buildKitSdkVersions() {
        String kitSdkVersionStr;
        try (
            InputStream inputStream =
                getClass().getResourceAsStream(ProjectConstants.KitSdkVersionConfig.KIT_SDK_VERSION_JSON_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            kitSdkVersionStr = reader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            log.error("read kitSdkVersion config fail, exception: {}.", e.getMessage());
            return;
        }

        kitSdkVersionMap = JSON.parseObject(kitSdkVersionStr, new KitSdkVersionType());
        log.info("kitSdkVersionMap size: {}.", kitSdkVersionMap.size());
    }

    private JSONObject getMappingJsonFromMappingFile(String configFileName) throws IOException {
        String pluginPackagePath = pluginPathObtainService.iterator().next().getPluginPackagePath();
        String configFilePath = pluginPackagePath + PluginConstant.PluginPackageDir.CONFIG_DIR + configFileName;

        return getResultList(FileUtil.unifyToUnixFileSeparator(configFilePath));
    }

    private <T> List<T> getResultList(String resultFilePath, Class<T> clazz) throws IOException, JSONException {
        if (resultFilePath == null || Files.notExists(Paths.get(resultFilePath))) {
            throw new NoSuchFileException("No result file generated! resultFilePath = " + resultFilePath);
        }

        String resultString = FileUtil.readToString(resultFilePath, StandardCharsets.UTF_8.toString());
        return JSON.parseArray(resultString, clazz);
    }

    private JSONObject getResultList(String resultFilePath) throws IOException, JSONException {
        if (resultFilePath == null || !new File(resultFilePath).exists()) {
            throw new NoSuchFileException("No result file generated! resultFilePath = " + resultFilePath);
        }

        String resultString = FileUtil.readToString(resultFilePath, StandardCharsets.UTF_8.toString());
        return JSON.parseObject(resultString);
    }

    private <T> T getResult(String resultFilePath, Class<T> clazz) throws IOException, JSONException {
        String resultString = FileUtil.readToString(resultFilePath, StandardCharsets.UTF_8.toString());
        return JSON.parseObject(resultString, clazz);
    }

    private void buildApiKey2MappingApiMap(List<MappingApiInfo> mappingApis,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap) {
        mappingApis.forEach(mappingApi -> {
            ApiKey apiKey =
                ApiKey.builder().kit(mappingApi.getKit()).oldNameInDesc(mappingApi.getOldNameInDesc()).build();
            apiKey2MappingApiMap.put(apiKey, mappingApi);
        });
    }

    private String replaceGrsAllianceDomain(String url) {
        if (!StringUtils.isEmpty(url) && !StringUtils.isEmpty(defaultDomain) && url.startsWith(defaultDomain)) {
            return url.replace(defaultDomain, allianceDomain);
        }
        return url;
    }

    private void buildKit2FixbotApisMap(TreeMap<String, Set<FixbotApiInfo>> kit2FixbotApiSetMap,
        TreeMap<String, List<FixbotApiInfo>> kit2FixbotApisMap) {
        kit2FixbotApiSetMap.forEach((kit, apiSet) -> {
            if (!apiSet.isEmpty()) {
                kit2FixbotApisMap.put(kit, new ArrayList<>(apiSet));
            }
        });
    }
}
