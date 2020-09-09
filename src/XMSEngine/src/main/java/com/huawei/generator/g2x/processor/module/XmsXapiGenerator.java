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
import com.huawei.generator.utils.FileUtils;
import com.huawei.generator.utils.KitInfoRes;
import com.huawei.generator.utils.StaticPatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for XmsXapiGenerator
 *
 * @since 2020-04-07
 */
public final class XmsXapiGenerator extends ModuleGenerator {
    private String generatePath;

    public XmsXapiGenerator(ProcessorUtils processorUtils) {
        super(processorUtils);
        generatePath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME);

        modulePath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME,
            XmsConstants.XMS_SUBMODULE_NAME, "xapi");
        summaryPath = String.join(File.separator, targetPath, "config");
        manifestPath = String.join(File.separator, modulePath, "src", "main");
    }

    @Override
    void resolveDepMap(Map<String, Set<String>> allDepMap) {
        kitSet.forEach(kit -> {
            if (allDepMap.get(kit) != null) {
                depList.put(kit, allDepMap.get(kit));
            }
        });
    }

    @Override
    boolean generateCode() {
        List<String> excludes = new ArrayList<>();
        if (!kitSet.contains("Push")) {
            excludes.add("xms/xmsaux/xapi/src/main/java/org/xms/f");
        }
        if (!kitSet.contains("Map")) {
            excludes.add("xms/xmsaux/xapi/src/main/java/org/xms/g");
        }

        StaticPatcher.copyResourceDir(pluginPath, generatePath, "xms/xmsaux/xapi", excludes);
        return true;
    }

    @Override
    public void createModule() {
        generateCode();
        generateGradle();
        copyManifest();
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
    void fillApplyPart(StringBuilder stringBuilder) {}

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
    void fillDependencyPart(StringBuilder stringBuilder) {}

    @Override
    void copyManifest() {}
}