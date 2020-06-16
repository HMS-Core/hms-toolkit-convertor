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

/**
 * BI Constant
 *
 * @since 2020-04-08
 */
public interface BIConstants {
    /**
     * start index to get subString from String "-Xmx1028m"
     * 1028m JVM Xmx
     */
    int JVM_XMX_VALUE_START_INDEX = 4;

    /**
     * jvm option xmx name
     */
    String JVM_OPT_XMX = "Xmx";

    /**
     * BI Service id
     */
    String BI_SERVICE_ID = "10001";

    /**
     * BI SDK function class name
     */
    String BI_SDK_CLASS_NAME = "com.huawei.hms.common.util.trace.TraceUtils";

    /**
     * BI SDK function method name
     */
    String BI_SDK_METHOD_NAME = "trace";
}
