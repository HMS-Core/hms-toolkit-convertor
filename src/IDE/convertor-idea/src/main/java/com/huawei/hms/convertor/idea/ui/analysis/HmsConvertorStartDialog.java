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

import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.ConversionHelpEnum;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.GrsServiceProvider;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.UIUtil;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListModel;

/**
 * HMS convertor start dialog
 *
 * @since 2019-06-27
 */
@Slf4j
public class HmsConvertorStartDialog extends DialogWrapper {
    private static final Pattern EXCLUDE_PATTERN = Pattern.compile("\\.idea|\\.gradle|build|\\.git|\\.svn|gradle");

    private JPanel rootPanel;

    private JTextField inspectPathTextField;

    private JButton browserInspectPathButton;

    private JList excludePathList;

    private JButton addButton;

    private JButton removeButton;

    private JLabel backupLabel;

    private JTextField backupPathTextField;

    private JButton browserBackupPathButton;

    private JLabel anDirLabel;

    private JLabel excDirLabel;

    private JLabel lineLabel;

    private JLabel step1Label;

    private JLabel step2Label;

    private JCheckBox commentCheckBox;

    private JLabel typeLable;

    private JRadioButton appRadio;

    private JRadioButton sdkRadio;

    private Project project;

    private String projectBasePath;

    private ConfigCacheService configCacheService;

    private String allianceDomain;

    public HmsConvertorStartDialog(@NotNull Project project, @NotNull String projectBasePath) {
        super(project);
        this.project = project;
        this.projectBasePath = projectBasePath;
        configCacheService = ConfigCacheService.getInstance();
        init();
    }

    @Override
    public void init() {
        super.init();
        setTitle(Constant.PLUGIN_NAME);
        getWindow().setIconImage(ImageLoader.loadFromResource(UIConstants.Dialog.WINDOW_ICON_RESOURCE));

        initComponents();
        initShowData();
        addListener();
    }

    @Override
    public Action[] createActions() {
        return new Action[] {getCancelAction(), getOKAction(), getHelpAction()};
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Override
    public Action getHelpAction() {
        Action helpAction = super.getHelpAction();
        helpAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
        helpAction.putValue(Action.NAME, HmsConvertorBundle.message("help"));
        return helpAction;
    }

    @Override
    public void doHelpAction() {
        // bi report action: click help link.
        BIReportService.getInstance().traceHelpClick(project.getBasePath(), ConversionHelpEnum.PATH_SETTING);

        ApplicationManager.getApplication().invokeLater(() -> {
            BrowserUtil.browse(allianceDomain + HmsConvertorBundle.message("directory_url"));
        }, ModalityState.any());
    }

    @Override
    public Action getCancelAction() {
        Action cancelAction = super.getCancelAction();
        cancelAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        cancelAction.putValue(Action.NAME, HmsConvertorBundle.message("cancel_c"));
        return cancelAction;
    }

    @Override
    public void doCancelAction() {
        // bi report action: trace cancel operation.
        BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.PATH_SETTING);
        super.doCancelAction();
    }

    @Override
    public Action getOKAction() {
        Action okAction = super.getOKAction();
        okAction.putValue(Action.NAME, HmsConvertorBundle.message("next_n"));
        okAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
        return okAction;
    }

