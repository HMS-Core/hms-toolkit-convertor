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

package com.huawei.hms.convertor.core.result.conversion;

import lombok.Data;

/**
 * Convertion Piont Description
 *
 * @since 2019-03-20
 */
@Data
public final class ConversionPointDesc {
    private String text;
    private String url;
    private String kit;
    private String gmsVersion;
    private String hmsVersion;
    private String methodName;
    private String version;
    private String status;
    private boolean support;
    private boolean isUpdate;
    private String dependencyName;
    private String type;
    private boolean sdk;

    public ConversionPointDesc() {}

}
