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

import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Update mapping_relations.json
 *
 * @since 2019-12-07
 */
public class ClassMappingManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassMappingManager.class);

    private static final String USR_DIR = System.getProperty("user.dir");

    private static final String MAPPING_PATH =
        String.join(File.separator, USR_DIR, "src", "main", "resources", "xms", "maputil");

    private static final String MAPPING_FILE = "mapping_relations";

    private Map<String, JClass> definitions = new HashMap<>();

    private Map<String, List<ClassRelation>> map = new HashMap<>();

    public void saveMap(String... inPaths) {
        for (String str : inPaths) {
            File in = new File(str);
            File[] files = in.listFiles();
            if (files == null) {
                LOGGER.info("{} is not a directory", str);
                return;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    generateClassMapping(file);
                }
            }
        }
        String result = new GsonBuilder().setPrettyPrinting().create().toJson(map);
        FileUtils.createJsonFile(result, MAPPING_PATH, MAPPING_FILE);
    }

    private void generateClassMapping(File inPath) {
        definitions = new HashMap<>();
        FileUtils.walkDir(inPath, this::addClassDef);
        generateKitMapping(inPath.getName());
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
        map.put(kitName, out);
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
}
