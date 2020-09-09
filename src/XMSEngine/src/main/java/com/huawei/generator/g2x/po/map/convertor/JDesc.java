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

package com.huawei.generator.g2x.po.map.convertor;

import com.huawei.generator.g2x.po.map.Desc;
import com.huawei.generator.g2x.po.map.MethodDesc;

import com.google.gson.annotations.Expose;

/**
 * json parse base object
 *
 * @since 2019-11-28
 */
public class JDesc extends Desc {
    @Expose
    public String name;

    @Expose
    private boolean autoConvert;

    @Expose
    private String methodName;

    public JDesc(Desc desc) {
        this.text = desc.getText();
        this.url = desc.getUrl();
        this.kit = desc.getKit();
        this.gmsVersion = desc.getGmsVersion();
        this.hmsVersion = desc.getHmsVersion();
        this.autoConvert = "AUTO".equals(desc.getStatus());
        this.methodName = "";
    }

    public JDesc(MethodDesc desc) {
        this.text = desc.getText();
        this.url = desc.getUrl();
        this.kit = desc.getKit();
        this.gmsVersion = desc.getGmsVersion();
        this.hmsVersion = desc.getHmsVersion();
        this.autoConvert = "AUTO".equalsIgnoreCase(desc.getStatus());
        this.methodName = desc.getMethodName();
    }

    public boolean isAutoConvert() {
        return autoConvert;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
