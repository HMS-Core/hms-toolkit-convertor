/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.codebot.analyzer.x2y.java.lazyfix;

import com.huawei.codebot.framework.lazyfix.LazyFixer;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.utils.ComparatorUtil;
import org.apache.commons.collections4.keyvalue.MultiKey;

import java.util.ArrayList;
import java.util.List;

/**
 * An concrete changer used to process lazy defect instance
 *
 * @since 2020-04-17
 */
public class LazyFixChanger extends LazyFixer {
    public LazyFixChanger() {
        focusedFileExtensions = new String[] {"java", "kt", "gradle", "xml", "properties", "json", "aidl"};
        defaultIgnoreList = new String[] {".git", ".svn", ".mm", ".repo", ".google", ".opensource", ".git"};
    }

    @Override
    protected List<DefectInstance> preprocessDefectInstances(List<DefectInstance> defectInstanceList) {
        return dealWithImportMessage(lazyFixMerge(defectInstanceList));
    }

    @Override
    protected List<DefectInstance> postprocessDefectInstances(List<DefectInstance> defectInstanceList) {
        return lazyDeduplication(defectInstanceList);
    }

    /**
     * lazyFix also has multiple imports converted into one import. In this case, deduplication is required.
     * If the descriptions of desc are consistent, all duplicate fixLines are set to null and deleted directly.
     */
    private List<DefectInstance> lazyDeduplication(List<DefectInstance> defectInstancesFinalTemp) {
        String lineBreak = System.lineSeparator();
        List<DefectInstance> defectInstancesReturn = new ArrayList<>();
        List<String> deduplicationString = new ArrayList<>();
        for (DefectInstance defectInstance : defectInstancesFinalTemp) {
            Object fixedLineObjectTemp =
                    defectInstance.fixedLines.get(defectInstance.mainFixedFilePath, defectInstance.mainFixedLineNumber);
            if (fixedLineObjectTemp instanceof String) {
                String fixedLine = (String) fixedLineObjectTemp;
                String[] fixedLineSplit = fixedLine.split(lineBreak);
                StringBuilder finalFixLine = new StringBuilder();
                for (String s : fixedLineSplit) {
                    if (!deduplicationString.contains(s)) {
                        deduplicationString.add(s);
                        finalFixLine.append(lineBreak).append(s);
                    }
                }
                // If it is a 2 to 1 conversion, one line will be recognized as an empty ""
                if (finalFixLine.toString().equals("")) {
                    finalFixLine.append("  ");
                }
                // If a conversion is performed and it is proven to start with LineBreak,the first LineBreak is removed
                if (finalFixLine.toString().startsWith(lineBreak)) {
                    finalFixLine = new StringBuilder(finalFixLine.substring(lineBreak.length(), finalFixLine.length()));
                }
                defectInstance.fixedLines.put(
                        defectInstance.mainFixedFilePath, defectInstance.mainFixedLineNumber, finalFixLine.toString());
                defectInstancesReturn.add(defectInstance);
            }
        }
        return defectInstancesReturn;
    }

    /**
     * According to special requirements,
     * if there are any modifications in the import area,
     * modify its desc according to the added content
     */
    private List<DefectInstance> dealWithImportMessage(List<DefectInstance> defectInstancesOrigin) {
        String lineBreak = System.lineSeparator();
        List<DefectInstance> finalList = new ArrayList<>();
        if (defectInstancesOrigin != null) {
            for (DefectInstance defectInstanceOne : defectInstancesOrigin) {
                String isImportMessageTemp = defectInstanceOne.lazyFixedLines.values().toString();
                String isImportMessage =
                        isImportMessageTemp.substring(1, isImportMessageTemp.length() - 1); // delete "[]"
                if (!isImportMessage.contains("import ")) {
                    finalList.add(defectInstanceOne);
                } else {
                    String[] listDesc = isImportMessage.split(lineBreak);
                    String descMessage = defectInstanceOne.message;

                    defectInstanceOne.message = specialDesc(listDesc, descMessage); // update desc
                    finalList.add(defectInstanceOne); // Add the results after updating desc
                }
            }
        }
        return finalList;
    }

