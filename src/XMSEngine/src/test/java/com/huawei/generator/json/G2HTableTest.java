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

package com.huawei.generator.json;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @since 2019-11-27
 */
public class G2HTableTest {
    @Test
    public void generateTableJson() {
        Set<String> whiteMethods = new HashSet<>();
        whiteMethods.add("connect");
        whiteMethods.add("resume");
        Map<String, Set<String>> whites = new HashMap<>();
        whites.put("com.google.android.gms.location.Client", whiteMethods);
        Set<String> blackMethods = new HashSet<>();
        blackMethods.add("disconnect");
        Map<String, Set<String>> blacks = new HashMap<>();
        blacks.put("com.google.android.gms.location.Client", blackMethods);

        G2HTable table = new G2HTable();
        table.setWhites(whites);
        table.setBlacks(blacks);
        String json = new Gson().toJson(table);
        G2HTable mirrored = new Gson().fromJson(json, G2HTable.class);
        Assert.assertNotNull(mirrored);
    }
}