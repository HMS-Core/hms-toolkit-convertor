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

package com.huawei.codebot.analyzer.x2y.java.other.complexchanger;

import static com.huawei.codebot.analyzer.x2y.xml.XmlModificationChanger.MANIFEST_PATH;

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.GradleModificationChanger;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.SpecificModificationChanger;
import com.huawei.codebot.analyzer.x2y.xml.XmlModificationChanger;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixBotArguments;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.exception.CodeBotFileException;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.lazyfix.SyncedCompositeDefectFixer;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A changer used to process Complex Startup Activity
 *
 * @since 2020-04-17
 */
public class ComplexStartupActivityChanger extends SyncedCompositeDefectFixer {
    /**
     * initialize CODE_PATH
     */
    public static final String CODE_PATH = "src" + File.separator + "main" + File.separator + "java";

    /**
     * initialize detectors
     */
    protected ArrayList<GenericDefectFixer> detectors = new ArrayList<GenericDefectFixer>();

    /**
     * initialize projectFiles
     */
    protected Map<String, ProjectFile> projectFiles = new HashMap<String, ProjectFile>();

    private String fixerType;

    public ComplexStartupActivityChanger(String fixerType) {
        this.fixerType = fixerType;
    }

    @Override
    public void preprocessAndAutoFix(FixBotArguments args) throws CodeBotRuntimeException {
        String subjectProjectOrModuleCodeFolder = args.getRepoPath();
        this.basicFormatAfterFix = true;
        // Initialize the project path
        initProjectFolders(subjectProjectOrModuleCodeFolder);
        // Project Module analysis, generate xml, gradle, corresponding code path of each Module
        analyzeProjectAndGenerateDetectors(subjectProjectOrModuleCodeFolder);
        // Project Module analysis, generate xml, gradle, corresponding code path of each Module
        this.initializeDetectors();
        // Initialize all fixers
        this.initializeAtomicFixers();

        for (Map.Entry<String, ProjectFile> fileEntry : projectFiles.entrySet()) {
            for (GenericDefectFixer detector : detectors) {
                // Decide on repair path according to Changer
                if (detector instanceof GradleModificationChanger) {
                    generateDefectInstance(stringToList(fileEntry.getValue().getGradlePath()), detector);
                } else if (detector instanceof XmlModificationChanger) {
                    generateDefectInstance(stringToList(fileEntry.getValue().getXmlPath()), detector);
                } else {
                    if (detector instanceof SpecificModificationChanger) {
                        generateDefectInstance(fileEntry.getValue().getCodeFilePaths(), detector);
                    }
                }
            }
            if (detectWarnings()) {
                List<String> tempFixFilePaths = getTargetCodePath(getContextInfo(),
                    fileEntry.getValue().getCodeFilePaths());
                for (GenericDefectFixer fixer : atomicFixers) {
                    generateDefectInstance(tempFixFilePaths, fixer);
                    this.defectInstances.addAll(fixer.defectInstances);
                }
            }
        }
    }

    /**
     * initialize Detectors
     *
     * @throws CodeBotRuntimeException
     */
    private void initializeDetectors() throws CodeBotRuntimeException {
        this.detectors.add(new ComplexGradleModificationChanger(fixerType));
        this.detectors.add(new ComplexXmlModificationChanger(fixerType));
    }

    private void generateDefectInstance(List<String> analyzedFilePaths, GenericDefectFixer atomicFixer)
            throws CodeBotRuntimeException {
        atomicFixer.analyzedFilePaths = analyzedFilePaths;
        atomicFixer.defectInstances.clear();

        // Detecting defects
        List<DefectInstance> totalDefectInstances = new ArrayList<>();
        for (String filePath : atomicFixer.analyzedFilePaths) {
            List<DefectInstance> defectWarningsInSingleFile = atomicFixer.detectDefectsForSingleFile(filePath);
            if (defectWarningsInSingleFile == null || defectWarningsInSingleFile.size() == 0) {
                continue;
            }
            // Filtering alerts that do not meet requirements
            defectWarningsInSingleFile =
                    defectWarningsInSingleFile.stream()
                            .filter(this::verifyWarning)
                            .collect(Collectors.toList());

            totalDefectInstances.addAll(defectWarningsInSingleFile);
            // Update the number of detected defects
            atomicFixer.totalDetectedDefectNumber += defectWarningsInSingleFile.size();
            atomicFixer.analyzedFileNum.getAndIncrement();
            atomicFixer.defectInstances.addAll(totalDefectInstances);
        }
    }

    /**
     * split string to list by ,
     */
    private List<String> stringToList(String strs) {
        String[] str = strs.split(",");
        return Arrays.asList(str);
    }

