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

package com.huawei.codebot.analyzer.x2y.io.config;

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructGradleManual;
import com.huawei.codebot.framework.AbstractJSONConfig;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class GradleManualJSONConfig extends AbstractJSONConfig {
    GradleManualJSONConfig(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
    }

    @Override
    public String getFileName() {
        if (DefectFixerType.LIBADAPTION.toString().equals(getFixerTypeString())) {
            return "wisehub-gradle-manual-hms";
        }
        if (DefectFixerType.WISEHUB.toString().equals(getFixerTypeString())) {
            return "wisehub-gradle-manual";
        }
        return DefectFixerType.LIBADAPTION_GRADLEWARNING.toString();
    }

    List<StructGradleManual> getGradleManual() {
        List<StructGradleManual> listGradleManual = new ArrayList<>();
        JSONObject jsonObject = this.getJSON();
        if (jsonObject.keySet().contains("gradleManual")) {
            JSONArray manualArray = jsonObject.getJSONArray("gradleManual");
            for (int i = 0; i < manualArray.length(); i++) {
                JSONObject jsonObjectTemp = (JSONObject) manualArray.get(i);
                StructGradleManual addTemp = new StructGradleManual();
                if (jsonObjectTemp.keySet().contains("nodeName")) {
                    addTemp.setGradleManualName((String) jsonObjectTemp.get("nodeName"));
                    if (StringUtils.isNotEmpty(addTemp.getGradleManualName())) {
                        addTemp.setDesc(JsonUtil.toMap(jsonObjectTemp.getJSONObject("desc")));
                        listGradleManual.add(addTemp);
                    }
                }
            }
        }
        return listGradleManual;
    }

}
