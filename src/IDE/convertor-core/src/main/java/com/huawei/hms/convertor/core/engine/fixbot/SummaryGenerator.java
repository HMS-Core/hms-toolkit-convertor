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

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.ProjectInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.ApiAnalyseResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.ApiKey;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.FixbotApiInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.MappingApiInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitApiStatisticsResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitSdkVersion;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitStatisticsResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.project.ProjectApiStatisticsResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.project.ProjectStatisticsResult;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.core.result.summary.SummaryConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.XmsGenerateService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.KitUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Summary generator
 *
 * @since 2020-02-24
 */
@Slf4j
@Setter
@Getter
public final class SummaryGenerator {
    private static final int MAX_UNSUPPORTED_API_ONE_LINE = 3;

    private static final int LARGE_FONTSIZE = 14;

    private static final int MORE_INCOMPATIBLE_API_ONE_LINE = 4;

    private static final int LESS_INCOMPATIBLE_API_ONE_LINE = 3;

    private static final int MAX_LINE_UNSUPPORTED_VERSION = 2;

    private static final String SM_SEPARATOR = "; &nbsp;&nbsp;";

    private static final String SM_SPACE = " ";

    private static final String SM_SEMICOLON = ";";

    private static final String SM_BR = "<br>";

    private static final String SM_HTML_BEGIN = "<html>";

    private static final String SM_HTML_END = "</html>";

    private static final String CONVERT_RATE_FORMAT = "0%";

    private FixbotResultParser fixbotResultParser;

    private ConversionGenerator conversionGenerator;

    private List<String> unsupportKits4GaddH;

    private List<String> unsupportKits4G2H;

    /**
     * unsupportKits4G2H and unsupportKits4GaddH
     *
     * @see unsupportKits4G2H
     * @see unsupportKits4GaddH
     */
    private List<String> unsupportKits4G2HandGaddH;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<String, List<ApiAnalyseResult>> kit2MethodAnalyseResultsMap;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<String, List<ApiAnalyseResult>> kit2ClassAnalyseResultsMap;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<String, List<ApiAnalyseResult>> kit2FieldAnalyseResultsMap;

    private List<KitStatisticsResult> kitStatisticsResults;

    private ProjectStatisticsResult projectStatisticsResult;

    private Map<String, String> showData;

    public SummaryGenerator(FixbotResultParser fixbotResultParser, ConversionGenerator conversionGenerator) {
        this.fixbotResultParser = fixbotResultParser;
        this.conversionGenerator = conversionGenerator;
        unsupportKits4GaddH = new ArrayList<>();
        unsupportKits4G2H = new ArrayList<>();
        kit2MethodAnalyseResultsMap = new HashMap<>();
        kit2ClassAnalyseResultsMap = new HashMap<>();
        kit2FieldAnalyseResultsMap = new HashMap<>();
        kitStatisticsResults = new ArrayList<>();
        projectStatisticsResult = new ProjectStatisticsResult();
        showData = new HashMap<>();
    }

    public Map<String, String> extractShowData() {
        generateMethodData();
        generateClassData();
        generateFieldData();
        generateUnsupportedKitData();
        countKitStatisticsResults();
        countProjectStatisticsResult();
        generateShowData();
        generateDependencyData();
        getJdkCompileVersion();
        return showData;
    }

    private void getJdkCompileVersion() {
        ProjectInfo projectInfo = fixbotResultParser.getProjectInfo();
        showData.put(SummaryConstants.JDK_COMPILE_VERSION, projectInfo.getSourceCompatibility());
    }

    private void generateMethodData() {
        Map<ApiKey, MappingApiInfo> methodKey2MappingMethodMap4GaddH = new HashMap<>();
        Map<ApiKey, MappingApiInfo> methodKey2MappingMethodMap4G2H = new HashMap<>();
        extractApiInfo(fixbotResultParser.getMethodKey2MappingMethodMap4AutoGaddH(),
            fixbotResultParser.getMethodKey2MappingMethodMap4ManualGaddH(),
            fixbotResultParser.getMethodKey2MappingMethodMap4AutoG2H(),
            fixbotResultParser.getMethodKey2MappingMethodMap4ManualG2H(), fixbotResultParser.getKit2FixbotMethodsMap(),
            methodKey2MappingMethodMap4GaddH, methodKey2MappingMethodMap4G2H);
        log.info("methodKey2MappingMethodMap4GaddH size: {}, methodKey2MappingMethodMap4G2H size: {}.",
            methodKey2MappingMethodMap4GaddH.size(), methodKey2MappingMethodMap4G2H.size());

        buildKit2ApiAnalyseResultsMap(fixbotResultParser.getKit2FixbotMethodsMap(),
            conversionGenerator.getMethodKey2FileCountMap(), conversionGenerator.getMethodKey2BlockCountMap(),
            methodKey2MappingMethodMap4GaddH, methodKey2MappingMethodMap4G2H, kit2MethodAnalyseResultsMap);
        log.info("kit2MethodAnalyseResultsMap size: {}.", kit2MethodAnalyseResultsMap.size());
    }

