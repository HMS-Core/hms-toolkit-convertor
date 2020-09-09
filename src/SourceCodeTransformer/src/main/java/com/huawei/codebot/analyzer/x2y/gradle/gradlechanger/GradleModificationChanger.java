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
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAddClassPathInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAddMavenInReposotories;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddApplyPlugin;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddMessage;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddIndirectDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAidl;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppDeleteGmsApplyPlugin;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppDeleteInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppReplace;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructDeleteClasspathInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructDeleteClasspathInRepositories;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructSettingGradle;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructSpecialAdd;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructVersionWarnings;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructGradleHeadquarter;
import com.huawei.codebot.analyzer.x2y.gradle.utils.GradleFileUtils;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.context.Context;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.CompilePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final HashMap DESC = new HashMap();
    /**
     * gson to read from json
     */
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    static {
        DESC.put("kit", "Common");
        DESC.put("autoConvert", true);
        DESC.put("text", "GMS dependencies will be deleted.A newest version will be added");
        DESC.put("url", "");
    }

    /**
     * Map <String projectAddMavenInRepositories,Struct StructAddMavenInReposotories >
     */
    public Map<String, StructAddMavenInReposotories> projectAddMavenInRepositories;
    /**
     * Map <String projectDeleteClasspathInDependencies,Struct projectDeleteClasspathInDependencies >
     */
    public Map<String, StructDeleteClasspathInDependencies> projectDeleteClasspathInDependencies;
    /**
     * Map <String projectDeleteClasspathInRepositories,Struct projectDeleteClasspathInRepositories >
     */
    public Map<String, StructDeleteClasspathInRepositories> projectDeleteClasspathInRepositories;
    /**
     * Map <String projectAddClassPathInDependencies,Struct projectAddClassPathInDependencies >
     */
    public Map<String, StructAddClassPathInDependencies> projectAddClassPathInDependencies;
    /**
     * Map <String appBuildGradleAddMessage,Struct appBuildGradleAddMessage >
     */
    public Map<String, StructAppAddMessage> appBuildGradleAddMessage;
    /**
     * Map <String appDeleteGmsApplyPlugin,Struct appDeleteGmsApplyPlugin >
     */
    public Map<String, StructAppDeleteGmsApplyPlugin> appDeleteGmsApplyPlugin;
    /**
     * Map <String appBuildGradleReplace,Struct appBuildGradleReplace >
     */
    public Map<String, StructAppReplace> appBuildGradleReplace;
    /**
     * Map <String appDeleteInDependencies,Struct appDeleteInDependencies >
     */
    public Map<String, StructAppDeleteInDependencies> appDeleteInDependencies;
    /**
     * Map <String appAddApplyPlugin,Struct appAddApplyPlugin >
     */
    public Map<String, StructAppAddApplyPlugin> appAddApplyPlugin;
    /**
     * Map <String appAidls,Struct appAidls >
     */
    public Map<String, StructAppAidl> appAidls;
    /**
     * Map <String appAddIndirectDependencies,Struct appAddIndirectDependencies >
     */
    public Map<String, StructAppAddIndirectDependencies> appAddIndirectDependencies;
    /**
     * Map <String appAddInDependencies,Struct appAddInDependencies >
     */
    public Map<String, StructAppAddInDependencies> appAddInDependencies;
    /**
     * Map <String addImplementationsByAidl,Struct addImplementationsByAidl >
     */
    public Set<StructAppAidl> addImplementationsByAidl;
    /**
     * String currentFileLineBreak
     */
    public String currentFileLineBreak;
    /**
     * String currentScope
     */
    public String currentScope;
    /**
     * special add in settings.gradle
     */
    public StructSettingGradle specialAddInSettingsGradle;
    /**
     * special add one line in app build.gradle dependency
     */
    public StructSpecialAdd specialAddInDependency;
    /**
     * special add one line in app build.gradle aplly
     */
    public StructSpecialAdd specialAddInApply;
    /**
     * if version continue alphabet like '17.0.0-beta', we need to generate an alarm.
     */
    public StructVersionWarnings versionWarnings;
    /**
     * special add allprojects node in project build.gradle if it is missing in project build.gradle
     */
    public StructSpecialAdd allProjectsInfo;
    
    // Record whether allprojects node exists in bulid.gradle under project directory
    private boolean isContinueAllProject;

    public HashSet<String> legalDependenciesForXms = new HashSet<>();
    private String currentFile;
    private String fileContent;

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

    public String getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    private void initConfig(StructGradleHeadquarter config) {
        // project: add maven
        projectAddMavenInRepositories = new HashMap<>();
        for (StructAddMavenInReposotories addMaven : config.getProjectAddMavenInRepositories()) {
            projectAddMavenInRepositories.put(addMaven.getAddMavenInRepositoriesName(), addMaven);
        }

        Set<String> delsClPath = new HashSet<>();
        // project: add classpath in dependencies
        projectAddClassPathInDependencies = new HashMap<>();
        for (StructAddClassPathInDependencies addClassPath : config.getProjectAddClassPathInDependencies()) {
            projectAddClassPathInDependencies.put(addClassPath.getAddClassPathInDependenciesName(), addClassPath);
            delsClPath.add(addClassPath.getAddClassPathInDependenciesName());
        }

        // project: delete classpath in dependencies
        projectDeleteClasspathInDependencies = new HashMap<>();
        for (StructDeleteClasspathInDependencies deleteClasspath : config.getProjectDeleteClasspathInDependencies()) {
            projectDeleteClasspathInDependencies.put(deleteClasspath.getDeleteClasspathInDependenciesName(),
                    deleteClasspath);
        }

        for (String del : delsClPath) {
            String name = del;
            String version = "";
            int index = del.lastIndexOf(":");
            if (index > 0) {
                name = del.substring(0, index);
                version = del.substring(index + 1);
            }
            projectDeleteClasspathInDependencies.put(name, new StructDeleteClasspathInDependencies(name, version, DESC));
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
        // app: add in dependencies
        appAddInDependencies = new HashMap<>();
        for (StructAppAddInDependencies dependency : config.getAppAddInDependencies()) {
            appAddInDependencies.put(dependency.getOriginGoogleName(), dependency);
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

        addImplementationsByAidl = new HashSet<>();

        // Special add in app build.gradle in dependency
        //region this is a tmp fix for xms, it will be refactor in the new version
        String specialAddStr = config.getSpecialAddInDependency().getAddString();
        if (!StringUtils.isEmpty(specialAddStr) && specialAddStr.contains("@")) {
            String[] strings = specialAddStr.split("@");
            legalDependenciesForXms.addAll(Arrays.asList(strings).subList(0, strings.length - 1));
            StructSpecialAdd tempSpecialAddInDependency = config.getSpecialAddInDependency();
            tempSpecialAddInDependency.setAddString(strings[strings.length - 1]);
            specialAddInDependency = tempSpecialAddInDependency;
        } else {
            specialAddInDependency = config.getSpecialAddInDependency();
        }
        //endregion

        // Special add in settings.gradle
        specialAddInSettingsGradle = config.getSettingGradle();
        // Special add in app build.gradle in app
        specialAddInApply = config.getSpecialAddInApp();

        allProjectsInfo = config.getAllProjectsInfo();

        versionWarnings = config.getVersionWarnings();
    }

    /**
     * checkout all files
     */
    private void checkAidlFiles() {
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
                LOGGER.error("An exception occurred during the processing:", e);
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
        if (StringUtils.isEmpty(buggyFilePath)) {
            return null;
        }
        currentFile = buggyFilePath;
        currentFileDefectInstances = new ArrayList<>();
        AstBuilder builder = new AstBuilder();
        try {
            fileContent = FileUtils.getFileContent(buggyFilePath);
        } catch (IOException e) {
            logger.error(Throwables.getStackTraceAsString(e));
        }
        //Support ext{}
        if (fileContent != null) {
            List<String> lines = FileUtils.cutStringToList(fileContent);
            for (String line : lines) {
                Context context = Context.getContext();
                Set keySet = context.getContextMap().keySet();
                for (Object key : keySet) {
                    String keyStr = ((MultiKey) key).getKey(0).toString();
                    if (!line.contains(keyStr)) {
                        continue;
                    }
                    String valueStr = context.getContextMap().get(key).toString();
                    fileContent = fileContent.replace(keyStr, valueStr);
                }
            }
        }
        List<ASTNode> nodes = builder.buildFromString(CompilePhase.CONVERSION, true, fileContent);
        if (buggyFilePath.endsWith(GradleFileUtils.SETTING_GRADLE_FILE)) {
            for (ASTNode node : nodes) {
                SettingGradleModificationVisitor visitor = new SettingGradleModificationVisitor(this);
                visitor.addIncludeXmsadapterValue();

                node.visit(visitor);
            }
            return this.currentFileDefectInstances;
        }

        if (!buggyFilePath.endsWith("build.gradle")) {
            return currentFileDefectInstances;
        }
        if (buggyFilePath.endsWith(GradleFileUtils.BUILD_GRADLE_FILE)) {
            boolean isProject = GradleFileUtils.isProjectBuildGradleFile(new File(buggyFilePath));
            currentScope = isProject ? GradleFileUtils.PROJECT_BUILD_GRADLE : GradleFileUtils.MODULE_BUILD_GRADLE;
            currentFileLineBreak = StringUtil.getLineBreak(fileContent);

            isContinueAllProject = !currentScope.equals(GradleFileUtils.PROJECT_BUILD_GRADLE);
            for (ASTNode node : nodes) {
                GradleModificationVisitor visitor;
                if (currentScope.equals(GradleFileUtils.PROJECT_BUILD_GRADLE)) {
                    visitor = new ProjectBuildGradleModificationVisitor(this);
                } else {
                    visitor = new ModuleBuildGradleModificationVisitor(this);
                }
                node.visit(visitor);
                visitor.addApplyPlugins(currentFileLines.size());
            }
        }
        return currentFileDefectInstances;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        return null;
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

    /**
     * Get the flag of weather the project build.gradle continue allprojects
     *
     * @return true if all projects node exists in bulid.gradle
     */
    public boolean isContinueAllProject() {
        return isContinueAllProject;
    }

    /**
     * Set the flag of weather the project build.gradle continue allprojects
     * 
     * @param value value
     */
    public void setContinueAllProject(boolean value) {
        isContinueAllProject = value;
    }
}
