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

import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.result.conversion.SourceConvertorToolWindow;
import com.huawei.hms.convertor.idea.ui.result.summary.SummaryToolWindow;
import com.huawei.hms.convertor.idea.ui.result.xms.XmsDiffToolWindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Hms convertor tool window
 *
 * @since 2019/11/27
 */
@Slf4j
public class HmsConvertorToolWindow extends SimpleToolWindowPanel implements Disposable {
    private JPanel rootPanel = new JPanel();

    private JTabbedPane tabbedPane = new JTabbedPane();

    private Project project;

    private SummaryToolWindow summaryToolWindow;

    private SourceConvertorToolWindow sourceConvertorToolWindow;

    private XmsDiffToolWindow xmsDiffWindow;

    public HmsConvertorToolWindow(@NotNull Project project) {
        super(true, true);
        this.project = project;

        init();
    }

    @Override
    public void dispose() {
    }

    public void showTabbedPane(int toolWindowIndex) {
        tabbedPane.setSelectedIndex(toolWindowIndex);
    }

    public SummaryToolWindow getSummaryToolWindow() {
        return summaryToolWindow;
    }

    public SourceConvertorToolWindow getSourceConvertorToolWindow() {
        return sourceConvertorToolWindow;
    }

    public XmsDiffToolWindow getXmsDiffWindow() {
        return xmsDiffWindow;
    }

    public void setXmsDiffVisible(boolean visible) {
        if (visible) {
            if (tabbedPane.getTabCount() < UIConstants.ToolWindow.HmsConvertor.TABBED_PANE_COUNT_LIMIT) {
                tabbedPane.add(HmsConvertorBundle.message("versiondiff"), xmsDiffWindow.getRootPanel());
            }
        } else {
            if (tabbedPane.getTabCount() == UIConstants.ToolWindow.HmsConvertor.TABBED_PANE_COUNT_LIMIT) {
                tabbedPane.removeTabAt(UIConstants.ToolWindow.HmsConvertor.TABBED_PANE_MAX_INDEX);
            }
        }
    }

    private void init() {
        rootPanel.setLayout(new BorderLayout());

        summaryToolWindow = new SummaryToolWindow(project);
        tabbedPane.add(HmsConvertorBundle.message("summary"), summaryToolWindow.getRootPanel());

        sourceConvertorToolWindow = new SourceConvertorToolWindow(project);
        tabbedPane.add(HmsConvertorBundle.message("conversion"), sourceConvertorToolWindow.getRootPanel());

        xmsDiffWindow = new XmsDiffToolWindow(project, null);

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        setContent(rootPanel);
    }
}
