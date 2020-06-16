/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.core.engine.fixbot.util;

import com.huawei.hms.convertor.util.Constant;

import com.alibaba.fastjson.JSON;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fixbot VM Options
 *
 * @since 2020-03-10
 */
@Slf4j
public final class FixbotConfigs {
    private static final String FIXBOT_MAIN_CLASS = "com.huawei.codebot.entry.codemigrate.MainEntry4CodeMigrate";

    private static final String FIXBOT_VMOPTIONS_FILENAME = "/fixbot.vmoptions";

    private static final String CUSTOM_VMOPTIONS_FILENAME = "convertor.vmoptions";

    private static final String DEPENDENCY_EXTENSION_NAME = ".jar";

    private static final String[] ALLOWED_VM_OPTIONS = new String[] {"-Xms", "-Xmx", "-Xmn", "-Xss", "-XX:"};

    private static final FixbotConfigs FIXBOT_CONFIGS = new FixbotConfigs();

    private FixbotConfigs() {
    }

    /**
     * Get singleton instance of {@code FixbotConfigs}
     *
     * @return The singleton instance of {@code FixbotConfigs}
     */
    public static FixbotConfigs getInstance() {
        return FIXBOT_CONFIGS;
    }

    /**
     * Get fixbot vm options
     *
     * @param enginePath Engine path
     * @return Fixbot vm options
     */
    List<String> getVmOptions(String enginePath) {
        FixbotVmOptions vmOptions = JSON.parseObject(readDefaultVmOptions(), FixbotVmOptions.class);
        List<String> configs = new ArrayList<>();
        String javaCommandPath = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        configs.add(javaCommandPath);
        List<String> customVmOptions = getCustomVmOptions();
        if (!customVmOptions.isEmpty()) {
            configs.addAll(customVmOptions);
        } else {
            configs.addAll(vmOptions.getJavaOpts());
        }
        configs.add("-cp");
        String classPath = getFixbotClassPath(enginePath, vmOptions.getClassPath());
        configs.add(classPath);
        configs.add(FIXBOT_MAIN_CLASS);
        return configs;
    }

    private String readDefaultVmOptions() {
        try (InputStream inputStream = getClass().getResourceAsStream(FIXBOT_VMOPTIONS_FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            log.error("Not found fixbot.vmoptions", e);
            return "";
        }
    }

    private String getFixbotClassPath(String enginePath, Map<String, List<String>> classPath) {
        Optional<String> fixbotClasspath = getFixbotDependencies(enginePath, classPath).stream()
            .reduce((dependency1, dependency2) -> dependency1 + File.pathSeparator + dependency2);
        return fixbotClasspath.orElse("");
    }

    private List<String> getFixbotDependencies(String enginePath, Map<String, List<String>> classPath) {
        List<String> allDependencies = getAllDependencies(enginePath);

        return classPath.values()
            .stream()
            .map(dependencyNames -> getFixbotDependencies(allDependencies, dependencyNames))
            .reduce(this::mergeDependencies)
            .orElseGet(ArrayList::new);
    }

    private List<String> getAllDependencies(String enginePath) {
        File engineFolder = new File(enginePath);
        String[] files = engineFolder.list();
        if (files == null) {
            return new ArrayList<>();
        }

        return Stream.of(files)
            .filter(file -> !Files.isDirectory(Paths.get(file)) && file.endsWith(DEPENDENCY_EXTENSION_NAME))
            .collect(Collectors.toList());
    }

    private List<String> getFixbotDependencies(List<String> allDependencies, List<String> dependencyNames) {
        return dependencyNames.stream()
            .map(dependencyName -> getFixbotDependency(allDependencies, dependencyName))
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toList());
    }

    private String getFixbotDependency(List<String> allDependencies, String dependencyName) {
        for (String dependency : allDependencies) {
            if (dependency.contains(dependencyName)) {
                return dependency;
            }
        }
        return "";
    }

    private List<String> mergeDependencies(List<String> dependencies1, List<String> dependencies2) {
        dependencies1.addAll(dependencies2);
        return dependencies1;
    }

    private List<String> getCustomVmOptions() {
        Path customVmOptions = Paths.get(Constant.CONFIG_CACHE_PATH, CUSTOM_VMOPTIONS_FILENAME);
        try {
            return Files.readAllLines(customVmOptions)
                .stream()
                .filter(this::validateVmOptions)
                .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private boolean validateVmOptions(String vmOption) {
        for (String allowedVmOption : ALLOWED_VM_OPTIONS) {
            if (vmOption.startsWith(allowedVmOption) && vmOption.length() > allowedVmOption.length()) {
                return true;
            }
        }
        return false;
    }

    @Data
    private static class FixbotVmOptions {
        private List<String> javaOpts;

        private Map<String, List<String>> classPath;
    }
}
