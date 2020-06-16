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

import com.huawei.hms.convertor.core.result.diff.XmsDiff;
import com.huawei.hms.convertor.core.result.diff.XmsDiffCacheManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Xms diff cache manager
 *
 * @since 2020-03-24
 */
@Setter
@Getter
@Slf4j
public final class XmsDiffCacheService {
    private static final XmsDiffCacheService XMS_DIFF_CACHE_SERVICE = new XmsDiffCacheService();

    private XmsDiffCacheService() {
    }

    /**
     * Get singleton instance of {@code XmsDiffCacheService}
     *
     * @return The singleton instance of {@code XmsDiffCacheService}
     */
    public static XmsDiffCacheService getInstance() {
        return XMS_DIFF_CACHE_SERVICE;
    }

    /**
     * Set xms diff info
     *
     * @param projectBasePath projectBasePath
     * @param xmsDiff xms diff
     */
    public void setXmsDiff(String projectBasePath, XmsDiff xmsDiff) {
        XmsDiffCacheManager.getInstance().setXmsDiff(projectBasePath, xmsDiff);
    }

    /**
     * Save xms diff info
     *
     * @param projectBasePath projectBasePath
     */
    public void saveXmsDiff(String projectBasePath) {
        XmsDiffCacheManager.getInstance().saveXmsDiff(projectBasePath);
    }

    /**
     * Load xms diff info
     *
     * @param projectBasePath project base path
     * @return xms diff info
     */
    public XmsDiff loadXmsDiff(String projectBasePath) {
        return XmsDiffCacheManager.getInstance().loadXmsDiff(projectBasePath);
    }
}
