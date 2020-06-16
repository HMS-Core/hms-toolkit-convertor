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

package com.huawei.generator.gen;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test for XmsClassCollector
 *
 * @since 2019-12-01
 */
public class XmsClassCollectorTest {
    @Test
    public void testOnClass() {
        List<String> zzz = new ArrayList<>();
        zzz.add("com.google.android.gms.tasks.CancellationToken");
        Set<String> clazz = XmsClassCollector.collectXmsClass(zzz);
        Set<String> result = new HashSet<>();
        result.add("org.xms.g.tasks.CancellationToken");
        result.add("org.xms.g.tasks.OnTokenCanceledListener");
        Assert.assertEquals(clazz.size(), result.size());
        for (String xxx : clazz) {
            Assert.assertTrue(result.contains(xxx));
        }
    }
}
