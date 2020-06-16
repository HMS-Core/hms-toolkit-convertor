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

package com.huawei.hms.convertor.core.result.diff;

import com.huawei.generator.g2x.po.summary.Diff;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Getter
@Setter
@Slf4j
@NoArgsConstructor
public final class XmsDiff {
    private List<String> addList = new ArrayList();

    private TreeMap<String, String> updatedMap = new TreeMap();

    private TreeMap<String, String> modMap = new TreeMap();

    private List<String> delList = new ArrayList();

    private List<String> lastKitList = new ArrayList();

    private List<String> newKitList = new ArrayList();

    private String lastGeneratedTime;

    private String currentTime;

    private String lastToolVersion;

    private String currentToolVersion;

    private String oldXMSLocation;

    private String newXMSLocation;

    private String targetXMSLocation;

    private String depDescription;

    private boolean hasDiffContent;

    public XmsDiff(Diff diff) {
        addList.addAll(diff.getAddList());
        updatedMap.putAll(diff.getUpdatedMap());
        modMap.putAll(diff.getModMap());
        delList.addAll(diff.getDelList());
        lastKitList.addAll(diff.getLastKitList());
        newKitList.addAll(diff.getNewKitList());
        lastGeneratedTime = diff.getLastGeneratedTime();
        currentTime = diff.getCurrentTime();
        lastToolVersion = diff.getLastToolVersion();
        currentToolVersion = diff.getCurrentToolVersion();
        oldXMSLocation = diff.getOldXMSLocation();
        newXMSLocation = diff.getNewXMSLocation();
        targetXMSLocation = diff.getTargetXMSLocation();
        depDescription = diff.getDepDescription();
        hasDiffContent = diff.hasDiffContent();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
