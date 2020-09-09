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
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
public final class TodoResourceLoader {
    public enum Factory {
        INSTANCE;

        private boolean enablePatch = true;

        TodoResourceLoader createLoader(String jarPath, String relativePath, Map<String, String> kitVersionMap) {
            TodoResourceLoader loader = new TodoResourceLoader(jarPath, relativePath, kitVersionMap);
            loader.enablePatch = this.enablePatch;
            return loader;
        }

        public void disablePatch() {
            enablePatch = false;
        }

        public void enablePatch() {
            enablePatch = true;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoResourceLoader.class);

    private static final String DEFAULT_ROOT =
        String.join(File.separator, System.getProperty("user.dir"), "src", "main", "resources", "xms", "patch");

    private boolean enablePatch;

    private String jarPath;

    private String relativePath;

    private Map<String, String> contents;

    private Map<String, String> kitVersionMap;

    private TodoResourceLoader(String jarPath, String relativePath, Map<String, String> kitVersionMap) {
        this.contents = new HashMap<>();
        this.jarPath = jarPath;
        this.relativePath = relativePath;
        this.kitVersionMap = kitVersionMap;
        if (jarPath == null) {
            loadPatchesFromFileSystem();
        } else {
            loadPatchesFromJar();
        }
    }

    Optional<String> getContent(String key) {
        if (!enablePatch) {
            return Optional.empty();
        }
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
        String kitName;
        String path;
        String version;
        String variantPath;
        for (File kitDir : kitDirs) {
            try {
                path = kitDir.getCanonicalPath();
                kitName = path.substring(path.lastIndexOf(File.separator) + 1);
                version = kitVersionMap.get(kitName);
                if (version == null || version.length() == 0) {
                    variantPath = path + File.separator + relativePath;
                } else {
                    variantPath = path + File.separator + version + File.separator + relativePath;
                }
                File variantDir = new File(variantPath);
                File[] patchFiles = variantDir.listFiles();
                if (patchFiles == null) {
                    continue;
                }
                Arrays.stream(patchFiles).filter(File::isFile).forEach(allPatchFiles::add);
            } catch (IOException e) {
                LOGGER.error("Get kitDir's canonicalPath failed!");
            }
        }
        for (File file : allPatchFiles) {
            parseSinglePatchJsonFile(file);
        }
    }

    private void loadPatchesFromJar() {
        try (ZipFile zipFile = new ZipFile(jarPath);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(jarPath)))) {
            ZipEntry nextEntry = zip.getNextEntry();
            while (nextEntry != null) {
                if (nextEntry.getName().startsWith("xms/patch") && nextEntry.getName().endsWith(".json")) {
                    String version = kitVersionMap.get(nextEntry.getName().split("/")[2]);
                    String path;
                    if (version == null || version.length() == 0) {
                        path = "/" + relativePath + "/";
                    } else {
                        path = "/" + version + "/" + relativePath + "/";
                    }
                    if (nextEntry.getName().contains(path)) {
                        InputStream resourceAsStream = zipFile.getInputStream(nextEntry);
                        parseSinglePatchStream(resourceAsStream);
                    }
                }
                nextEntry = zip.getNextEntry();
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Read jar as ZipInputStream failed, can't find the jar");
        } catch (IOException e) {
            LOGGER.error("Read jar failed");
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
        } catch (FileNotFoundException e) {
            LOGGER.error("Input file does not exist!");
        } catch (IOException e) {
            LOGGER.error("Close resource failed when parsing single patch Json file!");
        }
    }

    public static class MapTypeToken extends TypeToken<Map<String, String>> {
    }

    private void parseSinglePatchStream(InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8.name())) {
            Type type = new MapTypeToken().getType();
            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(reader, type);
            if (map != null) {
                addAutoFillCode(map);
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("The Character Encoding is not supported!");
        } catch (JsonIOException e) {
            LOGGER.error("Read inputStream as Json failed!");
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid content exists in input Json!");
        } catch (IOException e) {
            LOGGER.error("Close resource failed when parsing single patch stream!");
        }
    }
}
