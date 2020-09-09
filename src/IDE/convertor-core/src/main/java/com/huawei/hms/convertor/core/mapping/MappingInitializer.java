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

package com.huawei.hms.convertor.core.mapping;

import com.huawei.hms.convertor.core.plugin.PluginConstant;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Kits mapping initializer
 *
 * @since 2020-03-27
 */
@Slf4j
public final class MappingInitializer {
    /**
     * The maximum number of directory levels to visit mapping file
     */
    private static final int MAPPING_WORK_DEPTH = 2;

    /**
     * Init kits mappings
     */
    public static void initialize() {
        String newVersionName = getNewVersionName();
        log.info("New version name: {}", newVersionName);

        Optional<Path> mappingStorePathResult = getMappingStorePath();
        mappingStorePathResult.ifPresent(mappingStorePath -> {
            String oldVersionName = getOldVersionName(mappingStorePath);
            log.info("Old version name: {}", oldVersionName);

            if (newVersionName.equals(oldVersionName)) {
                return;
            }

            deleteOldMappingFiles(mappingStorePath);
            createMappingStoreFolder(mappingStorePath);
            updateMappingVersion(mappingStorePath);
            List<String> mappings = getMappingFiles();
            updateMappingFiles(mappingStorePath, mappings);
        });
    }

    private static String getNewVersionName() {
        String versionFilePath =
            MappingConstant.MappingDir.MAPPING_DIR + MappingConstant.MappingVersionFile.VERSION_FILE;
        Properties versionProps = new Properties();

        try (InputStream inputStream = MappingInitializer.class.getResourceAsStream(versionFilePath)) {
            versionProps.load(inputStream);
        } catch (IOException e) {
            log.error("{} not found", versionFilePath);
            return "";
        }

        if (!versionProps.containsKey(MappingConstant.MappingVersionFile.KEY_VERSION_NAME)) {
            log.error("{} not found in {}", MappingConstant.MappingVersionFile.KEY_VERSION_NAME, versionFilePath);
            return "";
        }

        return versionProps.getProperty(MappingConstant.MappingVersionFile.KEY_VERSION_NAME);
    }

    private static boolean isMappingFile(Path targetFile) {
        return targetFile.toString().endsWith(MappingConstant.MappingFile.MAPPING_FILE_EXT);
    }

    private static Optional<Path> getMappingStorePath() {
        URL jarFileUrl = MappingInitializer.class.getResource(MappingConstant.MappingDir.MAPPING_DIR);
        if (!jarFileUrl.getProtocol().equals("jar")) {
            return Optional.empty();
        }

        String jarParentPath = "";
        JarURLConnection jarURLConnection = null;
        try {
            URLConnection urlConnection = jarFileUrl.openConnection();
            if (!(urlConnection instanceof JarURLConnection)) {
                return Optional.empty();
            }
            jarURLConnection = (JarURLConnection) urlConnection;
            JarFile currentJarFile = jarURLConnection.getJarFile();
            String jarFileName = currentJarFile.getName();
            Path jarFilePath = Paths.get(jarFileName).getParent();
            if (Objects.isNull(jarFilePath)) {
                log.error("Cannot get mapping store path");
                return Optional.empty();
            }
            jarParentPath = jarFilePath.toString();
        } catch (IOException e) {
            log.error("Cannot get mapping store path", e);
            return Optional.empty();
        }

        return Optional.of(Paths.get(jarParentPath, PluginConstant.PluginPackageDir.CONFIG_DIR_NAME));
    }

    private static String getOldVersionName(Path mappingStorePath) {
        Path oldVersionPath = Paths.get(mappingStorePath.toString(), MappingConstant.MappingVersionFile.VERSION_FILE);
        Properties versionProps = new Properties();

        try (InputStream inputStream = Files.newInputStream(oldVersionPath)) {
            versionProps.load(inputStream);
        } catch (IOException e) {
            log.error("{} not found", oldVersionPath);
            return "";
        }

        if (!versionProps.containsKey(MappingConstant.MappingVersionFile.KEY_VERSION_NAME)) {
            log.error("{} not found in {}", MappingConstant.MappingVersionFile.KEY_VERSION_NAME, oldVersionPath);
            return "";
        }

        return versionProps.getProperty(MappingConstant.MappingVersionFile.KEY_VERSION_NAME);
    }

    private static void deleteOldMappingFiles(Path mappingStorePath) {
        try {
            FileUtils.forceDelete(mappingStorePath.toFile());
        } catch (FileNotFoundException e) {
            log.info("No mappings files");
        } catch (IOException e) {
            log.error("Cannot delete old mapping files", e);
        }
    }

    private static void createMappingStoreFolder(Path mappingStorePath) {
        if (Files.notExists(mappingStorePath)) {
            File mappingStoreParent = mappingStorePath.toFile();
            try {
                FileUtils.forceMkdir(mappingStoreParent);
            } catch (IOException e) {
                log.error("Cannot create mapping store folder", e);
            }
        }
    }

    private static void updateMappingVersion(Path mappingStorePath) {
        String versionFilePath =
            MappingConstant.MappingDir.MAPPING_DIR + MappingConstant.MappingVersionFile.VERSION_FILE;
        try (InputStream versionStream = MappingInitializer.class.getResourceAsStream(versionFilePath)) {
            Files.copy(versionStream,
                Paths.get(mappingStorePath.toString(), MappingConstant.MappingVersionFile.VERSION_FILE));
        } catch (IOException e) {
            log.error("Copy mapping version file error", e);
        }
    }

    private static List<String> getMappingFiles() {
        List<String> mappings = new ArrayList<>();

        try {
            URI currentJarUri = MappingInitializer.class.getResource(MappingConstant.MappingDir.MAPPING_DIR).toURI();
            FileSystem fileSystem = FileSystems.newFileSystem(currentJarUri, Collections.emptyMap());
            Path mappingsPath = fileSystem.getPath(MappingConstant.MappingDir.MAPPING_DIR);
            Stream<Path> walk = Files.walk(mappingsPath, MAPPING_WORK_DEPTH);
            Iterator<Path> files = walk.iterator();
            while (files.hasNext()) {
                Path file = files.next();
                if (!isMappingFile(file)) {
                    continue;
                }

                mappings.add(file.toString());
                log.info("Found mapping: {}", file);
            }
        } catch (URISyntaxException | IOException e) {
            log.error("No mapping files found", e);
        }

        return mappings;
    }

    private static void updateMappingFiles(Path mappingStorePath, List<String> mappingFiles) {
        if (mappingFiles.isEmpty()) {
            return;
        }

        for (String mappingFile : mappingFiles) {
            copyMappingFile(mappingStorePath, mappingFile);
        }
    }

    private static void copyMappingFile(Path mappingStorePath, String mappingFile) {
        try (InputStream mappingStream = MappingInitializer.class.getResourceAsStream(mappingFile)) {
            Path mappingFilePath = Paths.get(mappingFile).getFileName();
            if (Objects.isNull(mappingFilePath)) {
                log.error("Copy mapping files error");
                return;
            }

            Files.copy(mappingStream, Paths.get(mappingStorePath.toString(), mappingFilePath.toString()));
        } catch (IOException e) {
            log.error("Copy mapping files error", e);
        }
    }
}
