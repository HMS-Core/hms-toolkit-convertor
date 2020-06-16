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
 * white/black list for G method
 * 
 * @since 2019-11-27
 */
public class G2HTable {
    @SerializedName("whites")
    private Map<String, Set<String>> whites = new HashMap<>();

    @SerializedName("blacks")
    private Map<String, Set<String>> blacks = new HashMap<>();

    public Map<String, Set<String>> getWhites() {
        return whites;
    }

    public void setWhites(Map<String, Set<String>> whites) {
        this.whites = whites;
    }

    public Map<String, Set<String>> getBlacks() {
        return blacks;
    }

    public void setBlacks(Map<String, Set<String>> blacks) {
        this.blacks = blacks;
    }

    /**
     * merge black and white list
     * 
     * @param table the data tobe merged
     */
    public void merge(G2HTable table) {
        blacks.putAll(table.getBlacks());
        whites.putAll(table.getWhites());
    }

    /**
     * clear black and white list
     */
    public void clear() {
        blacks.clear();
        whites.clear();
    }
}
