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

package com.huawei.generator.g2x.po.map;

import com.google.gson.annotations.Expose;

/**
 * MDesc: used as a description container for extension see{@link com.huawei.generator.g2x.po.map.extension},
 * which is used for catching all gms or firebase elements beyond our config
 * including:
 * 1. text -> literal description for this field, method or class, build with a default value
 * 2. url -> url of the reference, build with a default value
 * 3. kit -> kit name, which is designed by us, using "Common" as default
 * 4. autoConvert -> can be auto modified by wisehub plugins, true for auto, false for manual
 * 5. support -> in manual, whether hms provides the functionality that app developer can build by themselves
 * Note: when autoConvert is true, support must be true.
 *
 * @since 2019-11-27
 */
public class ExDesc {
    @Expose
    public String text;

    @Expose
    public String url;

    @Expose
    public String kit; // mark Common as default

    @Expose
    public String status;

    @Expose
    public boolean support;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSupport() {
        return support;
    }

    public void setSupport(boolean support) {
        this.support = support;
    }
}
