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

package com.huawei.generator.g2x.po.map.auto;

import com.google.gson.annotations.Expose;

import java.util.LinkedList;
import java.util.List;

/**
 * Auto: container of auto-fields, methods and classes.
 * The result will be printed into "wisehub-auto.json".
 *
 * @since 2019-11-27
 */
public class Auto {
    @Expose
    private List<AutoClass> autoClasses = new LinkedList<>();

    @Expose
    private List<AutoMethod> autoMethods = new LinkedList<>();

    @Expose
    private List<AutoField> autoFields = new LinkedList<>();

    public Auto() {
    }

    public List<AutoClass> getAutoClasses() {
        return autoClasses;
    }

    public List<AutoMethod> getAutoMethods() {
        return autoMethods;
    }

    public List<AutoField> getAutoFields() {
        return autoFields;
    }
}
