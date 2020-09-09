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

package com.huawei.generator.gen;

/**
 * Some constants for javadoc
 *
 * @since 2020-07-09
 */
public class JavadocConstants {
    public static final String UNSUPPORTED_METHOD_INFO = "XMS does not provide this api";

    public static final String UNSUPPORTED_CLASS_INFO =
        "HMS api does not provide in this Class. More details about the related GMS api can be seen"
            + "in the reference below";

    public static final String SUPPORT_ENVIR_INFO =
        "Support running environments including both HMS and GMS which are chosen by users";

    public static final String BELOW_REFERENCE_INFO = "Below are the references of HMS apis and GMS apis respectively:";

    public static final String FIELD_CREATOR_INFO = "android.os.Parcelable.Creator.CREATOR a public CREATOR field that "
        + "generates instances of your Parcelable class from a Parcel";

    public static final String WITHOUT_IMPLEMENTATIONS = "Providing both HMS and GMS APIs without implementations";
}
