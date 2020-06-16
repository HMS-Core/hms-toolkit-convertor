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

package com.huawei.hms.convertor.idea.ui.recovery;

import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.ConversionHelpEnum;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.core.event.context.project.ProjectEvent;
import com.huawei.hms.convertor.core.project.backup.ProjectRecoveryParams;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.result.HmsConvertorToolWindow;
import com.huawei.hms.convertor.idea.util.GrsServiceProvider;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.IconUtil;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.EventService;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ImageLoader;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Recovery dialog
 *
 * @since 2019/12/6
 */
@Slf4j
public class RecoveryDialog extends DialogWrapper {
    private JPanel rootPanel;

    private JLabel descLabel;

    private JLabel backupPathLabel;

    private JTextField backupPathTextField;

    private JButton browseBackupPathButton;

    private JLabel backupPointLabel;

    private JComboBox backupComboBox;

    private JLabel recoveryPathLabel;

    private JTextField recoveryPathTextField;

    private JButton browseRecoveryPathButton;

    private Project project;

    private ConfigCacheService configCacheService;

    private String backupPath;

    private String inspectPath;

    public RecoveryDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        if (null == project) {
            return;
        }
        configCacheService = ConfigCacheService.getInstance();
        backupPath =
            configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.BACK_PATH, String.class, "");
        inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");

        init();
    }

    private static boolean checkArchive(String fileName, String inspectFolder) {
        String regexOriginalArchive = "^" + inspectFolder + "(.)(\\d+)(.zip)";
        String regexProcessArchive = "^" + inspectFolder + "(.)(\\d+)(.)(\\D+)(.G).(H.process_)(\\d+)(%.zip)";
        if (fileName.matches(regexOriginalArchive) || fileName.matches(regexProcessArchive)) {
            return true;
        }
        return false;
    }

    @Override
    public void init() {
        super.init();
        setTitle(Constant.PLUGIN_NAME);
        getWindow().setIconImage(ImageLoader.loadFromResource("/icons/convertor.png"));

        descLabel.setText(HmsConvertorBundle.message("recovery_desc"));
        backupPathLabel.setText(HmsConvertorBundle.message("archive_path"));
        browseBackupPathButton.setText(HmsConvertorBundle.message("browse_b"));
        backupPointLabel.setText(HmsConvertorBundle.message("archive_point"));
        recoveryPathLabel.setText(HmsConvertorBundle.message("recovery_path"));
        browseRecoveryPathButton.setText(HmsConvertorBundle.message("browse_r"));
        backupPathTextField.setEditable(false);
        recoveryPathTextField.setEditable(false);
        browseBackupPathButton.setMnemonic(KeyEvent.VK_B);
        browseRecoveryPathButton.setMnemonic(KeyEvent.VK_R);
        initData();
        addListener();
    }

    private void initData() {
        setBackupPath(backupPath);
        setRecoveryPath(inspectPath);

        if (!StringUtil.isEmpty(backupPath) && new File(backupPath).exists()) {
            setBackupPoints(backupPath, inspectPath);
        }
        log.info("initData: backupPath = {}, recovery path = {}", backupPath, inspectPath);
    }

    private void addListener() {
        browseBackupPathButton.addActionListener(actionEvent -> {
            String backup = browseSingleFolder(HmsConvertorBundle.message("select_backup_path"));
            if (StringUtil.isEmpty(backup)) {
                return;
            }

            setBackupPath(backup);
            setBackupPoints(backup, inspectPath);
        });

        browseRecoveryPathButton.addActionListener(actionEvent -> {
            String recoveryPath = browseSingleFolder(HmsConvertorBundle.message("select_recovery_path"));
            if (StringUtil.isEmpty(recoveryPath)) {
                return;
            }

            setRecoveryPath(recoveryPath);
        });
    }

    @Override
    public Action[] createActions() {
        return new Action[] {getCancelAction(), getOKAction(), getHelpAction()};
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
        // bi report action: helpClick.
        BIReportService.getInstance().traceHelpClick(project.getBasePath(), ConversionHelpEnum.RESTORE_PROJECT);

        String allianceDomain = GrsServiceProvider.getGrsAllianceDomain();
        ApplicationManager.getApplication().invokeLater(() -> {
            BrowserUtil.browse(allianceDomain + HmsConvertorBundle.message("restore_url"));
        }, ModalityState.any());
    }

    @Override
    public Action getOKAction() {
        Action okAction = super.getOKAction();
        okAction.putValue(Action.NAME, HmsConvertorBundle.message("confirm_o"));
        okAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
        return okAction;
    }

    @Override
    public Action getCancelAction() {
        Action cancelAction = super.getCancelAction();
        cancelAction.putValue(Action.NAME, HmsConvertorBundle.message("cancel_c"));
        cancelAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        return cancelAction;
    }

    @Override
    public void doOKAction() {
        final ValidationInfo validationInfo = doValidate();
        if (null != validationInfo) {
            return;
        }

        final int toRecovery = Messages.showYesNoDialog(project,
            HmsConvertorBundle.message4Param("confirm_to_recovery", getSelectedBackupPoint()), Constant.PLUGIN_NAME,
            IconUtil.NOTICE);
        if (toRecovery != Messages.YES) {
            // bi report action: trace cancel operation.
            BIReportService.getInstance()
                .traceCancelListener(project.getBasePath(), CancelableViewEnum.RESTORE_PROJECT_CONFIRM);
            return;
        }

        HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
            hmsConvertorToolWindow.getSummaryToolWindow().asyncClearData();
            hmsConvertorToolWindow.getSourceConvertorToolWindow().asyncClearData();
            hmsConvertorToolWindow.getXmsDiffWindow().refreshData(null);
        });

        final Task task = new Task.Backgroundable(project, Constant.PLUGIN_NAME, true, PerformInBackgroundOption.DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                recoveryProject(indicator);
            }
        };
        task.queue();
        super.doOKAction();
    }

    private void recoveryProject(ProgressIndicator indicator) {
        if (indicator == null) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("recovery_error"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        indicator.setIndeterminate(true);
        indicator.setText("Recovering project...");

        Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
            ProjectRecoveryParams params = new ProjectRecoveryParams(project.getBasePath(), getBackupPath(),
                getSelectedBackupPoint(), getRecoveryPath());
            Result result = EventService.getInstance()
                .submitProjectEvent(ProjectEvent.<ProjectRecoveryParams, Result> of(project.getBasePath(),
                    EventType.RECOVERY_EVENT, params, (message) -> {
                        semaphore.release();
                        if (message.isOk()) {
                            LocalFileSystem.getInstance().refresh(false);
                            loadConversion();
                            final String successMessage =
                                HmsConvertorBundle.message4Param("recovery_success", getSelectedBackupPoint());
                            BalloonNotifications.showSuccessNotification(successMessage, project, Constant.PLUGIN_NAME,
                                true);
                        } else {
                            log.error("Recovery failed: {}", message.getMessage());
                            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("recovery_error"),
                                project, Constant.PLUGIN_NAME, true);
                        }
                    }));
            if (!result.isOk()) {
                BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("recovery_error"), project,
                    Constant.PLUGIN_NAME, true);
            }
            semaphore.acquire();
        } catch (InterruptedException e) {
            if (semaphore.availablePermits() == 0) {
                semaphore.release();
            }
            log.error(e.getMessage(), e);
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("recovery_error"), project,
                Constant.PLUGIN_NAME, true);
        }
    }

    private void loadConversion() {
        ApplicationManager.getApplication().invokeLater(() -> {
            final HmsConvertorToolWindow hmsConvertorToolWindow =
                HmsConvertorUtil.getHmsConvertorToolWindow(project).get();
            if (getSelectedBackupPoint().contains("process_")) {
                hmsConvertorToolWindow.getSummaryToolWindow().loadLastConversion();
                hmsConvertorToolWindow.getSourceConvertorToolWindow().loadLastConversion();
                hmsConvertorToolWindow.getXmsDiffWindow().loadXmsDiff();
            } else {
                hmsConvertorToolWindow.getSummaryToolWindow().asyncClearData();
                hmsConvertorToolWindow.getSourceConvertorToolWindow().asyncClearData();
                hmsConvertorToolWindow.getXmsDiffWindow().refreshData(null);
            }
        }, ModalityState.defaultModalityState());
    }

    @Nullable
    @Override
    public ValidationInfo doValidate() {
        if (StringUtil.isEmpty(getBackupPath())) {
            return new ValidationInfo(HmsConvertorBundle.message("no_backup_path"));
        }

        if (StringUtil.isEmpty(getSelectedBackupPoint())) {
            return new ValidationInfo(HmsConvertorBundle.message("no_backup_point"));
        }

        if (StringUtil.isEmpty(getRecoveryPath())) {
            return new ValidationInfo(HmsConvertorBundle.message("no_recovery_path"));
        }

        return null;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    private String browseSingleFolder(String description) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        descriptor.setTitle(Constant.PLUGIN_NAME);
        descriptor.setDescription(description);
        descriptor.setShowFileSystemRoots(true);

        VirtualFile singleFolder = FileChooser.chooseFile(descriptor, project, null);
        if (null == singleFolder) {
            return "";
        }
        return singleFolder.getPath().replace("\\", "/");
    }

    private String getBackupPath() {
        return backupPathTextField.getText();
    }

    private void setBackupPath(String backupPath) {
        backupPathTextField.setText(backupPath);
    }

    private String getSelectedBackupPoint() {
        return String.valueOf(backupComboBox.getSelectedItem());
    }

    private void setBackupPoints(String backupPath, String inspectPath) {
        backupComboBox.removeAllItems();

        Set<String> archiveFileNameSet = new HashSet<>();

        File backupDir = new File(backupPath);
        File[] backupPoints = backupDir.listFiles();
        final String inspectFolder = inspectPath.substring(inspectPath.lastIndexOf(Constant.SEPARATOR) + 1);
        if (null == backupPoints) {
            log.error("backupPoints is null");
            return;
        }
        for (File backupPoint : backupPoints) {
            String archiveFileName = backupPoint.getName();
            if (checkArchive(archiveFileName, inspectFolder)) {
                archiveFileNameSet.add(archiveFileName);
            }
        }

        TreeSet<String> processFiles = new TreeSet<>(Comparator.reverseOrder());
        TreeSet<String> originFiles = new TreeSet<>(Comparator.reverseOrder());
        archiveFileNameSet.forEach(file -> {
            if (file.contains(".G2H.process_") || file.contains(".G&H.process_")) {
                processFiles.add(file);
            } else {
                originFiles.add(file);
            }
        });
        processFiles.forEach(item -> backupComboBox.addItem(item));
        originFiles.forEach(item -> backupComboBox.addItem(item));
    }

    private String getRecoveryPath() {
        return recoveryPathTextField.getText();
    }

    private void setRecoveryPath(String recoveryPath) {
        recoveryPathTextField.setText(recoveryPath);
    }

    @Override
    public void doCancelAction() {
        // bi trace cancel operation: restore project view.
        BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.RESTORE_PROJECT);
        super.doCancelAction();
    }

}
