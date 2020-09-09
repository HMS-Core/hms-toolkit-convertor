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

package com.huawei.codebot.analyzer.x2y.global;

import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.MethodInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.framework.api.CodeBotResultCode;
import com.huawei.codebot.framework.exception.CodeBotFileException;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.parser.kotlin.KotlinLexer;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.huawei.codebot.utils.FileUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A hub embedding one or more analyzer to analyze a list of files specified by file paths
 *
 * @since 2019-07-14
 */
public class AnalyzerHub extends Observable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerHub.class);
    private static AnalyzerHub analyzerHub;
    private int javaFileNum = 0;
    private int javaTotalLineNum = 0;
    private int kotlinFileNum = 0;
    private int kotlinTotalLineNum = 0;

    private AnalyzerHub() throws CodeBotRuntimeException {
        super();
        Map<String, ClassInfo> classInfoMap = ClassMemberService.getInstance().getClassInfoMap();
        Map<String, FieldInfo> fieldInfoMap = ClassMemberService.getInstance().getFieldInfoMap();
        Map<String, List<MethodInfo>> methodInfoMap = ClassMemberService.getInstance().getMethodInfoMap();
        Map<String, List<Object>> offlineRecord;
        List<String> apisList = new ArrayList<>();
        apisList.add("android.api");
        ClassLoader classloader = AnalyzerHub.class.getClassLoader();
        File filename = Paths.get(System.getProperty("user.codemigrate.dir"), File.separator, "dependency.txt")
                .toFile();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
            String line;
            while (StringUtils.isNotEmpty(line = br.readLine())) {
                apisList.add(line.trim());
            }
        } catch (IOException e) {
            LOGGER.info("dependency txt is not found! We should analyze project dependency version first.");
        }
        for (String apiPath : apisList) {
            apiPath = "data/" + apiPath;
            try (InputStream is = classloader.getResourceAsStream(apiPath)) {
                try (SecureObjectInputStream in = new SecureObjectInputStream(is)) {
                    offlineRecord = (Map<String, List<Object>>) in.readObject();
                }
            } catch (IOException | ClassNotFoundException e1) {
                throw new CodeBotRuntimeException(CodeBotResultCode.FILE_OPERATION,
                        "fail to read android.api, the path is " + apiPath, null, e1);
            }
            if (offlineRecord == null) {
                continue;
            }
            List<Object> classInfos = offlineRecord.get("ClassInfo");
            if (classInfos != null) {
                for (Object object : classInfos) {
                    ClassInfo classInfo = (ClassInfo) object;
                    classInfoMap.put(classInfo.getQualifiedName(), classInfo);
                }
                LOGGER.info("{} classInfos have been loaded.", classInfos.size());
            }
            List<Object> fieldInfos = offlineRecord.get("FieldInfo");
            if (fieldInfos != null) {
                for (Object object : fieldInfos) {
                    FieldInfo fieldInfo = (FieldInfo) object;
                    fieldInfoMap.put(fieldInfo.getQualifiedName(), fieldInfo);
                }
                LOGGER.info("{} fieldInfos have been loaded.", fieldInfos.size());
            }
            List<Object> methodInfos = offlineRecord.get("MethodInfo");
            if (methodInfos == null) {
                continue;
            }
            for (Object object : methodInfos) {
                MethodInfo methodInfo = (MethodInfo) object;
                if (!methodInfoMap.containsKey(methodInfo.getQualifiedName())) {
                    methodInfoMap.put(methodInfo.getQualifiedName(), new ArrayList<>());
                }
                methodInfoMap.get(methodInfo.getQualifiedName()).add(methodInfo);
            }
            LOGGER.info("{} methodInfos have been loaded.", methodInfos.size());
        }
    }

    /**
     * to read the AnalyzerHub
     *
     * @return instance of AnalyzerHub
     * @throws CodeBotRuntimeException code bot runtime exception
     */
    public static synchronized AnalyzerHub getInstance() throws CodeBotRuntimeException {
        if (analyzerHub == null) {
            analyzerHub = new AnalyzerHub();
        }
        return analyzerHub;
    }

    /**
     * @param filePaths paths of files which need to be analyzed
     * @throws CodeBotFileException code bot file exception
     */
    public void analyze(List<String> filePaths) throws CodeBotFileException {
        if (CollectionUtils.isEmpty(filePaths)) {
            return;
        }
        for (String filePath : filePaths) {
            String codeContent;
            try {
                codeContent = FileUtils.getFileContent(filePath);
            } catch (IOException ex) {
                throw new CodeBotFileException("can not get file content of " + filePath, null, ex);
            }
            List<String> fileLines = FileUtils.cutStringToList(codeContent);
            if (filePath.endsWith(".java")) {
                javaTotalLineNum += fileLines.size();
                final CompilationUnit cu = JavaFileAnalyzer.generateAST(codeContent);
                UniqueVisitor visitor = new UniqueVisitor();
                cu.accept(visitor);
                javaFileNum++;
            } else if (filePath.endsWith(".kt")) {
                kotlinTotalLineNum += fileLines.size();
                KotlinLexer kotlinLexer = new KotlinLexer(CharStreams.fromString(codeContent));
                CommonTokenStream commonTokenStream = new CommonTokenStream(kotlinLexer);
                KotlinParser kotlinParser = new KotlinParser(commonTokenStream);
                KotlinParser.KotlinFileContext tree = kotlinParser.kotlinFile();
                GlobalKotlinVisitor visitor = new GlobalKotlinVisitor();
                tree.accept(visitor);
                kotlinFileNum++;
            }
        }
    }

    /**
     * analyzeByMultiThread
     *
     * @param filePaths        paths of files which need to be analyzed
     * @param threadNum        number of threads to launch parallelism
     * @param threadExeFileNum files in each group
     */
    public void analyzeByMultiThread(List<String> filePaths, int threadNum, int threadExeFileNum) {
        if (CollectionUtils.isEmpty(filePaths)) {
            return;
        }
        // 2 dimensions to 3 dimensions
        List<List<String>> spliceList = spliceListByStep(filePaths, threadExeFileNum);
        List<Callable<Boolean>> threadList = new ArrayList<>();
        for (List<String> fileList : spliceList) {
            threadList.add(() -> {
                // analyze each group of files
                analyze(fileList);
                return true;
            });
        }

        LOGGER.info("execute thread amount is {}", threadList.size());

        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        int normalFinish = 0;
        try {
            List<Future<Boolean>> futures = executorService.invokeAll(threadList);
            for (Future<Boolean> future : futures) {
                try {
                    future.get();
                    normalFinish++;
                } catch (ExecutionException e) {
                    LOGGER.error("execute exception", e);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("execute invoke exception", e);
        } finally {
            executorService.shutdown();
        }
        long end = System.currentTimeMillis();
        LOGGER.info("execute thread end, normal finished size is {}, cost time {}s",
                normalFinish, ((end - start) * 1.0 / 1000));
    }

    private <T> List<List<T>> spliceListByStep(List<T> fileList, int stepLen) {
        List<List<T>> fileListLs = new ArrayList<>();

        if (CollectionUtils.isEmpty(fileList)) {
            return fileListLs;
        }
        if (stepLen <= 0) {
            fileListLs.add(fileList);
            return fileListLs;
        }

        int step = 0;
        while (true) {
            int currIndex = step * stepLen;
            int nextIndex = (step + 1) * stepLen;

            if (nextIndex >= fileList.size()) {
                fileListLs.add(fileList.subList(currIndex, fileList.size()));
                break;
            }
            fileListLs.add(fileList.subList(currIndex, nextIndex));
            step++;
        }
        return fileListLs;
    }

    /**
     * Notice Analyzers' postAnalyze method and log some data.
     */
    public void postAnalyze() {
        setChanged();
        notifyObservers();
        LOGGER.info("Total java files: {}", javaFileNum);
        LOGGER.info("Total java lines: {}", javaTotalLineNum);
        LOGGER.info("Total kotlin files: {}", kotlinFileNum);
        LOGGER.info("Total kotlin lines: {}", kotlinTotalLineNum);
    }

    private class GlobalKotlinVisitor extends KotlinLocalVariablesVisitor {
        @Override
        public Boolean visitClassDeclaration(KotlinParser.ClassDeclarationContext ctx) {
            super.visitClassDeclaration(ctx);
            Context<ParserRuleContext> context = new Context<>(kotlinTypeInferencer, ctx);
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(context);
            return true;
        }

    }

    private class UniqueVisitor extends JavaLocalVariablesInMethodVisitor {
        @Override
        public boolean visit(TypeDeclaration node) {
            boolean continueVisit = super.visit(node);
            Context<ASTNode> context = new Context<>(javaTypeInferencer, node);
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(context);
            return continueVisit;
        }
    }

    static class Context<T> {
        TypeInferencer typeInferencer;
        T node;

        Context(TypeInferencer typeInferencer, T node) {
            this.typeInferencer = typeInferencer;
            this.node = node;
        }
    }
}
