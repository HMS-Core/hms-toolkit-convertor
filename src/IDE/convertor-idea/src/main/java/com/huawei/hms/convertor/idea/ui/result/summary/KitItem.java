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

package com.huawei.hms.convertor.idea.ui.result.summary;

import com.alibaba.fastjson.JSON;

/**
 * Kit item
 *
 * @since 2019/11/28
 */
public final class KitItem {
    private int id;

    private String kitName;

    private int methodCount;

    public KitItem(int id, String kitName, int methodCount) {
        this.id = id;
        this.kitName = kitName;
        this.methodCount = methodCount;
    }

    public int getId() {
        return id;
    }

    public String getKitName() {
        return kitName;
    }

    public int getMethodCount() {
        return methodCount;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
