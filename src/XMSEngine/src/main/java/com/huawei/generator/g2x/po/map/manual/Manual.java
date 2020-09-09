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

package com.huawei.generator.g2x.po.map.manual;

import com.huawei.generator.g2x.po.map.extension.ExPackage;

import com.google.gson.annotations.Expose;

import java.util.LinkedList;
import java.util.List;

/**
 * Manual: container of manual-fields, methods and classes.
 * The result will be printed into "wisehub-manual.json".
 *
 * @since 2019-11-27
 */
public class Manual {
    @Expose
    private List<ManualClass> manualClasses = new LinkedList<>();

    @Expose
    private List<ManualMethod> manualMethods = new LinkedList<>();

    @Expose
    private List<ManualField> manualFields = new LinkedList<>();

    @Expose
    private List<ExPackage> manualPackages = new LinkedList<>();

    public List<ManualClass> getManualClasses() {
        return manualClasses;
    }

    public List<ManualMethod> getManualMethods() {
        return manualMethods;
    }

    public List<ManualField> getManualFields() {
        return manualFields;
    }

    public List<ExPackage> getManualPackages() {
        return manualPackages;
    }
}
