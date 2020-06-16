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

import com.google.gson.annotations.Expose;
import com.huawei.generator.g2x.po.map.Desc;
import com.huawei.generator.g2x.po.map.MDesc;

/**
 * json parse base object
 *
 * @since 2019-11-28
 */
public class JDesc {
    @Expose
    private String text;

    @Expose
    private String url;

    @Expose
    private String kit;

    @Expose
    private String gmsVersion;

    @Expose
    private String hmsVersion;

    @Expose
    private String methodName;

    public JDesc(Desc desc) {
        this.text = desc.getText();
        this.url = desc.getUrl();
        this.kit = desc.getKit();
        this.gmsVersion = desc.getGmsVersion();
        this.hmsVersion = desc.getHmsVersion();
        this.methodName = "";
    }

    public JDesc(MDesc desc) {
        this.text = desc.getText();
        this.url = desc.getUrl();
        this.kit = desc.getKit();
        this.gmsVersion = desc.getGmsVersion();
        this.hmsVersion = desc.getHmsVersion();
        this.methodName = desc.getMethodName();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKit() {
        return kit;
    }

    public void setKit(String kit) {
        this.kit = kit;
    }

    public String getGmsVersion() {
        return gmsVersion;
    }

    public void setGmsVersion(String gmsVersion) {
        this.gmsVersion = gmsVersion;
    }

    public String getHmsVersion() {
        return hmsVersion;
    }

    public void setHmsVersion(String hmsVersion) {
        this.hmsVersion = hmsVersion;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
