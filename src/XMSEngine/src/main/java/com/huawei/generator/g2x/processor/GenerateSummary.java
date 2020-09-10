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

import com.huawei.generator.g2x.po.summary.Diff;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for generator summary
 *
 * @since 2020-02-24
 */
public class GenerateSummary {
    public GeneratorResult result = GeneratorResult.SUCCESS;

    public Diff diff;

    public List<String> xmsCodePaths = new ArrayList<>();

    public GenerateSummary() {
    }

    public GenerateSummary(GeneratorResult result) {
        this.result = result;
    }

    public List<String> getXmsCodePaths() {
        return xmsCodePaths;
    }

    public void setXmsCodePaths(List<String> xmsCodePaths) {
        this.xmsCodePaths = xmsCodePaths;
    }

    public GeneratorResult getResult() {
        return result;
    }

    public void setResult(GeneratorResult result) {
        this.result = result;
    }

    public Diff getDiff() {
        return diff;
    }

    public void setDiff(Diff diff) {
        this.diff = diff;
    }
}
