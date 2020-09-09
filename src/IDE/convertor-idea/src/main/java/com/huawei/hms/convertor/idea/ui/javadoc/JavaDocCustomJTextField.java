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
import com.huawei.hms.convertor.idea.util.StringUtil;

import com.huawei.hms.convertor.idea.util.ToolWindowUtil;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Custom JTextField
 *
 * @since 2020-08-05
 */
public class JavaDocCustomJTextField extends JTextField {

    private static final long serialVersionUID = -8604470249321985522L;

    private static final String BUTTON_ICON_RESOURCE = "/icons/icon_close.png";

    private Project project;

    private JButton button;

    public JavaDocCustomJTextField(Project project) {
        this.project = project;
        textAddLafManagerListener();
        textAddDocumentListener();
        textAddFocusListener();
        button = new JavaDocCustomJButton();
    }

    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder(g);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initButton();
        add(button);
    }

    private void initButton() {
        ImageIcon icon = new ImageIcon(JavaDocCustomJTextField.class.getResource(BUTTON_ICON_RESOURCE));
        button.setIcon(icon);
        button.hide();
        ApiDetailToolWindow apiDetailToolWindow = ToolWindowUtil.getApiDetailToolWindow(project).get();
        buttonAddListener(apiDetailToolWindow);
        setLayout(new FlowLayout(FlowLayout.RIGHT, UIConstants.JavaDoc.LAYOUT_HORIZONTAL_GAP,
            UIConstants.JavaDoc.LAYOUT_VERTICAL_GAP));
    }

    private void buttonAddListener(ApiDetailToolWindow apiDetailToolWindow) {
        CardLayout cardLayout = apiDetailToolWindow.getCardLayout();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setText("");
                cardLayout.show(apiDetailToolWindow.getContentPanel(), UIConstants.JavaDoc.JAVADOC_TOOLWINDOW);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void textAddFocusListener() {
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (UIUtil.isUnderDarcula()) {
                    setForeground(UIConstants.JavaDoc.TEXTFIELD_FOCUS_Gained_COLOR);
                } else {
                    setForeground(Color.BLACK);
                }
                if (StringUtil.isEmpty(getText()) || UIConstants.JavaDoc.SEARCH_API.equals(getText())) {
                    setText("");
                    button.hide();
                } else {
                    button.show(true);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (StringUtil.isEmpty(getText()) || UIConstants.JavaDoc.SEARCH_API.equals(getText())) {
                    button.hide();
                    if (UIUtil.isUnderDarcula()) {
                        setForeground(UIConstants.JavaDoc.TEXTFIELD_FOCUS_LOST_DARCULA_COLOR);
                    } else {
                        setForeground(UIConstants.JavaDoc.TEXTFIELD_FOCUS_LOST_COLOR);
                    }
                    setText(UIConstants.JavaDoc.SEARCH_API);
                }
            }
        });
    }

    private void textAddDocumentListener() {
        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (e.getLength() == 0 || UIConstants.JavaDoc.SEARCH_API.equals(getText())) {
                    button.hide();
                } else {
                    button.show(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                button.hide();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void textAddLafManagerListener() {
        LafManager.getInstance().addLafManagerListener(new LafManagerListener() {
            @Override
            public void lookAndFeelChanged(LafManager source) {
                addNotify();
                setText("");
            }
        });
    }
}
