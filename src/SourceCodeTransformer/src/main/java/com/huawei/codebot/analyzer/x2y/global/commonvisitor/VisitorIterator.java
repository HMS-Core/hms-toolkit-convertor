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

import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.VariableInfo;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;
import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

class VisitorIterator {
    private Map<String, FieldInfo> fieldInfoMap;

    VisitorIterator() {
        fieldInfoMap = ClassMemberService.getInstance().getFieldInfoMap();
    }

    void loadSuperField(Stack<Map<String, VariableInfo>> varMaps, String classFullName) {
        Set<TypeInfo> superTypes = InheritanceService.getAllSuperClassesAndInterfaces(classFullName);
        LinkedList<TypeInfo> list = new LinkedList<>(superTypes);
        Iterator<TypeInfo> itr = list.descendingIterator();
        while (itr.hasNext()) {
            TypeInfo superType = itr.next();
            Map<String, VariableInfo> superFieldMap = new ConcurrentHashMap<>();
            varMaps.push(superFieldMap);
            fieldInfoMap.entrySet().parallelStream()
                    .filter(entry -> entry.getKey().startsWith(superType.getQualifiedName()))
                    .forEach(entry -> {
                        FieldInfo fieldInfo = entry.getValue();
                        if (fieldInfo != null) {
                            VariableInfo variableInfo = new VariableInfo(fieldInfo);
                            superFieldMap.put(variableInfo.getName(), variableInfo);
                        }
                    });
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
