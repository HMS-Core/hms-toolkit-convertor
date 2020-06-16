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
import com.huawei.generator.g2x.po.map.Desc;

/**
 * AutoClass: description of auto-class, indicating wishhub plugins can modified them directly.
 * Such as G.ClassG can be replaced by X.ClassX, directly.
 * These fields will be put into wisehub-auto.json#autoClasses.
 * including:
 * 1. oldClassName -> old class name - g
 * 2. newClassName -> new class name - x
 * 3. desc -> details of this class, see {@link Desc}
 * Note: For auto class, the following fields of desc should be filled by us according to a default value:
 * 1. desc'text: which will be replace by which
 *
 * @since 2019-11-27
 */
public class AutoClass {
    @Expose
    private String oldClassName;

    @Expose
    private String newClassName;

    @Expose
    private Desc desc;

    public AutoClass(String oldClassName, String newClassName, Desc desc) {
        this.oldClassName = oldClassName;
        this.newClassName = newClassName;
        this.desc = desc;
    }

    // reserve for document
    public AutoClass() {
        this.oldClassName = "gms.Name";
        this.newClassName = "xms.Name";
        this.desc = new Desc();
    }

    public String getOldClassName() {
        return oldClassName;
    }

    public String getNewClassName() {
        return newClassName;
    }

    public Desc getDesc() {
        return desc;
    }

    public void setOldClassName(String oldClassName) {
        this.oldClassName = oldClassName;
    }

    public void setNewClassName(String newClassName) {
        this.newClassName = newClassName;
    }

    public void setDesc(Desc desc) {
        this.desc = desc;
    }
}
