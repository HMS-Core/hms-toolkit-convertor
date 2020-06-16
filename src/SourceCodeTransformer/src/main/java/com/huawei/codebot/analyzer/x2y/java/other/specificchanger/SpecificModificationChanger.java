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

package com.huawei.codebot.analyzer.x2y.java.other.specificchanger;

import static com.huawei.codebot.framework.FixStatus.NONEFIX;

import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.bean.GenericFunction;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.bean.JavaClass;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.bean.JavaClassCreationInstance;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.bean.JavaCodeAnalyzer;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.bean.JavaMethod;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A changer used to process some specific changes.
 *
 * @since 2020-04-20
 */
public class SpecificModificationChanger extends AndroidAppFixer {
    private static List<String> propertyFileNames = new ArrayList<>();

    private List<ReplaceData> replaceBuilderPatterns = new ArrayList<>();

    private Map<String, String> deleteUrlPatterns = new HashMap<>();

    private Map<String, String> deleteFilePatterns = new HashMap<>();

    private List<ReplaceData> replaceScopePatterns = new ArrayList<>();

    private List<ReplaceData> deleteScopePatterns = new ArrayList<>();

    public SpecificModificationChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        SpecificJsonPattern specificJsonPattern = configService.getSpecificJsonPattern();
        initConfig(specificJsonPattern);
    }

    /**
     * Do nothing for subclass.
     */
    protected SpecificModificationChanger() {
    }

    /**
     * Init all patterns of changer.
     *
     * @param specificJsonPattern An {@link SpecificJsonPattern} instance
     */
    protected void initConfig(SpecificJsonPattern specificJsonPattern) {
        if (specificJsonPattern != null) {
            replaceBuilderPatterns = specificJsonPattern.getReplaceBuilderPatterns();
            deleteUrlPatterns = specificJsonPattern.getDeleteUrlPatterns();
            deleteFilePatterns = specificJsonPattern.getDeleteFilePatterns();
            replaceScopePatterns = specificJsonPattern.getReplaceScopePatterns();
            deleteScopePatterns = specificJsonPattern.getDeleteScopePatterns();
        }
        this.basicFormatAfterFix = true;
    }

    private static void getPropertyFilePath(List<String> analyzedFilePaths) {
        for (String filePath : analyzedFilePaths) {
            if (filePath.endsWith(".properties")) {
                propertyFileNames.add(filePath);
            }
        }
    }

    private void detectUrlDefects(List<DefectInstance> defectInstances, String buggyFilePath) {
        try {
            List<String> fileContent =
                FileUtils.getOriginalFileLines(buggyFilePath, FileUtils.detectCharset(buggyFilePath));
            for (int i = 0; i < fileContent.size(); i++) {
                for (Entry<String, String> entry : deleteUrlPatterns.entrySet()) {
                    String url = entry.getKey();
                    String desc = entry.getValue();
                    if (fileContent.get(i).contains(url)) {
                        DefectInstance defectInstance =
                            createWarningDefectInstance(buggyFilePath, i + 1, fileContent.get(i), desc);
                        defectInstances.add(defectInstance);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTargetVariableValueInPropertyFile(String variable) {
        for (String propertyName : propertyFileNames) {
            try {
                List<String> fileContent =
                    FileUtils.getOriginalFileLines(propertyName, FileUtils.detectCharset(propertyName));
                for (String lineContent : fileContent) {
                    if (lineContent.contains(variable)) {
                        int index = lineContent.indexOf("=");
                        return lineContent.substring(index + 1).trim();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        if (propertyFileNames.size() == 0) {
            getPropertyFilePath(analyzedFilePaths);
        }
        List<DefectInstance> defectInstances = new ArrayList<>();
        JavaCodeAnalyzer codeAnalyzer = new JavaCodeAnalyzer();
        JavaClass javaFile = codeAnalyzer.extractJavaClassInfo(buggyFilePath);
        String packageName = javaFile.getPackageName();
        String className = javaFile.getClassName();
        Set<String> imports = javaFile.getImports();
        for (GenericFunction genericFunction : javaFile.getMethods()) {
            if (genericFunction instanceof JavaMethod) {
                JavaMethod javaMethods = (JavaMethod) genericFunction;
                for (JavaClassCreationInstance classInstance : javaMethods.getClassInstances()) {
                    String generatedQualifiedConstructorName =
                        getNameBasedOnImportStatement(imports, classInstance.getTypeName());
                    String qualifiedConstructorName = null;
                    if (generatedQualifiedConstructorName == null) {
                        generatedQualifiedConstructorName = packageName + "." + classInstance.getTypeName();
                        qualifiedConstructorName = classInstance.getTypeName();
                    }
                    generateDefectInstance(
                        defectInstances,
                        buggyFilePath,
                        generatedQualifiedConstructorName,
                        qualifiedConstructorName,
                        classInstance,
                        packageName,
                        className,
                        imports);
                }
            }
        }
        detectUrlDefects(defectInstances, buggyFilePath);
        return defectInstances;
    }

    @Override
    protected List<DefectInstance> detectDefectsInPropertiesFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        detectUrlDefects(defectInstances, buggyFilePath);
        return defectInstances;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJsonFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        try {
            String fileContent = FileUtils.getFileContent(buggyFilePath);
            for (Entry<String, String> entry : deleteFilePatterns.entrySet()) {
                String fileName = entry.getKey();
                String desc = entry.getValue();
                if (buggyFilePath.contains(fileName)) {
                    DefectInstance defectInstance = createWarningDefectInstance(buggyFilePath, 1, fileContent, desc);
                    defectInstances.add(defectInstance);
                }
            }
            return defectInstances;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getNameBasedOnImportStatement(Set<String> imports, String instance) {
        int index = instance.lastIndexOf(".");
        String subInstance;
        String lastSubInstance;
        for (String importName : imports) {
            if (index != -1) {
                subInstance = instance.substring(0, index);
                lastSubInstance = instance.substring(index + 1);
                if (importName.endsWith(subInstance)) {
                    return importName + "." + lastSubInstance;
                }
            } else {
                if (importName.endsWith(instance)) {
                    return importName;
                }
            }
        }
        return null;
    }

    private void generateDefectInstance(
        List<DefectInstance> defectInstances,
        String buggyFilePath,
        String calleeFullName,
        String qualifiedConstructorName,
        JavaClassCreationInstance classInstance,
        String packageName,
        String className,
        Set<String> imports) {
        scanReplaceBuilderPattern(
            defectInstances,
            buggyFilePath,
            calleeFullName,
            qualifiedConstructorName,
            classInstance,
            packageName,
            className,
            imports);

        scanRenameScopePattern(
            defectInstances,
            buggyFilePath,
            calleeFullName,
            qualifiedConstructorName,
            classInstance,
            packageName,
            className,
            imports);

        if (!deleteScopePatterns.isEmpty()
            && (calleeFullName.equals(deleteScopePatterns.get(0).name)
            || (qualifiedConstructorName != null
            && qualifiedConstructorName.equals(deleteScopePatterns.get(0).name)))) {
            List<String> args = classInstance.getArguments();
            if (args != null && args.size() == 1) {
                if (isScopeContainsIdentifier(packageName, className, args.get(0), imports)) {
                    DefectInstance defectInstance =
                        createWarningDefectInstance(
                            buggyFilePath,
                            classInstance.getStartLine(),
                            classInstance.getVariableDeclarationStatement(),
                            deleteScopePatterns.get(0).description);
                    defectInstances.add(defectInstance);
                }
            }
        }
    }

    private void scanRenameScopePattern(
        List<DefectInstance> defectInstances,
        String buggyFilePath,
        String calleeFullName,
        String qualifiedConstructorName,
        JavaClassCreationInstance classInstance,
        String packageName,
        String className,
        Set<String> imports) {
        for (ReplaceData replaceScopeData : replaceScopePatterns) {
            if (calleeFullName.equals(replaceScopeData.name)
                || (qualifiedConstructorName != null && qualifiedConstructorName.equals(replaceScopeData.name))) {
                List<String> args = classInstance.getArguments();
                if (args != null && args.size() == 1) {
                    if (args.get(0).equals(replaceScopeData.parameterContainsIdentifier)) {
                        addDefectInstance(defectInstances, buggyFilePath, classInstance, replaceScopeData, args);
                    } else {
                        String varInitValue = getConstantValue(packageName, className, imports, args.get(0));
                        if (varInitValue != null && varInitValue.equals(replaceScopeData.parameterContainsIdentifier)) {
                            addDefectInstance(defectInstances, buggyFilePath, classInstance, replaceScopeData, args);
                        }
                    }
                }
            }
        }
    }

    private void scanReplaceBuilderPattern(
        List<DefectInstance> defectInstances,
        String buggyFilePath,
        String calleeFullName,
        String qualifiedConstructorName,
        JavaClassCreationInstance classInstance,
        String packageName,
        String className,
        Set<String> imports) {
        for (ReplaceData replaceBuilderData : replaceBuilderPatterns) {
            if (calleeFullName.equals(replaceBuilderData.name)
                || (qualifiedConstructorName != null && qualifiedConstructorName.equals(replaceBuilderData.name))) {
                List<String> args = classInstance.getArguments();
                if (!args.isEmpty()) {
                    int index = args.get(0).indexOf("getProperty");
                    if (args.get(0).contains(replaceBuilderData.parameterContainsIdentifier)) {
                        addDefectInstance(defectInstances, buggyFilePath, classInstance, replaceBuilderData, args);
                    } else if (index != -1) {
                        String subParm = args.get(0).substring(index);
                        int left = subParm.indexOf("(");
                        int right = subParm.indexOf(")");
                        if (left != -1 && right != -1) {
                            String value = getTargetVariableValueInPropertyFile(subParm.substring(left + 2, right - 2));
                            if (value != null
                                && value.equals(replaceBuilderData.parameterContainsIdentifier.substring(1))) {
                                addDefectInstance(
                                    defectInstances, buggyFilePath, classInstance, replaceBuilderData, args);
                            }
                        }
                    } else {
                        String varInitValue = getConstantValue(packageName, className, imports, args.get(0));
                        if (varInitValue != null
                            && varInitValue.contains(replaceBuilderData.parameterContainsIdentifier)) {
                            addDefectInstance(defectInstances, buggyFilePath, classInstance, replaceBuilderData, args);
                        } else if (args.get(0).contains("+")) {
                            String[] parms = args.get(0).split("\\+");
                            StringBuilder allParmsValue = new StringBuilder();
                            for (String parm : parms) {
                                String parmVarValue =
                                    getConstantValue(packageName, className, imports, parm.replace(" ", ""));
                                if (parmVarValue != null) {
                                    allParmsValue.append(parmVarValue);
                                }
                            }
                            if (allParmsValue.toString().contains(replaceBuilderData.parameterContainsIdentifier)) {
                                addDefectInstance(
                                    defectInstances, buggyFilePath, classInstance, replaceBuilderData, args);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addDefectInstance(
        List<DefectInstance> defectInstances,
        String buggyFilePath,
        JavaClassCreationInstance classInstance,
        ReplaceData replaceBuilderData,
        List<String> args) {
        String fixedLine =
            classInstance.getVariableDeclarationStatement().replace(args.get(0), replaceBuilderData.newContent);
        DefectInstance defectInstance =
            createDefectInstance(
                buggyFilePath,
                classInstance.getStartLine(),
                classInstance.getVariableDeclarationStatement(),
                fixedLine);
        defectInstance.setMessage(replaceBuilderData.description);
        if (!classInstance.getFixFlag()) {
            defectInstance.setStatus(NONEFIX.toString());
            defectInstance.isFixed = false;
        }
        defectInstances.add(defectInstance);
    }

    private static String getConstantValue(String packageName, String className, Set<String> imports, String instance) {
        String variableFullName = getNameBasedOnImportStatement(imports, instance);
        Map<String, FieldInfo> fieldInfoMap = ClassMemberService.getInstance().getFieldInfoMap();
        if (variableFullName != null) {
            if (fieldInfoMap.get(variableFullName) != null) {
                return fieldInfoMap.get(variableFullName).getInitValue();
            }
        } else {
            if (fieldInfoMap.get(packageName + "." + instance) != null) {
                return fieldInfoMap.get(packageName + "." + instance).getInitValue();
            } else if (fieldInfoMap.get(packageName + "." + className + "." + instance) != null) {
                return fieldInfoMap.get(packageName + "." + className + "." + instance).getInitValue();
            }
        }
        return null;
    }

    private Boolean isScopeContainsIdentifier(
        String packageName, String className, String argument, Set<String> imports) {
        String varInitValue = getConstantValue(packageName, className, imports, argument);
        for (ReplaceData deleteScopeData : deleteScopePatterns) {
            if (argument.equals(deleteScopeData.parameterContainsIdentifier)
                || (varInitValue != null && varInitValue.equals(deleteScopeData.parameterContainsIdentifier))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    protected void mergeDuplicateFixedLines(List<DefectInstance> defectInstances) {
        removeDuplicateDefectInstance(defectInstances, replaceBuilderPatterns);
    }

    private static void removeDuplicateDefectInstance(
        List<DefectInstance> defectInstances, List<ReplaceData> replaceBuilderPatterns) {
        for (ReplaceData replaceBuilderData : replaceBuilderPatterns) {
            List<DefectInstance> deleteDefectInstances = new ArrayList<>();
            Map<String, String> tempMap = new HashMap<>();
            for (DefectInstance defectInstance : defectInstances) {
                Object fixedLineContent =
                    defectInstance.fixedLines.get(
                        defectInstance.mainBuggyFilePath, defectInstance.mainBuggyLineNumber);
                String desc = defectInstance.getMessage();
                int lineNumber = defectInstance.mainBuggyLineNumber;
                if (desc.equals(replaceBuilderData.description)) {
                    if (fixedLineContent != null && tempMap.get(lineNumber + desc) == null) {
                        tempMap.put(lineNumber + desc, desc);
                    } else if (fixedLineContent == null && tempMap.get(lineNumber + desc) != null) {
                        deleteDefectInstances.add(defectInstance);
                    }
                }
            }
            defectInstances.removeAll(deleteDefectInstances);
        }
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_SPECIFICMODIFICATION;
            info.description = "Google GMS should be changed to Huawei HMS";
            this.info = info;
        }
        return this.info;
    }
}
