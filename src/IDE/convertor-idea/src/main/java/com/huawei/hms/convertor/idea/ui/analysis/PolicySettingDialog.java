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

package com.huawei.hms.convertor.idea.ui.analysis;

import static com.huawei.hms.convertor.idea.ui.analysis.HmsConvertorXmsGenerater.inferenceGAndHOrMultiApk;

import com.huawei.generator.g2x.po.summary.Diff;
import com.huawei.hms.convertor.core.bi.bean.FunctionSelectionBean;
import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.ConversionHelpEnum;
import com.huawei.hms.convertor.core.bi.enumration.ConversionStrategyEnum;
import com.huawei.hms.convertor.core.bi.enumration.OperationViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.ProjectTypeEnum;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.diff.XmsDiff;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;
import com.huawei.hms.convertor.core.result.summary.SummaryConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.HmsConvertorState;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.ClientUtil;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.idea.util.TimeUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.ConversionCacheService;
import com.huawei.hms.convertor.openapi.SummaryCacheService;
import com.huawei.hms.convertor.openapi.XmsDiffCacheService;
import com.huawei.hms.convertor.openapi.XmsGenerateService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.util.KitUtil;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.UIUtil;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Policy setting dialog
 *
 * @since 2020-04-29
 */
public class PolicySettingDialog extends DialogWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(PolicySettingDialog.class);

    private static final Pattern XMS_PATH_PATTERN = Pattern.compile("(.)*\\/src\\/main\\/java$");

    private static final Pattern G_PATH_PATTERN = Pattern.compile("(.)*\\/src$");

    private static final String SPACE_FOR_TABLE = " ";

    private static final int MOUSE_SINGLE_CLICK = 1;

    private static final int FIRST_COLUMN_COUNT = 3;

    private static final int FIRST_COLUMN_MARGIN = 30;

    private static final int SECOND_COLUMN_MARGIN = 15;

    private static final int TABLE_PREFER_HEIGHT = 350;

    private static final int TABLE_MIN_HEIGHT = 200;

    private static final int TABLE_WIDTH_MARGIN = 80;

    private static final int TABLE_ITEM_HEIGHT = -1;

    private static final int TABLE_POLICY_FONTSIZE_GAP = 2;

    private static final int POLICY_NOTICE_MARGIN = 10;

    private static final int POLICY_NOTICE_FS_MARGIN = 5;

    private static final int PANEL_PREFER_WIDTH_FS_12 = 840;

    private static final int PANEL_MIN_WIDTH_FS_12 = 840;

    private static final int PANEL_PREFER_HEIGHT_FS_12 = 610;

    private static final int PANEL_MIN_HEIGHT_FS_12 = 600;

    private static final int PANEL_PREFER_WIDTH_FS_14 = 860;

    private static final int PANEL_MIN_WIDTH_FS_14 = 860;

    private static final int PANEL_PREFER_HEIGHT_FS_14 = 670;

    private static final int PANEL_MIN_HEIGHT_FS_14 = 660;

    private static final int PANEL_PREFER_WIDTH_FS_16 = 960;

    private static final int PANEL_MIN_WIDTH_FS_16 = 960;

    private static final int PANEL_PREFER_HEIGHT_FS_16 = 710;

    private static final int PANEL_MIN_HEIGHT_FS_16 = 702;

    private static final int PANEL_PREFER_WIDTH_FS_18 = 1032;

    private static final int PANEL_MIN_WIDTH_FS_18 = 1032;

    private static final int PANEL_PREFER_HEIGHT_FS_18 = 760;

    private static final int PANEL_MIN_HEIGHT_FS_18 = 752;

    private static final int PANEL_PREFER_WIDTH_FS_20 = 1140;

    private static final int PANEL_MIN_WIDTH_FS_20 = 1140;

    private static final int PANEL_PREFER_HEIGHT_FS_20 = 810;

    private static final int PANEL_MIN_HEIGHT_FS_20 = 804;

    private String type;

    private JPanel contentPane;

    private JRadioButton toHmsRadioButton;

    private JRadioButton andHmsHFRadioButton;

    private JRadioButton andHmsGFRadioButton;

    private JLabel analysisLabel;

    private JLabel detailsLabel;

    private JLabel policySelectLabel;

    private JLabel xmsPathLabel;

    private JLabel row1Label;

    private JLabel row21Label;

    private JLabel row31Label;

    private JLabel row32Label;

    private JLabel row41Label;

    private JLabel row42Label;

    private JLabel addHmsPromptLabel;

    private JLabel toHmsPromptLabel;

    private JPanel tableManagerPanel;

    private JPanel row1Panel;

    private JPanel row1_1Panel;

    private JPanel row1_2Panel;

    private JPanel row1_3Panel;

    private JPanel row3Panel;

    private JPanel row3_1Panel;

    private JPanel row3_2Panel;

    private JPanel row3_3Panel;

    private JPanel row4Panel;

    private JPanel row4_1Panel;

    private JPanel row4_2Panel;

    private JPanel row4_3Panel;

    private JPanel row5Panel;

    private JPanel row5_1Panel;

    private JPanel row5_2Panel;

    private JPanel row5_3Panel;

    private JLabel addHmsTableLabel;

    private JLabel toHmsTableLabel;

    private JPanel row1_2_1Panel;

    private JPanel row1_2_2Panel;

    private JPanel row1_2_3Panel;

    private JLabel row22Label;

    private JLabel lineLabel;

    private JLabel step1Label;

    private JLabel step2Label;

    private JLabel tableRow1Label;

    private JLabel tableRow2Label;

    private JLabel tableRow3Label;

    private JLabel tableRow4Label;

    private JLabel APILabel;

    private JLabel methodLabel;

    private JLabel pathHelpLabel;

    private JCheckBox checkBoxG;

    private JLabel noticeLabel;

    private JLabel firstNoticeLabel;

    private JLabel secondNoticeLabel;

    private JLabel thirdNoticeLabel;

    private JLabel fourthNoticeLabel;

    private JPanel fourthNoticePanel;

    private JLabel notice1Label;

    private JLabel notice2Label;

    private JLabel notice3Label;

    private JLabel notice4Label;

    private JLabel fifthNoticeLabel;

    private JTextField xmsPathTextField;

    private JPanel row2_1Panel;

    private JPanel xmsTittlePanel;

    private Project project;

    private Action previousAction;

    private ConfigCacheService configCacheService;

    private Map<String, String> showData;

    private HmsConvertorXmsGenerater hmsConvertorXmsGenerater;

    private String inspectFolder;

    private String allianceDomain;

    private boolean commentEnable;

    public PolicySettingDialog(@NotNull Project project) {
        super(project);
        this.project = project;
        showData = SummaryCacheService.getInstance().getShowData(project.getBasePath());
        configCacheService = ConfigCacheService.getInstance();
        previousAction = new PreviousAction(this);
        previousAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
        String inspectPath = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.INSPECT_PATH, String.class, "");
        allianceDomain = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.ALLIANCE_DOMAIN, String.class, "");
        inspectFolder = inspectPath.substring(inspectPath.lastIndexOf(Constant.SEPARATOR) + 1);
        commentEnable = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.COMMENT,
            Boolean.class, false);

        init();
        addListener();

        // Adjust the display size.
        adjustPanelSize();
        adjsutTableSize();
    }

    public static Set<String> getGaddHKitWhiteList() {
        Set<String> supportKitInfo = XmsGenerateService.supportKitInfo();
        return supportKitInfo;
    }

    @Override
    public void init() {
        super.init();
        setTitle(Constant.PLUGIN_NAME);
        getWindow().setIconImage(ImageLoader.loadFromResource("/icons/convertor.png"));

        noticeLabel.setText(HmsConvertorBundle.message("notice_title"));
        firstNoticeLabel.setText(HmsConvertorBundle.message("notice_first"));
        secondNoticeLabel.setText(HmsConvertorBundle.message("notice_second"));
        thirdNoticeLabel.setText(HmsConvertorBundle.message("notice_third"));
        fourthNoticeLabel.setText("");
        fifthNoticeLabel.setText("");
        firstNoticeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        secondNoticeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        thirdNoticeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fourthNoticeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        detailsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        analysisLabel.setText(HmsConvertorBundle.message("view_analysis_result"));
        detailsLabel.setText(HmsConvertorBundle.message("details"));
        policySelectLabel.setText(HmsConvertorBundle.message("select_policy"));
        xmsPathLabel.setText(HmsConvertorBundle.message("configure_xms_path"));
        toHmsRadioButton.setText(HmsConvertorBundle.message("to_hms_t"));
        type = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_TYPE, String.class,
            "");
        if (type.equals(ProjectConstants.Type.APP)) {
            andHmsHFRadioButton.setText(HmsConvertorBundle.message("add_hms_h"));
            andHmsGFRadioButton.setText(HmsConvertorBundle.message("add_hms_g"));
        } else {
            andHmsHFRadioButton.setText(HmsConvertorBundle.message("add_hms"));
            andHmsGFRadioButton.setVisible(false);
            xmsPathTextField.setVisible(false);
            checkBoxG.setVisible(false);
            xmsTittlePanel.setVisible(false);
        }

        addHmsTableLabel.setText(HmsConvertorBundle.message("add_hms"));
        toHmsTableLabel.setText(HmsConvertorBundle.message("to_hms"));
        tableRow1Label.setText(SPACE_FOR_TABLE + HmsConvertorBundle.message("table_row_1_desc"));
        tableRow2Label.setText(SPACE_FOR_TABLE + HmsConvertorBundle.message("table_row_2_desc"));
        tableRow3Label.setText(SPACE_FOR_TABLE + HmsConvertorBundle.message("table_row_3_desc"));
        tableRow4Label.setText(SPACE_FOR_TABLE + HmsConvertorBundle.message("table_row_4_desc"));
        step1Label.setIcon(IconUtil.STEP_1_FINISH);
        step2Label.setIcon(IconUtil.RUNNING);
        checkBoxG.setText(HmsConvertorBundle.message("only_g"));
        ButtonGroup policyButtonGroup = new ButtonGroup();
        policyButtonGroup.add(toHmsRadioButton);
        policyButtonGroup.add(andHmsHFRadioButton);
        policyButtonGroup.add(andHmsGFRadioButton);

        andHmsHFRadioButton.setMnemonic(KeyEvent.VK_H);
        andHmsGFRadioButton.setMnemonic(KeyEvent.VK_G);
        toHmsRadioButton.setMnemonic(KeyEvent.VK_T);

        setTableParams();

        // Selected by default.
        andHmsHFRadioButton.setSelected(true);

        initShowData();
    }

    private void adjsutTableSize() {
        tableManagerPanel.setPreferredSize(
            new Dimension(contentPane.getPreferredSize().width - TABLE_WIDTH_MARGIN, TABLE_PREFER_HEIGHT));
        tableManagerPanel
            .setMinimumSize(new Dimension(contentPane.getPreferredSize().width - TABLE_WIDTH_MARGIN, TABLE_MIN_HEIGHT));
        int column1Width = contentPane.getPreferredSize().width / FIRST_COLUMN_COUNT - FIRST_COLUMN_MARGIN;
        row1_1Panel.setPreferredSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row1_1Panel.setMinimumSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row2_1Panel.setPreferredSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row2_1Panel.setMinimumSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row3_1Panel.setPreferredSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row3_1Panel.setMinimumSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row4_1Panel.setPreferredSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row4_1Panel.setMinimumSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row5_1Panel.setPreferredSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        row5_1Panel.setMinimumSize(new Dimension(column1Width, TABLE_ITEM_HEIGHT));
        int column2Width = contentPane.getPreferredSize().width / FIRST_COLUMN_COUNT - SECOND_COLUMN_MARGIN;
        row1_2Panel.setPreferredSize(new Dimension(column2Width, TABLE_ITEM_HEIGHT));
        row1_2Panel.setMinimumSize(new Dimension(column2Width, TABLE_ITEM_HEIGHT));
        row3_2Panel.setPreferredSize(new Dimension(column2Width, TABLE_ITEM_HEIGHT));
        row3_2Panel.setMinimumSize(new Dimension(column2Width, TABLE_ITEM_HEIGHT));
        row4_2Panel.setPreferredSize(new Dimension(column2Width, TABLE_ITEM_HEIGHT));
        row4_2Panel.setMinimumSize(new Dimension(column2Width, TABLE_ITEM_HEIGHT));
        addHmsTableLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD,
            UISettings.getInstance().getFontSize() + TABLE_POLICY_FONTSIZE_GAP));
        toHmsTableLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD,
            UISettings.getInstance().getFontSize() + TABLE_POLICY_FONTSIZE_GAP));
    }

    private void adjustPanelSize() {
        int fontSize = UISettings.getInstance().getFontSize();
        int noticeHeight = 0;
        if (fourthNoticePanel.isVisible()) {
            int line = Integer.parseInt(showData.get(SummaryConstants.NOT_SUPPORT_VERSION_LINE));
            noticeHeight = POLICY_NOTICE_MARGIN + fontSize + (POLICY_NOTICE_FS_MARGIN + fontSize) * line;
        }
        LOG.info("fontSize = {} noticeHeight = {}", fontSize, noticeHeight);
        if (fontSize < 14) {
            contentPane
                .setPreferredSize(new Dimension(PANEL_PREFER_WIDTH_FS_12, PANEL_PREFER_HEIGHT_FS_12 + noticeHeight));
            contentPane.setMinimumSize(new Dimension(PANEL_MIN_WIDTH_FS_12, PANEL_MIN_HEIGHT_FS_12 + noticeHeight));
        } else if (fontSize < 16) {
            contentPane
                .setPreferredSize(new Dimension(PANEL_PREFER_WIDTH_FS_14, PANEL_PREFER_HEIGHT_FS_14 + noticeHeight));
            contentPane.setMinimumSize(new Dimension(PANEL_MIN_WIDTH_FS_14, PANEL_MIN_HEIGHT_FS_14 + noticeHeight));
        } else if (fontSize < 18) {
            contentPane
                .setPreferredSize(new Dimension(PANEL_PREFER_WIDTH_FS_16, PANEL_PREFER_HEIGHT_FS_16 + noticeHeight));
            contentPane.setMinimumSize(new Dimension(PANEL_MIN_WIDTH_FS_16, PANEL_MIN_HEIGHT_FS_16 + noticeHeight));
        } else if (fontSize < 20) {
            contentPane
                .setPreferredSize(new Dimension(PANEL_PREFER_WIDTH_FS_18, PANEL_PREFER_HEIGHT_FS_18 + noticeHeight));
            contentPane.setMinimumSize(new Dimension(PANEL_MIN_WIDTH_FS_18, PANEL_MIN_HEIGHT_FS_18 + noticeHeight));
        } else {
            contentPane
                .setPreferredSize(new Dimension(PANEL_PREFER_WIDTH_FS_20, PANEL_PREFER_HEIGHT_FS_20 + noticeHeight));
            contentPane.setMinimumSize(new Dimension(PANEL_MIN_WIDTH_FS_20, PANEL_MIN_HEIGHT_FS_20 + noticeHeight));
        }
    }

    private void setXmsPath() {
        String folder = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        if (checkBoxG.isSelected()) {
            setXmsPath(folder + "/xmsadapter/src");
        } else {
            setXmsPath(folder + "/xmsadapter/src/main/java");
        }
    }

    private void setTableParams() {
        addHmsPromptLabel.setIcon(IconUtil.PROMPT_QUESTION);
        toHmsPromptLabel.setIcon(IconUtil.PROMPT_QUESTION);
        pathHelpLabel.setIcon(IconUtil.WARN);
        APILabel.setIcon(IconUtil.WARN);
        methodLabel.setIcon(IconUtil.WARN);
        Color borderColor;
        Color titleColor;
        Color row1Color;
        Color otherRowColor;
        if (UIUtil.isUnderDarcula()) {
            lineLabel.setIcon(IconUtil.GUIDE_LINE);
            notice1Label.setIcon(IconUtil.POINT_ITEM_LIGHT);
            notice2Label.setIcon(IconUtil.POINT_ITEM_LIGHT);
            notice3Label.setIcon(IconUtil.POINT_ITEM_LIGHT);
            notice4Label.setIcon(IconUtil.POINT_ITEM_LIGHT);
            borderColor = new Color(122, 138, 153);
            titleColor = new Color(183, 185, 188);
            row1Color = new Color(71, 76, 83);
            otherRowColor = new Color(71, 76, 83);
        } else {
            lineLabel.setIcon(IconUtil.GUIDE_LINE_GRAY);
            notice1Label.setIcon(IconUtil.POINT_ITEM_DARK);
            notice2Label.setIcon(IconUtil.POINT_ITEM_DARK);
            notice3Label.setIcon(IconUtil.POINT_ITEM_DARK);
            notice4Label.setIcon(IconUtil.POINT_ITEM_DARK);
            borderColor = new Color(122, 138, 153);
            titleColor = new Color(49, 51, 53);
            row1Color = new Color(212, 212, 212);
            otherRowColor = new Color(221, 221, 221);
        }
        tableManagerPanel.setBorder(BorderFactory.createLineBorder(borderColor));
        detailsLabel.setForeground(JBColor.BLUE);
        firstNoticeLabel.setForeground(JBColor.BLUE);
        secondNoticeLabel.setForeground(JBColor.BLUE);
        thirdNoticeLabel.setForeground(JBColor.BLUE);
        fourthNoticeLabel.setForeground(JBColor.BLUE);
        fifthNoticeLabel.setForeground(JBColor.BLUE);
        addHmsTableLabel.setForeground(titleColor);
        toHmsTableLabel.setForeground(titleColor);
        row1Panel.setBackground(row1Color);
        row1_1Panel.setBackground(row1Color);
        row1_2Panel.setBackground(row1Color);
        row1_2_1Panel.setBackground(row1Color);
        row1_2_2Panel.setBackground(row1Color);
        row1_2_3Panel.setBackground(row1Color);
        row1_3Panel.setBackground(row1Color);
        row3Panel.setBackground(otherRowColor);
        row3_1Panel.setBackground(otherRowColor);
        row3_2Panel.setBackground(otherRowColor);
        row3_3Panel.setBackground(otherRowColor);
        Color backgroundColor = contentPane.getBackground();
        row4Panel.setBackground(backgroundColor);
        row4_1Panel.setBackground(backgroundColor);
        row4_2Panel.setBackground(backgroundColor);
        row4_3Panel.setBackground(backgroundColor);
        row5Panel.setBackground(otherRowColor);
        row5_1Panel.setBackground(otherRowColor);
        row5_2Panel.setBackground(otherRowColor);
        row5_3Panel.setBackground(otherRowColor);
    }

    private void initShowData() {
        List<String> excludePaths = new ArrayList<>(configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.EXCLUDE_PATH, List.class, new ArrayList()));
        String inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        List<String> xmsPaths = FileUtil.findPathsByMask(XMS_PATH_PATTERN, inspectPath, excludePaths);
        setXmsPath();
        setVisible(xmsPaths);
        RoutePolicy routePolicy = RoutePolicy.G_AND_H;
        LOG.info("init: routePolicy = {}, routePriority = HMS First, xmsPaths = {}", routePolicy,
            StringUtil.join(xmsPaths.toArray(new String[0]), ","));

        initTableData();
    }

    private void initTableData() {
        row1Label.setText(showData.get(SummaryConstants.GMS_DEPENDENCY));
        row21Label.setText("<html>Auto: " + showData.get(SummaryConstants.SUPPORT_AUTO_COUNT_ADDHMS) + "<br>Manual: "
            + showData.get(SummaryConstants.SUPPORT_MANUAL_COUNT_ADDHMS) + "<br>Auto Rate: "
            + showData.get(SummaryConstants.SUPPORT_RATE_ADDHMS) + "</html>");
        row22Label.setText("<html>Auto: " + showData.get(SummaryConstants.SUPPORT_AUTO_COUNT_TOHMS) + "<br>Manual: "
            + showData.get(SummaryConstants.SUPPORT_MANUAL_COUNT_TOHMS) + "<br>Auto Rate: "
            + showData.get(SummaryConstants.SUPPORT_RATE_TOHMS) + "</html>");
        row31Label.setText(showData.get(SummaryConstants.NOT_SUPPORT_API_STR_ADDHMS));
        row32Label.setText(showData.get(SummaryConstants.NOT_SUPPORT_API_STR_TOHMS));
        row41Label.setText(showData.get(SummaryConstants.NOT_SUPPORT_METHOD_COUNT_TOHMS));
        row42Label.setText("");
        setUpdateGMSNotice();

        if (StringUtils.equals(showData.get(SummaryConstants.NOT_SUPPORT_VERSION_COUNT), "0")) {
            fourthNoticePanel.setVisible(false);
        }
        if (StringUtils.equals(showData.get(SummaryConstants.NOT_SUPPORT_API_STR_ADDHMS), Constant.NA)
            && showData.get(SummaryConstants.NOT_SUPPORT_API_STR_TOHMS).equals(Constant.NA)) {
            APILabel.setVisible(false);
        }
        if (StringUtils.equals(showData.get(SummaryConstants.NOT_SUPPORT_METHOD_COUNT_TOHMS), "0")) {
            methodLabel.setVisible(false);
        }
    }

    private void setUpdateGMSNotice() {
        if (!fourthNoticePanel.isVisible()) {
            return;
        }
        fourthNoticeLabel.setText(showData.get(SummaryConstants.NOT_SUPPORT_VERSION_COUNT) + " "
            + HmsConvertorBundle.message("notice_fourth"));
        fifthNoticeLabel.setText(showData.get(SummaryConstants.NOT_SUPPORT_VERSION_SHOW));
        createPopupMenu();
    }

    private void createPopupMenu() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.addSeparator();
        actionGroup.add(new CopyAction());
        actionGroup.addSeparator();

        PopupHandler.installPopupHandler(fifthNoticeLabel, actionGroup, ActionPlaces.UNKNOWN,
            ActionManager.getInstance());
    }

    private void addListener() {
        addHmsPromptLabel.addMouseListener(new Browse(allianceDomain, HelpLinkType.ADD_HMS));
        toHmsPromptLabel.addMouseListener(new Browse(allianceDomain, HelpLinkType.TO_HMS));
        pathHelpLabel.addMouseListener(new Browse(allianceDomain, HelpLinkType.XMS_PATH));

        checkBoxG.addActionListener(event -> {
            showInfo();
            setXmsPathEnabled4G();
        });

        APILabel.addMouseListener(new Browse(allianceDomain, HelpLinkType.NOT_SUPPORT_API));
        methodLabel.addMouseListener(new Browse(allianceDomain, HelpLinkType.NOT_SUPPORT_METHOD));

        detailsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == MOUSE_SINGLE_CLICK)) {
                    SummaryDialog summaryDialog = new SummaryDialog(project);
                    summaryDialog.refreshData(SummaryCacheService.getInstance().getKit2Methods(project.getBasePath()),
                        SummaryCacheService.getInstance().getAllKits(project.getBasePath()));
                    summaryDialog.show();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                detailsLabel.setText("<html><u>" + HmsConvertorBundle.message("details") + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                detailsLabel.setText(HmsConvertorBundle.message("details"));
            }
        });

        toHmsRadioButton.addItemListener(event -> {
            setXmsPathEnabled();
        });

        andHmsHFRadioButton.addItemListener(event -> {
            if (type.equals(ProjectConstants.Type.SDK)) {
                setXmsPathVisible();
            } else {
                setXmsPathEnabled4G();
            }
        });

        andHmsGFRadioButton.addItemListener(event -> {
            setXmsPathEnabled4G();
        });

        addUrl(firstNoticeLabel, "notice_1_url", HmsConvertorBundle.message("notice_first"));
        addUrl(secondNoticeLabel, "notice_2_url", HmsConvertorBundle.message("notice_second"));
        addUrl(thirdNoticeLabel, "notice_3_url", HmsConvertorBundle.message("notice_third"));
        addUrl(fourthNoticeLabel, "notice_4_url", showData.get(SummaryConstants.NOT_SUPPORT_VERSION_COUNT) + " "
            + HmsConvertorBundle.message("notice_fourth"));

        fifthNoticeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (fifthNoticeLabel.getText().contains("...more") && SwingUtilities.isLeftMouseButton(e)
                    && (e.getClickCount() == MOUSE_SINGLE_CLICK)) {
                    Messages.showInfoMessage(project, showData.get(SummaryConstants.NOT_SUPPORT_VERSION_DIALOG_SHOW),
                        "All GMS dependencies and compatible version");
                }
                super.mouseClicked(e);
            }
        });
    }

    private void setXmsPathVisible() {
        andHmsGFRadioButton.setVisible(false);
        xmsPathLabel.setVisible(false);
        xmsPathTextField.setVisible(false);
        checkBoxG.setVisible(false);
    }

    private void addUrl(JLabel label, String url, String string) {
        label.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                // bi report action: click help link.
                BIReportService.getInstance().traceHelpClick(project.getBasePath(), convertBIString(url));
                BrowserUtil.browse(allianceDomain + HmsConvertorBundle.message(url));
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                label.setText("<html><u>" + string + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                label.setText(string);
            }
        });
    }

    private ConversionHelpEnum convertBIString(String notice) {
        switch (notice) {
            case "notice_1_url":
                return ConversionHelpEnum.PRE_ANALYZE_JDK;
            case "notice_2_url":
                return ConversionHelpEnum.PRE_ANALYZE_ANDROID_X;
            case "notice_3_url":
                return ConversionHelpEnum.PRE_ANALYZE_SDK_VERSION;
            case "notice_4_url":
                return ConversionHelpEnum.PRE_ANALYZE_NOT_SUPPORT_DEPENDENCY;
            default:
                return null;
        }
    }

    private void setVisible(List<String> xmsPaths) {
        if (xmsPaths.size() > 1) {
            pathHelpLabel.setVisible(true);
        } else {
            pathHelpLabel.setVisible(false);
        }
    }

    private void showInfo() {
        if (checkBoxG.isSelected()) {
            Messages.showDialog(project,
                HmsConvertorBundle.message("OnlyG_constraint") + " <a href=\"" + allianceDomain
                    + HmsConvertorBundle.message("OnlyG_constraint2"),
                UIConstants.Dialog.TITLE_WARNING, new String[] {"OK"}, Messages.OK, Messages.getInformationIcon());
        }
    }

    private void setXmsPathEnabled() {
        final boolean isToHms = toHmsRadioButton.isSelected();

        xmsPathLabel.setEnabled(!isToHms);
        xmsPathTextField.setEnabled(!isToHms);
        checkBoxG.setEnabled(!isToHms);
    }

    private void setXmsPathEnabled4G() {
        final boolean isOnlyG = checkBoxG.isSelected();
        String inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        List<String> excludePaths = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.EXCLUDE_PATH, List.class, new ArrayList());
        setXmsPath();
        if (!getXmsPath().isEmpty()) {
            return;
        }
        if (isOnlyG) {
            if (getXmsPath().isEmpty()) {
                List<String> xms4GPaths = FileUtil.findPathsByMask(G_PATH_PATTERN, inspectPath, excludePaths);
                if (xms4GPaths.size() == 1) {
                    setXmsPath(xms4GPaths.get(0));
                } else {
                    setXmsPath("");
                }
            }
        } else {
            List<String> xmsPaths = FileUtil.findPathsByMask(XMS_PATH_PATTERN, inspectPath, excludePaths);
            if (xmsPaths.size() == 1) {
                setXmsPath(xmsPaths.get(0));
            } else {
                setXmsPath("");
            }
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    public Action getHelpAction() {
        Action helpAction = super.getHelpAction();
        helpAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
        helpAction.putValue(Action.NAME, HmsConvertorBundle.message("help"));
        return helpAction;
    }

    @Override
    public Action[] createActions() {
        return new Action[] {previousAction, getCancelAction(), getOKAction(), getHelpAction()};
    }

    @Override
    public Action getOKAction() {
        Action okAction = super.getOKAction();
        okAction.putValue(Action.NAME, HmsConvertorBundle.message("Analysis_n"));
        okAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
        return okAction;
    }

    @Override
    public Action getCancelAction() {
        Action cancelAction = super.getCancelAction();
        cancelAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        cancelAction.putValue(Action.NAME, HmsConvertorBundle.message("cancel_c"));
        return cancelAction;
    }

    @Override
    public void doHelpAction() {
        // bi report action: click help link.
        BIReportService.getInstance().traceHelpClick(project.getBasePath(), ConversionHelpEnum.PRE_ANALYZE);

        ApplicationManager.getApplication().invokeLater(() -> {
            BrowserUtil.browse(allianceDomain + HmsConvertorBundle.message("policy_url"));
        }, ModalityState.any());
    }

    @Override
    public void doOKAction() {
        // Detect xms adapter in current project.
        List<String> gAndHXmsPaths = FileUtil.getXmsPaths(project.getBasePath(), false);
        List<String> multiApkXmsPaths = FileUtil.getXmsPaths(project.getBasePath(), true);
        if (!toHmsRadioButton.isSelected() && gAndHXmsPaths.size() > 1 || multiApkXmsPaths.size() > 2) {
            Messages.showWarningDialog(project,
                constructMultiNotice(HmsConvertorBundle.message("multi_xms_adapter_notice"), gAndHXmsPaths,
                    multiApkXmsPaths),
                Constant.PLUGIN_NAME);
            return;
        }

        // If the current project contains old configurations,
        // infer the previous policy options.
        boolean convertedByOldSetting = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.CONVERTED_BY_OLD_SETTING, boolean.class, false);
        if (convertedByOldSetting) {
            RoutePolicy routePolicy = configCacheService.getProjectConfig(project.getBasePath(),
                ConfigKeyConstants.ROUTE_POLICY, RoutePolicy.class, RoutePolicy.UNKNOWN);
            inferenceGAndHOrMultiApk(project, routePolicy);
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST,
                getGAndHStartegy());
        }

        if (!checkHasConvertibleKit(SummaryCacheService.getInstance().getAllKits(project.getBasePath()))) {
            Messages.showWarningDialog(project, HmsConvertorBundle.message("no_convertible_notice"), "Warning");
            return;
        }

        // If the user changes the policy, reset the related configuration
        // and the xms adapter layer.
        if (cancelByChangePolicy()) {
            // bi report action: trace cancel operation.
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.POLICY_CHANGE);
            return;
        }

        // bi trace analyze time cost: analyze begins.
        BIInfoManager.getInstance().setAnalyzeBeginTime(project.getBasePath(), System.currentTimeMillis());

        final Task task = new Task.Backgroundable(project, Constant.PLUGIN_NAME, true, PerformInBackgroundOption.DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                asyncInspectAndGenerateAdapter(progressIndicator);
                // Remove the old policy flag.
                configCacheService.deleteProjectConfig(project.getBasePath(),
                    ConfigKeyConstants.CONVERTED_BY_OLD_SETTING);
            }
        };
        task.queue();
        super.doOKAction();
    }

    private String constructMultiNotice(String head, List<String> gAndHXmsPaths, List<String> multiApkXmsPaths) {
        StringBuffer sb = new StringBuffer();
        sb.append(UIConstants.Html.HTML_HEAD).append(head).append(UIConstants.Html.BR);
        if (gAndHXmsPaths != null) {
            for (String gAndHXmsPath : gAndHXmsPaths) {
                sb.append(UIConstants.Html.SPACE).append(" - ").append(gAndHXmsPath).append(UIConstants.Html.BR);
            }
        }
        if (multiApkXmsPaths != null) {
            for (String multiApkXmsPath : multiApkXmsPaths) {
                sb.append(UIConstants.Html.SPACE).append(" - ").append(multiApkXmsPath).append(UIConstants.Html.BR);
            }
        }
        sb.append(UIConstants.Html.HTML_END);
        return sb.toString();
    }

    private boolean checkHasConvertibleKit(List<String> allScannedKits) {
        if (allScannedKits == null || allScannedKits.size() == 0) {
            return false;
        }

        List<String> allScanndKitsCopy = new ArrayList<String>();
        allScanndKitsCopy.addAll(allScannedKits);
        allScanndKitsCopy.remove(KitsConstants.COMMON);
        allScanndKitsCopy.remove(KitsConstants.OTHER);
        if (allScanndKitsCopy.size() == 0) {
            return false;
        }

        // Perform Check Action
        if (toHmsRadioButton.isSelected()) {
            return toHmsSupportCheck(allScanndKitsCopy);
        } else {
            return addHmsSupportCheck(allScanndKitsCopy);
        }
    }

    private boolean toHmsSupportCheck(List<String> allScanndKits) {
        boolean supportToHms = false;
        for (int i = 0; i < allScanndKits.size(); i++) {
            if (KitUtil.supportKitToH(allScanndKits.get(i))) {
                supportToHms = true;
                break;
            }
        }
        if (supportToHms) {
            return true;
        } else {
            return false;
        }
    }

    private boolean addHmsSupportCheck(List<String> allScanndKits) {
        Set<String> addHmsSupportKits = getGaddHKitWhiteList();
        if (addHmsSupportKits == null || addHmsSupportKits.size() == 0) {
            return false;
        }
        addHmsSupportKits.add(KitsConstants.ML);
        boolean supportAndHms = false;
        for (int i = 0; i < allScanndKits.size(); i++) {
            if (addHmsSupportKits.contains(allScanndKits.get(i))) {
                supportAndHms = true;
                break;
            }
        }
        return supportAndHms;
    }

    private boolean cancelByChangePolicy() {
        RoutePolicy oldRoutePolicy = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.ROUTE_POLICY, RoutePolicy.class, RoutePolicy.UNKNOWN);

        boolean oldHGStrategy = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST,
            boolean.class, false);
        boolean oldIsMultiApk = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK,
            boolean.class, false);
        if (oldRoutePolicy == RoutePolicy.G_TO_H) {
            if (!toHmsRadioButton.isSelected()) {
                int decide = Messages.showDialog(project, HmsConvertorBundle.message("tohmspolicy_change_notice"),
                    Constant.PLUGIN_NAME, new String[] {"Continue", "Cancel"}, Messages.NO,
                    Messages.getInformationIcon());
                if (decide == Messages.NO || decide == -1) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        if (oldRoutePolicy == RoutePolicy.G_AND_H) {
            RoutePolicy newRoutePolicy = toHmsRadioButton.isSelected() ? RoutePolicy.G_TO_H : RoutePolicy.G_AND_H;
            boolean newHGStrategy = getGAndHStartegy();
            boolean newIsMultiApk = checkBoxG.isSelected();
            StringBuilder oldPolicy = new StringBuilder();
            if (oldIsMultiApk) {
                oldPolicy = (oldHGStrategy == true) ? oldPolicy.append("Multi APK (HMS API First)")
                    : oldPolicy.append("Multi APK (GMS API First)");
            } else {
                oldPolicy = (oldHGStrategy == true) ? oldPolicy.append("Add HMS API (HMS API First)")
                    : oldPolicy.append("Add HMS API (GMS API First)");
            }
            if (newRoutePolicy == RoutePolicy.G_TO_H || oldHGStrategy != newHGStrategy
                || oldIsMultiApk != newIsMultiApk) {
                return showAndHMSPolicyChangeNotice(oldPolicy.toString());
            }
        }
        return false;
    }

    private boolean showAndHMSPolicyChangeNotice(String oldPolicy) {
        int decide =
            Messages.showDialog(project, HmsConvertorBundle.message4Param("andhmspolicy_change_notice", oldPolicy),
                Constant.PLUGIN_NAME, new String[] {"Continue", "Cancel"}, Messages.NO, Messages.getInformationIcon());
        if (decide == Messages.NO || decide == -1) {
            return true;
        } else {
            deleteOldPolicyContent();
            return false;
        }
    }

    private void deleteOldPolicyContent() {
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.ROUTE_POLICY);
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST);
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK);
        deleteNewModule();
        deleteOldXmsCode();
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.XMS_PATH);
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.XMS_MULTI_PATH);
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.NEW_MODULE);
        String repoID = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.REPO_ID, String.class, "");
        String saveFilePath = Constant.PLUGIN_CACHE_PATH + repoID + "/" + ProjectConstants.Result.LAST_XMSDIFF_JSON;
        com.intellij.openapi.util.io.FileUtil.delete(new File(saveFilePath));
        LocalFileSystem.getInstance().refresh(true);
    }

    private void deleteOldXmsCode() {
        boolean convertedByOldSetting = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.CONVERTED_BY_OLD_SETTING, boolean.class, false);
        if (!convertedByOldSetting) {
            return;
        }

        List<String> gAndHXmsPaths = FileUtil.getXmsPaths(project.getBasePath(), false);
        List<String> multiApkXmsPaths = FileUtil.getXmsPaths(project.getBasePath(), true);
        for (int i = 0; i < gAndHXmsPaths.size(); i++) {
            String path = gAndHXmsPaths.get(i);
            com.intellij.openapi.util.io.FileUtil.delete(new File(path));
        }
        for (int i = 0; i < multiApkXmsPaths.size(); i++) {
            String path = multiApkXmsPaths.get(i);
            com.intellij.openapi.util.io.FileUtil
                .delete(new File(path.substring(0, path.length() - "/java/org/xms".length())));
        }
    }

    @Override
    public void doCancelAction() {
        // bi report action: trace cancel operation.
        BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.PRE_ANALYZE);
        HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
            hmsConvertorToolWindow.getSummaryToolWindow()
                .refreshData(SummaryCacheService.getInstance().getKit2Methods(project.getBasePath()));
            hmsConvertorToolWindow.showTabbedPane(UIConstants.ToolWindow.TAB_SUMMARY_INDEX);
        });
        ConversionCacheService.getInstance().addConversions(project.getBasePath(), Collections.EMPTY_LIST, false);
        super.doCancelAction();
    }

    private void asyncInspectAndGenerateAdapter(ProgressIndicator indicator) {
        if (indicator == null) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("analyze_error"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        indicator.setIndeterminate(true);
        try {
            hmsConvertorXmsGenerater = new HmsConvertorXmsGenerater(project);
            HmsConvertorState.set(project, HmsConvertorState.RUNNING);
            RoutePolicy routePolicy = getRoutePolicy();
            LOG.info("Start to second analyze and generate xms adapter. routePolicy = {}", routePolicy);
            TimeUtil.getInstance().getStartTime();

            // Generate an XMS adapter for current project.
            if (routePolicy.equals(RoutePolicy.G_AND_H)) {
                indicator.setText("Generating an XMS adapter for " + inspectFolder + "...");
                indicator.setText2(HmsConvertorBundle.message("indicater_generate_notice2"));
                boolean success = hmsConvertorXmsGenerater.generateNewModule(
                    SummaryCacheService.getInstance().getAllDependency(project.getBasePath()), getGAndHStartegy(),
                    checkBoxG.isSelected());
                if (!success || indicator.isCanceled()) {
                    if (indicator.isCanceled()) {
                        // bi report action: trace cancel operation.
                        BIReportService.getInstance()
                            .traceCancelListener(project.getBasePath(), CancelableViewEnum.SECOND_ANALYZE);
                    }

                    clearCacheByCancel();
                    deleteOldPolicyContent();
                    return;
                }
            }

            // Analyze the project by new policy
            // and generate related conversion list.
            if (routePolicy.equals(RoutePolicy.G_AND_H)) {
                indicator.setText("Analyzing " + inspectFolder + "... Please wait.");
                indicator.setText2(HmsConvertorBundle.message("indicater_analyze_notice2"));
                if (!ClientUtil.getPluginPackagePath().isPresent()) {
                    throw new NoSuchFileException(HmsConvertorBundle.message("no_engine_found"));
                }
                String pluginPackagePath = ClientUtil.getPluginPackagePath().get();
                pluginPackagePath = pluginPackagePath.replace("\\", "/");
                HmsConvertorStarter starter =
                    new HmsConvertorStarter(project, routePolicy, UISettings.getInstance().getFontSize());
                if (!starter.inspectSource(pluginPackagePath, commentEnable)) {
                    BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("engine_analysis_error"),
                        project, Constant.PLUGIN_NAME, true);
                    return;
                }
            }
            if (indicator.isCanceled()) {
                // bi report action: trace cancel operation.
                BIReportService.getInstance()
                    .traceCancelListener(project.getBasePath(), CancelableViewEnum.SECOND_ANALYZE);
                if (routePolicy == RoutePolicy.G_AND_H) {
                    clearCacheByCancel();
                    deleteOldPolicyContent();
                }
                return;
            }
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.ROUTE_POLICY, routePolicy);
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST,
                getGAndHStartegy());
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK,
                checkBoxG.isSelected());

            // bi report action: trace function selection.
            traceFunctionSelection(project.getBasePath());

            // If the current policy is 'To HMS', delete xms module.
            deleteModule(routePolicy);

            // Refresh data in the tool windows.
            HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
                hmsConvertorToolWindow.getSummaryToolWindow()
                    .refreshData(SummaryCacheService.getInstance().getKit2Methods(project.getBasePath()));
                hmsConvertorToolWindow.getSourceConvertorToolWindow()
                    .refreshData(ConversionCacheService.getInstance().getAllConversions(project.getBasePath()));
                hmsConvertorToolWindow.showTabbedPane(UIConstants.ToolWindow.TAB_CONVERSION_INDEX);

                SummaryCacheService.getInstance().saveSummary(project.getBasePath());
                ConversionCacheService.getInstance().saveConversions(project.getBasePath());
                openReadme();
            });

            showXmsDiff(hmsConvertorXmsGenerater.getDiff());
            LOG.info("Second analyze finished! Elapsed time: {}", TimeUtil.getInstance().getElapsedTime());

            // bi report action: trace analyze time cost, analyze ends.
            Long timeCost =
                System.currentTimeMillis() - BIInfoManager.getInstance().getAnalyzeBeginTime(project.getBasePath());
            BIReportService.getInstance()
                .traceTimeAnalyzeCost(project.getBasePath(), OperationViewEnum.PRE_ANALYZE_VIEW,
                    String.valueOf(timeCost), BIReportService.getInstance().getJvmXmx(project.getBasePath()));
            BIInfoManager.getInstance().clearData(project.getBasePath());

            // bi report action: trace analyze result.
            BIReportService.getInstance().traceAnalyzeResult(project.getBasePath());

            // sync convert time to local file
            ConfigCacheService.getInstance()
                .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.NEW_CONVERSION_BEGIN_TIME,
                    BIReportService.getInstance().getNewConversionBeginTime(project.getBasePath()));
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("analyze_error"), project,
                Constant.PLUGIN_NAME, true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("analyze_error"), project,
                Constant.PLUGIN_NAME, true);
        } finally {
            HmsConvertorState.set(project, HmsConvertorState.IDLE);
        }
    }

    private void traceFunctionSelection(String basePath) {
        String projectType = configCacheService.getProjectConfig(basePath, ConfigKeyConstants.PROJECT_TYPE,
            String.class, ProjectConstants.Type.APP);
        boolean commentMode =
            configCacheService.getProjectConfig(basePath, ConfigKeyConstants.COMMENT, boolean.class, false);
        RoutePolicy routePolicy = configCacheService.getProjectConfig(basePath, ConfigKeyConstants.ROUTE_POLICY,
            RoutePolicy.class, RoutePolicy.G_TO_H);
        boolean hmsFirst =
            configCacheService.getProjectConfig(basePath, ConfigKeyConstants.HMS_FIRST, boolean.class, false);
        boolean variantApk =
            configCacheService.getProjectConfig(basePath, ConfigKeyConstants.MULTI_APK, boolean.class, false);
        String strategy;
        if (projectType.equals(ProjectConstants.Type.APP)) {
            strategy = routePolicy.equals(RoutePolicy.G_TO_H) ? ConversionStrategyEnum.HMS.getValue()
                : hmsFirst ? ConversionStrategyEnum.H_FIRST.getValue() : ConversionStrategyEnum.G_FIRST.getValue();
        } else {
            strategy = routePolicy.equals(RoutePolicy.G_TO_H) ? ConversionStrategyEnum.HMS.getValue()
                : ConversionStrategyEnum.H_FIRST.getValue();
        }

        FunctionSelectionBean data = FunctionSelectionBean.builder()
            .projectType(projectType.equals(ProjectConstants.Type.APP) ? ProjectTypeEnum.APP.getValue()
                : ProjectTypeEnum.SDK.getValue())
            .commentMode(commentMode)
            .strategy(strategy)
            .variantApk(variantApk)
            .build();
        BIReportService.getInstance().traceFunctionSelection(project.getBasePath(), data);
    }

    private void openReadme() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String xmsAdapterPath = project.getBasePath() + XmsConstants.XMS_ADAPTER;
            String defectFile = xmsAdapterPath.replace("\\", "/") + XmsConstants.README_FILE;
            File file = new File(defectFile);
            if (file.exists()) {
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
                FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            }
        }, ModalityState.defaultModalityState());
    }

    private void showXmsDiff(Diff diff) {
        if (diff == null) {
            LOG.info("Empty diff");
            HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
                hmsConvertorToolWindow.getXmsDiffWindow().refreshData(null);
            });
        } else {
            LOG.info("Have diff");
            HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
                hmsConvertorToolWindow.getXmsDiffWindow().refreshData(new XmsDiff(diff));
                XmsDiffCacheService.getInstance().saveXmsDiff(project.getBasePath());
                hmsConvertorToolWindow.showTabbedPane(UIConstants.ToolWindow.TAB_XMSDIFF_INDEX);
            });
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showInfoMessage(project, HmsConvertorBundle.message("xms_regenerate_notice"),
                    Constant.PLUGIN_NAME);
            }, ModalityState.defaultModalityState());
        }
    }

    private void deleteModule(RoutePolicy routePolicy) {
        boolean isNewModule = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.NEW_MODULE,
            boolean.class, false);
        if (isNewModule && routePolicy.equals(RoutePolicy.G_TO_H)) {
            deleteNewModule();
        }
    }

    private boolean getGAndHStartegy() {
        if (andHmsHFRadioButton.isSelected()) {
            return true;
        }
        return false;
    }

    private void deleteNewModule() {
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.NEW_MODULE);
        String modulePath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "") + XmsConstants.XMS_ADAPTER;
        FileUtil.deleteFiles(new File(modulePath));
        LocalFileSystem.getInstance().refresh(true);
    }

    private String getXmsPath() {
        return xmsPathTextField.getText().replace("\\", "/");
    }

    private void setXmsPath(String xmsPath) {
        xmsPathTextField.setText(xmsPath);
    }

    private RoutePolicy getRoutePolicy() {
        if (toHmsRadioButton.isSelected()) {
            return RoutePolicy.G_TO_H;
        } else {
            return RoutePolicy.G_AND_H;
        }
    }

    private void clearCacheByCancel() {
        ConversionCacheService.getInstance().clearConversions(project.getBasePath());
        SummaryCacheManager.getInstance().clearKit2Methods(project.getBasePath());
    }

    private class Browse extends MouseAdapter {
        private String allianceDomain;

        private HelpLinkType url;

        Browse(String allianceDomain, HelpLinkType url) {
            this.allianceDomain = allianceDomain;
            this.url = url;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // bi report action: click help link.
            BIReportService.getInstance().traceHelpClick(project.getBasePath(), convertBIString(url));
            ApplicationManager.getApplication()
                .invokeLater(() -> BrowserUtil.browse(allianceDomain + HmsConvertorBundle.message(url.getValue())),
                    ModalityState.any());
        }

        private ConversionHelpEnum convertBIString(HelpLinkType urlType) {
            switch (urlType) {
                case ADD_HMS:
                    return ConversionHelpEnum.PRE_ANALYZE_GH;
                case TO_HMS:
                    return ConversionHelpEnum.PRE_ANALYZE_G2H;
                case XMS_PATH:
                    return ConversionHelpEnum.PRE_ANALYZE_XMS_PATH;
                case NOT_SUPPORT_API:
                    return ConversionHelpEnum.PRE_ANALYZE_NOT_SUPPORT_API;
                case NOT_SUPPORT_METHOD:
                    return ConversionHelpEnum.PRE_ANALYZE_NOT_SUPPORT_METHOD;
                default:
                    return null;
            }
        }
    }

    private class CopyAction extends AnAction {
        CopyAction() {
            super("Copy");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            String myString = showData.get(SummaryConstants.NOT_SUPPORT_VERSION_CONTENT);
            StringSelection stringSelection = new StringSelection(myString);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }

    public class PreviousAction extends DialogWrapperAction {
        private static final long serialVersionUID = 120992005988298014L;

        private PolicySettingDialog policySettingDialog;

        protected PreviousAction(PolicySettingDialog policySettingDialog) {
            super(HmsConvertorBundle.message("previous_p"));
            this.policySettingDialog = policySettingDialog;
        }

        @Override
        protected void doAction(ActionEvent actionEvent) {
            policySettingDialog.doCancelAction();
            String inspectPath = configCacheService.getProjectConfig(project.getBasePath(),
                ConfigKeyConstants.INSPECT_PATH, String.class, "");
            HmsConvertorStartDialog dialog = new HmsConvertorStartDialog(project, inspectPath);
            dialog.show();
        }
    }
}
