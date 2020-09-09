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

package com.huawei.hms.convertor.openapi;

import com.huawei.codebot.entry.codemigrate.VersionInfoRetriever;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.ConversionGenerator;
import com.huawei.hms.convertor.core.engine.fixbot.FixbotResultParser;
import com.huawei.hms.convertor.core.engine.fixbot.SummaryGenerator;
import com.huawei.hms.convertor.core.engine.fixbot.model.FixbotAnalysisParams;
import com.huawei.hms.convertor.core.engine.fixbot.model.FixbotParamPolicy;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotParams;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;
import com.huawei.hms.convertor.openapi.result.ErrorCode;
import com.huawei.hms.convertor.util.ErrorStreamProcessor;
import com.huawei.hms.convertor.util.ExecutorServiceBuilder;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.util.InputStreamProcessor;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Fixbot engine class
 *
 * @since 2020-02-11
 */
@Slf4j
public final class FixbotAnalyzeService {
    private static final String THREAD_NAME_PREFIX = "project-";

    private static final String INPUT_STREAM_NAME_SUFFIX = "-fixbot-process-input-stream-%d";

    private static final String ERROR_STREAM_NAME_SUFFIX = "-fixbot-process-error-stream-%d";

    private static final FixbotAnalyzeService FIXBOT_SERVICE = new FixbotAnalyzeService();

    private FixbotAnalyzeService() {
    }

    /**
     * Get singleton instance of {@code FixbotAnalyzeService}
     *
     * @return The singleton instance of {@code FixbotAnalyzeService}
     */
    public static FixbotAnalyzeService getInstance() {
        return FIXBOT_SERVICE;
    }

    public Optional<Map<String, String>> preAnalysis4DependencyVersion(FixbotParams fixbotParams) {
        String[] arguments = fixbotParams.toPreAnalysisStringArgs();
        log.info("begin preAnalysis, arguments: {}.", Arrays.asList(arguments));

        Map<String, String> dependencyVersionMap;
        try {
            dependencyVersionMap = VersionInfoRetriever.getAllKitsVersion(arguments);
        } catch (CodeBotRuntimeException e) {
            log.error("fixbot preAnalysis for dependency version fail, exception: {}.", e.getMessage());
            return Optional.empty();
        }

        log.info("end preAnalysis, dependencyVersionMap: {}.", dependencyVersionMap);
        return Optional.of(dependencyVersionMap);
    }

    /**
     * Execute fixbot
     *
     * @param fixbotParams    fixbot params
     * @param progressService the ratio of fixbot analysis
     * @param projectBasePath project base path
     */
    public int executeFixbot(FixbotParams fixbotParams, ProgressService progressService, String projectBasePath) {
        logJavaEnvironment();
        String projectName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.INSPECT_FOLDER, String.class, "");
        ExecutorService inputStreamExecutor =
            ExecutorServiceBuilder.newSingleThreadExecutor(THREAD_NAME_PREFIX + projectName + INPUT_STREAM_NAME_SUFFIX);
        ExecutorService errorStreamExecutor =
            ExecutorServiceBuilder.newSingleThreadExecutor(THREAD_NAME_PREFIX + projectName + ERROR_STREAM_NAME_SUFFIX);

        String[] arguments = fixbotParams.toAnalysisStringArgs();
        log.info("Startup arguments: {}", Arrays.asList(arguments));

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(new File(fixbotParams.getEnginePath()));

