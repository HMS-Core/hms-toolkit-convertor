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

package com.huawei.codebot.analyzer.x2y.java.field.access;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.gradle.utils.GradleFileUtils;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.RenameBaseChanger;
import com.huawei.codebot.analyzer.x2y.java.field.access.codegen.Mapping;
import com.huawei.codebot.analyzer.x2y.java.field.access.codegen.SwitchCaseAdapter;
import com.huawei.codebot.analyzer.x2y.java.field.access.codegen.SwitchCaseNode;
import com.huawei.codebot.analyzer.x2y.java.visitor.JavaRenameBaseVisitor;
import com.huawei.codebot.analyzer.x2y.java.visitor.KotlinRenameBaseVisitor;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotFileException;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.huawei.codebot.analyzer.x2y.java.field.access.codegen.SwitchStatementInfo;
import com.huawei.codebot.analyzer.x2y.java.field.access.codegen.SwitchCaseInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.framework.utils.JsonUtil;
import com.huawei.codebot.utils.FileUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * field access changer uesd in G2X
 *
 * @since 2020-04-16
 */
public class FieldAccessChanger extends RenameBaseChanger {

    private static final Logger logger = LoggerFactory.getLogger(FieldAccessChanger.class);

    private static final String ENUM_FIEL_PATH = "src" + File.separator + "main" +File.separator + "java";

    private static final String DEFAULT_MODULE_NAME = "default";
    /**
     * Find the specific replacement for the smallest field in the configuration file
     */
    private Map<String, String> simplifiedRenamePattern;
    private Map<String, String> simpleG2HRenamePattern = new HashMap<>();
    private Map<String, Map> simpleG2HDescriptions = new HashMap<>();

    private Set<String> startPackage = new HashSet<>();
    private Set<String> movedFile = new HashSet<>();
    private Map<SwitchStatementInfo, Set<SwitchCaseInfo>> switchFunctionInfos = new HashMap<>();
    private Map<JavaFile, Set<SwitchStatement>> switchInfos = new HashMap<>();
    private Map<SwitchStatement, SwitchStatementInfo> switchStatements = new HashMap<>();
    private Map<String, Integer> filePathName = new HashMap<>();

