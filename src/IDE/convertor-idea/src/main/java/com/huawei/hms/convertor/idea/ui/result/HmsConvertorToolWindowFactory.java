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

package com.huawei.hms.convertor.idea.ui.result;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import org.jetbrains.annotations.NotNull;

/**
 * Hms convertor tool window factory
 *
 * @since 2019-06-10
 */
public class HmsConvertorToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, ToolWindow toolWindow) {
        if (toolWindow == null) {
            return;
        }
        final ContentManager contentManager = toolWindow.getContentManager();
        final HmsConvertorToolWindow hmsConvertorToolWindow = new HmsConvertorToolWindow(project);
        final Content content = contentManager.getFactory().createContent(hmsConvertorToolWindow, null, false);
        contentManager.addContent(content);

        Disposer.register(project, hmsConvertorToolWindow);
    }
}
