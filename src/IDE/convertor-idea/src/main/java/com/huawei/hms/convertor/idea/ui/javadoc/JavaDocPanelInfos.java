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

package com.huawei.hms.convertor.idea.ui.javadoc;

import com.huawei.inquiry.docs.EntireDoc;

import lombok.Getter;
import lombok.Setter;

/**
 * infos Object
 *
 * @since 2020-07-29
 */
@Getter
@Setter
public class JavaDocPanelInfos {
    private String fullApi;

    private EntireDoc entireDoc;

    private EntireDoc.SCOPETYPE type;

    private boolean privateType;

    public JavaDocPanelInfos(String fullApi, EntireDoc entireDoc, EntireDoc.SCOPETYPE type, boolean isPrivate) {
        this.fullApi = fullApi;
        this.entireDoc = entireDoc;
        this.type = type;
        privateType = isPrivate;
    }
}
