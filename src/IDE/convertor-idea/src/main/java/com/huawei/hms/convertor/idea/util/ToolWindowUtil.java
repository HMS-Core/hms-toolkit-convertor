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

import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.javadoc.ApiDetailToolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import javax.swing.JComponent;

/**
 * @since 2018-05-28
 */
public final class ToolWindowUtil {
    private ToolWindowUtil() {
    }

    @Nullable
    public static ToolWindow getToolWindow(@NotNull final Project project, @NotNull String toolWindowId) {
        return ToolWindowManager.getInstance(project).getToolWindow(toolWindowId);
    }

    public static void showWindow(@NotNull final ToolWindow toolWindow) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            if (!toolWindow.isActive() && toolWindow.isAvailable()) {
                toolWindow.show(null);
            }
        }, ModalityState.any());
    }

    public static Optional<ApiDetailToolWindow> getApiDetailToolWindow(@NotNull Project project) {
        final ToolWindow toolWindow = ToolWindowUtil.getToolWindow(project,
            UIConstants.JavaDocToolWindow.JAVADOC_TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return Optional.empty();
        }

        Content[] contents = toolWindow.getContentManager().getContents();
        if (contents.length == 0) {
            ContentManager contentManager = toolWindow.getContentManager();
            contentManager.addContent(
                toolWindow.getContentManager().getFactory().createContent(new ApiDetailToolWindow(project), null, false));
            contents = toolWindow.getContentManager().getContents();
        }

        final JComponent component = contents[0].getComponent();
        return (component instanceof ApiDetailToolWindow) ? Optional.of((ApiDetailToolWindow) component)
            : Optional.empty();
    }
}
