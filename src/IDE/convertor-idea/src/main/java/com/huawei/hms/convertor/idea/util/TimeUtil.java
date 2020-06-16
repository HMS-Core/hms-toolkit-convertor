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

/**
 * Time util
 *
 * @since 2018-03-05
 */
public final class TimeUtil {
    private static TimeUtil instance = null;

    private long startTime;

    private TimeUtil() {
    }

    public static TimeUtil getInstance() {
        if (null == instance) {
            instance = new TimeUtil();
        }

        return instance;
    }

    public void getStartTime() {
        startTime = System.currentTimeMillis();
    }

    public String getElapsedTime() {
        long time = System.currentTimeMillis() - startTime;
        long min = time / 1000 / 60 % 60;
        long sec = time / 1000 % 60;
        long ms = time % 1000;

        StringBuilder elapsedTime = new StringBuilder();
        if (min > 0) {
            elapsedTime.append(min).append("m ");
        }
        if (sec > 0) {
            elapsedTime.append(sec).append("s ");
        }
        elapsedTime.append(ms).append("ms");

        return elapsedTime.toString();
    }
}
