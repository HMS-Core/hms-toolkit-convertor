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

package com.huawei.hms.convertor.core.engine.fixbot.model;

import com.alibaba.fastjson.JSON;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public final class ManualMethod {
    private String methodName;
    private List<String> paramTypes;
    private ConversionPointDesc desc;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
