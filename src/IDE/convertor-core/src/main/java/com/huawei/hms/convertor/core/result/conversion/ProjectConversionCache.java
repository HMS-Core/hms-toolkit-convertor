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

import com.huawei.hms.convertor.core.event.context.EventType;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Conversion cache manager
 *
 * @since 2020-02-27
 */
@Slf4j
public final class ProjectConversionCache {
    public boolean isEdit;

    private List<ConversionItem> conversionItemList;

    private Map<String, ConversionItem> conversionCache;

    private Map<String, List<String>> fileConversions;

    private List<ChangedCode> changedCodeList;

    /**
     * Used to temporarily save changed data, and clear data after callback
     */
    private Map<String, ConversionItem> correctedItemMap;

    public List<ConversionItem> getConversionItemList() {
        return conversionItemList;
    }

    public void setConversionItemList(List<ConversionItem> conversionItemList) {
        this.conversionItemList = conversionItemList;
    }

    public Map<String, ConversionItem> getConversionCache() {
        return conversionCache;
    }

    public void setConversionCache(Map<String, ConversionItem> conversionCache) {
        this.conversionCache = conversionCache;
    }

    public ProjectConversionCache() {
        changedCodeList = new ArrayList<>();
        conversionItemList = new ArrayList<>();
        conversionCache = new HashMap<>();
        fileConversions = new HashMap<>();
        correctedItemMap = new ConcurrentHashMap<>();
    }

    public void setFileConversions(List<ConversionItem> conversions) {
        conversions.forEach(conversionItem -> {
            if (fileConversions.containsKey(conversionItem.getFile())) {
                fileConversions.get(conversionItem.getFile()).add(conversionItem.getConversionId());
            } else {
                List<String> conversionIds = new ArrayList<>();
                conversionIds.add(conversionItem.getConversionId());
                fileConversions.put(conversionItem.getFile(), conversionIds);
            }
        });
    }

    /**
     * Get conversion item by unique id
     *
     * @param conversionId Conversion id
     * @return Conversion item
     */
    public ConversionItem getConversionItem(String conversionId) {
        return conversionCache.get(conversionId);
    }

    /**
     * Correct conversion cache by id, and will put changed items into {@code changedItemMap}
     *
     * @param conversionId Conversion id
     */
    public void correctCache(String conversionId, boolean convert) {
        ConversionItem appliedItem = getConversionItem(conversionId);
        if (Objects.isNull(appliedItem)) {
            log.error("None conversion item found, conversion id: {}", conversionId);
            return;
        }
        // return when is converted == operation type
        if (appliedItem.isConverted() == convert) {
            return;
        }
        appliedItem.setConverted(convert);
        correctedItemMap.put(conversionId, appliedItem);
        // return when is convertType == manual
        if (appliedItem.getConvertType().equals(ConvertType.MANUAL)) {
            return;
        }
        // Check convert/revert by appliedItem.isApplied()
        int fixChangedEndLineNumber = appliedItem.getFixEndLine() - appliedItem.getFixStartLine();
        int defectChangedEndLineNumber = appliedItem.getDefectEndLine() - appliedItem.getDefectStartLine();

        if (lineNoChange(appliedItem, fixChangedEndLineNumber, defectChangedEndLineNumber)) {
            // modify
            return;
        }
        int changedLineCount = getChangedLineCount(convert, appliedItem);

        ChangedCode changedCode = new ChangedCode(appliedItem.getFile(), appliedItem.getDefectStartLine(),
            appliedItem.getFixContent(), appliedItem.getDefectContent(), changedLineCount);
        changedCodeList.add(changedCode);
        EventType eventType = convert ? EventType.CONVERT_EVENT : EventType.REVERT_EVENT;
        correctCache(changedCode, eventType);
        log.info("Correct cache as applied conversion, file: {}, conversion id: {}", appliedItem.getFilePath(),
            conversionId);
    }

    /**
     * Correct conversion cache by changed code, and will put changed items into {@code changedItemMap}
     *
     * @param changedCode Changed code
     */
    public void correctCache(ChangedCode changedCode, EventType eventType) {
        log.info("Correct cache code changed, file: {}", changedCode.getRelativeFilePath());
        List<ConversionItem> fileItems = getConversionItemsByFile(changedCode.getRelativeFilePath());
        // revert repeat submission

        if (eventType.equals(EventType.EDIT_EVENT)) {
            log.info("Enter correctCache. {}, {}", changedCode.getChangedLineCount(),
                changedCode.getChangedEndLineNumber());
            for (ConversionItem item : fileItems) {
                if (item.getConvertType().equals(ConvertType.MANUAL)) {
                    continue;
                }

                boolean contextCompare = isContextCompare(changedCode, item);
                if (!contextCompare) {
                    continue;
                }

                int editChangeEndLine = changedCode.getChangedEndLineNumber();
                int changedLineCount = changedCode.getChangedLineCount();
                if ((reduceLineItem(item) && changedCode.getChangedLineCount() > 0)
                    || (!reduceLineItem(item) && changedCode.getChangedLineCount() < 0)) {
                    editChangeEndLine = changedCode.getChangedEndLineNumber() + changedLineCount;
                }

                int changedLineNum = getChangedLineNum(item);
                if (changedLineNum == Math.abs(changedCode.getChangedLineCount())
                    && editChangeEndLine == Math.abs(item.getDefectEndLine())
                    && item.getFile().equals(changedCode.getRelativeFilePath())) {
                    log.info("out not edit");
                    isEdit = false;
                    return;
                }
            }
            isEdit = true;
            log.info("out is edit");
        }

        adjustDefectItems(changedCode, fileItems);
        log.info("changedCode correctCache, correctedItemMap size is {}", correctedItemMap.size());
    }

