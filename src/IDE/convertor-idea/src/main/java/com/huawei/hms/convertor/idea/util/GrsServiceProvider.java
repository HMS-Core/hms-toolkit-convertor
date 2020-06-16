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

import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.openapi.util.text.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * GRS Service Provider
 *
 * @since 2020-01-15
 */
public final class GrsServiceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrsServiceProvider.class);

    private static final String GET_COUNTRY_CODE_CLASS = "com.huawei.hms.common.util.CommonUtils";

    private static final String GET_COUNTRY_CODE_METHOD = "getCountryRegion";

    private static final String GET_GRS_URL_CLASS = "com.huawei.hms.common.util.KitsOverseasUtils";

    private static final String GET_DEFAULT_GRS_URL_CLASS = "com.huawei.deveco.common.grs.GrsProperties";

    private static final String GET_GRS_URL_METHOD = "getGrsUrls";

    private static final String GET_ALLIANCE_CLASS = "com.huawei.deveco.common.grs.GrsServiceImpl";

    private static final String GET_ALLIANCE_METHOD = "getUrl";

    private static final Class<?>[] GET_ALLIANCE_METHOD_PARAMETER_TYPES =
        new Class<?>[] {String.class, String.class, String.class, String[].class};

    private static final String GRS_ALLIANCE_SERVICE_NAME = "com.huawei.devecostudio.global";

    private static final String GRS_ALLIANCE_KEY = "alliance";

    /**
     * Privacy statement detection
     *
     * @return If you do not agree to the privacy statement, then return{@code false}；else，return{@code true}
     */
    public static String getGrsAllianceDomain() {
        String countryCode = getCountryCode();
        LOGGER.info("Country code: {}", countryCode);
        String[] grsUrls = getGrsUrls();
        LOGGER.info("GRS urls: {}", Arrays.toString(grsUrls));
        List<Object> parameters = new ArrayList<>();
        PlatformReflectInvoker.InvokeResult result = null;
        parameters.add(GRS_ALLIANCE_SERVICE_NAME);
        parameters.add(GRS_ALLIANCE_KEY);
        parameters.add(countryCode);
        parameters.add(grsUrls);
        try {
            result = PlatformReflectInvoker.invokeMethod(GET_ALLIANCE_CLASS, GET_ALLIANCE_METHOD,
                GET_ALLIANCE_METHOD_PARAMETER_TYPES, parameters);
        } catch (PlatformReflectInvoker.NetworkTimeoutException e) {
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("grs_failed"), null,
                Constant.PLUGIN_NAME, true);
        }

        // Platform SDK not found or platform interface returns abnormal, get default configuration
        if (null == result) {
            LOGGER.error("SDK[com.huawei.hmstoolkit:common] not found or return unexpected value");
            return getDefaultAllianceDomain();
        }
        Object returnValue = result.getReturnValue();
        if (result.isSdkLost() || Objects.isNull(returnValue) || !(returnValue instanceof String)) {
            LOGGER.error("SDK[com.huawei.hmstoolkit:common] not found or return unexpected value");
            return getDefaultAllianceDomain();
        }

        String allianceDomain = (String) returnValue;
        return StringUtil.isEmpty(allianceDomain) ? getDefaultAllianceDomain() : allianceDomain;
    }

    private static String getDefaultAllianceDomain() {
        return HmsConvertorBundle.message("default_alliance_domain");
    }

    private static String getCountryCode() {
        PlatformReflectInvoker.InvokeResult result =
            PlatformReflectInvoker.invokeStaticMethod(GET_COUNTRY_CODE_CLASS, GET_COUNTRY_CODE_METHOD);

        // Platform SDK not found or platform interface returns abnormal, get default configuration
        Object returnValue = result.getReturnValue();
        if (result.isSdkLost() || Objects.isNull(returnValue) || !(returnValue instanceof String)) {
            LOGGER.error("SDK[com.huawei.hmstoolkit:common] not found or return unexpected value");
            return "";
        }

        return (String) returnValue;
    }

    private static String[] getGrsUrls() {
        PlatformReflectInvoker.InvokeResult result =
            PlatformReflectInvoker.invokeStaticMethod(GET_GRS_URL_CLASS, GET_GRS_URL_METHOD);

        // Platform SDK not found or platform interface returns abnormal, get default configuration
        Object returnValue = result.getReturnValue();
        if (result.isSdkLost() || Objects.isNull(returnValue) || !(returnValue instanceof String[])) {
            LOGGER.error("SDK[com.huawei.hmstoolkit:common] not found or return unexpected value");
            return getDefaultGrsUrls();
        }

        return (String[]) returnValue;
    }

    private static String[] getDefaultGrsUrls() {
        PlatformReflectInvoker.InvokeResult result =
            PlatformReflectInvoker.invokeMethod(GET_DEFAULT_GRS_URL_CLASS, GET_GRS_URL_METHOD);

        // Platform SDK not found or platform interface returns abnormal, get default configuration
        Object returnValue = result.getReturnValue();
        if (result.isSdkLost() || Objects.isNull(returnValue) || !(returnValue instanceof String[])) {
            LOGGER.error("SDK[com.huawei.hmstoolkit:common-grs] not found or return unexpected value");
            return new String[] {};
        }

        return (String[]) returnValue;
    }
}
