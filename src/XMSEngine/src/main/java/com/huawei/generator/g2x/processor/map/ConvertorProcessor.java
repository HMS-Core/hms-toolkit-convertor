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
import com.huawei.generator.g2x.po.map.convertor.GSummaryMap;
import com.huawei.generator.g2x.po.map.convertor.JDesc;
import com.huawei.generator.g2x.po.map.manual.Manual;
import com.huawei.generator.g2x.po.map.manual.ManualClass;
import com.huawei.generator.g2x.po.map.manual.ManualField;
import com.huawei.generator.g2x.po.map.manual.ManualMethod;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.gen.InvalidJsonException;
import com.huawei.generator.json.JClass;
import com.huawei.generator.utils.G2XMappingUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class for processing convertor
 *
 * @since 2020-04-07
 */
public final class ConvertorProcessor extends BaseProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertorProcessor.class);

    private ConvertorProcessor(ConvertorProcessorBuilder builder) {
        super(builder);
    }

    private static void generateClasses2DesMap(Auto auto, Manual manual, GSummaryMap map) {
        for (ManualClass clazz : manual.getManualClasses()) {
            map.getClass2DescMap().put(clazz.getClassName(), new JDesc(clazz.getDesc()));
        }
        for (AutoClass clazz : auto.getAutoClasses()) {
            map.getClass2DescMap().put(clazz.getOldClassName(), new JDesc(clazz.getDesc()));
        }
    }

    private static void generateMethods2DesMap(Auto auto, Manual manual, GSummaryMap map) {
        for (ManualMethod method : manual.getManualMethods()) {
            map.getMethod2DescMap()
                .put(G2XMappingUtils.simplifySignature(method.getDesc().getMethodName()), new JDesc(method.getDesc()));
        }
        for (AutoMethod method : auto.getAutoMethods()) {
            map.getMethod2DescMap()
                .put(G2XMappingUtils.simplifySignature(method.getDesc().getMethodName()), new JDesc(method.getDesc()));
        }
    }

    private static void generateFields2DesMap(Auto auto, Manual manual, GSummaryMap map) {
        for (ManualField field : manual.getManualFields()) {
            map.getField2DescMap().put(field.getFieldName(), new JDesc(field.getDesc()));
        }
        for (AutoField field : auto.getAutoFields()) {
            map.getField2DescMap().put(field.getOldFieldName(), new JDesc(field.getDesc()));
        }
    }

    public GSummaryMap processAllForConvertorSummary() throws InvalidJsonException {
        Auto auto = new Auto();
        Manual manual = new Manual();
        GSummaryMap map = new GSummaryMap();

        if (resolveAllClasses(auto, manual) != GeneratorResult.SUCCESS) {
            return new GSummaryMap();
        }

        if (super.allJsonValid) {
            generateClasses2DesMap(auto, manual, map);
            generateMethods2DesMap(auto, manual, map);
            generateFields2DesMap(auto, manual, map);
        } else {
            LOGGER.error("Found invalid json, generation aborted.");
            throw new InvalidJsonException();
        }
        return map;
    }

    @Override
    void process(ZipEntry entry, ZipFile zipFile, Auto auto, Manual manual) throws IOException {
        if (entry.getName().startsWith("xms/json") && entry.getName().endsWith(".json")) {
            JClass jClass = getJClassFromEntry(entry, zipFile);
            String[] pathStrs = getJsonPathFromEntry(entry,jClass);
            String kitName = pathStrs[pathStrs.length - 3];
            String dependencyName = kitName;

            // ml should be separated , the same as push
            if (kitName.equals("ml")) {
                dependencyName = dependencyName + pathStrs[pathStrs.length - 2];
            }
            String version = "";
            if (allJsonValid) {
                fillResult(auto, manual, jClass, kitName, dependencyName, version);
            }
        }
    }

    public static class ConvertorProcessorBuilder extends ProcessorBuilder {
        public ConvertorProcessorBuilder(String pluginPath) {
            super(pluginPath);
        }

        public ConvertorProcessorBuilder dollar(boolean dollar) {
            super.setDollar(dollar);
            return this;
        }

        public ConvertorProcessor build() {
            return new ConvertorProcessor(this);
        }
    }
}
