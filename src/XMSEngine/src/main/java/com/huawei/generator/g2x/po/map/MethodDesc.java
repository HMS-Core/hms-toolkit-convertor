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
 * MDesc: used as a description container for methods,
 * <p>
 * including:
 * 1. text means literal description for this field, method or class
 * 2. url means url of the reference
 * 3. kit means kit name, which is designed by us, such as Basement, Push, Location
 * 4. dependencyName means dependency used in build.gradle as well as jar name
 * 5. gmsVersion means version of gms
 * 6. hmsVersion means version of hms
 * 7. autoConvert means can be auto modified by wisehub plugins, true for auto, false for manual
 * 8. support means that in manual, whether hms provides the functionality that app developer can build by themselves
 * 9. methodName means method signature in format of canonical types
 * </p>
 * <p>
 * Note: when autoConvert is true, support must be true.
 * </p>
 *
 * @since 2019-11-27
 */
public class MethodDesc extends Desc {
    @Expose
    public String methodName;

    @Override
    public void setName(String name) {
        methodName = name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