    /**
     * Get corrected conversion items
     *
     * @return Corrected conversion items
     */
    public List<ConversionItem> getCorrectedItems() {
        log.info("getCorrectedItems, item size is {}", correctedItemMap.size());
        List<ConversionItem> correctItems = new ArrayList<>(correctedItemMap.values());
        correctedItemMap.clear();
        return correctItems;
    }

    /**
     * clear cache
     */
    public void clearProjectConversion() {
        conversionItemList.clear();
        conversionCache.clear();
        fileConversions.clear();
        correctedItemMap.clear();
    }

    /**
     * Get conversion items by relative file path
     *
     * @param filePath Relative file path
     * @return Conversion item
     */
    private List<ConversionItem> getConversionItemsByFile(String filePath) {
        List<String> conversionIds = fileConversions.get(filePath);
        if (Objects.isNull(conversionIds)) {
            return new ArrayList<>();
        }
        return conversionIds.stream()
            .map(conversionId -> conversionCache.get(conversionId))
            .collect(Collectors.toList());
    }

    private boolean lineNoChange(ConversionItem appliedItem, int fixChangedEndLineNumber,
        int defectChangedEndLineNumber) {
        return appliedItem.getFixEndLine() > 0 && appliedItem.getDefectEndLine() > 0
            && fixChangedEndLineNumber == defectChangedEndLineNumber;
    }

    private int getChangedLineCount(boolean convert, ConversionItem appliedItem) {
        int changedLineCount = appliedItem.getFixEndLine() - appliedItem.getFixStartLine()
            - (appliedItem.getDefectEndLine() - appliedItem.getDefectStartLine());
        if (appliedItem.getFixEndLine() > 0 && appliedItem.getDefectEndLine() > 0
            && appliedItem.getDefectStartLine() > 0 && appliedItem.getFixStartLine() > 0) {
            // 1 -> n
            changedLineCount = convert ? changedLineCount : -changedLineCount;
        } else {
            // insert or delete
            if (convert) {
                changedLineCount = changedLineCount + 1;
            } else {
                changedLineCount = -(changedLineCount + 1);
            }
        }
        return changedLineCount;
    }

    private void adjustDefectItems(ChangedCode changedCode, List<ConversionItem> fileItems) {
        // Correct line number of the conversions after the changed code
        for (ConversionItem item : fileItems) {
            if (item.isFileTailConvert()) {
                item.setFileTailConvert(false);
            }

            int itemLineNumber = Math.abs(item.getDefectStartLine());
            int currentLineNumber = Math.abs(changedCode.getChangedEndLineNumber());
            if (itemLineNumber < currentLineNumber) {
                continue;
            }

            if (itemLineNumber == currentLineNumber) {
                if (item.getDefectStartLine() == changedCode.getChangedEndLineNumber()) {
                    continue;
                }

                boolean isCurrentItemReplaceType = changedCode.getChangedEndLineNumber() > 0;
                boolean isItemInsertType = item.getDefectStartLine() < 0;
                if (isCurrentItemReplaceType && isItemInsertType) {
                    continue;
                }
            }
            adjustDefectLine(item, changedCode.getChangedLineCount());
            correctedItemMap.put(item.getConversionId(), item);
        }
    }

    private int getChangedLineNum(ConversionItem item) {
        int changedLineNum = Math.abs(item.getFixEndLine() - item.getFixStartLine()) + 1;
        if (item.getFixEndLine() > 0 && item.getDefectEndLine() > 0 && item.getDefectStartLine() > 0
            && item.getFixStartLine() > 0) {
            changedLineNum =
                item.getFixEndLine() - item.getFixStartLine() - (item.getDefectEndLine() - item.getDefectStartLine());
            changedLineNum = Math.abs(changedLineNum);
        }
        return changedLineNum;
    }

    private boolean isContextCompare(ChangedCode changedCode, ConversionItem item) {
        return changedCode.getNewCodeSnippet().contains(item.getFixContent())
            || changedCode.getOldCodeSnippet().contains(item.getFixContent())
            || item.getFixContent().contains(changedCode.getNewCodeSnippet())
            || item.getFixContent().contains(changedCode.getOldCodeSnippet());
    }

    private void adjustDefectLine(ConversionItem item, int changeLineCount) {
        int itemAdjustedStartLine = Math.abs(item.getDefectStartLine()) + changeLineCount;
        int itemAdjustedEndLine = Math.abs(item.getDefectEndLine()) + changeLineCount;
        if (item.getDefectStartLine() < 0) {
            item.setDefectStartLine(-itemAdjustedStartLine);
            item.setDefectEndLine(-itemAdjustedEndLine);
        } else {
            item.setDefectStartLine(itemAdjustedStartLine);
            item.setDefectEndLine(itemAdjustedEndLine);
        }
    }

    private boolean reduceLineItem(ConversionItem item) {
        if (item.getFixEndLine() > 0 && item.getDefectEndLine() > 0 && item.getDefectStartLine() > 0
            && item.getFixStartLine() > 0) {
            int defectLineCount = item.getDefectEndLine() - item.getDefectStartLine();
            int fixLineCount = item.getFixEndLine() - item.getFixStartLine();
            if (defectLineCount > fixLineCount) {
                return true;
            }
        }
        return false;
    }
}
