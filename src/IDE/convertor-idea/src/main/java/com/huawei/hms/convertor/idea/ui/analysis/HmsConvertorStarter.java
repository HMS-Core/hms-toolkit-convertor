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
import com.huawei.hms.convertor.core.bi.enumration.OperationViewEnum;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.FixbotAnalysisParams;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotConstants;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotParams;
import com.huawei.hms.convertor.core.mapping.DependencyApiMetadataGenerator;
import com.huawei.hms.convertor.core.mapping.GradleMappingGenerator;
import com.huawei.hms.convertor.core.mapping.JavaMappingGenerator;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.FileService;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.diff.Strategy;
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
import com.huawei.hms.convertor.openapi.ProgressService;
import com.huawei.hms.convertor.openapi.ProjectArchiveService;
import com.huawei.hms.convertor.openapi.SummaryCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.alibaba.fastjson.JSONException;
import com.huawei.hms.convertor.utils.XMSUtils;
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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * HMS convertor starter
 *
 * @since 2019-06-10
 */
@Slf4j
public final class HmsConvertorStarter {
    private static final int ASYNC_START_SLEEP_MILLIS = 10;

    private static final int OOM_ERROR_CODE = 2;

    private Project project;

    private RoutePolicy routePolicy;

    private ConfigCacheService configCacheService;

    private String inspectPath;

    private String inspectFolder;

    private int fontSize;

    private int fixbotExitValue;

    private String projectType;