    public FieldAccessChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.renamePatterns = configService.getFieldRenamePattern();
        this.fullName2Description = configService.getFieldRenameDescriptions();
        this.simplifiedRenamePattern = new HashMap<>();
        for (Map.Entry<String, String> entry : this.renamePatterns.entrySet()) {
            String[] key = entry.getKey().split("\\.");
            String[] value = entry.getValue().split("\\.");
            if (key.length > 0 && value.length > 0) {
                this.simplifiedRenamePattern.put(key[key.length - 1], value[value.length - 1]);
            }
        }
        getSimpleG2HRenamePattern();
    }

    @Override
    protected List<DefectInstance> detectDefectsForSingleProject() throws CodeBotFileException {
        List<DefectInstance> defectInstances = super.detectDefectsForSingleProject();
        processSwitchFunctions();
        processSwitchNodes(defectInstances);
        return defectInstances;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaRenameBaseVisitor visitor = new FieldAccessVisitor(javaFile, this);

        javaFile.compilationUnit.accept(visitor);
        List<DefectInstance> defectInstanceList =
                generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        removeIgnoreBlocks(defectInstanceList, javaFile.shielder);
        return defectInstanceList;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        if (StringUtils.isEmpty(buggyFilePath)) {
            return null;
        }
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinRenameBaseVisitor visitor =
                new KotlinRenameBaseVisitor(kotlinFile, this) {
                    private KotlinTypeInferencer inferencer = new KotlinTypeInferencer(this);

                    @Override
                    public Boolean visitPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        if (KotlinASTUtils.isFieldAccess(ctx)) {
                            TypeInfo typeInfo =
                                    inferencer.getQualifierType(ctx.primaryExpression(), ctx.postfixUnarySuffix());
                            if (typeInfo != null) {
                                String fieldFullNameOrigin =
                                        typeInfo.getQualifiedName()
                                                + "."
                                                + ctx.postfixUnarySuffix(ctx.postfixUnarySuffix().size() - 1)
                                                .navigationSuffix()
                                                .simpleIdentifier()
                                                .getText();
                                if (renamePatterns.containsKey(fieldFullNameOrigin)) {
                                    int startLineNumber = ctx.getStart().getLine();
                                    int endLineNumber = ctx.getStop().getLine();
                                    String buggyLine =
                                            String.join(
                                                    kotlinFile.lineBreak,
                                                    kotlinFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                                    DefectInstance defectInstance =
                                            createDefectInstance(buggyFilePath, startLineNumber, buggyLine, buggyLine);
                                    Map desc = fullName2Description.get(fieldFullNameOrigin);
                                    defectInstance.setMessage(desc == null ? null : gson.toJson(desc));
                                    defectInstance.isFixed = false;
                                    defectInstance.status = FixStatus.NONEFIX.toString();
                                    defectInstances.add(defectInstance);
                                }
                            }
                        }
                        return super.visitPostfixUnaryExpression(ctx);
                    }
                };
        try {
            kotlinFile.tree.accept(visitor);
        } catch (Exception e) {
            logger.error(buggyFilePath);
            logger.error(Arrays.toString(e.getStackTrace()));
        }
        List<DefectInstance> defectInstanceList =
                generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        removeIgnoreBlocks(defectInstanceList, kotlinFile.shielder);
        return defectInstanceList;
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_FIELDRENAME;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }

    private void getSimpleG2HRenamePattern() {
        Map<String, String> changePatterns = new HashMap<>();
        String jsonStr = "";
        try {
            jsonStr = FileUtils.getFileContent(".\\config\\wisehub-auto-hms.json");
        } catch (IOException e) {
            logger.error("Failed to parse json file");
        }
        JSONObject json = new JSONObject(jsonStr);
        JSONArray renamedFields = (JSONArray) json.get("autoFields");
        for (int i = 0; i < renamedFields.length(); i++) {
            JSONObject renamedField = (JSONObject) renamedFields.get(i);
            String oldFieldName = renamedField.getString("oldFieldName");
            String newFieldName = renamedField.getString("newFieldName");
            changePatterns.put(oldFieldName, newFieldName);
        }

        Map<String, Map> descriptions = new HashMap<>();
        JSONArray renamedClasses = (JSONArray) json.get("autoFields");
        for (int i = 0; i < renamedClasses.length(); i++) {
            JSONObject renamedClass = (JSONObject) renamedClasses.get(i);
            String oldClassName = renamedClass.getString("oldFieldName");
            Map desc = JsonUtil.toMap(renamedClass.getJSONObject("desc"));
            descriptions.put(oldClassName, desc);
        }

        this.simpleG2HRenamePattern = changePatterns;
        this.simpleG2HDescriptions = descriptions;
    }

    private void processPackageNode(JavaFile javaFile, SwitchStatement recordSwitchStatement, List<DefectInstance> defectInstances) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Object root = recordSwitchStatement.getRoot();
        if (root instanceof CompilationUnit) {
            int startPackageLineNumber = 0;
            String buggyLineContent = "";
            CompilationUnit compilationUnitRoot = (CompilationUnit) root;
            if (compilationUnitRoot.imports().size() == 0) {
                PackageDeclaration packageDeclaration = compilationUnitRoot.getPackage();
                startPackageLineNumber = javaFile.compilationUnit
                        .getLineNumber(packageDeclaration.getName().getStartPosition());
                buggyLineContent = javaFile.fileLines.get(startPackageLineNumber - 1);
            } else {
                ImportDeclaration importDeclaration = (ImportDeclaration)compilationUnitRoot
                        .imports().get(compilationUnitRoot.imports().size() - 1);
                startPackageLineNumber = javaFile.compilationUnit.getLineNumber(importDeclaration.getStartPosition());
                buggyLineContent = javaFile.fileLines.get(startPackageLineNumber - 1);
            }

            StringBuilder fixedLineContent = new StringBuilder(buggyLineContent);
            for (String str : startPackage) {
                fixedLineContent.append(System.getProperty("line.separator")).append("import ").append(str).append(";");
            }
            String extraPath = String.join("#", new ArrayList<>(movedFile));
            DefectInstance defectInstance =
                    createDefectInstance(javaFile.filePath, startPackageLineNumber, buggyLineContent, fixedLineContent.toString());
            Map<String, Object> tempDesc = new HashMap<>();
            tempDesc.put("fieldName", "");
            tempDesc.put("hmsVersion", "");
            tempDesc.put("dependencyName", "Common");
            tempDesc.put("kit", "Common");
            tempDesc.put("text", "Generate an enumeration class for automatic constant replacement in switch cases");
            tempDesc.put("support", true);
            tempDesc.put("url", "");
            tempDesc.put("type", "");
            tempDesc.put("gmsVersion", "");
            tempDesc.put("status", "AUTO");
            tempDesc.put("extraPath", extraPath);
            defectInstance.setMessage(gson.toJson(tempDesc));
            defectInstances.add(defectInstance);
        }
    }

    private void processSwitchNodes(List<DefectInstance> defectInstances) {
        if (CollectionUtils.isEmpty(defectInstances)) {
            return;
        }
        for (Map.Entry<JavaFile, Set<SwitchStatement>> entry : this.switchInfos.entrySet()) {
            Iterator iter = entry.getValue().iterator();
            SwitchStatement recordSwitchStatement = null;
            while (iter.hasNext()) {
                SwitchStatement tempSwitchStatement =  (SwitchStatement)iter.next();
                recordSwitchStatement = tempSwitchStatement;
                Set<SwitchCase> tempSwitchCaseSet = new HashSet<>();
                for (int i =0; i<tempSwitchStatement.statements().size();i++) {
                    if (tempSwitchStatement.statements().get(i) instanceof SwitchCase) {
                        SwitchCase switchCase = (SwitchCase)tempSwitchStatement.statements().get(i);
                        if (switchCase.getExpression() == null) {
                            continue;
                        }
                        tempSwitchCaseSet.add(switchCase);
                    }
                }
                processSwitchCaseStatement(entry.getKey(), tempSwitchStatement, tempSwitchCaseSet, defectInstances);
            }
            if (recordSwitchStatement != null) {
                processPackageNode(entry.getKey(), recordSwitchStatement, defectInstances);
            }
            startPackage = new HashSet<>();
            movedFile = new HashSet<>();
        }
    }

    private String getSwitchStatementFixContent(SwitchStatement switchStatement) {
        return this.switchStatements.get(switchStatement).getClassName() + "." +
                "translateValue(" + switchStatement.getExpression().toString()+")";
    }

    private Map<String, Object> getSwitchCaseDesc(SwitchStatement switchStatement, SwitchCase switchCase) {
        if (switchCase.getExpression() == null) {
            return null;
        }
        SwitchStatementInfo switchStatementInfo = switchStatements.get(switchStatement);
        String gFieldName = switchCase.getExpression().toString();
        Set<SwitchCaseInfo> switchCaseInfos = switchFunctionInfos.get(switchStatementInfo);
        for (SwitchCaseInfo tempSwitchCaseInfo : switchCaseInfos) {
            if (tempSwitchCaseInfo.getGmsFieldName().equals(gFieldName)) {
                return tempSwitchCaseInfo.getDesc();
            }
        }
        return null;
    }

    private String getSwitchCaseFixContent(JavaFile javaFile, SwitchStatement switchStatement, SwitchCase switchCase) {
        if (switchCase.getExpression() == null) {
            return "default :";
        }
        SwitchStatementInfo switchStatementInfo = switchStatements.get(switchStatement);
        String gFieldName = switchCase.getExpression().toString();
        String replaceName = switchStatementInfo.getClassName().toUpperCase()
                + "_" + gFieldName.replace('.', '_').toUpperCase();
        Set<SwitchCaseInfo> switchCaseInfos = switchFunctionInfos.get(switchStatementInfo);
        for (SwitchCaseInfo tempSwitchCaseInfo : switchCaseInfos) {
            if (tempSwitchCaseInfo.getGmsFieldName().equals(gFieldName)) {
                startPackage.add(switchStatementInfo.getPackageName() + "." + switchStatementInfo.getClassName());
                movedFile.add(this.switchStatements.get(switchStatement).getFilePath()
                        + File.separator + this.switchStatements.get(switchStatement).getClassName() + ".java");
                break;
            }
        }
        return replaceName;
    }

    private void processSwitchCaseStatement(JavaFile javaFile,
                                            SwitchStatement switchStatement, Set<SwitchCase> switchCaseSet,
                                            List<DefectInstance> defectInstances) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        //process switchStatement
        int startLineNumber = javaFile.compilationUnit.getLineNumber(switchStatement.getStartPosition());
        String buggyLineContent = switchStatement.getExpression().toString();
        String fixedLineContent = getSwitchStatementFixContent(switchStatement);
        DefectInstance defectInstance =
                createDefectInstance(javaFile.filePath, startLineNumber, buggyLineContent, fixedLineContent);
        Map<String, Object> tempDesc = this.switchStatements.get(switchStatement).getDesc();
        defectInstance.setMessage(tempDesc == null ? "" : gson.toJson(tempDesc));
        defectInstances.add(defectInstance);

        //process switchCaseSet
        for (SwitchCase switchCase : switchCaseSet) {
            int startSetLineNumber = javaFile.compilationUnit.getLineNumber(switchCase.getStartPosition());
            if (switchCase.getExpression() == null) {
                continue;
            }
            String buggySetLineContent = switchCase.getExpression().toString();

            String fixedSetLineContent = getSwitchCaseFixContent(javaFile, switchStatement, switchCase);
            tempDesc = getSwitchCaseDesc(switchStatement, switchCase);
            DefectInstance defectInstanceSet =
                    createDefectInstance(javaFile.filePath, startSetLineNumber, buggySetLineContent, fixedSetLineContent);
            defectInstanceSet.setMessage(tempDesc == null ? "" : gson.toJson(tempDesc));
            defectInstances.add(defectInstanceSet);
        }
    }

    private String getFilePath(String moduleName) {
        return "com" +File.separator + "xms_" + moduleName + File.separator + "extra";
    }

    private String getPackageName(String moduleName) {
        return "com.xms_"+moduleName+".extra";
    }

    private String getModuleName() {
        String moduleName = DEFAULT_MODULE_NAME;
        if (!GradleFileUtils.isProject(new File(this.currentFixedProjectFolder))) {
            return moduleName;
        }

        String tempStr = this.currentFilePath.replace(this.currentFixedProjectFolder, "");
        String[] dirPaths = tempStr.split("/|\\\\");
        if (dirPaths[1] != null) {
            return dirPaths[1];
        }
        return moduleName;
    }

    private void mergeSwitchStatement(Map.Entry<SwitchStatementInfo, Set<SwitchCaseInfo>> parentSet,
            Map.Entry<SwitchStatementInfo, Set<SwitchCaseInfo>> childSet) {
        for (SwitchCaseInfo switchCaseInfo : childSet.getValue()) {
            Iterator iter = parentSet.getValue().iterator();
            boolean flag = true;
            while (iter.hasNext()) {
                SwitchCaseInfo temp = (SwitchCaseInfo) iter.next();
                if (switchCaseInfo.getGmsFieldName().equals(temp.getGmsFieldName())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                parentSet.getValue().add(switchCaseInfo);
            }
        }
        //switch case指向具体的对应信息
        for (Map.Entry<SwitchStatement, SwitchStatementInfo> entry: switchStatements.entrySet()) {
            if (entry.getValue().equals(childSet.getKey())) {
                entry.setValue(parentSet.getKey());
            }
        }
    }

    private void processSwitchFunctions() {
        for (Map.Entry<SwitchStatementInfo, Set<SwitchCaseInfo>> entry : this.switchFunctionInfos.entrySet()) {
            for (Map.Entry<SwitchStatementInfo, Set<SwitchCaseInfo>> e : this.switchFunctionInfos.entrySet()) {
                boolean flag = true;
                if (!entry.getKey().getTypeInfo().equals(e.getKey().getTypeInfo())
                        || entry.getKey().equals(e.getKey()) || entry.getKey().isChildSet()) {
                    continue;
                }
                if (entry.getKey().getTypeInfo().equals(e.getKey().getTypeInfo())) {
                    for (String str : e.getKey().getClazzNameSet()) {
                        if (!entry.getKey().getClazzNameSet().contains(str)) {
                            flag = false;
                            break;
                        }
                    }
                }

                if (flag) {
                    mergeEntry(entry, e);
                }
            }
        }
        generatorCode();
    }

    private void mergeEntry(Map.Entry<SwitchStatementInfo, Set<SwitchCaseInfo>> entry, Map.Entry<SwitchStatementInfo,
            Set<SwitchCaseInfo>> entry1) {
        String packageName = getPackageName(entry.getKey().getModuleName());
        String filePath = getFilePath(entry.getKey().getModuleName());
        if (entry.getKey().getClazzNameSet().size() >= entry1.getKey().getClazzNameSet().size()) {
            entry1.getKey().setChildSet(true);
            mergeSwitchStatement(entry, entry1);
            entry.getKey().setPackageName(packageName);
            entry.getKey().setFilePath(filePath);
        } else {
            entry.getKey().setChildSet(true);
            mergeSwitchStatement(entry1, entry);
            entry1.getKey().setPackageName(packageName);
            entry1.getKey().setFilePath(filePath);
        }
    }

    public Integer getBaseIdx(SwitchStatementInfo key, String suffix) {
        Integer base = 0;
        File extraPath = new File(suffix + File.separator + key.getFilePath());
        File[] files = extraPath.listFiles();
        if (files == null || files.length == 0) {
            return base;
        }

        for (File file : files) {
            if (file != null && file.getAbsolutePath().contains(key.getModuleName())
                    && file.getName().contains(key.getClassName())) {
                base++;
            }
        }
        return base;
    }

    private void generatorCode() {
        Iterator iter = this.switchFunctionInfos.keySet().iterator();
        while (iter.hasNext()) {
            SwitchStatementInfo key = (SwitchStatementInfo)iter.next();
            if (key.isChildSet()) {
                iter.remove();
                this.switchFunctionInfos.remove(key);
                continue;
            }

            String suffix = this.fixPatternFolder.replace("fixpatterns", "")
                    + "fixbot" + File.separator + key.getModuleName()+ File.separator + ENUM_FIEL_PATH;
            Integer base = getBaseIdx(key, suffix);

            if (filePathName.get(key.getFilePath() + key.getClass()) == null) {
                filePathName.put(key.getFilePath() + key.getClassName(), 1);
                if (base != 0) {
                    String tempClassName = key.getClassName();
                    key.setClassName(key.getClassName() + "_");
                    key.setClassName(key.getClassName() + base);
                    key.setFilePath(key.getFilePath().replace(tempClassName, key.getClassName()));
                }
            } else {
                Integer idx = filePathName.get(key.getFilePath() + key.getClassName());
                filePathName.put(key.getFilePath() + key.getClassName(), idx + 1);
                String tempClassName = key.getClassName();
                key.setClassName(key.getClassName() + "_");
                key.setClassName(key.getClassName() + base + idx);
                key.setFilePath(key.getFilePath().replace(tempClassName, key.getClassName()));
            }

            if (key.getFilePath().contains(this.currentFixedFolderSuffix)) {
                key.setFilePath(key.getFilePath().replace(this.originalProjectFolder
                        + this.currentFixedFolderSuffix, ""));
            }

            SwitchCaseNode switchCaseNode = generateSwitchCode(key, suffix);
            SwitchCaseAdapter adapter = new SwitchCaseAdapter();
            adapter.generate(switchCaseNode);
        }
    }

    private SwitchCaseNode generateSwitchCode(SwitchStatementInfo key, String suffix) {
        String className = key.getClassName();
        String packageName = key.getPackageName();
        String filePath = suffix + File.separator + key.getFilePath()
                .replace(key.getClassName() + ".java", "");
        String typeInfo = Mapping.TYPE_OF_TYPE.get(key.getTypeInfo());

        Map<String, SwitchCaseNode.Pair> enumConstantInfo = new HashMap<>();
        Set<String> importSet = new HashSet<>();
        for (SwitchCaseInfo switchCaseInfo : this.switchFunctionInfos.get(key)) {
            importSet.add(switchCaseInfo.getImportName());
            enumConstantInfo.put(className.toUpperCase()+ "_" + switchCaseInfo.getReplaceName(),
                    SwitchCaseNode.Pair.create(switchCaseInfo.getHmsFieldName(), switchCaseInfo.getGmsFieldName()));
        }
        List<String> importList = new ArrayList<>(importSet);
        SwitchCaseNode.Pair type = SwitchCaseNode.Pair.create(typeInfo, typeInfo);
        SwitchCaseNode switchCaseNode = SwitchCaseNode.create(className, packageName, filePath, enumConstantInfo, type);
        switchCaseNode.imports().addAll(importList);

        return switchCaseNode;
    }

    private class FieldAccessVisitor extends JavaRenameBaseVisitor {
        JavaTypeInferencer typeInferencer = new JavaTypeInferencer(this);

        protected FieldAccessVisitor(JavaFile javaFile, RenameBaseChanger changer) {
            super(javaFile, changer);
        }

        @Override
        public boolean visit(Assignment node) {
            if (node.getLeftHandSide() instanceof FieldAccess) {
                FieldAccess fieldAccess = (FieldAccess) node.getLeftHandSide();
                TypeInfo typeInfo = typeInferencer.getExprType(fieldAccess.getExpression());
                if (typeInfo != null) {
                    String qualifier = typeInfo.getQualifiedName();
                    String simpleName = fieldAccess.getName().toString();
                    return changeAssigmentToSet(node, qualifier, simpleName);
                }
            } else if (node.getLeftHandSide() instanceof QualifiedName) {
                QualifiedName fieldAccess = (QualifiedName) node.getLeftHandSide();
                TypeInfo typeInfo = typeInferencer.getExprType(fieldAccess.getQualifier());
                if (typeInfo != null) {
                    String qualifier = typeInfo.getQualifiedName();
                    String simpleName = fieldAccess.getName().toString();
                    return changeAssigmentToSet(node, qualifier, simpleName);
                }
            }
            return super.visit(node);
        }

        /**
         * change a assignment to a setter, e.g. xxx.A = x; -> xxx.setA(x);
         *
         * @param node       a {@link Assignment} node
         * @param qualifier  qualifier of a field
         * @param simpleName simple name of a field
         * @return false if it was actually changed
         */
        private boolean changeAssigmentToSet(Assignment node, String qualifier, String simpleName) {
            if (renamePatterns.containsKey(qualifier + "." + simpleName)) {
                int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                int endLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition());
                int endColumnNumber = startColumnNumber + javaFile.getRawSignature(node).length();
                String buggyLine =
                        String.join(javaFile.lineBreak, javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                String setField =
                        javaFile.getRawSignature(node.getLeftHandSide())
                                .replace(
                                        simpleName,
                                        "set"
                                                + simplifiedRenamePattern
                                                        .get(simpleName)
                                                        .substring(0, 1)
                                                        .toUpperCase(Locale.ENGLISH)
                                                + simplifiedRenamePattern.get(simpleName).substring(1));
                String getField =
                        javaFile.getRawSignature(node.getLeftHandSide())
                                .replace(
                                        simpleName,
                                        "get"
                                                + simplifiedRenamePattern
                                                .get(simpleName)
                                                .substring(0, 1)
                                                .toUpperCase(Locale.ENGLISH)
                                                + simplifiedRenamePattern.get(simpleName).substring(1))
                                + "()";
                String newShortName;
                if (node.getOperator() == Assignment.Operator.ASSIGN) {
                    newShortName = setField + "(" + javaFile.getRawSignature(node.getRightHandSide()) + ")";
                } else {
                    newShortName =
                            setField
                                    + "("
                                    + getField
                                    + " "
                                    + node.getOperator()
                                    .toString()
                                    .substring(0, node.getOperator().toString().length() - 1)
                                    + " "
                                    + javaFile.getRawSignature(node.getRightHandSide())
                                    + ")";
                }
                Map description = fullName2Description.get(qualifier + "." + simpleName);
                String desc = description == null ? null : gson.toJson(description);
                if (newShortName.equals(javaFile.getRawSignature(node))) {
                    DefectInstance defectInstance =
                            createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                    defectInstance.setMessage(desc);
                    defectInstance.isFixed = false;
                    defectInstance.status = FixStatus.NONEFIX.toString();
                    defectInstances.add(defectInstance);
                } else {
                    updateChangeTraceForALine(
                            line2Change,
                            buggyLine,
                            newShortName,
                            startLineNumber,
                            startColumnNumber,
                            endColumnNumber,
                            desc);
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean visit(ImportDeclaration node) {
            String importLine = node.toString();
            String importLineName = node.getName().toString();
            int importLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
            if (!importLine.contains(" static ")) {
                importName2LineNumber.put(importLineName, importLineNumber);
            } else if (renamePatterns.containsKey(importLineName)) {
                // if this import is a static field import, we change it from field access to a getter
                // e.g. import static xxx.field1; -> yyy.getField1;
                int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                int endLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                String buggyLine =
                        String.join(javaFile.lineBreak, javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                String fixedLinePattern = renamePatterns.get(importLineName);
                String fixedLine =
                        "import static "
                                + fixedLinePattern.substring(0, fixedLinePattern.lastIndexOf("."))
                                + ".get"
                                + fixedLinePattern.substring(fixedLinePattern.lastIndexOf(".") + 1)
                                + ";";
                DefectInstance defectInstance =
                        createLazyDefectInstance(javaFile.filePath, startLineNumber, buggyLine, fixedLine);
                Map desc = fullName2Description.get(importLineName);
                defectInstance.setMessage(desc == null ? "" : gson.toJson(desc));
                defectInstances.add(defectInstance);
                return false;
            }

            return true;
        }

        @Override
        public boolean visit(FieldAccess node) {
            TypeInfo typeInfo = typeInferencer.getExprType(node.getExpression());
            if (typeInfo != null && typeInfo.getQualifiedName() != null) {
                String oldFullName = typeInfo.getQualifiedName() + "." + node.getName();
                if (renamePatterns.containsKey(typeInfo.getQualifiedName() + "." + node.getName())) {
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    int endLineNumber =
                            javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                    int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getName().getStartPosition());
                    int endColumnNumber =
                            javaFile.compilationUnit.getColumnNumber(node.getStartPosition() + node.getLength());
                    String buggyLine =
                            String.join(
                                    javaFile.lineBreak, javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    String get =
                            "get"
                                    + simplifiedRenamePattern
                                    .get(node.getName().toString())
                                    .substring(0, 1)
                                    .toUpperCase(Locale.ENGLISH)
                                    + simplifiedRenamePattern.get(node.getName().toString()).substring(1)
                                    + "()";
                    String fieldShortName = node.getName().toString();

                    Map description = fullName2Description.get(oldFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    if (get.equals(fieldShortName)) {
                        DefectInstance defectInstance =
                                createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                        defectInstance.setMessage(desc);
                        defectInstance.isFixed = false;
                        defectInstance.status = FixStatus.NONEFIX.toString();
                        defectInstances.add(defectInstance);
                    } else {
                        updateChangeTraceForALine(
                                line2Change, buggyLine, get, startLineNumber, startColumnNumber, endColumnNumber, desc);
                    }
                }
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(QualifiedName node) {
            TypeInfo typeInfo = typeInferencer.getExprType(node.getQualifier());
            if (typeInfo != null && typeInfo.getQualifiedName() != null) {
                String oldFullName = typeInfo.getQualifiedName() + "." + node.getName();
                String fullPathNodeTemp = node.getFullyQualifiedName();
                if (!renamePatterns.containsKey(oldFullName) && renamePatterns.containsKey(fullPathNodeTemp)) {
                    oldFullName = fullPathNodeTemp;
                }
                if (renamePatterns.containsKey(oldFullName)) {
                    // something needs to be treat specially; when comes in switch-case, we do not change, just warning
                    if (node.getParent() instanceof SwitchCase) {
                        if (switchInfos.get(javaFile) != null && switchInfos.get(javaFile).contains(node.getParent().getParent())) {
                            return false;
                        }
                        int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                        Map desc = new HashMap(fullName2Description.get(oldFullName));
                        desc.put("status", "MANUAL");
                        String message = gson.toJson(desc);
                        DefectInstance defectInstance =
                                FieldAccessChanger.this.createWarningDefectInstance(
                                        javaFile.filePath,
                                        startLineNumber,
                                        javaFile.fileLines.get(startLineNumber - 1),
                                        message);
                        defectInstances.add(defectInstance);
                        return false;
                    }
                    // generally create a getter of new corresponding QualifiedName from the old QualifiedName
                    String newFullName = renamePatterns.get(oldFullName);
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    int endLineNumber =
                            javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                    String buggyLine =
                            String.join(
                                    javaFile.lineBreak, javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    String[] replacementString = getExistShortNames(buggyLine, oldFullName, newFullName);
                    String oldShortName = replacementString[0];
                    String newShortName = replacementString[1];
                    int startColumnNumber =
                            javaFile.compilationUnit.getColumnNumber(node.getStartPosition())
                                    + javaFile.getRawSignature(node).lastIndexOf(oldShortName);
                    int endColumnNumber = startColumnNumber + oldShortName.length();

                    StringBuilder get = new StringBuilder();
                    String[] splittedNewShortName = newShortName.split("\\.");
                    if (splittedNewShortName.length == 1) {
                        get =
                                new StringBuilder(
                                        "get"
                                                + splittedNewShortName[splittedNewShortName.length - 1]
                                                .substring(0, 1)
                                                .toUpperCase(Locale.ENGLISH)
                                                + splittedNewShortName[splittedNewShortName.length - 1].substring(1)
                                                + "()");
                    } else {
                        for (int i = 0; i < splittedNewShortName.length - 1; i++) {
                            get.append(splittedNewShortName[i]).append(".");
                        }
                        get.append("get")
                                .append(
                                        splittedNewShortName[splittedNewShortName.length - 1]
                                                .substring(0, 1)
                                                .toUpperCase(Locale.ENGLISH))
                                .append(splittedNewShortName[splittedNewShortName.length - 1].substring(1))
                                .append("()");
                    }

                    Map description = fullName2Description.get(oldFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    if (get.toString().equals(oldShortName)) {
                        // do not change if the getter equals to oldShortName, just generate a non-fix defectInstance
                        DefectInstance defectInstance =
                                createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                        defectInstance.setMessage(desc);
                        defectInstance.isFixed = false;
                        defectInstance.status = FixStatus.NONEFIX.toString();
                        defectInstances.add(defectInstance);
                    } else {
                        updateChangeTraceForALine(
                                line2Change,
                                buggyLine,
                                get.toString(),
                                startLineNumber,
                                startColumnNumber,
                                endColumnNumber,
                                desc);
                    }

                    String oldImportName = oldFullName.substring(0, oldFullName.lastIndexOf("."));
                    if (importName2LineNumber.get(oldImportName) != null) {
                        String temp = renamePatterns.get(oldFullName);
                        int importBuggyLineNumber = importName2LineNumber.get(oldImportName);
                        String importBuggyLine =
                                String.join(
                                        javaFile.lineBreak,
                                        javaFile.fileLines.subList(importBuggyLineNumber - 1, importBuggyLineNumber));
                        String importChangeLine = temp.substring(0, temp.lastIndexOf("."));
                        String importNewLine = "import " + importChangeLine + ";";
                        DefectInstance importLazyDefectInstanceTemp =
                                createLazyDefectInstance(
                                        javaFile.filePath, importBuggyLineNumber, importBuggyLine, importNewLine);
                        importLazyDefectInstanceTemp.message = desc;
                        defectInstances.add(importLazyDefectInstanceTemp);
                    }
                }
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(SimpleName node) {
            int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
            Object root = node.getRoot();
            if (root instanceof CompilationUnit) {
                String[] fullType = JavaTypeInferencer.getFullType(node.toString(), (CompilationUnit) root);
                if (fullType.length != 0 && renamePatterns.containsKey(fullType[0] + "." + fullType[1])) {
                    String buggyLine = javaFile.fileLines.get(startLineNumber - 1);
                    String originalClassType = node.toString();
                    String newClassType = renamePatterns.get(fullType[0] + "." + fullType[1]);
                    newClassType = newClassType.substring(newClassType.lastIndexOf(".") + 1);
                    String get =
                            "get"
                                    + newClassType.substring(0, 1).toUpperCase(Locale.ENGLISH)
                                    + newClassType.substring(1)
                                    + "()";
                    int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition());
                    int endColumnNumber = startColumnNumber + node.toString().length();

                    Map description = fullName2Description.get(fullType[0] + "." + fullType[1]);
                    String desc = description == null ? null : gson.toJson(description);
                    if (get.equals(originalClassType)) {
                        DefectInstance defectInstance =
                                createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                        defectInstance.setMessage(desc);
                        defectInstance.isFixed = false;
                        defectInstance.status = FixStatus.NONEFIX.toString();
                        defectInstances.add(defectInstance);
                    } else {
                        updateChangeTraceForALine(
                                line2Change, buggyLine, get, startLineNumber, startColumnNumber, endColumnNumber, desc);
                    }
                }
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(SwitchStatement node) {
            collectSwitchCaseInfo(node);
            return super.visit(node);
        }

        private void collectSwitchCaseInfo(SwitchStatement node) {
            Set<SwitchCase> switchCaseSet = new HashSet<>();
            boolean flag = false;
            for (int i =0; i<node.statements().size(); i++) {
                if (node.statements().get(i) instanceof SwitchCase) {
                    SwitchCase switchCase = (SwitchCase) node.statements().get(i);
                    if (switchCase.getExpression() == null) {
                        continue;
                    }
                    if (switchCase.getExpression() instanceof QualifiedName) {
                        QualifiedName qualifiedName = (QualifiedName) switchCase.getExpression();
                        TypeInfo typeInfo = typeInferencer.getExprType(qualifiedName.getQualifier());
                        if (typeInfo == null) {
                            return;
                        }
                        if (simpleG2HRenamePattern.containsKey(typeInfo.getQualifiedName() + "." + qualifiedName.getName())) {
                            flag = true;
                        } else {
                            flag = false;
                            break;
                        }
                    }
                    switchCaseSet.add(switchCase);
                }
            }
            if (flag) {
                if (switchInfos.containsKey(javaFile)) {
                    switchInfos.get(javaFile).add(node);
                } else {
                    Set<SwitchStatement> tempSwitchStatementsSet = new HashSet<>();
                    tempSwitchStatementsSet.add(node);
                    switchInfos.put(javaFile, tempSwitchStatementsSet);
                }
                processSwitchCaseFile(node, switchCaseSet);
            }
        }

        private void processSwitchCaseFile(SwitchStatement switchStatement, Set<SwitchCase> switchCaseSet) {
            Set<String> declareNames = new HashSet<>();
            Set<String> clazzNames = new HashSet<>();
            boolean flag = false;
            String moduleName = getModuleName();

            //process switchStatement
            SwitchStatementInfo switchStatementInfo = new SwitchStatementInfo();
            switchStatementInfo.setTypeInfo(typeInferencer.getExprType(switchStatement.getExpression()).toString());
            switchStatementInfo.setModuleName(moduleName);
            switchStatementInfo.setPackageName(getPackageName(moduleName));

            switchStatementInfo.setClassName(javaFile.name + getTransferName(switchStatementInfo.getTypeInfo()));
            switchStatementInfo.setFilePath(getFilePath(moduleName));
            Map<String, Object> tempDesc = new HashMap<>();
            tempDesc.put("fieldName", switchStatement.getExpression().toString());
            tempDesc.put("hmsVersion", "");
            tempDesc.put("dependencyName", "Common");
            tempDesc.put("kit", "Common");
            tempDesc.put("text", "Replace expression statements " +
                    "using the translateValue method in the generated enumeration class");
            tempDesc.put("support", true);
            tempDesc.put("url", "");
            tempDesc.put("type", "");
            tempDesc.put("gmsVersion", "");
            tempDesc.put("status", "AUTO");
            switchStatementInfo.setDesc(tempDesc);

            //process switchCaseSet
            Set<SwitchCaseInfo> switchCaseInfos = new HashSet<>();
            for (SwitchCase switchCase : switchCaseSet) {
                SwitchCaseInfo switchCaseInfo = new SwitchCaseInfo();
                if (switchCase.getExpression() == null) {
                    continue;
                }
                switchCaseInfo.setGmsFieldName(switchCase.getExpression().toString());
                switchCaseInfo.setDesc(tempDesc);
                if (switchCase.getExpression() instanceof QualifiedName) {
                    QualifiedName qualifiedName = (QualifiedName) switchCase.getExpression();
                    TypeInfo typeInfo = typeInferencer.getExprType(qualifiedName.getQualifier());
                    switchCaseInfo.setImportName(typeInferencer.getExprType(qualifiedName.getQualifier()).toString());
                    switchCaseInfo.setReplaceName(switchCaseInfo.getGmsFieldName().replace('.', '_').toUpperCase());
                    declareNames.add(typeInfo.getQualifiedName());
                    clazzNames.add(switchCaseInfo.getImportName() + "_" + switchCaseInfo.getGmsFieldName());
                    if (simpleG2HRenamePattern.containsKey(typeInfo.getQualifiedName() + "." + qualifiedName.getName())) {
                        switchCaseInfo.setHmsFieldName(simpleG2HRenamePattern.get(typeInfo.getQualifiedName() + "."
                                + qualifiedName.getName()));
                        Map desc = simpleG2HDescriptions.get(typeInfo.getQualifiedName() + "."
                                + qualifiedName.getName());
                        desc.put("text", "Replace a constant with the corresponding" +
                                " constant in the generated enumeration class");
                        switchCaseInfo.setDesc(desc);
                        flag = true;
                    } else {
                        switchCaseInfo.setHmsFieldName(typeInfo.getQualifiedName() + "." + qualifiedName.getName());
                    }
                } else if (switchCase.getExpression() instanceof SimpleName) {
                    SimpleName simpleName = (SimpleName) switchCase.getExpression();
                    String[] fullType = JavaTypeInferencer.getFullType(simpleName.toString(), (CompilationUnit) simpleName.getRoot());
                    String packageName = fullType[0];
                    switchCaseInfo.setImportName(fullType[0]);
                    declareNames.add(fullType[0]);
                    clazzNames.add(switchCaseInfo.getImportName() + "_" + switchCaseInfo.getGmsFieldName());
                    switchCaseInfo.setReplaceName(packageName.replace('.', '_').toUpperCase()
                            + "_" + switchCaseInfo.getGmsFieldName().replace('.', '_').toUpperCase());
                    switchCaseInfo.setHmsFieldName(switchCaseInfo.getGmsFieldName());
                }
                switchCaseInfos.add(switchCaseInfo);
            }
            switchStatementInfo.setDeclareNames(declareNames);
            switchStatementInfo.setClazzNameSet(clazzNames);
            if (flag) {
                switchStatements.put(switchStatement, switchStatementInfo);
                switchFunctionInfos.put(switchStatementInfo, switchCaseInfos);
            }
        }

        private String getTransferName(String typeInfo) {
            return Mapping.TYPE_OF_NAME.get(typeInfo);
        }
    }
}