    private void generateClassData() {
        Map<ApiKey, MappingApiInfo> classKey2MappingClassMap4GaddH = new HashMap<>();
        Map<ApiKey, MappingApiInfo> classKey2MappingClassMap4G2H = new HashMap<>();
        extractApiInfo(fixbotResultParser.getClassKey2MappingClassMap4AutoGaddH(),
            fixbotResultParser.getClassKey2MappingClassMap4ManualGaddH(),
            fixbotResultParser.getClassKey2MappingClassMap4AutoG2H(),
            fixbotResultParser.getClassKey2MappingClassMap4ManualG2H(), fixbotResultParser.getKit2FixbotClassesMap(),
            classKey2MappingClassMap4GaddH, classKey2MappingClassMap4G2H);
        log.info("classKey2MappingClassMap4GaddH size: {}, classKey2MappingClassMap4G2H size: {}.",
            classKey2MappingClassMap4GaddH.size(), classKey2MappingClassMap4G2H.size());

        buildKit2ApiAnalyseResultsMap(fixbotResultParser.getKit2FixbotClassesMap(),
            conversionGenerator.getClassKey2FileCountMap(), conversionGenerator.getClassKey2BlockCountMap(),
            classKey2MappingClassMap4GaddH, classKey2MappingClassMap4G2H, kit2ClassAnalyseResultsMap);
        log.info("kit2ClassAnalyseResultsMap size: {}.", kit2ClassAnalyseResultsMap.size());
    }

    private void generateFieldData() {
        Map<ApiKey, MappingApiInfo> fieldKey2MappingFieldMap4GaddH = new HashMap<>();
        Map<ApiKey, MappingApiInfo> fieldKey2MappingFieldMap4G2H = new HashMap<>();
        extractApiInfo(fixbotResultParser.getFieldKey2MappingFieldMap4AutoGaddH(),
            fixbotResultParser.getFieldKey2MappingFieldMap4ManualGaddH(),
            fixbotResultParser.getFieldKey2MappingFieldMap4AutoG2H(),
            fixbotResultParser.getFieldKey2MappingFieldMap4ManualG2H(), fixbotResultParser.getKit2FixbotFieldsMap(),
            fieldKey2MappingFieldMap4GaddH, fieldKey2MappingFieldMap4G2H);
        log.info("fieldKey2MappingFieldMap4GaddH size: {}, fieldKey2MappingFieldMap4G2H size: {}.",
            fieldKey2MappingFieldMap4GaddH.size(), fieldKey2MappingFieldMap4G2H.size());

        buildKit2ApiAnalyseResultsMap(fixbotResultParser.getKit2FixbotFieldsMap(),
            conversionGenerator.getFieldKey2FileCountMap(), conversionGenerator.getFieldKey2BlockCountMap(),
            fieldKey2MappingFieldMap4GaddH, fieldKey2MappingFieldMap4G2H, kit2FieldAnalyseResultsMap);
        log.info("kit2FieldAnalyseResultsMap size: {}.", kit2FieldAnalyseResultsMap.size());
    }

    private void generateUnsupportedKitData() {
        Set<String> supportKits4AddHms = XmsGenerateService.supportKitInfo();
        supportKits4AddHms.add(KitsConstants.COMMON);
        supportKits4AddHms.add(KitsConstants.OTHER);
        supportKits4AddHms.add(KitsConstants.ML);
        fixbotResultParser.getAllKits().forEach(kit -> {
            if (!KitUtil.supportKitToH(kit)) {
                unsupportKits4G2H.add(kit);
            }
            if (!supportKits4AddHms.contains(kit)) {
                unsupportKits4GaddH.add(kit);
            }
        });

        String notSupportAPIToHStr = constructStr(unsupportKits4G2H);
        String notSupportAPI4ToHms =
            (unsupportKits4G2H.isEmpty()) ? Constant.NA : SM_HTML_BEGIN + notSupportAPIToHStr + SM_HTML_END;
        showData.put(SummaryConstants.NOT_SUPPORT_API_STR_TOHMS, notSupportAPI4ToHms);

        String notSupportAPIAddHStr = constructStr(unsupportKits4GaddH);
        String notSupport4AddHms =
            (unsupportKits4GaddH.isEmpty()) ? Constant.NA : SM_HTML_BEGIN + notSupportAPIAddHStr + SM_HTML_END;
        showData.put(SummaryConstants.NOT_SUPPORT_API_STR_ADDHMS, notSupport4AddHms);
    }

