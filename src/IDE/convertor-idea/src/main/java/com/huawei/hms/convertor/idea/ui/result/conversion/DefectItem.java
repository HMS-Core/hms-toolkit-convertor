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
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.util.PropertyUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defect item bean class
 *
 * @since 2019-06-12
 */
@Setter
@Getter
@NoArgsConstructor
public final class DefectItem {
    private static final String KIT_URL_PREFIX = "/consumer/en";

    private static final Map<String, String> KIT_URL_MAP = new HashMap<>();

    static {
        String separator = ",";
        String[] kitAndUrlList = PropertyUtil.readProperty("kit_and_url").split(separator);

        for (String kitAndUrl : kitAndUrlList) {
            String kitAndUrlStr = kitAndUrl.trim();
            if (!StringUtil.isEmpty(kitAndUrlStr)) {
                KIT_URL_MAP.put(kitAndUrlStr.split(":")[0], kitAndUrlStr.split(":")[1]);
            }
        }
    }

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

    private String detail;

    private String url;

    public DefectItem(ConversionItem item) {
        setConversionId(item.getConversionId());
        setFilePath(item.getFilePath());
        setApplied(item.isApplied());
        setFile(item.getFile());
        setDefectStartLine(item.getDefectStartLine());
        setDefectEndLine(item.getDefectEndLine());
        setDefectContent(item.getDefectContent());
        setFixStartLine(item.getFixStartLine());
        setFixEndLine(item.getFixEndLine());
        setFixContent(item.getFixContent());
        setFixStatus(item.getFixStatus());
        getDescriptions().addAll(item.getDescriptions());
        setMergedDescription(item.getMergedDescription());
        setConvertType(item.getConvertType());
        setKitName(item.getKitName());
        setConfirmed(false);
        setConverted(item.isConverted());
        setFileTailConvert(item.isFileTailConvert());
        setNewModule(item.isNewModule());
        enable = true;
        setDetail("<html><u><font color=\"#1895fc\">Detail</font></u></html>");
        constructDetailUrl(item.getDescriptions(), item.getKitName());
    }

    /**
     * Set the Detail link to the url in the Description of ConversionPointDesc if not empty, and then escape the
     * following function. Otherwise set the Detail link to the kit introduction website and escape the following
     * function. If no specific introduction site found, the Detail link is not visible.
     *
     * @param descriptions descriptions in item
     * @param kitName kitName in item
     */
    private void constructDetailUrl(List<ConversionPointDesc> descriptions, String kitName) {
        for (ConversionPointDesc description : descriptions) {
            String urlInDesc = description.getUrl();
            if (!StringUtil.isEmpty(urlInDesc)) {
                setUrl(urlInDesc);
                return;
            }
        }

        String name = kitName.substring(1, kitName.length() - 1).split(",")[0];
        if (KIT_URL_MAP.containsKey(name)) {
            setUrl(KIT_URL_PREFIX + KIT_URL_MAP.get(name));
            return;
        }

        setDetail("");
    }
}
