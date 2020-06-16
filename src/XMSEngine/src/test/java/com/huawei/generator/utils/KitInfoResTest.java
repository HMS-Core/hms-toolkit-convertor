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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class KitInfoResTest {
    @Test
    public void kitInfoTest() {
        Assert.assertFalse(KitInfoRes.INSTANCE.getKitDependencyMap().isEmpty());
        Assert.assertFalse(KitInfoRes.INSTANCE.getNormalizeKitMap().isEmpty());
        Assert.assertFalse(KitInfoRes.INSTANCE.getUnnormalizeKitMap().isEmpty());
        Assert.assertFalse(KitInfoRes.INSTANCE.getSupportList().isEmpty());
        List<String> allKits = new ArrayList<>();
        allKits.add("Awareness");
        allKits.add("Push");
        allKits.add("Basement");
        Assert.assertFalse(KitInfoRes.INSTANCE.getDefaultSdkVersion(allKits).isEmpty());
    }
}
