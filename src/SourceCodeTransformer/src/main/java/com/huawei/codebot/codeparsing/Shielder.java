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

package com.huawei.codebot.codeparsing;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains regions that should be excluded during fixing.
 *
 * @since 2020-02-17
 */
public class Shielder {
    private List<Pair<Integer, Integer>> regions = new ArrayList<>();

    public Shielder() {}

    /**
     * add the region that should be excluded during fixing.
     *
     * @param start the start line number of the region
     * @param end the end line number of the region
     */
    public void addRegion(int start, int end) {
        Pair<Integer, Integer> region = Pair.of(start, end);
        addRegion(region);
    }

    private void addRegion(Pair<Integer, Integer> region) {
        this.regions.add(region);
    }

    /**
     * check whether the given line is in one of the regions
     *
     * @param lineNum the given line number
     * @return true if it is in the region.
     */
    public boolean shouldIgnore(int lineNum) {
        for (Pair<Integer, Integer> region : regions) {
            if (lineNum >= region.getLeft() && lineNum <= region.getRight()) {
                return true;
            }
        }
        return false;
    }

    /**
     * check whether the given region(start and end) has intersection with one of the regions in the shielder
     *
     * @param start the start line number of the given region
     * @param end the end line number of the given region
     * @return true if it has intersection
     */
    public boolean shouldIgnore(int start, int end) {
        for (Pair<Integer, Integer> region : regions) {
            if (start <= region.getLeft() && end >= region.getLeft()) {
                return true;
            }
            if (start >= region.getLeft() && start <= region.getRight()) {
                return true;
            }
        }
        return false;
    }
}
