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

package com.huawei.hms.convertor.core.bi;

import com.huawei.hms.convertor.core.bi.bean.AnalyzeResultBean;
import com.huawei.hms.convertor.core.bi.bean.ApiBean;
import com.huawei.hms.convertor.core.bi.bean.ApiInfoBean;
import com.huawei.hms.convertor.core.bi.bean.BIDataBean;
import com.huawei.hms.convertor.core.bi.bean.BaseBIData;
import com.huawei.hms.convertor.core.bi.bean.KitInfoBean;
import com.huawei.hms.convertor.core.bi.bean.SourceInfoBean;
import com.huawei.hms.convertor.core.bi.enumration.BIActionEnum;
import com.huawei.hms.convertor.core.bi.enumration.ConversionStatusEnum;
import com.huawei.hms.convertor.core.engine.fixbot.model.MethodItem;
import com.huawei.hms.convertor.core.kits.KitsConstants;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionCacheManager;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;
import com.huawei.hms.convertor.core.result.summary.SummaryConstants;
import com.huawei.hms.convertor.openapi.ConversionCacheService;
import com.huawei.hms.convertor.util.Constant;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * bi trace data service
 *
 * @since 2020-04-02
 */
@Slf4j
@Getter
@Setter
public class BITraceService {
    private String newConversionBeginTime;

    private String jvmXmx;

    private String projectPath;

