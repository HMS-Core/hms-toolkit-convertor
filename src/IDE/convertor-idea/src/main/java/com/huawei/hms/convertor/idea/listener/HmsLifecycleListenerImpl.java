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

package com.huawei.hms.convertor.idea.listener;

import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.ConversionCacheService;
import com.huawei.hms.convertor.openapi.EventService;
import com.huawei.hms.convertor.openapi.SummaryCacheService;
import com.huawei.hms.convertor.openapi.XmsDiffCacheService;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Convertor startup activity
 *
 * @since 2020-03-04
 */
@Slf4j
public class HmsLifecycleListenerImpl implements ProjectManagerListener {
    // Unique idea project instance of each project.
    private Project project;

    public HmsLifecycleListenerImpl(Project project) {
        this.project = project;
    }

    @Override
    public void projectClosing(Project project) {
        if (!this.project.equals(project)) {
            return;
        }
        log.info("Project closing : {}", project.getName());

        // Save data (Summary, Conversion, XmsDiff).
        SummaryCacheService.getInstance().saveSummary(project.getBasePath());
        ConversionCacheService.getInstance().saveConversions(project.getBasePath());
        XmsDiffCacheService.getInstance().saveXmsDiff(project.getBasePath());

        // Remove documentListener.
        HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
            hmsConvertorToolWindow.getSourceConvertorToolWindow().asyncClearData();
        });

        try {
            ConfigCacheService.getInstance().clearProjectConfig(project.getBasePath());

            // clear bi report service instance.
            BIReportService.getInstance().clearTraceService(project.getBasePath());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        EventService.getInstance().shutdownProjectEventContext(project.getBasePath());
    }

    @Override
    public void projectClosed(Project project) {
        if (!this.project.equals(project)) {
            return;
        }
        log.info("Project closed: {}", project.getName());
    }
}
