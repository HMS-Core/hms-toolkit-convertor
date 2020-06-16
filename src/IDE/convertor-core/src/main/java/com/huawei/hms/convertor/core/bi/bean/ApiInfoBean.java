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

import java.util.List;

/**
 * Api info data bean
 *
 * @since 2020-03-30
 */
@Builder
@Getter
public class ApiInfoBean extends BaseBIData {
    /**
     * Total number of APIs used for integration, deduplicated.
     */
    private String totalNum;

    /**
     * Api invoked num.
     */
    private String invokeNum;

    /**
     * Total number of automatically conversion APIs in TO HMS.
     */
    private String autoConversionNumG2H;

    /**
     * Total number of automatically conversion APIs IN Add HMS.
     */
    private String autoConversionNumGH;

    /**
     * Total number of manual conversion APIs in To HMS.
     */
    private String manualConversionNumG2H;

    /**
     * Total number of manual conversion APIs in Add HMS.
     */
    private String manualConversionNumGH;

    /**
     * Auto convert rate in To HMS.
     */
    private String autoConversionRateG2H;

    /**
     * Auto convert rate in Add HMS.
     */
    private String autoConversionRateGH;

    /**
     * Total number of not support conversion APIs
     */
    private String notSupportConversionNum;

    /**
     * List of not support conversion APIs
     */
    private List<ApiBean> notSupportList;
}
