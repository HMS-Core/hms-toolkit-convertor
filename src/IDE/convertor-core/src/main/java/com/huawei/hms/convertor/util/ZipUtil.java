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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ZipUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ZipUtil.class);

    private static final int BUFFER = 512;

    /**
     * Compressed file
     *
     * @param dirPath Compress the source file path.
     * @param fileName Compressed target file path
     **/
    public static void compress(String dirPath, String fileName) {
        String zipFileName = fileName + Constant.EXTENSION_ZIP; // Adding the file name extension

        File dirFile = FileUtils.getFile(dirPath);
        List<File> fileList = getAllFile(dirFile);

        ZipEntry zipEntry = null;

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            byte[] buffer = new byte[BUFFER];
            for (File file : fileList) {
                if (file.isFile()) { // if there is file,Compress it
                    dealEntry(dirPath, file, zos, buffer);
                } else { // Write to the zip entry.
                    zipEntry = new ZipEntry(getRelativePath(dirPath, file) + Constant.SEPARATOR);
                    zos.putNextEntry(zipEntry);
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error("File not found when compress file");
        } catch (IOException e) {
            LOG.error("Exception occur when compress file", e);
        }
    }

    private static void dealEntry(String dirPath, File file, ZipOutputStream zos, byte[] buffer) {
        ZipEntry zipEntry = new ZipEntry(getRelativePath(dirPath, file));
        zipEntry.setSize(file.length());
        zipEntry.setTime(file.lastModified());

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            zos.putNextEntry(zipEntry);
            int readLength = 0; // Indicates the length of the read data.
            while ((readLength = is.read(buffer, 0, BUFFER)) != -1) {
                zos.write(buffer, 0, readLength);
            }
        } catch (FileNotFoundException e) {
            LOG.error("File not found when deal with entry");
        } catch (IOException e) {
            LOG.error("Exception occur when deal with entry", e);
        }
    }

    /**
     * unzip
     */
    public static void decompress(String zipFileName, String destPath) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName))) {
            dealWithDecompressEntry(zis, destPath);
        } catch (FileNotFoundException e) {
            LOG.error("File not found when decompress");
        } catch (IOException e) {
            LOG.error("Exception occur when decompress", e);
        }
    }

    private static void dealWithDecompressEntry(ZipInputStream zis, String destPath) {
        ZipEntry zipEntry = null;
        byte[] buffer = new byte[BUFFER];
        int readLength = 0; // Indicates the length of the read data.
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
                    LOG.error("Exception occur when deal with create file", e);
                }
            }
        } catch (IOException e) {
            LOG.error("Exception occur when deal with decompressEntry", e);
        }
    }

    /**
     * Obtains all files in the source file path.
     *
     * @param dirFile Compress the source file path.
     */
    public static List<File> getAllFile(File dirFile) {
        List<File> fileList = new ArrayList<>();

        File[] files = dirFile.listFiles();
        if (null == files) {
            return fileList;
        }
        for (File file : files) {
            if (file.isFile()) {
                fileList.add(file);
            } else {
                File[] fileArray = file.listFiles();
                if (null == fileArray) {
                    continue;
                }
                if (fileArray.length != 0) {
                    fileList.addAll(getAllFile(file)); // Add the recursive file to the fileList.
                } else { // Empty directory
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    /**
     * Obtaining the relative path
     *
     * @param dirPath Source File Path
     * @param tarFile Prepare a single file to be compressed.
     */
    private static String getRelativePath(String dirPath, File tarFile) {
        File dirFile = FileUtils.getFile(dirPath);
        File file = tarFile;
        String relativePath = file.getName();

        while (true) {
            file = file.getParentFile();
            if (file == null) {
                break;
            }
            if (file.equals(dirFile)) {
                break;
            } else {
                relativePath = file.getName() + Constant.SEPARATOR + relativePath;
            }
        }
        return relativePath;
    }

    /**
     * Create file.
     *
     * @param destPath Destination Path
     * @param fileName Relative path of the decompressed file.
     */
    public static File createFile(String destPath, String fileName) {
        String[] dirs = fileName.split(Constant.SEPARATOR); // Break down the directories of each level of file names.
        File file = FileUtils.getFile(destPath);

        if (dirs.length > 1) { // The file has an upper-level directory.
            for (int i = 0; i < dirs.length - 1; i++) {
                // Create file objects one by one to know the upper-level directory of the file.
                file = FileUtils.getFile(file, dirs[i]);
            }

            if (!file.exists()) {
                boolean result = file.mkdirs(); // If the directory corresponding to the file does not exist, create it.
                if (!result) {
                    LOG.error("mkdirs failed");
                }
            }

            file = FileUtils.getFile(file, dirs[dirs.length - 1]); // create file
            return file;
        } else {
            if (!file.exists()) { // If the target directory does not exist, create it.
                boolean result = file.mkdirs();
                if (!result) {
                    LOG.error("mkdirs failed");
                }
            }
            file = FileUtils.getFile(file, dirs[0]); // Create file.
            return file;
        }
    }
}
