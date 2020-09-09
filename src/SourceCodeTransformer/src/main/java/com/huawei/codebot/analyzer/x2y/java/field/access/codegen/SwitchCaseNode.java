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
package com.huawei.codebot.analyzer.x2y.java.field.access.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Switch Case Node
 *
 * @since 3.0.0.300
 */
public class SwitchCaseNode {
    private String className;

    private String packageName;

    private String filePath;

    private List<String> imports;

    private Map<String, Pair> instances;

    private Pair type;

    private SwitchCaseNode(String className, String packageName, String filePath, Map<String, Pair> instances,
            Pair type) {
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.imports = new ArrayList<>();
        this.instances = instances;
        this.type = type;
    }

    public static SwitchCaseNode create(String fileName, String packageName,
                                        String filePath, Map<String,Pair> instances, Pair type) {
        return new SwitchCaseNode(fileName, packageName, filePath, instances, type);
    }

    public List<String> imports() {
        return imports;
    }

    public String className() {
        return className;
    }

    public String filePath() {
        return filePath;
    }

    public Pair type() {
        return type;
    }

    public Map<String, Pair> instances() {
        return instances;
    }

    public String packageName() {
        return packageName;
    }

    public static class Pair {
        private String hmsType;
        private String gmsType;

        private Pair(String hmsType, String gmsType) {
            this.hmsType = hmsType;
            this.gmsType = gmsType;
        }

        public static Pair create(String hmsType, String gmsType) {
            return new Pair(hmsType, gmsType);
        }

        public String gmsType() {
            return gmsType;
        }

        public String hmsType() {
            return hmsType;
        }
    }
}
