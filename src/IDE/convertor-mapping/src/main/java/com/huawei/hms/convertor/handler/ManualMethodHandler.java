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

import com.huawei.hms.convertor.constants.Constant;
import com.huawei.hms.convertor.g2h.map.auto.Auto;
import com.huawei.hms.convertor.g2h.map.desc.Desc;
import com.huawei.hms.convertor.g2h.map.desc.MethodDesc;
import com.huawei.hms.convertor.g2h.map.manual.BlockList;
import com.huawei.hms.convertor.g2h.map.manual.Manual;
import com.huawei.hms.convertor.g2h.map.manual.ManualMethod;
import com.huawei.hms.convertor.g2h.processor.MethodResult;
import com.huawei.hms.convertor.json.JClass;
import com.huawei.hms.convertor.json.JMapping;
import com.huawei.hms.convertor.json.JMethod;
import com.huawei.hms.convertor.utils.AutoConvertor;
import com.huawei.hms.convertor.utils.FileUtil;
import com.huawei.hms.convertor.utils.TextUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * process the methods that not support automatic convert
 *
 * @since 2020-07-05
 */
public class ManualMethodHandler extends Handler<JMethod> {
    public ManualMethodHandler(Auto auto, Manual manual, JClass jClass, String version) {
        super(auto, manual, jClass, version);
    }

    @Override
    public String handlerRequest(JMapping<JMethod> mapping) {
        if (AutoConvertor.methodValidator(mapping) == MethodResult.MANUAL || mapping.h() == null) {
            if (mapping.g() == null) {
                return null;
            }

            String kitName = getJClass().getKitName();
            String gName = getJClass().gName() + "." + mapping.g().name();
            String text = "HMS does not provide a corresponding method for \"" + getJClass().gName() + "."
                + mapping.g().getFullMethodOrField() + "\", please delete it.";
            String kitUrl = "";
            BlockList blockList = new BlockList();

            mapping.g().setgClassName(getJClass().gName());
            ManualMethod method;
            List<String> weakParamTypes = new LinkedList<>();
            for (String gMethodName : blockList.manualConvertStar) {
                if (gMethodName.equals(gName)) {
                    List<String> paramList = mapping.g().getWeakParaList(true);
                    for (String s : paramList) {
                        if (s.contains("...")) {
                            weakParamTypes.add("*...");
                            continue;
                        }
                        weakParamTypes.add("*");
                    }
                    continue;
                }

                weakParamTypes = mapping.g().getWeakParaList(true);
            }

            if (!FileUtil.getProperty(Constant.DEVELOPER_EN_URL).equals(mapping.url())) {
                kitUrl = mapping.url();
            }

            if (mapping.status() == null || mapping.status().equals("notSupport")) {
                if (!"Documents to be completed".equals(mapping.text())) {
                    text = mapping.text();
                }

                Desc desc = MethodDesc.builder()
                    .text(text)
                    .url(kitUrl)
                    .kit(kitName)
                    .dependencyName(mapping.dependencyName())
                    .gmsVersion(getVersion())
                    .hmsVersion("todo")
                    .type("method")
                    .status(AutoConvertor.getAutoConvert(mapping.status()))
                    .support(false)
                    .build();
                desc.setName(mapping.g().getSignature(true));
                method = new ManualMethod(TextUtil.degenerifyContains(gName), mapping.g().getParaList(true),
                    weakParamTypes, desc);
            } else {
                Desc desc = MethodDesc.builder()
                    .text(mapping.text())
                    .url(kitUrl)
                    .kit(kitName)
                    .dependencyName(mapping.dependencyName())
                    .gmsVersion(getVersion())
                    .hmsVersion("todo")
                    .type("method")
                    .status(AutoConvertor.getAutoConvert(mapping.status()))
                    .support(true)
                    .build();
                desc.setName(mapping.g().getSignature(true));
                method = new ManualMethod(TextUtil.degenerifyContains(gName), mapping.g().getParaList(true),
                    weakParamTypes, desc);
            }
            getManual().getManualMethods().add(method);
        }
        if (getNextHandler() != null) {
            return getNextHandler().handlerRequest(mapping);
        }
        return null;
    }
}
