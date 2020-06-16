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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * To-do resource loader
 *
 * @since 2019-11-21
 */
public class TodoResourceLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoResourceLoader.class);

    private static final String DEFAULT_ROOT =
        String.join(File.separator, System.getProperty("user.dir"), "src", "main", "resources", "xms", "patch");

    private String jarPath;

    private String relativePath;

    private Map<String, String> contents;

    Optional<String> getContent(String key) {
        return Optional.ofNullable(contents.get(key));
    }

    private void loadPatchesFromFileSystem() {
        Set<File> allPatchFiles = new HashSet<>();
        File root = new File(DEFAULT_ROOT);
        if (!root.exists()) {
            LOGGER.error("file or directory dose not exist!");
            return;
        }
        File[] kitDirs = root.listFiles();
        if (kitDirs == null) {
            throw new IllegalStateException("Patch path is not directory");
        }
        for (File kitDir : kitDirs) {
            try {
                String variantPath = kitDir.getCanonicalPath() + File.separator + relativePath;
                File variantDir = new File(variantPath);
                File[] patchFiles = variantDir.listFiles();
                if (patchFiles == null) {
                    continue;
                }
                Arrays.stream(patchFiles).filter(File::isFile).forEach(allPatchFiles::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (File file : allPatchFiles) {
            parseSinglePatchJsonFile(file);
        }
    }

    private void loadPatchesFromJar() {
        try (ZipFile zipFile = new ZipFile(jarPath);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(jarPath)))) {
            while (true) {
                ZipEntry nextEntry;
                nextEntry = zip.getNextEntry();
                if (nextEntry == null) {
                    break;
                } else {
                    if (nextEntry.getName().startsWith("xms/patch")
                            && nextEntry.getName().contains("/" + relativePath + "/")
                            && nextEntry.getName().endsWith(".json")) {
                        try (InputStream resourceAsStream = zipFile.getInputStream(nextEntry)) {
                            parseSinglePatchStream(resourceAsStream);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    TodoResourceLoader(String jarPath, String relativePath) {
        this.contents = new HashMap<>();
        this.jarPath = jarPath;
        this.relativePath = relativePath;
        if (jarPath == null) {
            loadPatchesFromFileSystem();
        } else {
            loadPatchesFromJar();
        }
    }

    private void addAutoFillCode(Map<String, String> mapItem) {
        for (Map.Entry<String, String> entry : mapItem.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (contents.containsKey(key)) {
                throw new IllegalStateException("Discovered same signature method " + key);
            } else {
                contents.put(key, value);
            }
        }
    }

    private void parseSinglePatchJsonFile(File file) {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            parseSinglePatchStream(fileInputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void parseSinglePatchStream(InputStream inputStream) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8.name())) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(reader, type);
            if (map != null) {
                addAutoFillCode(map);
            }
        }
    }
}
