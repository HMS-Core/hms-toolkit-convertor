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

package com.huawei.hms.convertor.openapi;

import com.huawei.hms.convertor.core.bi.BITraceManager;
import com.huawei.hms.convertor.core.bi.bean.CancelListenerBean;
import com.huawei.hms.convertor.core.bi.bean.ConversionOperationBean;
import com.huawei.hms.convertor.core.bi.bean.ExportClickBean;
import com.huawei.hms.convertor.core.bi.bean.FunctionSelectionBean;
import com.huawei.hms.convertor.core.bi.bean.HelpClickBean;
import com.huawei.hms.convertor.core.bi.bean.JavaDocMenuBean;
import com.huawei.hms.convertor.core.bi.bean.MenuSelectionBean;
import com.huawei.hms.convertor.core.bi.bean.TimeCostBean;
import com.huawei.hms.convertor.core.bi.enumration.AnalyseExportEnum;
import com.huawei.hms.convertor.core.bi.enumration.BIActionEnum;
import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.ConversionHelpEnum;
import com.huawei.hms.convertor.core.bi.enumration.JavaDocActionEnum;
import com.huawei.hms.convertor.core.bi.enumration.MenuEnum;
import com.huawei.hms.convertor.core.bi.enumration.OperationViewEnum;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * bi report service
 *
 * @since 2020-03-27
 */
@Slf4j
public final class BIReportService {
    private static final BIReportService INSTANCE = new BIReportService();

    private BIReportService() {
    }

    public static BIReportService getInstance() {
        return INSTANCE;
    }

    /**
     * Trace menu selection.
     *
     * @param projectPath project path
     * @param menu menu selected
     */
    public void traceMenuSelection(String projectPath, MenuEnum menu) {
        if (menu == null) {
            return;
        }

        if (menu.equals(MenuEnum.NEW_CONVERSION)) {
            BITraceManager.getInstance()
                .setNewConversionBeginTime(projectPath, String.valueOf(System.currentTimeMillis()));
        }
        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.MENU_SELECTION,
                MenuSelectionBean.builder().menu(menu.getValue()).build());
    }

    /**
     * Trace project source info.
     *
     * @param projectPath project path
     */
    public void traceSourceInfo(String projectPath) {
        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.SOURCE_INFO, BITraceManager.getInstance().getSourceInfo(projectPath));
    }

    /**
     * Trace operation of processing the analysis result.
     *
     * @param projectPath project path
     * @param data bi data.
     */
    public void traceConversionOperation(String projectPath, ConversionOperationBean data) {
        BITraceManager.getInstance().traceData(projectPath, BIActionEnum.CONVERT_OPERATION, data);
    }

    /**
     * Trace Analyze Result.
     *
     * @param projectPath project path
     */
    public void traceAnalyzeResult(String projectPath) {
        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.ANALYZE_RESULT,
                BITraceManager.getInstance().getAnalyzeResult(projectPath));
    }

    /**
     * Trace usage of some function options in the tool.
     *
     * @param projectPath project path
     * @param data bi data.
     */
    public void traceFunctionSelection(String projectPath, FunctionSelectionBean data) {
        BITraceManager.getInstance().traceData(projectPath, BIActionEnum.FUNCTION_SELECTION, data);
    }

    /**
     * Trace Cancellation of page operations.
     *
     * @param projectPath project path
     * @param view cancel view.
     */
    public void traceCancelListener(String projectPath, CancelableViewEnum view) {
        if (view == null) {
            return;
        }

        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.CANCEL_LISTENER,
                CancelListenerBean.builder().view(view.getValue()).build());
    }

    /**
     * Trace help click.
     *
     * @param projectPath project path
     * @param help help view.
     */
    public void traceHelpClick(String projectPath, ConversionHelpEnum help) {
        if (help == null) {
            return;
        }
        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.HELP_CLICK, HelpClickBean.builder().helpView(help.getValue()).build());
    }

    public void traceExportClick(String projectPath, AnalyseExportEnum export) {
        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.EXPORT_CLICK,
                ExportClickBean.builder().exportView(export.getValue()).build());
    }

    /**
     * Trace analysis time cost.
     *
     * @param projectPath project path
     * @param view operation view.
     * @param timeCost time cost
     * @param jvmXmx jvmXmx option.
     */
    public void traceTimeAnalyzeCost(String projectPath, OperationViewEnum view, String timeCost, String jvmXmx) {
        if (view == null) {
            return;
        }

        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.TIME_ANALYZE_COST,
                TimeCostBean.builder().analyzeScenario(view.getValue()).jvmXmx(jvmXmx).timeCost(timeCost).build());
    }

    /**
     * Get the beginning time of a new conversion.
     *
     * @param projectPath project path
     * @return The beginning time of a new conversion.
     */
    public String getNewConversionBeginTime(String projectPath) {
        return BITraceManager.getInstance().getNewConversionBeginTime(projectPath);
    }

    /**
     * Get analyze process jvm Xmx
     *
     * @param projectPath project path
     * @return jvm Xmx
     */
    public String getJvmXmx(String projectPath) {
        return BITraceManager.getInstance().getJvmXmx(projectPath);
    }

    /**
     * Set analyze process jvm Xmx
     *
     * @param projectPath project path
     */
    public void setJvmXmx(String projectPath, List<String> jvmOpts) {
        BITraceManager.getInstance().setJvmXmx(projectPath, jvmOpts);
    }

    /**
     * init bi report service.
     *
     * @param projectPath project path
     */
    public void init(String projectPath) {
        BITraceManager.getInstance().init(projectPath);
    }

    /**
     * close the android studio, clear the cached trace service instance.
     *
     * @param projectPath project path
     */
    public void clearTraceService(String projectPath) {
        BITraceManager.getInstance().clearTraceServiceInstance(projectPath);
    }

    /**
     * Trace javadoc selection.
     *
     * @param projectPath project path
     * @param menu menu selected
     */
    public void traceJavaDocSelection(String projectPath, JavaDocActionEnum menu) {
        if (menu == null) {
            return;
        }

        if (menu.equals(JavaDocActionEnum.HMS_API_HELPER)) {
            BITraceManager.getInstance()
                .setNewConversionBeginTime(projectPath, String.valueOf(System.currentTimeMillis()));
        }
        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.JAVADOC_SELECTION,
                JavaDocMenuBean.builder().menu(menu.getValue()).build());
    }

    public void traceJavaDocSearch(String projectPath, JavaDocActionEnum menu) {
        if (menu == null) {
            return;
        }

        if (menu.equals(JavaDocActionEnum.HMS_API_SEARCH)) {
            BITraceManager.getInstance()
                .setNewConversionBeginTime(projectPath, String.valueOf(System.currentTimeMillis()));
        }
        BITraceManager.getInstance()
            .traceData(projectPath, BIActionEnum.JAVADOC_SEARCH,
                JavaDocMenuBean.builder().menu(menu.getValue()).build());
    }

}