    public BITraceService(String projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * trace and report data
     *
     * @param action bi action
     * @param data data bean
     */
    void traceData(BIActionEnum action, BaseBIData data) {
        if (data == null || projectPath == null) {
            return;
        }
        BIDataBean biDataBean = BIDataBean.builder()
            .action(action.getValue())
            .timeStamp(getTimeStamp())
            .detail(data.toString())
            .build();

        invokeBISdk(action.getValue(), biDataBean.toString(), getProjectName());
    }

    SourceInfoBean getSourceInfo() {
        Map<String, String> detailData = SummaryCacheManager.getInstance().getShowData(projectPath);
        if (null == detailData) {
            return null;
        }
        String autoConvertNumG2H = detailData.get(SummaryConstants.SUPPORT_AUTO_COUNT_TOHMS);
        String manualConvertNumG2H = detailData.get(SummaryConstants.SUPPORT_MANUAL_COUNT_TOHMS);
        String autoConvertRateG2H = detailData.get(SummaryConstants.SUPPORT_RATE_TOHMS);
        String autoConvertNumGH = detailData.get(SummaryConstants.SUPPORT_AUTO_COUNT_ADDHMS);
        String manualConvertNumGH = detailData.get(SummaryConstants.SUPPORT_MANUAL_COUNT_ADDHMS);
        String autoConvertRateGH = detailData.get(SummaryConstants.SUPPORT_RATE_ADDHMS);

        TreeMap<String, List<MethodItem>> kitMethods = SummaryCacheManager.getInstance().getKit2Methods(projectPath);
        int totalNum = kitMethods.values().size();
        List<ApiBean> notSupportApiList = new ArrayList<>();
        List<KitInfoBean> kitInfoBeanList = new ArrayList<>();

        Set<Map.Entry<String, List<MethodItem>>> entrySet = kitMethods.entrySet();

        for (Map.Entry<String, List<MethodItem>> entry : entrySet) {
            int autoConvert = 0;
            int manualConvert = 0;
            int notSupport = 0;
            int totalSize = entry.getValue().size();
            if (totalSize == 0) {
                continue;
            }

            for (MethodItem method : entry.getValue()) {
                if (!method.isSupport()) {
                    notSupportApiList
                        .add(ApiBean.builder().apiName(method.getMethodName()).kit(entry.getKey()).build());
                    notSupport++;
                }

                if (ConvertType.AUTO.equals(method.getConvertStatus())) {
                    autoConvert++;
                } else {
                    manualConvert++;
                }
            }

            if (autoConvert == totalSize) {
                kitInfoBeanList.add(KitInfoBean.builder()
                    .name(entry.getKey())
                    .convertStatus(ConversionStatusEnum.AUTO.getValue())
                    .autoConversionRate(convertRate(autoConvert * 1.0 / totalSize))
                    .totalApiNum(String.valueOf(totalSize))
                    .build());
                continue;
            }
            if (manualConvert == totalSize) {
                kitInfoBeanList.add(KitInfoBean.builder()
                    .name(entry.getKey())
                    .convertStatus(ConversionStatusEnum.MANUAL.getValue())
                    .autoConversionRate(convertRate(0))
                    .totalApiNum(String.valueOf(totalSize))
                    .build());
                continue;
            }
            if (autoConvert < totalSize && autoConvert > 0) {
                kitInfoBeanList.add(KitInfoBean.builder()
                    .name(entry.getKey())
                    .convertStatus(ConversionStatusEnum.MIXED.getValue())
                    .autoConversionRate(convertRate(autoConvert * 1.0 / totalSize))
                    .totalApiNum(String.valueOf(totalSize))
                    .build());
                continue;
            }

            if (notSupport == totalSize) {
                kitInfoBeanList.add(KitInfoBean.builder()
                    .name(entry.getKey())
                    .convertStatus(ConversionStatusEnum.NOT_SUPPORT.getValue())
                    .autoConversionRate(convertRate(0))
                    .totalApiNum(String.valueOf(totalSize))
                    .build());
            }
        }

        int notSupportApiNum = notSupportApiList.size();

        List<ConversionItem> conversionItemList = ConversionCacheManager.getInstance().getAllConversions(projectPath);
        long invokeNum = conversionItemList.stream().filter(it -> isApiConversionItem(it.getKitName())).count();
        ApiInfoBean apiInfoBean = ApiInfoBean.builder()
            .autoConversionNumG2H(autoConvertNumG2H)
            .manualConversionNumG2H(manualConvertNumG2H)
            .autoConversionRateG2H(autoConvertRateG2H)
            .autoConversionNumGH(autoConvertNumGH)
            .manualConversionNumGH(manualConvertNumGH)
            .autoConversionRateGH(autoConvertRateGH)
            .totalNum(String.valueOf(totalNum))
            .invokeNum(String.valueOf(invokeNum))
            .notSupportConversionNum(String.valueOf(notSupportApiNum))
            .notSupportList(notSupportApiList)
            .build();

        return SourceInfoBean.builder().kitInfo(kitInfoBeanList).apiInfo(apiInfoBean).build();
    }

    private String convertRate(double value) {
        return new DecimalFormat(ProjectConstants.Common.PERCENT).format(value);
    }

    /**
     * get analyze result
     *
     * @return analyze result
     */
    AnalyzeResultBean getAnalyzeResult() {
        long apiConversionNum = 0;
        long apiAutoConversionNum = 0;
        long commonConversionNum = 0;
        long commonAutoConversionNum = 0;
        List<ConversionItem> conversionItemList =
            ConversionCacheService.getInstance().getAllConversions(projectPath);
        for (ConversionItem item : conversionItemList) {
            String kitNames = item.getKitName();
            if (kitNames.contains(KitsConstants.COMMON)) {
                commonAutoConversionNum = item.getConvertType().equals(ConvertType.AUTO) ? commonAutoConversionNum + 1
                    : commonAutoConversionNum;
                commonConversionNum++;
            }
            if (isApiConversionItem(kitNames)) {
                apiAutoConversionNum =
                    item.getConvertType().equals(ConvertType.AUTO) ? apiAutoConversionNum + 1 : apiAutoConversionNum;
                apiConversionNum++;
            }
        }
        return AnalyzeResultBean.builder()
            .apiConversionNum(String.valueOf(apiConversionNum))
            .apiAutoConversionNum(String.valueOf(apiAutoConversionNum))
            .apiManualConversionNum(String.valueOf(apiConversionNum - apiAutoConversionNum))
            .commonConversionNum(String.valueOf(commonConversionNum))
            .commonAutoConversionNum(String.valueOf(commonAutoConversionNum))
            .commonManualConversionNum(String.valueOf(commonConversionNum - commonAutoConversionNum))
            .build();
    }

    private boolean isApiConversionItem(String kitNames) {
        return kitNames.contains(KitsConstants.COMMON) && kitNames.contains(",")
            || !kitNames.contains(KitsConstants.COMMON) && kitNames.length() > 0;
    }

    private String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    void setJvmXmx(List<String> jvmOpt) {
        for (String opt : jvmOpt) {
            if (opt.contains(BIConstants.JVM_OPT_XMX)) {
                jvmXmx = opt.substring(BIConstants.JVM_XMX_VALUE_START_INDEX);
            }
        }
    }

    private void invokeBISdk(String action, String detail, String projectName) {
        try {
            Class biClass = Class.forName(BIConstants.BI_SDK_CLASS_NAME);
            Class<?>[] methodParameterTypes = new Class<?>[] {String.class, String.class, String.class, String.class};
            Method biMethod = biClass.getDeclaredMethod(BIConstants.BI_SDK_METHOD_NAME, methodParameterTypes);
            biMethod.invoke(null, action, detail, projectName, BIConstants.BI_SERVICE_ID);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log.debug("invoke method failed");
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.debug("access method failed ");
        }
    }

    private String getProjectName() {
        return projectPath.substring(projectPath.lastIndexOf(Constant.SEPARATOR) + 1);
    }
}
