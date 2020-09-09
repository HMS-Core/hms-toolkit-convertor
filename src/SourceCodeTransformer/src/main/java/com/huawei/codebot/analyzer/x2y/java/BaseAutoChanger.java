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

package com.huawei.codebot.analyzer.x2y.java;

import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.lazyfix.SyncedCompositeDefectFixer;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.utils.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the top level changer that consists of a group of low level changers, and these low level changers would
 * be processed in synchronized.
 * <br/>
 *
 * @since 2020-04-21
 */
public abstract class BaseAutoChanger extends SyncedCompositeDefectFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAutoChanger.class);
    /**
     * An identifier of G2H.
     */
    protected static final String FIXER_TYPE_G2H = DefectFixerType.LIBADAPTION.toString();
    /**
     * An identifier of G2X.
     */
    protected static final String FIXER_TYPE_X2Y = DefectFixerType.WISEHUB.toString();

    public BaseAutoChanger() {
        focusedFileExtensions = new String[]{"java", ".kt", "xml", "gradle", "properties", "json", "aidl"};
        defaultIgnoreList = new String[]{".git", ".svn", ".repo", ".mm", ".google", ".opensource", ".idea", ".gradle"};
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
        LOGGER.error("extractFixInstancesForSingleCodeFile method is not implemented.");
    }

    @Override
    public void inferFixPatterns(String outputFilePath) {
        LOGGER.error("inferFixPatterns method is not implemented.");
    }

    @Override
    protected List<DefectInstance> mergeDefectInstancesOfNewFixer(GenericDefectFixer fixer) {
        // Examples of defects in the new fixer that require apply
        List<DefectInstance> defectInstancesToApply = new ArrayList<>();

        // Group defect instances according to line number,
        // and separate instances with the same line number according to isFixed
        Map<String, DefectInstance[]> filterIndex = new HashMap<>();
        groupDefectInstanceByLinesAndIsFix(filterIndex);

        // group new defect instance by file path
        Map<String, List<DefectInstance>> newDefectInstanceMap = new HashMap<>();
        fixer.defectInstances.forEach(
                defectWarning -> {
                    // Used to prevent LazyDefectInstance from being deduplicated and filtered out
                    // 1. If only contains lazy, pass it backwards to prevent lazy from entering the merge function
                    // 2. If only contains direct, then put it into the normal processing flow, normal merge
                    if (defectWarning.buggyLines.isEmpty() && !defectWarning.lazyBuggyLines.isEmpty()) {
                        defectInstancesToApply.add(defectWarning);
                    } else if (!defectWarning.buggyLines.isEmpty() && defectWarning.lazyBuggyLines.isEmpty()) {
                        // Store defect instances according to defect repair files
                        Set<String> buggyFiles = defectWarning.getBuggyAndOtherFiles();
                        buggyFiles.forEach(
                                buggyFile -> {
                                    if (!newDefectInstanceMap.containsKey(buggyFile)) {
                                        newDefectInstanceMap.put(buggyFile, new ArrayList<>());
                                    }
                                    newDefectInstanceMap.get(buggyFile).add(defectWarning);
                                });
                    } else {
                        // Only extract the lazy part
                        DefectInstance lazyTemp = new DefectInstance();
                        lazyTemp.lazyBuggyLines = defectWarning.lazyBuggyLines;
                        lazyTemp.defectType = defectWarning.defectType;
                        lazyTemp.message = defectWarning.message;
                        lazyTemp.mainBuggyLineNumber = defectWarning.mainBuggyLineNumber;
                        lazyTemp.mainBuggyFilePath = defectWarning.mainBuggyFilePath;
                        lazyTemp.mainFixedFilePath = defectWarning.mainFixedFilePath;
                        lazyTemp.mainFixedLineNumber = defectWarning.mainFixedLineNumber;
                        lazyTemp.lazyFixedLines = defectWarning.lazyFixedLines;
                        lazyTemp.isFixed = true;
                        lazyTemp.status = defectWarning.status;
                        defectInstancesToApply.add(lazyTemp);

                        // Extract only the direct part
                        DefectInstance directTemp = new DefectInstance();
                        directTemp.buggyLines = defectWarning.buggyLines;
                        directTemp.defectType = defectWarning.defectType;
                        directTemp.message = defectWarning.message;
                        directTemp.mainBuggyLineNumber = defectWarning.mainBuggyLineNumber;
                        directTemp.mainBuggyFilePath = defectWarning.mainBuggyFilePath;
                        directTemp.mainFixedFilePath = defectWarning.mainFixedFilePath;
                        directTemp.mainFixedLineNumber = defectWarning.mainFixedLineNumber;
                        directTemp.fixedLines = defectWarning.fixedLines;
                        directTemp.isFixed = true;
                        directTemp.status = defectWarning.status;

                        // Store defect instances according to defect repair files
                        Set<String> buggyFiles = directTemp.getBuggyAndOtherFiles();
                        buggyFiles.forEach(
                                buggyFile -> {
                                    if (!newDefectInstanceMap.containsKey(buggyFile)) {
                                        newDefectInstanceMap.put(buggyFile, new ArrayList<>());
                                    }
                                    newDefectInstanceMap.get(buggyFile).add(directTemp);
                                });
                    }
                });

        // merge instance
        for (Map.Entry<String, List<DefectInstance>> entry : newDefectInstanceMap.entrySet()) {
            String buggyFilePath = entry.getKey();

            // orderedBuggyToFixedLineNumber
            List<Pair<Integer, Integer>> orderedBuggyToFixedLineNumber =
                    orderedBuggyToFixedLineNumberMap.get(buggyFilePath);

            for (DefectInstance instance : entry.getValue()) {
                // recover defect instance
                DefectInstance recoveredDefectInstance = recoverDefectInstance(instance, orderedBuggyToFixedLineNumber);

                // key
                String key = buggyFilePath + "###" + recoveredDefectInstance.mainBuggyLineNumber;

                // merge
                DefectInstance oldDefectInstance = null;
                if (!recoveredDefectInstance.isInsertInstance() && filterIndex.containsKey(key)) {
                    DefectInstance[] matchedDefectInstances = filterIndex.get(key);
                    if (instance.isFixed) {
                        oldDefectInstance = matchedDefectInstances[0];
                    } else {
                        oldDefectInstance = matchedDefectInstances[1];
                    }
                }
                if (oldDefectInstance != null) {
                    if (fixer.shouldOverridePreviousFix) {
                        if (instance.isFixed) {
                            instance.defectType = getFixerInfo().type.toString();
                            defectInstancesToApply.add(instance);
                            removeDefectInstance(oldDefectInstance);
                            recoveredDefectInstance.message =
                                    mergeMessage(oldDefectInstance.message, recoveredDefectInstance.message);
                            addNewDefectInstance(recoveredDefectInstance);
                        } else {
                            oldDefectInstance.message = mergeMessage(oldDefectInstance.message, instance.message);
                        }
                    }
                } else if (!fixer.shouldNewFixIgnored) {
                    recoveredDefectInstance.defectType = this.getFixerInfo().type.toString();
                    recoveredDefectInstance.message = updateMessage(recoveredDefectInstance.message);
                    defectInstancesToApply.add(instance);
                    addNewDefectInstance(recoveredDefectInstance);
                }
            }

            // update buggyToFixedLineNumberMap
            updateBuggyToFixedLineNumberMap(buggyFilePath);
        }

        return defectInstancesToApply;
    }


    private void groupDefectInstanceByLinesAndIsFix(Map<String, DefectInstance[]> filterIndex){
        for (DefectInstance instance : defectInstances) {
            String key = instance.mainBuggyFilePath + "###" + instance.mainBuggyLineNumber;
            if (!filterIndex.containsKey(key)) {
                filterIndex.put(key, new DefectInstance[2]);
            }
            DefectInstance[] matchedDefectInstances = filterIndex.get(key);
            if (instance.isFixed) {
                matchedDefectInstances[0] = instance;
            } else {
                matchedDefectInstances[1] = instance;
            }
        }
    }


    @Override
    public String mergeMessage(String oldMessage, String newMessage) {
        return StringUtil.joinMessage(oldMessage, newMessage);
    }

    @Override
    public String updateMessage(String oldMessage) {
        return StringUtil.formatMessage(oldMessage);
    }
}
