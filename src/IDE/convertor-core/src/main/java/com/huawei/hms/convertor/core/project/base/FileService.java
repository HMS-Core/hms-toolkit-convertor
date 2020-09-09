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

package com.huawei.hms.convertor.core.project.base;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Platform service interface
 *
 * @since 2020-03-23
 */
public interface FileService {
    /**
     * Copy fromDir to toDir
     *
     * @param fromDir from file
     * @param toDir to file
     * @throws IOException file IO exception
     */
    void copyDir(File fromDir, File toDir) throws IOException;

    /**
     * Copy fromDir to toDir with filter
     *
     * @param fromDir from file
     * @param toDir to file
     * @param fileFilter file filter
     * @throws IOException file IO exception
     */
    void copyDirWithFilter(File fromDir, File toDir, FileFilter fileFilter) throws IOException;

    /**
     * Pre process and refresh
     *
     * @param folderName cache folder name
     */
    void preProcess(String folderName);

    /**
     * delete file use intellij interface
     *
     * @param file file
     */
    void delFile(File file);
}
