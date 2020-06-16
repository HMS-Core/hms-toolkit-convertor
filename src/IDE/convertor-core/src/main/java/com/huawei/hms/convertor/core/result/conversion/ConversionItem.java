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

import com.huawei.codebot.framework.FixStatus;

import lombok.Data;

import java.util.List;

/**
 * Conversion item
 *
 * @since 2020-02-29
 */
@Data
public final class ConversionItem {
    private String conversionId;

    private String filePath;

    /**
     *  whether the conversion result is applied. ：{@code true} - Converted ，{@code false} - Restored
     */
    private boolean applied;

    private String file;

    private int defectStartLine;

    private int defectEndLine;

    private String defectContent;

    private int fixStartLine;

    private int fixEndLine;

    private String fixContent;

    private FixStatus fixStatus;

    private List<ConversionPointDesc> descriptions;

    private String mergedDescription;

    private String convertType;

    private String kitName;

    private boolean isConfirmed = false;

    private boolean isConverted = false;

    private boolean isFileTailConvert = false;

    private boolean newModule = false;
}
