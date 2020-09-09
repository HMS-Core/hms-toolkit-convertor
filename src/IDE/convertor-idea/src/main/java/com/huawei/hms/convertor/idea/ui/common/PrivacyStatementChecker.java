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

package com.huawei.hms.convertor.idea.ui.common;

import com.huawei.hms.convertor.idea.util.PlatformReflectInvoker;

import com.intellij.openapi.project.Project;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Privacy statement checker
 *
 * @since 2020-01-02
 */
@Slf4j
public final class PrivacyStatementChecker {
    private static final String API_CLASS = "com.huawei.hms.common.util.CommonUtils";

    private static final String API_METHOD = "checkAgreementStatus";

    /**
     * Check if user does not agree with privacy statement
     *
     * @return If user does not agree with privacy statement, return {@code true};
     *         otherwise return {@code false}
     * @param project Project
     */
    public static boolean isNotAgreed(Project project) {
        PlatformReflectInvoker.InvokeResult result = PlatformReflectInvoker.invokeStaticMethod(API_CLASS, API_METHOD,
            new Class<?>[] {Project.class}, new Object[] {project});

        // The platform SDK is not found. The privacy statement check is skipped by default.
        if (result.isSdkNotFound()) {
            return false;
        }

        // Failed to invoke the platform interface. The privacy statement check is not agreed.
        Object returnValue = result.getReturnValue();
        if (Objects.isNull(returnValue)) {
            return true;
        }

        // The platform interface invokes error. The privacy statement check is not agreed.
        if (!(returnValue instanceof Boolean)) {
            log.error("Unexpected return value, type error");
            return true;
        }

        return !((boolean) returnValue);
    }
}
