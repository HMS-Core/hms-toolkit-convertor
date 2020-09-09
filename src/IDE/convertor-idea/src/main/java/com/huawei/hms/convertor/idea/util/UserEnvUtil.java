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

package com.huawei.hms.convertor.idea.util;

import com.intellij.openapi.util.SystemInfo;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * User environment util
 *
 * @since 2019/5/10
 */
public final class UserEnvUtil {
    private String ideName;

    private String ideVersion;

    private int ideBaselineVersion;

    private String pluginId;

    private String pluginName;

    private String pluginVersion;

    private String osName;

    private String osVersion;

    private String osArch;

    private int screenWidth;

    private int screenHeight;

    public UserEnvUtil() {
    }

    public static UserEnvUtil create() {
        final UserEnvUtil userEnvUtil = new UserEnvUtil();

        userEnvUtil.setIdeName(ClientUtil.getIdeName());
        userEnvUtil.setIdeVersion(ClientUtil.getIdeVersion());
        userEnvUtil.setIdeBaselineVersion(VersionUtil.getIdeBaselineVersion());

        ClientUtil.getPluginId().ifPresent(pluginId -> userEnvUtil.setPluginId(pluginId));
        ClientUtil.getPluginName().ifPresent(pluginName -> userEnvUtil.setPluginName(pluginName));

        ClientUtil.getPluginVersion().ifPresent(pluginVersion -> userEnvUtil.setPluginVersion(pluginVersion));

        userEnvUtil.setOsName(SystemInfo.OS_NAME);
        userEnvUtil.setOsVersion(SystemInfo.OS_VERSION);
        userEnvUtil.setOsArch(SystemInfo.OS_ARCH);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        userEnvUtil.setScreenWidth(screenSize.width);
        userEnvUtil.setScreenHeight(screenSize.height);

        return userEnvUtil;
    }

    public void setIdeName(String ideName) {
        this.ideName = ideName;
    }

    public void setIdeVersion(String ideVersion) {
        this.ideVersion = ideVersion;
    }

    public void setIdeBaselineVersion(int ideBaselineVersion) {
        this.ideBaselineVersion = ideBaselineVersion;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    @Override
    public String toString() {
        return "UserEnvUtil { " + "ideName='" + ideName + '\'' + ", ideVersion='" + ideVersion + '\''
            + ", ideBaselineVersion='" + ideBaselineVersion + '\'' + ", pluginId='" + pluginId + '\'' + ", pluginName='"
            + pluginName + '\'' + ", pluginVersion='" + pluginVersion + '\'' + ", osName='" + osName + '\''
            + ", osVersion='" + osVersion + '\'' + ", osArch='" + osArch + '\'' + ", screenWidth='" + screenWidth + '\''
            + ", screenHeight='" + screenHeight + '\'' + " }";
    }

}
