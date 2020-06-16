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

package com.huawei.generator;

import com.huawei.generator.build.ClassMappingManager;
import com.huawei.generator.build.KClassGen;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Pre-build
 *
 * @since 2019-02-07
 */
public class PreBuild {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreBuild.class);

    private static final String USR_DIR = System.getProperty("user.dir");

    private static final String JSON_PATH = String.join(File.separator, USR_DIR, "target", "classes", "xms", "json");

    private static final String FIREBASE_PATH =
        String.join(File.separator, USR_DIR, "target", "classes", "xms", "agc-json");

    @Test
    public void preBuild() {
        LOGGER.debug("Starting pre-build");
        KClassGen.INSTANCE.generate();
        new ClassMappingManager().saveMap(JSON_PATH, FIREBASE_PATH);
    }
}
