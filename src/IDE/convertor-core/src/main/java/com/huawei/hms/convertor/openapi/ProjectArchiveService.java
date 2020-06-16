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

import com.huawei.hms.convertor.core.project.backup.ProjectBackup;

import lombok.extern.slf4j.Slf4j;

/**
 * Project archive service class
 *
 * @since 2020-03-23
 */
@Slf4j
public final class ProjectArchiveService {
    /**
     * Backup project - automatic mode
     *
     * @param projectBasePath project base path
     * @return success - true
     */
    public static boolean backupProject(String projectBasePath) {
        return ProjectBackup.getInstance().autoBackup(projectBasePath);
    }

    /**
     * Save summary/conversion list/xms diff data
     *
     * @param projectBasePath projectBasePath
     */
    public static void saveAllToolWindowData(String projectBasePath) {
        SummaryCacheService.getInstance().saveSummary(projectBasePath);
        ConversionCacheService.getInstance().saveConversions(projectBasePath);
        XmsDiffCacheService.getInstance().saveXmsDiff(projectBasePath);
    }
}
