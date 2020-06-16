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
import com.huawei.hms.convertor.core.engine.fixbot.model.AutoMethod;
import com.huawei.hms.convertor.core.engine.fixbot.model.ManualMethod;
import com.huawei.hms.convertor.core.engine.fixbot.model.MethodItem;
import com.huawei.hms.convertor.core.engine.fixbot.model.ParseResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hms.convertor.util.PropertyUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

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

    private static final String AUTO_METHODS = "autoMethods";

    private static final String MANUAL_METHODS = "manualMethods";

    private String projectBasePath;

    private String allianceDomain;

    private String defaultDomain;

    private RoutePolicy routePolicy;

    private String type;

    private int fontSize;

    private String folderName;

    private String resultPath;

    private ServiceLoader<FixbotExtractService> fixbotExtractService;

    private List<DefectFile> defectFiles;

    private List<ParseResult> addHmsAutoMethods;

    private List<ParseResult> toHmsAutoMethods;

    private List<ParseResult> addHmsManualMethods;

    private List<ParseResult> toHmsManualMethods;

    private List<String> allKits = new ArrayList<>();

    private List<String> allDependencies = new ArrayList<>();

    private TreeMap<String, List<MethodItem>> kit2Methods = new TreeMap<>();

    private Map<String, List<ConversionPointDesc>> id2DescriptionsMap = new HashMap<>();

    public FixbotResultParser(String projectBasePath, String allianceDomain, RoutePolicy routePolicy, String type,
        int fontSize) {
        this.allianceDomain = allianceDomain;
        this.routePolicy = routePolicy;
        this.projectBasePath = projectBasePath;
        this.type = type;
        this.fontSize = fontSize;
        this.folderName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");

        fixbotExtractService = ServiceLoader.load(FixbotExtractService.class, getClass().getClassLoader());
    }

    public boolean parseFixbotResult() {
        try {
            resultPath = (Constant.PLUGIN_CACHE_PATH + folderName).replace("\\", "/");
            String defectFilesPath = resultPath + "/" + ProjectConstants.Result.DEFECT_FILES_JSON;
            String defectInstancesPath = resultPath + "/" + ProjectConstants.Result.DEFECT_INSTANCES_JSON;

            // Extract DefectFile and UIDefectInstance form engine result files.
            defectFiles = getResultList(defectFilesPath, DefectFile.class);
            List<UIDefectInstance> defectInstances = getResultList(defectInstancesPath, UIDefectInstance.class);

            // Parse defect descriptions.
            parseDefectInstances(defectInstances);

            // Extract information about all methods from the mapping files.
            addHmsAutoMethods = getAutoMethods(ProjectConstants.Mapping.ADD_HMS_AUTO_JSON_FILE);
            toHmsAutoMethods = getAutoMethods(ProjectConstants.Mapping.TO_HMS_AUTO_JSON_FILE);
            addHmsManualMethods = getManualMethods(ProjectConstants.Mapping.ADD_HMS_MANUAL_JSON_FILE);
            toHmsManualMethods = getManualMethods(ProjectConstants.Mapping.TO_HMS_MANUAL_JSON_FILE);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private void parseDefectInstances(List<UIDefectInstance> defectInstances) throws JSONException {
        if (defectInstances == null) {
            log.warn("The defect descriptions list is null");
            return;
        }

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
                id2DescriptionsMap.put(block.getDefectBlockId(), descriptions);
            }

            for (ConversionPointDesc description : descriptions) {
                // Generate a description indicating that the GMS needs to be upgraded.
                if (description.isUpdate()) {
                    description.setText(
                        description.getText() + RECOMMENDED_GMS_SDK_VERSION + description.getVersion() + POINTER);
                }

                // Generate a complete URL.
                String url = description.getUrl();
                if (StringUtils.isEmpty(defaultDomain)) {
                    defaultDomain = PropertyUtil.readProperty("default_alliance_domain");
                }
                if (!StringUtils.isEmpty(url) && !StringUtils.isEmpty(defaultDomain) && url.startsWith(defaultDomain)) {
                    description.setUrl(url.replace(defaultDomain, allianceDomain));
                }

                // Get the GMS API information.
                String kit = description.getKit();
                if (StringUtils.isEmpty(kit)) {
                    continue;
                }
                if (!allKits.contains(kit)) {
                    allKits.add(kit);
                }

                // Extract kit-methods information.
                extractKit2MethodInfo(description, kit);

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
        log.info("allKits size is {}，allDependencies size is {}", allKits.size(), allDependencies.size());
    }

    private void extractKit2MethodInfo(ConversionPointDesc description, String kit) {
        String methodName = description.getMethodName();
        MethodItem methodItem = null;
        if (!StringUtils.isEmpty(methodName)) {
            methodItem = new MethodItem(methodName, description.getStatus(), description.isSupport());
        }

        if (kit2Methods.containsKey(kit)) {
            // Add the method item when the key-value pair exists.
            List<MethodItem> methodItems = kit2Methods.get(kit);
            List<String> methodNames = new ArrayList<>();
            methodItems.forEach(item -> methodNames.add(item.getMethodName()));

            if (methodName != null && methodItem != null && !methodNames.contains(methodItem.getMethodName())) {
                methodItems.add(methodItem);
            }
        } else {
            // If the key-value pair does not exist, add it.
            List<MethodItem> methodItems = new ArrayList<>();
            if (methodName != null ) {
                methodItems.add(methodItem);
            }
            if (!kit.equals(KitsConstants.COMMON) && !kit.equals(KitsConstants.OTHER)) {
                kit2Methods.put(kit, methodItems);
            }
        }
    }

    private List<ParseResult> getAutoMethods(String configFileName) throws IOException, JSONException {
        List<ParseResult> results = new ArrayList<>();
        String pluginPackagePath = fixbotExtractService.iterator().next().getPluginPackagePath();
        String configFilePath = pluginPackagePath + "/lib/config/" + configFileName;

        // Load the information about all auto-methods in the mapping.
        JSONObject wiseHubs = getResultList(configFilePath.replace("\\", Constant.SEPARATOR));
        Object list = wiseHubs.get(AUTO_METHODS);
        List<AutoMethod> autoMethods = JSON.parseArray(list.toString(), AutoMethod.class);
        for (AutoMethod autoMethod : autoMethods) {
            ParseResult parseResult = new ParseResult();
            parseResult.setMethodName(autoMethod.getDesc().getMethodName());
            parseResult.setKit(autoMethod.getDesc().getKit());
            parseResult.setConvertStatus(autoMethod.getDesc().getStatus());
            parseResult.setSupport(autoMethod.getDesc().isSupport());
            parseResult.setDependencyName(autoMethod.getDesc().getDependencyName());
            results.add(parseResult);
        }
        return results;
    }

    private List<ParseResult> getManualMethods(String configFileName) throws IOException, JSONException {
        List<ParseResult> results = new ArrayList<>();
        String pluginPackagePath = fixbotExtractService.iterator().next().getPluginPackagePath();
        String configFilePath = pluginPackagePath + "/lib/config/" + configFileName;

        // Load the information about all manual-methods in the mapping.
        JSONObject wiseHubs = getResultList(configFilePath.replace("\\", Constant.SEPARATOR));
        Object list = wiseHubs.get(MANUAL_METHODS);
        List<ManualMethod> manualMethods = JSON.parseArray(list.toString(), ManualMethod.class);
        for (ManualMethod manualMethod : manualMethods) {
            ParseResult parseResult = new ParseResult();
            parseResult.setMethodName(manualMethod.getDesc().getMethodName());
            parseResult.setKit(manualMethod.getDesc().getKit());
            parseResult.setConvertStatus(manualMethod.getDesc().getStatus());
            parseResult.setSupport(manualMethod.getDesc().isSupport());
            results.add(parseResult);
        }
        return results;
    }

    private <T> List<T> getResultList(String resultFilePath, Class<T> clazz) throws IOException, JSONException {
        if (resultFilePath == null || Files.notExists(Paths.get(resultFilePath))) {
            throw new NoSuchFileException("No result file generated! resultFilePath = " + resultFilePath);
        }

        String resultString = FileUtil.readToString(resultFilePath, Constant.UTF8);
        return JSON.parseArray(resultString, clazz);
    }

    private JSONObject getResultList(String resultFilePath) throws IOException, JSONException {
        if (resultFilePath == null || !new File(resultFilePath).exists()) {
            throw new NoSuchFileException("No result file generated! resultFilePath = " + resultFilePath);
        }

        String resultString = FileUtil.readToString(resultFilePath, Constant.UTF8);
        return JSON.parseObject(resultString);
    }
}
