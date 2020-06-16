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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Privacy statement verification
 *
 * @since 2020-01-02
 */
public final class PrivacyStatementChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyStatementChecker.class);

    private static final String API_CLASS = "com.huawei.hms.common.hiai.ui.HuaweiTip";

    private static final String API_METHOD = "isAgreePrivacy";

    /**
     * Privacy statement detection
     *
     * @return If the subscriber does not agree with the privacy statement ,return {@code false};
     *      Otherwise return {@code true}
     */
    public static boolean isNotAgreed() {
        PlatformReflectInvoker.InvokeResult result = PlatformReflectInvoker.invokeStaticMethod(API_CLASS, API_METHOD);

        // The platform SDK is not found. The privacy statement check is skipped by default.
        if (result.isSdkLost()) {
            return false;
        }

        // Failed to invoke the platform interface. the value returned of privacy statement is not agreed.
        Object returnValue = result.getReturnValue();
        if (Objects.isNull(returnValue)) {
            return true;
        }

        if (!(returnValue instanceof Boolean)) {
            LOGGER.error("Unexpected return value, type error");
            return true;
        }

        return !((boolean) returnValue);
    }
}