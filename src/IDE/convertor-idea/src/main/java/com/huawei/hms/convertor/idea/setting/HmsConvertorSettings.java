/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.idea.setting;

import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * HMS Convertor old settings,
 * which is no longer uesd.
 *
 * @since 2019-06-27
 */
@State(name = "HmsConvertorSettings", storages = @Storage("HmsConvertorSettings.xml"))
public class HmsConvertorSettings implements PersistentStateComponent<HmsConvertorSettings> {
    private String inspectPath;

    private List<String> excludeList = new ArrayList<>();

    private String backupPath;

    private RoutePolicy routePolicy;

    private boolean multiApk;

    private boolean hmsFirst;

    private List<String> xmsAdaptorPathList = new ArrayList<>();

    private List<String> xms4GAdaptorPathList = new ArrayList<>();

    private String repoId;

    private String inspectFolder;

    private String allianceDomain;

    private boolean commentEnable;

    private String type;

    public static HmsConvertorSettings getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, HmsConvertorSettings.class);
    }

    @Nullable
    @Override
    public HmsConvertorSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull HmsConvertorSettings hmsConvertorSettings) {
        XmlSerializerUtil.copyBean(hmsConvertorSettings, this);
    }

    public String getInspectPath() {
        return inspectPath;
    }

    public void setInspectPath(String inspectPath) {
        this.inspectPath = inspectPath;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public List<String> getExcludeList() {
        return excludeList;
    }

    public void setExcludeList(List<String> excludeList) {
        this.excludeList = excludeList;
    }

    public RoutePolicy getRoutePolicy() {
        return routePolicy;
    }

    public void setRoutePolicy(RoutePolicy routePolicy) {
        this.routePolicy = routePolicy;
    }

    public boolean isMultiApk() {
        return multiApk;
    }

    public void setMultiApk(boolean multiApk) {
        this.multiApk = multiApk;
    }

    public boolean isHmsFirst() {
        return hmsFirst;
    }

    public void setHmsFirst(boolean hmsFirst) {
        this.hmsFirst = hmsFirst;
    }

    public List<String> getXmsAdaptorPathList() {
        return xmsAdaptorPathList;
    }

    public void setXmsAdaptorPathList(List<String> xmsAdaptorPathList) {
        this.xmsAdaptorPathList = xmsAdaptorPathList;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getInspectFolder() {
        return inspectFolder;
    }

    public void setInspectFolder(String inspectFolder) {
        this.inspectFolder = inspectFolder;
    }

    public List<String> getXms4GAdaptorPathList() {
        return xms4GAdaptorPathList;
    }

    public void setXms4GAdaptorPathList(List<String> xms4GAdaptorPathList) {
        this.xms4GAdaptorPathList = xms4GAdaptorPathList;
    }

    public String getAllianceDomain() {
        return allianceDomain;
    }

    public void setAllianceDomain(String allianceDomain) {
        this.allianceDomain = allianceDomain;
    }

    public boolean isCommentEnable() {
        return commentEnable;
    }

    public void setCommentEnable(boolean commentEnable) {
        this.commentEnable = commentEnable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
