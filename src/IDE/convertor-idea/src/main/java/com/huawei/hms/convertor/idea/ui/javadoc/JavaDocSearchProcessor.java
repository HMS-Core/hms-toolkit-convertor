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

import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.JavaDocActionEnum;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.PrivacyStatementChecker;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.JavaDocUtil;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.idea.util.ToolWindowUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.inquiry.InquiryEntrances;
import com.huawei.inquiry.docs.Docs;
import com.huawei.inquiry.docs.EntireDoc;
import com.huawei.inquiry.docs.ZClassDoc;
import com.huawei.inquiry.docs.ZFieldDoc;
import com.huawei.inquiry.docs.ZMethodDoc;

import com.intellij.openapi.project.Project;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * java doc search process
 *
 * @since 2020-08-05
 */
@Slf4j
public class JavaDocSearchProcessor {
    private Map<String, Docs> result = null;

    private JavaDocPanelInfos javaDocPanelInfos = null;

    private EntireDoc entireDoc = null;

    private Map<String, String> dependencyVersionMapForDoc = new HashMap<>();

    private Project project;

    private JTextField searchTextField;

    private JButton searchButton;

    private JPanel contentPanel;

    private JavaDocToolWindow javaDocToolWindow;

    private JavaDocSearchToolWindow javaDocSearchToolWindow;

    private CardLayout cardLayout;

    private JList listResult;

    private JLabel contentLabel;

    private JScrollPane scrollPane;

    private String text;

    private DefaultListModel model;

    public JavaDocSearchProcessor(Project project, ApiDetailToolWindow apiDetailToolWindow) {
        addHandleListener(project, apiDetailToolWindow);
    }

    private void addHandleListener(Project project, ApiDetailToolWindow apiDetailToolWindow) {
        this.project = project;
        cardLayout = apiDetailToolWindow.getCardLayout();
        javaDocToolWindow = apiDetailToolWindow.getJavaDocToolWindow();
        javaDocSearchToolWindow = apiDetailToolWindow.getJavaDocSearchToolWindow();
        searchTextField = apiDetailToolWindow.getSearchTextField();
        searchButton = apiDetailToolWindow.getSearchButton();
        contentPanel = apiDetailToolWindow.getContentPanel();
        dependencyVersionMapForDoc = JavaDocUtil.getDependencyVersionMap(project);
        contentLabel = javaDocSearchToolWindow.getContentLabel();
        listResult = javaDocSearchToolWindow.getList();
        listResult.setFixedCellHeight(UIConstants.JavaDoc.LIST_CELL_RENDERER_HEIGHT);
        listResult.setFont(new Font(UIConstants.JavaDoc.HUAWEI_SANS, Font.PLAIN, UIConstants.JavaDoc.LABEL_FONT_SIZE));
        scrollPane = javaDocSearchToolWindow.getScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        textFieldAddKeyListener();
        buttonAddMouseListener();
        listResultAddListener();
    }

