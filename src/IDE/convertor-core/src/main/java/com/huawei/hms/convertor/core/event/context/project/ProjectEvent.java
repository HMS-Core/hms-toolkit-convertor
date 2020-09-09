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

package com.huawei.hms.convertor.core.event.context.project;

import com.huawei.hms.convertor.core.event.context.Event;
import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.util.Constant;

import lombok.Getter;
import lombok.ToString;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 * Project level event
 *
 * @param <D> Event data type
 * @param <M> Callback message type
 * @since 2020-03-24
 */
@Getter
@ToString(callSuper = true)
public class ProjectEvent<D, M> extends Event<D, M> {
    private String projectPath;

    private String projectName;

    private ProjectEvent(EventType type, Consumer<M> callback, String projectPath) {
        super(type, null, callback);
        this.projectPath = projectPath;

        initProjectName();
    }

    private ProjectEvent(EventType type, D data, Consumer<M> callback, String projectPath) {
        super(type, data, callback);
        this.projectPath = projectPath;

        initProjectName();
    }

    /**
     * Initialize event
     *
     * @param projectPath Project base path
     * @param type Event type
     * @param callback Event callback
     * @param <D> Event data type
     * @param <M> Callback message type
     * @return Event object
     */
    public static <D, M> ProjectEvent<D, M> of(String projectPath, EventType type, Consumer<M> callback) {
        return new ProjectEvent<>(type, callback, projectPath);
    }

    /**
     * Initialize event
     *
     * @param projectPath Project base path
     * @param type Event type
     * @param data Event data
     * @param callback Event callback
     * @param <D> Event data type
     * @param <M> Callback message type
     * @return Event object
     */
    public static <D, M> ProjectEvent<D, M> of(String projectPath, EventType type, D data, Consumer<M> callback) {
        return new ProjectEvent<>(type, data, callback, projectPath);
    }

    /**
     * Check if code edit event
     *
     * @return {@code true} if code edit event; {@code false} otherwise
     */
    boolean isEditEvent() {
        return getType() == EventType.EDIT_EVENT;
    }

    private void initProjectName() {
        projectName =
            StringUtils.substring(projectPath, StringUtils.lastIndexOf(projectPath, Constant.UNIX_FILE_SEPARATOR_IN_CHAR) + 1);
    }
}