    private void generateShowData() {
        ProjectApiStatisticsResult methodStatisticsResult = projectStatisticsResult.getMethodStatisticsResult();

        int methodCount = methodStatisticsResult.getApiCount();
        String gmsDependencies = projectStatisticsResult.getKitCount() + " APIs, " + methodCount + " methods";
        showData.put(SummaryConstants.GMS_DEPENDENCY, gmsDependencies);

        int supportCount4GaddH =
            methodStatisticsResult.getAutoCount4GaddH() + methodStatisticsResult.getManualCount4GaddH();
        int unsupportCount4GaddH = methodCount - supportCount4GaddH;
        int supportCount4G2H = methodStatisticsResult.getAutoCount4G2H() + methodStatisticsResult.getManualCount4G2H();
        int unsupportCount4G2H = methodCount - supportCount4G2H;
        log.info("supportCount4GaddH: {}, unsupportCount4GaddH: {}, supportCount4G2H: {}, unsupportCount4G2H: {}.",
            supportCount4GaddH, unsupportCount4GaddH, supportCount4G2H, unsupportCount4G2H);
        showData.put(SummaryConstants.NOT_SUPPORT_METHOD_COUNT_ADDHMS, String.valueOf(unsupportCount4GaddH));
        showData.put(SummaryConstants.NOT_SUPPORT_METHOD_COUNT_TOHMS, String.valueOf(unsupportCount4G2H));
        showData.put(SummaryConstants.SUPPORT_AUTO_COUNT_ADDHMS,
            String.valueOf(methodStatisticsResult.getAutoCount4GaddH()));
        showData.put(SummaryConstants.SUPPORT_AUTO_COUNT_TOHMS,
            String.valueOf(methodStatisticsResult.getAutoCount4G2H()));
        showData.put(SummaryConstants.SUPPORT_MANUAL_COUNT_ADDHMS,
            String.valueOf(methodStatisticsResult.getManualCount4GaddH()));
        showData.put(SummaryConstants.SUPPORT_MANUAL_COUNT_TOHMS,
            String.valueOf(methodStatisticsResult.getManualCount4G2H()));

        DecimalFormat df = new DecimalFormat(CONVERT_RATE_FORMAT);
        String convertRateAH = (supportCount4GaddH == 0) ? Constant.NA
            : df.format((float) methodStatisticsResult.getAutoCount4GaddH() / supportCount4GaddH);
        String convertRateTH = (supportCount4G2H == 0) ? Constant.NA
            : df.format((float) methodStatisticsResult.getAutoCount4G2H() / supportCount4G2H);
        log.info("convertRateAH: {}, convertRateTH: {}.", convertRateAH, convertRateTH);
        showData.put(SummaryConstants.SUPPORT_RATE_ADDHMS, convertRateAH);
        showData.put(SummaryConstants.SUPPORT_RATE_TOHMS, convertRateTH);
    }

    private void generateDependencyData() {
        Map<String, String> dependency2Version = new HashMap<>();
        fixbotResultParser.getBlockId2DescriptionsMap()
            .forEach((key, descriptions) -> descriptions
                .forEach(description -> extractDependencyInfo(description, dependency2Version)));

        StringBuilder notSuportVersionShow = new StringBuilder();
        StringBuilder notSuportVersionDialogShow = new StringBuilder();
        StringBuilder notSuportVersionContent = new StringBuilder();
        int seperateCount = (fixbotResultParser.getFontSize() < LARGE_FONTSIZE) ? MORE_INCOMPATIBLE_API_ONE_LINE
            : LESS_INCOMPATIBLE_API_ONE_LINE;
        int count = 0;
        for (Map.Entry<String, String> ite : dependency2Version.entrySet()) {
            String key = ite.getKey();
            count++;
            if (count == seperateCount) {
                notSuportVersionShow.append(key)
                    .append(SM_SPACE)
                    .append(dependency2Version.get(key))
                    .append(SM_SEMICOLON + SM_BR);
            } else if (count < seperateCount * MAX_LINE_UNSUPPORTED_VERSION) {
                notSuportVersionShow.append(key)
                    .append(SM_SPACE)
                    .append(dependency2Version.get(key))
                    .append(SM_SEPARATOR);
            } else if (count == seperateCount * MAX_LINE_UNSUPPORTED_VERSION) {
                notSuportVersionShow.append("<u>...more</u>");
            } else {
                log.error("Illegal argument, count: {}, seperateCount: {}", count, seperateCount);
            }

            notSuportVersionDialogShow.append(key)
                .append(SM_SPACE)
                .append(dependency2Version.get(key))
                .append(SM_SEPARATOR);
            if (count % seperateCount == 0) {
                notSuportVersionDialogShow.replace(notSuportVersionDialogShow.length() - SM_SEPARATOR.length(),
                    notSuportVersionDialogShow.length(), SM_SEMICOLON);
                notSuportVersionDialogShow.append(SM_BR);
            }

            notSuportVersionContent.append(key)
                .append(SM_SPACE)
                .append(dependency2Version.get(key))
                .append(SM_SEMICOLON + SM_SPACE);
        }

        handleDependencyStr(count, notSuportVersionShow, notSuportVersionDialogShow);

        showData.put(SummaryConstants.NOT_SUPPORT_VERSION_SHOW,
            SM_HTML_BEGIN + notSuportVersionShow.toString() + SM_HTML_END);
        showData.put(SummaryConstants.NOT_SUPPORT_VERSION_DIALOG_SHOW,
            SM_HTML_BEGIN + notSuportVersionDialogShow.toString() + SM_HTML_END);
        showData.put(SummaryConstants.NOT_SUPPORT_VERSION_CONTENT, notSuportVersionContent.toString());

        int line = (count % seperateCount > 0) ? ((count / seperateCount) + 1) : (count / seperateCount);
        showData.put(SummaryConstants.NOT_SUPPORT_VERSION_COUNT, String.valueOf(count));
        showData.put(SummaryConstants.NOT_SUPPORT_VERSION_LINE, String.valueOf(line));
    }

