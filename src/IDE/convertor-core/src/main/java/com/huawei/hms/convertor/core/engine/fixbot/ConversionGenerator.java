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

package com.huawei.hms.convertor.core.engine.fixbot;

import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.dispatch.model.DefectFile;
import com.huawei.codebot.framework.model.DefectBlock;
import com.huawei.hms.convertor.core.bi.enumration.DescriptionTypeEnum;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.ApiKey;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotConstants;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.util.KitUtil;

import com.alibaba.fastjson.JSONException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Conversion list generator
 *
 * @since 2020-04-07
 */
@Setter
@Getter
@Slf4j
public final class ConversionGenerator {
    private static final String PERMISSION_FILE_PATH = "xmsadapter/README.md";

    private static final String ADAPTER_BUILD_PATH = "xmsadapter/build.gradle";

    private static final String PERMISSION_CONTENT = "add uses-permissions";

    private static final String PERMISSION_HELP = "Please add uses-permissions into \"AndroidManifest.xml\" of "
        + "your application module. Check section 3 of Readme file in xmsadapter module for more details.";

    private static final String ORIGINAL_DIR = "original";

    private static final int ORIGINAL_DIR_LEN = ORIGINAL_DIR.length();

    private static final String DESCRIPTION_SUFFIX = "; ";

    private static final String DELIMITER_TEXT = "Please add relevant package:";

    private static final int CLASS_DESCRIPTIONS_MIN_SIZE = 2;

    private FixbotResultParser fixbotResultParser;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<ApiKey, Integer> methodKey2FileCountMap;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<ApiKey, Integer> methodKey2BlockCountMap;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<ApiKey, Integer> classKey2FileCountMap;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<ApiKey, Integer> classKey2BlockCountMap;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<ApiKey, Integer> fieldKey2FileCountMap;

    /**
     * Kit not included: Common, Other, Unsupport Kit
     */
    private Map<ApiKey, Integer> fieldKey2BlockCountMap;

    public ConversionGenerator(FixbotResultParser fixbotResultParser) {
        this.fixbotResultParser = fixbotResultParser;
        methodKey2FileCountMap = new HashMap<>();
        methodKey2BlockCountMap = new HashMap<>();
        classKey2FileCountMap = new HashMap<>();
        classKey2BlockCountMap = new HashMap<>();
        fieldKey2FileCountMap = new HashMap<>();
        fieldKey2BlockCountMap = new HashMap<>();
    }

