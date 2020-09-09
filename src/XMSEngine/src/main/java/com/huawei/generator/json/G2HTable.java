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

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * trust/block list for G method
 *
 * @since 2019-11-27
 */
public class G2HTable {
    @SerializedName("trusts")
    private Map<String, Set<String>> trusts = new HashMap<>();

    @SerializedName("blocks")
    private Map<String, Set<String>> blocks = new HashMap<>();

    public Map<String, Set<String>> getTrusts() {
        return trusts;
    }

    public void setTrusts(Map<String, Set<String>> trusts) {
        this.trusts = trusts;
    }

    public Map<String, Set<String>> getBlocks() {
        return blocks;
    }

    public void setBlocks(Map<String, Set<String>> blocks) {
        this.blocks = blocks;
    }

    /**
     * merge blocksList and trustList
     * 
     * @param table the data tobe merged
     */
    public void merge(G2HTable table) {
        blocks.putAll(table.getBlocks());
        trusts.putAll(table.getTrusts());
    }

    /**
     * clear blocksList and trustList
     */
    public void clear() {
        blocks.clear();
        trusts.clear();
    }
}
