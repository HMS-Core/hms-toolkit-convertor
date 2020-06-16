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

package com.huawei.hms.convertor.idea.ui.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * bi data manager
 *
 * @since 2020-04-03
 */

public final class BIInfoManager {
    private static final BIInfoManager INSTANCE = new BIInfoManager();

    private Map<String, Long> analyzeBeginTimeMap = new HashMap<>();

    private BIInfoManager() {
    }

    public static BIInfoManager getInstance() {
        return INSTANCE;
    }

    public void clearData(String projectPath) {
        analyzeBeginTimeMap.remove(projectPath);
    }

    public Long getAnalyzeBeginTime(String projectPath) {
        return analyzeBeginTimeMap.get(projectPath);
    }

    public void setAnalyzeBeginTime(String projectPath, Long analyzeBeginTime) {
        analyzeBeginTimeMap.put(projectPath, analyzeBeginTime);
    }
}