    private void handleDependencyStr(int count, StringBuilder notSuportVersionShow,
        StringBuilder notSuportVersionDialogShow) {
        if (notSuportVersionShow.length() == 0) {
            notSuportVersionShow.append(Constant.NA);
        }

        if (count > 0 && notSuportVersionShow
            .subSequence(notSuportVersionShow.length() - SM_SEPARATOR.length(), notSuportVersionShow.length())
            .equals(SM_SEPARATOR)) {
            notSuportVersionShow.replace(notSuportVersionShow.length() - SM_SEPARATOR.length(),
                notSuportVersionShow.length(), SM_SEMICOLON);
        }
        if (count > 0
            && notSuportVersionDialogShow
                .subSequence(notSuportVersionDialogShow.length() - SM_SEPARATOR.length(),
                    notSuportVersionDialogShow.length())
                .equals(SM_SEPARATOR)) {
            notSuportVersionDialogShow.replace(notSuportVersionDialogShow.length() - SM_SEPARATOR.length(),
                notSuportVersionDialogShow.length(), SM_SEMICOLON);
        }
    }

    private String constructStr(List<String> notSupportAPIs) {
        StringBuilder notSupportAPIStr = new StringBuilder();
        int notSupportAPINum = 0;
        for (String api : notSupportAPIs) {
            notSupportAPIStr.append(api);
            notSupportAPINum++;
            if (notSupportAPINum % MAX_UNSUPPORTED_API_ONE_LINE == 0) {
                notSupportAPIStr.append("<br>");
            } else {
                notSupportAPIStr.append("; ");
            }
        }
        return notSupportAPIStr.toString();
    }

    private void extractDependencyInfo(ConversionPointDesc description, Map<String, String> versionKit) {
        if (description.isUpdate() && !versionKit.containsKey(description.getDependencyName())) {
            versionKit.put(description.getDependencyName(), description.getVersion());
        }
    }

    private void extractApiInfo(Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4AutoGaddH,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4ManualGaddH,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4AutoG2H,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4ManualG2H,
        TreeMap<String, List<FixbotApiInfo>> kit2FixbotApisMap, Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4GaddH,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4G2H) {
        kit2FixbotApisMap.forEach((kit, fixbotApis) -> fixbotApis.forEach(fixbotApi -> {
            analysisApiInfo(kit, fixbotApi, apiKey2MappingApiMap4AutoGaddH, apiKey2MappingApiMap4ManualGaddH,
                apiKey2MappingApiMap4GaddH);
            analysisApiInfo(kit, fixbotApi, apiKey2MappingApiMap4AutoG2H, apiKey2MappingApiMap4ManualG2H,
                apiKey2MappingApiMap4G2H);
        }));
    }

    private void analysisApiInfo(String kit, FixbotApiInfo fixbotApi,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4Auto, Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4Manual,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap) {
        ApiKey apiKey = ApiKey.builder().kit(kit).oldNameInDesc(fixbotApi.getOldNameInDesc()).build();
        MappingApiInfo mappingApi4Auto = apiKey2MappingApiMap4Auto.get(apiKey);
        if (mappingApi4Auto != null && !ConvertType.MANUAL.equals(mappingApi4Auto.getConvertStatus())) {
            mappingApi4Auto.setIsAuto(true);
            buildApiKey2MappingApiMap(mappingApi4Auto, apiKey2MappingApiMap);
            return;
        }

        MappingApiInfo mappingApi4Manual = apiKey2MappingApiMap4Manual.get(apiKey);
        if (mappingApi4Manual == null) {
            log.warn("api not found in auto and manual mapping, apiKey: {}.", apiKey);
            return;
        }
        if (mappingApi4Manual.isSupport()) {
            mappingApi4Manual.setIsAuto(false);
            buildApiKey2MappingApiMap(mappingApi4Manual, apiKey2MappingApiMap);
        } else {
            buildApiKey2MappingApiMap(mappingApi4Manual, apiKey2MappingApiMap);
        }
    }

    private void buildApiKey2MappingApiMap(MappingApiInfo mappingApi,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap) {
        ApiKey apiKey = ApiKey.builder().kit(mappingApi.getKit()).oldNameInDesc(mappingApi.getOldNameInDesc()).build();
        apiKey2MappingApiMap.put(apiKey, mappingApi);
    }

    private void buildKit2ApiAnalyseResultsMap(TreeMap<String, List<FixbotApiInfo>> kit2FixbotApisMap,
        Map<ApiKey, Integer> apiKey2FileCountMap, Map<ApiKey, Integer> apiKey2BlockCountMap,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4GaddH, Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4G2H,
        Map<String, List<ApiAnalyseResult>> kit2ApiAnalyseResultsMap) {
        kit2FixbotApisMap.forEach((kit, fixbotApis) -> {
            kit2ApiAnalyseResultsMap.putIfAbsent(kit, new ArrayList<>());
            fixbotApis.forEach(fixbotApi -> buildKit2ApiAnalyseResultsMapEntry(kit, fixbotApi.getOldNameInDesc(),
                apiKey2FileCountMap, apiKey2BlockCountMap, apiKey2MappingApiMap4GaddH, apiKey2MappingApiMap4G2H,
                kit2ApiAnalyseResultsMap));
        });
    }