    public List<ConversionItem> extractConverisons() throws JSONException {
        List<ConversionItem> autoConversionList = new ArrayList<>();
        List<ConversionItem> manualConversionList = new ArrayList<>();
        Map<ApiKey, Set<String>> methodKey2FileIdSetMap = new HashMap<>();
        Map<ApiKey, Set<String>> methodKey2BlockIdSetMap = new HashMap<>();
        Map<ApiKey, Set<String>> classKey2FileIdSetMap = new HashMap<>();
        Map<ApiKey, Set<String>> classKey2BlockIdSetMap = new HashMap<>();
        Map<ApiKey, Set<String>> fieldKey2FileIdSetMap = new HashMap<>();
        Map<ApiKey, Set<String>> fieldKey2BlockIdSetMap = new HashMap<>();
        final int resultFilePathLen = fixbotResultParser.getResultPath().length();

        List<DefectFile> defectFiles = fixbotResultParser.getDefectFiles();
        if (CollectionUtils.isEmpty(defectFiles)) {
            log.warn("The defect files are empty.");
            return Collections.emptyList();
        }

        for (DefectFile defectFile : defectFiles) {
            final String originalFilePath = FileUtil.unifyToUnixFileSeparator(defectFile.getFilePath());
            final String fileRelativePath = originalFilePath.substring(resultFilePathLen + 1 + ORIGINAL_DIR_LEN + 1);
            final String fixFilePath = fixbotResultParser.getResultPath() + Constant.UNIX_FILE_SEPARATOR
                + FixbotConstants.FIXBOT_DIR + Constant.UNIX_FILE_SEPARATOR + fileRelativePath;
            String fileId = defectFile.getId();
            // only need to traverse autoFixedBlocks, because manualFixedBlocks is deprecated
            for (DefectBlock defectBlock : defectFile.getAutoFixedBlocks()) {
                ConversionItem conversionItem = new ConversionItem();
                String blockId = defectBlock.getId();
                List<ConversionPointDesc> descriptions = fixbotResultParser.getBlockId2DescriptionsMap().get(blockId);

                // Extract 'Kit Name' for the conversion item.
                HashSet<String> kitSet = new HashSet<>();
                descriptions.forEach(description -> {
                    String kit = description.getKit();
                    if (kit != null && KitUtil.supportKitToH(description.getKit())) {
                        kitSet.add(kit);
                        buildApiKey2FileIdAndBlockIdMap(kit, description.getMethodName(), fileId, blockId,
                            methodKey2FileIdSetMap, methodKey2BlockIdSetMap);
                        buildApiKey2FileIdAndBlockIdMap(kit, description.getClassName(), fileId, blockId,
                            classKey2FileIdSetMap, classKey2BlockIdSetMap);
                        buildApiKey2FileIdAndBlockIdMap(kit, description.getFieldName(), fileId, blockId,
                            fieldKey2FileIdSetMap, fieldKey2BlockIdSetMap);
                    }
                });
                if (kitSet.isEmpty()) {
                    continue;
                }
                if (kitSet.contains(KitsConstants.OTHER) && kitSet.size() != 1) {
                    kitSet.remove(KitsConstants.OTHER);
                }
                conversionItem.setKitName(kitSet.toString());

                // Extract file path for the conversion item.
                conversionItem.setFile(fileRelativePath);
                conversionItem.setFilePath(fileRelativePath);

                // Generate UUID for the conversion item.
                conversionItem.setConversionId(UUID.randomUUID().toString());

                // Extract the core information about the conversion for the conversion item.
                constructConvertedContent(originalFilePath, fixFilePath, defectBlock, conversionItem);

                // Extract the description for the conversion item.
                constructDescription(autoConversionList, manualConversionList, conversionItem, descriptions);
            }
        }

        countApiKey2FileAndBlockMap(methodKey2FileIdSetMap, methodKey2BlockIdSetMap, methodKey2FileCountMap,
            methodKey2BlockCountMap);
        countApiKey2FileAndBlockMap(classKey2FileIdSetMap, classKey2BlockIdSetMap, classKey2FileCountMap,
            classKey2BlockCountMap);
        countApiKey2FileAndBlockMap(fieldKey2FileIdSetMap, fieldKey2BlockIdSetMap, fieldKey2FileCountMap,
            fieldKey2BlockCountMap);
        log.info(
            "methodKey2FileCountMap size: {}, methodKey2BlockCountMap size: {}, classKey2FileCountMap size: {}, classKey2BlockCountMap size: {}, fieldKey2FileCountMap size: {}, fieldKey2BlockCountMap size: {}.",
            methodKey2FileCountMap.size(), methodKey2BlockCountMap.size(), classKey2FileCountMap.size(),
            classKey2BlockCountMap.size(), fieldKey2FileCountMap.size(), fieldKey2BlockCountMap.size());

        // When the policy is Add HMS API, the conversion list needs to be customized.
        if (fixbotResultParser.getRoutePolicy() == RoutePolicy.G_AND_H) {
            // Add the manual conversion item that describes how to add uses-permissions.
            ConversionItem permissionItem = createPermissionItem();
            manualConversionList.add(permissionItem);

            // Filter all conversions about "xmsadapter/build.gradle" file.
            autoConversionList.removeIf(item -> item.getFile().contains(ADAPTER_BUILD_PATH));
            manualConversionList.removeIf(item -> item.getFile().contains(ADAPTER_BUILD_PATH));
        }

        // Sort and merge conversion list.
        List<ConversionItem> conversionItems = sortAndMergeList(autoConversionList, manualConversionList);
        return conversionItems;
    }

