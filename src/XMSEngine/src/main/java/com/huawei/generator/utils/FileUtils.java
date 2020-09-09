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

import com.huawei.generator.g2x.processor.GeneratorResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Utils foe File
 *
 * @since 2019-11-27
 */
public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * File processor
     */
    public interface FileProcessor {
        /**
         * process one file
         *
         * @param ins input stream
         */
        void process(InputStream ins);
    }

    /**
     * Generating a .json File
     *
     * @param jsonString json string
     * @param filePath   file Path
     * @param fileName   fileName like xxx
     * @return Generator result ,file will be like xxx.json
     */
    public static GeneratorResult createJsonFile(String jsonString, String filePath, String fileName) {
        // Combine the full path of the file.
        String fullPath = File.separator + fileName + ".json";
        return createFile(jsonString, filePath, fullPath);
    }

    /**
     * Generating a file
     *
     * @param string   out put strings
     * @param filePath file Path
     * @param fileName fileName like xxx
     * @return Generator result ,file will be like xxx
     */
    public static GeneratorResult createFile(String string, String filePath, String fileName) {
        // Mark whether the file is generated successfully.
        boolean flag = true;

        // Combine the full path of the file.
        String fullPath = filePath + File.separator + fileName;

        // Generating a JSON File
        try {
            // Ensure that a new file is created.
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) { // If the parent directory does not exist, create it.
                flag = file.getParentFile().mkdirs();
            }
            if (!flag) {
                return GeneratorResult.INVALID_OUTPATH;
            }
            if (file.exists()) { // If yes, delete the old file.
                flag = file.delete();
            }
            if (!flag) {
                return GeneratorResult.INVALID_OUTPATH;
            }
            flag = file.createNewFile();

            // Writes the formatted character string to a file.
            try (Writer write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                write.write(string);
                write.flush();
            } catch (FileNotFoundException e) {
                LOGGER.error("Create output file stream failed!");
            }
        } catch (IOException e) {
            flag = false;
        }

        // Returns a flag indicating whether the operation is successful.
        if (!flag) {
            return GeneratorResult.INVALID_OUTPATH;
        }
        return GeneratorResult.SUCCESS;
    }

    /**
     * output json files
     *
     * @param obj obj to be serialized
     * @param outPath Output Path
     * @param fileName Output File Name
     * @return Output Status Return
     */
    public static GeneratorResult outPutJson(Object obj, String outPath, String fileName) {
        Gson g = new GsonBuilder().setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
        String str = g.toJson(obj);
        return createJsonFile(str, outPath, fileName);
    }

    /**
     * Walk a dir recursively and do some processing to each file.
     *
     * @param dir       the dir to be walked
     * @param processor file processor
     */
    public static void walkDir(File dir, FileProcessor processor) {
        File[] files = dir.listFiles();
        if (files == null) {
            LOGGER.info("{} is not a directory", dir.toString());
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                walkDir(f, processor);
            } else {
                try (InputStream ins = new FileInputStream(f)) {
                    processor.process(ins);
                } catch (FileNotFoundException e) {
                    LOGGER.error("Read input file stream failed!");
                } catch (IOException e) {
                    LOGGER.error("Close resource failed when walking dir!");
                }
            }
        }
    }

    /**
     * find file in root by name, note it may has files with same name.
     * For example, GlobalEnvSetting.java in g+h/g mode
     *
     * @param root       root folder
     * @param target     target
     * @param resultList result container
     */
    public static void findFileByName(File root, String target, List<File> resultList) {
        if (!root.exists()) {
            return;
        }
        File[] files = root.listFiles();
        if (files == null) {
            LOGGER.info("{} is not a directory", root.toString());
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                findFileByName(f, target, resultList);
            } else {
                if (f.getName().contains(target)) {
                    resultList.add(f);
                }
            }
        }
    }

    /**
     * copy between two files
     *
     * @param destFile   target file
     * @param sourceFile source file
     * @return copy result
     * @throws IOException if the file I/O operations are abnormal
     */
    public static boolean copyFile(File destFile, File sourceFile) throws IOException {
        try {
            if (destFile.exists() && !destFile.delete()) {
                return false;
            }
            if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs()) {
                return false;
            }
            Files.copy(sourceFile.toPath(), destFile.toPath());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * delete directory and its sub-dir
     *
     * @param file root directory
     * @return delete result
     */
    public static boolean delFile(File file) {
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            } else {
                for (File f : files) {
                    delFile(f);
                }
            }
        }
        return file.delete();
    }

    /**
     * read file to buffer string
     *
     * @param filePath file path
     * @return content of file
     */
    public static String getFileContent(String filePath) {
        StringBuilder buffer = new StringBuilder();
        try (
                InputStream is = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)
        ) {
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                buffer.append("\n");
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Read input file failed!");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("The Character Encoding is not supported.");
        } catch (IOException e) {
            LOGGER.error("Read line from reader or close resource failed!");
        }
        return buffer.toString();
    }

    /**
     * get list of all java file paths
     *
     * @param dir directory path
     * @return list of all file paths
     */
    public static List<String> listAllFiles(String dir) {
        List<String> paths = new ArrayList<>();
        try {
            File f = new File(dir);
            File[] files = f.listFiles();
            if (files == null || files.length == 0) {
                return paths;
            }
            for (File file : files) {
                if (file.isFile()) {
                    paths.add(file.getCanonicalPath());
                } else {
                    paths.addAll(listAllFiles(file.getCanonicalPath()));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Get canonical path failed!");
        }
        return paths;
    }

    /**
     * judge whether input entry is Json for generating xms code
     *
     * @param entry input
     * @return true if input is target Json
     */
    public static boolean isJson(ZipEntry entry) {
        return (entry.getName().startsWith("xms/json") || entry.getName().startsWith("xms/agc-json"))
            && entry.getName().endsWith(".json");
    }
}