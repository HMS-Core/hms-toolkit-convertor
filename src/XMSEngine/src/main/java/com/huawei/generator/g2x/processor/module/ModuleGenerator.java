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
import com.huawei.generator.g2x.processor.ProcessorUtils;
import com.huawei.generator.g2x.processor.javadoc.Processor;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    protected Map<String, String> kitVersionMap;

    public ModuleGenerator(ProcessorUtils processorUtils) {
        this.pluginPath = processorUtils.getPathMap().get(ParamKind.PLUGIN_PATH);
        this.backPath = processorUtils.getPathMap().get(ParamKind.BACKUP_PATH);
        this.targetPath = processorUtils.getPathMap().get(ParamKind.TARGET_PATH);
        this.oldSummary = processorUtils.getSummaries().get(ParamKind.OLD_SUMMARY);
        this.newSummary = processorUtils.getSummaries().get(ParamKind.NEW_SUMMARY);
        this.localFileSummary = processorUtils.getSummaries().get(ParamKind.LOCAL_SUMMARY);
        this.kitSet = resolveKitList(processorUtils.getKitMap());
        this.generateSummary = processorUtils.getSummary();
        this.kitVersionMap = processorUtils.getKitVersionMap();
    }

    /**
     * split allDepMap into g and h dependency list separately
     *
     * @param kitMap kitName maps to ADD / REMOVE
     * @return generate result
     */
    protected Set<String> resolveKitList(Map<String, String> kitMap) {
        Validator.validKits(kitMap);
        Set<String> oldKits = new HashSet<>();
        Set<String> delKits = new HashSet<>();
        Set<String> addKits = new HashSet<>();
        kitMap.forEach((kitName, status) -> {
            if ("ADD".equalsIgnoreCase(status)) {
                addKits.add(kitName);
            }
            if ("REMOVE".equalsIgnoreCase(status)) {
                delKits.add(kitName);
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

    protected void createManifestFile() {
        String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        String str =
            header + System.lineSeparator() + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\""
                + System.lineSeparator() + "\tpackage=\"org.xms\">" + System.lineSeparator() + System.lineSeparator()
                + "\t<!--For example if you build xmsadapter module and use account kit in your code, "
                + "the following permission should be added-->" + System.lineSeparator()
                + "\t<!--uses-permission android:name=\"android.permission.MANAGE_ACCOUNTS\"-->"
                + System.lineSeparator() + System.lineSeparator() + "</manifest>";
        FileUtils.createFile(str, manifestPath, "AndroidManifest.xml");
    }

    /**
     * generate Code
     *
     * @return success
     */
    abstract boolean generateCode();

    /**
     * create Module
     */
    public abstract void createModule();

    /**
     * generate Gradle
     *
     * @return success
     */
    abstract boolean generateGradle();

    /**
     * fill apply Part
     *
     * @param stringBuilder total string
     */
    abstract void fillApplyPart(StringBuilder stringBuilder);

    /**
     * fill Android Part
     *
     * @param stringBuilder total string
     */
    abstract void fillAndroidPart(StringBuilder stringBuilder);

    /**
     * fill Dependency Part
     *
     * @param stringBuilder total string
     */
    abstract void fillDependencyPart(StringBuilder stringBuilder);

    /**
     * copy Manifest
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
        G2HTables.openInnerBlockList();
        // copy value
        List<String> localDependencies = new LinkedList<>(dependencies);
        localDependencies.replaceAll(x -> x = G2XMappingUtils.unNormalizeKitName(x));
        GeneratorResult result = Validator.validateParam(xmsLocation, summaryPath, pluginPath, localDependencies);
        if (result != GeneratorResult.SUCCESS) {
            return result;
        }

        // generate xms code
        GeneratorBuilder builder = new GeneratorBuilder(pluginPath, xmsLocation)
            .strategy(localDependencies, GeneratorConfiguration.getConfiguration(strategyKind))
            .version(kitVersionMap);
        Generator generator = builder.build().initGeneratorForRouter();
        generator.generate();

        // generated file list
        List<File> generatedFiles = generator.getGeneratedFiles();

        // unzip javadoc web
        Processor.unZipFilesJar(pluginPath);

        // utils copy
        XModuleGenerator xModuleGenerator = new XModuleGenerator(new File(xmsLocation), generatedFiles);
        xModuleGenerator.generateModule(true, strategyKind, builder.getOriginKitList());
        return GeneratorResult.SUCCESS;
    }

    protected static void addSpace(StringBuilder stringBuilder, int spaceLength) {
        for (int i = 0; i < spaceLength; i++) {
            stringBuilder.append(" ");
        }
    }

    protected void anchorReplace(StringBuilder builder, String anchorStr, String replaceStr) {
        int index = builder.lastIndexOf(anchorStr);
        if (index > 0) {
            builder.replace(index, index + anchorStr.length(), replaceStr);
        }
    }

    protected StringBuilder readGradle() {
        String targetFile = String.join(File.separator, modulePath, "build.gradle");
        try (BufferedReader bufReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(new File(targetFile)), StandardCharsets.UTF_8))) {
            StringBuilder stringBuilder = new StringBuilder();
            String temp = bufReader.readLine();
            while (temp != null) {
                stringBuilder.append(temp);
                stringBuilder.append(System.lineSeparator());
                temp = bufReader.readLine();
            }
            return stringBuilder;
        } catch (FileNotFoundException e) {
            LOGGER.error("Gradle file does not exist!");
        } catch (IOException e) {
            LOGGER.error("Close resource failed when reading Gradle file!");
        }
        return new StringBuilder();
    }

    public String getSummaryPath() {
        return summaryPath;
    }
}
