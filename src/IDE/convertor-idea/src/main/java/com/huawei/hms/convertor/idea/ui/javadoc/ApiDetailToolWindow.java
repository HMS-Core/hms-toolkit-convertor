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
import com.huawei.hms.convertor.idea.util.JavaDocUtil;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

import lombok.Getter;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * api details tool window
 *
 * @since 2020-08-19
 */
@Getter
public class ApiDetailToolWindow extends SimpleToolWindowPanel implements Disposable {
    private static final long serialVersionUID = -6201459078525570151L;

    private JPanel rootPanel;

    private JButton searchButton;

    private JTextField searchTextField;

    private JPanel contentPanel;

    private JPanel borderPanel;

    private JPanel centerPanel;

    private JScrollPane scrollPane;

    private JScrollPane rootScrollPane;

    private JavaDocToolWindow javaDocToolWindow;

    private JavaDocSearchToolWindow javaDocSearchToolWindow;

    private Project project;

    private CardLayout cardLayout;

    public ApiDetailToolWindow(Project project) {
        super(true, true);
        this.project = project;
        init();
    }

    @Override
    public void dispose() {
    }

    public void refreshData() {
        JavaDocUtil.setJpanelStyle(rootPanel);
    }

    private void init() {
        refreshData();
        searchTextField = new JavaDocCustomJTextField(project);
        searchTextField.setBorder(BorderFactory.createEmptyBorder());
        borderPanel.add(searchTextField, BorderLayout.CENTER);
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        javaDocToolWindow = new JavaDocToolWindow(project);
        contentPanel.add(javaDocToolWindow.getJavaDocPanel(), UIConstants.JavaDoc.JAVADOC_TOOLWINDOW);
        javaDocSearchToolWindow = new JavaDocSearchToolWindow(project);
        setSearchToolWindowStyle();
        contentPanel.add(javaDocSearchToolWindow.getRootPanel(), UIConstants.JavaDoc.JAVADOC_SEARCH_TOOLWINDOW);
        cardLayout.show(contentPanel, UIConstants.JavaDoc.JAVADOC_TOOLWINDOW);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        setContent(rootScrollPane);
        LafManager.getInstance().addLafManagerListener(new LafManagerListener() {
            @Override
            public void lookAndFeelChanged(LafManager source) {
                refreshData();
                setSearchToolWindowStyle();
            }
        });
    }

    private void setSearchToolWindowStyle() {
        javaDocSearchToolWindow.getRootPanel().setBackground(UIConstants.JavaDoc.JLIST_BACKGROUND_COLOR);
        javaDocSearchToolWindow.getListPanel().setBackground(UIConstants.JavaDoc.JLIST_BACKGROUND_COLOR);
        javaDocSearchToolWindow.getList().setBackground(UIConstants.JavaDoc.JLIST_BACKGROUND_COLOR);
    }
}
