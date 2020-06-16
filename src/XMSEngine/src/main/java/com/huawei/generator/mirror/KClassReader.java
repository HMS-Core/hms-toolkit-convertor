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

package com.huawei.generator.mirror;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Reading from gms.json,hms.json and android.json.
 *
 * @since 2019-12-01
 */
public enum KClassReader {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(KClassReader.class);

    private final File GMS_TEST_JSON = new File(System.getProperty("user.dir") + File.separator + "gms.json");

    private final File HMS_TEST_JSON = new File(System.getProperty("user.dir") + File.separator + "hms.json");

    private final File ANDROID_TEST_JSON = new File(System.getProperty("user.dir") + File.separator + "android.json");

    private Map<String, KClass> gClassList;

    private Map<String, KClass> hClassList;

    private Map<String, KClass> androidClassList;

    /**
     * Reset all list loaded from json.
     */
    KClassReader() {
        String gClassJsonPath = "/mirror/gms.json";
        String hClassJsonPath = "/mirror/hms.json";
        String androidJsonPath = "/mirror/android.json";

        gClassList =
            new Gson().fromJson(getJsonReader(gClassJsonPath), new TypeToken<Map<String, KClass>>() {}.getType());
        hClassList =
            new Gson().fromJson(getJsonReader(hClassJsonPath), new TypeToken<Map<String, KClass>>() {}.getType());

        androidClassList =
            new Gson().fromJson(getJsonReader(androidJsonPath), new TypeToken<Map<String, KClass>>() {}.getType());

        // add for test
        if (GMS_TEST_JSON.exists() && HMS_TEST_JSON.exists() && ANDROID_TEST_JSON.exists()) {
            readExternalJson();
        }

        // Set each KMethod's KClass
        gClassList.values().forEach(kClass -> kClass.getMethods().forEach(kMethod -> kMethod.setClass(kClass)));
        hClassList.values().forEach(kClass -> kClass.getMethods().forEach(kMethod -> kMethod.setClass(kClass)));
        androidClassList.values().forEach(kClass -> kClass.getMethods().forEach(kMethod -> kMethod.setClass(kClass)));
    }

    public Map<String, KClass> getAndroidClassList() {
        return androidClassList;
    }

    public Map<String, KClass> getHClassList() {
        return hClassList;
    }

    private JsonReader getJsonReader(String path) {
        InputStream inputStream = getClass().getResourceAsStream(path);
        return new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public Map<String, KClass> getGClassList() {
        return gClassList;
    }

    private void readExternalJson() {
        try (InputStreamReader gIns = new InputStreamReader(new FileInputStream(GMS_TEST_JSON), StandardCharsets.UTF_8);
            InputStreamReader hIns = new InputStreamReader(new FileInputStream(HMS_TEST_JSON), StandardCharsets.UTF_8);
            InputStreamReader aIns =
                new InputStreamReader(new FileInputStream(ANDROID_TEST_JSON), StandardCharsets.UTF_8)) {
            gClassList = new Gson().fromJson(gIns, new TypeToken<Map<String, KClass>>() {}.getType());
            hClassList = new Gson().fromJson(hIns, new TypeToken<Map<String, KClass>>() {}.getType());
            androidClassList = new Gson().fromJson(aIns, new TypeToken<Map<String, KClass>>() {}.getType());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
