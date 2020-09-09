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
import com.huawei.generator.g2x.po.kit.KitMapping;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.exception.GeneratorResultException;
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
 * Class for Generator
 *
 * @since 2019-11-14
 */
public class Generator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

    // key -- jar name, value -- path of json of jar
    private Map<String, List<String>> jsonMap = new HashMap<>();

    private File inPath; // just for test

    private File outPath;

    private GenerationTaskManager manager;

    private String pluginPath; // the absolute path of generator

    private Map<String, String> kitVersionMap;

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
    public Generator(Set<String> kits, String output, GeneratorConfiguration configuration,
        Map<String, String> gmsVersion) {
        this.outPath = new File(output);
        this.configuration = configuration;
        this.manager = GenerationTaskManager.create(configuration.getFactory());
        this.kitVersionMap = KitMapping.processGmsVersion(gmsVersion);
        this.todoManager = new TodoManager(null, configuration, kitVersionMap);
        List<String> pos = new ArrayList<>();
        pos.add("/xms/json");
        pos.add("/xms/agc-json");
        staticDirs.addAll(kits);
        initJsonMap(pos);
        generateDefinitions(kits);
    }

    Generator(GeneratorBuilder builder) {
        this.pluginPath = builder.getPluginPath();
        this.outPath = builder.getOutPath();
        this.kitVersionMap = builder.getKitVersionMap();
        this.realKitList = builder.getRealkitList();
        this.staticDirs = builder.getStaticDirs();
        this.configuration = builder.getConfiguration();
        this.manager = GenerationTaskManager.create(configuration.getFactory(), pluginPath);
        this.todoManager = new TodoManager(pluginPath, configuration, kitVersionMap);
    }

    private void generateDefinitions(Set<String> jars) {
        for (String jar : jars) {
            List<String> paths = jsonMap.get(jar);
            if (paths == null) {
                LOGGER.warn("json of {} does not exist", jar);
                continue;
            }
            for (String path : paths) {
                if (kitVersionMap.containsKey(jar) && !path.contains(kitVersionMap.get(jar))) {
                    continue;
                }
                try (Stream<Path> walk = Files.walk(Paths.get(path))) {
                    List<Path> json = walk.filter(Files::isRegularFile)
                        .filter(filePath -> filePath.toAbsolutePath().toString().endsWith(".json"))
                        .collect(Collectors.toList());
                    for (Path p : json) {
                        try (FileInputStream fileInputStream = new FileInputStream(p.toFile())) {
                            addClassDef(fileInputStream);
                        } catch (FileNotFoundException e) {
                            LOGGER.error("Generate Definitions failed!");
                            return;
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Close resource failed when generating definitions!");
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
                File[] ks = kit.listFiles();
                if (ks == null || ks.length == 0) {
                    continue;
                }
                for (File k : ks) {
                    if (jsonMap.containsKey(kit.getName())) {
                        jsonMap.get(kit.getName()).add(k.getPath());
                    } else {
                        List<String> paths = new ArrayList<>();
                        paths.add(k.getPath());
                        jsonMap.put(kit.getName(), paths);
                    }
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
     * Returns API conversion statistics
     *
     * @return API conversion statistics
     */
    public ApiStats generate() {
        if (isAllJsonValid) {
            return generateClasses();
        } else {
            LOGGER.error("Found invalid json, generation aborted.");
            throw new InvalidJsonException();
        }
    }

    private void addClassDef(InputStream inputStream) {
        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JClass def = Parser.parse(isr);
        isAllJsonValid = JsonValidator.validate(def.gName() != null ? def.gName() : "", def) && isAllJsonValid;
        manager.addDefinition(def);
    }

    private ApiStats generateClasses() {
        manager.setAllClassMapping();
        // apis number after class filtering
        ApiStats stats = manager.initClasses();
        manager.generateAll().forEach(classNode -> {
            String packageName = classNode.packageName();
            CompilationUnitNode unitNode = new CompilationUnitNode();
            unitNode.setPackageNode(PackageNode.create(classNode.packageName()));
            unitNode.getClassNodes().add(classNode);
            if (classNode.hasTodo()) {
                TodoManager.createTodoBlockFor(unitNode);
            }
            File dir = new File(outPath, packageName.replace(".", File.separator));
            dir.mkdirs();
            File target = new File(dir, degenerify(classNode.shortName()) + ".java");
            generatedFiles.add(target);
            try (OutputStream os = new FileOutputStream(target)) {
                JavaCodeGenerator.from(unitNode, todoManager).to(os);
            } catch (FileNotFoundException e) {
                LOGGER.error("Output target file does not exist!");
            } catch (IOException e) {
                LOGGER.error("Close output stream failed!");
            }
        });
        String[] paraArray = new String[2];
        for (String jarName : staticDirs) {
            paraArray[0] = jarName;
            paraArray[1] = pluginPath;
            StaticPatcher.patchResources(paraArray, outPath, configuration, generatedFiles, kitVersionMap);
        }
        return stats;
    }

    public GeneratorResult resolveAllClasses(String zipFileName, Set<String> kitList) {
        try (ZipFile zipFile = new ZipFile(zipFileName);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(zipFileName)))) {
            ZipEntry nextEntry = zip.getNextEntry();
            while (nextEntry != null) {
                if ((nextEntry.getName().startsWith("xms/json") || nextEntry.getName().startsWith("xms/agc-json"))
                    && nextEntry.getName().endsWith(".json")
                    && Validator.validNameFromList(nextEntry.getName(), kitList)) {
                    InputStream inputStream = zipFile.getInputStream(nextEntry);
                    String[] pathArray = nextEntry.getName().split("/");
                    String kitName = pathArray[2];
                    if (kitVersionMap.containsKey(kitName)) {
                        String version = kitVersionMap.get(kitName);
                        String keyPath = "/json/" + kitName + "/" + version;
                        String agcPath = "/agc-json/" + kitName + "/" + version;
                        if (nextEntry.getName().contains(keyPath) || nextEntry.getName().contains(agcPath)) {
                            addClassDef(inputStream);
                        }
                    } else {
                        addClassDef(inputStream);
                    }
                }
                nextEntry = zip.getNextEntry();
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Plugin zip does not exist!");
            return GeneratorResult.MISSING_PLUGIN;
        } catch (IOException e) {
            LOGGER.error("Read content from zip failed!");
            return GeneratorResult.INNER_CRASH;
        }
        return GeneratorResult.SUCCESS;
    }
}
