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

package com.huawei.inquiry.utils;

import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.inquiry.InquiryEntrances;
import com.huawei.inquiry.docs.EntireDoc;
import com.huawei.inquiry.docs.ZClassDoc;

import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * util for javadoc
 *
 * @since 2020-07-25
 */
public class DocUtil {
    public static final String ERROR_IN_JSON = "error in jsons";

    public static final String DECLARATION = "# declaration! ";

    public static final String CANNOT_FIND = "# can not find ";

    private static final Logger LOGGER = LoggerFactory.getLogger(DocUtil.class);

    /**
     * judge a signature including class's, method's, field's whether belong to H.
     *
     * @param signature class's or method's or field's
     * @return true if the signature belongs to H
     */
    public static boolean isHType(String signature) {
        return signature.startsWith("com.huawei.hms") || signature.startsWith("com.huawei.hmf")
                || signature.startsWith("com.huawei.agconnect") || signature.startsWith("com.hw.passsdk")
                || signature.startsWith("com.huawei.wallet");
    }

    /**
     * judge a signature including class's, method's, field's whether belong to G.
     *
     * @param signature class's or method's or field's
     * @return true if the signature belongs to G
     */
    public static boolean isGType(String signature) {
        return signature.startsWith("com.google.android.gms") || signature.startsWith("com.google.firebase")
                || signature.startsWith("com.google.ads") || signature.startsWith("com.android.installreferrer")
                || signature.startsWith("com.google.android.libraries") || signature.startsWith("com.google.api");
    }

    /**
     * judge a signature including class's, method's, field's whether belong to X.
     *
     * @param signature class's or method's or field's
     * @return true if the signature belongs to X
     */
    public static boolean isXType(String signature) {
        return signature.startsWith("org.xms.f.") || signature.startsWith("org.xms.g.")
                || signature.startsWith("org.xms.installreferrer.");
    }

    /**
     * get the full package class name according to method's or field's signature.
     *
     * @param signature method's or field's
     * @return full package class name
     */
    public static String getParentName(String signature) {
        return MethodDocUtil.isMethod(signature) ? MethodDocUtil.getParentName(signature)
                : FieldDocUtil.getParentName(signature);
    }

    public static boolean isKitsScope(String signature) {
        List<String> pushPackageList = Arrays.asList(
            "org.xms.f.iid.", "org.xms.f.messaging.", "org.xms.g.iid.", "org.xms.g.gcm.",
            "com.google.android.gms.gcm.", "com.google.android.gms.iid.", "com.google.firebase.messaging.",
            "com.google.firebase.iid.",
            "com.huawei.hms.push.", "com.huawei.hms.mlsdkssaging.", "com.huawei.hms.aaid.HmsInstanceId",
            "com.huawei.hms.aaid.entity.AAIDResult", "com.huawei.hms.opendevice.",
            "com.huawei.hms.support.api.opendevice.OdidResult");
        List<String> walletPackageList = Arrays.asList(
            "org.xms.g.wallet.", "com.google.android.gms.wallet.",
            "com.huawei.hms.wallet.", "com.huawei.wallet.", "com.hw.passsdk.", "com.huawei.wallet.hmspass.service");
        List<String> locationPackageList = Arrays.asList(
            "org.xms.g.location.", "com.google.android.gms.location.", "com.huawei.hms.location.");

        List<String> allKitsPackagetList = new ArrayList<>();
        allKitsPackagetList.addAll(pushPackageList);
        allKitsPackagetList.addAll(walletPackageList);
        allKitsPackagetList.addAll(locationPackageList);
        for (String name : allKitsPackagetList) {
            if (signature.startsWith(name)) {
                return true;
            }
        }
        return false;
    }

    public static IllegalArgumentException jsonError(String message) {
        throw new IllegalArgumentException("jsons error for request " + message);
    }

    public static EntireDoc.STRATEGYTYPE getRequestType(String signature) {
        if (DocUtil.isGType(signature)) {
            return EntireDoc.STRATEGYTYPE.G;
        } else if (DocUtil.isHType(signature)) {
            return EntireDoc.STRATEGYTYPE.H;
        } else if (DocUtil.isXType(signature)) {
            return EntireDoc.STRATEGYTYPE.X;
        } else { // such as java.util.List
            return EntireDoc.STRATEGYTYPE.OTHER;
        }
    }

    // used in reading xms jsons about javadoc
    public static class MapTypeTokenX extends TypeToken<Map<String, XClassDoc>> {
    }

    // used in reading hms or gms jsons about javadoc
    public static class MapTypeTokenZ extends TypeToken<Map<String, ZClassDoc>> {
    }
}