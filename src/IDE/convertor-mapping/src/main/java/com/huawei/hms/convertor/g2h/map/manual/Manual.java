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

package com.huawei.hms.convertor.g2h.map.manual;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Manual {
    @Expose
    private ArrayList manualClasses = new ArrayList();

    @Expose
    private ArrayList manualMethods = new ArrayList();

    @Expose
    private ArrayList manualFields = new ArrayList();

    @Expose
    private ArrayList manualPackages = new ArrayList();

    public ArrayList getManualClasses() {
        return manualClasses;
    }

    public ArrayList getManualMethods() {
        return manualMethods;
    }

    public ArrayList getManualFields() {
        return manualFields;
    }

    public ArrayList getManualPackages() {
        return manualPackages;
    }

    public ManualMethod getOneManualMethod(int i) {
        return (ManualMethod) manualMethods.get(i);
    }

    public void setOneManualMethod(ManualMethod manual) {
        manualMethods.add(manual);
    }

    public void setManualMethods(List<ManualMethod> manualMethods) {
        this.manualMethods = (ArrayList) manualMethods;
    }
}
