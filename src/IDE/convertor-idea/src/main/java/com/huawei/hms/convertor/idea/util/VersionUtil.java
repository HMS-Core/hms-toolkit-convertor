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

package com.huawei.hms.convertor.idea.util;

import com.intellij.openapi.application.ApplicationInfo;

/**
 * Version util
 *
 * @since 2018-09-12
 */
public final class VersionUtil {
    /**
     * IDE baseline version 191
     */
    public static final int BASELINE_VERSION_191 = 191;

    /**
     * Get IDE baseline version
     *
     * @return IDE baseline version number
     **/
    public static int getIdeBaselineVersion() {
        final ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        return applicationInfo.getBuild().getBaselineVersion();
    }
}
