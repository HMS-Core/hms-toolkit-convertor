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

package com.huawei.generator.g2x.processor;

import org.junit.After;
import org.junit.Before;

/**
 * test MapBaseProcessor
 *
 * @since 2020-01-07
 */
public class MapBaseProcessorTest {
    @Before
    public void setup() {
        enableBlackList();
    }

    @After
    public void destroy() {
        disableBlackList();
    }

    private void enableBlackList() {
        System.setProperty("enable_black_list", "true");
    }

    private void disableBlackList() {
        System.setProperty("enable_black_list", "");
    }
}
