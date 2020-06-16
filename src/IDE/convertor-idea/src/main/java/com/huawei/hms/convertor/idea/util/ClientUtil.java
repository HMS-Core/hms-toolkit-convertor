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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.extensions.PluginId;

import java.io.File;
import java.util.Optional;

/**
 * Client utility class
 *
 * @since 2018-05-10
 */
public final class ClientUtil {
    private static final ApplicationInfo APPLICATION_INFO = ApplicationInfo.getInstance();

    private ClientUtil() {
    }

    public static String getIdeName() {
        return (APPLICATION_INFO != null) ? APPLICATION_INFO.getVersionName() : "";
    }

    public static String getIdeVersion() {
        return (APPLICATION_INFO != null) ? APPLICATION_INFO.getFullVersion() : "";
    }

    /**
     * Get plugin id
     *
     * @return plugin id
     */
    public static Optional<String> getPluginId() {
        if (getPluginDescriptor().isPresent()) {
            final Optional<IdeaPluginDescriptor> pluginDescriptor = getPluginDescriptor();
            final PluginId pluginId = pluginDescriptor.get().getPluginId();
            return pluginId != null ? Optional.of(pluginId.getIdString()) : Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Get plugin name
     *
     * @return plugin name
     */
    public static Optional<String> getPluginName() {
        if (getPluginDescriptor().isPresent()) {
            return Optional.of(getPluginDescriptor().get().getName());
        }
        return Optional.empty();
    }

    /**
     * Get plugin package path
     *
     * @return plugin package path
     */
    public static Optional<String> getPluginPackagePath() {
        if (getPluginDescriptor().isPresent()) {
            File pluginDescriptorPath = getPluginDescriptor().get().getPath();
            if (pluginDescriptorPath != null) {
                return Optional.of(pluginDescriptorPath.getPath());
            }
        }
        return Optional.empty();
    }

    /**
     * Get plugin version
     *
     * @return plugin version
     */
    public static Optional<String> getPluginVersion() {
        if (getPluginDescriptor().isPresent()) {
            return Optional.of(getPluginDescriptor().get().getVersion());
        }
        return Optional.empty();
    }

    private static Optional<IdeaPluginDescriptor> getPluginDescriptor() {
        final ClassLoader classLoader = ClientUtil.class.getClassLoader();
        if (null != classLoader && classLoader instanceof PluginClassLoader) {
            final PluginId pluginId = ((PluginClassLoader) classLoader).getPluginId();
            return Optional.of(PluginManager.getPlugin(pluginId));
        }
        return Optional.empty();
    }
}
