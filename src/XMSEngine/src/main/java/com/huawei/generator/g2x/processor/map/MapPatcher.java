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

/**
 * This class is used for patching special cases of weaktypes. When source code engine fixes, it should be removed.
 * For details, we have two cases,
 * 1. when one in auto, N >= 1 in Manual, one in auto should use ParamTypes, and vice versa
 * 2. for Methods in BlockList, use the ParamTypes
 *
 * @since 2020-2-6
 */
public class MapPatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapPatcher.class);

    private static HashMap<String, MethodInfo> manualBlockList = new HashMap<>();

    private static HashMap<String, MethodInfo> autoBlockList = new HashMap<>();

    static {
        manualBlockList.put("com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions",
            new MethodInfo("com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions", null));
        manualBlockList.put("com.google.android.gms.ads.MobileAds.initialize",
            new MethodInfo("com.google.android.gms.ads.MobileAds.initialize", null));
        autoBlockList.put("com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions",
            new MethodInfo("com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions", null));
        autoBlockList.put("com.google.android.gms.ads.MobileAds.initialize",
            new MethodInfo("com.google.android.gms.ads.MobileAds.initialize", null));
        autoBlockList.put("com.google.firebase.perf.FirebasePerformance.newHttpMetric",
            new MethodInfo("com.google.firebase.perf.FirebasePerformance.newHttpMetric", null));
    }

    public static void patchMap(Auto auto, Manual manual) {
        LOGGER.info("Patching G+H map");
        ManualMethod manualMethod = null;

        // detect the single weaktype method and reset weaktype in auto map
        for (AutoMethod autoMethod : auto.getAutoMethods()) {
            int inAuto = 0;
            for (AutoMethod autoMethodh : auto.getAutoMethods()) {
                if (autoMethod.getOldMethodName().equals(autoMethodh.getOldMethodName())) {
                    inAuto++;
                }
            }
            if ((inAuto == 1)) {
                replaceAutoTypes(auto, autoMethod);
            }
        }

        // detect the single weaktype method and reset weaktype in manual map
        for (ManualMethod manualMethodTemp : manual.getManualMethods()) {
            int inmanual = 0;
            for (ManualMethod tmpManualMethod : manual.getManualMethods()) {
                String methodName = manualMethodTemp.getMethodName();
                String tempMethodName = tmpManualMethod.getMethodName();
                if (methodName.equals(tempMethodName)) {
                    inmanual++;
                    manualMethod = manualMethodTemp;
                }
            }
            if (inmanual == 1) {
                replaceManualTypes(manual, manualMethod);
            }
        }

        // reset the weaktype by blocklist in auto
        for (AutoMethod autoMethod : auto.getAutoMethods()) {
            if (autoBlockList.containsKey(autoMethod.getOldMethodName())) {
                replaceAutoTypes(auto, autoMethod);
            }
        }
        // reset the weaktype by blocklist in manual
        for (ManualMethod tempManualMethod : manual.getManualMethods()) {
            if (manualBlockList.containsKey(tempManualMethod.getMethodName())) {
                replaceManualTypes(manual, tempManualMethod);
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

    public static String reBuildMDescMethodName(String oldMethodName, List<String> paramList) {
        if (paramList.size() < 1) {
            return oldMethodName;
        }
        StringBuilder builder = new StringBuilder(oldMethodName.split("\\(")[0] + "(");
        int index = 0;
        for (; index < paramList.size(); index++) {
            String str = paramList.get(index);
            builder.append(str.replace("#BUILT_IN.", "")).append(",");
        }
        if (paramList.size() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(")");
        return builder.toString();
    }

    // trust list for code engine, should be removed in the future
    public static List<String> findSameMethod(List<String> autoGMethodList, List<String> manualGMethodList) {
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
