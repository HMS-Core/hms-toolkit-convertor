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

package com.huawei.hms.convertor.core.engine.fixbot.model.project;

import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitSdkVersion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectStatisticsResult {
    private String projectName;

    private int kitCount;

    /**
     * <pre>
     * use largest among all Kit.
     * if all sdkVersion is legal, this is not null.
     * is any sdkVersion is illegal, this is null.
     * </pre>
     */
    private KitSdkVersion kitSdkVersion;

    private ProjectApiStatisticsResult methodStatisticsResult;

    private ProjectApiStatisticsResult classStatisticsResult;

    private ProjectApiStatisticsResult fieldStatisticsResult;

    private String unsupportKits;

    private String analyseDateTime;
}