    private void countKitStatisticsResults() {
        Map<String, KitApiStatisticsResult> kit2MethodStatisticsResultMap = new LinkedHashMap<>();
        buildKit2ApiStatisticsResultMap(kit2MethodAnalyseResultsMap, kit2MethodStatisticsResultMap);
        log.info("kit2MethodStatisticsResultMap size: {}.", kit2MethodStatisticsResultMap.size());

        Map<String, KitApiStatisticsResult> kit2ClassStatisticsResultMap = new LinkedHashMap<>();
        buildKit2ApiStatisticsResultMap(kit2ClassAnalyseResultsMap, kit2ClassStatisticsResultMap);
        log.info("kit2ClassStatisticsResultMap size: {}.", kit2ClassStatisticsResultMap.size());

        Map<String, KitApiStatisticsResult> kit2FieldStatisticsResultMap = new LinkedHashMap<>();
        buildKit2ApiStatisticsResultMap(kit2FieldAnalyseResultsMap, kit2FieldStatisticsResultMap);
        log.info("kit2FieldStatisticsResultMap size: {}.", kit2FieldStatisticsResultMap.size());

        // firstly, set Kit method statistics
        Set<String> alreadyStatisticsKitSet = new HashSet<>();
        Map<String, KitSdkVersion> kitSdkVersionMap = fixbotResultParser.getKitSdkVersionMap();
        kit2MethodStatisticsResultMap.forEach((kit, methodStatisticsResult) -> {
            KitStatisticsResult kitStatisticsResult = new KitStatisticsResult();
            kitStatisticsResult.setKit(kit);
            kitStatisticsResult.setKitSdkVersion(kitSdkVersionMap.get(kit));
            kitStatisticsResult.setMethodStatisticsResult(methodStatisticsResult);

            if (kit2ClassStatisticsResultMap.containsKey(kit)) {
                kitStatisticsResult.setClassStatisticsResult(kit2ClassStatisticsResultMap.get(kit));
            }

            if (kit2FieldStatisticsResultMap.containsKey(kit)) {
                kitStatisticsResult.setFieldStatisticsResult(kit2FieldStatisticsResultMap.get(kit));
            }
            alreadyStatisticsKitSet.add(kit);
            kitStatisticsResults.add(kitStatisticsResult);
        });

        // secondly, set Kit class statistics
        kit2ClassStatisticsResultMap.forEach((kit, classStatisticsResult) -> {
            if (!alreadyStatisticsKitSet.contains(kit)) {
                KitStatisticsResult kitStatisticsResult = new KitStatisticsResult();
                kitStatisticsResult.setKit(kit);
                kitStatisticsResult.setKitSdkVersion(kitSdkVersionMap.get(kit));
                kitStatisticsResult.setClassStatisticsResult(classStatisticsResult);

                if (kit2FieldStatisticsResultMap.containsKey(kit)) {
                    kitStatisticsResult.setFieldStatisticsResult(kit2FieldStatisticsResultMap.get(kit));
                }
                alreadyStatisticsKitSet.add(kit);
                kitStatisticsResults.add(kitStatisticsResult);
            }
        });

        // finally, set Kit field statistics
        kit2FieldStatisticsResultMap.forEach((kit, fieldStatisticsResult) -> {
            if (!alreadyStatisticsKitSet.contains(kit)) {
                KitStatisticsResult kitStatisticsResult = new KitStatisticsResult();
                kitStatisticsResult.setKit(kit);
                kitStatisticsResult.setKitSdkVersion(kitSdkVersionMap.get(kit));
                kitStatisticsResult.setFieldStatisticsResult(kit2FieldStatisticsResultMap.get(kit));
                alreadyStatisticsKitSet.add(kit);
                kitStatisticsResults.add(kitStatisticsResult);
            }
        });
        log.info("kitStatisticsResults size: {}.", kitStatisticsResults.size());

        unsupportKits4G2HandGaddH = new ArrayList<>(unsupportKits4G2H);
        unsupportKits4G2HandGaddH.retainAll(unsupportKits4GaddH);
        log.info("unsupportKits4G2HandGaddH size: {}.", unsupportKits4G2HandGaddH.size());
    }

    private void buildKit2ApiStatisticsResultMap(Map<String, List<ApiAnalyseResult>> kit2ApiAnalyseResultsMap,
        Map<String, KitApiStatisticsResult> kit2ApiStatisticsResultMap) {
        kit2ApiAnalyseResultsMap.forEach((kit, kitApiAnalyseResults) -> {
            KitApiStatisticsResult kitApiStatisticsResult = new KitApiStatisticsResult();
            countKitApiStatisticsResult(kitApiAnalyseResults, kitApiStatisticsResult);
            kit2ApiStatisticsResultMap.put(kit, kitApiStatisticsResult);
        });
    }

