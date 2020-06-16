/*
 * Copyright (C) 2019. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the Apache License, Version 2.0 (the "License").
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * Apache License, Version 2.0  for more details.
 */

package com.huawei.generator.g2x.processor.map;

import java.util.List;

/**
 * Class for storing method info
 *
 * @since 2020-04-07
 */
public class MethodInfo {
    private String fullName;

    private List<String> paramTypes;

    MethodInfo(String fullName, List<String> paramTypes) {
        this.fullName = fullName;
        this.paramTypes = paramTypes;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }
}
