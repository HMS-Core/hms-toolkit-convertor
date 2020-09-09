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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generate gradle projects.
 *
 * @since 2019-11-24
 */
public final class XModuleGenerator {
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

    private File outPath;

    private List<File> generatedFiles;

    public XModuleGenerator(File outPath, List<File> generatedFiles) {
        this.outPath = outPath;
        this.generatedFiles = generatedFiles;
    }

    /**
     * Generate a gradle module
     *
     * @param javaCodeForG2X indicates whether to generate javacode for the G2X interface
     * @param strategyKind XMS router strategy
     * @param originKitList list of original kits
     */
    public void generateModule(boolean javaCodeForG2X, GeneratorStrategyKind strategyKind, List<String> originKitList) {
        try {
            outPath = outPath.getCanonicalFile();
        } catch (IOException e) {
            LOGGER.error("Failed to get canonical file for gradle module");
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

        // generate gradle project and code according to non g2x interface call
        if (!javaCodeForG2X) {
            createDir("libs");
            createDir("src/main/java");
            copyModuleResource("gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.properties",
                "build.gradle", "gradlew", "gradlew.bat", "settings.gradle");
        }

        String codeDir = GeneratorConfiguration.getConfiguration(strategyKind).getCodePath();
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/" + codeDir + "/XBox", javaCodeForG2X);
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/" + codeDir + "/XObject", javaCodeForG2X);
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/" + codeDir + "/XGettable", javaCodeForG2X);
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/" + codeDir + "/XEnum", javaCodeForG2X);
        List<String> standardKitList = null;
        if (originKitList != null) {
            standardKitList = originKitList;
        }
        List<String> finalStandardKitList = standardKitList;
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/" + codeDir + "/Utils", s -> {
            s = s.replace("${XGH_MAP}", RuntimeTypeMappings.create(strategyKind).dumpMappings(finalStandardKitList));
            s = s.replace("${GMS_ML_MAP}",
                originKitList != null && (originKitList.contains("mlfirebase") || originKitList.contains("mlgms"))
                    ? ("mlGMSMap.putAll(map);" + System.lineSeparator()
                        + "        mlGMSMap.putAll(org.xms.g.vision.MLMapUtils.loadMLGmsMap());")
                    : "");
            s = s.replace("${FIREBASE_ML_MAP}", originKitList != null && (originKitList.contains("mlfirebase"))
                ? "map.putAll(org.xms.f.ml.vision.MLMapUtils.loadMLFirebaseMap());" : "");
            return s;
        }, javaCodeForG2X);

        if (strategyKind == GeneratorStrategyKind.G) {
            copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/g/GlobalEnvSetting", javaCodeForG2X);
        } else if (strategyKind == GeneratorStrategyKind.H) {
            copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/h/GlobalEnvSetting", javaCodeForG2X);
        } else {
            copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/GlobalEnvSetting",
                s -> s.replace("${STRATEGY}", strategyKind == GeneratorStrategyKind.HOrG ? "" : "; //"),
                javaCodeForG2X);
        }

        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/XInterface", javaCodeForG2X);
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/BridgeMethodUtils", javaCodeForG2X);
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/Function", javaCodeForG2X);
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/Parameter", javaCodeForG2X);
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/TypeNode", javaCodeForG2X);
        copyJavaSource(AstConstants.XMS_PACKAGE, "/xms/code/XmsLog", javaCodeForG2X);
    }

    private void createDir(String relativePath) {
        File file = new File(outPath, relativePath);
        if (!file.mkdirs()) {
            LOGGER.warn("{} already exists", file.toString());
        }
    }

    private void copyModuleResource(String... relPath) {
        for (String s : relPath) {
            copyFile(s, "/xms/module/" + s);
        }
    }

    private void copyFile(String relativeTargetPath, String resourcePath) {
        File file = new File(outPath, relativeTargetPath);
        try (InputStream ins = this.getClass().getResourceAsStream(resourcePath)) {
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
            LOGGER.error("Failed to copy file when copying module resource!");
        }
    }

    private void copyJavaSource(String packageName, String resourcePath, boolean javaCodeForG2X) {
        copyJavaSource(packageName, resourcePath, null, javaCodeForG2X);
    }

    private void copyJavaSource(String packageName, String resourcePath, TextHandler handler, boolean javaCodeForG2X) {
        File srcDir;
        File dir;
        if (javaCodeForG2X) {
            srcDir = new File(outPath, packageName.replace(".", File.separator));
            dir = new File(srcDir.getPath());
        } else {
            srcDir = new File(outPath, PROJECT_SOURCE_DIR);
            dir = new File(srcDir, packageName.replace(".", File.separator));
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
            text = text.replace("${X_PACKAGE}", packageName);
            if (handler != null) {
                text = handler.transform(text);
            }
            writer.write(text);
        } catch (IOException e) {
            LOGGER.error("Failed to copy java source file!");
        }
    }
}