    @Override
    protected void initializeAtomicFixers() throws CodeBotRuntimeException {
        this.atomicFixers.add(new ComplexSpecificModificationChanger(fixerType));
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_COMPLEXMODIFICATION;
            info.description = "g2h_complex_scene";
            this.info = info;
        }
        return this.info;
    }

    /**
     * analyze project and generate detectors
     *
     * @throws CodeBotRuntimeException
     */
    private void analyzeProjectAndGenerateDetectors(String codeFolder) throws CodeBotRuntimeException {
        List<String> analyzedFilePaths = new ArrayList<>();
        getTargetFilePaths(codeFolder, analyzedFilePaths);
        processFile(codeFolder, analyzedFilePaths);
    }

    /**
     * process files
     */
    private void processFile(String codeNetProjectCodeFolder, List<String> analyzedFilePaths) {
        List<String> gradleFilePaths = new ArrayList<String>();
        List<String> manifestXmlFilePaths = new ArrayList<String>();
        List<String> javaFilePaths = new ArrayList<String>();
        for (String path : analyzedFilePaths) {
            if (path.endsWith("build.gradle") && isAppGradleFile(path)) {
                gradleFilePaths.add(path);
            } else if (path.endsWith("AndroidManifest.xml")) {
                manifestXmlFilePaths.add(path);
            } else {
                if (path.endsWith(".java")) {
                    javaFilePaths.add(path);
                }
            }
        }
        for (String path : gradleFilePaths) {
            String modulePath = path.replace(File.separator + "build.gradle", "");
            if (!modulePath.equals(codeNetProjectCodeFolder)) {
                ProjectFile projectFile = new ProjectFile();
                projectFile.setProjectPath(codeNetProjectCodeFolder);
                projectFile.setModulePath(modulePath);
                projectFile.setGradlePath(path);
                projectFile.setXmlPath(getXmlPathInModule(manifestXmlFilePaths, modulePath));
                projectFile.setCodePaths(getCodePathInModule(javaFilePaths, modulePath));
                projectFile.setCodeFilePaths(getCodeFilePathInModule(javaFilePaths, projectFile.getCodePaths()));
                if (projectFile.getXmlPath() == null || projectFile.getCodePaths() == null) {
                    continue;
                }
                projectFiles.put(modulePath, projectFile);
            }
        }
    }

    /**
     * get target file paths
     *
     * @throws CodeBotFileException
     */
    private void getTargetFilePaths(String path, List<String> filePaths) throws CodeBotFileException {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            for (File file2 : files) {
                String canonicalPath = "";
                try {
                    canonicalPath = file2.getCanonicalPath();
                } catch (IOException ex) {
                    throw new CodeBotFileException("error in getCanonicalPath of file : " + path, null, ex);
                }
                if (file2.isFile()
                        && ((canonicalPath.endsWith(".java"))
                        || (canonicalPath.endsWith("build.gradle"))
                        || (canonicalPath.endsWith("AndroidManifest.xml")))
                        && !(canonicalPath.contains("build" + File.separator + "generated"))
                        && !(canonicalPath.contains("build" + File.separator + "intermediates"))) {
                    filePaths.add(canonicalPath);
                } else {
                    if (file2.isDirectory()) {
                        getTargetFilePaths(file2.getPath(), filePaths);
                    }
                }
            }
        }
    }

    /**
     * judge buildgradle is App file
     */
    public static Boolean isAppGradleFile(String path) {
        try {
            String content = FileUtils.getFileContent(path);
            if (content.contains("defaultConfig")
                    && content.contains("minSdkVersion")
                    && content.contains("targetSdkVersion")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * get Xml path in module
     */
    public static String getXmlPathInModule(List<String> manifestXmlFilePaths, String modulePath) {
        for (String filePath : manifestXmlFilePaths) {
            if (filePath.startsWith(modulePath) && filePath.endsWith(MANIFEST_PATH)) {
                return filePath;
            }
        }
        return null;
    }

    /**
     * get codes path in modules
     */
    public static String getCodePathInModule(List<String> javaFilePaths, String modulePath) {
        for (String filePath : javaFilePaths) {
            if (filePath.startsWith(modulePath) && filePath.contains(CODE_PATH)) {
                int index = filePath.indexOf(CODE_PATH) + CODE_PATH.length();
                String codePath = filePath.substring(0, index);
                return codePath;
            }
        }
        return null;
    }

    /**
     * get code file path in modules
     */
    public static List<String> getCodeFilePathInModule(List<String> javaFilePaths, String codePath) {
        if (codePath == null) {
            return null;
        }
        List<String> codeFilePaths = new ArrayList<String>();
        for (String filePath : javaFilePaths) {
            if (filePath.startsWith(codePath)) {
                codeFilePaths.add(filePath);
            }
        }
        return codeFilePaths;
    }

    /**
     * detect warnings
     */
    public Boolean detectWarnings() {
        for (GenericDefectFixer detector : detectors) {
            if (!isDetectionObject(detector.defectInstances)) {
                return false;
            }
        }
        return true;
    }

    /**
     * get context info
     */
    public List<String> getContextInfo() {
        List<String> targetPaths = new ArrayList<String>();
        for (GenericDefectFixer detector : detectors) {
            if (detector instanceof ComplexXmlModificationChanger) {
                for (DefectInstance defectInstance : detector.defectInstances) {
                    String className = String.valueOf(defectInstance.context.get("Activity").get(0));
                    String[] classSplitName = className.split("\\.");
                    StringBuilder classPath = new StringBuilder();
                    for (int i = 0; i < classSplitName.length; i++) {
                        if (i != classSplitName.length - 1) {
                            classPath.append(classSplitName[i]).append(File.separator);
                        } else {
                            classPath.append(classSplitName[i]);
                        }
                    }
                    targetPaths.add(classPath.toString());
                }
            }
        }
        return targetPaths;
    }

    /**
     * get target code path
     */
    public static List<String> getTargetCodePath(List<String> activityNames, List<String> javaCodeFilePaths) {
        List<String> activityPaths = new ArrayList<String>();
        for (String activityName : activityNames) {
            for (String codeFilePath : javaCodeFilePaths) {
                if (codeFilePath.contains(activityName)) {
                    activityPaths.add(codeFilePath);
                }
            }
        }
        return activityPaths;
    }

    /**
     * judge is detection object
     */
    public static Boolean isDetectionObject(List<DefectInstance> defectInstances) {
        if (defectInstances == null || defectInstances.size() == 0) {
            return false;
        }
        for (DefectInstance defectInstance : defectInstances) {
            if (!defectInstance.context.get("Complex").get(0).equals("complex")) {
                return false;
            }
        }
        return true;
    }
}
