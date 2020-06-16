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

import com.huawei.generator.g2x.po.summary.Summary;
import com.huawei.generator.g2x.processor.GenerateSummary;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.g2x.processor.map.Validator;
import com.huawei.generator.gen.Generator;
import com.huawei.generator.gen.GeneratorBuilder;
import com.huawei.generator.gen.GeneratorConfiguration;
import com.huawei.generator.gen.XModuleGenerator;
import com.huawei.generator.utils.FileUtils;
import com.huawei.generator.utils.G2HTables;
import com.huawei.generator.utils.G2XMappingUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for generate module
 *
 * @since 2020-04-07
 */
public abstract class ModuleGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleGenerator.class);

    protected String pluginPath;

    protected String modulePath;

    protected String summaryPath;

    protected String manifestPath;

    protected String backPath;

    protected String targetPath;

    protected Summary oldSummary;

    protected Summary newSummary;

    protected Summary localFileSummary;

    protected Set<String> kitSet;

    protected Map<String, Set<String>> depList = new HashMap<>();

    protected GenerateSummary generateSummary;

    public ModuleGenerator(Map<ParamKind, String> pathMap, Map<String, String> kitMap,
        Map<ParamKind, Summary> summaries, GenerateSummary generateSummary) {
        this.pluginPath = pathMap.get(ParamKind.PLUGIN_PATH);
        this.backPath = pathMap.get(ParamKind.BACKUP_PATH);
        this.targetPath = pathMap.get(ParamKind.TARGET_PATH);
        this.oldSummary = summaries.get(ParamKind.OLD_SUMMARY);
        this.newSummary = summaries.get(ParamKind.NEW_SUMMARY);
        this.localFileSummary = summaries.get(ParamKind.LOCAL_SUMMARY);
        this.kitSet = resolveKitList(kitMap);
        this.generateSummary = generateSummary;
    }

    /**
     * split allDepMap into g and h dependency list separately
     *
     * @param kitMap kitName -> Add / Del
     * @return generate result
     */
    protected Set<String> resolveKitList(Map<String, String> kitMap) {
        Validator.validKits(kitMap);
        Set<String> oldKits = new HashSet<>();
        Set<String> delKits = new HashSet<>();
        Set<String> addKits = new HashSet<>();
        kitMap.forEach((x, y) -> {
            if ("ADD".equalsIgnoreCase(y)) {
                addKits.add(x);
            }
            if ("REMOVE".equalsIgnoreCase(y)) {
                delKits.add(x);
            }
        });

        if (oldSummary != null) {
            oldKits = oldSummary.getKits();
        }

        oldKits.addAll(addKits);
        oldKits.removeAll(delKits);
        return oldKits;
    }

    /**
     * resolveDepMap
     *
     * @param allDepMap kit -> list<dependency>
     */
    abstract void resolveDepMap(Map<String, Set<String>> allDepMap);

    protected boolean createManifestFile() {
        String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        String str =
            header + System.lineSeparator() + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\""
                + System.lineSeparator() + "\tpackage=\"org.xms\">" + System.lineSeparator() + System.lineSeparator()
                + "\t<!--For example if you build xmsadapter module and use account kit in your code, "
                + "the following permission should be added-->" + System.lineSeparator()
                + "\t<!--uses-permission android:name=\"android.permission.MANAGE_ACCOUNTS\"-->"
                + System.lineSeparator() + System.lineSeparator() + "</manifest>";
        FileUtils.createFile(str, manifestPath, "AndroidManifest.xml");
        return true;
    }

    /**
     * generateCode
     *
     * @return sucess
     */
    abstract boolean generateCode();

    /**
     * createModule
     */
    public abstract void createModule();

    /**
     * generateGradle
     *
     * @return success
     */
    abstract boolean generateGradle();

    /**
     * fillApplyPart
     *
     * @param stringBuilder total string
     */
    abstract void fillApplyPart(StringBuilder stringBuilder);

    /**
     * fillAndroidPart
     *
     * @param stringBuilder total string
     */
    abstract void fillAndroidPart(StringBuilder stringBuilder);

    /**
     * fillDependencyPart
     *
     * @param stringBuilder total string
     */
    abstract void fillDependencyPart(StringBuilder stringBuilder);

    /**
     * copyManifest
     */
    abstract void copyManifest();

    protected GeneratorResult primaryGenerate(String pluginPath, String summaryPath, String xmsLocation,
        List<String> dependencies, GeneratorStrategyKind strategyKind) {
        LOGGER.info("primaryGenerate: param");
        LOGGER.info("pluginPath:{}", pluginPath);
        LOGGER.info("summaryPath:{}", summaryPath);
        LOGGER.info("xmsLocation:{}", xmsLocation);
        LOGGER.info("dependencies:{}:", dependencies.toString());
        LOGGER.info("strategyKind:{}", strategyKind.toString());
        G2HTables.openInnerBlackList();

        // copy value
        List<String> localDependencies = new LinkedList<>(dependencies);
        localDependencies.replaceAll(x -> x = G2XMappingUtils.unNormalizeKitName(x));
        GeneratorResult result = Validator.validateParam(xmsLocation, summaryPath, pluginPath);
        if (result != GeneratorResult.SUCCESS) {
            return result;
        }

        // generate xms code
        GeneratorBuilder builder = new GeneratorBuilder(pluginPath, xmsLocation).strategy(localDependencies,
            GeneratorConfiguration.getConfiguration(strategyKind));
        Generator generator = builder.build().initGeneratorForRouter();
        generator.generate();

        // generated file list
        List<File> generatedFiles = generator.getGeneratedFiles();

        // copy utils
        XModuleGenerator xModuleGenerator = new XModuleGenerator(new File(xmsLocation), generatedFiles);
        xModuleGenerator.generateModule(true, strategyKind, builder.getStandardKitList());
        return GeneratorResult.SUCCESS;
    }

    protected static void addSpace(StringBuilder stringBuilder, int spaceLength) {
        for (int i = 0; i < spaceLength; i++) {
            stringBuilder.append(" ");
        }
    }

    public String getSummaryPath() {
        return summaryPath;
    }
}
