/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.utils;

import com.huawei.hms.convertor.g2h.processor.MethodResult;
import com.huawei.hms.convertor.json.JMapping;
import com.huawei.hms.convertor.json.JMethod;

/**
 * get auto or manual by status
 *
 * @since 2020-07-05
 */
public class AutoConvertor {

    public static String getAutoConvert(String status) {
        if (status == null) {
            return "manual";
        }
        String autoConvert = "manual";
        switch (status) {
            case "matching":
                autoConvert = "Auto";
                break;
            case "complex":
                autoConvert = "Manual";
                break;
            case "manuallyAdapt":
                autoConvert = "Manual";
                break;
            case "notSupport":
                autoConvert = "Manual";
                break;
            case "dummy":
                autoConvert = "Manual";
                break;
            case "SpecialStatus":
                autoConvert = "Manual";
                break;
        }
        return autoConvert;
    }

    public static MethodResult methodValidator(JMapping<JMethod> mapping) {
        if (mapping.g() == null) {
            return MethodResult.IGNORE;
        }
        if (mapping.status() == null || mapping.status().equals("manuallyAdapt")
            || mapping.status().equals("complex")) {
            return MethodResult.MANUAL;
        }
        if (mapping.status().equals("matching") || mapping.status().equals("isDeprecated")
            || mapping.status().equals("willBeDeprecated") || mapping.status().equals("toBeImplemented")
            || mapping.status().equals("oneToMany") || mapping.status().equals("SpecialStatus")) {
            return MethodResult.AUTO;
        }

        if (mapping.status().equals("redundant")) {
            return MethodResult.IGNORE;
        }
        return MethodResult.MANUAL;
    }
}
