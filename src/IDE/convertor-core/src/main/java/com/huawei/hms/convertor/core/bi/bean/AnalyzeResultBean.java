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

package com.huawei.hms.convertor.core.bi.bean;

import lombok.Builder;
import lombok.Getter;

/**
 * Analyze Result data bean.
 *
 * @since 2020-03-30
 */
@Builder
@Getter
public class AnalyzeResultBean extends BaseBIData {
    /**
     * Total number of Api conversion.
     */
    private String apiConversionNum;

    /**
     * Total number of Api auto conversion.
     */
    private String apiAutoConversionNum;

    /**
     * Total number of Api manual conversion.
     */
    private String apiManualConversionNum;

    /**
     * Total number of Common conversion.
     */
    private String commonConversionNum;

    /**
     * Total number of Common auto conversion.
     */
    private String commonAutoConversionNum;

    /**
     * Total number of Common manual conversion.
     */
    private String commonManualConversionNum;
}
