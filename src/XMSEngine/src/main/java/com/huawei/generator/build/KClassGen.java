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

package com.huawei.generator.build;

import com.huawei.generator.g2x.po.kit.KitMapping;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.mirror.KClass;
import com.huawei.generator.utils.FileUtils;
import com.huawei.generator.utils.XMSUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Generate gms.json
 *
 * @since 2019-12-21
 */
public enum KClassGen {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(KClassGen.class);

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private static final String USR_DIR = System.getProperty("user.dir");

    private static final String JSON_ROOT =
        String.join(File.separator, USR_DIR, "src", "main", "resources", "xms", "json");

    private static final String FIREBASE_ROOT =
        String.join(File.separator, USR_DIR, "src", "main", "resources", "xms", "agc-json");

    private Map<String, KClass> allClasses = new HashMap<>();

    private Map<String, String> currentVersion = new HashMap<>();

    private static KClass toKClass(JClass jClass) {
        KClass kClass = new KClass();
        List<JMethod> methods = jClass.methods().stream().filter(it -> it.g() != null).map(JMapping::g).peek(m -> {
            if (jClass.isInterface() && !m.modifiers().contains("abstract")) {
                m.modifiers().add("abstract");
            }
        }).collect(Collectors.toList());
        kClass.setModifiers(jClass.modifiers());
        kClass.setClassName(jClass.gName());
        kClass.setSuperClass(jClass.superClass());
        kClass.setInterfaces(jClass.interfaces());
        kClass.setMethods(methods);
        kClass.setInnerClass(jClass.isInnerClass());
        kClass.setType(jClass.type());
        kClass.setFields(jClass.fields());
        return kClass;
    }

    private void populateAll(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            LOGGER.error("{} is not a directory", dir.toString());
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                populateAll(f);
            } else {
                visit(f);
            }
        }
    }

    private void visit(File f) {
        String kitName = "";
        if (f.getAbsolutePath().contains(JSON_ROOT)) {
            kitName = f.getAbsolutePath().replace(JSON_ROOT, "");
        }
        if (f.getAbsolutePath().contains(FIREBASE_ROOT)) {
            kitName = f.getAbsolutePath().replace(FIREBASE_ROOT, "");
        }
        String[] splits = kitName.split("[/\\\\]");
        kitName = splits[1];
        String version;
        if (currentVersion.containsKey(kitName)) {
            version = currentVersion.get(kitName);
            String keyPath = File.separator + "json" + File.separator + kitName + File.separator + version;
            String agcPath = File.separator + "agc-json" + File.separator + kitName + File.separator + version;
            if (!(f.getAbsolutePath().contains(keyPath) || f.getAbsolutePath().contains(agcPath))) {
                return;
            }
        }

        try (FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            JClass jClass = GSON.fromJson(isr, JClass.class);
            KClass kClass = toKClass(jClass);
            allClasses.put(XMSUtils.degenerify(kClass.getClassName()), kClass);
        } catch (FileNotFoundException e) {
            LOGGER.error("Input file in {} does not exist!", kitName);
        } catch (IOException e) {
            LOGGER.error("Close resource failed in {}", kitName);
        }
    }

    public Map<String, KClass> generate() {
        populateAll(new File(JSON_ROOT));
        populateAll(new File(FIREBASE_ROOT));
        return allClasses;
    }

    public Map<String, KClass> generateGmsClassList(Map<String, String> kitVersion, String pluginPath) {
        allClasses = new HashMap<>();
        if (kitVersion == null) {
            currentVersion = KitMapping.processGmsVersion(null);
        } else {
            currentVersion = kitVersion;
        }
        if (pluginPath == null) {
            generate();
            allClasses.values().forEach(kClass -> kClass.getMethods().forEach(kMethod -> kMethod.setClass(kClass)));
            return allClasses;
        }
        try (ZipFile zipFile = new ZipFile(pluginPath);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(pluginPath)))) {
            ZipEntry nextEntry = zip.getNextEntry();
            while (nextEntry != null) {
                if (FileUtils.isJson(nextEntry)) {
                    generateGmsClass(nextEntry, zipFile, currentVersion);
                }
                nextEntry = zip.getNextEntry();
            }
        } catch (IOException e) {
            LOGGER.error("Generate gmsClass list failed");
        }
        allClasses.values().forEach(kClass -> kClass.getMethods().forEach(kMethod -> kMethod.setClass(kClass)));
        return allClasses;
    }

    private void generateGmsClass(ZipEntry entry, ZipFile zipFile, Map<String, String> currentVersion) {
        String kitName = entry.getName().split("/")[2];
        if (currentVersion.containsKey(kitName)) {
            String version = currentVersion.get(kitName);
            String keyPath = "/json/" + kitName + "/" + version;
            String agcPath = "/agc-json/" + kitName + "/" + version;
            if (!(entry.getName().contains(keyPath) || entry.getName().contains(agcPath))) {
                return;
            }
        }
        try (InputStream inputstream = zipFile.getInputStream(entry);
            InputStreamReader isr = new InputStreamReader(inputstream, StandardCharsets.UTF_8)) {
            JClass jClass = GSON.fromJson(isr, JClass.class);
            KClass kClass = toKClass(jClass);
            allClasses.put(XMSUtils.degenerify(kClass.getClassName()), kClass);
        } catch (IOException e) {
            LOGGER.error("Generate gmsClass failed!");
        }
    }
}
