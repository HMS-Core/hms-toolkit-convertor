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
import com.huawei.hms.convertor.util.PropertyUtil;

import com.intellij.openapi.util.text.StringUtil;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;

/**
 * GRS service provider
 *
 * @since 2020-01-15
 */
@Slf4j
public final class GrsServiceProvider {
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
     * Get alliance domain from GRS
     *
     * @return Alliance domain which GRS provides, or default alliance domain.
     */
    public static String getGrsAllianceDomain() {
        String countryCode = getCountryCode();
        log.info("Country code: {}", countryCode);
        String[] grsUrls = getGrsUrls();
        log.info("GRS urls: {}", Arrays.toString(grsUrls));
        Object[] parameters = new Object[] {GRS_ALLIANCE_SERVICE_NAME, GRS_ALLIANCE_KEY, countryCode, grsUrls};
        PlatformReflectInvoker.InvokeResult result = null;
        try {
            result = PlatformReflectInvoker.invokeMethod(GET_ALLIANCE_CLASS, GET_ALLIANCE_METHOD,
                GET_ALLIANCE_METHOD_PARAMETER_TYPES, parameters);
        } catch (PlatformReflectInvoker.NetworkTimeoutException e) {
            log.warn("Get alliance domain from GRS failed, exception: {}.", e.getMessage());
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("grs_failed"), null,
                Constant.PLUGIN_NAME, true);
        }

        // Get default alliance domain if GRS failed
        if (result == null) {
            log.error("SDK[com.huawei.hmstoolkit:common] not found or return unexpected value");
            return getDefaultAllianceDomain();
        }
        Object returnValue = result.getReturnValue();
        if (result.isSdkNotFound() || Objects.isNull(returnValue) || !(returnValue instanceof String)) {
            log.error("SDK[com.huawei.hmstoolkit:common] not found or return unexpected value");
            return getDefaultAllianceDomain();
        }

        String allianceDomain = (String) returnValue;
        return StringUtil.isEmpty(allianceDomain) ? getDefaultAllianceDomain() : allianceDomain;
    }

    private static String getDefaultAllianceDomain() {
        return PropertyUtil.readProperty("default_alliance_domain");
    }

    private static String getCountryCode() {
        PlatformReflectInvoker.InvokeResult result =
            PlatformReflectInvoker.invokeStaticMethod(GET_COUNTRY_CODE_CLASS, GET_COUNTRY_CODE_METHOD);

        // Get default country code if GRS failed
        Object returnValue = result.getReturnValue();
        if (result.isSdkNotFound() || Objects.isNull(returnValue) || !(returnValue instanceof String)) {
            log.error("SDK[com.huawei.hmstoolkit:common] not found or return unexpected value");
            return "";
        }

        return (String) returnValue;
    }

    private static String[] getGrsUrls() {
        PlatformReflectInvoker.InvokeResult result =
            PlatformReflectInvoker.invokeStaticMethod(GET_GRS_URL_CLASS, GET_GRS_URL_METHOD);

        // Get default url if GRS failed
        Object returnValue = result.getReturnValue();
        if (result.isSdkNotFound() || Objects.isNull(returnValue) || !(returnValue instanceof String[])) {
            log.error("SDK[com.huawei.hmstoolkit:common] not found or return unexpected value");
            return getDefaultGrsUrls();
        }

        return (String[]) returnValue;
    }

    private static String[] getDefaultGrsUrls() {
        PlatformReflectInvoker.InvokeResult result =
            PlatformReflectInvoker.invokeMethod(GET_DEFAULT_GRS_URL_CLASS, GET_GRS_URL_METHOD);

        // Get default url if GRS failed
        Object returnValue = result.getReturnValue();
        if (result.isSdkNotFound() || Objects.isNull(returnValue) || !(returnValue instanceof String[])) {
            log.error("SDK[com.huawei.hmstoolkit:common-grs] not found or return unexpected value");
            return new String[] {};
        }

        return (String[]) returnValue;
    }
}