    private String specialDesc(String[] listDesc, String descMessage) {
        // Specific needs
        // Added description of reading the kit field in desc in defectInstance
        // Use the string matching scheme to analysis of the version field existing in the kit
        String kitString = "other";
        String kitTempString = descMessage.replaceAll(" ", ""); // delete all spaces
        if (kitTempString.contains("\"kit\":\"")) {
            int kitLocation = kitTempString.indexOf("\"kit\":\"");
            int startNumber = kitLocation + 7; // add the length for -> kit":"
            int length = 0;
            for (int i = 0; i < descMessage.length() - startNumber; i++) {
                if (kitTempString.charAt(startNumber + i) != '\"') {
                    length++;
                } else {
                    break;
                }
            }
            kitString = kitTempString.substring(startNumber, startNumber + length);
        }

        // get desc from special-desc-standard.json
        StringBuilder newDesc =
                new StringBuilder(
                        "{\"text\":\"" + "Please add relevant package:" + "\",\"url\":\"\",\"status\":\"AUTO\"}");
        for (String s : listDesc) {
            String tempString = s.substring("import ".length(), s.length() - 1);
            newDesc.append(",")
                    .append("{\"text\":\"")
                    .append(tempString)
                    .append("\",")
                    .append("\"kit\":\"")
                    .append(kitString)
                    .append("\",\"url\":\"\",\"status\":\"AUTO\"}"); // update desc files
        }
        return newDesc.toString();
    }

    /**
     * Merge all the contents of lazyDefectInstance,
     * and if the line numbers are the same,
     * merge the modified multiple lines into one line
     */
    private List<DefectInstance> lazyFixMerge(List<DefectInstance> defectInstanceList) {
        // The lazy here is in order. The previous order has been sorted according to the row number.
        // There will be very few unsorted.
        defectInstanceList.sort(ComparatorUtil.defectInstanceLazyFixComparator);
        List<DefectInstance> finalDefectInstance = new ArrayList<>();

        // Add the first lazyDefectInstance directly
        finalDefectInstance.add(defectInstanceList.get(0));
        for (int i = 1; i < defectInstanceList.size(); i++) {
            // If the final one of finalDefectInstance is the same as the one traversed, skip directly to the next item
            if (defectInstanceList.get(i).equals(finalDefectInstance.get(finalDefectInstance.size() - 1))) {
                continue;
            }

            // If the line number is different, use it directly
            if (!defectInstanceList
                    .get(i)
                    .mainBuggyLineNumber
                    .equals(finalDefectInstance.get(finalDefectInstance.size() - 1).mainBuggyLineNumber)) {
                finalDefectInstance.add(defectInstanceList.get(i));
            } else {
                // If the line numbers are the same, but the contents are different,
                // then this information is merged into the last defectInstance of finalDefectInstance
                int tempNumber = finalDefectInstance.size() - 1;
                DefectInstance lazyFinalTemp = finalDefectInstance.get(tempNumber);

                // Deduplication lazy defectInstance
                DefectInstance lazyTemp = defectInstanceList.get(i);

                // Supplement message, if not included, it exists in the previous one
                if (lazyFinalTemp.message != null
                        && lazyTemp.message != null
                        && !lazyFinalTemp.message.contains(lazyTemp.message)) {
                    lazyFinalTemp.message = lazyFinalTemp.message + ", " + lazyTemp.message;
                }

                // Supplement fixedline, if lazy does not contain, it exists in the previous one
                String finalStringTemp = lazyFinalTemp.lazyFixedLines.values().toString();
                String lazyStringTemp = lazyTemp.lazyFixedLines.values().toString();
                String lazyFinalTempFixline = finalStringTemp.substring(1, finalStringTemp.length() - 1);
                String lazyTempFixline = lazyStringTemp.substring(1, lazyStringTemp.length() - 1);

                // Get system newlines
                if (!lazyFinalTempFixline.contains(lazyTempFixline)) {
                    String lineBreak = System.lineSeparator();
                    lazyFinalTempFixline = lazyFinalTempFixline + lineBreak + lazyTempFixline;
                    for (Object keyTemp : lazyTemp.lazyFixedLines.keySet()) {
                        if (keyTemp instanceof MultiKey) {
                            MultiKey multiKey = (MultiKey) keyTemp;
                            if (multiKey.getKey(0) instanceof String && multiKey.getKey(1) instanceof Integer) {
                                String firstKey = (String) multiKey.getKey(0);
                                Integer secondKey = (Integer) multiKey.getKey(1);
                                // Update LazyFixLine
                                lazyFinalTemp.lazyFixedLines.put(firstKey, secondKey, lazyFinalTempFixline);
                            }
                        }
                    }
                }
                finalDefectInstance.remove(tempNumber); // pop the last one
                finalDefectInstance.add(lazyFinalTemp); // add the last updated defectInstance
            }
        }
        return finalDefectInstance;
    }
}
