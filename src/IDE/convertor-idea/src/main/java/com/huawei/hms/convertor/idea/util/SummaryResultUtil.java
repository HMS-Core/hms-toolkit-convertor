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

import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitStatisticsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SummaryResultUtil {
    public static List<String> computeKit4DependOnGmsClassOrField(List<KitStatisticsResult> kitStatisticsResults,
        List<String> kits4DependOnGmsMethod) {
        List<String> kits4DependOnGmsApi =
            kitStatisticsResults.stream().map(KitStatisticsResult::getKit).collect(Collectors.toList());
        List<String> kits4DependOnGmsClassOrField = new ArrayList<>(kits4DependOnGmsApi);
        kits4DependOnGmsClassOrField.removeAll(kits4DependOnGmsMethod);
        return kits4DependOnGmsClassOrField;
    }
}
