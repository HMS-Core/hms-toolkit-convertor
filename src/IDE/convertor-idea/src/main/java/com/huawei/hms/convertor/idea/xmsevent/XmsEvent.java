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

package com.huawei.hms.convertor.idea.xmsevent;

import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.idea.util.HmsKitList;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.util.text.StringUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Xms event
 *
 * @since 2020-03-09
 */
@Getter
@Setter
@Slf4j
public class XmsEvent {
    private String hmsKitItemsStirng;

    private List<String> addHmsKits;

    private List<String> delHmsKits;

    private Map<String, String> kitMap;

    private boolean isValidEvent;

    public XmsEvent(String hmsKitItemsStirng) {
        addHmsKits = new ArrayList<>();
        delHmsKits = new ArrayList<>();
        kitMap = new HashMap<>();
        this.hmsKitItemsStirng = hmsKitItemsStirng;
        parseData();
    }

    public XmsEvent(boolean isValidEvent) {
        this.isValidEvent = isValidEvent;
    }

    private void parseData() {
        try {
            HmsKitList hmsKitList = JSON.parseObject(hmsKitItemsStirng, HmsKitList.class);
            HmsKitList.parseList(hmsKitList, addHmsKits, delHmsKits);
            log.info("addHmsKits {} {}", addHmsKits.size(), StringUtil.join(addHmsKits, ","));
            log.info("delHmsKits {} {}", delHmsKits.size(), StringUtil.join(delHmsKits, ","));
            changeToMap();
            if (kitMap.isEmpty()) {
                log.warn("Hms kits empty!");
                isValidEvent = false;
                return;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        isValidEvent = true;
    }

    private void changeToMap() {
        for (String kitName : addHmsKits) {
            kitMap.put(kitName, XmsConstants.XMS_KIT_ADD);
        }
        for (String kitName : delHmsKits) {
            kitMap.put(kitName, XmsConstants.XMS_KIT_REMOVE);
        }
        Iterator<Map.Entry<String, String>> it = kitMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            log.info("key= {} and value = {}", entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String toString() {
        String details = "addHmsKits size = " + addHmsKits.size() + " " + StringUtil.join(addHmsKits, ",");
        details +=
            System.lineSeparator() + "delHmsKits size = " + delHmsKits.size() + " " + StringUtil.join(delHmsKits, ",");
        return details;
    }
}
