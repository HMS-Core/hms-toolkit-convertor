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

import com.huawei.hms.convertor.core.mapping.MappingInitializer;

/**
 * Mapping initialize service
 *
 * @since 2020-03-27
 */
public final class MappingInitService {
    private static final MappingInitService INIT_SERVICE = new MappingInitService();

    private MappingInitService() {
    }

    /**
     * Get singleton instance of {@code MappingInitService}
     *
     * @return The singleton instance of {@code MappingInitService}
     */
    public static MappingInitService getInstance() {
        return INIT_SERVICE;
    }

    /**
     * Init kits mappings
     */
    public void init() {
        MappingInitializer.initialize();
    }
}
