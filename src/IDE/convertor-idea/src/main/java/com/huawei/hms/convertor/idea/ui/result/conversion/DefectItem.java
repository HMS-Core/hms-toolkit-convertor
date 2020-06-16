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

package com.huawei.hms.convertor.idea.ui.result.conversion;

import com.huawei.codebot.framework.FixStatus;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Defect item bean class
 *
 * @since 2019-06-12
 */
@Setter
@Getter
@NoArgsConstructor
public final class DefectItem {
    private String conversionId;

    private String filePath;

    private boolean applied;

    private String file;

    private int defectStartLine;

    private int defectEndLine;

    private String defectContent;

    private int fixStartLine;

    private int fixEndLine;

    private String fixContent;

    private FixStatus fixStatus;

    private List<ConversionPointDesc> descriptions = new ArrayList<>();

    private String mergedDescription;

    private String convertType;

    private String kitName;

    private boolean isConfirmed;

    private boolean isConverted;

    private boolean isFileTailConvert;

    private boolean newModule;

    private boolean enable;

    public DefectItem(ConversionItem item) {
        this.setConversionId(item.getConversionId());
        this.setFilePath(item.getFilePath());
        this.setApplied(item.isApplied());
        this.setFile(item.getFile());
        this.setDefectStartLine(item.getDefectStartLine());
        this.setDefectEndLine(item.getDefectEndLine());
        this.setDefectContent(item.getDefectContent());
        this.setFixStartLine(item.getFixStartLine());
        this.setFixEndLine(item.getFixEndLine());
        this.setFixContent(item.getFixContent());
        this.setFixStatus(item.getFixStatus());
        this.getDescriptions().addAll(item.getDescriptions());
        this.setMergedDescription(item.getMergedDescription());
        this.setConvertType(item.getConvertType());
        this.setKitName(item.getKitName());
        this.setConfirmed(false);
        this.setConverted(item.isConverted());
        this.setFileTailConvert(item.isFileTailConvert());
        this.setNewModule(item.isNewModule());
        this.enable = true;
    }
}