    private void constructConvertedContent(String originalFilePath, String fixFilePath, DefectBlock fixedBlock,
        ConversionItem conversionItem) {
        // Set the start and end line number.
        conversionItem.setDefectStartLine(fixedBlock.getDefectBlockStartLine());
        conversionItem.setDefectEndLine(fixedBlock.getDefectBlockEndLine());
        conversionItem.setFixStartLine(fixedBlock.getAutoFixedBlockStartLine());
        conversionItem.setFixEndLine(fixedBlock.getAutoFixedBlockEndLine());
        // Set the fix status.
        conversionItem.setFixStatus(fixedBlock.getFixStatus());

        /*
         * Setting the content before conversion.
         * If the defect start line number is less than 0,
         * the behavior is insertion and the defect content is empty.
         */
        if (conversionItem.getDefectStartLine() < 0) {
            conversionItem.setDefectContent("");
        } else {
            conversionItem.setDefectContent(
                readDocument(originalFilePath, conversionItem.getDefectStartLine(), conversionItem.getDefectEndLine()));
        }

        /*
         * Setting the content after conversion.
         * If the fix start line number is less than 0,
         * the behavior is insertion and the fix content is empty.
         */
        if (conversionItem.getFixStartLine() < 0) {
            conversionItem.setFixContent("");
        } else {
            conversionItem.setFixContent(
                readDocument(fixFilePath, conversionItem.getFixStartLine(), conversionItem.getFixEndLine()));
        }
    }

    private String readDocument(String filePath, final int readStartLine, final int readEndLine) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(Paths.get(filePath).toString()), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines()
                .skip(readStartLine - 1)
                .limit(readEndLine - readStartLine + 1)
                .collect(Collectors.toList());

            if (lines.isEmpty()) {
                log.error("Get file content error");
                return "";
            }

