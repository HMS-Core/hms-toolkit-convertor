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

import com.huawei.generator.g2x.processor.GeneratorStrategyKind;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test for only G
 *
 * @since 2020-03-31
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GeneratorGTest {
    private static final String USR_DIR = System.getProperty("user.dir");

    private static final GeneratorStrategyKind GENERATOR_STRATEGY_KIND = GeneratorStrategyKind.G;

    @BeforeClass
    public static void globalSetup() {
        generateProject();
    }

    private static void generateProject() {
        new XModuleGenerator(Paths.get(targetRoot()).toFile(), new ArrayList<>()).generateModule(false,
            GENERATOR_STRATEGY_KIND, null);
    }

    @Before
    public void setup() {
        enableBlackList();
    }

    private void enableBlackList() {
        System.setProperty("enable_black_list", "true");
    }

    private static String targetRoot() {
        return "xms-adapter-G";
    }

    private void testGenerate(String kit, String... dependencies) {
        Set<String> kits = new HashSet<>();
        kits.add(kit);
        Collections.addAll(kits, dependencies);
        String outPath = String.join(File.separator, USR_DIR, targetRoot(), "src", "main", "java");
        GeneratorConfiguration configuration = GeneratorConfiguration.getConfiguration(GENERATOR_STRATEGY_KIND);
        Generator generator = new Generator(kits, outPath, configuration);
        generator.generate();
    }

    @Test
    public void testGenerateCore() {
        testGenerate(KitNames.FRAMEWORK);
    }

    @Test
    public void testGenerateNearby() {
        testGenerate(KitNames.NEARBY, KitNames.FRAMEWORK);
    }

    @Test
    public void testGenerateLocation() {
        testGenerate(KitNames.LOCATION, KitNames.FRAMEWORK);
    }

    @Test
    public void zzzTestAccount() {
        testGenerate(KitNames.ACCOUNT, KitNames.FRAMEWORK);
    }

    @Test
    public void testPanorama() {
        testGenerate(KitNames.PANORAMA, KitNames.FRAMEWORK);
    }

    @Test
    public void testSafety() {
        testGenerate(KitNames.SAFETY, KitNames.FRAMEWORK);
    }

    @Test
    public void testGenerateMap() {
        testGenerate(KitNames.MAPS, KitNames.FRAMEWORK, KitNames.LOCATION);
    }

    @Test
    public void testGenerateAnalytics() {
        testGenerate(KitNames.ANALYTICS, KitNames.FRAMEWORK);
    }

    @Test
    public void testGeneratePush() {
        testGenerate(KitNames.PUSH, KitNames.FRAMEWORK, KitNames.FIREBASE);
    }

    @Test
    public void testGenerateIdentity() {
        testGenerate(KitNames.IDENTITY, KitNames.FRAMEWORK);
    }

    @Test
    public void testAwareness() {
        testGenerate(KitNames.AWARENESS, KitNames.FRAMEWORK, KitNames.LOCATION, KitNames.SITE);
    }

    @Test
    public void testWallet() {
        testGenerate(KitNames.WALLET, KitNames.FRAMEWORK, KitNames.IDENTITY, KitNames.MAPS, KitNames.ACCOUNT);
    }

    @Test
    public void testGenerateHealth() {
        testGenerate(KitNames.HEALTH, KitNames.FRAMEWORK, KitNames.ACCOUNT);
    }

    @Test
    public void testAds() {
        testGenerate(KitNames.ADS, KitNames.FRAMEWORK);
    }

    @Test
    public void testGenerateML() {
        testGenerate(KitNames.ML, KitNames.FRAMEWORK, KitNames.FIREBASE, KitNames.ACCOUNT);
    }

    @Test
    public void testGenerateGame() {
        testGenerate(KitNames.GAME, KitNames.FRAMEWORK, KitNames.ACCOUNT);
    }

}
