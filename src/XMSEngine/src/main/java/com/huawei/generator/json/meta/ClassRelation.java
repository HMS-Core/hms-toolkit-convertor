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

package com.huawei.generator.json.meta;

import com.google.gson.annotations.SerializedName;

/**
 * Relation class
 * 
 * @since 2019-11-22
 */
public class ClassRelation {
    @SerializedName("gmsClassName")
    private String gmsClassName;

    @SerializedName("hmsClassName")
    private String hmsClassName;

    @SerializedName("xmsClassName")
    private String xmsClassName;

    public String getGmsClassName() {
        return gmsClassName;
    }

    public void setGmsClassName(String gmsClassName) {
        this.gmsClassName = gmsClassName;
    }

    public String getHmsClassName() {
        return hmsClassName;
    }

    public void setHmsClassName(String hmsClassName) {
        this.hmsClassName = hmsClassName;
    }

    public String getXmsClassName() {
        return xmsClassName;
    }

    public void setXmsClassName(String xmsClassName) {
        this.xmsClassName = xmsClassName;
    }

    @Override
    public String toString() {
        return "JUtilRelations{" + "gmsClassName='" + gmsClassName + '\'' + ", hmsClassName='" + hmsClassName + '\''
            + ", xmsClassName='" + xmsClassName + '\'' + '}';
    }
}
