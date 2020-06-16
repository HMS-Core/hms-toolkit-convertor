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

package com.huawei.generator.g2x.processor.map;

import com.huawei.generator.g2x.po.map.auto.Auto;
import com.huawei.generator.g2x.po.map.auto.AutoMethod;
import com.huawei.generator.g2x.po.map.manual.Manual;
import com.huawei.generator.g2x.po.map.manual.ManualMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is used for patching special cases of weaktypes. When source code engine fixes, it should be removed.
 * For details, we have two cases,
 * 1. when one in auto, N >= 1 in Manual, one in auto should use ParamTypes, and vice versa
 * 2. for Methods in BlackList, use the ParamTypes
 *
 * @since 2020-2-6
 */
public class MapPatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapPatcher.class);

    private static HashMap<String, MethodInfo> manualBlackList = new HashMap<>();

    private static HashMap<String, MethodInfo> autoBlackList = new HashMap<>();

    static {
        manualBlackList.put("com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions",
            new MethodInfo("com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions", null));
        manualBlackList.put("com.google.android.gms.ads.MobileAds.initialize",
            new MethodInfo("com.google.android.gms.ads.MobileAds.initialize", null));
        autoBlackList.put("com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions",
            new MethodInfo("com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions", null));
        autoBlackList.put("com.google.android.gms.ads.MobileAds.initialize",
            new MethodInfo("com.google.android.gms.ads.MobileAds.initialize", null));
    }

    static void patchMap(Auto xmsAuto, Manual xmsManual) {
        LOGGER.info("Patching G+H map");
        ManualMethod manualMethod = null;

        // detect the single weak-type method and reset weak-type in auto map
        for (AutoMethod tmpAutoMethod : xmsAuto.getAutoMethods()) {
            int inAuto = 0;
            for (AutoMethod autoMethodCopy : xmsAuto.getAutoMethods()) {
                if (tmpAutoMethod.getOldMethodName().equals(autoMethodCopy.getOldMethodName())) {
                    inAuto++;
                }
            }
            if ((inAuto == 1)) {
                replaceAutoTypes(xmsAuto, tmpAutoMethod);
            }
        }

        // detect the single weak-type method and reset weak-type in manual map
        for (ManualMethod tmpManualMethod : xmsManual.getManualMethods()) {
            int inManual = 0;
            for (ManualMethod manualMethodCopy : xmsManual.getManualMethods()) {
                String methodName = tmpManualMethod.getMethodName();
                String tempMethodName = manualMethodCopy.getMethodName();
                if (methodName.equals(tempMethodName)) {
                    inManual++;
                    manualMethod = tmpManualMethod;
                }
            }
            if (inManual == 1) {
                replaceManualTypes(xmsManual, manualMethod);
            }
        }

        // reset the weak-type by blacklist in auto
        for (AutoMethod autoMethod : xmsAuto.getAutoMethods()) {
            if (autoBlackList.containsKey(autoMethod.getOldMethodName())) {
                replaceAutoTypes(xmsAuto, autoMethod);
            }
        }

        // reset the weak-type by blacklist in manual
        for (ManualMethod tempManualMethod : xmsManual.getManualMethods()) {
            if (manualBlackList.containsKey(tempManualMethod.getMethodName())) {
                replaceManualTypes(xmsManual, tempManualMethod);
            }
        }
    }

    private static void replaceAutoTypes(Auto auto, AutoMethod autoMethod) {
        int index = auto.getAutoMethods().indexOf(autoMethod);
        AutoMethod method = auto.getAutoMethods().get(index);
        method.getWeakTypes().clear();
        method.getWeakTypes()
            .addAll(auto.getAutoMethods().get(auto.getAutoMethods().indexOf(autoMethod)).getParamTypes());
        method.getDesc()
            .setMethodName(reBuildMDescMethodName(method.getDesc().getMethodName(), method.getParamTypes()));
        auto.getAutoMethods().set(index, method);
    }

    private static void replaceManualTypes(Manual manual, ManualMethod manualMethod) {
        int index = manual.getManualMethods().indexOf(manualMethod);
        ManualMethod method = manual.getManualMethods().get(index);
        method.getWeakTypes().clear();
        method.getWeakTypes().addAll(method.getParamTypes());
        method.getDesc()
            .setMethodName(reBuildMDescMethodName(method.getDesc().getMethodName(), method.getParamTypes()));
        manual.getManualMethods().set(index, method);
    }

    private static String reBuildMDescMethodName(String oldMethodName, List<String> paramList) {
        if (paramList.size() < 1) {
            return oldMethodName;
        }
        StringBuilder builder = new StringBuilder();
        String r = oldMethodName.split("\\(")[0] + "(";
        builder.append(r);
        String params = paramList.stream().collect(Collectors.joining(","));
        builder.append(params.replace("#BUILT_IN.", ""));
        builder.append(")");
        return builder.toString();
    }

    // white list for code engine, should be removed in the future
    static List<String> findSameMethod(List<String> autoGMethodList, List<String> manualGMethodList) {
        List<String> returnList = new LinkedList<>();
        autoGMethodList.retainAll(manualGMethodList);
        for (String s : autoGMethodList) {
            if (!returnList.contains(s)) {
                returnList.add(s);
            }
        }
        return returnList;
    }
}
