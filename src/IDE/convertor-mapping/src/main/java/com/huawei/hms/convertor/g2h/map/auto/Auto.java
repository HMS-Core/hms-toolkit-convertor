/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.g2h.map.auto;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * support automatic convert
 *
 * @since 2020-07-06
 */
public class Auto {
    @Expose
    private List<AutoClass> autoClasses = new LinkedList<>();

    @Expose
    private List<Object> autoMethods = new LinkedList<>();

    @Expose
    private List<AutoField> autoFields = new LinkedList<>();

    @Expose
    private ArrayList autoPackages = new ArrayList();

    public List<AutoClass> getAutoClasses() {
        return autoClasses;
    }

    public List<Object> getAutoMethods() {
        return autoMethods;
    }

    public List<AutoField> getAutoFields() {
        return autoFields;
    }

    public Object getOneAutoMethod(int i) {
        return autoMethods.get(i);
    }

    public void setAutoMethods(List<Object> newAutoMethods) {
        autoMethods = newAutoMethods;
    }

    public void setOneAutoMethod(Object oneMethod) {
        autoMethods.add(oneMethod);
    }

    public ArrayList getAutoPackages() {
        return autoPackages;
    }

}
