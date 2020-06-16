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
 * AutoField: description of auto-fields, indicating wishhub plugins can modified them directly.
 * Such as G.ClassG.FieldG can be replaced by X.ClassX.FieldX, directly.
 * These fields will be put into wisehub-auto.json#autoFields.
 * including:
 * 1. oldFieldName -> old field name - g
 * 2. newFieldName -> new field name - x
 * 3. desc -> details of this field, see {@link Desc}
 * Note: For auto field, the following fields of desc should be filled by us according to a default value:
 * 1. desc'text: which will be replaced by which, IMPORTANT, this field can not be used in switch-case statement,
 *      for x provides a getter method which is not a constant
 *
 * @since 2019-11-27
 */
public class AutoField {
    @Expose
    private String oldFieldName;

    @Expose
    private String newFieldName;

    @Expose
    private Desc desc;

    public AutoField(String oldClassName, String newClassName, Desc desc) {
        this.oldFieldName = oldClassName;
        this.newFieldName = newClassName;
        this.desc = desc;
    }

    // reserve for document
    public AutoField() {
        this.oldFieldName = "gms.FirebaseAnalytics.Event.ADD_PAYMENT_INFO";
        this.newFieldName = "xms.HAEventType.HA_ADD_PAYMENT_INFO";
        this.desc = new Desc();
    }

    public String getOldFieldName() {
        return oldFieldName;
    }

    public String getNewFieldName() {
        return newFieldName;
    }

    public Desc getDesc() {
        return desc;
    }

    public void setOldFieldName(String oldFieldName) {
        this.oldFieldName = oldFieldName;
    }

    public void setNewFieldName(String newFieldName) {
        this.newFieldName = newFieldName;
    }

    public void setDesc(Desc desc) {
        this.desc = desc;
    }
}
