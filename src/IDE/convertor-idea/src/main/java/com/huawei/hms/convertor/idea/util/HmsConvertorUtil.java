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

import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.ui.result.HmsConvertorToolWindow;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.platform.ProjectBaseDirectory;
import com.intellij.ui.content.Content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.swing.JComponent;

/**
 * HMS convertor util
 *
 * @since 2019-07-01
 */
public final class HmsConvertorUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HmsConvertorUtil.class);

    private static final Pattern XMS_JAR_PATTERN = Pattern.compile("^xms-engine-[\\d+\\.]*(.)*\\.jar$");

    private HmsConvertorUtil() {
    }

    /**
     * Get project base path
     *
     * @param project the current project
     * @return the project base path
     */
    @Nullable
    public static String getProjectBasePath(@Nullable Project project) {
        if (null != project && null != project.getBasePath()) {
            return project.getBasePath();
        }

        if (null != ProjectBaseDirectory.getInstance(project)) {
            VirtualFile baseDir = ProjectBaseDirectory.getInstance(project).getBaseDir();
            if (null != baseDir) {
                return baseDir.getPath();
            }
        }

        return "";
    }

    /**
     * Get hms convertor tool window
     *
     * @param project the current project
     * @return the instance of hms convertor tool window
     */
    public static Optional<HmsConvertorToolWindow> getHmsConvertorToolWindow(@NotNull Project project) {
        final ToolWindow toolWindow = ToolWindowUtil.getToolWindow(project, UIConstants.ToolWindow.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return Optional.empty();
        }

        final Content[] contents = toolWindow.getContentManager().getContents();
        if (contents.length == 0) {
            return Optional.empty();
        }

        final JComponent component = contents[0].getComponent();
        return (component instanceof HmsConvertorToolWindow) ? Optional.of((HmsConvertorToolWindow) component)
            : Optional.empty();
    }

    /**
     * Find and set xms generator jar path
     *
     * @throws NoSuchFileException jar not exist
     */
    public static void findXmsGeneratorJar() throws NoSuchFileException {
        if (!StringUtil.isEmpty(System.getProperty(XmsConstants.KEY_XMS_JAR))) {
            if (LOG.isInfoEnabled()) {
                LOG.info("findXmsGeneratorJar: already set. ");
            }
            return;
        }

        String pluginPackagePath = ClientUtil.getPluginPackagePath().get();
        if (StringUtil.isEmpty(pluginPackagePath)) {
            throw new NoSuchFileException(HmsConvertorBundle.message("no_engine_found"));
        }
        String pluginJarPath = pluginPackagePath.replace("\\", "/");
        String pluginJarDir = pluginJarPath + "/lib";
        List<File> jars = FileUtil.findFilesByMask(XMS_JAR_PATTERN, new File(pluginJarDir));
        if (LOG.isInfoEnabled()) {
            LOG.info("Xms generator jar count = " + jars.size());
        }
        if (jars.isEmpty()) {
            throw new NoSuchFileException(HmsConvertorBundle.message("no_engine_found"));
        }

        String jarPath = jars.get(Constant.FIRST_INDEX).getPath().replace("\\", "/");
        System.setProperty(XmsConstants.KEY_XMS_JAR, jarPath);
    }
}
