/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.generator.g2x.processor;

import static com.huawei.generator.g2x.processor.XmsPublicUtils.XMS_PATH;
import static com.huawei.generator.g2x.processor.XmsPublicUtils.walkDir;

import com.huawei.generator.g2x.po.summary.Summary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for XmsPathsManager
 *
 * @since 2019-04-21
 */
public class XmsPathsManager {
    private List<XmsPath> paths = new LinkedList<>();

    public XmsPathsManager(String basePath) throws IOException {
        List<String> targetPaths = walkDir(basePath, new ArrayList<>(), Collections.singletonList(XMS_PATH));
        for (String path : targetPaths) {
            this.paths.add(new XmsPath(path));
        }
    }

    public static Set<String> legalPath(String targetPath, Map<String, String> kitMap,
        List<GeneratorStrategyKind> kindList) {
        String path = targetPath;
        Summary oldSummary = null;
        Set<String> kitSet = new HashSet<>(kitMap.keySet());
        if (path != null) {
            oldSummary = XmsService.resolveOldSummary(path);
            path = path.replace("/", File.separator);
        }
        if (oldSummary != null) {
            kitSet.addAll(oldSummary.getKits());
        }

        Set<String> result = new HashSet<>();
        if (kindList.contains(GeneratorStrategyKind.G) || kindList.contains(GeneratorStrategyKind.H)) {
            result.add(String.join(File.separator, path, "xmsadapter", "src", "xmsgh", "java", "org", "xms"));
            if (kindList.contains(GeneratorStrategyKind.G)) {
                result.add(String.join(File.separator, path, "xmsadapter", "src", "xmsg", "java", "org", "xms"));
            }
            if (kindList.contains(GeneratorStrategyKind.H)) {
                result.add(String.join(File.separator, path, "xmsadapter", "src", "xmsh", "java", "org", "xms"));
            }
        } else {
            result.add(String.join(File.separator, path, "xmsadapter", "src", "main", "java", "org", "xms"));
        }

        if (kitSet.contains("Push") || kitSet.contains("Map")) {
            result.add(String.join(File.separator, path, "xmsadapter", "xmsaux", "src", "main", "java", "org", "xms"));
            result.add(
                String.join(File.separator, path, "xmsadapter", "xmsaux", "xapi", "src", "main", "java", "org", "xms"));
            result.add(
                String.join(File.separator, path, "xmsadapter", "xmsaux", "xg", "src", "main", "java", "org", "xms"));
            result.add(
                String.join(File.separator, path, "xmsadapter", "xmsaux", "xh", "src", "main", "java", "org", "xms"));
        }
        return result;
    }

    public List<XmsPath> getPaths() {
        return paths;
    }
}
