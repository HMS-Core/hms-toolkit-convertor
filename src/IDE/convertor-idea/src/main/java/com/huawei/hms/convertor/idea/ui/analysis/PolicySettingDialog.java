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

import com.huawei.generator.g2x.po.summary.Diff;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.hms.convertor.core.bi.bean.FunctionSelectionBean;
import com.huawei.hms.convertor.core.bi.enumration.AnalyseExportEnum;
import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.ConversionHelpEnum;
import com.huawei.hms.convertor.core.bi.enumration.ConversionStrategyEnum;
import com.huawei.hms.convertor.core.bi.enumration.OperationViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.ProjectTypeEnum;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.diff.Strategy;
import com.huawei.hms.convertor.core.result.diff.UpdatedXmsService;
import com.huawei.hms.convertor.core.result.diff.XmsDiff;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;
import com.huawei.hms.convertor.core.result.summary.SummaryConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.HmsConvertorState;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.AnalyseResultExportUtil;
import com.huawei.hms.convertor.idea.util.ClientUtil;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.idea.util.TimeUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.ConversionCacheService;
import com.huawei.hms.convertor.openapi.ProgressService;
import com.huawei.hms.convertor.openapi.SummaryCacheService;
import com.huawei.hms.convertor.openapi.XmsDiffCacheService;
import com.huawei.hms.convertor.openapi.XmsGenerateService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.util.KitUtil;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.ShowFilePathAction;
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
import com.intellij.openapi.progress.ProcessCanceledException;
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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Policy setting dialog
 *
 * @since 2020-04-29
 */
@Slf4j
public class PolicySettingDialog extends DialogWrapper {
    private static final Pattern XMS_PATH_PATTERN = Pattern.compile("(.)*\\/src\\/main\\/java$");

    private static final Pattern GH_PATH_PATTERN = Pattern.compile("(.)*\\/src$");

    private static final String XMS_PATH_SELECTED = "/xmsadapter/src";

    private static final String XMS_PATH = "/xmsadapter/src/main/java";

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

    private static final String SUPPORT_JDK_COMPILE_VERSION_STRING = "1.8";

    private static final int PANEL_PREFER_HEIGHT_FS_20 = 810;

    private static final int PANEL_MIN_HEIGHT_FS_20 = 804;

    private static final int LENGTH_0 = 0;

    private static final int LENGTH_1 = 1;

    private static final int LENGTH_2 = 2;

    private String type;

    private String compileVersion;

    private JPanel contentPane;

    private JRadioButton toHmsRadioButton;

    private JRadioButton andHmsHFRadioButton;

    private JRadioButton andHmsGFRadioButton;

    private JLabel analysisLabel;

    private JLabel detailsLabel;

    private JLabel exportAnalyseResultLabel;

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

    private JCheckBox checkBoxH;

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

    private Action showLogAction;

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

        String inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        inspectFolder = StringUtils.substring(inspectPath, inspectPath.lastIndexOf(Constant.UNIX_FILE_SEPARATOR_IN_CHAR) + 1);
        if (StringUtils.isEmpty(inspectFolder)) {
            log.warn("inspect folder is empty.");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("start_error"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        previousAction = new PreviousAction(this);
        previousAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
        showLogAction = new ShowLogDialogAction();
        allianceDomain = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.ALLIANCE_DOMAIN,
            String.class, "");
        commentEnable = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.COMMENT,
            Boolean.class, false);

        init();
        addListener();