        try {
            log.info("Begin to execute fixbot");

            Process fixbotProcess = processBuilder.start();
            InputStreamProcessor inputStreamProcessor =
                new InputStreamProcessor(fixbotProcess, fixbotProcess.getInputStream(), progressService);
            ErrorStreamProcessor errorStreamProcessor = new ErrorStreamProcessor(fixbotProcess.getErrorStream());
            inputStreamExecutor.execute(inputStreamProcessor);
            errorStreamExecutor.execute(errorStreamProcessor);
            int exitValue = fixbotProcess.waitFor();
            inputStreamExecutor.shutdown();
            errorStreamExecutor.shutdown();

            log.info("The inputStream and errorStream of fixbotProcess have been processed completed");

            if (exitValue == 0) {
                log.info("Finish executing fixbot successfully");
                return 0;
            }

            if (progressService.isCancel()) {
                log.info("The user triggered the cancel button");
                return 0;
            }

            if (errorStreamProcessor.getErrorCode() == ErrorCode.OOM.getCode()) {
                exitValue = ErrorCode.OOM.getCode();
            }

            log.error("Failed to execute fixbot, exit value: {}", exitValue);
            return exitValue;
        } catch (IOException | InterruptedException e) {
            log.error("Execute fixbot error", e);
            return 1;
        }
    }

    /**
     * Parse engine result
     *
     * @param projectBasePath project base path
     * @param allianceDomain  domain
     * @param routePolicy     route policy
     * @param type            app or sdk
     * @param fontSize        font size
     * @return true if success
     */
    public boolean parseFixbotResult(String projectBasePath, String allianceDomain, RoutePolicy routePolicy,
        String type, int fontSize) {
        FixbotResultParser parser =
            new FixbotResultParser(projectBasePath, allianceDomain, routePolicy, type, fontSize);
        boolean result = parser.parseFixbotResult();

        if (result) {
            // Generator conversion list
            ConversionGenerator conversionGenerator = new ConversionGenerator(parser);
            List<ConversionItem> conversions = conversionGenerator.extractConverisons();
            // Set conversion cache
            ConversionCacheService.getInstance().addConversions(projectBasePath, conversions, false);

            // Generator summary
            SummaryGenerator summaryGenerator = new SummaryGenerator(parser, conversionGenerator);
            Map<String, String> showData = summaryGenerator.extractShowData();
            // Set summary cache
            SummaryCacheManager.getInstance().setShowData(projectBasePath, showData);
            SummaryCacheManager.getInstance()
                .setKit2MethodAnalyseResultsMap(projectBasePath, summaryGenerator.getKit2MethodAnalyseResultsMap());
            SummaryCacheManager.getInstance()
                .setKit2ClassAnalyseResultsMap(projectBasePath, summaryGenerator.getKit2ClassAnalyseResultsMap());
            SummaryCacheManager.getInstance()
                .setKit2FieldAnalyseResultsMap(projectBasePath, summaryGenerator.getKit2FieldAnalyseResultsMap());
            SummaryCacheManager.getInstance()
                .setKitStatisticsResults(projectBasePath, summaryGenerator.getKitStatisticsResults());
            SummaryCacheManager.getInstance()
                .setProjectStatisticsResult(projectBasePath, summaryGenerator.getProjectStatisticsResult());
            SummaryCacheManager.getInstance().setAllKits(projectBasePath, parser.getAllKits());
            SummaryCacheManager.getInstance().setAllDependencies(projectBasePath, parser.getAllDependencies());
            SummaryCacheManager.getInstance()
                .setKit2FixbotMethodsMap(projectBasePath, parser.getKit2FixbotMethodsMap());
            SummaryCacheManager.getInstance().setXmsSetting(projectBasePath, parser.getXmsSetting());
        }
        return result;
    }

    public FixbotParams buildFixbotPreAnalysisParams(String repoId, String repoPath,
        ConfigCacheService configCacheService,String projectBasePath
        ) {
        FixbotParams fixbotParams = new FixbotParams();
        fixbotParams.setCacheDirectory(repoId);
        fixbotParams.setInspectPath(repoPath);
        fixbotParams.setFixPath(buildFixbotParamFixPath(repoId));
        fixbotParams.setPolicy(FixbotParamPolicy.getPreAnalysisPolicy().getPolicy());
        fixbotParams.setExcludedPaths(buildFixbotParamExcludePaths(configCacheService, projectBasePath));
        return fixbotParams;
    }

    public FixbotParams buildFixbotAnalysisParams(FixbotAnalysisParams fixbotAnalysisParams) {
        FixbotParams fixbotParams = new FixbotParams();
        String pluginJarPath = fixbotAnalysisParams.getPluginPackagePath() + PluginConstant.PluginPackageDir.LIB_DIR;
        String configPath = fixbotAnalysisParams.getPluginPackagePath() + PluginConstant.PluginPackageDir.CONFIG_DIR;

        fixbotParams.setEnginePath(pluginJarPath);
        fixbotParams.initJvmOpt();
        BIReportService.getInstance().setJvmXmx(fixbotAnalysisParams.getProjectBasePath(), fixbotParams.getJvmOpt());
        fixbotParams.setMappingPath(configPath);
        fixbotParams.setCacheDirectory(fixbotAnalysisParams.getRepoId());
        fixbotParams.setInspectPath(fixbotAnalysisParams.getRepoPath());
        fixbotParams.setFixPath(buildFixbotParamFixPath(fixbotAnalysisParams.getRepoId()));

        fixbotParams.setPolicy(FixbotParamPolicy.getAnalysisPolicyByRoutePolicy(fixbotAnalysisParams.getRoutePolicy()).getPolicy());

        if (fixbotAnalysisParams.getProjectType().equalsIgnoreCase(ProjectConstants.Type.SDK)) {
            fixbotParams.setSdk(true);
        }

        fixbotParams.setOnlyG(fixbotAnalysisParams.getStrategy().isOnlyG());
        fixbotParams.setOnlyH(fixbotAnalysisParams.getStrategy().isOnlyH());
        fixbotParams.setExcludedPaths(buildFixbotParamExcludePaths(fixbotAnalysisParams.getConfigCacheService(), fixbotAnalysisParams.getProjectBasePath()));
        return fixbotParams;
    }

    private void logJavaEnvironment() {
        log.info("Java Version \"{}\", {}", System.getProperty("java.version"), System.getProperty("java.vendor"));
        log.info("{} (build {})", System.getProperty("java.runtime.name"), System.getProperty("java.runtime.version"));
        log.info("{} (build {}, {}, {})", System.getProperty("java.vm.name"), System.getProperty("java.vm.version"),
            System.getProperty("java.vm.info"), System.getProperty("java.vm.vendor"));
        log.info("{}, OS Version: {}, OS Arch: {}", System.getProperty("os.name"), System.getProperty("os.version"),
            System.getProperty("os.arch"));
    }

    private String buildFixbotParamFixPath(String repoId) {
        return PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + repoId;
    }

    private List<String> buildFixbotParamExcludePaths(ConfigCacheService configCacheService, String projectBasePath) {
        List<String> excludePaths = configCacheService.getProjectConfig(projectBasePath,
            ConfigKeyConstants.EXCLUDE_PATH, List.class, new ArrayList());
        excludePaths.remove("Common");
        List<String> arguments = new ArrayList<>();
        if (!excludePaths.isEmpty()) {
            for (String excludePath : excludePaths) {
                arguments.add(FileUtil.unifyToUnixFileSeparator(excludePath));
            }
        }

        List<String> xmsAdapterPathList = configCacheService.getProjectConfig(projectBasePath,
            ConfigKeyConstants.XMS_PATH, List.class, new ArrayList());
        if (!xmsAdapterPathList.isEmpty()) {
            for (String path : xmsAdapterPathList) {
                arguments.add(FileUtil.unifyToUnixFileSeparator(path) + "/org/xms");
            }
        }

        List<String> xms4GAdapterPaths = configCacheService.getProjectConfig(projectBasePath,
            ConfigKeyConstants.XMS_MULTI_PATH, List.class, new ArrayList());
        if (!xms4GAdapterPaths.isEmpty()) {
            for (String path : xms4GAdapterPaths) {
                arguments.add(FileUtil.unifyToUnixFileSeparator(path));
            }
        }

        String[] xmsModulePaths = FileUtil.getSummaryModule(projectBasePath);
        for (String xmsModulePath : xmsModulePaths) {
            arguments.add(FileUtil.unifyToUnixFileSeparator(xmsModulePath));
            // xmsadapter/build.gradle is fixed, user is advised not to modify it.
            // If the user modified this file for some special reason,
            // they can include xmsadapter/build.gradle themselves.
        }
        return arguments;
    }
}