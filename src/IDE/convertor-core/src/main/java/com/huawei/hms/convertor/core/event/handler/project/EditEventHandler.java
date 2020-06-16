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
import com.huawei.hms.convertor.core.result.conversion.ChangedCode;
import com.huawei.hms.convertor.core.result.conversion.ConversionCacheManager;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Code edit event handler
 *
 * @since 2020-02-29
 */
@Slf4j
public class EditEventHandler extends AbstractCallbackHandler<ChangedCode, List<ConversionItem>>
    implements GeneralEventHandler {
    private String projectPath;

    public EditEventHandler(String projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public <T> void handleEvent(String projectPath, T data) {
        if (!(data instanceof ChangedCode)) {
            log.error("Illegal data type, expect: {}", ChangedCode.class.getName());
            return;
        }
        ChangedCode changedCode = (ChangedCode) data;
        ConversionCacheManager.getInstance().correctCache(projectPath, changedCode);
    }

    @Override
    public List<ConversionItem> getCallbackMessage() {
        if (!ConversionCacheManager.getInstance().getFlag(projectPath)) {
            return Collections.emptyList();
        }
        return ConversionCacheManager.getInstance().getCorrectedItems(projectPath);
    }
}
