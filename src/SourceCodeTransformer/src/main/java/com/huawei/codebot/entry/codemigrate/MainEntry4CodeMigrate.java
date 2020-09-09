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

import com.huawei.codebot.analyzer.x2y.global.AnalyzerHub;
import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.global.java.ClassMemberAnalyzer;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinClassMemberAnalyzer;
import com.huawei.codebot.analyzer.x2y.java.CompositeChangerFactory;
import com.huawei.codebot.framework.AnalyzerConst;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixBotArguments;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.api.CodeBotResult;
import com.huawei.codebot.framework.api.CodeBotResultCode;
import com.huawei.codebot.framework.dispatch.argparser.CodeMigrateOptions;
import com.huawei.codebot.framework.exception.CodeBotFileException;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.exception.CodeBotWarning;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Main entry of this program's process.
 * </p>
 * <br/>
 * <p>
 * Note that this process's output has two stage,
 *     <ol>
 *         <li>In stage 1, there is a intermediate output directory, usually named 'xxx_fixed'</li>
 *         <li>
 *             In stage 2, it will generate the final output directory which has three subdirectory,
 *             usually named 'fixbot', 'manual' and 'original'
 *         </li>
 *     </ol>
 * </p>
 *
 * @since 2020-04-01
 */
public class MainEntry4CodeMigrate {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainEntry4CodeMigrate.class);
    private static boolean NEED_GLOBAL_ANALYSIS = true;
    private static String[] extensions = AnalyzerConst.FOCUSED_FILE_EXTENSIONS_CODEMIGRATE;
    private static String[] defaultIgnoreList = AnalyzerConst.DEFAULT_IGNORELIST_CODEMIGRATE;

    public static void main(String[] args) {
        CodeBotResult result = process(args);
        if (result.getCode().equals(CodeBotResultCode.SUCCESS.code())) {
            LOGGER.info("finished successfully.");
        } else {
            LOGGER.error("error occurred.");
            LOGGER.error(result.getMessage(), result.getException());
        }
    }

    /**
     * Application's main process method
     *
     * @param args arguments of command line
     * @return result of process
     */
    public static CodeBotResult process(String[] args) {
        ArgParserImpl4CodeMigrate parser;
        try {
            parser = new ArgParserImpl4CodeMigrate(args);
        } catch (CodeBotRuntimeException ex) {
            return CodeBotResult.failure(ex);
        }
        FixBotArguments arguments = parser.getFixBotArguments();
        String repoId = arguments.getRepoId();

        resolveGlobalSettings(arguments);

        @SuppressWarnings("unchecked")
        List<String> repoPaths = (List<String>) parser.getParsedAttrs().get(CodeMigrateOptions.REPO_PATH.getOptLower());
        if (repoPaths == null) {
            return CodeBotResult.failure("please give repoPath value.");
        }

        List<String> analyzedFilePaths = new ArrayList<>();
        String[] ignores = ArrayUtils.addAll(defaultIgnoreList, arguments.getIgnoredList());
        for (String repoPath : repoPaths) {
            // handling scenes with spaces in the repoPath
            repoPath = FileUtils.getNormalizedPath(FileUtils.handleSpaceInDir(repoPath));
            analyzedFilePaths.addAll(FileUtils.listAllFiles(repoPath, extensions, ignores, ignores, 10485760));
        }

        if (CollectionUtils.isEmpty(analyzedFilePaths)) {
            return CodeBotResult.failure("can not get files in repoPath.");
        }

        if (NEED_GLOBAL_ANALYSIS) {
            try {
                AnalyzerHub analyzerHub = AnalyzerHub.getInstance();
                analyzerHub.addObserver(new ClassMemberAnalyzer());
                analyzerHub.addObserver(new KotlinClassMemberAnalyzer());
                boolean tryMultiThread = false;
                if (!tryMultiThread) {
                    analyzerHub.analyze(analyzedFilePaths);
                } else {
                    int threadNum = 10;
                    // number of files processed by each thread
                    int threadExeFileNum = 50;
                    analyzerHub.analyzeByMultiThread(analyzedFilePaths, threadNum, threadExeFileNum);
                }
                analyzerHub.postAnalyze();
            } catch (CodeBotRuntimeException e) {
                return CodeBotResult.failure("can not finish all files analyzation.");
            }
            NEED_GLOBAL_ANALYSIS = false;
        }

        try {
            if (repoPaths.size() == 1) {
                arguments.setRepoPath(repoPaths.get(0));
                launchFix(arguments);
            } else {
                for (int i = 0; i < repoPaths.size(); i++) {
                    arguments.setRepoPath(repoPaths.get(i));
                    arguments.setRepoId(repoId + "." + i);
                    launchFix(arguments);
                }
            }
        } catch (CodeBotWarning ex) {
            LOGGER.error(ex.getErrorCode().message(), ex);
            return CodeBotResult.successWithError(ex);
        } catch (CodeBotRuntimeException ex) {
            LOGGER.error(ex.getErrorCode().message(), ex);
            return CodeBotResult.failure(ex);
        }
        return CodeBotResult.success();
    }

    private static void resolveGlobalSettings(FixBotArguments arguments) {
        if (arguments == null) {
            return;
        }
        String optionStr = arguments.getCaller();
        if (optionStr != null) {
            List<String> options = Arrays.asList(optionStr.split("#"));
            if (options.contains("SDK")) {
                GlobalSettings.setIsSDK(true);
            }
            if (options.contains("G")) {
                GlobalSettings.setIsOnlyG(true);
            }
            if (options.contains("H")) {
                GlobalSettings.setIsOnlyH(true);
            }
        }

        if ("wisehub".equals(arguments.getRuleSet())) {
            GlobalSettings.setIsWiseHub(true);
        }
    }
    private static void launchFix(FixBotArguments arguments) throws CodeBotRuntimeException {
        DefectFixerType fixerType = DefectFixerType.fromValue(arguments.getRuleSet());
        GenericDefectFixer fixer = CompositeChangerFactory.newAutoChanger(fixerType, arguments.isOnlyCheck());

        // detect and fix
        fixer.fixPatternFolder = Paths.get(arguments.getFixedFilePath(), "fixpatterns").toString();
        fixer.preprocessAndAutoFix(arguments);

        // FixBot-cli related operations
        CodeMigrateEntry entry = new CodeMigrateEntry(arguments);
        GenericDefectFixer c2cChanger = entry.doProcessing(arguments, fixer, null);

        for (DefectInstance defectInstance : c2cChanger.defectInstances) {
            if (defectInstance.buggyLines != null
                    && !defectInstance.buggyLines.isEmpty()
                    && defectInstance.fixedLines != null
                    && !defectInstance.fixedLines.isEmpty()
                    && defectInstance.isFixed) {
                for (Object object : defectInstance.buggyLines.entrySet()) {
                    if (object instanceof MultiKeyMap.Entry) {
                        MultiKeyMap.Entry en = (MultiKeyMap.Entry) object;
                        if (en.getKey() instanceof MultiKey) {
                            MultiKey key = (MultiKey) en.getKey();
                            String filePath = (String) key.getKey(0);
                            Integer startLine = (Integer) key.getKey(1);
                            String value = (String) en.getValue();
                            if (checkFileExtension(filePath) && value != null) {
                                try {
                                    String fileContent = FileUtils.getFileContent(filePath);
                                    List<String> fileLines = StringUtil.getLines(fileContent);
                                    String lineBreak = StringUtil.getLineBreak(fileContent);
                                    String[] lines = value.split(lineBreak);
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = startLine - 1; i < startLine - 1 + lines.length; i++) {
                                        sb.append("// [Modified By HMSConvertor] ")
                                                .append(fileLines.get(i))
                                                .append(lineBreak);
                                    }
                                    String fixedLine = (String) defectInstance.fixedLines.get(filePath, startLine);
                                    sb.append(fixedLine);
                                    defectInstance.fixedLines.put(filePath, startLine, sb.toString());
                                } catch (IOException e) {
                                    throw new CodeBotFileException("failed to getFileContent: " + filePath, key, e);
                                }
                            }
                        }
                    }
                }
            }
        }

        c2cChanger.generateFixesForSingleProject(c2cChanger.defectInstances);
        String suffix = "-comment";
        // FixBot-cli related operations
        arguments.setRepoId(arguments.getRepoId() + suffix);
        arguments.setFixedFilePath(arguments.getFixedFilePath() + suffix);
        CodeMigrateEntry entry1 = new CodeMigrateEntry(arguments);
        entry1.doProcessing(arguments, fixer, c2cChanger);
    }

    private static boolean checkFileExtension(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return false;
        }
        for (String extension : extensions) {
            if (filePath.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
