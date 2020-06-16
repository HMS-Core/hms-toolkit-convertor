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

package com.huawei.generator.gen;

import com.google.gson.Gson;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.json.JClass;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test for XH
 *
 * @since 2019-11-14
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GeneratorXHTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorTest.class);

    private static final String USR_DIR = System.getProperty("user.dir");

    private static final GeneratorStrategyKind GENERATOR_STRATEGY_KIND = GeneratorStrategyKind.XH;

    @BeforeClass
    public static void globalSetup() {
        generateProject();
    }

    private static void generateProject() {
        new XModuleGenerator(new File(targetRoot()), new ArrayList<>()).generateModule(false, GENERATOR_STRATEGY_KIND,
            null);
    }

    @Before
    public void setup() {
        enableBlackList();
    }

    private void enableBlackList() {
        System.setProperty("enable_black_list", "true");
    }

    private static String targetRoot() {
        return "xh";
    }

    private ApiStats testGenerate(Set<String> kits) {
        String outPath = String.join(File.separator, USR_DIR, targetRoot(), "src", "main", "java");
        GeneratorConfiguration configuration = GeneratorConfiguration.getConfiguration(GENERATOR_STRATEGY_KIND);
        Generator generator = new Generator(kits, outPath, configuration);
        return generator.generate();
    }

    @Test
    public void testGenerateCore() {
        String kit = KitNames.FRAMEWORK;
        Set<String> kits = new HashSet<>();
        kits.add(kit);
        ApiStats stats = testGenerate(kits);
        stats.setTotalFilterd(stats.getFilterByWild());
        stats.setGenerated(stats.getTotal() - stats.getTotalFilterd() - stats.getFakeApis());
        printStats(kit, stats);
        saveAndUpdateStats(kit, stats);
    }

    @Test
    public void testGenerateNearby() {
        generateWithStats(KitNames.NEARBY, KitNames.FRAMEWORK);
    }

    @Test
    public void testGenerateLocation() {
        generateWithStats(KitNames.LOCATION, KitNames.FRAMEWORK);
    }

    @Test
    public void zzzTestAccount() {
        generateWithStats(KitNames.ACCOUNT, KitNames.FRAMEWORK);
    }

    @Test
    public void testPanorama() {
        generateWithStats(KitNames.PANORAMA, KitNames.FRAMEWORK);
    }

    @Test
    public void testSafety() {
        generateWithStats(KitNames.SAFETY, KitNames.FRAMEWORK);
    }

    @Test
    public void testGenerateMap() {
        generateWithStats(KitNames.MAPS, KitNames.FRAMEWORK, KitNames.LOCATION);
    }

    @Test
    public void testGenerateAnalytics() {
        generateWithStats(KitNames.ANALYTICS, KitNames.FRAMEWORK);
    }

    @Test
    public void testGeneratePush() {
        generateWithStats(KitNames.PUSH, KitNames.FRAMEWORK, KitNames.FIREBASE);
    }

    @Test
    public void testGenerateIdentity() {
        generateWithStats(KitNames.IDENTITY, KitNames.FRAMEWORK);
    }

    @Test
    public void testAwareness() {
        generateWithStats(KitNames.AWARENESS, KitNames.FRAMEWORK, KitNames.LOCATION, KitNames.SITE);
    }

    @Test
    public void testWallet() {
        generateWithStats(KitNames.WALLET, KitNames.FRAMEWORK, KitNames.IDENTITY, KitNames.MAPS, KitNames.ACCOUNT);
    }

    @Test
    public void testGenerateHealth() {
        generateWithStats(KitNames.HEALTH, KitNames.FRAMEWORK, KitNames.ACCOUNT);
    }

    @Test
    public void testAds() {
        generateWithStats(KitNames.ADS, KitNames.FRAMEWORK);
    }

    @Test
    public void testGenerateMLGms() {
        generateWithStats(KitNames.ML, KitNames.FRAMEWORK);
    }

    @Test
    public void testGenerateMLFirebase() {
        generateWithStats(KitNames.ML, KitNames.FRAMEWORK, KitNames.FIREBASE, KitNames.ACCOUNT);
    }

    @Test
    public void testGenerateGame() {
        generateWithStats(KitNames.GAME, KitNames.FRAMEWORK, KitNames.ACCOUNT);
    }

    @Test
    public void testFilesRead() {
        // read push json
        String userDir = System.getProperty("user.dir");
        String pushPath = "/src/main/resources/xms/json/push/firebase-messaging";
        Path path = Paths.get(userDir + pushPath).toAbsolutePath();
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> paths = walk.filter(Files::isRegularFile)
                .filter(e -> e.toAbsolutePath().toString().endsWith(".json"))
                .collect(Collectors.toList());
            Assert.assertTrue(paths.size() > 0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * get number of apis in a given kit
     *
     * @param kit kit name
     * @return return number of apis in a given kit
     */
    private int getKitApis(String kit) {
        List<JClass> jClasses = readJsonsOf(kit);
        return jClasses.stream().map(e -> e.fields().size() + e.methods().size()).reduce(0, Integer::sum);
    }

    private List<JClass> readJsonsOf(String kit) {
        Path path = Paths.get(USR_DIR + File.separator + "src" + File.separator + "main" + File.separator + "resources"
            + File.separator + "xms" + File.separator + "json" + File.separator + kit);
        List<JClass> jClasses = new ArrayList<>();
        try (Stream<Path> files = Files.walk(path)) {
            files.filter(Files::isRegularFile).forEach(e -> {
                try {
                    JClass jClass = new Gson().fromJson(
                        new InputStreamReader(new FileInputStream(e.toString()), StandardCharsets.UTF_8), JClass.class);
                    jClasses.add(jClass);
                } catch (FileNotFoundException ex) {
                    LOGGER.error("Invalid File");
                }
            });
        } catch (IOException e) {
            LOGGER.error("Invalid Input");
        }
        return jClasses;
    }

    private void generateWithStats(String kit, String... dependencies) {
        Set<String> kits = allKits(kit, dependencies);
        ApiStats stats = testGenerate(kits);
        excludeDepencies(stats, dependencies);
        stats.setTotal(getKitApis(kit));
        printStats(kit, stats);
        saveAndUpdateStats(kit, stats);
    }

    private Set<String> allKits(String kit, String[] dependencies) {
        Set<String> kits = new HashSet<>();
        kits.add(kit);
        kits.addAll(Arrays.asList(dependencies));
        return kits;
    }

    private void excludeDepencies(ApiStats stats, String... dependencies) {
        int filterByWild = stats.getFilterByWild();
        int total = stats.getTotal();
        int kitGenerate = total - filterByWild;
        for (String dependency : dependencies) {
            ApiStats s = readFileStats(dependency);
            kitGenerate -= s.getGenerated();
        }

        // number of api auto converted, not forget to minus fakeApis(isDeprecated and notSupport and developerManual)
        stats.setGenerated(kitGenerate - stats.getFakeApis());
    }

    private void printStats(String kit, ApiStats stats) {
        LOGGER.warn("{}  have {} apis, {} have been transformed", kit, stats.getTotal(), stats.getGenerated());
    }

    private ApiStats readFileStats(String kit) {
        ApiStats stats = new ApiStats();
        try {
            List<String> lines =
                Files.readAllLines(Paths.get(USR_DIR + File.separator + "stats" + File.separator + kit + ".stats"));
            String line = lines.get(0);
            String[] data = line.split(" ");
            stats.setTotal(Integer.parseInt(data[0]));
            stats.setGenerated(Integer.parseInt(data[1]));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return stats;
    }

    private void saveAndUpdateStats(String kit, ApiStats stats) {
        try {
            List<Integer> data =
                Arrays.asList(stats.getTotal(), stats.getGenerated(), stats.getTotalFilterd(), stats.getNotSupported());
            File statsDir = new File(USR_DIR, "stats");
            if (statsDir.mkdirs()) {
                String fileName = statsDir.getCanonicalPath() + File.separator + kit + ".stats";
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                writer.write("");
                writer.flush();
                String builder = data.get(0) + " " + data.get(1);
                writer.write(builder);
                writer.flush();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
