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
import com.huawei.hms.convertor.g2h.map.auto.AutoField;
import com.huawei.hms.convertor.g2h.map.desc.Desc;
import com.huawei.hms.convertor.g2h.map.desc.FieldDesc;
import com.huawei.hms.convertor.g2h.map.manual.Manual;
import com.huawei.hms.convertor.json.JClass;
import com.huawei.hms.convertor.json.JFieldOrMethod;
import com.huawei.hms.convertor.json.JMapping;
import com.huawei.hms.convertor.utils.AutoConvertor;
import com.huawei.hms.convertor.utils.TextUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * process the fields that support automatic convert
 *
 * @since 2020-07-05
 */
@Slf4j
public class AutoFieldHandler extends Handler<JFieldOrMethod> {
    public AutoFieldHandler(Auto auto, Manual manual, JClass jClass, String version) {
        super(auto, manual, jClass, version);
    }

    @Override
    public String handlerRequest(JMapping<JFieldOrMethod> mapping) {
        String kitName = getJClass().getKitName();
        String text = mapping.text();
        if (mapping.g() == null || !mapping.g().isJField()) {
            log.error("kitName: {}, text: {}.", kitName, text);
            return null;
        }

        if (mapping.status().equals("matching") && mapping.h() != null) {
            String gName = getJClass().gName() + "." + mapping.g().asJField().name();
            String hName = "";
            if (!mapping.h().isJField()) {
                log.error("Convert error, kitName: {}, gName: {}.", kitName, gName);
                hName = mapping.hName();
            } else {
                hName = mapping.hName() + "." + mapping.h().asJField().name();
            }

            String oldClassName = TextUtil.degenerifyContains(gName);
            String newClassName = TextUtil.degenerifyContains(hName);

            Desc desc = FieldDesc.builder()
                .text(mapping.text())
                .url("")
                .kit(kitName)
                .dependencyName(mapping.dependencyName())
                .gmsVersion(getVersion())
                .hmsVersion("todo")
                .type("field")
                .status(AutoConvertor.getAutoConvert(mapping.status()))
                .support(true)
                .build();

            desc.setName(gName);

            AutoField field = new AutoField(oldClassName, newClassName, desc);
            getAuto().getAutoFields().add(field);
        }

        if (getNextHandler() != null) {
            return getNextHandler().handlerRequest(mapping);
        }
        return null;
    }
}
