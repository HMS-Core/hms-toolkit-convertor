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

import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;

import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Show Log Action
 *
 * @version 1.0.0
 * @since 2020-05-26
 */

public class ShowLogAction extends AnAction implements DumbAware {

    public ShowLogAction() {
        super(HmsConvertorBundle.message("show_log"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        File logDir = new File(PluginConstant.PluginDataDir.PLUGIN_LOG_PATH);
        ShowFilePathAction.openDirectory(logDir);
    }
}
