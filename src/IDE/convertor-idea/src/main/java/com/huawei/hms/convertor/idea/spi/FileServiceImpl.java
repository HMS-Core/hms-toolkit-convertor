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

package com.huawei.hms.convertor.idea.spi;

import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.FileService;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * File service impl
 *
 * @since 2020-03-20
 */
@Slf4j
public class FileServiceImpl implements FileService {
    private static void deleteCacheFile(File file, final String folderName) {
        if (file.getName().equals(folderName)
            || file.getName().equals(folderName + ProjectConstants.Common.COMMENT_SUFFIX)) {
            File[] resultFiles = file.listFiles();
            if (resultFiles == null) {
                return;
            }
            for (File resultFile : resultFiles) {
                com.huawei.hms.convertor.util.FileUtil.deleteFiles(resultFile);
            }
        }
    }

    @Override
    public void copyDir(File fromDir, File toDir) throws IOException {
        com.intellij.openapi.util.io.FileUtil.copyDir(fromDir, toDir);
    }

    @Override
    public void copyDirWithFilter(File fromDir, File toDir, FileFilter fileFilter) throws IOException {
        FileUtil.copyDir(fromDir, toDir, fileFilter);
    }

    @Override
    public void delFile(File file) {
        com.intellij.openapi.util.io.FileUtil.delete(file);
    }

    @Override
    public void preProcess(String folderName) {
        /* Make plugin cache directory */
        File cacheRootDirectory = new File(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH);
        if (!cacheRootDirectory.exists()) {
            boolean isSuccess = cacheRootDirectory.mkdir();
            if (!isSuccess) {
                log.error("Failed to make directory: {}", PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH);
                return;
            }
        }

        /* Delete the project previous result */
        File[] files = cacheRootDirectory.listFiles();
        if (files == null) {
            log.error("file is null");
            return;
        }
        for (File file : files) {
            deleteCacheFile(file, folderName);
        }
        LocalFileSystem.getInstance().refresh(true);
    }
}
