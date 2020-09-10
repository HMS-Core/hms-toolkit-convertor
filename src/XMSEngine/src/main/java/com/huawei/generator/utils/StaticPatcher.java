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

package com.huawei.generator.utils;

import com.huawei.generator.gen.GeneratorConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Patch source code with static resources
 *
 * @since 2019-12-04
 */
public class StaticPatcher {
    private static final String DISABLE_STATIC_PATCHER = "disable_static_patcher";

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticPatcher.class);

    private static final int BUFFER_SIZE = 1024;

    private final String staticRoot;

    private File outPath;

    private GeneratorConfiguration configuration;

    private List<File> generatedFiles;

    private StaticPatcher(File outPath, String root, GeneratorConfiguration configuration, List<File> generatedFiles) {
        this.outPath = outPath;
        this.staticRoot = root;
        this.configuration = configuration;
        this.generatedFiles = generatedFiles;
    }

    /**
     * Patch with static resources.
     *
     * @param paraArray param array
     * @param outPath source code output path
     * @param configuration generator configuration
     * @param generatedFiles list of generated files
     * @param kitVersionMap kit version
     */
    public static void patchResources(String[] paraArray, File outPath, GeneratorConfiguration configuration,
        List<File> generatedFiles, Map<String, String> kitVersionMap) {
        if (Boolean.getBoolean(DISABLE_STATIC_PATCHER)) {
            LOGGER.info("Static patcher is disabled.");
            return;
        }
        String jarName = paraArray[0];
        String pluginPath = paraArray[1];
        LOGGER.info("patching with static resources.{}", jarName);
        StaticPatcher.create(outPath, configuration, generatedFiles).patchResources(pluginPath, jarName, kitVersionMap);
    }

    private static StaticPatcher create(File outPath, GeneratorConfiguration configuration, List<File> generatedFiles) {
        return new StaticPatcher(outPath, "xms/static", configuration, generatedFiles);
    }

    private void patchResources(String pluginPath, String jarName, Map<String, String> kitVersionMap) {
        String version = kitVersionMap.get(jarName);
        if (pluginPath == null) { // Non-plug-in environment, UT running
            String staticPath = System.getProperty("user.dir") + "/src/main/resources/" + staticRoot;
            Path path = Paths.get(staticPath);
            processDir(path, jarName, version);
        } else { // plugin environment
            File file = Paths.get(pluginPath).toFile();
            if (!isExisted(pluginPath)) {
                return;
            }
            try (JarFile jar = new JarFile(file)) {
                processJar(jar, jarName, version);
            } catch (IOException e) {
                LOGGER.warn("Read jar file failed!");
            }
        }
    }

    /**
     * patch the static resources
     *
     * @param path where all the static resources located
     * @param jarName the name of jar
     * @param version kit version
     */
    private void processDir(Path path, String jarName, String version) {
        StringBuilder tempPath = new StringBuilder();
        if (version == null || version.length() == 0) {
            tempPath.append(File.separator).append(configuration.getStaticPath()).append(File.separator);
        } else {
            tempPath.append(File.separator)
                .append(version)
                .append(File.separator)
                .append(configuration.getStaticPath())
                .append(File.separator);
        }
        StringBuilder tempJarName = new StringBuilder();
        tempJarName.append(File.separator).append(jarName).append(File.separator);
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> staticResources = walk.filter(Files::isRegularFile)
                .filter(filePath -> filePath.toAbsolutePath().toString().contains(tempJarName)
                        && filePath.toAbsolutePath().toString().contains(tempPath)).collect(Collectors.toList());
            for (Path p : staticResources) {
                FileInputStream ins = new FileInputStream(p.toFile());
                if (p.toString().endsWith(".zip")) {
                    continue;
                }
                copyResource(p.toFile().getName(), ins);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Read static resources failed!");
        } catch (IOException e) {
            LOGGER.error("Close resource failed when copying the static resources");
        }
    }

    private void processJar(JarFile jar, String jarName, String version) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            String featurePath = "/" + jarName + "/";
            String featureGOrHPath;
            if (version == null || version.length() == 0) {
                featureGOrHPath = "/" + (configuration.getStaticPath()) + "/";
            } else {
                featureGOrHPath = "/" + version + "/" + (configuration.getStaticPath()) + "/";
            }
            if (!name.startsWith(staticRoot) || entry.isDirectory() || !name.contains(featurePath)) {
                continue;
            }
            if (!name.contains(featureGOrHPath)) {
                continue;
            }
            try (InputStream ins = jar.getInputStream(entry)) {
                copyResource(name.substring(1 + name.lastIndexOf('/')), ins);
            } catch (IOException e) {
                LOGGER.warn("Close resource failed when processing jar!");
            }
        }
    }

    public static void isFileExisted(File target, String name, InputStream ins) {
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            LOGGER.warn("Failed to make dirs for static resource patching: {}", target);
            return;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        try (FileOutputStream fos = new FileOutputStream(target)) {
            while ((bytesRead = ins.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            LOGGER.info("patched resource from {}", name);
        } catch (FileNotFoundException e) {
            LOGGER.error("Target file dos not exist!");
        } catch (IOException e) {
            LOGGER.error("Failed to copy {}", name, e);
        }
    }

    private void copyResource(String name, InputStream ins) {
        String targetName = name.replace(".", File.separator) + ".java";
        File target = new File(outPath, targetName);
        generatedFiles.add(target);
        isFileExisted(target, name, ins);
    }

    public static boolean isExisted(String pluginPath) {
        File file = Paths.get(pluginPath).toFile();
        if (!file.exists()) {
            LOGGER.error("the plugin does not exits");
            return false;
        }
        boolean exists = file.exists();
        if (!exists) {
            LOGGER.error("the plugin does not exits");
            return false;
        }
        return true;
    }

    public static void copyResourceDir(String pluginPath, String targetRoot, String includeRoot,
        List<String> excludes) {
        File file = Paths.get(pluginPath).toFile();
        if (!isExisted(pluginPath)) {
            return;
        }
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(includeRoot) || entry.isDirectory()) {
                    continue;
                }
                boolean skip = false;
                for (String exclude : excludes) {
                    if (name.startsWith(exclude)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
                try (InputStream ins = jar.getInputStream(entry)) {
                    String targetName = name;
                    if (name.startsWith("xms")) {
                        targetName = targetName.substring(3);
                    }

                    if (!(name.endsWith(".gradle") || name.endsWith(".xml") || name.endsWith(".zip")
                        || name.endsWith(".txt"))) {
                        targetName = targetName.replace(".", File.separator) + ".java";
                    }
                    File target = new File(targetRoot, targetName);
                    isFileExisted(target, name, ins);
                } catch (IOException e) {
                    LOGGER.warn("Read entry or close inputStream failed!");
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Read input jar or close input jar file failed!");
        }
    }
}
