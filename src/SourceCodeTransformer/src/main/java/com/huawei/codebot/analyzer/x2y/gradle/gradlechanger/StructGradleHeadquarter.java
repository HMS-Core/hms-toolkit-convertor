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

import java.util.ArrayList;
import java.util.List;

/**
 * Struct Gradle Headquarter
 * Define Gradle's overall data structure
 *
 * @since 2020-04-01
 */
public class StructGradleHeadquarter {
    private List<StructAddMavenInReposotories> projectAddMavenInRepositories;

    private List<StructDeleteClasspathInDependencies> projectDeleteClasspathInDependencies;

    private List<StructDeleteClasspathInRepositories> projectDeleteClasspathInRepositories;

    private List<StructAddClassPathInDependencies> projectAddClassPathInDependencies;

    private List<StructAppAddApplyPlugin> appAddApplyPlugin;

    private List<StructAppAddMessage> appBuildGradleAddMessage;

    private List<StructAppDeleteGmsApplyPlugin> appDeleteGmsApplyPlugin;

    private List<StructAppReplace> appBuildGradleReplace;

    private List<StructAppDeleteInDependencies> appDeleteInDependencies;

    private List<StructAppAddIndirectDependencies> appAddIndirectDependencies;

    private List<StructAppAddInDependencies> appAddInDependencies;

    private List<StructAppAidl> appAidls;

    private StructSettingGradle settingGradle;

    private StructSpecialAddInDependency specialAddInDependency;

    public StructGradleHeadquarter() {
        setProjectAddMavenInRepositories(new ArrayList<>());
        setProjectDeleteClasspathInDependencies(new ArrayList<>());
        setProjectDeleteClasspathInRepositories(new ArrayList<>());
        setProjectAddClassPathInDependencies(new ArrayList<>());
        setAppBuildGradleAddMessage(new ArrayList<>());
        setAppDeleteGmsApplyPlugin(new ArrayList<>());
        setAppBuildGradleReplace(new ArrayList<>());
        setAppDeleteInDependencies(new ArrayList<>());
        setAppAddApplyPlugin(new ArrayList<>());
        setAppAidls(new ArrayList<>());
        setAppAddIndirectDependencies(new ArrayList<>());
        setAppAddInDependencies(new ArrayList<>());
        setSettingGradle(new StructSettingGradle());
        setSpecialAddInDependency(new StructSpecialAddInDependency());
    }

    /**
     * add Maven In Repositories in project
     */
    public List<StructAddMavenInReposotories> getProjectAddMavenInRepositories() {
        return projectAddMavenInRepositories;
    }

    public void setProjectAddMavenInRepositories(List<StructAddMavenInReposotories> projectAddMavenInRepositories) {
        this.projectAddMavenInRepositories = projectAddMavenInRepositories;
    }

    /**
     * delete classpath in dependencies in project
     */
    public List<StructDeleteClasspathInDependencies> getProjectDeleteClasspathInDependencies() {
        return projectDeleteClasspathInDependencies;
    }

    public void setProjectDeleteClasspathInDependencies(
            List<StructDeleteClasspathInDependencies> projectDeleteClasspathInDependencies) {
        this.projectDeleteClasspathInDependencies = projectDeleteClasspathInDependencies;
    }

    /**
     * delete classpath in repositories in project
     */
    public List<StructDeleteClasspathInRepositories> getProjectDeleteClasspathInRepositories() {
        return projectDeleteClasspathInRepositories;
    }

    public void setProjectDeleteClasspathInRepositories(
            List<StructDeleteClasspathInRepositories> projectDeleteClasspathInRepositories) {
        this.projectDeleteClasspathInRepositories = projectDeleteClasspathInRepositories;
    }

    /**
     * add class path in dependencies in project
     */
    public List<StructAddClassPathInDependencies> getProjectAddClassPathInDependencies() {
        return projectAddClassPathInDependencies;
    }

    public void setProjectAddClassPathInDependencies(
            List<StructAddClassPathInDependencies> projectAddClassPathInDependencies) {
        this.projectAddClassPathInDependencies = projectAddClassPathInDependencies;
    }

    /**
     * add apply plugin in app
     */
    public List<StructAppAddApplyPlugin> getAppAddApplyPlugin() {
        return appAddApplyPlugin;
    }

    public void setAppAddApplyPlugin(List<StructAppAddApplyPlugin> appAddApplyPlugin) {
        this.appAddApplyPlugin = appAddApplyPlugin;
    }

    /**
     * build gradle add message in app
     */
    public List<StructAppAddMessage> getAppBuildGradleAddMessage() {
        return appBuildGradleAddMessage;
    }

    public void setAppBuildGradleAddMessage(List<StructAppAddMessage> appBuildGradleAddMessage) {
        this.appBuildGradleAddMessage = appBuildGradleAddMessage;
    }

    /**
     * delete gms apply plugin in app
     */
    public List<StructAppDeleteGmsApplyPlugin> getAppDeleteGmsApplyPlugin() {
        return appDeleteGmsApplyPlugin;
    }

    public void setAppDeleteGmsApplyPlugin(List<StructAppDeleteGmsApplyPlugin> appDeleteGmsApplyPlugin) {
        this.appDeleteGmsApplyPlugin = appDeleteGmsApplyPlugin;
    }

    /**
     * build gradle replace in app
     */
    public List<StructAppReplace> getAppBuildGradleReplace() {
        return appBuildGradleReplace;
    }

    public void setAppBuildGradleReplace(List<StructAppReplace> appBuildGradleReplace) {
        this.appBuildGradleReplace = appBuildGradleReplace;
    }

    /**
     * delete in dependencies in app
     */
    public List<StructAppDeleteInDependencies> getAppDeleteInDependencies() {
        return appDeleteInDependencies;
    }

    public void setAppDeleteInDependencies(List<StructAppDeleteInDependencies> appDeleteInDependencies) {
        this.appDeleteInDependencies = appDeleteInDependencies;
    }

    /**
     * add in direct Dependencies in app
     */
    public List<StructAppAddIndirectDependencies> getAppAddIndirectDependencies() {
        return appAddIndirectDependencies;
    }

    public void setAppAddIndirectDependencies(List<StructAppAddIndirectDependencies> appAddIndirectDependencies) {
        this.appAddIndirectDependencies = appAddIndirectDependencies;
    }

    /**
     * add in Dependencies in app
     * now use appAddInDependencies seldom use appAddIndirectDependencies
     */
    public List<StructAppAddInDependencies> getAppAddInDependencies() {
        return appAddInDependencies;
    }

    public void setAppAddInDependencies(List<StructAppAddInDependencies> appAddInDependencies) {
        this.appAddInDependencies = appAddInDependencies;
    }

    /**
     * add aidls in app
     */
    public List<StructAppAidl> getAppAidls() {
        return appAidls;
    }

    public void setAppAidls(List<StructAppAidl> appAidls) {
        this.appAidls = appAidls;
    }

    /**
     * special demand in settings.gradle
     */
    public StructSettingGradle getSettingGradle() {
        return settingGradle;
    }

    public void setSettingGradle(StructSettingGradle settingGradle) {
        this.settingGradle = settingGradle;
    }

    /**
     * special demand
     * add in gradle dependency directly
     */
    public StructSpecialAddInDependency getSpecialAddInDependency() {
        return specialAddInDependency;
    }

    public void setSpecialAddInDependency(StructSpecialAddInDependency specialAddInDependency) {
        this.specialAddInDependency = specialAddInDependency;
    }
}
