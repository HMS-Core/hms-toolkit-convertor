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

package com.huawei.hms.convertor.core.event.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event type
 *
 * @since 2020-02-24
 */
@Getter
@AllArgsConstructor
public enum EventType {
    /**
     * Code edit event
     */
    EDIT_EVENT(1, "Code edit event"),

    /**
     * Convert event
     */
    CONVERT_EVENT(2, "Convert event"),

    /**
     * Revert event
     */
    REVERT_EVENT(3, "Revert event"),

    /**
     * Save all event
     */
    SAVE_ALL_EVENT(4, "Save all event"),

    /**
     * Recovery event
     */
    RECOVERY_EVENT(5, "Recovery event");

    private final int code;

    private final String name;
}
