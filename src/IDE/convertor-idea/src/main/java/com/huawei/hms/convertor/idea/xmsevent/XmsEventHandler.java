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

package com.huawei.hms.convertor.idea.xmsevent;

import static com.huawei.hms.convertor.util.HmsConvertorUtil.parseGradle;

import com.huawei.generator.g2x.po.summary.Diff;
import com.huawei.generator.g2x.processor.GenerateSummary;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.g2x.processor.ProcessorUtils;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.diff.XmsDiff;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.ClientUtil;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.idea.util.ToolWindowUtil;
import com.huawei.hms.convertor.idea.util.UiUtil;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.XmsGenerateService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Xms event handler
 *
 * @since 2020-03-09
 */
@Slf4j
public class XmsEventHandler {
    private Project project;

    private String projectBasePath;

    private String pluginJarPath;

    private Map<String, Set<String>> allKit2Dependency;

    private Map<String, String> kitMap;

    private String xmsTempPath;

    private String xmsBackupFolder;

    public XmsEventHandler(Project projectIn, String pluginJarPathIn) {
        project = projectIn;
        projectBasePath = projectIn.getBasePath();
        pluginJarPath = pluginJarPathIn;
        String configFilePath = ClientUtil.getPluginPackagePath().get() + "/lib/config/"
            + ProjectConstants.Mapping.ADD_HMS_GRADLE_JSON_FILE;
        try {
            allKit2Dependency = parseGradle(configFilePath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Handle event
     *
     * @param event Event Object
     */
    public void handleEvent(XmsEvent event) {
        kitMap = event.getKitMap();
        log.info("handle event");
        getBackupPath();
        log.info("backup path {}", xmsTempPath);
        if (allKit2Dependency == null || allKit2Dependency.isEmpty() || kitMap.isEmpty()) {
            log.error("Empty dependency");
            return;
        }
        boolean convertedByOldSetting = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.CONVERTED_BY_OLD_SETTING, boolean.class, false);
        if (convertedByOldSetting) {
            UiUtil.setStatusBarInfo(project, "Begin regenerate xms adapter with old project...");
            xmsGenerateWithOldProject();
            ConfigCacheService.getInstance()
                .deleteProjectConfig(project.getBasePath(), ConfigKeyConstants.CONVERTED_BY_OLD_SETTING);
        } else {
            UiUtil.setStatusBarInfo(project, "Begin regenerate xms adapter...");
            xmsGenerate();
        }

        UiUtil.setStatusBarInfo(project, "finish regenerate xms adapter");
    }

    private boolean moveOldXmsToBackup(String sourcePath, String sourcePath2, boolean useOnlyG) {
        log.info("xmsTempPath {}", xmsTempPath);
        File xmsadapterTemp = new File(xmsTempPath);
        if (xmsadapterTemp.exists()) {
            com.intellij.openapi.util.io.FileUtil.delete(xmsadapterTemp);
        }
        try {
            if (useOnlyG) {
                String sourceGhPath;
                String sourceGPath;
                if (sourcePath.contains("xmsgh")) {
                    sourceGhPath = sourcePath;
                    sourceGPath = sourcePath2;
                } else {
                    sourceGhPath = sourcePath2;
                    sourceGPath = sourcePath;
                }
                log.info("fromPath {} toPath {}", sourceGPath, xmsTempPath + "/src/xmsg/java/org/xmss");
                com.intellij.openapi.util.io.FileUtil.copyDir(new File(sourceGPath),
                    new File(xmsTempPath + "/src/xmsg/java/org/xms"));
                log.info("fromPath {} toPath {}", sourceGhPath, xmsTempPath + "/src/xmsgh/java/org/xms");
                com.intellij.openapi.util.io.FileUtil.copyDir(new File(sourceGhPath),
                    new File(xmsTempPath + "/src/xmsgh/java/org/xms"));
                com.intellij.openapi.util.io.FileUtil
                    .delete(new File(sourcePath.substring(0, sourcePath.length() - "/java/org/xms".length())));
                com.intellij.openapi.util.io.FileUtil
                    .delete(new File(sourcePath2.substring(0, sourcePath2.length() - "/java/org/xms".length())));
            } else {
                File xmsTempFile = new File(xmsTempPath + "/src/main/org");
                log.info("fromPath {} toPath {}", sourcePath, xmsTempPath + "/src/main/org");
                com.intellij.openapi.util.io.FileUtil.copyDir(new File(sourcePath), xmsTempFile);
                com.intellij.openapi.util.io.FileUtil
                    .delete(new File(sourcePath.substring(0, sourcePath.length() - 4)));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void xmsGenerateWithOldProject() {
        log.info("Begin regenerate xms adapter with old project...");
        List<String> gAndHXmsPaths = FileUtil.getXmsPaths(project.getBasePath(), false);
        List<String> multiApkXmsPaths = FileUtil.getXmsPaths(project.getBasePath(), true);

        if (gAndHXmsPaths.size() > 1 && multiApkXmsPaths.size() > 2) {
            showWarningDialog(HmsConvertorBundle.message("multi_xms_adapter_notice"), Constant.PLUGIN_NAME);
            return;
        }

        Optional<GenerateSummary> generateSummary = generateXmsCode(gAndHXmsPaths, multiApkXmsPaths);
        if (!generateSummary.isPresent()) {
            log.error("XMS adapter regenerated failed");
            return;
        }
        if (generateSummary.get().result.getKey() == 0) {
            List<GeneratorStrategyKind> strategy =
                XmsGenerateService.inferStrategy(pluginJarPath, projectBasePath + XmsConstants.XMS_ADAPTER).strategy;
            boolean hmsFirst = false;
            for (GeneratorStrategyKind s : strategy) {
                if (GeneratorStrategyKind.HOrG.equals(s)) {
                    hmsFirst = true;
                    break;
                }
            }
            ConfigCacheService.getInstance()
                .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST, hmsFirst);
            if (gAndHXmsPaths.size() == 1) {
                ConfigCacheService.getInstance()
                    .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK, false);
            } else {
                ConfigCacheService.getInstance()
                    .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK, true);
            }
            showDiff(generateSummary.get().getDiff());
        } else {
            log.error("XMS adapter regenerated failed {}", generateSummary.get().result.getMessage());
            restoreXms(gAndHXmsPaths, multiApkXmsPaths);
        }
        log.info("End generate xms task");
    }

    private Optional<GenerateSummary> generateXmsCode(List<String> gAndHXmsPaths, List<String> multiApkXmsPaths) {
        // gAndH
        log.info(
            "generateXmsCode pluginJarPath {}, xmsTempPath {}, projectBasePath {}, kitMapSize {},"
                + " gAndHXmsPathsSize{}, multiApkXmsPathsSize {}",
            pluginJarPath, xmsTempPath, project.getBasePath(), kitMap.size(), gAndHXmsPaths.size(),
            multiApkXmsPaths.size());
        if (gAndHXmsPaths.size() == 1) {
            // move old xms
            if (!moveOldXmsToBackup(gAndHXmsPaths.get(0), null, false)) {
                log.error("Move xms failed");
                return Optional.empty();
            }
            // generate
            ProcessorUtils processorUtils = new ProcessorUtils.Builder().setPluginPath(pluginJarPath)
                .setOldPath(xmsTempPath)
                .setNewPath(project.getBasePath())
                .setKitMap(kitMap)
                .setAllDepMap(allKit2Dependency)
                .setUseOnlyG(false)
                .setThirdSDK(false)
                .build();
            return Optional.of(XmsGenerateService.createWithoutFirstStrategy(processorUtils));
        } else if (multiApkXmsPaths.size() == 2) { // multi Apk
            // move old xms
            if (!moveOldXmsToBackup(multiApkXmsPaths.get(0), multiApkXmsPaths.get(1), true)) {
                log.error("Move xms failed");
                return Optional.empty();
            }
            // generate
            ProcessorUtils processorUtils = new ProcessorUtils.Builder().setPluginPath(pluginJarPath)
                .setOldPath(xmsTempPath)
                .setNewPath(project.getBasePath())
                .setKitMap(kitMap)
                .setAllDepMap(allKit2Dependency)
                .setUseOnlyG(false)
                .setThirdSDK(true)
                .build();
            return Optional.of(XmsGenerateService.createWithoutFirstStrategy(processorUtils));
        } else {
            return Optional.empty();
        }
    }

    private void restoreXms(List<String> gAndHXmsPaths, List<String> multiApkXmsPaths) {
        if (gAndHXmsPaths.size() == 1) {
            String xmsPath = gAndHXmsPaths.get(0);
            try {
                com.intellij.openapi.util.io.FileUtil.copyDir(new File(xmsTempPath + "/src/main/org/xms"),
                    new File(xmsPath));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            String xmsghPath;
            String xmsgPath;
            if (multiApkXmsPaths.get(0).contains("xmsgh")) {
                xmsghPath = multiApkXmsPaths.get(0);
                xmsgPath = multiApkXmsPaths.get(1);
            } else {
                xmsghPath = multiApkXmsPaths.get(1);
                xmsgPath = multiApkXmsPaths.get(0);
            }
            try {
                com.intellij.openapi.util.io.FileUtil.copyDir(new File(xmsTempPath + "/src/xmsgh/java/org/xms"),
                    new File(xmsghPath));
                com.intellij.openapi.util.io.FileUtil.copyDir(new File(xmsTempPath + "/src/xmsg/java/org/xms"),
                    new File(xmsgPath));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        com.intellij.openapi.util.io.FileUtil.delete(new File(xmsTempPath));
    }

    private void xmsGenerate() {
        log.info("Begin supplement xms");
        boolean hmsFirst = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST, boolean.class, false);
        boolean multiApkMode = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK, boolean.class, false);
        String type = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_TYPE, String.class, "");
        List<GeneratorStrategyKind> kindList = new ArrayList<>();

        if (!moveXmsToBackup()) {
            log.error("Move xms failed");
            return;
        }
        if (hmsFirst) {
            kindList.add(GeneratorStrategyKind.HOrG);
        } else {
            kindList.add(GeneratorStrategyKind.GOrH);
        }

        if (multiApkMode) {
            kindList.add(GeneratorStrategyKind.G);
        }
        log.info(
            "pluginJarPath {}, xmsTempPath {}, projectBasePath {}, MultiApkMode {}, hmsFirst {}, kitMapSize {},"
                + " kindListSize {}, type {}",
            pluginJarPath, xmsTempPath, project.getBasePath(), multiApkMode, hmsFirst, kitMap.size(), kindList.size(),
            type);
        ProcessorUtils processorUtils = new ProcessorUtils.Builder().setPluginPath(pluginJarPath)
            .setBackPath(xmsTempPath)
            .setTargetPath(project.getBasePath())
            .setKitMap(kitMap)
            .setAllDepMap(allKit2Dependency)
            .setStrategyKindList(kindList)
            .setThirdSDK(ProjectConstants.Type.SDK.equals(type))
            .build();
        GenerateSummary generateSummary = XmsGenerateService.create(processorUtils);

        if (generateSummary.result.getKey() == 0) {
            showDiff(generateSummary.getDiff());
        } else {
            log.info("Generate xms failed {}", generateSummary.result.getMessage());
            try {
                com.intellij.openapi.util.io.FileUtil.copyDir(new File(xmsTempPath), new File(projectBasePath));
                com.intellij.openapi.util.io.FileUtil.delete(new File(xmsTempPath));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("End generate xms task");
        LocalFileSystem.getInstance().refresh(true);
    }

    private boolean moveXmsToBackup() {
        File xmsadapterTemp = new File(xmsTempPath);
        log.info("xmsTempPath {}", xmsTempPath);
        if (xmsadapterTemp.exists()) {
            com.intellij.openapi.util.io.FileUtil.delete(xmsadapterTemp);
        }
        try {
            com.intellij.openapi.util.io.FileUtil.copyDir(new File(projectBasePath + XmsConstants.XMS_ADAPTER),
                xmsadapterTemp);
            com.intellij.openapi.util.io.FileUtil.delete(new File(projectBasePath + XmsConstants.XMS_ADAPTER));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void showWarningDialog(String message, String title) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            Messages.showWarningDialog(project, message, title);
        }, ModalityState.any());
    }

    private void showDiff(Diff diff) {
        if (diff == null) {
            log.error("Empty diff");
            return;
        }
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ToolWindow toolWindow = ToolWindowUtil.getToolWindow(project, UIConstants.ToolWindow.TOOL_WINDOW_ID);
            if (null == toolWindow) {
                log.warn("Can not get HMS convertor tool window!");
                BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_tool_window"), project,
                    Constant.PLUGIN_NAME, true);
                return;
            }
            ToolWindowUtil.showWindow(toolWindow);
            HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
                hmsConvertorToolWindow.getXmsDiffWindow().refreshData(new XmsDiff(diff));
                hmsConvertorToolWindow.showTabbedPane(UIConstants.ToolWindow.TAB_XMSDIFF_INDEX);
            });
            Messages.showInfoMessage(project, HmsConvertorBundle.message("xms_regenerate_notice"),
                Constant.PLUGIN_NAME);
        }, ModalityState.any());
    }

    private boolean getCacheBackupPath() {
        String timestamp = LocalDateTime.now().format(Constant.BASIC_ISO_DATETIME);
        xmsTempPath = "";
        xmsBackupFolder = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_FOLDER, String.class, "")
            + ProjectConstants.Common.BACKUP_SUFFIX + "." + timestamp;
        String backupPath = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.BACK_PATH, String.class, "");
        if (StringUtil.isEmpty(backupPath)) {
            return false;
        }
        backupPath = Paths.get(backupPath, xmsBackupFolder).toString() + XmsConstants.XMS_ADAPTER;
        File backupDir = new File(backupPath);
        if (backupDir.exists() || backupDir.mkdirs()) {
            xmsTempPath = backupPath;
            return true;
        }
        return false;
    }

    private String browseSingleFolder(String description, String toSelectPath) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        descriptor.setTitle(Constant.PLUGIN_NAME);
        descriptor.setDescription(description);
        descriptor.setShowFileSystemRoots(true);

        VirtualFile toSelectFile = null;
        if (!com.intellij.openapi.util.text.StringUtil.isEmpty(toSelectPath)) {
            toSelectFile = LocalFileSystem.getInstance().findFileByPath(toSelectPath);
        }
        VirtualFile singleFolder = FileChooser.chooseFile(descriptor, project, toSelectFile);
        if (null == singleFolder) {
            return "";
        }
        return singleFolder.getPath().replace("\\", "/");
    }

    private void getBackupPath() {
        if (getCacheBackupPath()) {
            log.info("Backup path {}", xmsTempPath);
            return;
        }
        ApplicationManager.getApplication().invokeAndWait(() -> {
            Messages.showDialog(project, HmsConvertorBundle.message("set_backup_for_adapter_notice"),
                Constant.PLUGIN_NAME, new String[] {"OK"}, Messages.YES, Messages.getInformationIcon());
            int count = 0;
            while (StringUtil.isEmpty(xmsTempPath) && count < 5) {
                count++;
                String backupPath =
                    browseSingleFolder(HmsConvertorBundle.message("select_backup_path"), project.getBasePath());
                if (StringUtil.isEmpty(backupPath)) {
                    Messages.showDialog(project, HmsConvertorBundle.message("no_backup_path"), Constant.PLUGIN_NAME,
                        new String[] {"OK"}, Messages.YES, Messages.getInformationIcon());
                } else if (backupPath.contains(project.getBasePath())) {
                    Messages.showDialog(project, HmsConvertorBundle.message("backup_in_project"), Constant.PLUGIN_NAME,
                        new String[] {"OK"}, Messages.YES, Messages.getInformationIcon());
                } else {
                    String backupPathTemp =
                        Paths.get(backupPath, xmsBackupFolder).toString() + XmsConstants.XMS_ADAPTER;
                    if (new File(backupPathTemp).mkdirs()) {
                        ConfigCacheService.getInstance()
                            .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.BACK_PATH, backupPath);
                        xmsTempPath = backupPathTemp;
                        return;
                    } else {
                        Messages.showDialog(project, HmsConvertorBundle.message("invalid_backup_path"),
                            Constant.PLUGIN_NAME, new String[] {"OK"}, Messages.YES, Messages.getInformationIcon());
                    }
                }
            }
        }, ModalityState.any());
    }
}
