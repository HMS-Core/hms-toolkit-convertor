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

package com.huawei.codebot.analyzer.x2y.global.service;

import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.MethodInfo;
import com.huawei.codebot.analyzer.x2y.global.java.ClassMemberAnalyzer;
import com.huawei.codebot.framework.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassMemberService
 * Provide basic capabilities on ClassInfo FieldInfo MethodInfo
 *
 * @since 2019-07-14
 */
public class ClassMemberService {
    private static ClassMemberService instance;

    private ClassMemberService() {
    }

    public static synchronized ClassMemberService getInstance() {
        if (instance == null) {
            instance = new ClassMemberService();
        }
        return instance;
    }

    /**
     * Get Class information and return by map
     */
    public Map<String, ClassInfo> getClassInfoMap() {
        Map<String, ClassInfo> result = (Map<String, ClassInfo>) Context.getContext().getContextMap()
                .get(ClassMemberAnalyzer.class, "ClassInfo");
        if (result == null) {
            result = new HashMap<>();
            putClassInfoMap(result);
        }
        return result;
    }

    /**
     * incoming class information and store it into Context
     */
    public void putClassInfoMap(Map<String, ClassInfo> classInfoMap) {
        if (Context.getContext().getContextMap().get(ClassMemberAnalyzer.class, "ClassInfo") == null) {
            Context.getContext().getContextMap().put(ClassMemberAnalyzer.class, "ClassInfo", classInfoMap);
        }
    }

    /**
     * Get Field information and return by map
     */
    public Map<String, FieldInfo> getFieldInfoMap() {
        Map<String, FieldInfo> result = (Map<String, FieldInfo>) Context.getContext().getContextMap()
                .get(ClassMemberAnalyzer.class, "FieldInfo");
        if (result == null) {
            result = new HashMap<>();
            putFieldInfoMap(result);
        }
        return result;
    }

    /**
     * incoming field information and store it into Context
     */
    public void putFieldInfoMap(Map<String, FieldInfo> fieldInfoMap) {
        if (Context.getContext().getContextMap().get(ClassMemberAnalyzer.class, "FieldInfo") == null) {
            Context.getContext().getContextMap().put(ClassMemberAnalyzer.class, "FieldInfo", fieldInfoMap);
        }
    }

    /**
     * Get Method information and return by map
     */
    public Map<String, List<MethodInfo>> getMethodInfoMap() {
        Map<String, List<MethodInfo>> result = (Map<String, List<MethodInfo>>) Context.getContext().getContextMap()
                .get(ClassMemberAnalyzer.class, "MethodInfo");
        if (result == null) {
            result = new HashMap<>();
            putMethodInfoMap(result);
        }
        return result;
    }

    /**
     * incoming method information and store it into Context
     */
    public void putMethodInfoMap(Map<String, List<MethodInfo>> methodInfoMap) {
        if (Context.getContext().getContextMap().get(ClassMemberAnalyzer.class, "MethodInfo") == null) {
            Context.getContext().getContextMap().put(ClassMemberAnalyzer.class, "MethodInfo", methodInfoMap);
        }
    }

}
