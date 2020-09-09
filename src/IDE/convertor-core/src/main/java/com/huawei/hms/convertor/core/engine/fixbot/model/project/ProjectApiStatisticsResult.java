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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectApiStatisticsResult {
    private int apiCount;

    private int blockCount;

    private String supportRate;

    private String unsupportRate;

    private int autoCount4GaddH;

    private int manualCount4GaddH;

    private String autoRate4GaddH;

    private String manualRate4GaddH;

    private int autoCount4G2H;

    private int manualCount4G2H;

    private String autoRate4G2H;

    private String manualRate4G2H;
}
