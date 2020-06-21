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

import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.OperationViewEnum;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotParams;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.core.project.base.FileService;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.HmsConvertorState;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.result.HmsConvertorToolWindow;
import com.huawei.hms.convertor.idea.util.ClientUtil;
import com.huawei.hms.convertor.idea.util.GrsServiceProvider;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.TimeUtil;
import com.huawei.hms.convertor.idea.util.ToolWindowUtil;
import com.huawei.hms.convertor.idea.util.VersionUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.ConversionCacheService;
import com.huawei.hms.convertor.openapi.FixbotAnalyzeService;
import com.huawei.hms.convertor.openapi.ProjectArchiveService;
import com.huawei.hms.convertor.openapi.XmsGenerateService;
import com.huawei.hms.convertor.util.Constant;

import com.alibaba.fastjson.JSONException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.ToolWindowImpl;
import com.intellij.util.TimeoutUtil;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * HMS convertor starter
 *
 * @since 2019-06-10
 */
public final class HmsConvertorStarter {
    private static final Logger LOG = LoggerFactory.getLogger(HmsConvertorStarter.class);

    private static final String WISEHUB_AUTO_FILE_NAME = "/wisehub-auto.json";

    private static final String WISEHUB_MANUAL_FILE_NAME = "/wisehub-manual.json";

    private static final int ASYNC_START_SLEEPTIME = 10;

    private Project project;

    private RoutePolicy routePolicy;

    private ConfigCacheService configCacheService;

    private String inspectPath;

    private String inspectFolder;

    private int fontSize;

    public HmsConvertorStarter(@NotNull Project project, RoutePolicy routePolicy, int fontSize) {
        this.project = project;
        this.routePolicy = routePolicy;
        this.fontSize = fontSize;

        configCacheService = ConfigCacheService.getInstance();
        inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        inspectFolder = inspectPath.substring(inspectPath.lastIndexOf(Constant.SEPARATOR) + 1);
    }

