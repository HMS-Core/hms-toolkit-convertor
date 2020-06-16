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
 * API Result Object
 *
 * @since 2020-02-24
 */
@Getter
@AllArgsConstructor
public class Result<T> {
    private long code;

    private T data;

    private String message;

    /**
     * Assemble ok result
     *
     * @param <T> Type of result data
     * @return OK result object
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    public Result(long code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Assemble ok result
     *
     * @param data Result data
     * @param <T> Type of result data
     * @return OK result object
     */
    public static <T> Result<T> ok(T data) {
        ErrorCode errorCode = ErrorCode.SUCCESS;
        if (data instanceof Boolean && Boolean.FALSE.equals(data)) {
            errorCode = ErrorCode.FAILURE;
        }
        return of(data, errorCode);
    }

    /**
     * Assemble failed result
     *
     * @param message Error message
     * @param <T> Type of result data
     * @return Failed result object
     */
    public static <T> Result<T> failed(String message) {
        return of(ErrorCode.FAILURE.getCode(), message);
    }

    /**
     * Assemble failed result
     *
     * @param errorCode Error code
     * @param <T> Type of result data
     * @return Failed result object
     */
    public static <T> Result<T> failed(ErrorCode errorCode) {
        return of(errorCode);
    }

    /**
     * Assemble result
     *
     * @param data Result data
     * @param errorCode Error code
     * @param <T> Type of result data
     * @return Result object
     */
    public static <T> Result<T> of(T data, ErrorCode errorCode) {
        return of(errorCode.getCode(), data, errorCode.getMessage());
    }

    private static <T> Result<T> of(long code, String message) {
        return new Result<T>(code, message);
    }

    public static <T> Result<T> of(ErrorCode errorCode) {
        return of(errorCode.getCode(), errorCode.getMessage());
    }

    private static <T> Result<T> of(long code, T data, String message) {
        return new Result<T>(code, data, message);
    }

    /**
     * Check if the result is ok
     *
     * @return {@code true} if execute succeed; {@code false} otherwise
     */
    public boolean isOk() {
        return ErrorCode.SUCCESS.getCode() == code;
    }
}
