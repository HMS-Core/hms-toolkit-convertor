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

import com.huawei.generator.g2x.po.map.auto.Auto;
import com.huawei.generator.g2x.po.map.auto.AutoClass;
import com.huawei.generator.g2x.po.map.auto.AutoField;
import com.huawei.generator.g2x.po.map.auto.AutoMethod;
import com.huawei.generator.g2x.po.map.extension.G2XExtension;
import com.huawei.generator.g2x.po.map.manual.Manual;
import com.huawei.generator.g2x.po.map.manual.ManualClass;
import com.huawei.generator.g2x.po.map.manual.ManualField;
import com.huawei.generator.g2x.po.map.manual.ManualMethod;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.XmsConstants;
import com.huawei.generator.gen.InvalidJsonException;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.Parser;
import com.huawei.generator.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class for processing map
 *
 * @since 2020-04-07
 */
public final class MapProcessor extends Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapProcessor.class);

    private String outPath;

    private Map<String, String> currentVersion;

    private MapProcessor(MapProcessorBuilder builder) {
        super(builder);
        this.outPath = builder.outPath;
        this.currentVersion = builder.currentVersion;
    }

    /**
     * process all configs
     * 
     * @return generatorResult describes the result message
     * @throws InvalidJsonException if any invalid json file exists.
     */
    public GeneratorResult processAllTarget() throws InvalidJsonException {
        GeneratorResult generatorResult;
        Auto auto = new Auto();
        Manual manual = new Manual();

        generatorResult = super.resolveAllClasses(auto, manual);

        if (generatorResult != GeneratorResult.SUCCESS) {
            return generatorResult;
        }

        // if json has a defect, stop
        if (!allJsonValid) {
            LOGGER.error("Found invalid json, generation aborted.");
            throw new InvalidJsonException();
        }

        MapPatcher.patchMap(auto, manual);

        // add g2XExtension for unsupported gms or firebase
        G2XExtension g2XExtension = resolveG2XExtension();
        manual.getManualPackages().addAll(g2XExtension.getManualPackages());

        // sort items
        sortAuto(auto);
        sortManual(manual);

        generatorResult = FileUtils.outPutJson(auto, outPath, "wisehub-auto");
        if (generatorResult != GeneratorResult.SUCCESS) {
            return generatorResult;
        }

        generatorResult = FileUtils.outPutJson(manual, outPath, "wisehub-manual");
        if (generatorResult != GeneratorResult.SUCCESS) {
            return generatorResult;
        }

        // generate trust list for code analyze engine
        List<String> result = MapPatcher.findSameMethod(super.autoGMethodList, super.manualGMethodList);
        LOGGER.debug("trustList");
        for (String str : result) {
            // print trustList
            LOGGER.debug(str);
        }
        return generatorResult;
    }

    private G2XExtension resolveG2XExtension() {
        InputStream inputStream = MapProcessor.class.getResourceAsStream("/" + XmsConstants.G2X_MANUAL_EXTENSION);
        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return Parser.parseEx(isr);
    }

    // sort fields in auto class
    private void sortAuto(Auto auto) {
        auto.getAutoClasses().sort(Comparator.comparing(AutoClass::getOldClassName));
        auto.getAutoFields().sort(Comparator.comparing(AutoField::getOldFieldName));
        auto.getAutoMethods().sort(Comparator.comparing(AutoMethod::getOldMethodName));
    }

    // sort fields in manual class
    private void sortManual(Manual manual) {
        manual.getManualClasses().sort(Comparator.comparing(ManualClass::getClassName));
        manual.getManualFields().sort(Comparator.comparing(ManualField::getFieldName));
        manual.getManualMethods().sort(Comparator.comparing(ManualMethod::getMethodName));
    }

    @Override
    void process(ZipEntry entry, ZipFile zipFile, Auto auto, Manual manual) throws IOException {
        if ((entry.getName().startsWith("xms/json") || entry.getName().startsWith("xms/agc-json")
            || entry.getName().startsWith("xms/special_json")) && entry.getName().endsWith(".json")) {
            JClass jClass = getJClassFromEntry(entry, zipFile);
            String[] pathStrs = getJsonPathFromEntry(entry, jClass);
            String kitname = pathStrs[2];
            String dependencyName = kitname;
            if (this.currentVersion.containsKey(kitname)
                && !entry.getName().contains(this.currentVersion.get(kitname))) {
                return;
            }

            // mlfirebase and mlgms should be separated , the same as push
            if (kitname.equals("mlgms") || kitname.equals("mlfirebase")) {
                dependencyName = kitname;
                kitname = "ml";
            }

            // firebase json in gms will be excluded
            if (kitname.equals("firebase")) {
                return;
            }
            if (allJsonValid) {
                fillResult(auto, manual, jClass, kitname, dependencyName);
            }
        }
        if (entry.getName().startsWith("xms/common") && entry.getName().endsWith(".json")) {
            JClass jClass = getJClassFromEntry(entry, zipFile);
            Map<String, String> paramMap =
                buildParamMap(jClass, XmsConstants.SERIALIZATION_KIT_NAME, XmsConstants.SERIALIZATION_KIT_NAME);
            fillMethod(auto, manual, jClass, paramMap);
        }
        if (entry.getName().startsWith("xms/unsupport") && entry.getName().endsWith(".json")) {
            JClass jClass = getJClassFromEntry(entry, zipFile);
            String[] pathStrs = entry.getName().split("/");
            String kitname = pathStrs[2];
            Map<String, String> paramMap = buildParamMap(jClass, kitname, kitname);
            fillMethod(auto, manual, jClass, paramMap);
            fillField(auto, manual, jClass, paramMap);
        }
    }

    public static class MapProcessorBuilder extends ProcessorBuilder {
        private String outPath;

        private Map<String, String> currentVersion;

        public MapProcessorBuilder(String pluginPath, String outPath, Map<String, String> currentVersion) {
            super(pluginPath);
            this.outPath = outPath;
            this.currentVersion = currentVersion;
        }

        public MapProcessorBuilder unGenerified(boolean unGenerified) {
            super.setUnGenerified(unGenerified);
            return this;
        }

        public MapProcessorBuilder withClassName(boolean withClassName) {
            super.setWithClassName(withClassName);
            return this;
        }

        public MapProcessor build() {
            return new MapProcessor(this);
        }
    }
}
