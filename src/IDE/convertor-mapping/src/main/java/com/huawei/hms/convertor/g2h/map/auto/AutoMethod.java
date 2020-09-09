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

package com.huawei.hms.convertor.g2h.map.auto;

import com.huawei.hms.convertor.g2h.map.desc.Desc;

import com.google.gson.annotations.Expose;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * method that support automatic convert
 *
 * @since 2020-07-06
 */
@SuperBuilder
@Getter
@Slf4j
public class AutoMethod {
    @Expose
    private String oldMethodName;

    @Expose
    private String newMethodName;

    @Expose
    private List<String> paramTypes;

    @Expose
    private List<String> weakTypes;

    @Expose
    private Desc desc;

    @Builder.Default
    private List<String> oldParamTypes = new LinkedList<>();

    @Builder.Default
    private List<String> newParamTypes = new LinkedList<>();

    @Builder.Default
    private boolean readyToDelete = false;
}
