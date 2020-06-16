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
 * Operation of processing the analysis result.
 *
 * @since 2020-03-30
 */
@Builder
@Getter
public class ConversionOperationBean extends BaseBIData {
    /**
     * Total number of analysis results, including auto and manual conversion.
     */
    private String totalNum;

    /**
     * Total number of records that have been processed after the current processing.
     */
    private String processedNum;

    /**
     * Number of entries processed in the current conversion
     */
    private String currentProcessNum;

    /**
     * Whether to convert data in batches
     */
    private boolean isBatchProcess;

    /**
     * Time cost form the action "New Conversion" to current Operation.
     */
    private String timeCost;

    /**
     * Analysis result processing completion rate
     */
    private String processRate;
}
