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

package com.huawei.codebot.entry.codemigrate;

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.GradleProjectInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.java.Code2CommentChanger;
import com.huawei.codebot.framework.FixBotArguments;
import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.ICodeMigrateChanger;
import com.huawei.codebot.framework.api.CodeBotResultCode;
import com.huawei.codebot.framework.context.Context;
import com.huawei.codebot.framework.dispatch.model.DefectFile;
import com.huawei.codebot.framework.dispatch.model.DefectFile.StatusEnum;
import com.huawei.codebot.framework.dispatch.model.UIDefectInstance;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.BlockType;
import com.huawei.codebot.framework.model.DefectBlock;
import com.huawei.codebot.framework.model.DefectBlockIndex;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.utils.PathUtil;
import com.huawei.codebot.framework.utils.UUIDUtil;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The entry of the process which generate final output by intermediate output
 *
 * @since 2020-04-01
 */
public class CodeMigrateEntry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeMigrateEntry.class);
    /**
     * Id of the repository we want to process.
     */
    private String repoId;

    /**
     * Absolute path of the original repository that we want to process.
     */
    private String repoBasePath;

    /**
     * Absolute path of intermediate output directory.
     */
    private String fixedBasePath;

    /**
     * Absolute path of final output directory.
     */
    private String fixedResultPath;

    /**
     * Absolute path of below {@link #fixedResultPath},
     * used for storing a copy of {@link #repoBasePath}'s original files, usually named 'original'.
     */
    private String originalBasePath;

    /**
     * Absolute path of below {@link #fixedResultPath},
     * used for storing a copy of intermediate output, usually named 'fixbot'.
     */
    private String fixBotBasePath;

    /**
     * Absolute path of below {@link #fixedResultPath},
     * used for storing a copy of intermediate output, usually named 'manual'.
     */
    private String manualBasePath;

    /**
     * Mapping of files between {@link #repoBasePath} and {@link #originalBasePath}.
     */
    private Map<String, String> buggyFilePathInRepoAndOriginalPathMap;

    /**
     * Mapping of file path and the file's corresponding defectFile
     */
    private Map<String, DefectFile> uiDefectFiles;

    /**
     * A list of UIDefectInstance
     */
    private List<UIDefectInstance> uiDefectInstances;

    public CodeMigrateEntry(FixBotArguments arguments) {
        this.repoId = arguments.getRepoId();
        this.repoBasePath = arguments.getRepoPath();
        this.fixedResultPath = arguments.getFixedFilePath();

        this.fixedBasePath = repoBasePath + ICodeMigrateChanger.FIXFOLDER_SUFFIX;

        this.originalBasePath = Paths.get(fixedResultPath, "original").toString();
        this.fixBotBasePath = Paths.get(fixedResultPath, "fixbot").toString();
        this.manualBasePath = Paths.get(fixedResultPath, "manual").toString();
        buggyFilePathInRepoAndOriginalPathMap = new HashMap<>();
        uiDefectFiles = new TreeMap<>();
        uiDefectInstances = new ArrayList<>();
    }

    /**
     * <p>
     * Create folders of final output
     * </p>
     * <br/>
     * <p>
     * By default, the following three folders are automatically created:
     * <ul>
     * <li>${user.home}/.fixbotadaptor/repos/${repoId}/original</li>
     * <li>${user.home}/.fixbotadaptor/repos/${repoId}/fixBot</li>
     * <li>${user.home}/.fixbotadaptor/repos/${repoId}/manual</li>
     * </ul>
     * </p>
     */
    private void prepareFixBotFolder() throws CodeBotRuntimeException {
        if (!FileUtils.mkdirs(originalBasePath)) {
            throw new CodeBotRuntimeException(
                    CodeBotResultCode.FILE_OPERATION, "Fail to create original dir: " + originalBasePath, null, null);
        }
        if (!FileUtils.mkdirs(fixBotBasePath)) {
            throw new CodeBotRuntimeException(
                    CodeBotResultCode.FILE_OPERATION,
                    "Fail to create fixbotadaptor dir: " + fixBotBasePath,
                    null,
                    null);
        }
        if (!FileUtils.mkdirs(manualBasePath)) {
            throw new CodeBotRuntimeException(
                    CodeBotResultCode.FILE_OPERATION, "Fail to create manual dir: " + manualBasePath, null, null);
        }
    }

    /**
     * Copy files from {@link #repoBasePath} or {@link #fixedBasePath} to {@link #fixedResultPath}'s corresponding
     * subdirectory, include {@link #originalBasePath}, {@link #fixBotBasePath} and {@link #manualBasePath}
     *
     * @param fixer supply fixedFiles
     */
    private void copyFixBotFiles(GenericDefectFixer fixer) {
        LOGGER.info("copy file to fixbot folder");

        // all files in repoPath that contain buggyLines
        Set<String> buggyAndOtherFiles = fixer.getBuggyAndOtherFiles();
        // all files in repoPath that contain to be fixed lines
        Set<String> fixedFiles = fixer.getFixedFiles();

        // all files in repoPath that just contain buggyLines
        Set<String> buggyOnlyFiles = new HashSet<>(buggyAndOtherFiles);
        buggyOnlyFiles.removeAll(fixedFiles);
        for (String file : buggyOnlyFiles) {
            copyFixBotFile(file, BlockType.DEFECT_ONLY, fixer);
        }

        // all files in repoPath that contain buggyLines and to be fixed lines
        Set<String> buggyAndFixedFiles = new HashSet<>(buggyAndOtherFiles);
        buggyAndFixedFiles.retainAll(fixedFiles);
        for (String file : buggyAndFixedFiles) {
            copyFixBotFile(file, BlockType.DEFECT_AND_FIX, fixer);
        }

        // all files in repoPath that just contain to be fixed lines
        Set<String> fixedOnlyFiles = new HashSet<>(fixedFiles);
        fixedOnlyFiles.removeAll(buggyAndOtherFiles);
        for (String file : fixedOnlyFiles) {
            copyFixBotFile(file, BlockType.FIX_ONLY, fixer);
        }
    }

    /**
     * <p>
     *     Copy file from {@link #repoBasePath} or {@link #fixedBasePath} to {@link #fixedResultPath}'s corresponding
     *     subdirectory, include {@link #originalBasePath}, {@link #fixBotBasePath} and {@link #manualBasePath}
     * </p>
     * <br/>
     * <p>
     *     Actually, if a file:
     *     <ul>
     *         <li>
     *             has defect but is not fixed, we copy it from {@link #repoBasePath} to {@link #originalBasePath},
     *             {@link #fixBotBasePath} and {@link #manualBasePath}
     *         </li>
     *         <li>
     *             is fixed, we copy it from {@link #repoBasePath} to {@link #originalBasePath}, from
     *             {@link #fixedBasePath} to {@link #fixBotBasePath} and {@link #manualBasePath}
     *         </li>
     *     </ul>
     * </p>
     *
     * @param filePath  the original file path
     * @param blockType an identification of file type, has three possible values, defect_only, defect_and_fix, fix_only
     * @param fixer     supply corresponding files
     */
    private void copyFixBotFile(String filePath, BlockType blockType, GenericDefectFixer fixer) {
        String[] filePaths = calculateCopiedFilePaths(filePath, blockType, fixer);

        // Step1: copy to originalBasePath
        boolean copySucceed = FileUtils.copyFile(filePaths[0], filePaths[1]);
        if (!copySucceed) {
            LOGGER.error("fail to copy buggy file " + filePaths[0]);
        }
        // Step2: copy to fixBotBasePath
        copySucceed = FileUtils.copyFile(filePaths[2], filePaths[3]);
        if (!copySucceed) {
            LOGGER.error("fail to copy fixed file {}" + filePaths[2]);
        }
        // Step3: copy to manualBasePath
        copySucceed = FileUtils.copyFile(filePaths[2], filePaths[4]);
        if (!copySucceed) {
            LOGGER.error("fail to copy manual file {}" + filePaths[2]);
        }
    }

    /**
     * Calculate the corresponding {@link #fixedResultPath} paths of filePath according to the type of defect block
     *
     * @param filePath  original file path
     * @param blockType an identification of file type, has three possible values, defect_only, defect_and_fix, fix_only
     * @param fixer     supply corresponding files
     * @return an array that represent a mapping of files
     *          <br/>
     *          array[0] -> array[1], array[2] -> array[3] and array[4]
     *          <br/>
     *          {@link #repoBasePath}, {@link #originalBasePath}, {@link #fixedBasePath},
     *          {@link #fixBotBasePath}, {@link #manualBasePath}
     */
    private String[] calculateCopiedFilePaths(String filePath, BlockType blockType, GenericDefectFixer fixer) {
        String fixedFilePath;
        String sourceBasePath;
        String originalFilePath;
        String fixBotFilePath;
        String manualFilePath;

        if (blockType == BlockType.DEFECT_ONLY) {
            // this situation all file paths in final output are mapping to filePath
            fixedFilePath = filePath;
            sourceBasePath = repoBasePath;
        } else {
            // same as DEFECT_AND_FIX
            fixedFilePath = fixer.getCorrespondingFixedFile(filePath);
            sourceBasePath = fixedBasePath;
        }
        originalFilePath = PathUtil.getTargetFilePath(filePath, repoBasePath, originalBasePath);
        fixBotFilePath = PathUtil.getTargetFilePath(fixedFilePath, sourceBasePath, fixBotBasePath);
        manualFilePath = PathUtil.getTargetFilePath(fixedFilePath, sourceBasePath, manualBasePath);

        // establish path mapping between files in repoPath and FixBot-ui's original path
        // we need this map for updating file path for defectInstance
        buggyFilePathInRepoAndOriginalPathMap.put(filePath, originalFilePath);

        return new String[] {filePath, originalFilePath, fixedFilePath, fixBotFilePath, manualFilePath};
    }

    /**
     * Update all file path of fixer's defectInstances from {@link #repoBasePath} to {@link #fixedResultPath}
     *
     * @param fixer supply defectInstance
     */
    private void updateFilePath(GenericDefectFixer fixer) {
        // update for all defectInstance of fixer
        for (DefectInstance defectInstance : fixer.defectInstances) {
            // update mainBuggyFilePath and mainFixedFilePath
            defectInstance.mainBuggyFilePath =
                    buggyFilePathInRepoAndOriginalPathMap.get(defectInstance.mainBuggyFilePath);
            defectInstance.mainFixedFilePath =
                    buggyFilePathInRepoAndOriginalPathMap.get(defectInstance.mainFixedFilePath);

            // update buggyLines
            MapIterator iterator = defectInstance.buggyLines.mapIterator();
            defectInstance.buggyLines = updateFilePathInMultiKeyMap(iterator);

            // update otherLinesUnderFixing
            iterator = defectInstance.otherLinesUnderFixing.mapIterator();
            defectInstance.otherLinesUnderFixing = updateFilePathInMultiKeyMap(iterator);

            // update fixedLines
            iterator = defectInstance.fixedLines.mapIterator();
            defectInstance.fixedLines = updateFilePathInMultiKeyMap(iterator);
        }

        // update buggyToFixedLineNumMap's key
        Map<String, MultiKeyMap> buggyToFixedLineNumMap = new HashMap<>();
        for (Map.Entry<String, MultiKeyMap> entry : fixer.buggyToFixedLineNumMap.entrySet()) {
            String filePath = entry.getKey();
            buggyToFixedLineNumMap.put(buggyFilePathInRepoAndOriginalPathMap.get(filePath), entry.getValue());
        }
        fixer.buggyToFixedLineNumMap = buggyToFixedLineNumMap;
    }

    private MultiKeyMap updateFilePathInMultiKeyMap(MapIterator iterator) {
        MultiKeyMap newMap = new MultiKeyMap();
        while (iterator.hasNext()) {
            MultiKey keys = (MultiKey) iterator.next();
            String filePath = (String) keys.getKey(0);
            Integer lineNum = (Integer) keys.getKey(1);
            String content = (String) iterator.getValue();
            newMap.put(buggyFilePathInRepoAndOriginalPathMap.get(filePath), lineNum, content);
        }
        return newMap;
    }

    /**
     * Generate these two fields {@link #uiDefectInstances} and {@link #uiDefectFiles},
     * these two fields will be used for generating DefectInstance.json and DefectFile.json
     *
     * @param repoId ID of repository under fixbot_dir, like 'test1101'
     * @param fixer supply defectInstance
     */
    private void generateFixBotUIData(String repoId, GenericDefectFixer fixer) {
        LOGGER.info("generate fixbotadaptor ui data");
        for (DefectInstance defectInstance : fixer.defectInstances) {
            String buggyFilePath = defectInstance.mainBuggyFilePath;

            // generate uiDefectInstance by fixer's defectInstance
            com.huawei.codebot.framework.dispatch.model.UIDefectInstance uiDefectInstance =
                    generateUiDefectInstance(defectInstance);
            uiDefectInstances.add(uiDefectInstance);

            // set corresponding fields in uiDefectInstance from fixer's defectInstance
            copyFields(repoId, fixer, defectInstance, buggyFilePath, uiDefectInstance);

            // buggyLines
            setBuggyLines(repoId, fixer, defectInstance, uiDefectInstance);

            // fixed lines
            setFixedLines(repoId, fixer, defectInstance, buggyFilePath, uiDefectInstance);
        }
    }

    private void copyFields(String repoId, GenericDefectFixer fixer, DefectInstance defectInstance,
                            String buggyFilePath, UIDefectInstance uiDefectInstance) {
        DefectFile mainDefectFile = generateDefectFile(repoId, defectInstance.mainBuggyFilePath);
        uiDefectInstance.setDefectMainFileId(mainDefectFile.getId());
        uiDefectInstance.setDefectMainLine(Math.abs(defectInstance.mainBuggyLineNumber));
        if (defectInstance.mainFixedFilePath != null) {
            DefectFile mainFixedFile = generateDefectFile(repoId, defectInstance.mainFixedFilePath);
            uiDefectInstance.setFixedMainFileId(mainFixedFile.getId());
            int mainFixedLineNumber =
                    (Integer)
                            fixer.buggyToFixedLineNumMap
                                    .get(buggyFilePath)
                                    .get(defectInstance.mainFixedLineNumber, defectInstance);
            uiDefectInstance.setFixedMainLine(Math.abs(mainFixedLineNumber));
        }
    }

    private void setFixedLines(
            String repoId,
            GenericDefectFixer fixer,
            DefectInstance defectInstance, String buggyFilePath, UIDefectInstance uiDefectInstance) {
        MapIterator iterator = defectInstance.fixedLines.mapIterator();
        while (iterator.hasNext()) {
            // get file path and line number
            MultiKey keys = (MultiKey) iterator.next();
            String filePath = (String) keys.getKey(0);
            Integer fixedStartLineNum = (Integer) keys.getKey(1);

            if (fixedStartLineNum == null) {
                continue;
            }

            // skip when the defectInstance is both a buggy and a fixed point
            int buggyStartLineNum = fixedStartLineNum;
            if (!defectInstance.otherLinesUnderFixing.containsKey(filePath, buggyStartLineNum)) {
                continue;
            }

            // get defect file
            DefectFile defectFile = generateDefectFile(repoId, filePath);
            uiDefectInstance.addDefectFileIdItem(defectFile.getId());
            defectFile.addDefectInstancesItem(uiDefectInstance.getId());

            // create defect block
            FixStatus fixStatus = FixStatus.fromValue(defectInstance.getStatus());
            DefectBlock defectBlock = generateDefectBlock(fixStatus, defectFile);

            // block index
            DefectBlockIndex blockIndex = generateDefectBlockIndex(defectBlock, defectFile);
            uiDefectInstance.addDefectBlocksItem(blockIndex);
            // main fixed line
            if (defectInstance.mainFixedFilePath.equals(filePath)
                    && defectInstance.mainFixedLineNumber.intValue() == fixedStartLineNum) {
                // main fixed block
                uiDefectInstance.setMainFixedBlock(blockIndex);
            }

            // calculate line number according to fix type (add, delete or update)
            fixedStartLineNum =
                    (Integer)
                            fixer.buggyToFixedLineNumMap.get(buggyFilePath).get(fixedStartLineNum, defectInstance);
            int buggyEndLineNum;
            String buggyLine = (String) defectInstance.otherLinesUnderFixing.get(filePath, buggyStartLineNum);
            int buggyLineLength = (buggyLine == null) ? 0 : StringUtil.getLOC(buggyLine);
            int fixedEndLineNum;
            String fixedLine = (String) iterator.getValue();
            int fixedLineLength = (fixedLine == null) ? 0 : StringUtil.getLOC(fixedLine);
            if (fixedStartLineNum > 0) {
                buggyEndLineNum = buggyStartLineNum + buggyLineLength - 1;
                if (fixedLine == null) {
                    // branch delete statement
                    fixedStartLineNum = -fixedStartLineNum;
                    fixedEndLineNum = fixedStartLineNum;
                } else {
                    // branch update statement
                    fixedEndLineNum = fixedStartLineNum + fixedLineLength - 1;
                }
            } else {
                // branch of add statement
                buggyEndLineNum = buggyStartLineNum;
                fixedStartLineNum = -fixedStartLineNum;
                fixedEndLineNum = fixedStartLineNum + fixedLineLength - 1;
            }

            defectBlock.setDefectBlockStartLine(buggyStartLineNum);
            defectBlock.setDefectBlockEndLine(buggyEndLineNum);
            defectBlock.setAutoFixedBlockStartLine(fixedStartLineNum);
            defectBlock.setAutoFixedBlockEndLine(fixedEndLineNum);
            defectBlock.setManualFixedBlockStartLine(fixedStartLineNum);

            defectBlock.setManualFixedBlockEndLine(fixedEndLineNum);

            // block type
            defectBlock.setBlockType(BlockType.FIX_ONLY);
        }
    }

    private void setBuggyLines(String repoId, GenericDefectFixer fixer, DefectInstance defectInstance, UIDefectInstance uiDefectInstance) {
        MapIterator iterator = defectInstance.buggyLines.mapIterator();
        while (iterator.hasNext()) {
            // get file path and line number of the buggyLine
            MultiKey keys = (MultiKey) iterator.next();
            String filePath = (String) keys.getKey(0);
            Integer buggyStartLine = (Integer) keys.getKey(1);
            String buggyLine = (String) iterator.getValue();
            int buggyEndLine;
            if (buggyLine == null) {
                buggyEndLine = buggyStartLine;
            } else {
                int buggyLineLength = StringUtil.getLOC(buggyLine);
                buggyEndLine = buggyStartLine + buggyLineLength - 1;
            }

            DefectFile defectFile = generateDefectFile(repoId, filePath);
            uiDefectInstance.addDefectFileIdItem(defectFile.getId());
            defectFile.addDefectInstancesItem(uiDefectInstance.getId());

            FixStatus fixStatus = FixStatus.fromValue(defectInstance.getStatus());
            DefectBlock defectBlock = generateDefectBlock(fixStatus, defectFile);

            DefectBlockIndex blockIndex = generateDefectBlockIndex(defectBlock, defectFile);
            uiDefectInstance.addDefectBlocksItem(blockIndex);
            // set uiDefectInstance's mainDefectBlock if mainBuggy is just same as buggyLine
            if (defectInstance.mainBuggyFilePath.equals(filePath)
                    && defectInstance.mainBuggyLineNumber.intValue() == buggyStartLine.intValue()) {
                uiDefectInstance.setMainDefectBlock(blockIndex);
            }

            // get the fixedLine corresponding to buggyLine
            Integer fixedStartLine =
                    (Integer) fixer.buggyToFixedLineNumMap.get(filePath).get(buggyStartLine, defectInstance);
            String fixedLine = (String) defectInstance.fixedLines.get(filePath, buggyStartLine);
            int fixedEndLine;
            if (fixedLine == null) {
                fixedEndLine = fixedStartLine;
            } else {
                if (fixedStartLine > 0) {
                    fixedEndLine = fixedStartLine + StringUtil.getLOC(fixedLine) - 1;
                } else {
                    fixedEndLine = fixedStartLine - StringUtil.getLOC(fixedLine) + 1;
                }
            }

            if (defectInstance.fixedLines.containsKey(filePath, buggyStartLine)) {
                // both defect and fixed
                if (defectInstance.mainFixedFilePath.equals(filePath)
                        && defectInstance.mainFixedLineNumber.intValue() == buggyStartLine.intValue()) {
                    // main fixed block
                    uiDefectInstance.setMainFixedBlock(blockIndex);
                }
                if (fixedLine == null || buggyStartLine < 0) {
                    // delete statement or add statement
                    fixedStartLine = -fixedStartLine;
                    fixedEndLine = -fixedEndLine;
                }
                defectBlock.setBlockType(BlockType.DEFECT_AND_FIX);
            } else {
                // defects only
                defectBlock.setBlockType(BlockType.DEFECT_ONLY);
            }

            // set line number
            defectBlock.setDefectBlockStartLine(buggyStartLine);
            defectBlock.setDefectBlockEndLine(buggyEndLine);
            defectBlock.setAutoFixedBlockStartLine(fixedStartLine);
            defectBlock.setAutoFixedBlockEndLine(fixedEndLine);
            defectBlock.setManualFixedBlockStartLine(fixedStartLine);
            defectBlock.setManualFixedBlockEndLine(fixedEndLine);
        }
    }

    /**
     * Transform a DefectInstance to a UIDefectInstance
     *
     * @param defectInstance a DefectInstance instance
     * @return a UIDefectInstance instance
     */
    private com.huawei.codebot.framework.dispatch.model.UIDefectInstance generateUiDefectInstance(
            DefectInstance defectInstance) {
        com.huawei.codebot.framework.dispatch.model.UIDefectInstance uiDefectInstance =
                new com.huawei.codebot.framework.dispatch.model.UIDefectInstance();
        uiDefectInstance.setId(defectInstance.objectId.toString());
        uiDefectInstance.setRepoId(repoId);
        uiDefectInstance.setDefectType(defectInstance.getDefectType());
        uiDefectInstance.setDetectionTool("FixBot");
        uiDefectInstance.setDefectDescription(defectInstance.getMessage());
        uiDefectInstance.setStatus(
                com.huawei.codebot.framework.dispatch.model.UIDefectInstance.StatusEnum.NOT_REVIEWED);
        FixStatus fixStatus = FixStatus.fromValue(defectInstance.getStatus());
        uiDefectInstance.setFixStatus(fixStatus);
        uiDefectInstance.setTree(defectInstance.tree);
        return uiDefectInstance;
    }

    /**
     * Return a defectFile which derived from filePath, if it has been in the {@link #uiDefectFiles}, we retrieve it
     * else we create a new one and put it into the map.
     *
     * @param repoId   repo id
     * @param filePath file path that we want to generate defectFile for it
     * @return a DefectFile instance
     */
    private DefectFile generateDefectFile(String repoId, String filePath) {
        DefectFile defectFile;
        if (uiDefectFiles.containsKey(filePath)) {
            defectFile = uiDefectFiles.get(filePath);
        } else {
            defectFile = new DefectFile();
            defectFile.setId(UUIDUtil.get24UUID());
            defectFile.setRepoId(repoId);
            defectFile.setFilePath(filePath);
            defectFile.setStatus(StatusEnum.NOT_REVIEWED);
            uiDefectFiles.put(filePath, defectFile);
        }
        return defectFile;
    }

    /**
     * Generate a defectBlock and add it into defectFile before return it
     *
     * @param fixStatus  status of a defectInstance
     * @param defectFile defectFile to which defectBlock belongs
     * @return a DefectBlock instance
     */
    private DefectBlock generateDefectBlock(FixStatus fixStatus, DefectFile defectFile) {
        DefectBlock defectBlock = new DefectBlock();
        defectBlock.setId(UUIDUtil.get24UUID());
        defectBlock.setStatus(DefectBlock.StatusEnum.NOT_REVIEWED);
        defectBlock.setFixStatus(fixStatus);
        defectFile.addAutoFixedBlocksItem(defectBlock);
        defectFile.addManualFixedBlocksItem(defectBlock);
        return defectBlock;
    }

    /**
     * Generate a index by defectBlockId and defectFileId.
     *
     * @param defectBlock defectBlock
     * @param defectFile  defectFile
     * @return a DefectBlockIndex instance which index defectBlock to defectFile
     */
    private DefectBlockIndex generateDefectBlockIndex(DefectBlock defectBlock, DefectFile defectFile) {
        DefectBlockIndex blockIndex = new DefectBlockIndex();
        blockIndex.setDefectBlockId(defectBlock.getId());
        blockIndex.setDefectFileId(defectFile.getId());
        return blockIndex;
    }

    GenericDefectFixer doProcessing(FixBotArguments arguments, GenericDefectFixer fixer, GenericDefectFixer c2cChanger)
            throws CodeBotRuntimeException {
        boolean time1st = (c2cChanger == null);

        prepareFixBotFolder();

        if (time1st) {
            // 1st time MainEntry4CodeMigrate.launchFix call this method, we process the 'fixer' and clone
            // all defectInstances from 'fixer' to c2cChanger
            copyFixBotFiles(fixer);
            c2cChanger = new Code2CommentChanger();
            c2cChanger.preprocessAndAutoFix(arguments);
            c2cChanger.defectInstances = new ArrayList<>();
            for (DefectInstance defectInstance : fixer.defectInstances) {
                c2cChanger.defectInstances.add(defectInstance.clone());
            }
            updateFilePath(fixer);
        } else {
            // 2nd time call this method, we process the c2cChanger
            copyFixBotFiles(c2cChanger);
            updateFilePath(c2cChanger);
        }

        // delete the intermediate 'xxx_fixed' folder
        org.apache.commons.io.FileUtils.deleteQuietly(new File(fixedBasePath));

        if (time1st) {
            generateFixBotUIData(repoId, fixer);
        } else {
            generateFixBotUIData(repoId, c2cChanger);
        }

        writeFile();

        return c2cChanger;
    }

    private void writeFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        LOGGER.info("write defect instances to file, size: {}", uiDefectInstances.size());
        File defectInstances = new File(this.fixedResultPath, "DefectInstances.json");
        writeJsonFile(defectInstances, gson.toJson(uiDefectInstances));

        LOGGER.info("write defect files to file, size: {}", uiDefectFiles.size());
        File defectFiles = new File(this.fixedResultPath, "DefectFiles.json");
        writeJsonFile(defectFiles, gson.toJson(uiDefectFiles.values()));

        Context context = Context.getContext();
        if (context.getContextMap().containsKey(GradleProjectInfo.class, "")) {
            LOGGER.info("write project files to file.");
            GradleProjectInfo projectInfo = (GradleProjectInfo) context.getContextMap().get(GradleProjectInfo.class,
                    "");
            File projectInfoFile = new File(this.fixedResultPath, "ProjectInfo.json");
            writeJsonFile(projectInfoFile, gson.toJson(projectInfo.getProjectInfoMap()));
        }
        File globalSettingFile = new File(this.fixedResultPath, "XmsSetting.json");
        writeJsonFile(globalSettingFile, gson.toJson(GlobalSettings.toMap()));
    }

    private void writeJsonFile(File file, String jsonData) {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write(jsonData);
        } catch (IOException e) {
            LOGGER.error("An exception occurred during the processing:", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.error("An exception occurred during the processing:", e);
                }
            }
        }
    }
}
