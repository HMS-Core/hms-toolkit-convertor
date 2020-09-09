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

package com.huawei.hms.convertor.openapi.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Error Code
 *
 * @since 2020-02-24
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    /**
     * Execute succeed
     */
    SUCCESS(0, "Execute succeed"),

    /**
     * Execute failed
     */
    FAILURE(1, "Execute failed"),

    /**
     * Fixbot execute failed caused by OOM
     */
    OOM(2, "OutOfMemoryError");

    private final int code;

    private final String message;
}
