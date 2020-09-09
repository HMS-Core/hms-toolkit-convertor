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
    private static volatile TimeUtil instance = null;

    private static final int MINUTE_UNIT = 1000 * 60;

    private static final int SECOND_UNIT = 1000;

    private static final int SECOND = 60;

    private static final int MILLSECOND = 1000;

    private long startTime;

    private TimeUtil() {
    }

    public static TimeUtil getInstance() {
        if (instance == null) {
            instance = new TimeUtil();
        }

        return instance;
    }

    public void getStartTime() {
        startTime = System.currentTimeMillis();
    }

    public String getElapsedTime() {
        long time = System.currentTimeMillis() - startTime;
        long min = time / MINUTE_UNIT % SECOND;
        long sec = time / SECOND_UNIT % SECOND;
        long ms = time % MILLSECOND;

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
