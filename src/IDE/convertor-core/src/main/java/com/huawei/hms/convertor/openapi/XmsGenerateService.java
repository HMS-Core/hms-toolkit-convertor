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

import com.huawei.generator.g2x.po.summary.Summary;
import com.huawei.generator.g2x.processor.GenerateSummary;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.XMSRouterService;
import com.huawei.generator.g2x.processor.XmsService;
import com.huawei.generator.g2x.processor.ProcessorUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Xms generator class
 *
 * @since 2020-02-11
 */
public final class XmsGenerateService {
    /**
     * generate g+h mapping
     *
     * @param pluginJarPath xms package path
     * @param outPath mapping generation path
     * @param logPath log path
     * @return GeneratorResult
     */
    public static GeneratorResult generateXmsConfig(String pluginJarPath, String outPath, String logPath) {
        return XMSRouterService.generateXmsConfig(pluginJarPath, outPath, logPath, Collections.emptyMap(),
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

    /**
     * Create a new module and generate diff
     *
     * @param processorUtils should be build with
     *        pluginPath/backPath/targetPath/kitMap/allDepMap/strategyKindList/thirdSDK
     * @return generate result
     */
    public static GenerateSummary create(ProcessorUtils processorUtils) {
        return XmsService.create(processorUtils);
    }

    /**
     * Create a new module without g/h first and generate diff
     *
     * @param processorUtils should be build with
     *        pluginPath/oldPath/newPath/kitMap/allDepMap/useOnlyG/thirdSDK
     * @return result
     */
    public static GenerateSummary createWithoutFirstStrategy(ProcessorUtils processorUtils) {
        return XmsService.createWithoutFirstStrategy(processorUtils);
    }

    /**
     * D return summary for IDE to produce sdk/app and strategy
     *
     * @param pluginPath plugin path
     * @param rootPath path/xmsadapter
     * @return summary, null for fail
     */
    public static Summary inferStrategy(String pluginPath, String rootPath) {
        return XmsService.inferStrategy(pluginPath, rootPath);
    }
}
