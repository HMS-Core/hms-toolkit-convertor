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
import com.huawei.generator.g2x.po.map.ExDesc;

/**
 * ExPackage: used as a description container for packages which
 * used by source code analysis engine to catch special cases.
 * For example, when we add firebase into this field, source code engine will use this package
 * to catch all the firebase'elements beyond our auto-and-manual mapping relations.
 * We should incrementally fulfill g2x_manual_extension.json.
 * including:
 * 1. deletedPackageName -> target package name
 * 2. desc -> details of this class, see {@link ExDesc}
 * Note: currently, there are two packages that gms and firebase.
 *
 * @since 2019-11-27
 */
public class ExPackage {
    @Expose
    private String deletedPackageName;

    @Expose
    private ExDesc desc;

    public ExPackage() {
    }

    public ExPackage(String deletedPackageName, ExDesc desc) {
        this.deletedPackageName = deletedPackageName;
        this.desc = desc;
    }

    public String getDeletedPackageName() {
        return deletedPackageName;
    }

    public void setDeletedPackageName(String deletedPackageName) {
        this.deletedPackageName = deletedPackageName;
    }

    public ExDesc getDesc() {
        return desc;
    }

    public void setDesc(ExDesc desc) {
        this.desc = desc;
    }
}
