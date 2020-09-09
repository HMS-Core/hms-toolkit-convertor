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

package com.huawei.hms.convertor.idea.util;

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.ApiAnalyseResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitApiStatisticsResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitSdkVersion;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitStatisticsResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.project.ProjectApiStatisticsResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.project.ProjectStatisticsResult;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.result.export.AnalyseResultHyperlinkTable;
import com.huawei.hms.convertor.idea.ui.result.export.AnalyseResultInternalLinkDest;
import com.huawei.hms.convertor.idea.ui.result.export.AnalyseResultLine;
import com.huawei.hms.convertor.idea.ui.result.export.AnalyseResultTable;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.SummaryCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.PropertyUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class AnalyseResultExportUtil {
    private AnalyseResultExportUtil() {
    }

    /**
     * export pdf
     *
     * @param projectBasePath Project base path
     * @return export file path
     * @throws IOException IO exception
     */
    public static Optional<String> exportPdf(String projectBasePath) throws IOException {
        ProjectStatisticsResult projectStatistics =
            SummaryCacheService.getInstance().getProjectStatisticsResult(projectBasePath);
        String projectStatisticsLine = String.format(Locale.ROOT,
            "The project %s totally integrates %d methods and %d classes and %d fields of %d kits, and the details are shown in the table below. HMS Core unsupported Kit: %s.",
            projectStatistics.getProjectName(), projectStatistics.getMethodStatisticsResult().getApiCount(),
            projectStatistics.getClassStatisticsResult().getApiCount(),
            projectStatistics.getFieldStatisticsResult().getApiCount(), projectStatistics.getKitCount(),
            projectStatistics.getUnsupportKits());

        List<AnalyseResultTable> kitStatisticsHeaderTables = new ArrayList<>();
        buildKitStatisticsHeaderTable(kitStatisticsHeaderTables);
        List<KitStatisticsResult> kitStatisticsResults =
            SummaryCacheManager.getInstance().getKitStatisticsResults(projectBasePath);
        List<AnalyseResultTable> kitStatisticsTables = new ArrayList<>();
        buildKitStatisticsTables(kitStatisticsResults, projectStatistics, kitStatisticsTables);
        log.info("kitStatisticsTables size: {}.", kitStatisticsTables.size());

        Map<String, List<ApiAnalyseResult>> kit2MethodAnalyseResultsMap =
            SummaryCacheManager.getInstance().getKit2MethodAnalyseResultsMap(projectBasePath);
        Map<String, List<ApiAnalyseResult>> kit2ClassAnalyseResultsMap =
            SummaryCacheManager.getInstance().getKit2ClassAnalyseResultsMap(projectBasePath);
        Map<String, List<ApiAnalyseResult>> kit2FieldAnalyseResultsMap =
            SummaryCacheManager.getInstance().getKit2FieldAnalyseResultsMap(projectBasePath);
        Map<String, List<AnalyseResultTable>> kit2MethodAnalyseTablesMap = new LinkedHashMap<>();
        Map<String, List<AnalyseResultTable>> kit2ClassAnalyseTablesMap = new LinkedHashMap<>();
        Map<String, List<AnalyseResultTable>> kit2FieldAnalyseTablesMap = new LinkedHashMap<>();
        if (!validateApiAnalyseResultNum(kit2MethodAnalyseResultsMap, kit2ClassAnalyseResultsMap,
            kit2FieldAnalyseResultsMap)) {
            log.error("api analyse result num validate fail.");
            return Optional.empty();
        }
        buildKit2AnalyseTablesMap(kit2MethodAnalyseResultsMap, "Method", kit2MethodAnalyseTablesMap);
        buildKit2AnalyseTablesMap(kit2ClassAnalyseResultsMap, "Class", kit2ClassAnalyseTablesMap);
        buildKit2AnalyseTablesMap(kit2FieldAnalyseResultsMap, "Field", kit2FieldAnalyseTablesMap);
        log.info(
            "kit2MethodAnalyseTablesMap size: {}, kit2ClassAnalyseTablesMap size: {}, kit2FieldAnalyseTablesMap size: {}.",
            kit2MethodAnalyseTablesMap.size(), kit2ClassAnalyseTablesMap.size(), kit2FieldAnalyseTablesMap.size());

        String feedbackLine = String.format(Locale.ROOT,
            "The report is generated at %s. For reference only for HMS Convertor. If you find any problem about this report, please submit a feedback.",
            projectStatistics.getAnalyseDateTime());
        String[][] feedbackLinkTable = buildFeedbackLinkTable(projectBasePath);

        PdfHelper pdfHelper = new PdfHelper(UIConstants.CommonAnalyseResult.MEDIA_BOX_WIDTH, PDRectangle.A4.getHeight(),
            UIConstants.CommonAnalyseResult.MEDIA_BOX_MARGIN, UIConstants.CommonAnalyseResult.FONT,
            UIConstants.CommonAnalyseResult.FONT_SIZE, UIConstants.CommonAnalyseResult.CONTENT_MARGIN,
            UIConstants.CommonAnalyseResult.ROW_HEIGHT);
        pdfHelper.createPage();

        drawProjectStatisticsLine(pdfHelper, projectStatisticsLine);

        drawKitStatisticsHeaderTable(pdfHelper, kitStatisticsHeaderTables);

        int kitStatisticsTablePageIndex = pdfHelper.getPageIndex();
        float[] kitStatisticsTableStartXy = pdfHelper.getCurrentXy();
        pdfHelper.drawTablesHorizon(kitStatisticsTables);
        log.info("draw kit statistics tables success.");

        Map<String, AnalyseResultInternalLinkDest> kit2AnalyseTableStartPositionMap = new LinkedHashMap<>();
        drawKitAnalyseTables(pdfHelper, kit2MethodAnalyseTablesMap, kit2ClassAnalyseTablesMap,
            kit2FieldAnalyseTablesMap, kit2AnalyseTableStartPositionMap);
        log.info("draw kit analyse tables success.");

        drawFeedback(pdfHelper, feedbackLine, feedbackLinkTable);

        // internal link which may cross page can only be drawn after drawing all page content
        List<AnalyseResultInternalLinkDest[][]> kitStatisticsLinkTables = new ArrayList<>();
        buildKitStatisticsLinkTables(kitStatisticsResults, kit2AnalyseTableStartPositionMap, kitStatisticsLinkTables);
        drawKitStatisticsLinkTable(pdfHelper, kitStatisticsTablePageIndex, kitStatisticsTableStartXy,
            kitStatisticsLinkTables);
        log.info("draw kit statistics link tables success.");

        String analyseResultFilePath = getExportFilePath(projectBasePath);
        pdfHelper.saveAndClose(analyseResultFilePath);
        return Optional.of(analyseResultFilePath);
    }

    private static void drawProjectStatisticsLine(PdfHelper pdfHelper, String line) throws IOException {
        AnalyseResultLine projectStatisticsLine = AnalyseResultLine.builder().line(line).build();
        pdfHelper.drawLine(projectStatisticsLine);
    }

    private static void drawKitStatisticsHeaderTable(PdfHelper pdfHelper, List<AnalyseResultTable> tables)
        throws IOException {
        pdfHelper.drawTablesHorizon(tables);
        pdfHelper.removeMarginRowAfterHeaderTable();
    }

    private static void drawKitStatisticsLinkTable(PdfHelper pdfHelper, int kitStatisticsTablePageIndex,
        float[] kitStatisticsTableStartXy, List<AnalyseResultInternalLinkDest[][]> linkTables) throws IOException {
        PdfUtil.drawInternalLink(pdfHelper.getDocument(), kitStatisticsTablePageIndex,
            UIConstants.CommonAnalyseResult.CONTENT_MARGIN, UIConstants.CommonAnalyseResult.ROW_HEIGHT,
            UIConstants.KitStatisticsTable.COL_WIDTH_4_TOTAL, UIConstants.KitStatisticsTable.LINK_TABLE_CEL_WIDTH_4_KIT,
            UIConstants.KitStatisticsTable.LINK_TABLE_CEL_HEIGHT_4_KIT, kitStatisticsTableStartXy, linkTables.get(0));
    }

    private static void drawKitAnalyseTables(PdfHelper pdfHelper,
        Map<String, List<AnalyseResultTable>> kit2MethodAnalyseTablesMap,
        Map<String, List<AnalyseResultTable>> kit2ClassAnalyseTablesMap,
        Map<String, List<AnalyseResultTable>> kit2FieldAnalyseTablesMap,
        Map<String, AnalyseResultInternalLinkDest> kit2AnalyseTableStartPositionMap) throws IOException {
        // firstly, draw Kit method statistics table
        for (Map.Entry<String, List<AnalyseResultTable>> kit2MethodAnalyseTablesEntry : kit2MethodAnalyseTablesMap
            .entrySet()) {
            String kit = kit2MethodAnalyseTablesEntry.getKey();
            buildKit2AnalyseTableStartPositionMap(kit, pdfHelper, kit2AnalyseTableStartPositionMap);

            AnalyseResultLine kitLine = AnalyseResultLine.builder().line(kit).build();
            pdfHelper.drawLine(kitLine);

            pdfHelper.drawTablesHorizon(kit2MethodAnalyseTablesEntry.getValue());

            if (kit2ClassAnalyseTablesMap.containsKey(kit)) {
                pdfHelper.drawTablesHorizon(kit2ClassAnalyseTablesMap.get(kit));
            }

            if (kit2FieldAnalyseTablesMap.containsKey(kit)) {
                pdfHelper.drawTablesHorizon(kit2FieldAnalyseTablesMap.get(kit));
            }
        }

        // secondly, draw Kit class statistics table
        for (Map.Entry<String, List<AnalyseResultTable>> kit2ClassAnalyseTablesEntry : kit2ClassAnalyseTablesMap
            .entrySet()) {
            String kit = kit2ClassAnalyseTablesEntry.getKey();
            if (kit2MethodAnalyseTablesMap.containsKey(kit)) {
                continue;
            }

            buildKit2AnalyseTableStartPositionMap(kit, pdfHelper, kit2AnalyseTableStartPositionMap);

            AnalyseResultLine kitLine = AnalyseResultLine.builder().line(kit).build();
            pdfHelper.drawLine(kitLine);

            pdfHelper.drawTablesHorizon(kit2ClassAnalyseTablesEntry.getValue());

            if (kit2FieldAnalyseTablesMap.containsKey(kit)) {
                pdfHelper.drawTablesHorizon(kit2FieldAnalyseTablesMap.get(kit));
            }
        }

        // finally, draw Kit field statistics table
        for (Map.Entry<String, List<AnalyseResultTable>> kit2FieldAnalyseTablesEntry : kit2FieldAnalyseTablesMap
            .entrySet()) {
            String kit = kit2FieldAnalyseTablesEntry.getKey();
            if (kit2MethodAnalyseTablesMap.containsKey(kit) || kit2ClassAnalyseTablesMap.containsKey(kit)) {
                continue;
            }

            buildKit2AnalyseTableStartPositionMap(kit, pdfHelper, kit2AnalyseTableStartPositionMap);

            AnalyseResultLine kitLine = AnalyseResultLine.builder().line(kit).build();
            pdfHelper.drawLine(kitLine);

            pdfHelper.drawTablesHorizon(kit2FieldAnalyseTablesEntry.getValue());
        }
    }

    private static void drawFeedback(PdfHelper pdfHelper, String line, String[][] feedbackLinkTable)
        throws IOException {
        AnalyseResultLine feedbackLine = AnalyseResultLine.builder()
            .line(line)
            .hyperlinkTable(new AnalyseResultHyperlinkTable(UIConstants.FeedbackLinkTable.COL_WIDTH_4_FEEDBACK,
                UIConstants.FeedbackLinkTable.CEL_WIDTH_4_FEEDBACK, UIConstants.FeedbackLinkTable.CEL_HEIGHT_4_FEEDBACK,
                UIConstants.FeedbackLinkTable.START_X, feedbackLinkTable))
            .build();
        pdfHelper.drawLine(feedbackLine);
    }

    private static void buildKitStatisticsHeaderTable(List<AnalyseResultTable> headerTables) {
        int kitColCount = UIConstants.Util.AnalyseResultExport.StatisticsHeaderTable.KIT_COL_COUNT;
        int totalColCount = UIConstants.Util.AnalyseResultExport.StatisticsHeaderTable.TOTAL_COL_COUNT;
        int sdkColCount = UIConstants.Util.AnalyseResultExport.StatisticsHeaderTable.SDK_COL_COUNT;
        int supportColCount = UIConstants.Util.AnalyseResultExport.StatisticsHeaderTable.SUPPORT_COL_COUNT;
        int methodAutoColCount = UIConstants.Util.AnalyseResultExport.StatisticsHeaderTable.METHOD_AUTO_COL_COUNT;
        int classAutoColCount = UIConstants.Util.AnalyseResultExport.StatisticsHeaderTable.CLASS_AUTO_COL_COUNT;
        int fieldAutoColCount = UIConstants.Util.AnalyseResultExport.StatisticsHeaderTable.FIELD_AUTO_COL_COUNT;

        String[][] kitTable = new String[1][kitColCount];
        String[] kitHeader = new String[kitColCount];
        kitHeader[0] = "";
        kitTable[0] = kitHeader;
        headerTables.add(new AnalyseResultTable(UIConstants.KitStatisticsTable.HEADER_TABLE_COL_WIDTH_4_KIT, kitTable));

        String[][] totalTable = new String[1][totalColCount];
        String[] totalHeader = new String[totalColCount];
        totalHeader[0] = "Method usage";
        totalHeader[1] = "Class usage";
        totalHeader[2] = "Field usage";
        totalTable[0] = totalHeader;
        headerTables
            .add(new AnalyseResultTable(UIConstants.KitStatisticsTable.HEADER_TABLE_COL_WIDTH_4_TOTAL, totalTable));

        String[][] sdkTable = new String[1][sdkColCount];
        String[] sdkHeader = new String[sdkColCount];
        sdkHeader[0] = "SdkVersion";
        sdkTable[0] = sdkHeader;
        headerTables.add(new AnalyseResultTable(UIConstants.KitStatisticsTable.HEADER_TABLE_COL_WIDTH_4_SDK, sdkTable));

        String[][] supportTable = new String[1][supportColCount];
        String[] supportHeader = new String[supportColCount];
        supportHeader[0] = "HMS Core benchmark(method)";
        supportHeader[1] = "HMS Core benchmark(class)";
        supportHeader[2] = "HMS Core benchmark(field)";
        supportTable[0] = supportHeader;
        headerTables
            .add(new AnalyseResultTable(UIConstants.KitStatisticsTable.HEADER_TABLE_COL_WIDTH_4_SUPPORT, supportTable));

        String[][] methodAutoTable = new String[1][methodAutoColCount];
        String[] methodAutoHeader = new String[methodAutoColCount];
        methodAutoHeader[0] = "Add HMS API(method)";
        methodAutoHeader[1] = "To HMS API(method)";
        methodAutoTable[0] = methodAutoHeader;
        headerTables
            .add(new AnalyseResultTable(UIConstants.KitStatisticsTable.HEADER_TABLE_COL_WIDTH_4_AUTO, methodAutoTable));

        String[][] classAutoTable = new String[1][classAutoColCount];
        String[] classAutoHeader = new String[classAutoColCount];
        classAutoHeader[0] = "Add HMS API(class)";
        classAutoHeader[1] = "To HMS API(class)";
        classAutoTable[0] = classAutoHeader;
        headerTables
            .add(new AnalyseResultTable(UIConstants.KitStatisticsTable.HEADER_TABLE_COL_WIDTH_4_AUTO, classAutoTable));

        String[][] fieldAutoTable = new String[1][fieldAutoColCount];
        String[] fieldAutoHeader = new String[fieldAutoColCount];
        fieldAutoHeader[0] = "Add HMS API(field)";
        fieldAutoHeader[1] = "To HMS API(field)";
        fieldAutoTable[0] = fieldAutoHeader;
        headerTables
            .add(new AnalyseResultTable(UIConstants.KitStatisticsTable.HEADER_TABLE_COL_WIDTH_4_AUTO, fieldAutoTable));
    }

    private static void buildKitStatisticsTables(List<KitStatisticsResult> kitStatisticsResults,
        ProjectStatisticsResult projectStatisticsResult, List<AnalyseResultTable> tables) {
        int dataRowCount = kitStatisticsResults.size();
        // add table header row and end row
        int rowCount = dataRowCount + UIConstants.Util.AnalyseResultExport.StatisticsTables.ROW_COUNT_INDEX;
        int totalTableColCount = UIConstants.Util.AnalyseResultExport.StatisticsTables.TOTAL_TOTLE_COL_COUNT;
        int sdkTableColCount = UIConstants.Util.AnalyseResultExport.StatisticsTables.SDK_TABLE_COL_COUNT;
        int supportTableColCount = UIConstants.Util.AnalyseResultExport.StatisticsTables.SUPPORT_TABLE_COL_COUNT;
        int autoTableColCount = UIConstants.Util.AnalyseResultExport.StatisticsTables.AUTO_TABLE_COL_COUNT;

        String[][] totalTable = new String[rowCount][totalTableColCount];
        String[] totalHeader = new String[totalTableColCount];
        totalHeader[0] = "Integrated Kit";
        totalHeader[1] = "Api count";
        totalHeader[2] = "Call count";
        totalHeader[3] = "Api count";
        totalHeader[4] = "Call count";
        totalHeader[5] = "Api count";
        totalHeader[6] = "Call count";
        totalTable[0] = totalHeader;

        String[][] sdkTable = new String[rowCount][sdkTableColCount];
        String[] sdkHeader = new String[sdkTableColCount];
        sdkHeader[0] = "TargetSdkVersion";
        sdkHeader[1] = "MinSdkVersion(Add HMS API)";
        sdkHeader[2] = "MinSdkVersion(To HMS API)";
        sdkTable[0] = sdkHeader;

        String[][] supportTable = new String[rowCount][supportTableColCount];
        String[] supportHeader = new String[supportTableColCount];
        supportHeader[0] = "Unsupported count";
        supportHeader[1] = "Supported count";
        supportHeader[2] = "Unsupported count";
        supportHeader[3] = "Supported count";
        supportHeader[4] = "Unsupported count";
        supportHeader[5] = "Supported count";
        supportTable[0] = supportHeader;

        String[][] autoTable = new String[rowCount][autoTableColCount];
        String[] autoHeader = new String[autoTableColCount];
        autoHeader[0] = "Auto";
        autoHeader[1] = "Manual";
        autoHeader[2] = "Auto";
        autoHeader[3] = "Manual";
        autoHeader[4] = "Auto";
        autoHeader[5] = "Manual";
        autoHeader[6] = "Auto";
        autoHeader[7] = "Manual";
        autoHeader[8] = "Auto";
        autoHeader[9] = "Manual";
        autoHeader[10] = "Auto";
        autoHeader[11] = "Manual";
        autoTable[0] = autoHeader;

        for (int i = 0; i < dataRowCount; i++) {
            KitStatisticsResult kitStatistics = kitStatisticsResults.get(i);
            KitApiStatisticsResult methodStatistics = kitStatistics.getMethodStatisticsResult();
            KitApiStatisticsResult classStatistics = kitStatistics.getClassStatisticsResult();
            KitApiStatisticsResult fieldStatistics = kitStatistics.getFieldStatisticsResult();
            String[] totalTableRow = new String[totalTableColCount];
            totalTableRow[0] = kitStatistics.getKit();
            totalTableRow[1] = String.valueOf(methodStatistics.getApiCount());
            totalTableRow[2] = String.valueOf(methodStatistics.getBlockCount());
            totalTableRow[3] = String.valueOf(classStatistics.getApiCount());
            totalTableRow[4] = String.valueOf(classStatistics.getBlockCount());
            totalTableRow[5] = String.valueOf(fieldStatistics.getApiCount());
            totalTableRow[6] = String.valueOf(fieldStatistics.getBlockCount());
            totalTable[i + 1] = totalTableRow;

            KitSdkVersion kitSdkVersion = kitStatistics.getKitSdkVersion();
            String[] sdkTableRow = new String[sdkTableColCount];
            if (kitSdkVersion == null) {
                sdkTableRow[0] = Constant.NA;
                sdkTableRow[1] = Constant.NA;
                sdkTableRow[2] = Constant.NA;
            } else {
                sdkTableRow[0] = String.valueOf(kitSdkVersion.getTargetSdkVersion());
                sdkTableRow[1] = String.valueOf(kitSdkVersion.getMinSdkVersion4GaddH());
                sdkTableRow[2] = String.valueOf(kitSdkVersion.getMinSdkVersion4G2H());
            }
            sdkTable[i + 1] = sdkTableRow;

            String[] supportTableRow = new String[supportTableColCount];
            supportTableRow[0] = String.valueOf(methodStatistics.getUnsupportCount());
            supportTableRow[1] = String.valueOf(methodStatistics.getSupportCount());
            supportTableRow[2] = String.valueOf(classStatistics.getUnsupportCount());
            supportTableRow[3] = String.valueOf(classStatistics.getSupportCount());
            supportTableRow[4] = String.valueOf(fieldStatistics.getUnsupportCount());
            supportTableRow[5] = String.valueOf(fieldStatistics.getSupportCount());
            supportTable[i + 1] = supportTableRow;

            String[] autoTableRow = new String[autoTableColCount];
            autoTableRow[0] = String.valueOf(methodStatistics.getAutoCount4GaddH());
            autoTableRow[1] = String.valueOf(methodStatistics.getManualCount4GaddH());
            autoTableRow[2] = String.valueOf(methodStatistics.getAutoCount4G2H());
            autoTableRow[3] = String.valueOf(methodStatistics.getManualCount4G2H());
            autoTableRow[4] = String.valueOf(classStatistics.getAutoCount4GaddH());
            autoTableRow[5] = String.valueOf(classStatistics.getManualCount4GaddH());
            autoTableRow[6] = String.valueOf(classStatistics.getAutoCount4G2H());
            autoTableRow[7] = String.valueOf(classStatistics.getManualCount4G2H());
            autoTableRow[8] = String.valueOf(fieldStatistics.getAutoCount4GaddH());
            autoTableRow[9] = String.valueOf(fieldStatistics.getManualCount4GaddH());
            autoTableRow[10] = String.valueOf(fieldStatistics.getAutoCount4G2H());
            autoTableRow[11] = String.valueOf(fieldStatistics.getManualCount4G2H());
            autoTable[i + 1] = autoTableRow;
        }

        KitSdkVersion projectKitSdkVersion = projectStatisticsResult.getKitSdkVersion();
        ProjectApiStatisticsResult projectMethodStatistics = projectStatisticsResult.getMethodStatisticsResult();
        ProjectApiStatisticsResult projectClassStatistics = projectStatisticsResult.getClassStatisticsResult();
        ProjectApiStatisticsResult projectFieldStatistics = projectStatisticsResult.getFieldStatisticsResult();
        String[] totalTableSummary = new String[totalTableColCount];
        totalTableSummary[0] = "Total";
        totalTableSummary[1] = String.valueOf(projectMethodStatistics.getApiCount());
        totalTableSummary[2] = String.valueOf(projectMethodStatistics.getBlockCount());
        totalTableSummary[3] = String.valueOf(projectClassStatistics.getApiCount());
        totalTableSummary[4] = String.valueOf(projectClassStatistics.getBlockCount());
        totalTableSummary[5] = String.valueOf(projectFieldStatistics.getApiCount());
        totalTableSummary[6] = String.valueOf(projectFieldStatistics.getBlockCount());
        totalTable[rowCount - 1] = totalTableSummary;
        tables.add(new AnalyseResultTable(UIConstants.KitStatisticsTable.COL_WIDTH_4_TOTAL, totalTable));

        String[] sdkTableSummary = new String[sdkTableColCount];
        if (projectKitSdkVersion == null) {
            sdkTableSummary[0] = Constant.NA;
            sdkTableSummary[1] = Constant.NA;
            sdkTableSummary[2] = Constant.NA;
        } else {
            sdkTableSummary[0] = String.valueOf(projectKitSdkVersion.getTargetSdkVersion());
            sdkTableSummary[1] = String.valueOf(projectKitSdkVersion.getMinSdkVersion4GaddH());
            sdkTableSummary[2] = String.valueOf(projectKitSdkVersion.getMinSdkVersion4G2H());
        }
        sdkTable[rowCount - 1] = sdkTableSummary;
        tables.add(new AnalyseResultTable(UIConstants.KitStatisticsTable.COL_WIDTH_4_SDK, sdkTable));

        String[] supportTableSummary = new String[supportTableColCount];
        supportTableSummary[0] = projectMethodStatistics.getUnsupportRate();
        supportTableSummary[1] = projectMethodStatistics.getSupportRate();
        supportTableSummary[2] = projectClassStatistics.getUnsupportRate();
        supportTableSummary[3] = projectClassStatistics.getSupportRate();
        supportTableSummary[4] = projectFieldStatistics.getUnsupportRate();
        supportTableSummary[5] = projectFieldStatistics.getSupportRate();
        supportTable[rowCount - 1] = supportTableSummary;
        tables.add(new AnalyseResultTable(UIConstants.KitStatisticsTable.COL_WIDTH_4_SUPPORT_RATE, supportTable));

        String[] autoTableSummary = new String[autoTableColCount];
        autoTableSummary[0] = projectMethodStatistics.getAutoRate4GaddH();
        autoTableSummary[1] = projectMethodStatistics.getManualRate4GaddH();
        autoTableSummary[2] = projectMethodStatistics.getAutoRate4G2H();
        autoTableSummary[3] = projectMethodStatistics.getManualRate4G2H();
        autoTableSummary[4] = projectClassStatistics.getAutoRate4GaddH();
        autoTableSummary[5] = projectClassStatistics.getManualRate4GaddH();
        autoTableSummary[6] = projectClassStatistics.getAutoRate4G2H();
        autoTableSummary[7] = projectClassStatistics.getManualRate4G2H();
        autoTableSummary[8] = projectFieldStatistics.getAutoRate4GaddH();
        autoTableSummary[9] = projectFieldStatistics.getManualRate4GaddH();
        autoTableSummary[10] = projectFieldStatistics.getAutoRate4G2H();
        autoTableSummary[11] = projectFieldStatistics.getManualRate4G2H();
        autoTable[rowCount - 1] = autoTableSummary;
        tables.add(new AnalyseResultTable(UIConstants.KitStatisticsTable.COL_WIDTH_4_AUTO_RATE, autoTable));
    }

    private static void buildKitStatisticsLinkTables(List<KitStatisticsResult> kitStatisticsResults,
        Map<String, AnalyseResultInternalLinkDest> kit2AnalyseTableStartPositionMap,
        List<AnalyseResultInternalLinkDest[][]> linkTables) {
        int dataRowCount = kitStatisticsResults.size();
        // add table header row and end row
        int rowCount = dataRowCount + UIConstants.Util.AnalyseResultExport.StatisticsLinkTables.ROW_COUNT_INDEX;
        int kitLinkTableColCount = UIConstants.Util.AnalyseResultExport.StatisticsLinkTables.KIT_LINK_TABLE_COL_COUNT;
        AnalyseResultInternalLinkDest[][] kitLinkTable =
            new AnalyseResultInternalLinkDest[rowCount][kitLinkTableColCount];

        for (int i = 0; i < dataRowCount; i++) {
            KitStatisticsResult kitStatistics = kitStatisticsResults.get(i);
            String kit = kitStatistics.getKit();
            if (kit2AnalyseTableStartPositionMap.containsKey(kit)) {
                AnalyseResultInternalLinkDest[] kitLinkTableCol =
                    new AnalyseResultInternalLinkDest[kitLinkTableColCount];
                kitLinkTableCol[0] = kit2AnalyseTableStartPositionMap.get(kit);
                kitLinkTable[i + 1] = kitLinkTableCol;
            }
        }

        linkTables.add(kitLinkTable);
    }

    private static boolean validateApiAnalyseResultNum(Map<String, List<ApiAnalyseResult>> kit2MethodAnalyseResultsMap,
        Map<String, List<ApiAnalyseResult>> kit2ClassAnalyseResultsMap,
        Map<String, List<ApiAnalyseResult>> kit2FieldAnalyseResultsMap) {
        int methodNum = countApiAnalyseResultNum(kit2MethodAnalyseResultsMap);
        int methodMaxNum = Integer.parseInt(PropertyUtil.readProperty("method_analyse_result_export_max_num"));
        if (methodNum > methodMaxNum) {
            log.error("method analyse result export max num exceed, methodNum: {}, methodMaxNum: {}.", methodNum,
                methodMaxNum);
            return false;
        }

        int classNum = countApiAnalyseResultNum(kit2ClassAnalyseResultsMap);
        int classMaxNum = Integer.parseInt(PropertyUtil.readProperty("class_analyse_result_export_max_num"));
        if (classNum > classMaxNum) {
            log.error("class analyse result export max num exceed, classNum: {}, classMaxNum: {}.", classNum,
                classMaxNum);
            return false;
        }

        int fieldNum = countApiAnalyseResultNum(kit2FieldAnalyseResultsMap);
        int fieldMaxNum = Integer.parseInt(PropertyUtil.readProperty("field_analyse_result_export_max_num"));
        if (fieldNum > fieldMaxNum) {
            log.error("field analyse result export max num exceed, fieldNum: {}, fieldMaxNum: {}.", fieldNum,
                fieldMaxNum);
            return false;
        }
        return true;
    }

    private static int countApiAnalyseResultNum(Map<String, List<ApiAnalyseResult>> kit2ApiAnalyseResultsMap) {
        int num = 0;
        for (Map.Entry<String, List<ApiAnalyseResult>> entry : kit2ApiAnalyseResultsMap.entrySet()) {
            num += entry.getValue().size();
        }
        return num;
    }

    private static void buildKit2AnalyseTablesMap(Map<String, List<ApiAnalyseResult>> kit2ApiAnalyseResultsMap,
        String apiHeaderCol, Map<String, List<AnalyseResultTable>> kit2AnalyseTablesMap) {
        kit2ApiAnalyseResultsMap.forEach((kit, apiAnalyseResults) -> {
            List<AnalyseResultTable> tables = new ArrayList<>();
            buildKit2AnalyseTables(apiAnalyseResults, apiHeaderCol, tables);
            kit2AnalyseTablesMap.put(kit, tables);
        });
    }

    private static void buildKit2AnalyseTableStartPositionMap(String kit, PdfHelper pdfHelper,
        Map<String, AnalyseResultInternalLinkDest> kit2AnalyseTableStartPositionMap) {
        // destY use previous line of Kit analyse table
        kit2AnalyseTableStartPositionMap.put(kit,
            AnalyseResultInternalLinkDest.builder()
                .destPageIndex(pdfHelper.getPageIndex())
                .destY((int) (pdfHelper.getCurrentY() + UIConstants.CommonAnalyseResult.CONTENT_MARGIN))
                .build());
    }

    private static void buildKit2AnalyseTables(List<ApiAnalyseResult> apiAnalyseResults, String apiHeaderCol,
        List<AnalyseResultTable> tables) {
        int dataRowCount = apiAnalyseResults.size();
        // add table header row
        int rowCount = dataRowCount + UIConstants.Util.AnalyseResultExport.AnalyseTables.ROW_COUNT_INDEX;
        int apiTableColCount = UIConstants.Util.AnalyseResultExport.AnalyseTables.API_TABLE_COL_COUNT;
        int countTableColCount = UIConstants.Util.AnalyseResultExport.AnalyseTables.COUNT_TABLE_COL_COUNT;
        int supportTableColCount = UIConstants.Util.AnalyseResultExport.AnalyseTables.SUPPORT_TABLE_COL_COUNT;
        int autoTableColCount = UIConstants.Util.AnalyseResultExport.AnalyseTables.AUTO_TABLE_COL_COUNT;

        String[][] apiTable = new String[rowCount][apiTableColCount];
        String[] apiHeader = new String[apiTableColCount];
        apiHeader[0] = apiHeaderCol;
        apiTable[0] = apiHeader;

        String[][] countTable = new String[rowCount][countTableColCount];
        String[] countHeader = new String[countTableColCount];
        countHeader[0] = "File count";
        countHeader[1] = "Call count";
        countTable[0] = countHeader;

        String[][] supportTable = new String[rowCount][supportTableColCount];
        String[] supportHeader = new String[supportTableColCount];
        supportHeader[0] = "HMS Core support";
        supportTable[0] = supportHeader;

        String[][] supportHyperlinkTable = new String[rowCount][supportTableColCount];

        String[][] autoTable = new String[rowCount][autoTableColCount];
        String[] autoHeader = new String[autoTableColCount];
        autoHeader[0] = "Add HMS API";
        autoHeader[1] = "To HMS API";
        autoTable[0] = autoHeader;

        for (int i = 0; i < dataRowCount; i++) {
            ApiAnalyseResult apiStatistics = apiAnalyseResults.get(i);
            String[] apiTableCol = new String[apiTableColCount];
            apiTableCol[0] = apiStatistics.getOldName();
            apiTable[i + 1] = apiTableCol;

            String[] countTableCol = new String[countTableColCount];
            countTableCol[0] = String.valueOf(apiStatistics.getFileCount());
            countTableCol[1] = String.valueOf(apiStatistics.getBlockCount());
            countTable[i + 1] = countTableCol;

            String[] supportTableCol = new String[supportTableColCount];
            supportTableCol[0] = newApi2Readable(apiStatistics.getIsAuto4G2H(), apiStatistics.getNewName4G2H());
            supportTable[i + 1] = supportTableCol;

            if (needShowLink(apiStatistics.getIsAuto4G2H(), apiStatistics.getUrl())) {
                String[] supportHyperlinkTableCol = new String[supportTableColCount];
                supportHyperlinkTableCol[0] = apiStatistics.getUrl();
                supportHyperlinkTable[i + 1] = supportHyperlinkTableCol;
            }

            String[] autoTableCol = new String[autoTableColCount];
            autoTableCol[0] = auto2Readable(apiStatistics.getIsAuto4GaddH());
            autoTableCol[1] = auto2Readable(apiStatistics.getIsAuto4G2H());
            autoTable[i + 1] = autoTableCol;
        }
        tables.add(new AnalyseResultTable(UIConstants.KitApiAnalyseTable.COL_WIDTH_4_API, apiTable));
        tables.add(new AnalyseResultTable(UIConstants.KitApiAnalyseTable.COL_WIDTH_4_COUNT, countTable));
        tables.add(new AnalyseResultTable(UIConstants.KitApiAnalyseTable.COL_WIDTH_4_API, supportTable,
            new AnalyseResultHyperlinkTable(UIConstants.KitApiAnalyseTable.COL_WIDTH_4_API,
                UIConstants.KitApiAnalyseTable.LINK_TABLE_CEL_WIDTH_4_API,
                UIConstants.KitApiAnalyseTable.LINK_TABLE_CEL_HEIGHT_4_API, supportHyperlinkTable)));
        tables.add(new AnalyseResultTable(UIConstants.KitApiAnalyseTable.COL_WIDTH_4_AUTO, autoTable));
    }

    private static String[][] buildFeedbackLinkTable(String projectBasePath) {
        String allianceDomain = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.ALLIANCE_DOMAIN, String.class, "");
        String feedbackUrl = allianceDomain + HmsConvertorBundle.message("feedback_url");

        int rowCount = 1;
        int colCount = 1;

        String[][] table = new String[rowCount][colCount];
        String[] col = new String[colCount];
        col[0] = feedbackUrl;
        table[0] = col;
        return table;
    }

    private static String getExportFilePath(String projectBasePath) {
        String projectName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.INSPECT_FOLDER, String.class, "");
        String currentTimestamp = LocalDateTime.now().format(Constant.BASIC_ISO_DATETIME);
        String analyseResultFileName = String.join(".", projectName, currentTimestamp, "analysisResult", "pdf");

        String analyseResultDirectory = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.BACK_PATH, String.class, "");
        return analyseResultDirectory + Constant.UNIX_FILE_SEPARATOR + analyseResultFileName;
    }

    private static boolean needShowLink(Boolean isAuto4G2H, String url) {
        return isAuto4G2H != null && !isAuto4G2H && StringUtils.isNotBlank(url);
    }

    private static String auto2Readable(Boolean isAuto) {
        if (isAuto == null) {
            return "Unsupport";
        } else if (isAuto) {
            return "Auto";
        } else {
            return "Manual";
        }
    }

    private static String newApi2Readable(Boolean isAuto, String newApi) {
        if (isAuto == null) {
            return "Unsupport";
        } else if (isAuto) {
            if (StringUtils.isBlank(newApi)) {
                return "Auto";
            }
            return newApi;
        } else {
            return "Manual";
        }
    }
}
