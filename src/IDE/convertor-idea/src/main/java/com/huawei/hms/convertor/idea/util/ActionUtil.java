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

import com.huawei.hms.convertor.idea.ui.common.HmsConvertorState;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * Action utility class
 *
 * @since 2019-05-27
 */
public final class ActionUtil {
    private ActionUtil() {
    }

    /**
     * update action
     *
     * @param e the instance of AnActionEvent, action event
     * @param icon the icon of this action menu
     */
    public static void updateAction(@NotNull AnActionEvent e, @NotNull Icon icon) {
        if (e == null) {
            return;
        }
        final Presentation presentation = e.getPresentation();
        presentation.setIcon(icon);

        final Project project = e.getProject();
        if (project == null || !project.isInitialized() || project.isDisposed() || !project.isOpen()) {
            presentation.setEnabled(false);
            return;
        }

        final HmsConvertorState hmsConvertorState = HmsConvertorState.get(project);
        final boolean enable = hmsConvertorState.isIdle();
        presentation.setEnabled(enable);
    }

    /**
     * update action
     *
     * @param actionId the action ID
     * @param icon the icon of this action menu
     */
    public static void updateAction(@NotNull String actionId, @NotNull Icon icon) {
        final AnAction action = ActionManager.getInstance().getAction(actionId);
        if (null != action) {
            final Presentation presentation = action.getTemplatePresentation();
            presentation.setIcon(icon);
        }
    }

}
