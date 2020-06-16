/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.generator.g2x.processor.module;

/**
 * Enum for param type
 *
 * @since 2020-04-07
 */
public enum ParamKind {
    OLD_SUMMARY(0),
    NEW_SUMMARY(1),
    LOCAL_SUMMARY(2),

    PLUGIN_PATH(0),
    BACKUP_PATH(1),
    TARGET_PATH(2);

    private int index;

    ParamKind(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
