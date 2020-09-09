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

package com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger;

import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixBotArguments;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.lazyfix.SyncedCompositeDefectFixer;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.utils.FileUtils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used to conditional change
 * the changer will work when xml and gradle both have some dependency
 *
 * @since 2020-04-13
 */
public class ConditionalChanger extends SyncedCompositeDefectFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalChanger.class);

    private GradleConditionalChanger gradleConditionalChanger;
    private XmlConditionalChanger xmlConditionalChanger;

    public ConditionalChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        List<StructGradleXml> configList = configService.getConditionalConfig();
        gradleConditionalChanger = new GradleConditionalChanger(configList);
        xmlConditionalChanger = new XmlConditionalChanger(configList);
        focusedFileExtensions = new String[]{"xml", "gradle"};
        defaultIgnoreList = new String[]{".git", ".svn", ".repo", ".mm", ".google", ".opensource",
                ".idea", ".gradle"};
    }

    @Override
    protected void initializeAtomicFixers() {
        this.atomicFixers.add(gradleConditionalChanger);
        this.atomicFixers.add(xmlConditionalChanger);
    }

    @Override
    public void preprocessAndAutoFix(FixBotArguments args) throws CodeBotRuntimeException {
        String repoPath = args.getRepoPath();
        String[] ignoreLists = args.getIgnoredList();
        String timestamp = args.getTimestamp();
        String operator = args.getOperator();
        String versionNumber = args.getVersion();
        Date autofixStartDate = new Date();
        initProjectFolders(repoPath);
        copyOriginRepoToTempDirectory(ignoreLists);
        String tempDir = repoPath + TEMPFIXFOLDER_SUFFIX;
        calAnalyzedFilePaths(tempDir, ignoreLists);
        toAnalyzeFileNum = analyzedFilePaths.size();
        this.initializeAtomicFixers();
        String subjectProjectOrModuleName = args.getProjectName();
        for (GenericDefectFixer fixer : atomicFixers) {
            fixer.delayGenerateFixedFiles = true;
            fixer.currentFixedFolderSuffix = TEMPFIXFOLDER_SUFFIX;
            fixer.fixPatternFolder = this.fixPatternFolder;
            fixer.directOverride = true;
            fixer.preprocessAndAutoFix(new FixBotArguments(subjectProjectOrModuleName, tempDir, ignoreLists,
                    timestamp, operator, versionNumber));
            mergeDefectInstancesOfNewFixer(fixer);
        }

        updateFilePathAndCopyToFinalFixedPath();
        // delete xxx_tempFix temp package
        org.apache.commons.io.FileUtils.deleteQuietly(new File(tempDir));

        this.totalFixedDefectNumber = this.defectInstances.size();
        this.totalDetectedDefectNumber = this.defectInstances.size();

        Date autofixEndDate = new Date();
        LOGGER.info("AutofixStartDate: {}", autofixStartDate);
        LOGGER.info("AutofixEndDate: {}", autofixEndDate);
        LOGGER.info("All defects fixed/detected: {}/{}", totalFixedDefectNumber, totalDetectedDefectNumber);
        LOGGER.info("All buggy files updated: {}", totalFixedBuggyFiles);
    }

    private void updateFilePathAndCopyToFinalFixedPath() {
        Set<String> copiedFixeFiles = new HashSet<>();
        String tempDir = currentToFixProjectFolder + TEMPFIXFOLDER_SUFFIX;
        for (DefectInstance defectInstance : this.defectInstances) {
            // main buggy and fixed file path
            String buggyFilePath = defectInstance.mainBuggyFilePath;
            defectInstance.mainBuggyFilePath = buggyFilePath.replace(tempDir, currentToFixProjectFolder);
            String fixedFilePath = defectInstance.mainFixedFilePath;
            if (fixedFilePath != null) {
                defectInstance.mainFixedFilePath = fixedFilePath.replace(tempDir, currentToFixProjectFolder);
            }
            // buggy lines
            MapIterator iterator = defectInstance.buggyLines.mapIterator();
            defectInstance.buggyLines = getNewLines(tempDir, iterator);

            // other lines
            iterator = defectInstance.otherLinesUnderFixing.mapIterator();
            defectInstance.otherLinesUnderFixing = getNewLines(tempDir,iterator);

            // fixed lines
            MultiKeyMap newFixedLines = MultiKeyMap.multiKeyMap(new LinkedMap());
            iterator = defectInstance.fixedLines.mapIterator();
            while (iterator.hasNext()) {
                MultiKey keys = (MultiKey) iterator.next();
                String filePath = (String) keys.getKey(0);
                Integer lineNum = (Integer) keys.getKey(1);
                String content = (String) iterator.getValue();
                String originalFilePath = filePath.replace(tempDir, currentToFixProjectFolder);
                String newFilePath = filePath.replace(tempDir, currentFixedProjectFolder);
                if (!copiedFixeFiles.contains(newFilePath)) {
                    FileUtils.copyFile(filePath, newFilePath);
                    copiedFixeFiles.add(newFilePath);
                    this.totalFixedBuggyFiles++;
                }
                newFixedLines.put(originalFilePath, lineNum, content);
            }
            defectInstance.fixedLines = newFixedLines;
        }

        // buggy to fixed line number map
        Map<String, MultiKeyMap> newBuggyToFixedLineNumberMap = new HashMap<>();
        for (Map.Entry<String, MultiKeyMap> entry : buggyToFixedLineNumMap.entrySet()) {
            String filePath = entry.getKey().replace(tempDir, currentToFixProjectFolder);
            newBuggyToFixedLineNumberMap.put(filePath, entry.getValue());
        }
        buggyToFixedLineNumMap = newBuggyToFixedLineNumberMap;
    }

    private MultiKeyMap getNewLines(String tempDir, MapIterator iterator) {
        MultiKeyMap newLines = MultiKeyMap.multiKeyMap(new LinkedMap());
        while (iterator.hasNext()) {
            MultiKey keys = (MultiKey) iterator.next();
            String filePath = (String) keys.getKey(0);
            Integer lineNum = (Integer) keys.getKey(1);
            String content = (String) iterator.getValue();
            String newFilePath = filePath.replace(tempDir, currentToFixProjectFolder);
            newLines.put(newFilePath, lineNum, content);
        }
        return newLines;
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_CROSSGRADLEXML;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }
}
