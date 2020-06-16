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

package com.huawei.hms.convertor.core.result.diff;

import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;

import java.util.List;


/**
 * Function Description
 *
 * @since 2020-02-27
 */
public final class Dependency {
    private String version;

    private String originGoogleName;

    private List<String> addDependenciesName;

    private ConversionPointDesc descAuto;

    private ConversionPointDesc descManual;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOriginGoogleName() {
        return originGoogleName;
    }

    public void setOriginGoogleName(String originGoogleName) {
        this.originGoogleName = originGoogleName;
    }

    public List<String> getAddDependenciesName() {
        return addDependenciesName;
    }

    public void setAddDependenciesName(List<String> addDependenciesName) {
        this.addDependenciesName = addDependenciesName;
    }

    public ConversionPointDesc getDescAuto() {
        return descAuto;
    }

    public void setDescAuto(ConversionPointDesc descAuto) {
        this.descAuto = descAuto;
    }

    public ConversionPointDesc getDescManual() {
        return descManual;
    }

    public void setDescManual(ConversionPointDesc descManual) {
        this.descManual = descManual;
    }
}