        // Adjust the display size.
        adjustPanelSize();
        adjsutTableSize();
    }

    public static Set<String> getGaddHKitTrustList() {
        Set<String> supportKitInfos = XmsGenerateService.supportKitInfo();
        return supportKitInfos;
    }

    @Override
    public void init() {
        super.init();
        setTitle(Constant.PLUGIN_NAME);
        getWindow().setIconImage(ImageLoader.loadFromResource(UIConstants.Dialog.WINDOW_ICON_RESOURCE));

        noticeLabel.setText(HmsConvertorBundle.message("notice_title"));

        setJdkVersionNotification();

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
            checkBoxH.setVisible(false);
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
        checkBoxH.setText(HmsConvertorBundle.message("only_h"));
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

    @Override
    public Action getHelpAction() {
        Action helpAction = super.getHelpAction();
        helpAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
        helpAction.putValue(Action.NAME, HmsConvertorBundle.message("help"));
        return helpAction;
    }

    @Override
    public Action[] createActions() {
        return new Action[] {showLogAction, previousAction, getCancelAction(), getOKAction(), getHelpAction()};
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
        // Note: generate strategy by user's last convertion from config; default: multiApk = false, hFirst = true
        boolean multiApk = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK,
            boolean.class, false);
        boolean hFirst = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST,
            boolean.class, true);
        boolean onlyH =
            configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.ONLY_H, boolean.class, false);
        Strategy strategy = new Strategy();
        strategy.setHmsFirst(hFirst);
        strategy.setOnlyG(multiApk);
        strategy.setOnlyH(onlyH);
        List<GeneratorStrategyKind> generatorStrategyKinds = UpdatedXmsService.getStrategyKindList(strategy);
        Map<String, String> kitMap =
            UpdatedXmsService.getKitMap(SummaryCacheService.getInstance().getAllDependency(project.getBasePath()));
        List<String> modifiedRoutes =
            FileUtil.getUserModifiedRoutes(project.getBasePath(), kitMap, generatorStrategyKinds);
        if (!modifiedRoutes.isEmpty()) {
            Messages.showWarningDialog(project,
                constructMultiNotice(HmsConvertorBundle.message("multi_xms_adapter_notice"), modifiedRoutes),
                Constant.PLUGIN_NAME);
            return;
        }

        boolean convertedByOldSetting = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.CONVERTED_BY_OLD_SETTING, boolean.class, false);
        if (convertedByOldSetting) {
            RoutePolicy routePolicy = configCacheService.getProjectConfig(project.getBasePath(),
                ConfigKeyConstants.ROUTE_POLICY, RoutePolicy.class, RoutePolicy.UNKNOWN);
            updateConfigForOld(project, routePolicy);
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

        // before analyse backgroup task start, so need to clear export cache
        SummaryCacheService.getInstance().clearAnalyseResultCache4Export(project.getBasePath());

        Task task = new Task.Backgroundable(project, Constant.PLUGIN_NAME, true, PerformInBackgroundOption.DEAF) {
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

    @Override
    public void doCancelAction() {
        // bi report action: trace cancel operation.
        BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.PRE_ANALYZE);

        HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
            hmsConvertorToolWindow.getSummaryToolWindow()
                .refreshData(SummaryCacheService.getInstance().getKit2FixbotMethodsMap(project.getBasePath()),
                    SummaryCacheManager.getInstance().getKitStatisticsResults(project.getBasePath()));
            hmsConvertorToolWindow.showTabbedPane(UIConstants.ToolWindow.TAB_SUMMARY_INDEX);
        });

        // analyse dialog canceled
        // so need to clear export cache
        SummaryCacheService.getInstance().clearAnalyseResultCache4Export(project.getBasePath());
        // and need to clear conversion toolWindow cache
        ConversionCacheService.getInstance().clearConversions(project.getBasePath());
        SummaryCacheService.getInstance().clearAnalyseResultCache4ConversionToolWindow(project.getBasePath());
        // and need to clear summary toolWindow cache
        SummaryCacheService.getInstance().clearAnalyseResultCache4SummaryResult(project.getBasePath());
        super.doCancelAction();
    }

    private void setJdkVersionNotification() {
        String ideSdkVersion = System.getProperty("java.version");
        compileVersion = showData.get(SummaryConstants.JDK_COMPILE_VERSION);
        log.info("ide jdk version {}, app jdk version {}", ideSdkVersion, compileVersion);

        if (StringUtil.isEmpty(compileVersion)) {
            if (compareVersion(ideSdkVersion, SUPPORT_JDK_COMPILE_VERSION_STRING) == -1) {
                firstNoticeLabel.setText(HmsConvertorBundle.message("notice_first"));
            } else {
                firstNoticeLabel.setVisible(false);
                notice1Label.setVisible(false);
            }
            return;
        }

        if (compileVersion.startsWith("JavaVersion")) {
            String[] compileVersionList = compileVersion.split("_");
            String appVersion = compileVersionList[compileVersionList.length - 2] + "."
                + compileVersionList[compileVersionList.length - 1];
            if (compareVersion(appVersion, SUPPORT_JDK_COMPILE_VERSION_STRING) == -1) {
                firstNoticeLabel.setText(HmsConvertorBundle.message("notice_first"));
            } else {
                firstNoticeLabel.setVisible(false);
                notice1Label.setVisible(false);
            }
            return;
        }

        if (compareVersion(compileVersion, SUPPORT_JDK_COMPILE_VERSION_STRING) == -1) {
            firstNoticeLabel.setText(HmsConvertorBundle.message("notice_first"));
        } else {
            firstNoticeLabel.setVisible(false);
            notice1Label.setVisible(false);
        }
    }

    /**
     * @param version1 Variables for comparison
     * @param version2 Variables for comparison
     * @return version1 > version2 return 1; version1 < version2 return -1; version1 == version2 return 0;
     */
    private int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }
        String[] version1Array = version1.split("[._]");
        String[] version2Array = version2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = -1;
        try {
            while (index < minLen
                && (diff = Long.parseLong(version1Array[index]) - Long.parseLong(version2Array[index])) == 0) {
                index++;
            }
        } catch (NumberFormatException e) {
            log.error("unable to parse jdk compile version", e);
        }
        return diff >= 0 ? 1 : -1;
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
        log.info("fontSize: {} noticeHeight: {}", fontSize, noticeHeight);
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

        initExportAnalyseResultLabel();
    }

    private void initExportAnalyseResultLabel() {
        exportAnalyseResultLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportAnalyseResultLabel.setText(HmsConvertorBundle.message("policy_setting_export_analyse_result"));
    }

    private void setXmsPath() {
        String folder = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        if (checkBoxG.isSelected() || checkBoxH.isSelected()) {
            setXmsPath(folder + XMS_PATH_SELECTED);
        } else {
            setXmsPath(folder + XMS_PATH);
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
            borderColor = new Color(UIConstants.Dialog.PolicySetting.BorderColorEnum.R.getValue(), UIConstants.Dialog.PolicySetting.BorderColorEnum.G.getValue(), UIConstants.Dialog.PolicySetting.BorderColorEnum.B.getValue());
            titleColor = new Color(UIConstants.Dialog.PolicySetting.TitleColorDarculaEnum.R.getValue(), UIConstants.Dialog.PolicySetting.TitleColorDarculaEnum.G.getValue(), UIConstants.Dialog.PolicySetting.TitleColorDarculaEnum.B.getValue());
            row1Color = new Color(UIConstants.Dialog.PolicySetting.Row1ColorDarculaEnum.R.getValue(), UIConstants.Dialog.PolicySetting.Row1ColorDarculaEnum.G.getValue(), UIConstants.Dialog.PolicySetting.Row1ColorDarculaEnum.B.getValue());
            otherRowColor = new Color(UIConstants.Dialog.PolicySetting.OtherRowColorDarculaEnum.R.getValue(), UIConstants.Dialog.PolicySetting.OtherRowColorDarculaEnum.G.getValue(), UIConstants.Dialog.PolicySetting.OtherRowColorDarculaEnum.B.getValue());
        } else {
            lineLabel.setIcon(IconUtil.GUIDE_LINE_GRAY);
            notice1Label.setIcon(IconUtil.POINT_ITEM_DARK);
            notice2Label.setIcon(IconUtil.POINT_ITEM_DARK);
            notice3Label.setIcon(IconUtil.POINT_ITEM_DARK);
            notice4Label.setIcon(IconUtil.POINT_ITEM_DARK);
            borderColor = new Color(UIConstants.Dialog.PolicySetting.BorderColorEnum.R.getValue(), UIConstants.Dialog.PolicySetting.BorderColorEnum.G.getValue(), UIConstants.Dialog.PolicySetting.BorderColorEnum.B.getValue());
            titleColor = new Color(UIConstants.Dialog.PolicySetting.TitleColorEnum.R.getValue(), UIConstants.Dialog.PolicySetting.TitleColorEnum.G.getValue(), UIConstants.Dialog.PolicySetting.TitleColorEnum.B.getValue());
            row1Color = new Color(UIConstants.Dialog.PolicySetting.Row1ColorEnum.R.getValue(), UIConstants.Dialog.PolicySetting.Row1ColorEnum.G.getValue(), UIConstants.Dialog.PolicySetting.Row1ColorEnum.B.getValue());
            otherRowColor = new Color(UIConstants.Dialog.PolicySetting.OtherRowColorEnum.R.getValue(), UIConstants.Dialog.PolicySetting.OtherRowColorEnum.G.getValue(), UIConstants.Dialog.PolicySetting.OtherRowColorEnum.B.getValue());
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

        setExportAnalyseResultLabelColor();
    }

    private void setExportAnalyseResultLabelColor() {
        exportAnalyseResultLabel.setForeground(JBColor.BLUE);
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
        log.info("init, routePolicy: {}, routePriority: HMS First, xmsPaths: {}.", routePolicy,
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
            showGInfo();
            setXmsPathEnabled4GorH();
        });

        checkBoxH.addActionListener(event -> {
            showHInfo();
            setXmsPathEnabled4GorH();
        });

        APILabel.addMouseListener(new Browse(allianceDomain, HelpLinkType.NOT_SUPPORT_API));
        methodLabel.addMouseListener(new Browse(allianceDomain, HelpLinkType.NOT_SUPPORT_METHOD));

        detailsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == MOUSE_SINGLE_CLICK)) {
                    SummaryDialog summaryDialog = new SummaryDialog(project);
                    summaryDialog.refreshData(
                        SummaryCacheService.getInstance().getKit2FixbotMethodsMap(project.getBasePath()),
                        SummaryCacheManager.getInstance().getKitStatisticsResults(project.getBasePath()));
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
                setXmsPathEnabled4GorH();
            }
        });

        andHmsGFRadioButton.addItemListener(event -> {
            setXmsPathEnabled4GorH();
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

        addExportAnalyseResultLabelListener();
    }

    private void addExportAnalyseResultLabelListener() {
        exportAnalyseResultLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == MOUSE_SINGLE_CLICK)) {
                    BIReportService.getInstance()
                        .traceExportClick(project.getBasePath(), AnalyseExportEnum.PRE_ANALYZE);

                    ApplicationManager.getApplication().invokeAndWait(() -> {
                        exportAnalyseResult();
                    }, ModalityState.defaultModalityState());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                exportAnalyseResultLabel.setText(
                    "<html><u>" + HmsConvertorBundle.message("policy_setting_export_analyse_result") + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exportAnalyseResultLabel.setText(HmsConvertorBundle.message("policy_setting_export_analyse_result"));
            }
        });
    }

    private void setXmsPathVisible() {
        andHmsGFRadioButton.setVisible(false);
        xmsPathLabel.setVisible(false);
        xmsPathTextField.setVisible(false);
        checkBoxG.setVisible(false);
        checkBoxH.setVisible(false);
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

    private void showGInfo() {
        if (checkBoxG.isSelected()) {
            Messages.showDialog(project,
                HmsConvertorBundle.message("OnlyG_constraint") + " <a href=\"" + allianceDomain
                    + HmsConvertorBundle.message("OnlyG_constraint2"),
                UIConstants.Dialog.TITLE_WARNING, new String[] {"OK"}, Messages.OK, Messages.getInformationIcon());
        }
    }

    private void showHInfo() {
        if (checkBoxH.isSelected()) {
            Messages.showDialog(project,
                HmsConvertorBundle.message("OnlyH_constraint") + " <a href=\"" + allianceDomain
                    + HmsConvertorBundle.message("OnlyH_constraint2"),
                UIConstants.Dialog.TITLE_WARNING, new String[] {"OK"}, Messages.OK, Messages.getInformationIcon());
        }
    }

    private void setXmsPathEnabled() {
        boolean isToHms = toHmsRadioButton.isSelected();

        xmsPathLabel.setEnabled(!isToHms);
        xmsPathTextField.setEnabled(!isToHms);
        checkBoxG.setEnabled(!isToHms);
        checkBoxH.setEnabled(!isToHms);
    }

    private void setXmsPathEnabled4GorH() {
        boolean isOnlyG = checkBoxG.isSelected();
        boolean isOnlyH = checkBoxH.isSelected();
        String inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        List<String> excludePaths = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.EXCLUDE_PATH, List.class, new ArrayList());
        setXmsPath();
        if (!getXmsPath().isEmpty()) {
            return;
        }
        if (isOnlyG || isOnlyH) {
            if (getXmsPath().isEmpty()) {
                List<String> xms4GorHPaths = FileUtil.findPathsByMask(GH_PATH_PATTERN, inspectPath, excludePaths);
                if (xms4GorHPaths.size() == 1) {
                    setXmsPath(xms4GorHPaths.get(0));
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
        JScrollPane jScrollPane = new JScrollPane(contentPane);
        jScrollPane.setBorder(null);
        return jScrollPane;
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        final JPanel southPanel = (JPanel) super.createSouthPanel();
        JScrollPane jScrollPane = new JScrollPane(southPanel);
        jScrollPane.setBorder(null);
        return jScrollPane;
    }

    /**
     * just adapt to old version
     */
    private void updateConfigForOld(Project project, RoutePolicy oldRoutePolicy) {
        ConfigCacheService configCache = ConfigCacheService.getInstance();
        String[] files = FileUtil.getSummaryModule(project.getBasePath());
        RoutePolicy routePolicy = oldRoutePolicy;
        switch (files.length) {
            case LENGTH_2:
                routePolicy = RoutePolicy.G_AND_H;
                configCache.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK, true);
                break;
            case LENGTH_1:
                routePolicy = RoutePolicy.G_AND_H;
                configCache.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK, false);
                break;
            case LENGTH_0:
                routePolicy = RoutePolicy.G_TO_H;
                break;
            default:
                log.warn("project have more than one converted paths");
        }
        configCache.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.ROUTE_POLICY, routePolicy);
    }

    private String constructMultiNotice(String head, List<String> modifiedRoutes) {
        StringBuffer sb = new StringBuffer();
        sb.append(UIConstants.Html.HTML_HEAD).append(head).append(UIConstants.Html.BR);
        if (modifiedRoutes != null) {
            for (String modifiedRoute : modifiedRoutes) {
                sb.append(UIConstants.Html.SPACE).append(" - ").append(modifiedRoute).append(UIConstants.Html.BR);
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
        Set<String> addHmsSupportKits = getGaddHKitTrustList();
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
        boolean oldIsOnlyH =
            configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.ONLY_H, boolean.class, false);
        if (oldRoutePolicy == RoutePolicy.G_TO_H) {
            if (toHmsRadioButton.isSelected()) {
                return false;
            }
            int decide = Messages.showDialog(project, HmsConvertorBundle.message("tohmspolicy_change_notice"),
                Constant.PLUGIN_NAME, new String[] {"Continue", "Cancel"}, Messages.NO, Messages.getInformationIcon());
            if (decide == Messages.NO || decide == -1) {
                return true;
            } else {
                return false;
            }
        }

        if (oldRoutePolicy != RoutePolicy.G_AND_H) {
            return false;
        }
        RoutePolicy newRoutePolicy = toHmsRadioButton.isSelected() ? RoutePolicy.G_TO_H : RoutePolicy.G_AND_H;
        boolean newHGStrategy = getGAndHStartegy();
        boolean newIsMultiApk = checkBoxG.isSelected();
        boolean newIsOnlyH = checkBoxH.isSelected();
        if (newRoutePolicy == RoutePolicy.G_TO_H || oldHGStrategy != newHGStrategy || oldIsMultiApk != newIsMultiApk
            || oldIsOnlyH != newIsOnlyH) {
            return showAndHMSPolicyChangeNotice(getOldPolicy(oldHGStrategy, oldIsMultiApk, oldIsOnlyH));
        }
        return false;
    }

    private String getOldPolicy(boolean oldHGStrategy, boolean oldIsMultiApk, boolean oldIsOnlyH) {
        StringBuilder oldPolicy = new StringBuilder();
        if (oldIsMultiApk && !oldIsOnlyH) {
            oldPolicy = oldHGStrategy ? oldPolicy.append("Multi APK (HMS API First & GMS SDK)")
                : oldPolicy.append("Multi APK (GMS API First & GMS SDK)");
        } else if (oldIsOnlyH && !oldIsMultiApk) {
            oldPolicy = oldHGStrategy ? oldPolicy.append("Multi APK (HMS API First & HMS SDK)")
                : oldPolicy.append("Multi APK (GMS API First & HMS SDK)");
        } else if (oldIsMultiApk && oldIsOnlyH) {
            oldPolicy = oldHGStrategy ? oldPolicy.append("Multi APK (HMS API First & GMS SDK & HMS SDK)")
                : oldPolicy.append("Multi APK (GMS API First & GMS SDK & HMS SDK)");
        } else {
            oldPolicy = oldHGStrategy ? oldPolicy.append("Add HMS API (HMS API First)")
                : oldPolicy.append("Add HMS API (GMS API First)");
        }
        return oldPolicy.toString();
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
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.ONLY_H);
        deleteModule();
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.XMS_PATH);
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.XMS_MULTI_PATH);
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.NEW_MODULE);
        String repoID = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.REPO_ID, String.class, "");
        String saveFilePath = PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + repoID + Constant.UNIX_FILE_SEPARATOR
            + ProjectConstants.Result.LAST_XMSDIFF_JSON;
        com.intellij.openapi.util.io.FileUtil.delete(new File(saveFilePath));
        LocalFileSystem.getInstance().refresh(true);
    }

    private void asyncInspectAndGenerateAdapter(ProgressIndicator indicator) {
        if (indicator == null) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("analyze_error"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        indicator.setIndeterminate(false);
        RoutePolicy routePolicy = getRoutePolicy();
        try {
            indicator.checkCanceled();
            indicator.setFraction(ProgressService.ProgressStage.START_ANALYSIS.getFraction());

            hmsConvertorXmsGenerater = new HmsConvertorXmsGenerater(project);
            HmsConvertorState.set(project, HmsConvertorState.RUNNING);
            log.info("Start to second analyze and generate xms adapter. routePolicy: {}.", routePolicy);
            TimeUtil.getInstance().getStartTime();

            indicator.checkCanceled();
            indicator.setFraction(ProgressService.ProgressStage.NEW_XMS_GENERATOR.getFraction());
            // Generate an XMS adapter for current project.
            Strategy strategy = new Strategy();
            if (routePolicy.equals(RoutePolicy.G_AND_H)) {
                indicator.setText("Generating an XMS adapter for " + inspectFolder + "...");
                indicator.setText2(HmsConvertorBundle.message("indicator_generate_notice2"));
                strategy.setHmsFirst(getGAndHStartegy());
                strategy.setOnlyG(checkBoxG.isSelected());
                strategy.setOnlyH(checkBoxH.isSelected());
                boolean success = hmsConvertorXmsGenerater.generateNewModule(
                    SummaryCacheService.getInstance().getAllDependency(project.getBasePath()), strategy);

                indicator.checkCanceled();
                indicator.setFraction(ProgressService.ProgressStage.GENERATED_NEW_MODULE.getFraction());

                if (!success) {
                    clearCacheByCancel();
                    deleteOldPolicyContent();
                    return;
                }
            }

            // Analyze the project by new policy
            // and generate related conversion list.
            if (routePolicy.equals(RoutePolicy.G_AND_H)) {
                indicator.setText("Analyzing " + inspectFolder + "... Please wait.");
                indicator.setText2(HmsConvertorBundle.message("indicator_analyze_notice2"));
                if (!ClientUtil.getPluginPackagePath().isPresent()) {
                    throw new NoSuchFileException(HmsConvertorBundle.message("no_engine_found"));
                }
                String pluginPackagePath = ClientUtil.getPluginPackagePath().get();
                pluginPackagePath = FileUtil.unifyToUnixFileSeparator(pluginPackagePath);
                HmsConvertorStarter starter =
                    new HmsConvertorStarter(project, routePolicy, UISettings.getInstance().getFontSize());
                if (!starter.inspectSource(pluginPackagePath, commentEnable, indicator, strategy)) {
                    BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("engine_analysis_error"),
                        project, Constant.PLUGIN_NAME, true);
                    return;
                }
            }

            indicator.checkCanceled();
            indicator.setFraction(ProgressService.ProgressStage.FINISHED.getFraction());

            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.ROUTE_POLICY, routePolicy);
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST,
                getGAndHStartegy());
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK,
                checkBoxG.isSelected());
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.ONLY_H,
                checkBoxH.isSelected());

            // bi report action: trace function selection.
            traceFunctionSelection(project.getBasePath());

            // If the current policy is 'To HMS', delete xms module.
            deleteModule(routePolicy);

            // Refresh data in the tool windows.
            HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
                hmsConvertorToolWindow.getSummaryToolWindow()
                    .refreshData(SummaryCacheService.getInstance().getKit2FixbotMethodsMap(project.getBasePath()),
                        SummaryCacheManager.getInstance().getKitStatisticsResults(project.getBasePath()));
                hmsConvertorToolWindow.getSourceConvertorToolWindow()
                    .refreshData(ConversionCacheService.getInstance().getAllConversions(project.getBasePath()));
                hmsConvertorToolWindow.showTabbedPane(UIConstants.ToolWindow.TAB_CONVERSION_INDEX);

                SummaryCacheService.getInstance().saveSummary(project.getBasePath());
                ConversionCacheService.getInstance().saveConversions(project.getBasePath());
                openReadme();
            });

            showXmsDiff(hmsConvertorXmsGenerater.getDiff());
            log.info("Second analyze finished! Elapsed time: {}.", TimeUtil.getInstance().getElapsedTime());

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
        } catch (ProcessCanceledException e) {
            // bi report action: trace cancel operation.
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.SECOND_ANALYZE);
            BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("analyse_task_cancel_success"),
                project, Constant.PLUGIN_NAME, true);
            if (routePolicy.equals(RoutePolicy.G_AND_H)) {
                clearCacheByCancel();
                deleteOldPolicyContent();
            }
            log.warn(e.getMessage(), e);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("analyze_error"), project,
                Constant.PLUGIN_NAME, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
        boolean onlyH = configCacheService.getProjectConfig(basePath, ConfigKeyConstants.ONLY_H, boolean.class, false);
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
            .onlyH(onlyH)
            .build();
        BIReportService.getInstance().traceFunctionSelection(project.getBasePath(), data);
    }

    private void openReadme() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String xmsAdapterPath = project.getBasePath() + XmsConstants.XMS_ADAPTER;
            String defectFile = FileUtil.unifyToUnixFileSeparator(xmsAdapterPath) + XmsConstants.README_FILE;
            File file = new File(defectFile);
            if (file.exists()) {
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
                FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            }
        }, ModalityState.defaultModalityState());
    }

    private void exportAnalyseResult() {
        Optional<String> analyseFilePath;
        try {
            analyseFilePath = AnalyseResultExportUtil.exportPdf(project.getBasePath());
        } catch (Exception e) {
            log.error("export analyseResult fail, projectBasePath: {}.", project.getBasePath());
            BalloonNotifications.showWarnNotification(
                HmsConvertorBundle.message("policy_setting_export_analyse_result_error"), project, Constant.PLUGIN_NAME,
                true);
            return;
        }
        if (!analyseFilePath.isPresent()) {
            log.error("export analyseResult fail.");
            BalloonNotifications.showWarnNotification(
                HmsConvertorBundle.message("policy_setting_export_analyse_result_error"), project, Constant.PLUGIN_NAME,
                true);
            return;
        }

        File analyseResultFile = new File(analyseFilePath.get());
        ShowFilePathAction.openFile(analyseResultFile);
    }

    private void openLogDirectory() {
        File logDir = new File(PluginConstant.PluginDataDir.PLUGIN_LOG_PATH);
        ShowFilePathAction.openDirectory(logDir);
    }

    private void showXmsDiff(Diff diff) {
        if (diff == null) {
            log.info("Empty diff");
            HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
                hmsConvertorToolWindow.getXmsDiffWindow().refreshData(null);
            });
        } else {
            log.info("Have diff");
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
            deleteModule();
        }
    }

    private boolean getGAndHStartegy() {
        if (andHmsHFRadioButton.isSelected()) {
            return true;
        }
        return false;
    }

    private void deleteModule() {
        configCacheService.deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.NEW_MODULE);
        String[] modulePaths = FileUtil.getSummaryModule(project.getBasePath());
        if (null != modulePaths) {
            for (String modulePath : modulePaths) {
                FileUtil.deleteFiles(new File(modulePath));
            }
        }
        LocalFileSystem.getInstance().refresh(true);
    }

    private String getXmsPath() {
        return FileUtil.unifyToUnixFileSeparator(xmsPathTextField.getText());
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
        // analyse backgroup task canceled
        // so need to clear conversion toolWindow cache
        ConversionCacheService.getInstance().clearConversions(project.getBasePath());
        SummaryCacheService.getInstance().clearAnalyseResultCache4ConversionToolWindow(project.getBasePath());
        // and need to clear summary toolWindow cache
        SummaryCacheService.getInstance().clearAnalyseResultCache4SummaryResult(project.getBasePath());
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
        public CopyAction() {
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

    public class ShowLogDialogAction extends DialogWrapperAction {

        private static final long serialVersionUID = -5089548625097162040L;

        ShowLogDialogAction() {
            super(HmsConvertorBundle.message("show_log_s"));
        }

        @Override
        protected void doAction(ActionEvent e) {
            openLogDirectory();
        }
    }
}