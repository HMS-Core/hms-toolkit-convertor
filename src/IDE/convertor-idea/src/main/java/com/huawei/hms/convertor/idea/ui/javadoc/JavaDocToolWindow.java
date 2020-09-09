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
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.idea.util.ToolWindowUtil;
import com.huawei.inquiry.docs.EntireDoc;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * java doc tool window
 *
 * @since 2020-07-02
 */
@Getter
@Setter
public class JavaDocToolWindow extends SimpleToolWindowPanel implements Disposable {

    private static final long serialVersionUID = 2316825164975898332L;

    private JPanel javaDocPanel;

    private JPanel searchPanel;

    private JPanel apiLabelPanel;

    private JPanel apiContentPanel;

    private JPanel learnMorePanelUp;

    private JPanel learnMorePanelMiddle;

    private JPanel learnMorePanelDown;

    private JPanel javaDocDisplayPanel;

    private JScrollPane javaDocScrollPanel;

    private JScrollPane docScrollPaneGuideUp;

    private JScrollPane docScrollPaneGuideMiddle;

    private JScrollPane docScrollPaneGuideDown;

    private JLabel labelUp;

    private JLabel labelMiddle;

    private JLabel labelDown;

    private JLabel apiLabel;

    private JTextPane api;

    private JavaDocTextPanel backToApi;

    private JLabel learnMoreUp;

    private JLabel learnMoreMiddle;

    private JLabel learnMoreDown;

    private JTextPane docContentUp;

    private JTextPane docContentMiddle;

    private JTextPane docContentDown;

    private Project project;

    public JavaDocToolWindow(Project project) {
        super(true, true);
        this.project = project;
        init();
    }

    public JPanel getRootPanel() {
        return javaDocPanel;
    }

