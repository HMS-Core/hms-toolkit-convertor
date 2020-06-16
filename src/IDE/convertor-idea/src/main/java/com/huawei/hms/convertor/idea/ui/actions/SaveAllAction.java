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
import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.core.event.context.project.ProjectEvent;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.PrivacyStatementChecker;
import com.huawei.hms.convertor.idea.util.ActionUtil;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.EventService;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.TimeoutUtil;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;

/**
 * Save all action
 *
 * @since 2020-02-03
 */
@Slf4j
public class SaveAllAction extends AnAction {
    private static final int ASYC_SAVE_TASK_SLEEPTIME = 20;

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
        BIReportService.getInstance().traceMenuSelection(project.getBasePath(), MenuEnum.SAVE);

        ConfigCacheService configCacheService = ConfigCacheService.getInstance();
        if (StringUtil.isEmpty(configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.INSPECT_PATH, String.class, ""))) {
            log.warn("Save all: there is no inspectPath!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("first_analysis_notice"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        final String backupPath =
            configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.BACK_PATH, String.class, "");
        if (StringUtil.isEmpty(backupPath)) {
            log.warn("Save all: there is no backupPath!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_backup"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        // Start an asynchronous save task.
        final Task task = new Task.Backgroundable(project, Constant.PLUGIN_NAME, true, PerformInBackgroundOption.DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                asycSaveTask(project, indicator);
            }
        };
        task.queue();
    }

    private void asycSaveTask(Project project, ProgressIndicator indicator) {
        if (indicator == null) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_tool_window"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        indicator.setIndeterminate(true);
        TimeoutUtil.sleep(ASYC_SAVE_TASK_SLEEPTIME);
        indicator.setText(HmsConvertorBundle.message("indicater_saveall_notice"));

        Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
            Result result = EventService.getInstance()
                .submitProjectEvent(
                    ProjectEvent.<String, Result> of(project.getBasePath(), EventType.SAVE_ALL_EVENT, "", (message) -> {
                        semaphore.release();

                        // Show the result of task.
                        if (message.isOk()) {
                            String folderName = "";
                            if (message.getData() instanceof String) {
                                folderName = (String) message.getData();
                            }
                            final String successMessage =
                                "Project saved successfully: " + folderName + Constant.EXTENSION_ZIP;
                            BalloonNotifications.showSuccessNotification(successMessage, project, Constant.PLUGIN_NAME,
                                true);
                        } else {
                            log.error("Save all failed: {}", message.getMessage());
                            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("save_all_error"),
                                project, Constant.PLUGIN_NAME, true);
                        }
                    }));
            if (!result.isOk()) {
                BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("save_all_error"), project,
                    Constant.PLUGIN_NAME, true);
            }

            // Wait till the task ends.
            semaphore.acquire();
        } catch (InterruptedException e) {
            if (semaphore.availablePermits() == 0) {
                semaphore.release();
            }
            log.error(e.getMessage(), e);
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("save_all_error"), project,
                Constant.PLUGIN_NAME, true);
        }finally {
            semaphore.release();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        ActionUtil.updateAction(e, IconUtil.SAVEALL);
    }
}
