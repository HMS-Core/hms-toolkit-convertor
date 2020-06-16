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

import com.huawei.hms.convertor.core.engine.fixbot.model.MethodItem;
import com.huawei.hms.convertor.core.engine.fixbot.model.ParseResult;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.core.result.summary.SummaryConstants;
import com.huawei.hms.convertor.openapi.XmsGenerateService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.KitUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    private FixbotResultParser fixbotResultParser;

    private Map<String, List<ConversionPointDesc>> id2DescriptionsMap;

    private List<ParseResult> addHmsAutoMethods;

    private List<ParseResult> toHmsAutoMethods;

    private List<ParseResult> addHmsManualMethods;

    private List<ParseResult> toHmsManualMethods;

    private int fontSize;

    private Map<String, String> showData = new HashMap<>();

    public SummaryGenerator(FixbotResultParser parser) {
        this.fixbotResultParser = parser;
        this.id2DescriptionsMap = parser.getId2DescriptionsMap();
        this.addHmsAutoMethods = parser.getAddHmsAutoMethods();
        this.toHmsAutoMethods = parser.getToHmsAutoMethods();
        this.addHmsManualMethods = parser.getAddHmsManualMethods();
        this.toHmsManualMethods = parser.getToHmsManualMethods();
        this.fontSize = parser.getFontSize();
    }

    public Map<String, String> extractShowData() {
        generateMethodData();
        generateUnsupportedKitData();
        generateDependencyData();
        return showData;
    }

    private void generateMethodData() {
        int totalKitCount = fixbotResultParser.getAllKits().size();
        if (fixbotResultParser.getAllKits().contains(KitsConstants.COMMON)) {
            totalKitCount--;
        }
        if (fixbotResultParser.getAllKits().contains(KitsConstants.OTHER)) {
            totalKitCount--;
        }
        AtomicInteger totalMethodCount = new AtomicInteger();
        List<ParseResult> notSupportMethodList4ToH = new ArrayList<>();
        List<ParseResult> notSupportMethodList4AddH = new ArrayList<>();
        AtomicInteger autoMethod4ToH = new AtomicInteger();
        AtomicInteger autoMethod4AddH = new AtomicInteger();

        extractMethodInfo(
            totalMethodCount, notSupportMethodList4ToH, notSupportMethodList4AddH, autoMethod4ToH, autoMethod4AddH);

        String gmsDependencies = totalKitCount + " APIs, " + totalMethodCount + " methods";
        showData.put(SummaryConstants.GMS_DEPENDENCY, gmsDependencies);

        showData.put(SummaryConstants.NOT_SUPPORT_METHOD_COUNT_TOHMS, String.valueOf(notSupportMethodList4ToH.size()));
        showData.put(
            SummaryConstants.NOT_SUPPORT_METHOD_COUNT_ADDHMS, String.valueOf(notSupportMethodList4AddH.size()));
        showData.put(SummaryConstants.SUPPORT_AUTO_COUNT_TOHMS, String.valueOf(autoMethod4ToH.get()));
        showData.put(SummaryConstants.SUPPORT_AUTO_COUNT_ADDHMS, String.valueOf(autoMethod4AddH.get()));

        int total = totalMethodCount.get();
        int maunalMethod4ToH = total - autoMethod4ToH.get() - notSupportMethodList4ToH.size();
        int maunalMethod4AddH = total - autoMethod4AddH.get() - notSupportMethodList4ToH.size();
        maunalMethod4ToH = Math.max(maunalMethod4ToH, 0);
        maunalMethod4AddH = Math.max(maunalMethod4AddH, 0);
        showData.put(SummaryConstants.SUPPORT_MANUAL_COUNT_TOHMS, String.valueOf(maunalMethod4ToH));
        showData.put(SummaryConstants.SUPPORT_MANUAL_COUNT_ADDHMS, String.valueOf(maunalMethod4AddH));

        DecimalFormat df = new DecimalFormat("0%");

        String convertRateTH = (total - notSupportMethodList4ToH.size() == 0) ? Constant.NA
            : df.format((float) autoMethod4ToH.get() / (autoMethod4ToH.get() + maunalMethod4ToH));
        showData.put(SummaryConstants.SUPPORT_RATE_TOHMS, convertRateTH);
        String convertRateAH = (total - notSupportMethodList4ToH.size() == 0) ? Constant.NA
            : df.format((float) autoMethod4AddH.get() / (autoMethod4AddH.get() + maunalMethod4AddH));
        showData.put(SummaryConstants.SUPPORT_RATE_ADDHMS, convertRateAH);
    }

    private void generateUnsupportedKitData() {
        Set<String> supportKit4AddHms = XmsGenerateService.supportKitInfo();
        supportKit4AddHms.add(KitsConstants.COMMON);
        supportKit4AddHms.add(KitsConstants.OTHER);
        supportKit4AddHms.add(KitsConstants.ML);
        List<String> notSupportAPI4ToH = new ArrayList<>();
        List<String> notSupportAPI4AddH = new ArrayList<>();
        fixbotResultParser.getAllKits().forEach(kit -> {
            if (!KitUtil.supportKitToH(kit)) {
                notSupportAPI4ToH.add(kit);
            }
            if (!supportKit4AddHms.contains(kit)) {
                notSupportAPI4AddH.add(kit);
            }
        });
        String notSupportAPIToHStr = constructStr(notSupportAPI4ToH);
        String notSupportAPI4ToHms = (notSupportAPI4ToH.isEmpty()) ?
            Constant.NA : SM_HTML_BEGIN + notSupportAPIToHStr + SM_HTML_END;
        showData.put(SummaryConstants.NOT_SUPPORT_API_STR_TOHMS, notSupportAPI4ToHms);

        String notSupportAPIAddHStr = constructStr(notSupportAPI4AddH);
        String notSupport4AddHms =
            (notSupportAPI4AddH.isEmpty()) ? Constant.NA : SM_HTML_BEGIN + notSupportAPIAddHStr + SM_HTML_END;
        showData.put(SummaryConstants.NOT_SUPPORT_API_STR_ADDHMS, notSupport4AddHms);
    }

    private void generateDependencyData() {
        Map<String, String> dependency2Version = new HashMap<>();
        id2DescriptionsMap.forEach((key, descriptions) ->
            descriptions.forEach(description -> extractDependencyInfo(description, dependency2Version)));

        StringBuilder notSuportVersionShow = new StringBuilder();
        StringBuilder notSuportVersionDialogShow = new StringBuilder();
        StringBuilder notSuportVersionContent = new StringBuilder();
        int seperateCount =
            (fontSize < LARGE_FONTSIZE) ? MORE_INCOMPATIBLE_API_ONE_LINE : LESS_INCOMPATIBLE_API_ONE_LINE;
        int count = 0;
        for (Iterator ite = dependency2Version.keySet().iterator(); ite.hasNext();) {
            String key = ite.next().toString();
            count++;
            if (count == seperateCount) {
                notSuportVersionShow.append(key)
                    .append(SM_SPACE).append(dependency2Version.get(key)).append(SM_SEMICOLON + SM_BR);
            } else if (count < seperateCount * MAX_LINE_UNSUPPORTED_VERSION) {
                notSuportVersionShow.append(key)
                    .append(SM_SPACE).append(dependency2Version.get(key)).append(SM_SEPARATOR);
            } else if (count == seperateCount * MAX_LINE_UNSUPPORTED_VERSION) {
                notSuportVersionShow.append("<u>...more</u>");
            } else {
                log.error("Illegal argument, count: {}, seperateCount: {}", count, seperateCount);
            }

            notSuportVersionDialogShow.append(key)
                .append(SM_SPACE).append(dependency2Version.get(key)).append(SM_SEPARATOR);
            if (count % seperateCount == 0) {
                notSuportVersionDialogShow.replace(notSuportVersionDialogShow.length() - SM_SEPARATOR.length(),
                    notSuportVersionDialogShow.length(), SM_SEMICOLON);
                notSuportVersionDialogShow.append(SM_BR);
            }

            notSuportVersionContent.append(key)
                .append(SM_SPACE).append(dependency2Version.get(key)).append(SM_SEMICOLON + SM_SPACE);
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

    private void handleDependencyStr(
        int count, StringBuilder notSuportVersionShow, StringBuilder notSuportVersionDialogShow) {
        if (notSuportVersionShow.length() == 0) {
            notSuportVersionShow.append(Constant.NA);
        }

        if (count > 0 && notSuportVersionShow
            .subSequence(notSuportVersionShow.length() - SM_SEPARATOR.length(), notSuportVersionShow.length())
            .equals(SM_SEPARATOR)) {
            notSuportVersionShow.replace(notSuportVersionShow.length() - SM_SEPARATOR.length(),
                notSuportVersionShow.length(), SM_SEMICOLON);
        }
        if (count > 0 && notSuportVersionDialogShow
            .subSequence(
                notSuportVersionDialogShow.length() - SM_SEPARATOR.length(), notSuportVersionDialogShow.length())
            .equals(SM_SEPARATOR)) {
            notSuportVersionDialogShow.replace(notSuportVersionDialogShow.length() - SM_SEPARATOR.length(),
                notSuportVersionDialogShow.length(), SM_SEMICOLON);
        }
    }

    private String constructStr(List<String> notSupportAPI) {
        StringBuilder notSupportAPIStr = new StringBuilder();
        int notSupportAPINum = 0;
        for (String api : notSupportAPI) {
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

    private void extractMethodInfo(AtomicInteger totalMethodCount,
        List<ParseResult> notSupportMethodList4ToHms, List<ParseResult> notSupportMethodList4AddHms,
        AtomicInteger autoMethod4ToHms, AtomicInteger autoMethod4AddHms) {
        TreeMap<String, List<MethodItem>> kit2Methods = fixbotResultParser.getKit2Methods();

        kit2Methods.forEach((kitName, methodItems) -> {
            // Accumulate the number of total methods
            totalMethodCount.set(totalMethodCount.get() + methodItems.size());

            // Analysis Method Attribute for To HMS API and Add HMS API
            methodItems.forEach(methodItem -> {
                analysisMethodInfo4ToHms(kitName, methodItem, autoMethod4ToHms, notSupportMethodList4ToHms);
                analysisMethodInfo4AddHms(kitName, methodItem, autoMethod4AddHms, notSupportMethodList4AddHms);
            });
        });
    }

    private void analysisMethodInfo4ToHms(String kitName, MethodItem methodItem, AtomicInteger autoMethod4ToHms,
        List<ParseResult> notSupportMethodList4ToHms) {
        // The current analysis result is the G2H analysis result.
        // We can directly determine whether this method is an automatic conversion method.
        if (ConvertType.AUTO.equals(methodItem.getConvertStatus())) {
            autoMethod4ToHms.set(autoMethod4ToHms.get() + 1);
            return;
        }

        // Analyzes whether this method does not support conversion.
        for (ParseResult item : toHmsManualMethods) {
            if (kitName.equals(item.getKit()) && methodItem.getMethodName().equals(item.getMethodName())
                && !item.isSupport()) {
                notSupportMethodList4ToHms.add(item);
                return;
            }
        }
    }

    private void analysisMethodInfo4AddHms(String kitName, MethodItem methodItem, AtomicInteger autoMethodCount4AddHms,
        List<ParseResult> notSupportMethodList4AddHms) {
        // Analyze whether this method is an automatic conversion method.
        for (ParseResult autoMethod : addHmsAutoMethods) {
            if (kitName.equals(autoMethod.getKit()) && methodItem.getMethodName().equals(autoMethod.getMethodName())
                && !ConvertType.MANUAL.equals(autoMethod.getConvertStatus())) {
                autoMethodCount4AddHms.set(autoMethodCount4AddHms.get() + 1);
                return;
            }
        }

        // Analyzes whether this method does not support conversion.
        for (ParseResult manualMethod : addHmsManualMethods) {
            if (kitName.equals(manualMethod.getKit()) && methodItem.getMethodName().equals(manualMethod.getMethodName())
                && !manualMethod.isSupport()) {
                notSupportMethodList4AddHms.add(manualMethod);
                return;
            }
        }
    }
}
