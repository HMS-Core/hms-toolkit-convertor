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

package com.huawei.generator.g2x.processor.map;

import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.XmsService;
import com.huawei.generator.utils.KitInfoRes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * validate and refine kit-dependency and dependency-version
 *
 * @since 2019-11-25
 */
public class Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    public static GeneratorResult validateParam(String outPath, String summaryPath, String pluginPath) {
        // check outPaths
        if (outPath == null || outPath.isEmpty()) {
            return GeneratorResult.INVALID_OUTPATH;
        }
        if (summaryPath == null || summaryPath.isEmpty()) {
            return GeneratorResult.INVALID_SUMMARYPATH;
        }

        try {
            Paths.get(summaryPath);
        } catch (InvalidPathException e) {
            return GeneratorResult.INVALID_SUMMARYPATH;
        }

        try {
            Paths.get(outPath);
        } catch (InvalidPathException e) {
            return GeneratorResult.INVALID_OUTPATH;
        }

        // check pluginPath
        if (pluginPath == null || pluginPath.isEmpty()) {
            return GeneratorResult.MISSING_PLUGIN;
        }

        // input path must be jar package
        File file = new File((pluginPath));
        if (file.isDirectory() || !pluginPath.endsWith(".jar") || !file.exists()) {
            return GeneratorResult.MISSING_PLUGIN;
        }
        return GeneratorResult.SUCCESS;
    }

    public static List<String> generateEssentialDependency(List<String> kitList) {
        Set<String> result = new HashSet<>();
        // check dependency loop and mark walked node
        List<String> chain = new LinkedList<>();
        Queue<String> queue = new LinkedList<>();

        for (String str : kitList) {
            if (KitInfoRes.INSTANCE.getKitDependencyMap().containsKey(str.trim())) {
                queue.add(str);
                chain.add(str);
                result.add(str);
            }
        }

        while (queue.size() != 0) {
            String jarName = queue.poll();
            if (KitInfoRes.INSTANCE.getKitDependencyMap().containsKey(jarName.trim())) {
                List<String> relies = KitInfoRes.INSTANCE.getKitDependencyMap().get(jarName.trim());
                for (String rely : relies) {
                    if (KitInfoRes.INSTANCE.getKitDependencyMap().containsKey(rely.trim())) {
                        if (!chain.contains(rely)) {
                            queue.add(rely);
                            chain.add(rely);
                        }
                        result.add(rely);
                    }
                }
            }
        }
        return new LinkedList<>(result);
    }

    // check by list
    public static boolean validNameFromList(String name, Set<String> kitList) {
        if (name == null || !name.endsWith(".json")) {
            return false;
        }

        // unify path format
        String tmpName = name.replace(File.separator, "/");

        for (String str : kitList) {
            String keyPath = "/json/" + str + "/";
            String agcPath = "/agc-json/" + str + "/";
            if (tmpName.contains(keyPath) || tmpName.contains(agcPath)) {
                return true;
            }
        }
        return false;
    }

    public static void validKits(Map<String, String> kitMap) {
        Iterator<Map.Entry<String, String>> entryIterator = kitMap.entrySet().iterator();
        Set<String> supportList = XmsService.supportKitInfo();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            if (!supportList.contains(entry.getKey())) {
                LOGGER.warn("a unexpected kitName appeared {}", entry.getKey());
                entryIterator.remove();
            }
        }
    }
}
