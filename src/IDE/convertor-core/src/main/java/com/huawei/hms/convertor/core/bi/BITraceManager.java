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
import com.huawei.hms.convertor.core.bi.bean.BaseBIData;
import com.huawei.hms.convertor.core.bi.bean.SourceInfoBean;
import com.huawei.hms.convertor.core.bi.enumration.BIActionEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BI trace function manager
 *
 * @since 2020-04-13
 */
public final class BITraceManager {
    private static final BITraceManager INSTANCE = new BITraceManager();

    private Map<String, BITraceService> biInstanceMap = new HashMap<>();

    private BITraceManager() {
    }

    public static BITraceManager getInstance() {
        return INSTANCE;
    }

    /**
     * init bi trace service instance
     *
     * @param projectPath project path
     */
    public void init(String projectPath) {
        if (!biInstanceMap.containsKey(projectPath)) {
            biInstanceMap.put(projectPath, new BITraceService(projectPath));
        }
    }

    /**
     * trace data
     *
     * @param projectPath project path
     * @param action bi trace action
     * @param data bi trace data
     */
    public void traceData(String projectPath, BIActionEnum action, BaseBIData data) {
        getTraceServiceInstance(projectPath).traceData(action, data);
    }

    /**
     * get source info
     *
     * @param projectPath project path
     * @return project source info
     */
    public SourceInfoBean getSourceInfo(String projectPath) {
        return getTraceServiceInstance(projectPath).getSourceInfo();
    }

    /**
     * get analyze result
     *
     * @param projectPath project path
     * @return analyze result
     */
    public AnalyzeResultBean getAnalyzeResult(String projectPath) {
        return getTraceServiceInstance(projectPath).getAnalyzeResult();
    }

    /**
     * cache jvm option Xmx value
     *
     * @param projectPath project path
     * @param jvmOpt jvm Options
     */
    public void setJvmXmx(String projectPath, List<String> jvmOpt) {
        getTraceServiceInstance(projectPath).setJvmXmx(jvmOpt);
    }

    /**
     * get jvm option Xmx value
     *
     * @param projectPath project path
     * @return jvm option Xmx value
     */
    public String getJvmXmx(String projectPath) {
        return getTraceServiceInstance(projectPath).getJvmXmx();
    }

    private BITraceService getTraceServiceInstance(String projectPath) {
        if (!biInstanceMap.containsKey(projectPath)) {
            biInstanceMap.put(projectPath, new BITraceService(projectPath));
        }
        return biInstanceMap.get(projectPath);
    }

    /**
     * clear trace service instance
     *
     * @param projectPath project path
     */
    public void clearTraceServiceInstance(String projectPath) {
        biInstanceMap.remove(projectPath);
    }

    /**
     * cache conversion begin time
     *
     * @param projectPath project path
     * @param beginTime conversion begin time
     */
    public void setNewConversionBeginTime(String projectPath, String beginTime) {
        getTraceServiceInstance(projectPath).setNewConversionBeginTime(beginTime);
    }

    /**
     * get conversion begin time
     *
     * @param projectPath project path
     * @return conversion begin time
     */
    public String getNewConversionBeginTime(String projectPath) {
        return getTraceServiceInstance(projectPath).getNewConversionBeginTime();
    }

}