    private void listResultAddListener() {
        listResult.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listResult.getSelectedValue() == null) {
                    return;
                }
                text = replaceColorString(listResult.getSelectedValue().toString());
                queryApiInfoAndShow(text);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                listResult.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                listResult.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void buttonAddMouseListener() {
        searchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.info("Click the search button to search for information.");
                String searchText = searchTextField.getText();
                searchListDataAndShow(searchText);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                searchButton.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void textFieldAddKeyListener() {
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String searchAPI = searchTextField.getText();
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    log.info("Press Enter to query information.");
                    searchListDataAndShow(searchAPI);
                }
            }
        });
    }

    private void searchListDataAndShow(String text) {
        log.info("Start to invoke the interface to search for API information.parameter={}", text);
        if (StringUtils.isEmpty(text)) {
            return;
        }
        if (PrivacyStatementChecker.isNotAgreed(project)) {
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.PRIVACY);
            return;
        }
        BIReportService.getInstance().traceJavaDocSelection(project.getBasePath(), JavaDocActionEnum.HMS_API_SEARCH);
        setLabelStyle();
        result = InquiryEntrances.getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR), dependencyVersionMapForDoc)
            .search(text);
        log.info("API information search is successful and complete.");
        listResult.removeAll();
        if (MapUtils.isEmpty(result)) {
            listResult.setListData(new String[] {UIConstants.JavaDoc.NO_SEARCH_RESULT});
        } else {
            model = new DefaultListModel();
            convertColorString(result, text).stream().forEach(apiString -> {
                model.addElement(apiString);
            });
            listResult.setModel(model);
        }
        ApiDetailToolWindow apiDetailToolWindow = ToolWindowUtil.getApiDetailToolWindow(project).get();
        JavaDocUtil.setJScrollPaneStyle(apiDetailToolWindow);
        scrollPane.doLayout();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setValue(horizontalScrollBar.getMinimum());
        listResult.setPreferredSize(
            new Dimension(UIConstants.JavaDoc.LIST_WIDTH, model.getSize() * UIConstants.JavaDoc.LIST_HEIGHT));
        cardLayout.show(contentPanel, UIConstants.JavaDoc.JAVADOC_SEARCH_TOOLWINDOW);
    }

    private void queryApiInfoAndShow(String apiInfo) {
        if (result.get(apiInfo) == null) {
            return;
        }
        log.info("Click to query the Java API document from the list result.parameter={}", apiInfo);
        setLabelStyle();
        Class<? extends Docs> aClass = result.get(apiInfo).getClass();
        if (aClass.equals(XMethodDoc.class) || aClass.equals(ZMethodDoc.class)) {
            getInfosWhenMethod(apiInfo);
        } else if (aClass.equals(XClassDoc.class) || aClass.equals(ZClassDoc.class)) {
            getInfosWhenClass(apiInfo);
        } else if (aClass.equals(XFieldDoc.class) || aClass.equals(ZFieldDoc.class)) {
            getInfosWhenField(apiInfo);
        } else {
            javaDocPanelInfos = new JavaDocPanelInfos(HmsConvertorBundle.message("unknow_api"), null, null, false);
        }
        log.info("API information query is complete.");
        try {
            JavaDocUtil.showJavaDocWindow(project, javaDocPanelInfos);
            cardLayout.show(contentPanel, UIConstants.JavaDoc.JAVADOC_TOOLWINDOW);
        } catch (Exception exception) {
            log.error("show JavaDocWindow API information error. message={}", exception.getMessage());
        }
    }

    private void getInfosWhenClass(String apiInfo) {
        entireDoc =
            InquiryEntrances.getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR), dependencyVersionMapForDoc)
                .getDocs(apiInfo, EntireDoc.SCOPETYPE.CLASS);
        javaDocPanelInfos = new JavaDocPanelInfos(apiInfo, entireDoc, EntireDoc.SCOPETYPE.CLASS, false);
    }

    private void getInfosWhenMethod(String apiInfo) {
        entireDoc =
            InquiryEntrances.getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR), dependencyVersionMapForDoc)
                .getDocs(apiInfo, EntireDoc.SCOPETYPE.METHOD);
        javaDocPanelInfos = new JavaDocPanelInfos(apiInfo, entireDoc, EntireDoc.SCOPETYPE.METHOD, false);
    }

    private void getInfosWhenField(String apiInfo) {
        entireDoc =
            InquiryEntrances.getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR), dependencyVersionMapForDoc)
                .getDocs(apiInfo, EntireDoc.SCOPETYPE.FIELD);
        javaDocPanelInfos = new JavaDocPanelInfos(apiInfo, entireDoc, EntireDoc.SCOPETYPE.FIELD, false);
    }

    /**
     * String add color
     *
     * @param map Search result
     * @param str Character string whose color needs to be changed.
     */
    private List<String> convertColorString(Map<String, Docs> map, String str) {
        List<String> convertorList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(UIConstants.Html.GREEN_SPAN_HEAD).append(str).append(UIConstants.Html.SPAN_END);
        removeNullKey(map);
        String replace = str.replace("(", "\\(").replace(")", "\\)");
        Pattern pattern = Pattern.compile(replace);
        map.keySet()
            .stream()
            .forEach(e -> convertorList.add(
                UIConstants.Html.HTML_HEAD + pattern.matcher(e).replaceAll(sb.toString()) + UIConstants.Html.HTML_END));
        return convertorList;
    }

    private String replaceColorString(String infoAPI) {
        if (StringUtil.isEmpty(infoAPI)) {
            return "";
        }
        return infoAPI.replaceAll(UIConstants.Html.HTML_HEAD, "")
            .replaceAll(UIConstants.Html.GREEN_SPAN_HEAD, "")
            .replaceAll(UIConstants.Html.SPAN_END, "")
            .replaceAll(UIConstants.Html.HTML_END, "");
    }

    private void removeNullKey(Map map) {
        Set set = map.keySet();
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            remove(obj, iterator);
        }
    }

    private void remove(Object obj, Iterator iterator) {
        if (obj instanceof String) {
            String str = (String) obj;
            if (StringUtils.isBlank(str)) {
                iterator.remove();
            }
        }
    }

    private void setLabelStyle() {
        contentLabel.setFont(new Font(UIConstants.JavaDoc.HUAWEI_SANS, Font.PLAIN, 18));
        contentLabel.setPreferredSize(new Dimension(UIConstants.JavaDoc.AUTO_SIZE, UIConstants.JavaDoc.LABEL_HEIGHT));
    }
}
