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

import com.huawei.hms.convertor.openapi.ProgressService;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * description:read inputStream of fixbot process
 *
 * @since 2020-06-09
 */
@Slf4j
public class InputStreamProcessor implements Runnable {
    private Process fixbotProcess;

    private InputStream inputStream;

    private ProgressService progressService;

    public InputStreamProcessor(Process fixbotProcess, InputStream inputStream, ProgressService progressService) {
        this.fixbotProcess = fixbotProcess;
        this.inputStream = inputStream;
        this.progressService = progressService;
    }

    @Override
    public void run() {
        LinkedBlockingQueue<String> stageQueue = new LinkedBlockingQueue<>(progressService.getClassStages().size());
        stageQueue.addAll(progressService.getClassStages());
        String stage = stageQueue.poll();
        String nextStage = stageQueue.poll();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (progressService.isCancel()) {
                    fixbotProcess.destroy();
                    log.info("The user triggered the cancel button, and fixbot process is destory");
                }

                if (line.contains(stage)) {
                    progressService.updateEngineFraction(stage);
                    stage = nextStage;
                    if (stageQueue.size() > 0) {
                        nextStage = stageQueue.poll();
                    }
                } else {
                    if (line.contains(nextStage)) {
                        progressService.updateEngineFraction(nextStage);
                        if (stageQueue.size() > 1) {
                            stage = stageQueue.poll();
                            nextStage = stageQueue.poll();
                        } else if (stageQueue.size() == 1) {
                            stage = stageQueue.poll();
                        }
                    }
                }

                log.info(line);
            }
        } catch (IOException e) {
            log.error("Handle fixbot inputStream error", e);
        }
    }
}
