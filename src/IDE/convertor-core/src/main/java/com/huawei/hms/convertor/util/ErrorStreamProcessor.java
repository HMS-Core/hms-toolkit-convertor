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

package com.huawei.hms.convertor.util;

import com.huawei.hms.convertor.openapi.result.ErrorCode;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * description:read errorStream of fixbot process
 *
 * @since 2020-06-09
 */
@Getter
@Slf4j
public class ErrorStreamProcessor implements Runnable {
    private volatile int errorCode;

    private InputStream errorStream;

    public ErrorStreamProcessor(InputStream errorStream) {
        this.errorStream = errorStream;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(ErrorCode.OOM.getMessage())) {
                    log.error("Fixbot analysis failed, caused by OutOfMemoryError");
                    errorCode = ErrorCode.OOM.getCode();
                }
                log.info(line);
            }
        } catch (IOException e) {
            log.error("Handle fixbot errorStream error", e);
        }
    }
}
