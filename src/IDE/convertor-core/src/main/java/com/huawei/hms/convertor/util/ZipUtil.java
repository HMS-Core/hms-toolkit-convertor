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

package com.huawei.hms.convertor.util;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public final class ZipUtil {
    private static final int BUFFER = 512;

    /**
     * Compress
     *
     * @param dirPath Source file path.
     * @param fileName Target file path.
     **/
    public static void compress(String dirPath, String fileName) {
        String zipFileName = fileName + Constant.EXTENSION_ZIP;

        File dirFile = FileUtils.getFile(dirPath);
        List<File> fileList = getAllFile(dirFile);

        ZipEntry zipEntry;

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            byte[] buffer = new byte[BUFFER];
            for (File file : fileList) {
                Optional<String> relativePath = getRelativePath(dirPath, file);
                if (!relativePath.isPresent()) {
                    log.error("relative path max level exceed, fileName: {}.", file.getName());
                    return;
                }

                if (file.isFile()) {
                    dealEntry(relativePath.get(), file, zos, buffer);
                } else {
                    zipEntry = new ZipEntry(relativePath.get() + Constant.UNIX_FILE_SEPARATOR);
                    zos.putNextEntry(zipEntry);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("File not found when compress file", e);
        } catch (IOException e) {
            log.error("Exception occur when compress file", e);
        }
    }

    private static void dealEntry(String relativePath, File file, ZipOutputStream zos, byte[] buffer) {
        ZipEntry zipEntry = new ZipEntry(relativePath);
        zipEntry.setSize(file.length());
        zipEntry.setTime(file.lastModified());

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            zos.putNextEntry(zipEntry);
            int readLength;
            while ((readLength = is.read(buffer, 0, BUFFER)) != -1) {
                zos.write(buffer, 0, readLength);
            }
        } catch (FileNotFoundException e) {
            log.error("File not found when deal with entry", e);
        } catch (IOException e) {
            log.error("Exception occur when deal with entry", e);
        }
    }

    /**
     * Decompress
     */
    public static void decompress(String zipFileName, String destPath) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName))) {
            dealWithDecompressEntry(zis, destPath);
        } catch (FileNotFoundException e) {
            log.error("File not found when decompress", e);
        } catch (IOException e) {
            log.error("Exception occur when decompress", e);
        }
    }

    /**
     * Obtain all files in the source file path.
     *
     * @param dirFile Compress path.
     */
    public static List<File> getAllFile(File dirFile) {
        List<File> fileList = new ArrayList<>();

        File[] files = dirFile.listFiles();
        if (files == null) {
            return fileList;
        }
        for (File file : files) {
            if (file.isFile()) {
                fileList.add(file);
            } else {
                File[] fileArray = file.listFiles();
                if (fileArray == null) {
                    continue;
                }
                if (fileArray.length != 0) {
                    fileList.addAll(getAllFile(file));
                } else {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    /**
     * Create file.
     *
     * @param destPath Destination path.
     * @param fileName Relative path of the decompressed file.
     */
    public static File createFile(String destPath, String fileName) {
        String[] dirs = fileName.split(Constant.UNIX_FILE_SEPARATOR);
        File file = FileUtils.getFile(destPath);

        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                file = FileUtils.getFile(file, dirs[i]);
            }

            if (!file.exists()) {
                boolean result = file.mkdirs();
                if (!result) {
                    log.error("mkdirs failed");
                }
            }

            file = FileUtils.getFile(file, dirs[dirs.length - 1]);
            return file;
        } else {
            if (!file.exists()) {
                boolean result = file.mkdirs();
                if (!result) {
                    log.error("mkdirs failed");
                }
            }
            file = FileUtils.getFile(file, dirs[0]);
            return file;
        }
    }

    private static void dealWithDecompressEntry(ZipInputStream zis, String destPath) {
        ZipEntry zipEntry;
        byte[] buffer = new byte[BUFFER];
        int readLength;
        try {
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    Path filePath = Paths.get(destPath, zipEntry.getName());
                    if (Files.notExists(filePath)) {
                        Files.createDirectories(filePath);
                    }
                    continue;
                }

                File file = createFile(destPath, zipEntry.getName());
                try (OutputStream os = new FileOutputStream(file)) {
                    while ((readLength = zis.read(buffer, 0, BUFFER)) != -1) {
                        os.write(buffer, 0, readLength);
                    }
                } catch (IOException e) {
                    log.error("Exception occur when deal with create file", e);
                }
            }
        } catch (IOException e) {
            log.error("Exception occur when deal with decompressEntry", e);
        }
    }

    /**
     * Obtain the relative path.
     *
     * @param dirPath Source file path.
     * @param tarFile Compress file.
     */
    private static Optional<String> getRelativePath(String dirPath, File tarFile) {
        File dirFile = FileUtils.getFile(dirPath);
        File file = tarFile;
        String relativePath = file.getName();

        int pathMaxLevel = Integer.parseInt(PropertyUtil.readProperty("relative_path_max_level"));
        int currentLevel = 0;
        while (currentLevel < pathMaxLevel) {
            file = file.getParentFile();
            currentLevel++;
            if (file == null) {
                break;
            }
            if (file.equals(dirFile)) {
                break;
            } else {
                relativePath = file.getName() + Constant.UNIX_FILE_SEPARATOR + relativePath;
            }
        }
        if (currentLevel >= pathMaxLevel) {
            log.error("relative path max level exceed, fileName: {}.", tarFile.getName());
            return Optional.empty();
        }

        return Optional.of(relativePath);
    }
}
