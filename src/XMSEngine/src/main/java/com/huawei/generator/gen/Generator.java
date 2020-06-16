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

import static com.huawei.generator.utils.XMSUtils.degenerify;

import com.huawei.generator.ast.CompilationUnitNode;
import com.huawei.generator.ast.PackageNode;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.GeneratorResultException;
import com.huawei.generator.g2x.processor.map.Validator;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JsonValidator;
import com.huawei.generator.json.Parser;
import com.huawei.generator.utils.StaticPatcher;
import com.huawei.generator.utils.TodoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Generator class
 *
 * @since 2019-11-14
 */
public class Generator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

    // key -- jar name, value -- path of json of jar
    private Map<String, List<String>> jsonMap = new HashMap<>();

    private File outPath;

    private GenerationTaskManager manager;

    private String pluginPath; // the absolute path of generator

    private boolean isAllJsonValid = true;

    private List<String> staticDirs = new ArrayList<>();

    // kitlist with path information
    private List<String> realKitList;

    private GeneratorConfiguration configuration;

    private List<File> generatedFiles = new ArrayList<>();

    private TodoManager todoManager;

    // get GeneratedFiles
    public List<File> getGeneratedFiles() {
        return generatedFiles;
    }

    // just for test
    public Generator(Set<String> kits, String output, GeneratorConfiguration configuration) {
        this.outPath = new File(output);
        this.configuration = configuration;
        this.manager = GenerationTaskManager.create(configuration.getFactory());
        this.todoManager = new TodoManager(null, configuration);
        List<String> pos = new ArrayList<>();
        pos.add("/xms/json");
        pos.add("/xms/agc-json");
        staticDirs.addAll(kits);
        initJsonMap(pos);
        generateDefinitions(kits);
    }

    // builder
    Generator(GeneratorBuilder builder) {
        this.pluginPath = builder.pluginPath;
        this.outPath = builder.outPath;
        this.realKitList = builder.realkitList;
        this.staticDirs = builder.staticDirs;
        this.configuration = builder.configuration;
        this.manager = GenerationTaskManager.create(configuration.getFactory());
        this.todoManager = new TodoManager(pluginPath, configuration);
    }

    private void generateDefinitions(Set<String> jars) {
        for (String jar : jars) {
            List<String> paths = jsonMap.get(jar);
            if (paths == null) {
                LOGGER.warn("json of {} does not exist", jar);
                continue;
            }
            for (String path : paths) {
                try (Stream<Path> walk = Files.walk(Paths.get(path))) {
                    List<Path> json = walk.filter(Files::isRegularFile)
                        .filter(e -> e.toAbsolutePath().toString().endsWith(".json"))
                        .collect(Collectors.toList());
                    for (Path p : json) {
                        addClassDef(p.toFile());
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    return;
                }
            }
        }
    }

    private void initJsonMap(List<String> pos) {
        String jsonPath = pluginPath;
        if (pluginPath == null) {
            jsonPath = Paths.get(System.getProperty("user.dir") + "/src/main/resources/xms").toString();
        }
        Path pluginDir = Paths.get(jsonPath).getParent();
        if (pluginDir == null) {
            LOGGER.error("The pluginDir does not exist!");
            return;
        }
        for (String source : pos) {
            File jsonDir = Paths.get(pluginDir.toString() + source).toFile();
            if (!jsonDir.exists()) {
                LOGGER.error("Json resources do not exist in the plugin dir");
                return;
            }
            File[] kits = jsonDir.listFiles();
            if (kits == null) {
                LOGGER.error("Json resources do not exist in the plugin dir");
                return;
            }
            for (File kit : kits) {
                File[] kitFiles = kit.listFiles();
                if (kitFiles == null || kitFiles.length == 0) {
                    continue;
                }
                for (File kitFile : kitFiles) {
                    jsonMap.computeIfAbsent(kit.getName(), element -> new ArrayList<>()).add(kitFile.getPath());
                }
            }
        }
    }

    public Generator initGeneratorForRouter() {
        GeneratorResult r = resolveAllClasses(pluginPath, new HashSet<>(realKitList));
        if (r != GeneratorResult.SUCCESS) {
            throw new GeneratorResultException(r);
        }
        return this;
    }

    /**
     * return statistics of api converting
     *
     * @return return statistics of api converting
     */
    public ApiStats generate() {
        if (isAllJsonValid) {
            return generateClasses();
        } else {
            LOGGER.error("Found invalid json, generation aborted.");
            throw new InvalidJsonException();
        }
    }

    private void addClassDef(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)) {
            JClass def = Parser.parse(isr);
            isAllJsonValid = JsonValidator.validate(def.gName() != null ? def.gName() : "", def) && isAllJsonValid;
            manager.addDefinition(def);
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private ApiStats generateClasses() {
        manager.setAllClassMapping();
        // apis number after class filtering
        ApiStats stats = manager.initClasses();
        manager.generateAll().forEach(it -> {
            String packageName = it.packageName();
            CompilationUnitNode unitNode = new CompilationUnitNode();
            unitNode.setPackageNode(PackageNode.create(it.packageName()));
            unitNode.getClassNodes().add(it);
            if (it.hasTodo()) {
                TodoManager.createTodoBlockFor(unitNode);
            }
            File dir = new File(outPath, packageName.replace(".", File.separator));
            dir.mkdirs();
            File target = new File(dir, degenerify(it.shortName()) + ".java");
            generatedFiles.add(target);
            try (OutputStream os = new FileOutputStream(target)) {
                JavaCodeGenerator.from(unitNode, todoManager).to(os);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });
        for (String jarName : staticDirs) {
            StaticPatcher.patchResources(jarName, outPath, pluginPath, configuration, generatedFiles);
        }
        return stats;
    }

    public GeneratorResult resolveAllClasses(String zipFileName, Set<String> kitList) {
        try (ZipFile zipFile = new ZipFile(zipFileName);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(zipFileName)))) {
            while (true) {
                ZipEntry nextEntry;
                nextEntry = zip.getNextEntry();
                if (nextEntry == null) {
                    break;
                } else {
                    if ((nextEntry.getName().startsWith("xms/json") || nextEntry.getName().startsWith("xms/agc-json"))
                        && nextEntry.getName().endsWith(".json")
                        && Validator.validNameFromList(nextEntry.getName(), kitList)) {
                        addClassDef(zipFile, nextEntry);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return GeneratorResult.MISSING_PLUGIN;
        }
        return GeneratorResult.SUCCESS;
    }

    private void addClassDef(ZipFile zipFile, ZipEntry nextEntry) throws IOException {
        try (InputStream inputStream = zipFile.getInputStream(nextEntry);
            InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            JClass def = Parser.parse(isr);
            isAllJsonValid = JsonValidator.validate(def.gName() != null ? def.gName() : "", def) && isAllJsonValid;
            manager.addDefinition(def);
        }
    }
}
