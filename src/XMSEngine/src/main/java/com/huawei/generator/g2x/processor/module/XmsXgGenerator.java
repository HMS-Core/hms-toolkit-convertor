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

package com.huawei.generator.g2x.processor.module;

import com.huawei.generator.g2x.processor.ProcessorUtils;
import com.huawei.generator.g2x.processor.XmsConstants;
import com.huawei.generator.utils.EnhancerUtils;
import com.huawei.generator.utils.FileUtils;
import com.huawei.generator.utils.KitInfoRes;
import com.huawei.generator.utils.StaticPatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for XmsXgGenerator
 *
 * @since 2020-05-06
 */
public final class XmsXgGenerator extends ModuleGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmsXgGenerator.class);

    private String generatePath;

    public XmsXgGenerator(ProcessorUtils processorUtils) {
        super(processorUtils);
        resolveDepMap(processorUtils.getAllDepMap());
        generatePath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME);
        modulePath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME,
            XmsConstants.XMS_SUBMODULE_NAME, "xg");
        summaryPath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME, "config");
        manifestPath = String.join(File.separator, modulePath, "src", "main");
    }

    @Override
    void resolveDepMap(Map<String, Set<String>> allDepMap) {
        kitSet.forEach(kit -> {
            if (allDepMap.get(kit) != null) {
                allDepMap.get(kit).forEach(dependency -> {
                    if (EnhancerUtils.isGmsType(dependency)) {
                        depList.putIfAbsent(kit, new HashSet<>());
                        depList.get(kit).add(dependency);
                    } else {
                        LOGGER.error("dependency may should not be here；{}", dependency);
                    }
                });
            }
        });
    }

    @Override
    boolean generateCode() {
        List<String> excludes = new ArrayList<>();
        if (!kitSet.contains("Push")) {
            excludes.add("xms/xmsaux/xg/src/main/java/org/xms/f");
        }
        if (!kitSet.contains("Map")) {
            excludes.add("xms/xmsaux/xg/src/main/java/org/xms/g");
        }
        StaticPatcher.copyResourceDir(pluginPath, generatePath, "xms/xmsaux/xg", excludes);
        return true;
    }

    @Override
    public void createModule() {
        generateCode();
        generateGradle();
    }

    @Override
    boolean generateGradle() {
        StringBuilder stringBuilder = readGradle();
        fillApplyPart(stringBuilder);
        fillAndroidPart(stringBuilder);
        fillDependencyPart(stringBuilder);
        FileUtils.createFile(stringBuilder.toString(), modulePath, "build.gradle");
        return true;
    }

    @Override
    void fillApplyPart(StringBuilder stringBuilder) {
    }

    @Override
    void fillAndroidPart(StringBuilder stringBuilder) {
        Map<String, Integer> versionInfo = KitInfoRes.INSTANCE.getDefaultSdkVersion(new ArrayList<>(kitSet));
        String compileSdk = "";
        if (versionInfo.containsKey("compileSdkVersion")) {
            compileSdk = versionInfo.get("compileSdkVersion").toString();
        }
        String minSdk = "";
        String targetSdk = "";
        if (versionInfo.containsKey("minSdkVersion")) {
            minSdk = versionInfo.get("minSdkVersion").toString();
        }
        if (versionInfo.containsKey("targetSdkVersion")) {
            targetSdk = versionInfo.get("targetSdkVersion").toString();
        }

        anchorReplace(stringBuilder, "${SDKVERSION}", compileSdk);
        anchorReplace(stringBuilder, "${MINVERSION}", minSdk);
        anchorReplace(stringBuilder, "${TARGETVERSION}", targetSdk);
    }

    @Override
    void fillDependencyPart(StringBuilder stringBuilder) {
        StringBuilder builder = new StringBuilder();
        Set<String> addedDependency = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : depList.entrySet()) {
            if (entry.getKey().equals("Push") || entry.getKey().equals("Map")) {
                String kitName = entry.getKey();
                builder.append("//").append(kitName).append(System.lineSeparator());
                for (String s : entry.getValue()) {
                    if (!addedDependency.contains(s)) {
                        addedDependency.add(s);
                        builder.append("    compileOnly ")
                            .append("'").append(s).append("'").append(System.lineSeparator());
                    }
                }
            }
        }
        anchorReplace(stringBuilder, "${DEPENDENCY}", builder.toString());
    }

    @Override
    void copyManifest() {}
}