    public HmsConvertorStarter(@NotNull Project project, RoutePolicy routePolicy, int fontSize) {
        this.project = project;
        this.routePolicy = routePolicy;
        this.fontSize = fontSize;

        configCacheService = ConfigCacheService.getInstance();
        projectType = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_TYPE,
            String.class, "");
        inspectPath = configCacheService.getProjectConfig(project.getBasePath(), ConfigKeyConstants.INSPECT_PATH,
            String.class, "");
        inspectFolder = inspectPath.substring(inspectPath.lastIndexOf(Constant.UNIX_FILE_SEPARATOR_IN_CHAR) + 1);
    }

    /**
     * Start an analysis task.
     */
    public void start() {
        ToolWindow toolWindow = ToolWindowUtil.getToolWindow(project, UIConstants.ToolWindow.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            log.warn("Can not get HMS convertor tool window!");
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

    public boolean inspectSource(String pluginPackagePath, boolean isCommentEnable, ProgressIndicator indicator,
        Strategy strategy) {
        String repoID = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_ID, String.class, "");
        ServiceLoader.load(FileService.class, HmsConvertorStarter.class.getClassLoader())
            .iterator()
            .next()
            .preProcess(repoID);
        // Configure Engine Startup Parameters.
        FixbotAnalysisParams fixbotAnalysisParams = FixbotAnalysisParams.builder()
            .repoId(repoID)
            .pluginPackagePath(pluginPackagePath)
            .repoPath(inspectPath)
            .strategy(strategy)
            .projectBasePath(project.getBasePath())
            .routePolicy(routePolicy)
            .configCacheService(configCacheService)
            .projectType(projectType)
            .build();
        FixbotParams fixbotParams = FixbotAnalyzeService.getInstance().buildFixbotAnalysisParams(fixbotAnalysisParams);

        log.info("generate defect files begin");

        ProgressService progressService = new ProgressService();
        RefreshProgressTask refreshProgressTask =
            new RefreshProgressTask(progressService, indicator, project.getBasePath());

        fixbotExitValue =
            FixbotAnalyzeService.getInstance().executeFixbot(fixbotParams, progressService, project.getBasePath());
        if (fixbotExitValue != 0) {
            fixbotErrorProcessor();
            refreshProgressTask.shutdown();
            return false;
        }
        refreshProgressTask.shutdown();

        if (progressService.isCancel()) {
            indicator.checkCanceled();
        }

        log.info("generate defect files end");
        LocalFileSystem.getInstance().refresh(false);
        if (isCommentEnable) {
            repoID += ProjectConstants.Common.COMMENT_SUFFIX;
            ConfigCacheService.getInstance()
                .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.REPO_ID, repoID);
        }
        String type = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.PROJECT_TYPE, String.class, "");
        log.info("repoID = {}, isCommentEnable = {}, type = {}", repoID, isCommentEnable, type);

        // Parse engine result file.
        return FixbotAnalyzeService.getInstance()
            .parseFixbotResult(project.getBasePath(), GrsServiceProvider.getGrsAllianceDomain(), routePolicy, type,
                fontSize);
    }

    private void fixbotErrorProcessor() {
        String vmOptionsConfigFilePath =
            Paths.get(PluginConstant.PluginDataDir.CONFIG_CACHE_PATH, FixbotConstants.CUSTOM_VMOPTIONS_FILENAME)
                .toString();

        File vmOptionsConfigFile = new File(vmOptionsConfigFilePath);
        if (!vmOptionsConfigFile.exists()) {
            try {
                FileUtils.touch(vmOptionsConfigFile);
                try (OutputStream os = FileUtils.openOutputStream(vmOptionsConfigFile);
                    PrintWriter vmConfigWriter = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                    vmConfigWriter.println(FixbotConstants.DEFAULT_MAX_HEAP_MEMORY_SIZE);
                }
            } catch (IOException e) {
                log.warn(
                    "The vm options config file {} created failed, please create the file and modify the memory size manually",
                    vmOptionsConfigFilePath);
            }
        } else {
            log.info("The vm options config file {} is already exists", vmOptionsConfigFilePath);
        }
    }

    private void asyncStart(ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        if (VersionUtil.getIdeBaselineVersion() < VersionUtil.BASELINE_VERSION_191) {
            TimeoutUtil.sleep(ASYNC_START_SLEEP_MILLIS);
        }
        log.info("Start analysis, Name: {}, projectPath: {}, inspectPath: {}, inspectFolder: {}, routePolicy: {}",
            project.getName(), project.getBasePath(), inspectPath, inspectFolder, routePolicy);

        Optional<HmsConvertorToolWindow> hmsConvertorToolWindow = HmsConvertorUtil.getHmsConvertorToolWindow(project);
        if (!hmsConvertorToolWindow.isPresent()) {
            log.warn("HMS convertor tool window is null!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("no_tool_window"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        try {
            HmsConvertorState.set(project, HmsConvertorState.RUNNING);
            indicator.setText("Analyzing " + inspectFolder + "... Please wait.");
            indicator.setText2(HmsConvertorBundle.message("indicator_analyze_notice2"));

            TimeUtil.getInstance().getStartTime();
            startEngineAnalysis(indicator, hmsConvertorToolWindow.get());
            log.info("Analyze finished! Elapsed time: {}", TimeUtil.getInstance().getElapsedTime());

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
            // pre-analyse backgroup task canceled
            // so need to clear export cache
            SummaryCacheService.getInstance().clearAnalyseResultCache4Export(project.getBasePath());
            // and need to clear conversion toolWindow cache
            ConversionCacheService.getInstance().clearConversions(project.getBasePath());
            SummaryCacheService.getInstance().clearAnalyseResultCache4ConversionToolWindow(project.getBasePath());
            // and need to clear summary toolWindow cache
            SummaryCacheService.getInstance().clearAnalyseResultCache4SummaryResult(project.getBasePath());
            log.warn(ignore.getMessage(), ignore);

            // bi report action: trace cancel operation.
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.FIRST_ANALYZE);

            BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("pre_analyse_task_cancel_success"),
                project, Constant.PLUGIN_NAME, true);
        } catch (NoSuchFileException | JSONException e) {
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

    private void startEngineAnalysis(@NotNull ProgressIndicator indicator, HmsConvertorToolWindow toolWindow)
        throws NoSuchFileException {
        if (indicator == null) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("analyze_error"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        indicator.setFraction(ProgressService.ProgressStage.START_ANALYSIS.getFraction());

        String repoID = inspectFolder + "." + inspectPath.hashCode();
        log.info("repoID: {}", repoID);
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
        indicator.checkCanceled();
        indicator.setFraction(ProgressService.ProgressStage.CLEAR_DATA.getFraction());

        if (!ProjectArchiveService.backupProject(project.getBasePath())) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("backup_failed"), project,
                Constant.PLUGIN_NAME, true);
        }
        indicator.checkCanceled();
        indicator.setFraction(ProgressService.ProgressStage.BACKUP.getFraction());

        if (!ClientUtil.getPluginPackagePath().isPresent()) {
            throw new NoSuchFileException(HmsConvertorBundle.message("no_engine_found"));
        }
        String pluginPackagePath = ClientUtil.getPluginPackagePath().get();
        pluginPackagePath = FileUtil.unifyToUnixFileSeparator(pluginPackagePath);

        FixbotParams fixbotPreAnalysisParams = FixbotAnalyzeService.getInstance()
            .buildFixbotPreAnalysisParams(repoID, inspectPath, configCacheService, project.getBasePath());
        Optional<Map<String, String>> dependencyVersionMap =
            FixbotAnalyzeService.getInstance().preAnalysis4DependencyVersion(fixbotPreAnalysisParams);
        if (!dependencyVersionMap.isPresent()) {
            BalloonNotifications.showErrorNotification(
                HmsConvertorBundle.message("pre_analyse_task_for_dependency_version_fail"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }
        XMSUtils.specializedProcess(dependencyVersionMap.get());
        SummaryCacheManager.getInstance().setDependencyVersion(project.getBasePath(), dependencyVersionMap.get());

        boolean isGenerateDependencyApiMetadataSuccess =
            new DependencyApiMetadataGenerator().generate(pluginPackagePath, dependencyVersionMap.get());
        if (!isGenerateDependencyApiMetadataSuccess) {
            BalloonNotifications.showErrorNotification(
                HmsConvertorBundle.message("dependency_api_metadata_generate_task_fail"), project, Constant.PLUGIN_NAME,
                true);
            return;
        }

        boolean isGenerateGradleMappingSuccess =
            new GradleMappingGenerator().generate(pluginPackagePath, dependencyVersionMap.get());
        if (!isGenerateGradleMappingSuccess) {
            BalloonNotifications.showErrorNotification(HmsConvertorBundle.message("gradle_mapping_generate_task_fail"),
                project, Constant.PLUGIN_NAME, true);
            return;
        }

        boolean isGenerateJavaMappingSuccess =
            new JavaMappingGenerator().generate(pluginPackagePath, dependencyVersionMap.get());
        if (!isGenerateJavaMappingSuccess) {
            BalloonNotifications.showErrorNotification(HmsConvertorBundle.message("java_mapping_generate_task_fail"),
                project, Constant.PLUGIN_NAME, true);
        }

        if (!inspectSource(pluginPackagePath, configCacheService.getProjectConfig(project.getBasePath(),
            ConfigKeyConstants.COMMENT, Boolean.class, false), indicator, new Strategy())) {
            log.info("The fixbotExitValue is {}", fixbotExitValue);
            String message = HmsConvertorBundle.message("engine_analysis_error");
            if (fixbotExitValue == OOM_ERROR_CODE) {
                String vmOptionsConfigFilePath =
                    Paths.get(PluginConstant.PluginDataDir.CONFIG_CACHE_PATH, FixbotConstants.CUSTOM_VMOPTIONS_FILENAME)
                        .toString();
                message =
                    "Out of memory Error in fixbot analysis, change the memory size in file " + vmOptionsConfigFilePath;
            }
            BalloonNotifications.showWarnNotification(message, project, Constant.PLUGIN_NAME, true);
            return;
        }

        indicator.checkCanceled();
        indicator.setFraction(ProgressService.ProgressStage.FINISHED.getFraction());

        ApplicationManager.getApplication().invokeLater(() -> {
            PolicySettingDialog policySettingDialog = new PolicySettingDialog(project);
            policySettingDialog.show();
        }, ModalityState.defaultModalityState());
    }
}