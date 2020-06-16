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

import com.huawei.hms.convertor.core.engine.fixbot.ConversionGenerator;
import com.huawei.hms.convertor.core.engine.fixbot.FixbotResultParser;
import com.huawei.hms.convertor.core.engine.fixbot.SummaryGenerator;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotParams;

import com.huawei.hms.convertor.core.result.conversion.ConversionCacheManager;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Fixbot engine class
 *
 * @since 2020-02-11
 */
@Slf4j
public final class FixbotAnalyzeService {
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

    /**
     * Execute fixbot
     *
     * @param fixbotParams fixbot params
     */
    public int executeFixbot(FixbotParams fixbotParams) {
        logJavaEnvironment();

        String[] arguments = fixbotParams.toStringArgs();
        log.info("Startup arguments: {}", Arrays.asList(arguments));

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(new File(fixbotParams.getEnginePath()));

        try {
            log.info("Begin to execute fixbot");

            Process fixbotProcess = processBuilder.start();
            handleFixbotOutput(fixbotProcess.getInputStream());
            int exitValue = fixbotProcess.waitFor();
            if (exitValue != 0) {
                log.error("Failed to execute fixbot, exit value: {}", exitValue);
            } else {
                log.info("Finish executing fixbot successfully");
            }
            return exitValue;
        } catch (IOException | InterruptedException e) {
            log.error("Execute fixbot error", e);
            return 1;
        }
    }

    private void handleFixbotOutput(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        } catch (IOException e) {
            log.error("Handle foxbot output error", e);
        }
    }

    private void logJavaEnvironment() {
        log.info("Java Version \"{}\", {}", System.getProperty("java.version"), System.getProperty("java.vendor"));
        log.info("{} (build {})", System.getProperty("java.runtime.name"), System.getProperty("java.runtime.version"));
        log.info("{} (build {}, {}, {})", System.getProperty("java.vm.name"), System.getProperty("java.vm.version"),
            System.getProperty("java.vm.info"), System.getProperty("java.vm.vendor"));
        log.info("{}, OS Version: {}, OS Arch: {}", System.getProperty("os.name"), System.getProperty("os.version"),
            System.getProperty("os.arch"));
    }

    /**
     * Parse engine result
     *
     * @param projectBasePath project base path
     * @param allianceDomain domain
     * @param routePolicy route policy
     * @param type app or sdk
     * @param fontSize font size
     * @return true if success
     */
    public boolean parseFixbotResult(String projectBasePath, String allianceDomain, RoutePolicy routePolicy,
        String type, int fontSize) {
        FixbotResultParser parser = new FixbotResultParser(
            projectBasePath, allianceDomain, routePolicy, type, fontSize);
        boolean result = parser.parseFixbotResult();

        if (result) {
            // Generator conversion list
            ConversionGenerator conversionGenerator = new ConversionGenerator(parser);
            List<ConversionItem> conversions = conversionGenerator.extractConverisons();
            // Set conversion cache
            ConversionCacheManager.getInstance().addConversions(projectBasePath, conversions, false);

            // Generator summary
            SummaryGenerator summaryGenerator = new SummaryGenerator(parser);
            Map<String, String> showData = summaryGenerator.extractShowData();
            // Set summary cache
            SummaryCacheManager.getInstance().setShowData(projectBasePath, showData);
            SummaryCacheManager.getInstance().setAllKits(projectBasePath, parser.getAllKits());
            SummaryCacheManager.getInstance().setAllDependencies(projectBasePath, parser.getAllDependencies());
            SummaryCacheManager.getInstance().setKit2Methods(projectBasePath, parser.getKit2Methods());
        }
        return result;
    }
}
