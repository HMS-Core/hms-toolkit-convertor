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

package com.huawei.hms.convertor.idea.ui.actions;

import static com.huawei.hms.convertor.idea.ui.actions.HmsConvertorAction.saveConversionAndSummary;

import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.MenuEnum;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.PrivacyStatementChecker;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.result.HmsConvertorToolWindow;
import com.huawei.hms.convertor.idea.ui.result.conversion.DefectItem;
import com.huawei.hms.convertor.idea.util.ActionUtil;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Open last conversion action
 *
 * @since 2019/12/7
 */
@Slf4j
public class OpenLastAction extends AnAction {
    public static void openLastConversion(Project project) {
        try {
            if (!HmsConvertorUtil.getHmsConvertorToolWindow(project).isPresent()) {
                logWarnMessage("no_tool_window");
                throw new NoSuchFileException(HmsConvertorBundle.message("no_tool_window"));
            }

            ConfigCacheService configCacheService = ConfigCacheService.getInstance();
            if ((configCacheService == null) || StringUtil.isEmpty(configCacheService
                .getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH, String.class, ""))) {
                logWarnMessage("no_analyze_result");
                throw new NoSuchFileException(HmsConvertorBundle.message("no_analyze_result"));
            }
            String repoID = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.REPO_ID,
                String.class, "");
            if (StringUtil.isEmpty(repoID)) {
                logWarnMessage("no_analyze_result");
                throw new NoSuchFileException(HmsConvertorBundle.message("no_last_conversion"));
            }
            final String resultPath =
                FileUtil.unifyToUnixFileSeparator(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH) + repoID;
            if (Files.notExists(Paths.get(resultPath, ProjectConstants.Result.LAST_SUMMARY_JSON))
                || Files.notExists(Paths.get(resultPath, ProjectConstants.Result.LAST_CONVERSION_JSON))) {
                logWarnMessage("no_analyze_result");
                throw new NoSuchFileException(HmsConvertorBundle.message("no_last_conversion"));
            }

            if (!checkSavedFiles(repoID, project, configCacheService.getProjectConfig(project.getBasePath(),
                ConfigKeyConstants.COMMENT, Boolean.class, false))) {
                log.info("No record saved");
                return;
            }

            final HmsConvertorToolWindow hmsConvertorToolWindow =
                HmsConvertorUtil.getHmsConvertorToolWindow(project).get();
            hmsConvertorToolWindow.getSummaryToolWindow().loadLastConversion();
            hmsConvertorToolWindow.getSourceConvertorToolWindow().loadLastConversion();
            hmsConvertorToolWindow.getXmsDiffWindow().loadXmsDiff();
            hmsConvertorToolWindow.showTabbedPane(UIConstants.ToolWindow.TAB_CONVERSION_INDEX);
            showSuccessNotice(configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.COMMENT,
                Boolean.class, false), project);
        } catch (IOException e) {
            log.warn("Error during {}.", e.getMessage(), e);
            BalloonNotifications.showWarnNotification(e.getMessage(), project, Constant.PLUGIN_NAME, true);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        if (PrivacyStatementChecker.isNotAgreed(project)) {
            // bi report action: trace cancel operation.
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.PRIVACY);
            return;
        }
        // bi report action: menu click.
        BIReportService.getInstance().traceMenuSelection(project.getBasePath(), MenuEnum.OPEN_LAST);

        if ((!project.isInitialized()) || (project.isDisposed()) || (!project.isOpen())) {
            return;
        }

        // Save the data before loading the previous scanning result
        // to avoid misoperations during conversion.
        saveConversionAndSummary(project);
        openLastConversion(project);
    }

    @Override
    public void update(AnActionEvent e) {
        ActionUtil.updateAction(e, IconUtil.OPEN_LAST);
    }

    private static void logWarnMessage(String msg) {
        log.warn("Open last convert result: {}.", HmsConvertorBundle.message(msg));
    }

    private static boolean checkSavedFiles(String repoID, Project project, boolean isCommentMode) throws IOException {
        String convertSaveFilePath = PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + repoID
            + Constant.UNIX_FILE_SEPARATOR + ProjectConstants.Result.LAST_CONVERSION_JSON;
        String lastConversionString = FileUtil.readToString(convertSaveFilePath, StandardCharsets.UTF_8.toString());
        String summarySaveFilePath = PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + repoID
            + Constant.UNIX_FILE_SEPARATOR + ProjectConstants.Result.LAST_SUMMARY_JSON;
        String lastSummaryString = FileUtil.readToString(summarySaveFilePath, StandardCharsets.UTF_8.toString());
        List<DefectItem> defectItemList = JSON.parseArray(lastConversionString, DefectItem.class);
        JSONObject jsonObject = JSON.parseObject(lastSummaryString);
        if (defectItemList == null || jsonObject == null || (defectItemList.isEmpty() && jsonObject.isEmpty())) {
            if (isCommentMode) {
                Messages.showWarningDialog(project, HmsConvertorBundle.message("no_comment_record"),
                    Constant.PLUGIN_NAME);
            } else {
                Messages.showWarningDialog(project, HmsConvertorBundle.message("no_record"), Constant.PLUGIN_NAME);
            }
            return false;
        }
        return true;
    }

    private static void showSuccessNotice(boolean isCommentMode, Project project) {
        if (isCommentMode) {
            BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("last_comment_conversion_success"),
                project, Constant.PLUGIN_NAME, true);
        } else {
            BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("last_conversion_success"), project,
                Constant.PLUGIN_NAME, true);
        }
    }
}