    @Override
    public void doOKAction() {
        final ValidationInfo validationInfo = doValidate();
        if (validationInfo != null) {
            return;
        }

        boolean commentEnable = commentCheckBox.isSelected();
        if (null != configCacheService) {
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.COMMENT, commentEnable);
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.BACK_PATH,
                getBackupPath());
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
                getInspectPath());
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.EXCLUDE_PATH,
                getExcludePaths());
            String type = appRadio.isSelected() ? ProjectConstants.Type.APP : ProjectConstants.Type.SDK;
            configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_TYPE, type);
        }

        // Begin analysis task.
        HmsConvertorStarter starter =
            new HmsConvertorStarter(project, RoutePolicy.G_TO_H, UISettings.getInstance().getFontSize());
        Runnable runnable = () -> starter.start();

        // bi trace analyze time cost: analyze begins.
        BIInfoManager.getInstance().setAnalyzeBeginTime(project.getBasePath(), System.currentTimeMillis());
        ApplicationManager.getApplication().invokeAndWait(runnable);
        super.doOKAction();
    }

    @Nullable
    @Override
    public ValidationInfo doValidate() {
        if (StringUtil.isEmpty(getInspectPath())) {
            inspectPathTextField.requestFocus();
            return new ValidationInfo(HmsConvertorBundle.message("no_analyze_path"));
        }

        if (StringUtil.isEmpty(getBackupPath())) {
            backupPathTextField.requestFocus();
            return new ValidationInfo(HmsConvertorBundle.message("no_backup_path"));
        }

        if (getBackupPath().contains(getInspectPath() + Constant.UNIX_FILE_SEPARATOR)) {
            backupPathTextField.requestFocus();
            return new ValidationInfo(HmsConvertorBundle.message("backup_in_project"));
        }

        if (getBackupPath().equals(getInspectPath())) {
            backupPathTextField.requestFocus();
            return new ValidationInfo(HmsConvertorBundle.message("backup_in_project"));
        }

        return null;
    }

    private void initComponents() {
        anDirLabel.setText(HmsConvertorBundle.message("aly_dir"));
        excDirLabel.setText(HmsConvertorBundle.message("exc_dir"));
        typeLable.setText(HmsConvertorBundle.message("project_type"));
        appRadio.setText(HmsConvertorBundle.message("app"));
        appRadio.setSelected(true);
        sdkRadio.setText(HmsConvertorBundle.message("sdk"));
        ButtonGroup policyButtonGroup = new ButtonGroup();
        policyButtonGroup.add(appRadio);
        policyButtonGroup.add(sdkRadio);
        browserBackupPathButton.setText(HmsConvertorBundle.message("browse_b"));
        browserInspectPathButton.setText(HmsConvertorBundle.message("browse_s"));
        addButton.setText(HmsConvertorBundle.message("add_a"));
        removeButton.setText(HmsConvertorBundle.message("remove_r"));
        backupLabel.setText(HmsConvertorBundle.message("backup_dir"));
        commentCheckBox.setText(HmsConvertorBundle.message("comment_valid"));
        inspectPathTextField.setEditable(false);
        backupPathTextField.setEditable(false);
        step1Label.setIcon(IconUtil.RUNNING);
        step2Label.setIcon(IconUtil.WAIT);
        if (UIUtil.isUnderDarcula()) {
            lineLabel.setIcon(IconUtil.GUIDE_LINE);
        } else {
            lineLabel.setIcon(IconUtil.GUIDE_LINE_GRAY);
        }
        addButton.setMnemonic(KeyEvent.VK_A);
        removeButton.setMnemonic(KeyEvent.VK_R);
        browserInspectPathButton.setMnemonic(KeyEvent.VK_S);
        browserBackupPathButton.setMnemonic(KeyEvent.VK_B);

        allianceDomain = GrsServiceProvider.getGrsAllianceDomain();
        configCacheService.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.ALLIANCE_DOMAIN,
            allianceDomain);
    }

    private void initShowData() {
        String inspectPath;
        String backupPath;
        List<String> excludePaths;
        if ((configCacheService == null) || StringUtil.isEmpty(configCacheService
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH, String.class, ""))) {
            inspectPath = projectBasePath;
            backupPath = "";
            excludePaths = FileUtil.findFoldersByMask(EXCLUDE_PATTERN, inspectPath);
        } else {
            inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
                String.class, "");
            backupPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.BACK_PATH,
                String.class, "");
            excludePaths = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.EXCLUDE_PATH,
                List.class, new ArrayList<String>());
            mergeExcludePaths(excludePaths, inspectPath);
        }

        setInspectPath(inspectPath);
        setExcludePaths(excludePaths);
        setBackupPath(backupPath);

        log.info("initData: inspectPath: {}, backupPath: {}, excludePaths: {}", inspectPath, backupPath,
            String.join(",", excludePaths));
    }

    private void mergeExcludePaths(List<String> oldExcludePaths, String inspectPath) {
        List<String> newExcludePaths = FileUtil.findFoldersByMask(EXCLUDE_PATTERN, inspectPath);
        for (String path : newExcludePaths) {
            if (!oldExcludePaths.contains(path)) {
                oldExcludePaths.add(path);
            }
        }
    }

    private void addListener() {
        browserInspectPathButton.addActionListener(event -> {
            String inspectPath =
                browseSingleFolder(HmsConvertorBundle.message("select_analyze_path"), configCacheService
                    .getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH, String.class, ""));
            if (StringUtil.isEmpty(inspectPath)) {
                return;
            }

            setInspectPath(inspectPath);

            List<String> excludePaths = FileUtil.findFoldersByMask(EXCLUDE_PATTERN, inspectPath);
            setExcludePaths(excludePaths);
        });

        browserBackupPathButton.addActionListener(event -> {
            String backupPath = browseSingleFolder(HmsConvertorBundle.message("select_backup_path"), configCacheService
                .getProjectConfig(project.getBasePath(), ConfigKeyConstants.BACK_PATH, String.class, ""));
            if (StringUtil.isEmpty(backupPath)) {
                return;
            }

            setBackupPath(backupPath);
        });

        addButton.addActionListener(event -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createMultipleFoldersDescriptor();
            descriptor.setTitle(Constant.PLUGIN_NAME);
            descriptor.setDescription(HmsConvertorBundle.message("select_exclude_path"));
            descriptor.setShowFileSystemRoots(true);
            VirtualFile rootPath = LocalFileSystem.getInstance().findFileByPath(getInspectPath());
            descriptor.setRoots(rootPath);

            VirtualFile[] files = FileChooser.chooseFiles(descriptor, project, null);
            if (files.length == 0) {
                return;
            }

            List<String> excludePaths = getExcludePaths();
            for (VirtualFile file : files) {
                String excludePath = FileUtil.unifyToUnixFileSeparator(file.getPath());
                if (isNewItem(excludePath, excludePaths)) {
                    excludePaths.add(excludePath);
                }
            }

            setExcludePaths(excludePaths);
        });

        removeButton.addActionListener(event -> {
            List<String> removePaths = excludePathList.getSelectedValuesList();
            List<String> excludePaths = getExcludePaths();
            excludePaths.removeAll(removePaths);
            setExcludePaths(excludePaths);
        });
    }

    private String browseSingleFolder(String description, String toSelectPath) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        descriptor.setTitle(Constant.PLUGIN_NAME);
        descriptor.setDescription(description);
        descriptor.setShowFileSystemRoots(true);

        VirtualFile toSelectFile = null;
        if (!StringUtil.isEmpty(toSelectPath)) {
            toSelectFile = LocalFileSystem.getInstance().findFileByPath(toSelectPath);
        }
        VirtualFile singleFolder = FileChooser.chooseFile(descriptor, project, toSelectFile);
        if (singleFolder == null) {
            return "";
        }
        return FileUtil.unifyToUnixFileSeparator(singleFolder.getPath());
    }

    private String getInspectPath() {
        return inspectPathTextField.getText();
    }

    private void setInspectPath(String inspectPath) {
        inspectPathTextField.setText(inspectPath);
    }

    private String getBackupPath() {
        return backupPathTextField.getText();
    }

    private void setBackupPath(String backupPath) {
        backupPathTextField.setText(backupPath);
    }

    private List<String> getExcludePaths() {
        List<String> excludePaths = new ArrayList<>();

        ListModel<String> listModel = excludePathList.getModel();
        int len = listModel.getSize();
        for (int i = 0; i < len; i++) {
            excludePaths.add(listModel.getElementAt(i));
        }

        return excludePaths;
    }

    private void setExcludePaths(List<String> excludePaths) {
        excludePathList.setListData(excludePaths.toArray());
    }

    private <E> boolean isNewItem(E item, List<E> list) {
        for (E e : list) {
            if (item.equals(e)) {
                return false;
            }
        }

        return true;
    }
}
