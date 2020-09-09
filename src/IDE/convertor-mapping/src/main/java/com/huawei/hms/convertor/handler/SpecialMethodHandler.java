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

package com.huawei.hms.convertor.handler;

import com.huawei.hms.convertor.g2h.map.auto.Auto;
import com.huawei.hms.convertor.g2h.map.desc.Desc;
import com.huawei.hms.convertor.g2h.map.desc.MethodDesc;
import com.huawei.hms.convertor.g2h.map.manual.Manual;
import com.huawei.hms.convertor.g2h.map.manual.ManualMethod;
import com.huawei.hms.convertor.g2h.processor.MethodResult;
import com.huawei.hms.convertor.json.JClass;
import com.huawei.hms.convertor.json.JMapping;
import com.huawei.hms.convertor.json.JMethod;
import com.huawei.hms.convertor.utils.AutoConvertor;
import com.huawei.hms.convertor.utils.TextUtil;

/**
 * process the special methods that not support automatic convert
 *
 * @since 2020-07-05
 */
public class SpecialMethodHandler extends Handler<JMethod> {
    public SpecialMethodHandler(Auto auto, Manual manual, JClass jClass, String version) {
        super(auto, manual, jClass, version);
    }

    @Override
    public String handlerRequest(JMapping<JMethod> mapping) {
        if (AutoConvertor.methodValidator(mapping) == MethodResult.AUTO && mapping.h() == null) {
            if (mapping.status().equals("SpecialStatus")) {
                String gName = getJClass().gName() + "." + mapping.g().name();

                Desc desc = MethodDesc.builder()
                    .text(mapping.text())
                    .url(mapping.url())
                    .kit(getJClass().getKitName())
                    .dependencyName(mapping.dependencyName())
                    .gmsVersion(getVersion())
                    .hmsVersion("todo")
                    .type("method")
                    .status(AutoConvertor.getAutoConvert(mapping.status()))
                    .support(true)
                    .build();
                desc.setName(mapping.g().getSignature(true));
                ManualMethod method = new ManualMethod(TextUtil.degenerifyContains(gName),
                    mapping.g().getParaList(true), mapping.g().getParaList(true), desc);
                getManual().getManualMethods().add(method);
            }
        }

        if (getNextHandler() != null) {
            return getNextHandler().handlerRequest(mapping);
        }
        return null;
    }
}