    /**
     * Start an analysis task.
     */
    public void start() {
        ToolWindow toolWindow = ToolWindowUtil.getToolWindow(project, UIConstants.ToolWindow.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            LOG.warn("Can not get HMS convertor tool window!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_tool_window"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        // Make sure the tool window is initialized.
        if (toolWindow instanceof ToolWindowImpl) {
            ((ToolWindowImpl) toolWindow).ensureContentInitialized();
        }
        ToolWindowUtil.showWindow(toolWindow);

        // Start an asynchronous analysis task.
        final Task task = new Task.Backgroundable(project, Constant.PLUGIN_NAME, true, PerformInBackgroundOption.DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                asyncStart(indicator);
            }
        };
        task.queue();
    }

    private void asyncStart(ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        if (VersionUtil.getIdeBaselineVersion() < VersionUtil.BASELINE_VERSION_191) {
            TimeoutUtil.sleep(ASYNC_START_SLEEPTIME);
        }
        LOG.info("Start analysis, Name = {}, projectPath = {}, inspectPath = {}, inspectFolder = {}, routePolicy = {}",
            project.getName(), project.getBasePath(), inspectPath, inspectFolder, routePolicy);

        Optional<HmsConvertorToolWindow> hmsConvertorToolWindow = HmsConvertorUtil.getHmsConvertorToolWindow(project);
        if (!hmsConvertorToolWindow.isPresent()) {
            LOG.warn("HMS convertor tool window is null!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_tool_window"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        try {
            HmsConvertorState.set(project, HmsConvertorState.RUNNING);
            indicator.setText("Analyzing " + inspectFolder + "... Please wait.");
            indicator.setText2(HmsConvertorBundle.message("indicater_analyze_notice2"));

            TimeUtil.getInstance().getStartTime();
            startEngineAnalysis(indicator, hmsConvertorToolWindow.get());
            LOG.info("Analyze finished! Elapsed time: {}", TimeUtil.getInstance().getElapsedTime());

            // BI report action: trace source info
            BIReportService.getInstance().traceSourceInfo(project.getBasePath());

            // BI report action: trace analyze time cost, analyze ends.
            Long timeCost =
                System.currentTimeMillis() - BIInfoManager.getInstance().getAnalyzeBeginTime(project.getBasePath());
            BIReportService.getInstance()
                .traceTimeAnalyzeCost(project.getBasePath(), OperationViewEnum.BASE_SETTING_VIEW,
                    String.valueOf(timeCost), BIReportService.getInstance().getJvmXmx(project.getBasePath()));
            BIInfoManager.getInstance().clearData(project.getBasePath());
        } catch (ProcessCanceledException ignore) {
            ConversionCacheService.getInstance().clearConversions(project.getBasePath());
            SummaryCacheManager.getInstance().clearKit2Methods(project.getBasePath());
            LOG.warn(ignore.getMessage(), ignore);

            // bi report action: trace cancel operation.
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.FIRST_ANALYZE);
        } catch (NoSuchFileException | JSONException e) {
            LOG.warn("file not found error");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("analyze_error"), project,
                Constant.PLUGIN_NAME, true);
        } finally {
            HmsConvertorState.set(project, HmsConvertorState.IDLE);
        }
    }

    private void startEngineAnalysis(@NotNull ProgressIndicator indicator, HmsConvertorToolWindow toolWindow)
        throws NoSuchFileException {
        String repoID = inspectFolder + "." + inspectPath.hashCode();
        LOG.info("repoID = {}", repoID);
        ConfigCacheService.getInstance().updateProjectConfig(project.getBasePath(), ConfigKeyConstants.REPO_ID, repoID);
        ConfigCacheService.getInstance()
            .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_FOLDER, inspectFolder);
        ConfigCacheService.getInstance()
            .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_ID, repoID);
        ConfigCacheService.getInstance()
            .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH, inspectPath);

        // Clear data of tool windows.
        toolWindow.getSummaryToolWindow().asyncClearData();
        toolWindow.getSourceConvertorToolWindow().asyncClearData();
        toolWindow.getXmsDiffWindow().refreshData(null);

        if (!ProjectArchiveService.backupProject(project.getBasePath())) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("backup_failed"), project,
                Constant.PLUGIN_NAME, true);
        }
        if (!ClientUtil.getPluginPackagePath().isPresent()) {
            throw new NoSuchFileException(HmsConvertorBundle.message("no_engine_found"));
        }
        String pluginPackagePath = ClientUtil.getPluginPackagePath().get();
        pluginPackagePath = pluginPackagePath.replace("\\", "/");

        // Check whether the xms configuration file needs to be generated.
        generateXmsConfigFiles(pluginPackagePath);

        if (!inspectSource(pluginPackagePath, configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.COMMENT, Boolean.class, false))) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("engine_analysis_error"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        while (true) {
            indicator.checkCanceled();
            break;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            PolicySettingDialog policySettingDialog = new PolicySettingDialog(project);
            policySettingDialog.show();
        }, ModalityState.defaultModalityState());
    }

    public boolean inspectSource(String pluginPackagePath, boolean isCommentEnable) {
        String repoID = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_ID, String.class, "");
        ServiceLoader.load(FileService.class, HmsConvertorStarter.class.getClassLoader())
            .iterator()
            .next()
            .preProcess(repoID);

        // Configure Engine Startup Parameters.
        FixbotParams fixbotParams = getArguments(pluginPackagePath, repoID, inspectPath);

        LOG.info("generate defect files begin");
        if (FixbotAnalyzeService.getInstance().executeFixbot(fixbotParams) != 0) {
            return false;
        }
        LOG.info("generate defect files end");
        LocalFileSystem.getInstance().refresh(false);

        if (isCommentEnable) {
            repoID += ProjectConstants.Common.COMMENT_SUFFIX;
            ConfigCacheService.getInstance()
                .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.REPO_ID, repoID);
        }
        String type = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_TYPE, String.class, "");
        LOG.info("repoID = {}, commentEnable = {}, type = {}", repoID, isCommentEnable, type);

        // Parse engine result file.
        FixbotAnalyzeService.getInstance()
            .parseFixbotResult(project.getBasePath(), GrsServiceProvider.getGrsAllianceDomain(), routePolicy, type,
                fontSize);
        return true;
    }

    private void generateXmsConfigFiles(final String pluginPackagePath) {
        String pluginJarPath = System.getProperty(XmsConstants.KEY_XMS_JAR);
        String outPath = pluginPackagePath + "/lib/config";
        String wisehubAutoPath = outPath + WISEHUB_AUTO_FILE_NAME;
        String wisehubManualPath = outPath + WISEHUB_MANUAL_FILE_NAME;
        File wisehubAutoMapping = new File(wisehubAutoPath);
        File wisehubManualMapping = new File(wisehubManualPath);
        if (wisehubAutoMapping.exists() && wisehubManualMapping.exists()) {
            LOG.warn("XmsConfigFiles exist");
            return;
        }

        GeneratorResult generateXmsResult =
            XmsGenerateService.generateXmsConfig(pluginJarPath, outPath, Constant.PLUGIN_LOG_PATH);
        if (generateXmsResult.getKey() != 0) {
            LOG.warn("Error during generate xms config files, error type = {}", generateXmsResult.getMessage());
            BalloonNotifications.showWarnNotification(
                "Error during generate xms config files, error type = " + generateXmsResult.getMessage(), project,
                Constant.PLUGIN_NAME, true);
        }
    }

    private FixbotParams getArguments(String pluginPackagePath, String repoID, String repoPath) {
        FixbotParams fixbotParams = new FixbotParams();
        String pluginJarPath = pluginPackagePath + "/lib";
        String configPath = ClientUtil.getPluginPackagePath().get().replace("\\", "/") + "/lib/config";

        fixbotParams.setEnginePath(pluginJarPath);
        fixbotParams.initJvmOpt();
        BIReportService.getInstance().setJvmXmx(project.getBasePath(), fixbotParams.getJvmOpt());
        fixbotParams.setMappingPath(configPath);
        fixbotParams.setCacheDirectory(repoID);
        fixbotParams.setInspectPath(repoPath);
        fixbotParams.setFixPath((Constant.PLUGIN_CACHE_PATH + repoID).replace("\\", "/"));

        if (routePolicy.equals(RoutePolicy.G_AND_H)) {
            fixbotParams.setPolicy("wisehub");
        } else {
            fixbotParams.setPolicy("libadaption");
        }

        List<String> excludePaths = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.EXCLUDE_PATH, List.class, new ArrayList());
        excludePaths.remove("Common");
        List<String> arguments = new ArrayList<>();
        if (!excludePaths.isEmpty()) {
            for (String excludePath : excludePaths) {
                arguments.add(excludePath.replace("\\", "/"));
            }
        }

        List<String> xmsAdapterPathList = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.XMS_PATH, List.class, new ArrayList());
        if (!xmsAdapterPathList.isEmpty()) {
            for (String path : xmsAdapterPathList) {
                arguments.add(path.replace("\\", "/") + "/org/xms");
            }
        }

        List<String> xms4GAdapterPaths = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.XMS_MULTI_PATH, List.class, new ArrayList());
        if (!xms4GAdapterPaths.isEmpty()) {
            for (String path : xms4GAdapterPaths) {
                arguments.add(path.replace("\\", "/"));
            }
        }
        String xmsModulePath = configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.INSPECT_PATH, String.class, "");

        if (!xmsModulePath.isEmpty()) {
            File xmsFile = new File(xmsModulePath.replace("\\", "/") + "/xmsadapter");
            if (xmsFile.exists()) {
                arguments.add(xmsModulePath.replace("\\", "/") + "/xmsadapter/src");
                arguments.add(xmsModulePath.replace("\\", "/") + "/xmsadapter/build");
                arguments.add(xmsModulePath.replace("\\", "/") + "/xmsadapter/libs");
                if (routePolicy.equals(RoutePolicy.G_TO_H)) {
                    arguments.add(xmsModulePath.replace("\\", "/") + "/xmsadapter/build.gradle");
                }
            }
        }

        fixbotParams.setExcludedPaths(arguments);
        return fixbotParams;
    }
}
