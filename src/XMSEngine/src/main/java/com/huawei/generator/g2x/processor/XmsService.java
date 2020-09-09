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

import static com.huawei.generator.g2x.processor.XmsConstants.XG_MODULE_NAME;
import static com.huawei.generator.g2x.processor.XmsConstants.XH_MODULE_NAME;
import static com.huawei.generator.g2x.processor.XmsConstants.XMS_MODULE_NAME;

import com.huawei.generator.g2x.po.summary.Diff;
import com.huawei.generator.g2x.po.summary.Summary;
import com.huawei.generator.g2x.processor.map.Validator;
import com.huawei.generator.g2x.processor.module.ModuleGenerator;
import com.huawei.generator.g2x.processor.module.ParamKind;
import com.huawei.generator.g2x.processor.module.XmsAdaptorGenerator;
import com.huawei.generator.g2x.processor.module.XmsRootGenerator;
import com.huawei.generator.g2x.processor.module.XmsXapiGenerator;
import com.huawei.generator.g2x.processor.module.XmsXgGenerator;
import com.huawei.generator.g2x.processor.module.XmsXhGenerator;
import com.huawei.generator.gen.RuntimeTypeMappings;
import com.huawei.generator.mirror.KClassReader;
import com.huawei.generator.utils.EnhancerUtils;
import com.huawei.generator.utils.FileUtils;
import com.huawei.generator.utils.KitInfoRes;
import com.huawei.generator.utils.SummaryPathUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * New entry of xms-router
 * Path
 * xmsadapter: g+h
 * src -> main -> java -> org -> xms
 * g+h / g
 * src -> xmsgh -> java -> org -> xms
 * src -> xmsg -> java -> org -> xms
 * Summary:
 * 1. add thirdSDK?
 * 2. add strategy
 * Diff:
 * add short list
 *
 * @since 2020-02-26
 */
