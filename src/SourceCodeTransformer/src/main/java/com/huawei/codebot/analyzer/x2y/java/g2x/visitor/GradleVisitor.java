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

package com.huawei.codebot.analyzer.x2y.java.g2x.visitor;

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.java.g2x.AppFileModifyChanger;
import com.huawei.codebot.analyzer.x2y.java.g2x.codegen.AppFileGen;
import com.huawei.codebot.analyzer.x2y.xml.CodeNetDocumentLocator;
import com.huawei.codebot.analyzer.x2y.xml.CodeNetSaxReader;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Check gradle file to determine the Application class path
 *
 * @since 2020-07-08
 */
public class GradleVisitor extends CodeVisitorSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleVisitor.class);
    private static final String APPLY_PLUGIN_APP = "this.apply([plugin:com.android.application])";
    private static final String ANDROID_MANIFEST_FILE
            = "src" + File.separator + "main" + File.separator + "AndroidManifest.xml";

    private boolean isProjectBuildGradle;
    private AppFileModifyChanger changer;

    public GradleVisitor (boolean isProjectBuildGradle, AppFileModifyChanger changer) {
        this.isProjectBuildGradle = isProjectBuildGradle;
        this.changer = changer;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (!GlobalSettings.isIsSDK() && !isProjectBuildGradle) {
            if (APPLY_PLUGIN_APP.equals(call.getText())) {
                File mainBuildGradleFile = new File(changer.currentFilePath);
                GlobalSettings.setMainModuleName(mainBuildGradleFile.getParentFile().getName());
                if (mainBuildGradleFile.exists()) {
                    File file = mainBuildGradleFile.getParentFile();
                    try {
                        FileVisitor visitor = new FileVisitor(changer);
                        Files.walkFileTree(file.toPath(), visitor);
                    } catch (IOException e) {
                        LOGGER.error("It is failed to parse xml file");
                    }
                }
            }
        }
    }

    private static class FileVisitor extends SimpleFileVisitor<Path> {

        private AppFileModifyChanger changer;

        public FileVisitor(AppFileModifyChanger changer) {
            this.changer = changer;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (file.endsWith(ANDROID_MANIFEST_FILE)) {
                Locator locator = new LocatorImpl();
                DocumentFactory docFactory = new CodeNetDocumentLocator(locator);
                SAXReader reader = new CodeNetSaxReader(docFactory, locator);
                Document document = null;
                try {
                    document = reader.read(file.toFile());
                } catch (DocumentException e) {
                    LOGGER.error("It is failed to parse xml file");
                }
                if (document != null && document.getRootElement() != null) {
                    Element element = document.getRootElement();
                    Attribute rootAttribute = element.attribute(0);
                    String packageName = rootAttribute.getValue();
                    String packagePath = packageName.replace(".", File.separator);
                    GlobalSettings.setAppFilePath(File.separator + packagePath + File.separator + "MyApp.java");
                    String fixbotPath = changer.fixPatternFolder.replace("fixpatterns", "fixbot");
                    String appFilePath = file.getParent() + File.separator + "java" + File.separator + packagePath
                            + File.separator + "MyApp.java";
                    appFilePath = appFilePath.replace(changer.currentFixedProjectFolder, fixbotPath);
                    AppFileGen.createAppClass(appFilePath, packageName);
                    return FileVisitResult.TERMINATE;
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
