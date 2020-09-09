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

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.ProgressService;
import com.huawei.hms.convertor.util.ExecutorServiceBuilder;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Obtain the execution progress of fixbot in real time
 *
 * @since 2020-05-25
 */
@Slf4j
public class RefreshProgressTask {
    private static final long REFRESH_DELAY = 100L;

    private static final long REFRESH_PERIOD = 500L;

    private static final String THREAD_NAME_PREFIX = "project-";

    private static final String THREAD_NAME_SUFFIX = "refresh-progress-timer-thread-%d";

    private String projectName;

    private double currentFraction;

    private ScheduledThreadPoolExecutor scheduledExecutor;

    /**
     * get the progress of fixbot in real time
     *
     * @param progressService the current fraction according to progress of fixbot
     * @param indicator progress indicator
     * @param projectBasePath project base path
     */
    public RefreshProgressTask(ProgressService progressService, ProgressIndicator indicator, String projectBasePath) {
        projectName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.INSPECT_FOLDER, String.class, "");
        scheduledExecutor = ExecutorServiceBuilder
            .newScheduledThreadPoolExecutor(THREAD_NAME_PREFIX + projectName + THREAD_NAME_SUFFIX);

        executeTimerTask(progressService, indicator);
    }

    private void executeTimerTask(ProgressService progressService, ProgressIndicator indicator) {
        scheduledExecutor.scheduleAtFixedRate(() -> timerExecuteTask(progressService, indicator), REFRESH_DELAY,
            REFRESH_PERIOD, TimeUnit.MILLISECONDS);
    }

    private void timerExecuteTask(ProgressService progressService, ProgressIndicator indicator) {
        try {
            refreshProgress(progressService, indicator);
            cancelIndicator(progressService, indicator);
        } catch (Throwable e) {
            log.error("failed to process timer refresh.", e);
        }
    }

    private void cancelIndicator(ProgressService progressService, ProgressIndicator indicator) {
        try {
            indicator.checkCanceled();
        } catch (ProcessCanceledException e) {
            log.info("Catch indicator cancel signal.");
            progressService.setCancel(true);
        }
    }

    private void refreshProgress(ProgressService progressService, ProgressIndicator indicator) {
        double fraction = progressService.getCurrentFraction();
        double diff = 0.0000001;

        if (Math.abs(currentFraction - fraction) > diff) {
            currentFraction = fraction;
            indicator.setFraction(currentFraction);
            if (Math.abs(currentFraction - ProgressService.EngineStage.CODE_MIGRATE_ENTRY.getFraction()) < diff) {
                return;
            }
        }
    }

    void shutdown() {
        scheduledExecutor.shutdown();
        log.info("timer execute task has finished.");
    }
}
