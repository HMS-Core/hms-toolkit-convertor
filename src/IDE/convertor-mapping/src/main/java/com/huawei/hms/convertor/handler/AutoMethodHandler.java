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
import com.huawei.hms.convertor.g2h.map.auto.AutoMethod;
import com.huawei.hms.convertor.g2h.map.auto.AutoMethodWithParam;
import com.huawei.hms.convertor.g2h.map.auto.NewParamValue;
import com.huawei.hms.convertor.g2h.map.auto.OldParamIndex;
import com.huawei.hms.convertor.g2h.map.desc.Desc;
import com.huawei.hms.convertor.g2h.map.desc.MethodDesc;
import com.huawei.hms.convertor.g2h.map.manual.Manual;
import com.huawei.hms.convertor.g2h.processor.MethodResult;
import com.huawei.hms.convertor.json.JClass;
import com.huawei.hms.convertor.json.JMapping;
import com.huawei.hms.convertor.json.JMethod;
import com.huawei.hms.convertor.utils.AutoConvertor;
import com.huawei.hms.convertor.utils.TextUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * process the methods that support automatic convert
 *
 * @since 2020-07-05
 */
public class AutoMethodHandler extends Handler<JMethod> {
    public AutoMethodHandler(Auto auto, Manual manual, JClass jClass, String version) {
        super(auto, manual, jClass, version);
    }

    @Override
    public String handlerRequest(JMapping<JMethod> mapping) {
        if (AutoConvertor.methodValidator(mapping) == MethodResult.AUTO && null != mapping.h()) {
            String gName = getJClass().gName() + "." + mapping.g().name();
            String hName = mapping.hName() + "." + mapping.h().name();

            mapping.g().setgClassName(getJClass().gName());
            mapping.h().setgClassName(mapping.hName());
            String hMethod = mapping.h().getFullMethodOrField();
            String gMethod = mapping.g().getFullMethodOrField();

            String text = "\"" + gMethod + "\" will be replaced by \"" + hMethod + "\"";

            if (mapping.status().equals("SpecialStatus")) {
                text = text + "\n " + mapping.text();
            }

            List<Object> newParamsList = new LinkedList<>();
            if (mapping.oldIndex() != null || mapping.newParams() != null) {
                if (mapping.newParams() != null) {
                    String[] newParams = mapping.newParams();
                    for (String newParam : newParams) {
                        if (!newParam.equals("noNewParamValues")) {
                            newParamsList.add(new NewParamValue(newParam));
                        }
                    }
                }
                if (mapping.oldIndex() != null) {
                    String[] oldIndex = mapping.oldIndex();
                    for (String index : oldIndex) {
                        newParamsList.add(new OldParamIndex(index));
                    }
                }

                Desc desc = MethodDesc.builder()
                    .text(text)
                    .url("")
                    .kit(getJClass().getKitName())
                    .dependencyName(mapping.dependencyName())
                    .gmsVersion(getVersion())
                    .hmsVersion("todo")
                    .type("method")
                    .status(AutoConvertor.getAutoConvert(mapping.status()))
                    .support(true)
                    .build();
                desc.setName(mapping.g().getSignature(true));

                AutoMethodWithParam method = (AutoMethodWithParam) AutoMethodWithParam.builder()
                    .oldMethodName(TextUtil.degenerifyContains(gName))
                    .newMethodName(TextUtil.degenerifyContains(hName))
                    .desc(desc)
                    .paramTypes(mapping.g().getParaList(true))
                    .weakTypes(mapping.g().getWeakParaList(true))
                    .newParams(newParamsList)
                    .build();
                getAuto().getAutoMethods().add(method);

            } else {
                Desc desc = MethodDesc.builder()
                    .text(text)
                    .url("")
                    .kit(getJClass().getKitName())
                    .dependencyName(mapping.dependencyName())
                    .gmsVersion(getVersion())
                    .hmsVersion("todo")
                    .type("method")
                    .status(AutoConvertor.getAutoConvert(mapping.status()))
                    .support(true)
                    .build();
                desc.setName(mapping.g().getSignature(true));

                AutoMethod method = AutoMethod.builder()
                    .oldMethodName(TextUtil.degenerifyContains(gName))
                    .newMethodName(TextUtil.degenerifyContains(hName))
                    .desc(desc)
                    .paramTypes(mapping.g().getParaList(true))
                    .weakTypes(mapping.g().getWeakParaList(true))
                    .build();

                getAuto().getAutoMethods().add(method);
            }
        }

        if (getNextHandler() != null) {
            return getNextHandler().handlerRequest(mapping);
        }
        return null;
    }
}
