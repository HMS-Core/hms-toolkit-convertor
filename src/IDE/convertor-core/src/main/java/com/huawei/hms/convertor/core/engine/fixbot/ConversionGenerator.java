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

import com.alibaba.fastjson.JSONException;
import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.dispatch.model.DefectFile;
import com.huawei.codebot.framework.model.DefectBlock;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotConstants;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.KitUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private static final String PERMISSION_CONTENT = "add uses-permissions";

    private static final String PERMISSION_HELP = "Please add uses-permissions into \"AndroidManifest.xml\" of "
        + "your application module. Check section 3 of Readme file in xmsadapter module for more details.";

    private static final String INIT_DEFECT_CONTENT = "GlobalEnvSetting.init()";

    private static final String INIT_HELP = "Notice: invoke GlobalEnvSetting.init() to initialize GMS and HMS "
        + "environment variables for Add HMS API policy.";

    private static final String ORIGINAL_DIR = "original";

    private static final int ORIGINAL_DIR_LEN = ORIGINAL_DIR.length();

    private FixbotResultParser fixbotResultParser;

    private RoutePolicy routePolicy;

    private Map<String, List<ConversionPointDesc>> id2DescriptionsMap;

    private String type;

    private String resultFilePath;

    private String projectBasePath;

    public ConversionGenerator(FixbotResultParser fixbotResultParser) {
        this.fixbotResultParser = fixbotResultParser;
        this.routePolicy = fixbotResultParser.getRoutePolicy();
        this.id2DescriptionsMap = fixbotResultParser.getId2DescriptionsMap();
        this.type = fixbotResultParser.getType();
        this.resultFilePath = fixbotResultParser.getResultPath();
        this.projectBasePath = fixbotResultParser.getProjectBasePath();
    }

    public List<ConversionItem> extractConverisons() throws JSONException {
        List<ConversionItem> autoConversionList = new ArrayList<>();
        List<ConversionItem> manualConversionList = new ArrayList<>();

        final int resultFilePathLen = resultFilePath.length();

        List<DefectFile> defectFiles = fixbotResultParser.getDefectFiles();
        if (defectFiles == null) {
            log.warn("The defect files list is null");
            return Collections.emptyList();
        }

        for (DefectFile defectFile : defectFiles) {
            final String originalFilePath = defectFile.getFilePath().replace("\\", "/");
            final String fileRelativePath = originalFilePath.substring(resultFilePathLen + 1 + ORIGINAL_DIR_LEN + 1);
            for (DefectBlock fixedBlock : defectFile.getAutoFixedBlocks()) {
                ConversionItem conversionItem = new ConversionItem();
                List<ConversionPointDesc> descriptions = id2DescriptionsMap.get(fixedBlock.getId());

                // Extract 'Kit Name' for the conversion item.
                HashSet<String> kitSet = new HashSet<>();
                final String fixFilePath = resultFilePath + Constant.SEPARATOR + FixbotConstants.FIXBOT_DIR
                        + Constant.SEPARATOR + fileRelativePath;
                descriptions.forEach(description -> {
                    if (description.getKit() != null && KitUtil.supportKitToH(description.getKit())) {
                        kitSet.add(description.getKit());
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
                constructConvertedContent(originalFilePath, fixFilePath, fixedBlock, conversionItem);

                // Extract the description for the conversion item.
                constructDescription(autoConversionList, manualConversionList, conversionItem, descriptions);
            }
        }

        // When the policy is Add HMS API, the conversion list needs to be customized.
        if (routePolicy == RoutePolicy.G_AND_H) {
            // Add the manual conversion item that describes how to add uses-permissions.
            ConversionItem permissionItem = createPermissionItem();
            manualConversionList.add(permissionItem);

            // Filter all conversions about "xmsadapter/build.gradle" file.
            autoConversionList.removeIf(item -> item.getFile().contains("xmsadapter/build.gradle"));
            manualConversionList.removeIf(item -> item.getFile().contains("xmsadapter/build.gradle"));
        }

        // Sort and merge conversion list.
        List<ConversionItem> conversionItems = sortAndMergeList(autoConversionList, manualConversionList);

        // Add the manual conversion item that describes how to init xms adapter in the Add HMS API (App) policy.
        if (routePolicy == RoutePolicy.G_AND_H && ProjectConstants.Type.APP.equals(type)) {
            ConversionItem lastItem = createInitItem();
            conversionItems.add(lastItem);
        }
        return conversionItems;
    }

    private void constructConvertedContent(
        String originalFilePath, String fixFilePath, DefectBlock fixedBlock, ConversionItem conversionItem) {
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
                readDocument(
                    originalFilePath, conversionItem.getDefectStartLine(), conversionItem.getDefectEndLine()));
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
            log.error("Get file content error: " + e.getMessage(), e);
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
        if (newDescription.subSequence(newDescription.length() - 2, newDescription.length()).equals("; ")) {
            newDescription.delete(newDescription.length() - 2, newDescription.length());
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
        if (text.equals("Please add relevant package:")) {
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
        if (descriptions.isEmpty() || descriptions.size() < 2) {
            return;
        }

        boolean flag = false;
        for (ConversionPointDesc description : descriptions) {
            if (description.getType() == null) {
                continue;
            }
            if (description.getType().equals("class") && !description.isSupport()) {
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
                if ((description.getType().equals("method") || description.getType().equals("field"))
                    && description.isSupport()) {
                    filterEnabled = true;
                    break;
                }
            }
        }

        if (filterEnabled) {
            descriptions
                .removeIf(item -> item.getType() != null && item.getType().equals("class") && !item.isSupport());
        }
    }

    private List<ConversionItem> sortAndMergeList(
        List<ConversionItem> autoConvertList, List<ConversionItem> manualConvertList) {
        int autoConvertCount;
        List<ConversionItem> normalAutoList = getAutoOrDummyDefectItemList(autoConvertList, ConvertType.AUTO);
        List<ConversionItem> dummyAutoList = getAutoOrDummyDefectItemList(autoConvertList, ConvertType.DUMMY);

        // Sort normal auto conversion list.
        Map<String, List<ConversionItem>> file2NormalAutoListMap = normalAutoList.stream()
            .filter(defectItem -> defectItem.getFixStatus().equals(FixStatus.AUTOFIX))
            .collect(Collectors.groupingBy(ConversionItem::getFile, toSortedList()));
        TreeMap<String, List<ConversionItem>> file2AutoNormalListTreeMap = new TreeMap<>(file2NormalAutoListMap);
        List<ConversionItem> conversionItems = new ArrayList<>();
        for (Iterator ite = file2AutoNormalListTreeMap.keySet().iterator(); ite.hasNext();) {
            String file = ite.next().toString();
            conversionItems.addAll(file2AutoNormalListTreeMap.get(file));
        }

        // Sort dummy auto conversion list.
        Map<String, List<ConversionItem>> file2DummyAutoListMap = dummyAutoList.stream()
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
        int manualConvertCount;
        manualConvertCount = totalCount - autoConvertCount;

        log.info("autoConvertCount = {}, manualConvertCount = {}, totalCount = {}",
            autoConvertCount, manualConvertCount, totalCount);
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

    private ConversionItem createInitItem() {
        return createConversionItem(true);
    }

    private ConversionItem createPermissionItem() {
        return createConversionItem(false);
    }

    /**
     * Generate a customized conversion item.
     *
     * @param isInit init flag
     * @return the customized conversion item
     */
    private ConversionItem createConversionItem(boolean isInit) {
        ConversionItem item = new ConversionItem();
        item.setConversionId(UUID.randomUUID().toString());
        item.setDefectEndLine(1);
        item.setDefectStartLine(1);
        item.setFixEndLine(1);
        item.setFixStartLine(1);
        item.setConvertType(ConvertType.MANUAL);
        item.setFixStatus(FixStatus.AUTOFIX);
        item.setFileTailConvert(false);
        List<String> kits = new ArrayList<>();
        kits.add(KitsConstants.COMMON);
        item.setKitName(kits.toString());
        ConversionPointDesc description = new ConversionPointDesc();
        description.setStatus(ConvertType.MANUAL);
        description.setSupport(false);
        description.setKit(KitsConstants.COMMON);
        description.setUrl("");
        if (isInit) {
            item.setFile(Constant.NA);
            item.setDefectContent(INIT_DEFECT_CONTENT);
            description.setText(INIT_HELP);
            item.setMergedDescription(INIT_HELP);
        } else {
            String filePath = PERMISSION_FILE_PATH;
            item.setFile(filePath);
            item.setDefectContent(PERMISSION_CONTENT);
            description.setText(PERMISSION_HELP);
            item.setMergedDescription(PERMISSION_HELP);
        }
        List<ConversionPointDesc> desps = new ArrayList<>();
        desps.add(description);
        item.setDescriptions(desps);
        return item;
    }
}
