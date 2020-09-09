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
 * MethodDesc: used as a description container for extension see{@link com.huawei.generator.g2x.po.map.extension},
 * which is used for catching all gms or firebase elements beyond our config
 * <p>
 * including:
 * 1. text means literal description for this field, method or class, build with a default value
 * 2. url means url of the reference, build with a default value
 * 3. kit means kit name, which is designed by us, using "Common" as default
 * 4. autoConvert means can be auto modified by wisehub plugins, true for auto, false for manual
 * 5. support means that in manual, whether hms provides the functionality that app developer can build by themselves
 * </p>
 * <p>
 * Note: when autoConvert is true, support must be true.
 * </p>
 * @since 2019-11-27
 */
public class ExtensionDesc extends Desc {
    @Expose
    public String name;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
