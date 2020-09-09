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
import com.huawei.codebot.analyzer.x2y.xml.CodeNetDocumentLocator;
import com.huawei.codebot.analyzer.x2y.xml.CodeNetSaxReader;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import java.io.File;
import java.util.List;

/**
 * Check AndroidManifest.xml find whether there has Application class, check java code or kotlin code find whether
 * need classloader pattern
 *
 * @since 2020-07-08
 */
public class KeyClassCheckChanger extends AndroidAppFixer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyClassCheckChanger.class);

    private static final String IGNORE_KEY_WORLD = File.separator + "xmsadapter" + File.separator
            + "src" + File.separator + "main" + File.separator;

    private static final String ANDROID_MANIFEST_FILE
            = "src" + File.separator + "main" + File.separator + "AndroidManifest.xml";

    private static final String APPLICATION = "application";

    private static final String NAME= "name";

    private MethodModel methodModel;

    public MethodModel getMethodModel() {
        return methodModel;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        if (buggyFilePath.contains(IGNORE_KEY_WORLD)) {
            return null;
        }

        JavaFileAnalyzer codeAnalyzer = new JavaFileAnalyzer();
        JavaFile javaFile = codeAnalyzer.extractJavaFileInfo(buggyFilePath);
        CompilationUnit unit = javaFile.compilationUnit;
        if (GlobalSettings.isNeedClassloader()) {
            return null;
        }
        List<ImportDeclaration> importDeclarationList = unit.imports();
        for (ImportDeclaration importDeclaration: importDeclarationList) {
            Name name = importDeclaration.getName();
            if (!(name instanceof QualifiedName)) {
                continue;
            }
            QualifiedName qualifiedName = (QualifiedName) name;
            String fullQualifiedName = qualifiedName.getFullyQualifiedName();
            if (GlobalSettings.SET.contains(fullQualifiedName)) {
                GlobalSettings.setNeedClassloader(true);
                break;
            }
        }
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        if (GlobalSettings.isHasApplication()) {
            return null;
        }
        if (buggyFilePath.endsWith(ANDROID_MANIFEST_FILE)) {
            File file = new File(buggyFilePath);
            Locator locator = new LocatorImpl();
            DocumentFactory docFactory = new CodeNetDocumentLocator(locator);
            SAXReader saxReader = new CodeNetSaxReader(docFactory, locator);
            Document document = null;
            try {
                document = saxReader.read(new File(buggyFilePath));
            } catch (DocumentException e) {
                LOGGER.error("It is failed to parse xml file");
            }
            assert document != null;
            Element element = document.getRootElement();
            List<Element> elementList = element.elements();
            for (Element ele : elementList) {
                if (ele.getQualifiedName().equals(APPLICATION)) {
                    if (ele.attribute(NAME) != null) {
                        GlobalSettings.setHasApplication(true);
                        this.methodModel = new MethodModel();
                        String appName = ele.attributeValue(NAME);
                        setMethodModelValue(element, appName, file);
                    }
                    break;
                }
            }
        }
        return null;
    }

    private void setMethodModelValue(Element element, String appName, File file) {
        Attribute rootAttribute = element.attribute(0);
        String packageName =rootAttribute.getValue();
        if (appName.startsWith(".")) {
            packageName = packageName + appName;
        } else {
            packageName = appName;
        }
        String packagePath = packageName.replace(".", File.separator);
        String parentPath = file.getParent();
        String currentJavaFilePath = parentPath + File.separator
                + "java" + File.separator + packagePath + ApplicationClassUtils.JAVA_SUFFIX;
        String currentKotlinFilePath = parentPath + File.separator
                + "java" + File.separator + packagePath + ApplicationClassUtils.KOTLIN_SUFFIX;
        File appJavaFile = new File(currentJavaFilePath);
        File appKotlinFile = new File(currentKotlinFilePath);
        if (appJavaFile.exists()) {
            setJavaMethodModel(currentJavaFilePath);
        } else if (appKotlinFile.exists()) {
            setKotlinMethodModel(currentKotlinFilePath);
        } else {
            LOGGER.error("This Application class is not in current project,"
                    + "You should inherit it and rewrite onCreate method");
        }
    }

    private void setJavaMethodModel(String buggyFilePath) {
        JavaFileAnalyzer codeAnalyzer = new JavaFileAnalyzer();
        JavaFile javaFile = codeAnalyzer.extractJavaFileInfo(buggyFilePath);
        CompilationUnit unit = javaFile.compilationUnit;
        List types =  unit.types();
        for (Object type: types) {
            if (type instanceof TypeDeclaration) {
                TypeDeclaration typeDeclaration = (TypeDeclaration) type;
                this.methodModel = new MethodModel();
                this.methodModel.setUnit(unit);
                this.methodModel.setTypeDeclaration(typeDeclaration);
                this.methodModel.setBuggyFilePath(buggyFilePath);
                this.methodModel.setJavaFile(true);
            }
        }
    }

    private void setKotlinMethodModel(String buggyFilePath) {
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        ParserRuleContext tree = (ParserRuleContext) kotlinFile.tree;
        KotlinParser.TopLevelObjectContext topLevelObjectContext = tree.getRuleContext(KotlinParser
                .TopLevelObjectContext.class, 0);
        KotlinParser.DeclarationContext declarationContext = topLevelObjectContext
                .getRuleContext(KotlinParser.DeclarationContext.class, 0);
        KotlinParser.ClassDeclarationContext classDeclarationContext = declarationContext
                .getRuleContext(KotlinParser.ClassDeclarationContext.class, 0);
        this.methodModel = new MethodModel();
        this.methodModel.setJavaFile(false);
        this.methodModel.setBuggyFilePath(buggyFilePath);
        this.methodModel.setClassDeclarationContext(classDeclarationContext);
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String s) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        ParserRuleContext tree = (ParserRuleContext) kotlinFile.tree;
        if (!GlobalSettings.isNeedClassloader()) {
            KotlinParser.ImportListContext importListContext
                    = tree.getRuleContext(KotlinParser.ImportListContext.class, 0);
            if (importListContext == null) {
                return null;
            }
            List<KotlinParser.ImportHeaderContext> importListContextList
                    = importListContext.getRuleContexts(KotlinParser.ImportHeaderContext.class);
            if (CollectionUtils.isEmpty(importListContextList)) {
                return null;
            }
            for (KotlinParser.ImportHeaderContext importContext : importListContextList) {
                KotlinParser.IdentifierContext identifierContext = importContext
                        .getRuleContext(KotlinParser.IdentifierContext.class, 0);
                if (identifierContext != null && GlobalSettings.SET.contains(identifierContext.getText())) {
                    GlobalSettings.setNeedClassloader(true);
                    break;
                }
            }
        }
        return null;
    }

    @Override
    protected void generateFixCode(DefectInstance defectInstance) {

    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String s) {

    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.WISEHUB;
            info.description = "Key Class Check Changer";
            this.info = info;
        }
        return this.info;
    }
}
