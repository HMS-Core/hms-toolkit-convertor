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

package com.huawei.codebot.analyzer.x2y.global;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * GlobalSettings when processing convertion
 *
 * @since 3.0.1
 */
public class GlobalSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettings.class);
    // this convertion is for SDK or Not
    private static boolean isSDK = false;
    // this convertion is for Xms or Not
    private static boolean isWiseHub = false;
    // product flavour for only g
    private static boolean isOnlyG = false;
    // product flavour for only h
    private static boolean isOnlyH = false;
    // there is Application class, it is true
    private static boolean hasApplication = false;
    // there need classloader pattern, it is true
    private static boolean needClassloader = false;
    // record Application class path, using it in XmlModificationChanger
    private static String appFilePath = "";
    // record main module name
    private static String mainModuleName = "";
    // a temp record to handle product Flavour
    public static final Set<String> SET = Collections.unmodifiableSet(new HashSet<String>() {
        {
            add("com.google.firebase.messaging.FirebaseMessagingService");
            add("com.google.android.gms.maps.MapFragment");
            add("com.google.android.gms.maps.StreetViewPanoramaFragment");
            add("com.google.android.gms.maps.SupportMapFragment");
            add("com.google.android.gms.maps.SupportStreetViewPanoramaFragment");
        }
    });

    /**
     * Determine if it is a HMS type.
     *
     * @param target target class name
     * @return if is hms type return true, otherwise false.
     */
    public static boolean isHmsType(String target) {
        if (StringUtils.isEmpty(target)){
            return false;
        }
        if ((target.startsWith("com.huawei.hms") || target.startsWith("com.huawei.hmf")
                || target.startsWith("com.huawei.agconnect"))) {
            return true;
        }
        return false;
    }

    /**
     * Determine if it is a GMS type.
     *
     * @param target target class name
     * @return if is gms type return true, otherwise false.
     */
    public static boolean isGmsType(String target) {
        if (StringUtils.isEmpty(target)){
            return false;
        }
        if ((target.startsWith("com.google.android.gms")
                || target.startsWith("com.google.firebase")
                || target.startsWith("com.google.ads")
                || target.startsWith("com.android.installreferrer")
                || target.startsWith("com.google.android.libraries")
                || target.startsWith("com.google.api"))) {
            return true;
        }
        return false;
    }

    /**
     * serialize globalsetting
     *
     * @return a map describe all public static fields of GlobleSetting,class
     */
    public static HashMap<String, Object> toMap() {
        HashMap<String, Object> globalSetting = new HashMap<>();
        Field[] fields = GlobalSettings.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                // static 00001000b
                int staticFlag = 8;
                // final 00010000b
                int finalFlag = 16;
                // generate the map continues all static field which is not final.
                if ((field.getModifiers() & staticFlag) == staticFlag
                        && (field.getModifiers() & finalFlag) != finalFlag) {
                    globalSetting.put(field.getName(), field.get(null));
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("unexpect field dump");
            }
        }
        return globalSetting;
    }

    public static boolean isWiseHub() {
        return isWiseHub;
    }

    public static void setIsWiseHub(boolean isWiseHub) {
        GlobalSettings.isWiseHub = isWiseHub;
    }

    public static boolean isIsSDK() {
        return isSDK;
    }

    public static void setIsSDK(boolean isSDK) {
        GlobalSettings.isSDK = isSDK;
    }

    public static boolean isIsOnlyG() {
        return isOnlyG;
    }

    public static void setIsOnlyG(boolean isOnlyG) {
        GlobalSettings.isOnlyG = isOnlyG;
    }

    public static boolean isIsOnlyH() {
        return isOnlyH;
    }

    public static void setIsOnlyH(boolean isOnlyH) {
        GlobalSettings.isOnlyH = isOnlyH;
    }

    public static boolean isHasApplication() {
        return hasApplication;
    }

    public static void setHasApplication(boolean hasApplication) {
        GlobalSettings.hasApplication = hasApplication;
    }

    public static boolean isNeedClassloader() {
        return needClassloader;
    }

    public static void setNeedClassloader(boolean needClassloader) {
        GlobalSettings.needClassloader = needClassloader;
    }

    public static String getAppFilePath() {
        return appFilePath;
    }

    public static void setAppFilePath(String appFilePath) {
        GlobalSettings.appFilePath = appFilePath;
    }

    public static String getMainModuleName() {
        return mainModuleName;
    }

    public static void setMainModuleName(String mainModuleName) {
        GlobalSettings.mainModuleName = mainModuleName;
    }
}
