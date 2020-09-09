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

/**
 * API generation statistics
 *
 * @since 2019-12-08
 */

public class ApiStats {
    private int total;

    /**
     * Number of APIs in the class that is filtered out by *
     */
    private int filterByWild;

    private int notSupported;

    /**
     * Number of APIs in the notSupport and isDeprecated states that are converted
     */
    private int fakeApis;

    /**
     * Total number of filtered APIs = Number of APIs in the JSON file + filterByWild
     */
    private int totalFilterd;

    private int generated;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getGenerated() {
        return generated;
    }

    public void setGenerated(int generated) {
        this.generated = generated;
    }

    public int getFilterByWild() {
        return filterByWild;
    }

    public void setFilterByWild(int filterByWild) {
        this.filterByWild = filterByWild;
    }

    public int getTotalFilterd() {
        return totalFilterd;
    }

    public void setTotalFilterd(int totalFilterd) {
        this.totalFilterd = totalFilterd;
    }

    public int getNotSupported() {
        return notSupported;
    }

    public void setNotSupported(int notSupported) {
        this.notSupported = notSupported;
    }

    public int getFakeApis() {
        return fakeApis;
    }

    public void setFakeApis(int fakeApis) {
        this.fakeApis = fakeApis;
    }
}
