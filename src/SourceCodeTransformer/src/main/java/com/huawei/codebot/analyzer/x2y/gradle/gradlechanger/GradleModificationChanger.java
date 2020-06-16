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

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.CompilePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gradle Modification Changer
 * core to change the build.gradle
 *
 * @since 2020-04-01
 */
public class GradleModificationChanger extends AndroidAppFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleModificationChanger.class);
    Map<String, StructAddMavenInReposotories> projectAddMavenInRepositories;
    Map<String, StructDeleteClasspathInDependencies> projectDeleteClasspathInDependencies;
    Map<String, StructDeleteClasspathInRepositories> projectDeleteClasspathInRepositories;
    Map<String, StructAddClassPathInDependencies> projectAddClassPathInDependencies;
    Map<String, StructAppAddMessage> appBuildGradleAddMessage;
    Map<String, StructAppDeleteGmsApplyPlugin> appDeleteGmsApplyPlugin;
    Map<String, StructAppReplace> appBuildGradleReplace;
    Map<String, StructAppDeleteInDependencies> appDeleteInDependencies;
    Map<String, StructAppAddApplyPlugin> appAddApplyPlugin;
    Map<String, StructAppAidl> appAidls;
    Map<String, StructAppAddIndirectDependencies> appAddIndirectDependencies;
    Map<String, StructAppAddInDependencies> appAddInDependencies;
    Set<StructAppAidl> addImplementationsByAidl;
    String currentFileLineBreak;
    String currentScope;
    StructSettingGradle specialAddInSettingsGradle;
    StructSpecialAddInDependency specialAddInDependency;
    final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public GradleModificationChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        StructGradleHeadquarter configGradle = configService.getGradleHeadquarter();
        initConfig(configGradle);
    }

    /**
     * for subclasses initConfig
     */
    protected GradleModificationChanger(StructGradleHeadquarter config) {
        initConfig(config);
    }

    void initConfig(StructGradleHeadquarter config) {
        initProject(config);

        initApp(config);

        addImplementationsByAidl = new HashSet<>();

        // Special add in settings.gradle
        specialAddInSettingsGradle = config.getSettingGradle();

        // Special add in app build.gradle in dependency
        specialAddInDependency = config.getSpecialAddInDependency();
    }

    private void initProject(StructGradleHeadquarter config) {
        // project: add maven
        projectAddMavenInRepositories = new HashMap<>();
        for (StructAddMavenInReposotories addMaven : config.getProjectAddMavenInRepositories()) {
            projectAddMavenInRepositories.put(addMaven.getAddMavenInRepositoriesName(), addMaven);
        }

        // project: delete classpath in dependencies
        projectDeleteClasspathInDependencies = new HashMap<>();
        for (StructDeleteClasspathInDependencies deleteClasspath : config.getProjectDeleteClasspathInDependencies()) {
            projectDeleteClasspathInDependencies.put(deleteClasspath.getDeleteClasspathInDependenciesName(),
                    deleteClasspath);
        }

        // project: delete classpath in repositories
        projectDeleteClasspathInRepositories = new HashMap<>();
        for (StructDeleteClasspathInRepositories deleteClasspath : config.getProjectDeleteClasspathInRepositories()) {
            projectDeleteClasspathInRepositories.put(deleteClasspath.getDeleteClasspathInRepositoriesName(),
                    deleteClasspath);
        }

        // project: add classpath in dependencies
        projectAddClassPathInDependencies = new HashMap<>();
        for (StructAddClassPathInDependencies addClassPath : config.getProjectAddClassPathInDependencies()) {
            projectAddClassPathInDependencies.put(addClassPath.getAddClassPathInDependenciesName(), addClassPath);
        }
    }

    private void initApp(StructGradleHeadquarter config) {
        // app: build gradle add message
        appBuildGradleAddMessage = new HashMap<>();
        for (StructAppAddMessage addMessage : config.getAppBuildGradleAddMessage()) {
            appBuildGradleAddMessage.put(addMessage.getAddMessageInDependenciesImplementation(), addMessage);
        }

        // app: delete gms apply plugin
        appDeleteGmsApplyPlugin = new HashMap<>();
        for (StructAppDeleteGmsApplyPlugin deleteGms : config.getAppDeleteGmsApplyPlugin()) {
            appDeleteGmsApplyPlugin.put(deleteGms.getDeleteApplyPluginInApp(), deleteGms);
        }

        // app: build gradle replace
        appBuildGradleReplace = new HashMap<>();
        for (StructAppReplace replace : config.getAppBuildGradleReplace()) {
            appBuildGradleReplace.put(replace.getOriginGoogleName(), replace);
        }

        // app: delete in dependencies
        appDeleteInDependencies = new HashMap<>();
        for (StructAppDeleteInDependencies deleteInDependencies : config.getAppDeleteInDependencies()) {
            appDeleteInDependencies.put(deleteInDependencies.getDeleteClasspathInDependenciesName(),
                    deleteInDependencies);
        }

        // app: add apply plugin
        appAddApplyPlugin = new HashMap<>();
        for (StructAppAddApplyPlugin applyPlugin : config.getAppAddApplyPlugin()) {
            appAddApplyPlugin.put(applyPlugin.getAddApplyPluginInApp(), applyPlugin);
        }

        // app: aidl
        appAidls = new HashMap<>();
        if (config.getAppAidls() != null) {
            for (StructAppAidl appAidl : config.getAppAidls()) {
                String aidlname = appAidl.getAidlName().substring(0, appAidl.getAidlName().length() - 5);
                String aidlPath = aidlname.replace(".", File.separator) + ".aidl";
                appAidls.put(aidlPath, appAidl);
            }
        }

        // app: add indirect dependencies
        appAddIndirectDependencies = new HashMap<>();
        for (StructAppAddIndirectDependencies dependency : config.getAppAddIndirectDependencies()) {
            appAddIndirectDependencies.put(dependency.getOriginGoogleName(), dependency);
        }

        // app: add in dependencies
        appAddInDependencies = new HashMap<>();
        for (StructAppAddInDependencies dependency : config.getAppAddInDependencies()) {
            appAddInDependencies.put(dependency.getOriginGoogleName(), dependency);
        }
    }

    /**
     * checkout all files
     */
    void checkAidlFiles() {
        for (String filePath : analyzedFilePaths) {
            for (Map.Entry<String, StructAppAidl> entry : appAidls.entrySet()) {
                if (filePath.endsWith(entry.getKey())) {
                    StructAppAidl structAppAidl = entry.getValue();
                    addImplementationsByAidl.add(structAppAidl);
                }
            }
        }
    }

    @Override
    protected List<DefectInstance> detectDefectsForSingleProject() {
        checkAidlFiles();
        List<DefectInstance> defectInstances = new ArrayList<>();
        for (String filePath : analyzedFilePaths) {
            try {
                List<DefectInstance> defectWarnings = detectDefectsForSingleFile(filePath);
                if (defectWarnings != null) {
                    defectInstances.addAll(defectWarnings);
                }
                analyzedFileNum.getAndIncrement();
            } catch (Exception e) {
                LOGGER.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            }
        }
        return defectInstances;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buggyFilePath) {
        currentFileDefectInstances = new ArrayList<>();

        if (buggyFilePath.endsWith("settings.gradle")) {
            String buggyLine = "";
            int startLineNumber = 2;
            String fixedLine = specialAddInSettingsGradle.getAddString();
            Map desc = specialAddInSettingsGradle.getDesc();
            if (fixedLine == null || fixedLine.equals("") || desc.size() == 0) {
                return currentFileDefectInstances;
            } else {
                DefectInstance instance = createDefectInstance(buggyFilePath, -startLineNumber, buggyLine, fixedLine);
                instance.setMessage(gson.toJson(desc));
                currentFileDefectInstances.add(instance);
                return currentFileDefectInstances;
            }
        }

        if (!buggyFilePath.endsWith("build.gradle")) {
            return currentFileDefectInstances;
        }

        try {
            String fileContent = FileUtils.getFileContent(buggyFilePath);
            if (fileContent.contains("allprojects")) {
                currentScope = "project";
            } else if (fileContent.contains("minSdkVersion") || fileContent.contains("targetSdkVersion")) {
                currentScope = "app";
            } else {
                return currentFileDefectInstances;
            }
            currentFileLineBreak = StringUtil.getLineBreak(fileContent);
            parseGradleFile(fileContent);
        } catch (Exception e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
        }
        return currentFileDefectInstances;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        return null;
    }

    /**
     * use parser to visit gradle files
     * input file output visit nodes
     */
    void parseGradleFile(String fileContent) {
        AstBuilder builder = new AstBuilder();
        List<ASTNode> nodes = builder.buildFromString(CompilePhase.CONVERSION, true, fileContent);
        for (ASTNode node : nodes) {
            GradleModificationVisitor visitor = new GradleModificationVisitor(this);
            node.visit(visitor);
            visitor.addApplyPlugins(currentFileLines.size());
        }
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_GRADLEMODIFICATION;
            info.description = "gradle changer";
            this.info = info;
        }
        return this.info;
    }
}
