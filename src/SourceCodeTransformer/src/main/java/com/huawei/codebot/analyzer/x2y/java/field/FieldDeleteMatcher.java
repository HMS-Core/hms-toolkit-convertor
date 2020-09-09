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

package com.huawei.codebot.analyzer.x2y.java.field;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;

import java.util.List;
import java.util.Set;

/**
 * A matcher used to match field that need to be deleted
 *
 * @since 2020-04-16
 */
public class FieldDeleteMatcher extends FieldMatcher {
    private Set<String> changePatterns;

    public FieldDeleteMatcher(Set<String> fieldSet, JavaLocalVariablesInMethodVisitor visitor) {
        super(visitor);
        this.changePatterns = fieldSet;
    }

    @Override
    protected FieldChangePattern getFieldChangePattern(String qualifier, String simpleName) {
        if (qualifier != null && simpleName != null) {
            StringBuilder sb = new StringBuilder(qualifier).append(".").append(simpleName);
            if (changePatterns.contains(sb.toString())) {
                // this changePattern set directly contains this field qualified name, it means this field need to be
                // deleted, so we return a FieldChangePattern which oldFieldName equals to newFieldName
                FieldName oldFieldName = new FieldName(qualifier, simpleName);
                FieldName newFieldName = new FieldName(qualifier, simpleName);
                return new FieldChangePattern(oldFieldName, newFieldName);
            } else {
                // if this changePattern set doesn't directly contains this field qualified name, we need to check
                // whether this field's super type has a shadow field
                Set<TypeInfo> superClassAndInterface = InheritanceService.getAllSuperClassesAndInterfaces(qualifier);
                for (TypeInfo superClass : superClassAndInterface) {
                    sb.setLength(0);
                    sb.append(superClass.getQualifiedName()).append(".").append(simpleName);
                    if (changePatterns.contains(sb.toString())) {
                        FieldName oldFieldName = new FieldName(qualifier, simpleName);
                        FieldName newFieldName = new FieldName(qualifier, simpleName);
                        FieldName actualFieldName = new FieldName(sb.toString());
                        FieldChangePattern result = new FieldChangePattern(oldFieldName, newFieldName);
                        result.setActualFieldName(actualFieldName);
                        return result;
                    }
                }
            }
        }
        return null;
    }
}
