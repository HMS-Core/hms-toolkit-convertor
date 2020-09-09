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

import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.MenuEnum;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.analysis.HmsConvertorStartDialog;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.PrivacyStatementChecker;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.ActionUtil;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.idea.util.UserEnvUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.ProjectArchiveService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.intellij.application.options.editor.AutoImportOptionsConfigurable;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.CodeInsightWorkspaceSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import javax.swing.event.HyperlinkEvent;

/**
 * New conversion action
 *
 * @since 2019-06-10
 */
@Slf4j
public class HmsConvertorAction extends AnAction {
    /**
     * Save the date in the tool windows.
     *
     * @param project project instance
     */
    public static void saveConversionAndSummary(Project project) {
        ProjectArchiveService.saveAllToolWindowData(project.getBasePath());
        LocalFileSystem.getInstance().refresh(true);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Check privacy statement.
        if (PrivacyStatementChecker.isNotAgreed(project)) {
            // bi report action: trace cancel operation.
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.PRIVACY);
            return;
        }

        // bi report action: menu click.
        BIReportService.getInstance().traceMenuSelection(project.getBasePath(), MenuEnum.NEW_CONVERSION);

        // Enable when the project is initialized.
        if ((!project.isInitialized()) || (project.isDisposed()) || (!project.isOpen())) {
            return;
        }
        try {
            /* Save all unsaved files */
            FileDocumentManager.getInstance().saveAllDocuments();
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);

            // Get operating environment information.
            UserEnvUtil userEnv = UserEnvUtil.create();
            log.info(userEnv.toString());

            String projectBasePath = HmsConvertorUtil.getProjectBasePath(project);
            if (StringUtil.isEmpty(projectBasePath)) {
                throw new NoSuchFileException(HmsConvertorBundle.message("project_ptah_null"));
            }

            // Unsupported settings : isAddUnambiguousImports and isOptimizeImports
            if (!confirmSettings(project)) {
                return;
            }

            // Save before detecting whether to load the last scan result.
            saveConversionAndSummary(project);
            if (!checkWhetherOpenLastConversion(project)) {
                return;
            }

            HmsConvertorUtil.findXmsGeneratorJar();
            HmsConvertorUtil.findMapping4G2hJar();
            HmsConvertorStartDialog dialog = new HmsConvertorStartDialog(project, projectBasePath);
            dialog.show();
        } catch (NoSuchFileException ex) {
            log.warn("project, XMSEngine jar or G2H mapping jar not found, exception: {}.", ex.getMessage());
            BalloonNotifications.showErrorNotification(ex.getMessage(), project, Constant.PLUGIN_NAME, true);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        ActionUtil.updateAction(e, IconUtil.INSPECT);
        ActionUtil.updateAction(UIConstants.Action.CONVERTOR_ACTION_ID, IconUtil.CONVERTOR);
    }

    private boolean confirmSettings(Project project) {
        boolean isAddUnambiguousImports = CodeInsightSettings.getInstance().ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY;
        boolean isOptimizeImports = CodeInsightWorkspaceSettings.getInstance(project).optimizeImportsOnTheFly;
        log.info("isAddUnambiguousImports: {}, isOptimizeImports: {}", isAddUnambiguousImports, isOptimizeImports);
        if (isAddUnambiguousImports || isOptimizeImports) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("auto_import_tips"), project,
                new NotificationListener.Adapter() {
                    @Override
                    protected void hyperlinkActivated(@NotNull Notification notification,
                        @NotNull HyperlinkEvent hyperlinkEvent) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, AutoImportOptionsConfigurable.class);
                    }
                }, Constant.PLUGIN_NAME, true);

            return false;
        }

        return true;
    }

    private boolean checkWhetherOpenLastConversion(Project project) {
        ConfigCacheService configCacheService = ConfigCacheService.getInstance();
        String repoId = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.REPO_ID, String.class, "");
        if (configCacheService == null || StringUtil.isEmpty(repoId)) {
            return true;
        }
        String lastConversionFilePath = Paths
            .get(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH, repoId, ProjectConstants.Result.LAST_CONVERSION_JSON)
            .toString();
        if (FileUtil.isInvalidDirectoryPath(lastConversionFilePath)) {
            return true;
        }
        File savedFile = new File(lastConversionFilePath);

        // Prompt only when there is a cache file.
        if (savedFile.exists()) {
            int toContinue =
                Messages.showDialog(project, HmsConvertorBundle.message("load_last_msg"), Constant.PLUGIN_NAME,
                    new String[] {"New", "Continue", "Cancel"}, Messages.NO, Messages.getInformationIcon());
            if (toContinue == Messages.YES) { // New converison.
                return true;
            } else if (toContinue == Messages.NO) { // Open last converison.
                OpenLastAction.openLastConversion(project);
            } else { // Cancel
                log.info("Cancel");
                // bi report action: trace cancel operation.
                BIReportService.getInstance()
                    .traceCancelListener(project.getBasePath(), CancelableViewEnum.NEW_CONVERSION);
            }
            return false;
        }
        return true;
    }
}
