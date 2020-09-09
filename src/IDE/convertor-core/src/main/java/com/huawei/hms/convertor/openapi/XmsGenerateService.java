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

import com.huawei.generator.g2x.processor.GenerateSummary;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.g2x.processor.ProcessorUtils;
import com.huawei.generator.g2x.processor.XMSRouterService;
import com.huawei.generator.g2x.processor.XmsService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Xms generator class
 *
 * @since 2020-02-11
 */
public final class XmsGenerateService {
    public static GeneratorResult generateXmsConfig(String pluginJarPath, String outPath, String logPath,
        Map<String, String> dependencyVersionMap) {
        return XMSRouterService.generateXmsConfig(pluginJarPath, outPath, logPath, dependencyVersionMap,
            Collections.emptyMap());
    }

    /**
     * Query the Set<String> of g+h supports kits
     *
     * @return GeneratorResult
     */
    public static Set supportKitInfo() {
        return XmsService.supportKitInfo();
    }

    public static GenerateSummary create(String pluginPath, String backPath, String targetPath,
        Map<String, String> kitMap, Map<String, Set<String>> allDepMap, List<GeneratorStrategyKind> strategykindList,
        boolean thirdSdk, Map<String, String> dependencyVersionMap, boolean useClassloader) {
        ProcessorUtils.Builder builder = new ProcessorUtils.Builder();
        builder.setPluginPath(pluginPath)
            .setBackPath(backPath)
            .setTargetPath(targetPath)
            .setKitMap(kitMap)
            .setAllDepMap(allDepMap)
            .setStrategyKindList(strategykindList)
            .setThirdSDK(thirdSdk)
            .setGmsVersionMap(dependencyVersionMap)
            .setNeedClassLoader(useClassloader)
            .build();
        ProcessorUtils processorUtils = new ProcessorUtils(builder);
        return XmsService.create(processorUtils);
    }

}