    private void countKitApiStatisticsResult(List<ApiAnalyseResult> apiAnalyseResults,
        KitApiStatisticsResult kitApiStatisticsResult) {
        int totalCount = apiAnalyseResults.size();
        kitApiStatisticsResult.setApiCount(totalCount);

        int blockCount = 0;
        int autoCount4GaddH = 0;
        int manualCount4GaddH = 0;
        int autoCount4G2H = 0;
        int manualCount4G2H = 0;
        // used to simplify count
        int blackHole;
        for (ApiAnalyseResult apiAnalyseResult : apiAnalyseResults) {
            blockCount += apiAnalyseResult.getBlockCount();
            Boolean isAuto4GaddH = apiAnalyseResult.getIsAuto4GaddH();
            Boolean isAuto4G2H = apiAnalyseResult.getIsAuto4G2H();
            if (isAuto4GaddH != null) {
                blackHole = isAuto4GaddH ? autoCount4GaddH++ : manualCount4GaddH++;
            }
            if (isAuto4G2H != null) {
                blackHole = isAuto4G2H ? autoCount4G2H++ : manualCount4G2H++;
            }
        }

        int supportCount = Math.max(autoCount4GaddH + manualCount4GaddH, autoCount4G2H + manualCount4G2H);
        kitApiStatisticsResult.setBlockCount(blockCount);
        kitApiStatisticsResult.setSupportCount(supportCount);
        kitApiStatisticsResult.setUnsupportCount(totalCount - supportCount);
        kitApiStatisticsResult.setAutoCount4GaddH(autoCount4GaddH);
        kitApiStatisticsResult.setManualCount4GaddH(manualCount4GaddH);
        kitApiStatisticsResult.setAutoCount4G2H(autoCount4G2H);
        kitApiStatisticsResult.setManualCount4G2H(manualCount4G2H);
    }