public class XmsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmsService.class);

    private static final Map<GeneratorStrategyKind, String> STRATEGY_MODULE_MAP = new HashMap<>();

    private static final int BUFFER_SIZE = 1024;

    private static final int PLUGIN_PATH = 0;

    private static final int BACKUP_PATH = 1;

    private static final int TARGET_PATH = 2;

    static {
        STRATEGY_MODULE_MAP.put(GeneratorStrategyKind.G, XMS_MODULE_NAME);
        STRATEGY_MODULE_MAP.put(GeneratorStrategyKind.GOrH, XMS_MODULE_NAME);
        STRATEGY_MODULE_MAP.put(GeneratorStrategyKind.HOrG, XMS_MODULE_NAME);
        STRATEGY_MODULE_MAP.put(GeneratorStrategyKind.XG, XG_MODULE_NAME);
        STRATEGY_MODULE_MAP.put(GeneratorStrategyKind.XH, XH_MODULE_NAME);
    }

    /**
     * A generate support kitname info
     * 
     * @return list of supported kits' information
     */
    public static Set<String> supportKitInfo() {
        return new HashSet<>(KitInfoRes.INSTANCE.getSupportList());
    }

    /**
     * B create a new module and generate diff
     *
     * @param processorUtils should be build with
     *        backPath/pluginPath/targetPath/kitMap/allDepMap/strategyKindList/thirdSDK/needClassloader
     * @return generate result
     */
    public static GenerateSummary create(ProcessorUtils processorUtils) {
        Map<String, String> currentVersion = processorUtils.getKitVersionMap();
        KClassReader.INSTANCE.generateGmsClassList(currentVersion, processorUtils.getPluginPath());
        RuntimeTypeMappings.generateMappingRelation(currentVersion, processorUtils.getPluginPath());

        LOGGER.info("create: param");
        LOGGER.info("backPath:{}", processorUtils.getBackPath());
        LOGGER.info("pluginPath:{}", processorUtils.getPluginPath());
        LOGGER.info("targetPath:{}", processorUtils.getTargetPath());
        LOGGER.info("kitMap:{}", processorUtils.getKitMap().toString());
        LOGGER.info("allDepMap:{}:", processorUtils.getAllDepMap().toString());
        LOGGER.info("kindList:{}", processorUtils.getStrategyKindList().toString());
        LOGGER.info("SDK:{}", processorUtils.isThirdSDK());
        LOGGER.info("gmsVersionMap:{}", processorUtils.getGmsVersionMap().toString());
        LOGGER.info("needClassloader:{}", processorUtils.getNeedClassLoader());
        LOGGER.info("validate kitMap and allDepMap");

        Map<ParamKind, String> paths = new HashMap<>();
        paths.put(ParamKind.PLUGIN_PATH, processorUtils.getPluginPath());
        paths.put(ParamKind.BACKUP_PATH, processorUtils.getBackPath());
        paths.put(ParamKind.TARGET_PATH, processorUtils.getTargetPath());
        Map<ParamKind, Summary> summaries = new HashMap<>();
        Summary newSummary = new Summary();
        Summary oldSummary = null;
        if (processorUtils.getBackPath() != null) {
            oldSummary = resolveOldSummary(processorUtils.getBackPath());
        }
        Summary localFileSummary = new Summary();
        EnhancerUtils.inferSummary(
                localFileSummary,
                String.join(File.separator, processorUtils.getBackPath(), XmsConstants.XMS_MODULE_NAME),
                processorUtils.getPluginPath());
        summaries.put(ParamKind.NEW_SUMMARY, newSummary);
        summaries.put(ParamKind.OLD_SUMMARY, oldSummary);
        summaries.put(ParamKind.LOCAL_SUMMARY, localFileSummary);
        GenerateSummary generateSummary = new GenerateSummary();

        ProcessorUtils newUtils =
                new ProcessorUtils
                        .Builder(processorUtils)
                        .setPathMap(paths)
                        .setSummaries(summaries)
                        .setSummary(generateSummary)
                        .build();
        Validator.validKits(newUtils.getKitMap());
        ModuleGenerator moduleGenerator = new XmsAdaptorGenerator(newUtils);
        moduleGenerator.createModule();

        // classloader
        if (newUtils.getNeedClassLoader() && !newUtils.isThirdSDK()) {
            XmsRootGenerator rootModule = new XmsRootGenerator(newUtils);
            rootModule.createModule();
            XmsXapiGenerator xmsAdaptorGenerator = new XmsXapiGenerator(newUtils);
            xmsAdaptorGenerator.createModule();
            XmsXgGenerator xmsXgGenerator = new XmsXgGenerator(newUtils);
            xmsXgGenerator.createModule();
            XmsXhGenerator xmsXhGenerator = new XmsXhGenerator(newUtils);
            xmsXhGenerator.createModule();
        }
        EnhancerUtils.inferSummary(newSummary,
                String.join(File.separator, newUtils.getTargetPath(), XMS_MODULE_NAME),
                newUtils.getPluginPath());
        FileUtils.outPutJson(newSummary, moduleGenerator.getSummaryPath(), "summary");
        LOGGER.info("Summary generated in:{}, fileSize:{}:", moduleGenerator.getSummaryPath(),
                newSummary.allFiles.size());

        // backPath is null leads to a new generation,diff is null
        if (newUtils.getBackPath() == null || newUtils.getBackPath().isEmpty()) {
            return generateSummary;
        }

        // backPath is not null leads to a generation with diff build
        Diff diff = buildDiff(newSummary, oldSummary, localFileSummary);
        LOGGER.info("Diff generated mod{},update{},add{},del{}", diff.getModMap().size(), diff.getUpdatedMap().size(),
                diff.getAddList().size(), diff.getDelList().size());
        if (diff.isChanged()) {
            generateSummary.setDiff(diff);
        }
        return generateSummary;
    }

    /**
     * update a new module
     *
     * @param basePath project path
     * @return updated result
     */
    public static GenerateSummary update(String basePath) throws IOException {
        ProcessorUtils.Builder builder = new ProcessorUtils.Builder();
        builder.setPluginPath("pluginPath"); //plugin
        builder.setTargetPath("targetPath"); //plugin
        builder.setBackPath("backPath");     //plugin
        builder.setKitMap(XmsKitsInfo.getKitMap(basePath));
        builder.setAllDepMap(new HashMap<>());//plugin
        builder.setStrategyKindList(XmsKitsInfo.getStrategyKindList(basePath));
        builder.setThirdSDK(XmsKitsInfo.isSDK(basePath));
        builder.setGmsVersionMap(XmsKitsInfo.getGmsVersionMap(basePath));
        builder.setNeedClassLoader(XmsKitsInfo.needClassLoader(basePath));
        ProcessorUtils pu = new ProcessorUtils(builder);
        return create(pu);
    }

    private static Diff buildDiff(Summary newSummary, Summary oldSummary, Summary localFileSummary) {
        // current - local ==> into diff
        Diff diff = newSummary.diffWithAllFiles(localFileSummary, false);
        if (oldSummary == null) {
            return diff;
        }

        // for each x in mod, if old equals local, move to update, for x is xms-self update
        List<String> mods = new ArrayList<>(diff.getModMapRelativePaths().keySet());
        for (String mod : mods) {
            if (oldSummary.allFiles.containsKey(mod) && localFileSummary.allFiles.containsKey(mod)) {
                // check equals
                String oldSha = oldSummary.allFiles.get(mod);
                String localSha = localFileSummary.allFiles.get(mod);
                if (oldSha.equals(localSha)) {
                    // move to update
                    diff.getUpdatedMapRelativePaths().put(mod, mod);
                    diff.getUpdatedMap()
                        .put(newSummary.moduleLocation + File.separator + mod,
                            localFileSummary.moduleLocation + File.separator + mod);

                    // del from mod
                    diff.getModMapRelativePaths().remove(mod);
                    diff.getModMap().remove(newSummary.moduleLocation + File.separator + mod);
                }
            }
        }

        return diff;
    }

    /**
     * C create a new module without g/h first and generate diff
     *
     * @param processorUtils should be build with
     *        pluginPath/oldPath/newPath/kitMap/allDepMap/useOnlyG/thirdSDK/needClassloader
     * @return result
     */
    public static GenerateSummary createWithoutFirstStrategy(ProcessorUtils processorUtils) {
        LOGGER.info("createWithoutFirstStrategy: param");
        LOGGER.info("backPath:{}", processorUtils.getOldPath());
        LOGGER.info("pluginPath:{}", processorUtils.getPluginPath());
        LOGGER.info("targetPath:{}", processorUtils.getNewPath());
        LOGGER.info("kitMap:{}", processorUtils.getKitMap().toString());
        LOGGER.info("allDepMap:{}:", processorUtils.getAllDepMap().toString());
        LOGGER.info("useOnlyG:{}", processorUtils.isUseOnlyG());
        LOGGER.info("SDK:{}", processorUtils.isThirdSDK());

        GeneratorStrategyKind strategyKind = XmsAdaptorGenerator.inferGHFirst(processorUtils.getOldPath());
        LOGGER.info("inferGHFirst:{}", strategyKind);

        List<GeneratorStrategyKind> strategyKindList = new LinkedList<>(Collections.singletonList(strategyKind));
        if (processorUtils.isUseOnlyG()) {
            strategyKindList.add(GeneratorStrategyKind.G);
        }
        LOGGER.info("strategyKindList:{}", strategyKindList.toString());
        return create(processorUtils);
    }

    /**
     * D return summary for IDE to produce sdk/app and strategy
     *
     * @param pluginPath plugin path
     * @param rootPath path/xmsadapter
     * @return summary, null for fail
     */
    public static Summary inferStrategy(String pluginPath, String rootPath) {
        // find summary.json
        List<File> files = new ArrayList<>();
        FileUtils.findFileByName(new File(rootPath), "summary.json", files);

        if (files.isEmpty()) {
            return null;
        }
        File file = files.get(0);
        return SummaryPathUtils.buildSummaryFromJson(file);
    }

    static Summary resolveOldSummary(String backPath) {
        List<File> oldSummaryFiles = new ArrayList<>();
        FileUtils.findFileByName(new File(backPath), "summary.json", oldSummaryFiles);
        Summary oldSummary = null;
        if (oldSummaryFiles.size() > 0) {
            File oldSummaryFile = oldSummaryFiles.get(0);
            oldSummary = SummaryPathUtils.buildSummaryFromJson(oldSummaryFile);
        }

        LOGGER.info("OldSummary read from:{}:", backPath);
        LOGGER.info("OldSummary:{}, FileSize：{}", oldSummary, oldSummary == null ? "null" : oldSummary.allFiles.size());

        // reset oldSummaryFile#moduleLocation, for it has been move to backpath
        if (oldSummary != null) {
            oldSummary.moduleLocation = backPath;
        }
        return oldSummary;
    }
}