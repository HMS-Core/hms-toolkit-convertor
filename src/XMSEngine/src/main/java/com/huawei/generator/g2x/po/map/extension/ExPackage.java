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

import com.huawei.generator.g2x.po.map.ExtensionDesc;

import com.google.gson.annotations.Expose;

/**
 * ExPackage: used as a description container for packages which
 * used by source code analysis engine to catch special cases.
 * For example, when we add firebase into this field, source code engine will use this package
 * to catch all the firebase'elements beyond our auto-and-manual mapping relations.
 * We should incrementally fulfill g2x_manual_extension.json.
 * <p>
 * including:
 * 1. deletedPackageName means target package name
 * 2. desc means details of this class, see {@link ExtensionDesc}
 * </p>
 * <p>
 * Note: currently, there are two packages that gms and firebase.
 * </p>
 *
 * @since 2019-11-27
 */
public class ExPackage {
    @Expose
    private String deletedPackageName;

    @Expose
    private ExtensionDesc desc;

    public ExPackage() {
    }

    public ExPackage(String deletedPackageName, ExtensionDesc desc) {
        this.deletedPackageName = deletedPackageName;
        this.desc = desc;
    }

    public String getDeletedPackageName() {
        return deletedPackageName;
    }

    public void setDeletedPackageName(String deletedPackageName) {
        this.deletedPackageName = deletedPackageName;
    }

    public ExtensionDesc getDesc() {
        return desc;
    }

    public void setDesc(ExtensionDesc desc) {
        this.desc = desc;
    }
}
