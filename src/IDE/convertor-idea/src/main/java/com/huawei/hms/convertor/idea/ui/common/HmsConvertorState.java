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

package com.huawei.hms.convertor.idea.ui.common;

import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HMS convertor state
 *
 * @since 2019-06-10
 */
public enum HmsConvertorState {
    IDLE,
    RUNNING;

    private static final Map<Project, HmsConvertorState> STATE_BY_PROJECT = new ConcurrentHashMap<>();

    /**
     * Get plugin state
     *
     * @param project the project to analysis
     * @return plugin state
     */
    @NotNull
    public static HmsConvertorState get(@NotNull final Project project) {
        HmsConvertorState state = STATE_BY_PROJECT.get(project);
        return null == state ? IDLE : state;
    }

    /**
     * Set plugin state
     *
     * @param project the project to analysis
     * @param state plugin state
     */
    public static void set(@NotNull final Project project, @NotNull final HmsConvertorState state) {
        STATE_BY_PROJECT.put(project, state);
    }

    /**
     * Judge plugin is idle or not
     *
     * @return plugin is idle, true: yes; false: no
     */
    public boolean isIdle() {
        return IDLE.equals(this);
    }
}
