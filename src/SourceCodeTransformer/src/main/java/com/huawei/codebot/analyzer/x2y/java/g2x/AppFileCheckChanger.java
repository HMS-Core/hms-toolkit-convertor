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

package com.huawei.codebot.analyzer.x2y.java.g2x;

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixBotArguments;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.exception.CodeBotFileException;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.lazyfix.SyncedCompositeDefectFixer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A changer to check whether is Application class and whether there need classloader pattern
 *
 * @since 2020-07-08
 */
public class AppFileCheckChanger extends SyncedCompositeDefectFixer {

    public AppFileCheckChanger(String fixerType) {

    }

    @Override
    public void preprocessAndAutoFix(FixBotArguments args) throws CodeBotRuntimeException {
        String subjectProjectOrModuleCodeFolder = args.getRepoPath();
        this.initProjectFolders(subjectProjectOrModuleCodeFolder);
        this.analyzedFilePaths = this.analyzeProjectAndGenerateDetectors(subjectProjectOrModuleCodeFolder);
        this.toAnalyzeFileNum = this.analyzedFilePaths.size();
        this.initializeAtomicFixers();
        checkClass(atomicFixers.get(0), ApplicationClassUtils.FILE_SUFFIX);
        if (!GlobalSettings.isHasApplication()) {
            checkClass(atomicFixers.get(1), ApplicationClassUtils.GRADLE_SUFFIX);
        } else {
            KeyClassCheckChanger changer = (KeyClassCheckChanger) atomicFixers.get(0);
            MethodModel methodModel = changer.getMethodModel();
            if (methodModel.isJavaFile()) {
                MethodUtils.createOnCreateMethodInJava(methodModel.getUnit(), methodModel.getTypeDeclaration(),
                        methodModel.getBuggyFilePath(), this);
            } else {
                MethodUtils.crateOnCreateMethodInKotlin(methodModel.getClassDeclarationContext(),
                        methodModel.getBuggyFilePath(), this);
            }
        }
    }

    private List<String> analyzeProjectAndGenerateDetectors(String codeFolder)
            throws CodeBotRuntimeException {
        List<String> analyzedFilePaths = new ArrayList<String>();
        getTargetFilePaths(codeFolder, analyzedFilePaths);
        return analyzedFilePaths;
    }

    private void getTargetFilePaths(String path, List<String> filePaths) throws CodeBotFileException {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }

        for (File file2 : files) {
            String canonicalPath = "";
            try {
                canonicalPath = file2.getCanonicalPath();
            } catch (IOException ex) {
                throw new CodeBotFileException("error in getCanonicalPath of file : " + path, null, ex);
            }
            if (file2.isFile()
                    && ((canonicalPath.endsWith(ApplicationClassUtils.JAVA_SUFFIX))
                    || ((canonicalPath.endsWith(ApplicationClassUtils.KOTLIN_SUFFIX)))
                    || (canonicalPath.endsWith(ApplicationClassUtils.BUILD_GRADLE))
                    || (canonicalPath.endsWith(ApplicationClassUtils.ANDROIDMANIFEST)))) {
                filePaths.add(canonicalPath);
            } else {
                if (file2.isDirectory()) {
                    getTargetFilePaths(file2.getPath(), filePaths);
                }
            }
        }
    }

    private void checkClass(GenericDefectFixer atomicFixer, String suffix) throws CodeBotRuntimeException {
        for (String filePath : this.analyzedFilePaths) {
            if (!GlobalSettings.isNeedClassloader() || !GlobalSettings.isHasApplication()) {
                if (suffix.equals(ApplicationClassUtils.FILE_SUFFIX) || filePath.endsWith(suffix)) {
                    atomicFixer.detectDefectsForSingleFile(filePath);
                }
            }
        }
    }

    @Override
    protected void initializeAtomicFixers() {
        this.atomicFixers.add(new KeyClassCheckChanger());
        AppFileModifyChanger appFileModifyCheckChanger = new AppFileModifyChanger();
        appFileModifyCheckChanger.fixPatternFolder = this.fixPatternFolder;
        appFileModifyCheckChanger.currentFixedProjectFolder =  this.currentFixedProjectFolder;
        this.atomicFixers.add(appFileModifyCheckChanger);
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.WISEHUB;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }
}
