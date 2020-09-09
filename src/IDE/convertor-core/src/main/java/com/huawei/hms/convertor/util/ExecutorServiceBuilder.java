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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Execute service builder
 *
 * @since 2020-02-26
 */
public final class ExecutorServiceBuilder {
    private static final int THREAD_POOL_SIZE = 1;

    private static final int THREAD_QUEUE_CAPACITY = 100;

    /**
     * New single thread executor
     *
     * @param threadNameFormat Thread name format
     * @return Thread executor
     */
    public static ExecutorService newSingleThreadExecutor(String threadNameFormat) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadNameFormat).build();
        return new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(THREAD_QUEUE_CAPACITY), threadFactory);
    }

    /**
     * New single scheduled thread executor
     *
     * @param threadNameFormat Thread name format
     * @return scheduled thread executor
     */
    public static ScheduledThreadPoolExecutor newScheduledThreadPoolExecutor(String threadNameFormat) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadNameFormat).build();
        return new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE, threadFactory);
    }
}