    private void countProjectStatisticsResult() {
        String projectName = ConfigCacheService.getInstance()
            .getProjectConfig(fixbotResultParser.getProjectBasePath(), ConfigKeyConstants.INSPECT_FOLDER, String.class,
                "");
        projectStatisticsResult.setProjectName(projectName);
        projectStatisticsResult.setKitCount(kitStatisticsResults.size());
        log.info("kitCount: {}.", projectStatisticsResult.getKitCount());

        // used to compute max sdkVersion, so init to -1
        int minSdkVersion4GaddH = -1;
        int minSdkVersion4G2H = -1;
        int targetSdkVersion = -1;

        int methodCount = 0;
        int methodBlockCount = 0;
        int supportMethodCount = 0;
        int unsupportMethodCount = 0;
        int methodCount4AutoGaddH = 0;
        int methodCount4ManualGaddH = 0;
        int methodCount4AutoG2H = 0;
        int methodCount4ManualG2H = 0;

        int classCount = 0;
        int classBlockCount = 0;
        int supportClassCount = 0;
        int unsupportClassCount = 0;
        int classCount4AutoGaddH = 0;
        int classCount4ManualGaddH = 0;
        int classCount4AutoG2H = 0;
        int classCount4ManualG2H = 0;

        int fieldCount = 0;
        int fieldBlockCount = 0;
        int supportFieldCount = 0;
        int unsupportFieldCount = 0;
        int fieldCount4AutoGaddH = 0;
        int fieldCount4ManualGaddH = 0;
        int fieldCount4AutoG2H = 0;
        int fieldCount4ManualG2H = 0;
        for (KitStatisticsResult kitStatisticsResult : kitStatisticsResults) {
            KitSdkVersion kitSdkVersion = kitStatisticsResult.getKitSdkVersion();
            if (kitSdkVersion != null) {
                minSdkVersion4GaddH = Math.max(minSdkVersion4GaddH, kitSdkVersion.getMinSdkVersion4GaddH());
                minSdkVersion4G2H = Math.max(minSdkVersion4G2H, kitSdkVersion.getMinSdkVersion4G2H());
                targetSdkVersion = Math.max(targetSdkVersion, kitSdkVersion.getTargetSdkVersion());
            }

            KitApiStatisticsResult methodStatisticsResult = kitStatisticsResult.getMethodStatisticsResult();
            methodCount += methodStatisticsResult.getApiCount();
            methodBlockCount += methodStatisticsResult.getBlockCount();
            supportMethodCount += methodStatisticsResult.getSupportCount();
            unsupportMethodCount += methodStatisticsResult.getUnsupportCount();
            methodCount4AutoGaddH += methodStatisticsResult.getAutoCount4GaddH();
            methodCount4ManualGaddH += methodStatisticsResult.getManualCount4GaddH();
            methodCount4AutoG2H += methodStatisticsResult.getAutoCount4G2H();
            methodCount4ManualG2H += methodStatisticsResult.getManualCount4G2H();

            KitApiStatisticsResult classStatisticsResult = kitStatisticsResult.getClassStatisticsResult();
            classCount += classStatisticsResult.getApiCount();
            classBlockCount += classStatisticsResult.getBlockCount();
            supportClassCount += classStatisticsResult.getSupportCount();
            unsupportClassCount += classStatisticsResult.getUnsupportCount();
            classCount4AutoGaddH += classStatisticsResult.getAutoCount4GaddH();
            classCount4ManualGaddH += classStatisticsResult.getManualCount4GaddH();
            classCount4AutoG2H += classStatisticsResult.getAutoCount4G2H();
            classCount4ManualG2H += classStatisticsResult.getManualCount4G2H();

            KitApiStatisticsResult fieldStatisticsResult = kitStatisticsResult.getFieldStatisticsResult();
            fieldCount += fieldStatisticsResult.getApiCount();
            fieldBlockCount += fieldStatisticsResult.getBlockCount();
            supportFieldCount += fieldStatisticsResult.getSupportCount();
            unsupportFieldCount += fieldStatisticsResult.getUnsupportCount();
            fieldCount4AutoGaddH += fieldStatisticsResult.getAutoCount4GaddH();
            fieldCount4ManualGaddH += fieldStatisticsResult.getManualCount4GaddH();
            fieldCount4AutoG2H += fieldStatisticsResult.getAutoCount4G2H();
            fieldCount4ManualG2H += fieldStatisticsResult.getManualCount4G2H();
        }

        if (minSdkVersion4GaddH > 0 && minSdkVersion4G2H > 0 && targetSdkVersion > 0) {
            KitSdkVersion kitSdkVersion = new KitSdkVersion();
            kitSdkVersion.setMinSdkVersion4GaddH(minSdkVersion4GaddH);
            kitSdkVersion.setMinSdkVersion4G2H(minSdkVersion4G2H);
            kitSdkVersion.setTargetSdkVersion(targetSdkVersion);
            projectStatisticsResult.setKitSdkVersion(kitSdkVersion);
        }
        log.info("project kitSdkVersion: {}.", projectStatisticsResult.getKitSdkVersion());

        DecimalFormat decimalFormat = new DecimalFormat("0%");
        String supportMethodRate = computeRate(supportMethodCount, methodCount, decimalFormat);
        String unsupportMethodRate = computeRate(unsupportMethodCount, methodCount, decimalFormat);
        int methodCount4GaddH = methodCount4AutoGaddH + methodCount4ManualGaddH;
        String methodRate4AutoGaddH = computeRate(methodCount4AutoGaddH, methodCount4GaddH, decimalFormat);
        String methodRate4ManualGaddH = computeRate(methodCount4ManualGaddH, methodCount4GaddH, decimalFormat);
        int methodCount4G2H = methodCount4AutoG2H + methodCount4ManualG2H;
        String methodRate4AutoG2H = computeRate(methodCount4AutoG2H, methodCount4G2H, decimalFormat);
        String methodRate4ManualG2H = computeRate(methodCount4ManualG2H, methodCount4G2H, decimalFormat);

        ProjectApiStatisticsResult methodStatisticsResult = new ProjectApiStatisticsResult();
        methodStatisticsResult.setApiCount(methodCount);
        methodStatisticsResult.setBlockCount(methodBlockCount);
        methodStatisticsResult.setSupportRate(supportMethodRate);
        methodStatisticsResult.setUnsupportRate(unsupportMethodRate);
        methodStatisticsResult.setAutoCount4GaddH(methodCount4AutoGaddH);
        methodStatisticsResult.setManualCount4GaddH(methodCount4ManualGaddH);
        methodStatisticsResult.setAutoRate4GaddH(methodRate4AutoGaddH);
        methodStatisticsResult.setManualRate4GaddH(methodRate4ManualGaddH);
        methodStatisticsResult.setAutoCount4G2H(methodCount4AutoG2H);
        methodStatisticsResult.setManualCount4G2H(methodCount4ManualG2H);
        methodStatisticsResult.setAutoRate4G2H(methodRate4AutoG2H);
        methodStatisticsResult.setManualRate4G2H(methodRate4ManualG2H);
        projectStatisticsResult.setMethodStatisticsResult(methodStatisticsResult);
        log.info("project methodStatisticsResult: {}.", methodStatisticsResult);

        String supportClassRate = computeRate(supportClassCount, classCount, decimalFormat);
        String unsupportClassRate = computeRate(unsupportClassCount, classCount, decimalFormat);
        int classCount4GaddH = classCount4AutoGaddH + classCount4ManualGaddH;
        String autoClassRate4GaddH = computeRate(classCount4AutoGaddH, classCount4GaddH, decimalFormat);
        String manualClassRate4GaddH = computeRate(classCount4ManualGaddH, classCount4GaddH, decimalFormat);
        int classCount4G2H = classCount4AutoG2H + classCount4ManualG2H;
        String autoClassRate4G2H = computeRate(classCount4AutoG2H, classCount4G2H, decimalFormat);
        String manualClassRate4G2H = computeRate(classCount4ManualG2H, classCount4G2H, decimalFormat);

        ProjectApiStatisticsResult classStatisticsResult = new ProjectApiStatisticsResult();
        classStatisticsResult.setApiCount(classCount);
        classStatisticsResult.setBlockCount(classBlockCount);
        classStatisticsResult.setSupportRate(supportClassRate);
        classStatisticsResult.setUnsupportRate(unsupportClassRate);
        classStatisticsResult.setAutoCount4GaddH(classCount4AutoGaddH);
        classStatisticsResult.setManualCount4GaddH(classCount4ManualGaddH);
        classStatisticsResult.setAutoRate4GaddH(autoClassRate4GaddH);
        classStatisticsResult.setManualRate4GaddH(manualClassRate4GaddH);
        classStatisticsResult.setAutoCount4G2H(classCount4AutoG2H);
        classStatisticsResult.setManualCount4G2H(classCount4ManualG2H);
        classStatisticsResult.setAutoRate4G2H(autoClassRate4G2H);
        classStatisticsResult.setManualRate4G2H(manualClassRate4G2H);
        projectStatisticsResult.setClassStatisticsResult(classStatisticsResult);
        log.info("project classStatisticsResult: {}.", classStatisticsResult);

        String supportFieldRate = computeRate(supportFieldCount, fieldCount, decimalFormat);
        String unsupportFieldRate = computeRate(unsupportFieldCount, fieldCount, decimalFormat);
        int fieldCount4GaddH = fieldCount4AutoGaddH + fieldCount4ManualGaddH;
        String autoFieldRate4GaddH = computeRate(fieldCount4AutoGaddH, fieldCount4GaddH, decimalFormat);
        String manualFieldRate4GaddH = computeRate(fieldCount4ManualGaddH, fieldCount4GaddH, decimalFormat);
        int fieldCount4G2H = fieldCount4AutoG2H + fieldCount4ManualG2H;
        String autoFieldRate4G2H = computeRate(fieldCount4AutoG2H, fieldCount4G2H, decimalFormat);
        String manualFieldRate4G2H = computeRate(fieldCount4ManualG2H, fieldCount4G2H, decimalFormat);

        ProjectApiStatisticsResult fieldStatisticsResult = new ProjectApiStatisticsResult();
        fieldStatisticsResult.setApiCount(fieldCount);
        fieldStatisticsResult.setBlockCount(fieldBlockCount);
        fieldStatisticsResult.setSupportRate(supportFieldRate);
        fieldStatisticsResult.setUnsupportRate(unsupportFieldRate);
        fieldStatisticsResult.setAutoCount4GaddH(fieldCount4AutoGaddH);
        fieldStatisticsResult.setManualCount4GaddH(fieldCount4ManualGaddH);
        fieldStatisticsResult.setAutoRate4GaddH(autoFieldRate4GaddH);
        fieldStatisticsResult.setManualRate4GaddH(manualFieldRate4GaddH);
        fieldStatisticsResult.setAutoCount4G2H(fieldCount4AutoG2H);
        fieldStatisticsResult.setManualCount4G2H(fieldCount4ManualG2H);
        fieldStatisticsResult.setAutoRate4G2H(autoFieldRate4G2H);
        fieldStatisticsResult.setManualRate4G2H(manualFieldRate4G2H);
        projectStatisticsResult.setFieldStatisticsResult(fieldStatisticsResult);
        log.info("project fieldStatisticsResult: {}.", fieldStatisticsResult);

        String unsupportKitsStr =
            (unsupportKits4G2HandGaddH.isEmpty()) ? Constant.NA : String.join(", ", unsupportKits4G2HandGaddH);
        projectStatisticsResult.setUnsupportKits(unsupportKitsStr);
        projectStatisticsResult.setAnalyseDateTime(LocalDateTime.now().format(Constant.READABLE_DATETIME));
        log.info("project unsupportKits: {}, analyseDateTime: {}.", projectStatisticsResult.getUnsupportKits(),
            projectStatisticsResult.getAnalyseDateTime());
    }

