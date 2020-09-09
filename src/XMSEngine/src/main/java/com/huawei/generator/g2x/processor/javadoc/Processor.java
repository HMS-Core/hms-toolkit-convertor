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

package com.huawei.generator.g2x.processor.javadoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Unzip web javadoc to user's path
 *
 * @since 2020-07-10
 */
public class Processor {
    private static final String USR_DIR = System.getProperty("user.dir");

    private static final String FILE_NAME = "javadoc_web.zip";

    private static final File ZIP_FILE = new File(USR_DIR + File.separator
        + String.join(File.separator, "src", "main", "resources", "javadoc-web", FILE_NAME));

    private static final String USR_HOME = System.getProperty("user.home");

    private static final String TARGET_PATH =
        USR_HOME + File.separator + ".hmstoolkit" + File.separator + "convertor" + File.separator;

    public static void unZipFiles() {
        try {
            ZipFile zip = new ZipFile(ZIP_FILE, Charset.forName("GBK"));
            String name = zip.getName()
                .substring(zip.getName().lastIndexOf('\\') + 1, zip.getName().lastIndexOf('.'));

            File pathFile = new File(TARGET_PATH + name);
            if (!pathFile.exists()) {
                boolean mkdirs = pathFile.mkdirs();
                if (!mkdirs) {
                    throw new IllegalStateException("Fail to mkdirs");
                }
            }
            outputFiles(zip);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to unzip web javadoc to user path");
        }
    }

    public static void unZipFilesJar(String pluginPath) {
        try (ZipFile zipFile = new ZipFile(pluginPath);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(pluginPath)))) {
            ZipEntry nextEntry = zip.getNextEntry();
            while (nextEntry != null) {
                if (nextEntry.getName().endsWith(FILE_NAME)) {
                    InputStream inputstream = zipFile.getInputStream(nextEntry);
                    copyResource(inputstream);
                    break;
                }
                nextEntry = zip.getNextEntry();
            }

            File pathFile = new File(TARGET_PATH + "javadoc_web");
            if (!pathFile.exists()) {
                boolean mkdirs = pathFile.mkdirs();
                if (!mkdirs) {
                    throw new IllegalStateException("Fail to mkdirs");
                }
            }
            ZipFile javaDocZip = new ZipFile(TARGET_PATH + File.separator + FILE_NAME);
            outputFiles(javaDocZip);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to unzip web javadoc to user path");
        }
    }

    private static void copyResource(InputStream ins) {
        File target = new File(TARGET_PATH, FILE_NAME);
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            return;
        }
        byte[] buffer = new byte[1024];
        int bytesRead;
        try (FileOutputStream fos = new FileOutputStream(target)) {
            while ((bytesRead = ins.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Fail to copy unzip web javadoc to user path");
        }
    }

    private static void outputFiles(ZipFile zip) throws IOException {
        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) ((Enumeration) entries).nextElement();
            String zipEntryName = entry.getName();
            try (InputStream in = zip.getInputStream(entry)) {
                String outPath = (TARGET_PATH + zipEntryName).replaceAll("\\*", "/");
                File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!file.exists()) {
                    boolean mkdirs = file.mkdirs();
                    if (!mkdirs) {
                        throw new IllegalStateException("Fail to mkdirs");
                    }
                }

                if (new File(outPath).isDirectory()) {
                    continue;
                }
                try (FileOutputStream out = new FileOutputStream(outPath)) {
                    byte[] buf1 = new byte[1024];
                    int len;
                    while ((len = in.read(buf1)) > 0) {
                        out.write(buf1, 0, len);
                    }
                }
            }
        }
    }
}
