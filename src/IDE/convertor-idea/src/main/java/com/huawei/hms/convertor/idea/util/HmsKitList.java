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

import com.huawei.hms.convertor.idea.ui.analysis.PolicySettingDialog;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.util.text.StringUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
@Slf4j
public final class HmsKitList {
    private static final Map<String, String> HMS_MAP;

    static {
        HMS_MAP = new HashMap<String, String>();
        HMS_MAP.put("safetydetect", "Safetynet");
        HMS_MAP.put("dtm-api", "DTM");
        HMS_MAP.put("drive", "Drive");
        HMS_MAP.put("panorama", "Panorama");
        HMS_MAP.put("scan", "Scan");
        HMS_MAP.put("iap", "IAP");
        HMS_MAP.put("hihealth-base", "Health");
        HMS_MAP.put("nearby", "Nearby");
        HMS_MAP.put("ads-identifier", "Ads");
        HMS_MAP.put("ads-lite", "Ads");
        HMS_MAP.put("ml-computer-vision", "ML");
        HMS_MAP.put("location", "Location");
        HMS_MAP.put("awareness", "Awareness");
        HMS_MAP.put("maps", "Map");
        HMS_MAP.put("hianalytics", "Analytics");
        HMS_MAP.put("hwid", "Account");
        HMS_MAP.put("push", "Push");
        HMS_MAP.put("game", "Game");
        HMS_MAP.put("identity", "Identity");
        HMS_MAP.put("site", "Site");
        HMS_MAP.put("fido", "FIDO");
        HMS_MAP.put("wallet", "Wallet");
    }

    private List<HmsKitItem> begin = new ArrayList<>();

    private List<HmsKitItem> after = new ArrayList<>();

    public HmsKitList() {
    }

    /**
     * Parse each list
     *
     * @param hmsKitList hmsKit list
     * @param addDepList add dep list
     * @param delDepList delete dep list
     */
    public static void parseList(HmsKitList hmsKitList, List<String> addDepList, List<String> delDepList) {
        List<HmsKitItem> beginTemp = new ArrayList<>();
        List<HmsKitItem> afterTemp = new ArrayList<>();

        HmsKitItem hmsKitItem;
        for (int i = 0; i < hmsKitList.getBegin().size(); i++) {
            hmsKitItem = hmsKitList.getBegin().get(i);
            beginTemp.add(new HmsKitItem(hmsKitItem.getGroupId(), hmsKitItem.getArtifactId(), hmsKitItem.getVersion()));
        }
        for (int i = 0; i < hmsKitList.getAfter().size(); i++) {
            hmsKitItem = hmsKitList.getAfter().get(i);
            afterTemp.add(new HmsKitItem(hmsKitItem.getGroupId(), hmsKitItem.getArtifactId(), hmsKitItem.getVersion()));
        }

        for (int i = 0; i < hmsKitList.getBegin().size(); i++) {
            hmsKitItem = hmsKitList.getBegin().get(i);
            for (int j = 0; j < afterTemp.size(); j++) {
                if (afterTemp.get(j).isSame(hmsKitItem)) {
                    afterTemp.remove(j);
                    j--;
                }
            }
        }
        for (int i = 0; i < hmsKitList.getAfter().size(); i++) {
            hmsKitItem = hmsKitList.getAfter().get(i);
            for (int j = 0; j < beginTemp.size(); j++) {
                if (beginTemp.get(j).isSame(hmsKitItem)) {
                    beginTemp.remove(j);
                    j--;
                }
            }
        }
        log.info("beginTemp {} {}", beginTemp.size(), StringUtil.join(beginTemp, ","));
        log.info("afterTemp {} {}", afterTemp.size(), StringUtil.join(afterTemp, ","));
        artifactId2Kit(beginTemp, afterTemp, addDepList, delDepList);
        changMLtoMLgmsAndMLfirebase(addDepList);
        changMLtoMLgmsAndMLfirebase(delDepList);
        filterHmsKitsList(addDepList);
        filterHmsKitsList(delDepList);
    }

    private static void changMLtoMLgmsAndMLfirebase(List<String> hmsKitsList) {
        if (hmsKitsList.contains("ML")) {
            hmsKitsList.add("MLgms");
            hmsKitsList.add("MLfirebase");
            log.info("Replace ML with MLgms and MLfirebase");
        }
    }

    private static void filterHmsKitsList(List<String> hmsKitsList) {
        List<String> tempList = new ArrayList<>();
        tempList.addAll(hmsKitsList);
        hmsKitsList.clear();
        Set<String> supportKitInfos = PolicySettingDialog.getGaddHKitWhiteList();
        log.info("tempList kit list: {}", com.intellij.openapi.util.text.StringUtil.join(tempList, ","));
        log.info("hmsKitsList kit list: {}", com.intellij.openapi.util.text.StringUtil.join(hmsKitsList, ","));
        log.info("Support kit list: {}", com.intellij.openapi.util.text.StringUtil.join(supportKitInfos, ","));
        for (String item : tempList) {
            if (!StringUtil.isEmpty(item) && supportKitInfos.contains(item) && !hmsKitsList.contains(item)) {
                hmsKitsList.add(item);
            }
        }
        log.info("hmsKitsList list: {}", StringUtil.join(hmsKitsList, ","));
        return;
    }

    private static void artifactId2Kit(List<HmsKitItem> beginTemp, List<HmsKitItem> afterTemp, List<String> addDepList,
        List<String> delDepList) {
        // get remove kit
        String artifactId;
        for (int i = 0; i < beginTemp.size(); i++) {
            artifactId = beginTemp.get(i).getArtifactId();
            if (HMS_MAP.containsKey(artifactId) && !delDepList.contains(HMS_MAP.get(artifactId))) {
                delDepList.add(HMS_MAP.get(artifactId));
            }
        }
        // get add kit
        for (int i = 0; i < afterTemp.size(); i++) {
            artifactId = afterTemp.get(i).getArtifactId();
            if (HMS_MAP.containsKey(artifactId) && !delDepList.contains(HMS_MAP.get(artifactId))) {
                addDepList.add(HMS_MAP.get(artifactId));
            }
        }
        log.info("addDepList = {} {}", addDepList.size(), StringUtil.join(addDepList, ","));
        log.info("delDepList = {} {}", delDepList.size(), StringUtil.join(delDepList, ","));
        if (addDepList.contains("Ads") && delDepList.contains("Ads")) {
            addDepList.remove("Ads");
            delDepList.remove("Ads");
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
