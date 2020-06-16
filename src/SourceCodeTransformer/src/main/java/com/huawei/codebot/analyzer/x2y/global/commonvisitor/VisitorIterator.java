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

package com.huawei.codebot.analyzer.x2y.global.commonvisitor;

import com.google.common.collect.Lists;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.VariableInfo;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;
import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

class VisitorIterator {
    private Map<String, FieldInfo> fieldInfoMap;

    VisitorIterator() {
        fieldInfoMap = ClassMemberService.getInstance().getFieldInfoMap();
    }

    void loadSuperField(Stack<Map<String, VariableInfo>> varMaps, String classFullName) {
        List<TypeInfo> superTypes = InheritanceService.getAllSuperClassesAndInterfaces(classFullName);
        for (TypeInfo superType : Lists.reverse(superTypes)) {
            Map<String, VariableInfo> superFieldMap = new HashMap<>();
            varMaps.push(superFieldMap);
            for (Map.Entry<String, FieldInfo> entry : fieldInfoMap.entrySet()) {
                if (entry.getKey().startsWith(superType.getQualifiedName())) {
                    FieldInfo fieldInfo = entry.getValue();
                    VariableInfo variableInfo = new VariableInfo();
                    variableInfo.setName(fieldInfo.getName());
                    variableInfo.setOwnerClasses(fieldInfo.getOwnerClasses());
                    variableInfo.setPackageName(fieldInfo.getPackageName());
                    variableInfo.setType(fieldInfo.getType());
                    superFieldMap.put(variableInfo.getName(), variableInfo);
                }
            }
        }
    }

    VariableInfo getVarInfo(final Stack<Map<String, VariableInfo>> varMaps, final String name) {
        ListIterator<Map<String, VariableInfo>> it = varMaps.listIterator(varMaps.size());
        while (it.hasPrevious()) {
            Map<String, VariableInfo> map = it.previous();
            VariableInfo varInfo = map.get(name);
            if (varInfo != null) {
                return varInfo;
            }
        }
        return null;
    }
}
