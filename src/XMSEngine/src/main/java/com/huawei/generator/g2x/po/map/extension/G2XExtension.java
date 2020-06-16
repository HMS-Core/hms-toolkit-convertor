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

package com.huawei.generator.g2x.po.map.extension;

import com.google.gson.annotations.Expose;

import java.util.LinkedList;
import java.util.List;

/**
 * G2XExtension: used as a description container for extension mapping relations
 * used by source code analysis engine to catch special cases.
 *
 * @since 2019-11-27
 */
public class G2XExtension {
    @Expose
    private List<ExPackage> manualPackages = new LinkedList<>();

    public G2XExtension() {
    }

    public List<ExPackage> getManualPackages() {
        return manualPackages;
    }

    public void setManualPackages(List<ExPackage> manualPackages) {
        this.manualPackages = manualPackages;
    }
}