    private String computeRate(int numerator, int denominator, DecimalFormat decimalFormat) {
        return (denominator == 0) ? Constant.NA : decimalFormat.format((float) numerator / denominator);
    }

    private void buildKit2ApiAnalyseResultsMapEntry(String kit, String oldNameInDesc,
        Map<ApiKey, Integer> apiKey2FileCountMap, Map<ApiKey, Integer> apiKey2BlockCountMap,
        Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4GaddH, Map<ApiKey, MappingApiInfo> apiKey2MappingApiMap4G2H,
        Map<String, List<ApiAnalyseResult>> kit2ApiAnalyseResultsMap) {
        ApiAnalyseResult apiAnalyseResult = new ApiAnalyseResult();
        ApiKey apiKey = ApiKey.builder().kit(kit).oldNameInDesc(oldNameInDesc).build();
        if (apiKey2FileCountMap.containsKey(apiKey)) {
            apiAnalyseResult.setFileCount(apiKey2FileCountMap.get(apiKey));
        }
        if (apiKey2BlockCountMap.containsKey(apiKey)) {
            apiAnalyseResult.setBlockCount(apiKey2BlockCountMap.get(apiKey));
        }
        if (apiKey2MappingApiMap4GaddH.containsKey(apiKey)) {
            MappingApiInfo mappingApi = apiKey2MappingApiMap4GaddH.get(apiKey);
            apiAnalyseResult.setOldName(mappingApi.getOldName());
            apiAnalyseResult.setIsAuto4GaddH(mappingApi.getIsAuto());
        }
        if (apiKey2MappingApiMap4G2H.containsKey(apiKey)) {
            MappingApiInfo mappingApi = apiKey2MappingApiMap4G2H.get(apiKey);
            apiAnalyseResult.setOldName(mappingApi.getOldName());
            apiAnalyseResult.setNewName4G2H(mappingApi.getNewName());
            apiAnalyseResult.setUrl(mappingApi.getUrl());
            apiAnalyseResult.setIsAuto4G2H(mappingApi.getIsAuto());
        }
        if (!apiKey2MappingApiMap4GaddH.containsKey(apiKey) && !apiKey2MappingApiMap4G2H.containsKey(apiKey)) {
            log.warn("api not found in G+H and G2H mapping, apiKey: {}.", apiKey);
            return;
        }
        kit2ApiAnalyseResultsMap.get(kit).add(apiAnalyseResult);
    }

}
