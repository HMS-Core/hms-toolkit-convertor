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
import com.huawei.hms.convertor.g2h.map.auto.AutoClass;
import com.huawei.hms.convertor.g2h.map.desc.ClassDesc;
import com.huawei.hms.convertor.g2h.map.desc.Desc;
import com.huawei.hms.convertor.g2h.map.manual.Manual;
import com.huawei.hms.convertor.json.JClass;
import com.huawei.hms.convertor.utils.TextUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * process the classes that support automatic convert
 *
 * @since 2020-07-05
 */
@Slf4j
public class AutoClassHandler extends Handler {
    public AutoClassHandler(Auto auto, Manual manual, JClass jClass, String version) {
        super(auto, manual, jClass, version);
    }

    @Override
    public String handlerRequest() {
        String className = getJClass().gName();
        String kitName = getJClass().getKitName();
        if (getJClass().getStatus() == null) {
            log.info("kit: {},  gms class name: {}.", kitName, className);
        }

        Desc desc = ClassDesc.builder()
            .text("\"" + getJClass().gName() + "\" will be replaced by \"" + getJClass().hName() + "\"")
            .url("")
            .kit(kitName)
            .dependencyName(getJClass().dependencyName())
            .gmsVersion(getVersion())
            .hmsVersion("todo")
            .type("class")
            .status("Auto")
            .support(true)
            .build();

        desc.setName(className);

        if (null != getJClass().hName() && getJClass().hName().length() > 0) {
            AutoClass autoClass = new AutoClass(TextUtil.degenerifyContains(getJClass().gName()),
                TextUtil.degenerifyContains(getJClass().hName()), desc);

            getAuto().getAutoClasses().add(autoClass);
        }

        if (getNextHandler() != null) {
            return getNextHandler().handlerRequest();
        }
        return null;
    }
}
