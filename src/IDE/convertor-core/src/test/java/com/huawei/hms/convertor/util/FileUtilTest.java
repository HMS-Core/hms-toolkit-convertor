/*
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
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

import groovy.util.logging.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
public class FileUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testDeleteFiles_dir() throws IOException {
        final File testDirectory = new File(temporaryFolder.newFolder(), "testDeleteQuietlyDir");
        final File testFile = new File(testDirectory, "testDeleteQuietlyFile");
        testDirectory.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final OutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testFile.toPath()));
        try {
            generateTestData(output, 0);
        } finally {
            output.close();
        }
        assertTrue(testDirectory.exists());
        assertTrue(testFile.exists());
        FileUtil.deleteFiles(testDirectory);
        assertFalse("Check No Exist", testDirectory.exists());
        assertFalse("Check No Exist", testFile.exists());
    }

    @Test
    public void testDeleteFiles_file() throws IOException {
        final File testFile = new File(temporaryFolder.newFolder(), "testDeleteQuietlyFile");
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final OutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testFile.toPath()));
        try {
            generateTestData(output, 0);
        } finally {
            output.close();
        }
        assertTrue(testFile.exists());
        FileUtil.deleteFiles(testFile);
        assertFalse("Check No Exist", testFile.exists());
    }


    private static void generateTestData(final OutputStream out, final long size) throws IOException {
        for (int i = 0; i < size; i++) {
            out.write((byte) ((i % 127) + 1));
        }
    }
}