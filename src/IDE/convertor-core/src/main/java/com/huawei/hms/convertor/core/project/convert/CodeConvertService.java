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

package com.huawei.hms.convertor.core.project.convert;

import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.openapi.result.Result;

/**
 * Encapsulating the transcoding Interface
 *
 * @since 2020-02-11
 */
public interface CodeConvertService {
    /**
     * init
     *
     * @param projectPath projectPath
     */
    void init(String projectPath);

    /**
     * convert
     *
     * @param projectPath projectPath
     * @param conversionItem conversion
     * @return Result
     */
    Result convert(String projectPath, ConversionItem conversionItem);

    /**
     * revert
     *
     * @param projectPath projectPath
     * @param conversionItem conversion
     * @return Result
     */
    Result revert(String projectPath, ConversionItem conversionItem);
}
