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
import com.huawei.hms.convertor.core.project.backup.ProjectRecovery;
import com.huawei.hms.convertor.core.project.backup.ProjectRecoveryParams;
import com.huawei.hms.convertor.openapi.result.Result;

/**
 * Recovery event handler
 *
 * @since 2020-03-23
 */
public class RecoveryEventHandler extends AbstractCallbackHandler<ProjectRecoveryParams, Result>
    implements GeneralEventHandler {
    @Override
    public <T> void handleEvent(String projectPath, T eventData) {
        if (eventData instanceof ProjectRecoveryParams) {
            ProjectRecoveryParams params = (ProjectRecoveryParams) eventData;
            ProjectRecovery.getInstance()
                .recoveryProject(params.getProjectBasePath(), params.getBackupPath(), params.getBackupPoint(),
                    params.getRecoveryPath());
        } else {
            ProjectRecovery.getInstance().setResult(Result.failed(""));
        }
    }

    @Override
    protected Result getCallbackMessage() {
        return ProjectRecovery.getInstance().getResult();
    }
}