    public void refreshData(JavaDocPanelInfos javaDocPanelInfos) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            asyncClearData();
            setOpaque();
            setSearchText();
            apiLabel.setText("API");
            api.setText(JavaDocUtil.setApiTextStyle(javaDocPanelInfos.getFullApi()));
            if (javaDocPanelInfos.getType() == null || javaDocPanelInfos.getEntireDoc() == null) {
                initApiDocMessage();
                setNotOpaque();
                return;
            }
            EntireDoc.STRATEGYTYPE type = javaDocPanelInfos.getEntireDoc().getStrategyType();
            JavaDocService javaDocService;
            switch (type) {
                case X:
                    javaDocService = javaDocPanelInfos.isPrivateType() ? new OthersJavaDoc(javaDocPanelInfos, this)
                        : new XmsJavaDoc(javaDocPanelInfos, this, project);
                    break;
                case H:
                    javaDocService = javaDocPanelInfos.isPrivateType() ? new OthersJavaDoc(javaDocPanelInfos, this)
                        : new HmsJavaDoc(javaDocPanelInfos, this, project);
                    break;
                case G:
                    javaDocService = javaDocPanelInfos.isPrivateType() ? new OthersJavaDoc(javaDocPanelInfos, this)
                        : new GmsJavaDoc(javaDocPanelInfos, this, project);
                    break;
                case OTHER:
                    javaDocService = new OthersJavaDoc(javaDocPanelInfos, this);
                    break;
                default:
                    return;
            }
            javaDocService.setJavaDocPanel();
        }, ModalityState.defaultModalityState());
    }

    public void updateStyle() {
        JavaDocUtil.setLabelStyle(apiLabel);
        JavaDocUtil.setApiContentStyle(api);
        JavaDocUtil.setLabelStyle(labelUp);
        JavaDocUtil.setLabelStyle(labelMiddle);
        JavaDocUtil.setLabelStyle(labelDown);
        JavaDocUtil.setContentStyle(docContentUp);
        JavaDocUtil.setContentStyle(docContentMiddle);
        JavaDocUtil.setContentStyle(docContentDown);
        JavaDocUtil.setContentScrollPaneStyle(docScrollPaneGuideUp);
        JavaDocUtil.setContentScrollPaneStyle(docScrollPaneGuideMiddle);
        JavaDocUtil.setContentScrollPaneStyle(docScrollPaneGuideDown);
        JavaDocUtil.setJavaDocScrollPaneStyle(javaDocScrollPanel);
        JavaDocUtil.setJpanelStyle(javaDocPanel);
    }

    @Override
    public void dispose() {
    }

    private void init() {
        docContentUp.setEditable(false);
        docContentMiddle.setEditable(false);
        docContentDown.setEditable(false);
        api.setEditable(false);
        api.setEditorKit(JavaDocUtil.getHTMLEditorKit());
        initDisplayMessage();
    }

    private void asyncClearData() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            initDisplayMessage();
            JavaDocUtil.customRemoveMouseListener(learnMoreUp);
            JavaDocUtil.customRemoveMouseListener(learnMoreMiddle);
            JavaDocUtil.customRemoveMouseListener(learnMoreDown);
        }, ModalityState.defaultModalityState());
        JavaDocUtil.removeMethodDocListener(docContentDown);
        JavaDocUtil.removeMethodDocListener(docContentUp);
        JavaDocUtil.removeMethodDocListener(docContentMiddle);
    }

    private void setSearchText() {
        ApiDetailToolWindow apiDetailToolWindow = ToolWindowUtil.getApiDetailToolWindow(project).get();
        JTextField searchTextField = apiDetailToolWindow.getSearchTextField();
        if (UIUtil.isUnderDarcula()) {
            searchTextField.setForeground(UIConstants.JavaDoc.TEXTFIELD_FOCUS_LOST_DARCULA_COLOR);
        } else {
            searchTextField.setForeground(UIConstants.JavaDoc.TEXTFIELD_FOCUS_LOST_COLOR);
        }
        if (StringUtil.isEmpty(searchTextField.getText())) {
            searchTextField.setText(UIConstants.JavaDoc.SEARCH_API);
            return;
        }
        if (UIUtil.isUnderDarcula()) {
            searchTextField.setForeground(UIConstants.JavaDoc.TEXTFIELD_FOCUS_Gained_COLOR);
        } else {
            searchTextField.setForeground(Color.BLACK);
        }
    }

    private void initDisplayMessage() {
        apiLabel.setText("");
        api.setText("");
        backToApi.setText("");
        backToApi.setContentType("text/html");
        backToApi.setTextApi(null,false);
        initApiDocMessage();
        setNotOpaque();
    }

    private void initApiDocMessage() {
        labelUp.setText("");
        labelMiddle.setText("");
        labelDown.setText("");
        docContentUp.setText("");
        docContentMiddle.setText("");
        docContentDown.setText("");
        docContentUp.setContentType("text/html");
        docContentMiddle.setContentType("text/html");
        docContentDown.setContentType("text/html");
        javaDocScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        javaDocScrollPanel.setBorder(BorderFactory.createEmptyBorder());
        docScrollPaneGuideUp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        docScrollPaneGuideMiddle.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        docScrollPaneGuideDown.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JavaDocUtil.setLabelLink(learnMoreUp, "", null);
        JavaDocUtil.setLabelLink(learnMoreMiddle, "", null);
        JavaDocUtil.setLabelLink(learnMoreDown, "", null);
        updateStyle();
    }

    private void setOpaque() {
        docScrollPaneGuideUp.setOpaque(true);
        docScrollPaneGuideUp.getViewport().setOpaque(true);
        docContentUp.setOpaque(true);
        docScrollPaneGuideMiddle.setOpaque(true);
        docScrollPaneGuideMiddle.getViewport().setOpaque(true);
        docContentMiddle.setOpaque(true);
        docScrollPaneGuideDown.setOpaque(true);
        docScrollPaneGuideDown.getViewport().setOpaque(true);
        docContentDown.setOpaque(true);
    }

    private void setNotOpaque() {
        api.setOpaque(false);
        javaDocPanel.setOpaque(false);
        javaDocScrollPanel.setOpaque(false);
        javaDocScrollPanel.getViewport().setOpaque(false);
        docScrollPaneGuideUp.setOpaque(false);
        docScrollPaneGuideUp.getViewport().setOpaque(false);
        docContentUp.setOpaque(false);
        docScrollPaneGuideMiddle.setOpaque(false);
        docScrollPaneGuideMiddle.getViewport().setOpaque(false);
        docContentMiddle.setOpaque(false);
        docScrollPaneGuideDown.setOpaque(false);
        docScrollPaneGuideDown.getViewport().setOpaque(false);
        docContentDown.setOpaque(false);
        learnMorePanelUp.setOpaque(false);
        learnMorePanelMiddle.setOpaque(false);
        learnMorePanelDown.setOpaque(false);
        javaDocDisplayPanel.setOpaque(false);
        apiLabelPanel.setOpaque(false);
        apiContentPanel.setOpaque(false);
    }
}
