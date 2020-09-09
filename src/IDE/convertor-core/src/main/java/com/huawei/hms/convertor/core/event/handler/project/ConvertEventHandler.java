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

package com.huawei.hms.convertor.core.event.handler.project;

import com.huawei.hms.convertor.core.event.handler.AbstractCallbackHandler;
import com.huawei.hms.convertor.core.project.convert.CodeConvertService;
import com.huawei.hms.convertor.core.project.convert.GradleSyncService;
import com.huawei.hms.convertor.core.result.conversion.ConversionCacheManager;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.openapi.ConversionCacheService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Convert event handler
 *
 * @since 2020-02-29
 */
@Slf4j
public class ConvertEventHandler extends AbstractCallbackHandler<String, List<ConversionItem>>
    implements GeneralEventHandler {
    private CodeConvertService codeConvertService;
    private GradleSyncService gradleSyncService;
    private static final String GRADLE_FILE_EXTENSION = ".gradle";

    private String projectPath;

    public ConvertEventHandler(String projectPath) {
        ServiceLoader<CodeConvertService> codeConvertServices =
            ServiceLoader.load(CodeConvertService.class, getClass().getClassLoader());
        ServiceLoader<GradleSyncService> gradleSyncServices =
            ServiceLoader.load(GradleSyncService.class, getClass().getClassLoader());
        gradleSyncService = gradleSyncServices.iterator().next();
        codeConvertService = codeConvertServices.iterator().next();
        this.projectPath = projectPath;
        codeConvertService.init(projectPath);
        gradleSyncService.init(projectPath);
    }

    @Override
    public <T> void handleEvent(String projectPath, T data) {
        if (!(data instanceof String)) {
            log.error("Illegal event data type, expected: String");
            return;
        }
        String conversionId = (String) data;
        ConversionCacheManager.getInstance().correctCache(projectPath, conversionId, true);
        ConversionItem conversionItem = ConversionCacheService.getInstance().getConversion(projectPath, conversionId);
        codeConvertService.convert(projectPath, conversionItem);
        syncGradle(projectPath, conversionItem);
    }

    @Override
    public List<ConversionItem> getCallbackMessage() {
        return ConversionCacheManager.getInstance().getCorrectedItems(projectPath);
    }

    // Determine whether it is necessary to automatically synchronize the project
    private boolean determineIfNeedSync(String projectPath, ConversionItem conversionItem) {
        List<ConversionItem> conversionItemList = ConversionCacheService.getInstance().getAllConversions(projectPath);
        boolean isGradle = conversionItem.getFile().endsWith(GRADLE_FILE_EXTENSION);
        if (!isGradle) {
            return false;
        }
        boolean isAllConverted = conversionItemList.stream().filter(item -> item.getFile().endsWith(GRADLE_FILE_EXTENSION)).allMatch(item -> item.isConverted());
        return isAllConverted;

    }

    private void syncGradle(String projectPath, ConversionItem conversionItem) {
        if (!determineIfNeedSync(projectPath, conversionItem)) {
            return;
        }
        gradleSyncService.sync(projectPath);
    }
}