            return StringUtils.join(lines, Constant.LINE_SEPARATOR);
        } catch (IOException e) {
            log.error("Get file content error: {}.", e.getMessage(), e);
            return "";
        }
    }

    private void constructDescription(List<ConversionItem> autoConvertList, List<ConversionItem> manualConvertList,
        ConversionItem conversionItem, List<ConversionPointDesc> descriptions) {
        List<ConversionPointDesc> descriptionList = new ArrayList<>(descriptions);
        // Filter the class description.
        filterClassDescriptions(descriptionList);
        List<ConversionPointDesc> descriptionListCopy = new ArrayList<>(descriptionList);

        // If none of the states is manual, the value of 'isAutoConvert' is true.
        boolean isAutoConvert = true;
        for (ConversionPointDesc description : descriptionListCopy) {
            isAutoConvert = isAutoConvert && isAuto(description.getStatus());
        }

        // Combine all descriptions into one.
        StringBuilder newDescription = new StringBuilder("<html>");
        for (ConversionPointDesc description : descriptionListCopy) {
            String text = description.getText();
            String url = description.getUrl();
            if (StringUtils.isEmpty(text)) {
                continue;
            }

            // Skip the description that does not need to be displayed.
            if (!isAutoConvert && !conversionItem.getFixStatus().equals(FixStatus.AUTOFIX)
                && isAuto(description.getStatus())) {
                descriptionList.remove(description);
                continue;
            }

            if (!StringUtils.isEmpty(url)) {
                newDescription.append("<u>").append(text).append("</u>");
            } else {
                newDescription.append(text);
            }
            addDelimiter(text, newDescription);
        }
        if (newDescription.subSequence(newDescription.length() - DESCRIPTION_SUFFIX.length(), newDescription.length()).equals(DESCRIPTION_SUFFIX)) {
            newDescription.delete(newDescription.length() - DESCRIPTION_SUFFIX.length(), newDescription.length());
        }
        newDescription.append("</html>");

        // Set the conversion properties.
        conversionItem.setDescriptions(descriptionList);
        conversionItem.setMergedDescription(newDescription.toString());
        conversionItem.setConfirmed(false);
        if (isAutoConvert) {
            if (isDummyType(descriptionList)) {
                conversionItem.setConvertType(ConvertType.DUMMY);
                autoConvertList.add(conversionItem);
            } else {
                conversionItem.setConvertType(ConvertType.AUTO);
                autoConvertList.add(conversionItem);
            }
        } else {
            conversionItem.setConvertType(ConvertType.MANUAL);
            manualConvertList.add(conversionItem);
        }
    }

    private boolean isAuto(String convertStatus) {
        return !ConvertType.MANUAL.equals(convertStatus);
    }

    private boolean isDummyType(List<ConversionPointDesc> descriptions) {
        boolean result = false;
        for (ConversionPointDesc description : descriptions) {
            if (ConvertType.DUMMY.equals(description.getStatus())) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void addDelimiter(String text, StringBuilder stringBuilder) {
        if (text.equals(DELIMITER_TEXT)) {
            stringBuilder.append(" ");
        } else {
            stringBuilder.append("; ");
        }
    }

    /**
     * Filter the class description that is not supported
     * when the current description list contains method or field.
     *
     * @param descriptions description list
     */
    private void filterClassDescriptions(List<ConversionPointDesc> descriptions) {
        if (descriptions.isEmpty() || descriptions.size() < CLASS_DESCRIPTIONS_MIN_SIZE) {
            return;
        }

        boolean flag = false;
        for (ConversionPointDesc description : descriptions) {
            if (description.getType() == null) {
                continue;
            }
            if (description.getType().equals(DescriptionTypeEnum.CLASS.getValue()) && !description.isSupport()) {
                flag = true;
                break;
            }
        }

        boolean filterEnabled = false;
        if (flag) {
            for (ConversionPointDesc description : descriptions) {
                if (description.getType() == null) {
                    continue;
                }
                if ((description.getType().equals(DescriptionTypeEnum.METHOD.getValue()) || description.getType().equals(DescriptionTypeEnum.FIELD.getValue()))
                    && description.isSupport()) {
                    filterEnabled = true;
                    break;
                }
            }
        }

        if (filterEnabled) {
            descriptions
                .removeIf(item -> item.getType() != null && item.getType().equals(DescriptionTypeEnum.CLASS.getValue()) && !item.isSupport());
        }
    }

    private List<ConversionItem> sortAndMergeList(List<ConversionItem> autoConvertList,
        List<ConversionItem> manualConvertList) {
        int autoConvertCount;
        int manualConvertCount;
        List<ConversionItem> normalAutoList = getAutoOrDummyDefectItemList(autoConvertList, ConvertType.AUTO);
        List<ConversionItem> dummyAutoList = getAutoOrDummyDefectItemList(autoConvertList, ConvertType.DUMMY);

        // Sort normal auto conversion list.
        Map<String,
            List<ConversionItem>> file2NormalAutoListMap = normalAutoList.stream()
                .filter(defectItem -> defectItem.getFixStatus().equals(FixStatus.AUTOFIX))
                .collect(Collectors.groupingBy(ConversionItem::getFile, toSortedList()));
        TreeMap<String, List<ConversionItem>> file2AutoNormalListTreeMap = new TreeMap<>(file2NormalAutoListMap);
        List<ConversionItem> conversionItems = new ArrayList<>();
        for (Iterator ite = file2AutoNormalListTreeMap.keySet().iterator(); ite.hasNext();) {
            String file = ite.next().toString();
            conversionItems.addAll(file2AutoNormalListTreeMap.get(file));
        }

        // Sort dummy auto conversion list.
        Map<String,
            List<ConversionItem>> file2DummyAutoListMap = dummyAutoList.stream()
                .filter(defectItem -> defectItem.getFixStatus().equals(FixStatus.AUTOFIX))
                .collect(Collectors.groupingBy(ConversionItem::getFile, toSortedList()));
        TreeMap<String, List<ConversionItem>> file2AutoDummyListTreeMap = new TreeMap<>(file2DummyAutoListMap);
        for (Iterator ite = file2AutoDummyListTreeMap.keySet().iterator(); ite.hasNext();) {
            String file = ite.next().toString();
            conversionItems.addAll(file2AutoDummyListTreeMap.get(file));
        }
        autoConvertCount = conversionItems.size();

        // Sort manual conversion list.
        Map<String, List<ConversionItem>> file2ManualListMap =
            manualConvertList.stream().collect(Collectors.groupingBy(ConversionItem::getFile, toSortedList()));
        TreeMap<String, List<ConversionItem>> file2ManualListTreeMap = new TreeMap<>(file2ManualListMap);
        for (Iterator ite = file2ManualListTreeMap.keySet().iterator(); ite.hasNext();) {
            String file = ite.next().toString();
            conversionItems.addAll(file2ManualListTreeMap.get(file));
        }
        final int totalCount = conversionItems.size();
        manualConvertCount = totalCount - autoConvertCount;

        log.info("autoConvertCount: {}, manualConvertCount: {}, totalCount: {}", autoConvertCount, manualConvertCount,
            totalCount);
        return conversionItems;
    }

    private List<ConversionItem> getAutoOrDummyDefectItemList(List<ConversionItem> list, String category) {
        List<ConversionItem> ans = new ArrayList<>();
        switch (category) {
            case ConvertType.AUTO:
                for (ConversionItem item : list) {
                    if (ConvertType.AUTO.equals(item.getConvertType())) {
                        ans.add(item);
                    }
                }
                break;
            case ConvertType.DUMMY:
                for (ConversionItem item : list) {
                    if (ConvertType.DUMMY.equals(item.getConvertType())) {
                        ans.add(item);
                    }
                }
                break;
            default:
                log.error("Get list error!");
        }
        return ans;
    }

    // Convert a list into a file - item map, and sort each item by 'defect start line' in each file.
    private Collector<ConversionItem, ?, List<ConversionItem>> toSortedList() {
        return Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new),
            list -> list.stream()
                .sorted(Comparator.comparing(item -> Math.abs(item.getDefectStartLine())))
                .collect(Collectors.toList()));
    }

    private ConversionItem createPermissionItem() {
        ConversionItem item = new ConversionItem();
        item.setConversionId(UUID.randomUUID().toString());
        item.setDefectEndLine(1);
        item.setDefectStartLine(1);
        item.setFixEndLine(1);
        item.setFixStartLine(1);
        item.setConvertType(ConvertType.MANUAL);
        item.setFixStatus(FixStatus.AUTOFIX);
        item.setFileTailConvert(false);
        item.setFile(PERMISSION_FILE_PATH);
        item.setDefectContent(PERMISSION_CONTENT);
        item.setMergedDescription(PERMISSION_HELP);

        List<String> kits = new ArrayList<>();
        kits.add(KitsConstants.COMMON);
        item.setKitName(kits.toString());

        ConversionPointDesc description = new ConversionPointDesc();
        description.setStatus(ConvertType.MANUAL);
        description.setSupport(false);
        description.setKit(KitsConstants.COMMON);
        description.setUrl("");
        description.setText(PERMISSION_HELP);
        List<ConversionPointDesc> descriptions = new ArrayList<>();
        descriptions.add(description);
        item.setDescriptions(descriptions);
        return item;
    }

    private void buildApiKey2FileIdAndBlockIdMap(String kit, String oldNameInDesc, String fileId, String blockId,
        Map<ApiKey, Set<String>> apiKey2FileIdSetMap, Map<ApiKey, Set<String>> apiKey2BlockIdMap) {
        if (kit.equals(KitsConstants.COMMON) || kit.equals(KitsConstants.OTHER) || StringUtils.isEmpty(oldNameInDesc)) {
            return;
        }

        // trim GMS name in desc
        ApiKey apiKey = ApiKey.builder().kit(kit).oldNameInDesc(oldNameInDesc.trim()).build();
        apiKey2FileIdSetMap.putIfAbsent(apiKey, new HashSet<>());
        apiKey2FileIdSetMap.get(apiKey).add(fileId);
        apiKey2BlockIdMap.putIfAbsent(apiKey, new HashSet<>());
        apiKey2BlockIdMap.get(apiKey).add(blockId);
    }

    private void countApiKey2FileAndBlockMap(Map<ApiKey, Set<String>> apiKey2FileIdSetMap,
        Map<ApiKey, Set<String>> apiKey2BlockIdMap, Map<ApiKey, Integer> apiKey2FileCountMap,
        Map<ApiKey, Integer> apiKey2BlockCountMap) {
        apiKey2FileIdSetMap.forEach((apiKey, fileIdSet) -> apiKey2FileCountMap.put(apiKey, fileIdSet.size()));
        apiKey2BlockIdMap.forEach((apiKey, blockIdSet) -> apiKey2BlockCountMap.put(apiKey, blockIdSet.size()));
    }
}
