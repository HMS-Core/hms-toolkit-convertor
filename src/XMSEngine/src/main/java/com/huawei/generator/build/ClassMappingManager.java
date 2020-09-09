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

import static com.huawei.generator.utils.XMSUtils.degenerify;

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.Parser;
import com.huawei.generator.json.meta.ClassRelation;
import com.huawei.generator.utils.FileUtils;
import com.huawei.generator.utils.TypeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Update mapping_relations.json
 *
 * @since 2019-12-07
 */
public class ClassMappingManager {
    private static Map<String, String> currentVersion = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassMappingManager.class);

    private static final String USR_DIR = System.getProperty("user.dir");

    private static final String JSON_PATH =
        String.join(File.separator, USR_DIR, "src", "main", "resources", "xms", "json");

    private static final String FIREBASE_PATH =
        String.join(File.separator, USR_DIR, "src", "main", "resources", "xms", "agc-json");

    private Map<String, List<ClassRelation>> relationMappings = new HashMap<>();

    private Map<String, JClass> definitions = new HashMap<>();

    public static Map<String, String> getCurrentVersion() {
        return currentVersion;
    }

    public void saveMap(Map<String, String> kitVersion, String[] inPaths) {
        for (String str : inPaths) {
            File in = new File(str);
            File[] files = in.listFiles();
            if (files == null) {
                LOGGER.info("{} is not a directory", str);
                return;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    generateClassMapping(file, kitVersion);
                }
            }
        }
        currentVersion.putAll(kitVersion);
    }

    private void generateClassMapping(File inPath, Map<String, String> kitVersion) {
        definitions = new HashMap<>();
        File[] files = inPath.listFiles();
        String kitName = inPath.getName();
        if (kitVersion.containsKey(kitName)) {
            for (File f : files) {
                String version = kitVersion.get(kitName);
                String keyPath = "\\json\\" + kitName + "\\" + version;
                String agcPath = "\\agc-json\\" + kitName + "\\" + version;
                if (!(f.getAbsolutePath().contains(keyPath) || f.getAbsolutePath().contains(agcPath))) {
                    continue;
                }
                FileUtils.walkDir(f, this::addClassDef);
                generateKitMapping(inPath.getName());
            }
        } else {
            FileUtils.walkDir(inPath, this::addClassDef);
            generateKitMapping(inPath.getName());
        }
    }

    private void addClassDef(InputStream inputStream) {
        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JClass def = Parser.parse(isr);
        definitions.put(TypeNode.create(def.gName()).toX().toString(), def);
        definitions.values().stream().map(jClass -> jClass.methods().size() + jClass.fields().size());
    }

    private void generateKitMapping(String kitName) {
        List<ClassRelation> out = new ArrayList<>();
        definitions.values().forEach(jClass -> {
            ClassRelation relations = new ClassRelation();
            relations.setGmsClassName(degenerify(jClass.gName()));
            relations.setHmsClassName(degenerify(jClass.hName()));
            String xmsName = processXName(jClass);
            if (TypeUtils.isGmsInterface(jClass.gName()) || TypeUtils.isGmsAbstract(jClass.gName())) {
                relations.setXmsClassName(xmsName + "$XImpl");
            } else {
                relations.setXmsClassName(xmsName);
            }
            out.add(relations);
        });
        relationMappings.put(kitName, out);
    }

    /**
     * Used to process xms name for mapping relations.
     *
     * @param jClass target jClass
     */
    private String processXName(JClass jClass) {
        List<String> stash = new ArrayList<>();
        String className = TypeNode.create(jClass.gName()).toX().toString();
        while (isInnerClass(className)) {
            int index = className.lastIndexOf(".");
            stash.add(className.substring(index + 1));
            className = className.substring(0, index);
        }
        StringBuilder name = new StringBuilder(className);
        for (int i = stash.size() - 1; i >= 0; i--) {
            name.append("$").append(stash.get(i));
        }
        return name.toString();
    }

    private boolean isInnerClass(String className) {
        String substring = className.substring(0, className.lastIndexOf("."));
        String name = substring.substring(substring.lastIndexOf(".") + 1);
        char first = name.charAt(0);
        return first >= 'A' && first <= 'Z';
    }

    public Map<String, List<ClassRelation>> generateMap(Map<String, String> kitVersion, String pluginPath) {
        relationMappings = new HashMap<>();
        if (pluginPath == null) {
            ClassMappingManager mappingManager = new ClassMappingManager();
            mappingManager.saveMap(kitVersion, new String[] {FIREBASE_PATH, JSON_PATH});
            return relationMappings;
        }
        try (ZipFile zipFile = new ZipFile(pluginPath);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(pluginPath)))) {
            ZipEntry nextEntry = zip.getNextEntry();
            while (nextEntry != null) {
                if (FileUtils.isJson(nextEntry)) {
                    generateMappingRelation(nextEntry, zipFile, kitVersion);
                }
                nextEntry = zip.getNextEntry();
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Get input file stream failed!");
        } catch (IOException e) {
            LOGGER.info("Read or close zip file failed!");
        }
        return relationMappings;
    }

    private void generateMappingRelation(ZipEntry entry, ZipFile zipFile, Map<String, String> kitVersion) {
        currentVersion.putAll(kitVersion);
        String kitName = entry.getName().split("/")[2];
        if (kitVersion.containsKey(kitName)) {
            String version = kitVersion.get(kitName);
            String keyPath = "/json/" + kitName + "/" + version;
            String agcPath = "/agc-json/" + kitName + "/" + version;
            if (!(entry.getName().contains(keyPath) || entry.getName().contains(agcPath))) {
                return;
            }
        }
        try (InputStream inputStream = zipFile.getInputStream(entry)) {
            generateMapping(kitName, inputStream);
        } catch (IOException e) {
            LOGGER.info("Generate MappingRelation failed!");
        }
    }

    private void generateMapping(String kitName, InputStream inputStream) {
        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JClass jClass = Parser.parse(isr);
        ClassRelation relations = new ClassRelation();
        relations.setGmsClassName(degenerify(jClass.gName()));
        relations.setHmsClassName(degenerify(jClass.hName()));
        String xmsName = processXName(jClass);
        if (TypeUtils.isGmsInterface(jClass.gName()) || TypeUtils.isGmsAbstract(jClass.gName())) {
            relations.setXmsClassName(xmsName + "$XImpl");
        } else {
            relations.setXmsClassName(xmsName);
        }
        if (relationMappings.containsKey(kitName)) {
            relationMappings.get(kitName).add(relations);
        } else {
            List<ClassRelation> classRelations = new ArrayList<>();
            classRelations.add(relations);
            relationMappings.put(kitName, classRelations);
        }
    }
}
