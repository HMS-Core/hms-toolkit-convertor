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

package com.huawei.hms.convertor.core.engine.fixbot.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public final class MappingApiInfo {
    private String oldNameInDesc;

    private String oldName;

    /**
     * <pre>
     * if convert type is Auto/Dummy, this is HMS/XMS api name.
     * if convert type is Manual, this is null.
     * </pre>
     */
    private String newName;

    /**
     * if api not provide url, this is null.
     */
    private String url;

    private String kit;

    private String convertStatus;

    /**
     * <pre>
     * if convert type is Auto, this is true.
     * if convert type is Manual, this is false.
     * if convert type is Unsupport, this is null.
     * </pre>
     */
    private Boolean isAuto;

    private boolean support;

    private String dependencyName;
}
