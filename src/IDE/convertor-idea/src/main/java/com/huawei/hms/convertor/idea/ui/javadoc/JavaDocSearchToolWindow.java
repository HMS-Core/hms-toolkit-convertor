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

package com.huawei.hms.convertor.idea.ui.javadoc;

import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.ToolWindowUtil;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import lombok.Setter;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.CardLayout;
import java.awt.Color;

/**
 * JavaDoc Class Center
 *
 * @since 2020-08-17
 */
@Getter
@Setter
public class JavaDocSearchToolWindow extends SimpleToolWindowPanel implements Disposable {

    private static final long serialVersionUID = 4056498685518390695L;

    private JPanel rootPanel;

    private JList list;

    private JLabel contentLabel;

    private JScrollPane scrollPane;

    private JPanel listPanel;

    public JavaDocSearchToolWindow(Project project) {
        super(true, true);
    }

    @Override
    public void dispose() {

    }
}
