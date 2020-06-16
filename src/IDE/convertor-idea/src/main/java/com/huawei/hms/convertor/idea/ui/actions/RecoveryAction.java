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
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.PrivacyStatementChecker;
import com.huawei.hms.convertor.idea.ui.recovery.RecoveryDialog;
import com.huawei.hms.convertor.idea.util.ActionUtil;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

/**
 * Recovery action
 *
 * @since 2019-11-14
 */
public class RecoveryAction extends AnAction {
    private static final Logger LOG = LoggerFactory.getLogger(HmsConvertorAction.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        if (PrivacyStatementChecker.isNotAgreed()) {
            // bi report action: trace cancel operation.
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.PRIVACY);
            return;
        }

        // bi report action: menu click.
        BIReportService.getInstance().traceMenuSelection(project.getBasePath(), MenuEnum.RESTORE);

        /* Save all unsaved files */
        FileDocumentManager.getInstance().saveAllDocuments();

        ConfigCacheService configCacheService = ConfigCacheService.getInstance();
        if ((null == configCacheService) || StringUtil.isEmpty(configCacheService
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH, String.class, ""))) {
            LOG.warn("Recovery: there is no inspectPath!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("first_analysis_notice"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        final String backupPath = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.BACK_PATH, String.class, "");
        if (StringUtil.isEmpty(backupPath)) {
            LOG.warn("Recovery: there is no backupPath!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_backup"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        final File backupFolder = new File(backupPath);
        File[] files = backupFolder.listFiles();
        if (!backupFolder.exists() || files == null || (files.length == 0)) {
            LOG.warn("Recovery: there is no files in backupPath!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_backup"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        RecoveryDialog recoveryDialog = new RecoveryDialog(project);
        recoveryDialog.show();

    }

    @Override
    public void update(AnActionEvent e) {
        ActionUtil.updateAction(e, IconUtil.RECOVERY);
    }
}
