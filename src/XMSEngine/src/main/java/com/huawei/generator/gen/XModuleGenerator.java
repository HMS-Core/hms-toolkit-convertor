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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generate gradle projects.
 *
 * @since 2019-11-24
 */
public class XModuleGenerator {
    public interface TextHandler {
        /**
         * transform an original String
         *
         * @param origin original String
         * @return transformed string
         */
        String transform(String origin);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(XModuleGenerator.class);

    private static final String PROJECT_SOURCE_DIR = "src/main/java";

    private static final String XMS_CODE = "/xms/code/";

    private static final String LIBS = "libs";

    private static final List<String> GRADLE_FILES = Arrays.asList("gradle/wrapper/gradle-wrapper.jar",
        "gradle/wrapper/gradle-wrapper.properties", "build.gradle", "gradlew", "gradlew.bat", "settings.gradle");

    private static final String XMS_BOX = "/XBox";
    private static final String XMS_OBJECT = "/XObject";
    private static final String XMS_GETTABLE = "/XGettable";
    private static final String XMS_ENUM = "/XEnum";
    private static final String INTERFACE = "XInterface";
    private static final String BRIDGE_METHOD_UTILS = "BridgeMethodUtils";
    private static final String FUNCTION = "Function";
    private static final String PARAMETER = "Parameter";
    private static final String TYPE_NODE = "TypeNode";
    private static final String LOG = "XmsLog";
    private static final String UTILS = "/Utils";
    private static final String XGH_MAP = "${XGH_MAP}";
    private static final String XMS_MODULE = "/xms/module/";
    private static final String X_PACKAGE = "${X_PACKAGE}";
    private static final String STRATEGY = "${STRATEGY}";
    private static final String MAPPING_RELATIONS = "/xms/maputil/mapping_relations.json";
    private static final String GLOBALENVSETTING = "/xms/code/GlobalEnvSetting";
    private static final String G_GLOBALENVSETTING = "/xms/code/g/GlobalEnvSetting";

    private File outPath;

    private List<File> generatedFiles;

    public XModuleGenerator(File outPath, List<File> generatedFiles) {
        this.outPath = outPath;
        this.generatedFiles = generatedFiles;
    }

    /**
     * Generate a gradle module
     *
     * @param javaCodeForG2X whether generate java code for g2x
     * @param strategyKind XMS router strategy
     */
    public void generateModule(boolean javaCodeForG2X, GeneratorStrategyKind strategyKind,
        List<String> specifiedKitList) {
        try {
            outPath = outPath.getCanonicalFile();
        } catch (IOException e) {
            LOGGER.error("Failed to get canonical file for {}", outPath);
            return;
        }
        boolean error = false;
        if (outPath.exists() && !outPath.isDirectory()) {
            LOGGER.error("{} is not a directory, failed to generate xms project", outPath.toString());
            error = true;
        }
        if (!outPath.exists() && !outPath.mkdirs()) {
            LOGGER.error("Failed to create directories in {}", outPath);
            error = true;
        }
        if (error) {
            throw new IllegalArgumentException("Failed to generate xms project");
        }

        // generate gradle project and code according to not g2X api
        if (!javaCodeForG2X) {
            createDir(LIBS);
            createDir(PROJECT_SOURCE_DIR);
            copyModuleResource();
        }

        String codeDir = GeneratorConfiguration.getConfiguration(strategyKind).getCodePath();
        copyJavaSource(XMS_CODE + codeDir + XMS_BOX, javaCodeForG2X);
        copyJavaSource(XMS_CODE + codeDir + XMS_OBJECT, javaCodeForG2X);
        copyJavaSource(XMS_CODE + codeDir + XMS_GETTABLE, javaCodeForG2X);
        copyJavaSource(XMS_CODE + codeDir + XMS_ENUM, javaCodeForG2X);

        copyJavaSource(XMS_CODE + codeDir + UTILS,
            s -> s.replace(XGH_MAP, RuntimeTypeMappings.create(strategyKind)
                .dumpMappings(MAPPING_RELATIONS, specifiedKitList)),
            javaCodeForG2X);

        if (strategyKind == GeneratorStrategyKind.G) {
            copyJavaSource(G_GLOBALENVSETTING, javaCodeForG2X);
        } else {
            copyJavaSource(GLOBALENVSETTING,
                s -> s.replace(STRATEGY, strategyKind == GeneratorStrategyKind.HOrG ? "" : "; //"),
                javaCodeForG2X);
        }

        copyJavaSource(XMS_CODE + INTERFACE, javaCodeForG2X);
        copyJavaSource(XMS_CODE + BRIDGE_METHOD_UTILS, javaCodeForG2X);
        copyJavaSource(XMS_CODE + FUNCTION, javaCodeForG2X);
        copyJavaSource(XMS_CODE + PARAMETER, javaCodeForG2X);
        copyJavaSource(XMS_CODE + TYPE_NODE, javaCodeForG2X);
        copyJavaSource(XMS_CODE + LOG, javaCodeForG2X);
    }

    private void createDir(String relativePath) {
        File file = new File(outPath, relativePath);
        if (!file.mkdirs()) {
            LOGGER.warn("{} already exists", file.toString());
        }
    }

    private void copyModuleResource() {
        for (String s : XModuleGenerator.GRADLE_FILES) {
            copyFile(s, XMS_MODULE + s);
        }
    }

    private void copyFile(String relativeTargetPath, String resourcePath) {
        File file = new File(outPath, relativeTargetPath);
        try (InputStream ins = getClass().getResourceAsStream(resourcePath)) {
            if (file.exists() && !file.delete()) {
                LOGGER.warn("File already exists: {}", file.toString());
                return;
            }
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                LOGGER.warn("Failed to create dir for {}", file.toString());
                return;
            }
            Files.copy(ins, file.toPath());
        } catch (IOException e) {
            LOGGER.error("Failed to copy {}", resourcePath, e);
        }
    }

    private void copyJavaSource(String resourcePath, boolean javaCodeForG2X) {
        copyJavaSource(resourcePath, null, javaCodeForG2X);
    }

    private void copyJavaSource(String resourcePath, TextHandler handler, boolean javaCodeForG2X) {
        File srcDir;
        File dir;
        if (javaCodeForG2X) {
            srcDir = new File(outPath, AstConstants.XMS_PACKAGE.replace(".", File.separator));
            dir = new File(srcDir.getPath());
        } else {
            srcDir = new File(outPath, PROJECT_SOURCE_DIR);
            dir = new File(srcDir, AstConstants.XMS_PACKAGE.replace(".", File.separator));
        }

        if (!dir.exists() && !dir.mkdirs()) {
            LOGGER.warn("Failed to create dir for {}", dir);
            return;
        }
        String name = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        File target = new File(dir, name + ".java");
        generatedFiles.add(target);
        try (InputStream ins = getClass().getResourceAsStream(resourcePath);
            FileOutputStream fos = new FileOutputStream(target);
            Writer writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            InputStreamReader isr = new InputStreamReader(ins, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr)) {
            String text = reader.lines().collect(Collectors.joining("\n")) + '\n';
            text = text.replace(X_PACKAGE, AstConstants.XMS_PACKAGE);
            if (handler != null) {
                text = handler.transform(text);
            }
            writer.write(text);
        } catch (IOException e) {
            LOGGER.error("Failed to copy {}", resourcePath, e);
        }
    }
}
