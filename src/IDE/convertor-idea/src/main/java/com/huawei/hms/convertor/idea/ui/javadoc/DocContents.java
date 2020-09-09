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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * doc content
 *
 * @since 2020-08-17
 */
@Getter
@Setter
@Builder
public class DocContents {
    private String xmsContentTemp;

    private String hmsContentTemp;

    private String gmsContentTemp;

    public DocContents(String xmsContentTemp, String hmsContentTemp, String gmsContentTemp) {
        this.xmsContentTemp = xmsContentTemp;
        this.hmsContentTemp = hmsContentTemp;
        this.gmsContentTemp = gmsContentTemp;
    }
}
