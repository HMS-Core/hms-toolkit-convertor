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

package com.huawei.generator.method.component;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.mirror.KClass;
import com.huawei.generator.exception.UnExpectedProcessException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Component for xapi generation
 *
 * @since 2020-03-24
 */
public final class StubComponent extends Component {
    public static final Component INSTANCE = new StubComponent();

    public StubComponent() {
        super("Z");
    }

    @Override
    public String retVarName() {
        throw new UnExpectedProcessException();
    }

    @Override
    public String getXMethodName() {
        throw new UnExpectedProcessException();
    }

    @Override
    public String toZMethodName(String xType) {
        throw new UnExpectedProcessException();
    }

    @Override
    public String zReturn() {
        throw new UnExpectedProcessException();
    }

    @Override
    public boolean isZType(String typeName) {
        throw new UnExpectedProcessException();
    }

    @Override
    public String x2Z(String xType) {
        throw new UnExpectedProcessException();
    }

    @Override
    public boolean isH() {
        throw new UnExpectedProcessException();
    }

    @Override
    public String getZClassWithXClass() {
        throw new UnExpectedProcessException();
    }

    @Override
    public String toX(String typeName) {
        throw new UnExpectedProcessException();
    }

    @Override
    public TypeNode toX(TypeNode zType) {
        throw new UnExpectedProcessException();
    }

    @Override
    public String zImpl() {
        throw new UnExpectedProcessException();
    }

    @Override
    public List<JMapping<JMethod>> wholeMapping(ClassNode xNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public boolean isZImplMethod(JMapping<JMethod> mapping, JClass def) {
        throw new UnExpectedProcessException();
    }

    @Override
    public boolean hasZ(JMapping<JMethod> mapping, JClass def) {
        throw new UnExpectedProcessException();
    }

    @Override
    public <T> T jMethod(JMapping<T> mapping) {
        throw new UnExpectedProcessException();
    }

    @Override
    public Map<String, KClass> getZClassList() {
        throw new UnExpectedProcessException();
    }

    @Override
    public TypeNode getZType(ClassNode node) {
        throw new UnExpectedProcessException();
    }

    @Override
    public String zName(JClass def) {
        throw new UnExpectedProcessException();
    }

    @Override
    public String zInstanceFieldName() {
        throw new UnExpectedProcessException();
    }

    @Override
    public boolean isMatching(JMapping mapping) {
        throw new UnExpectedProcessException();
    }

    @Override
    public boolean isMatching(JClass def) {
        throw new UnExpectedProcessException();
    }

    @Override
    public boolean needToDoBlockInZImpl() {
        return false;
    }

    @Override
    public String getInstancePrefix() {
        throw new UnExpectedProcessException();
    }

    @Override
    public String getZClassNameForDoc(XClassDoc classDocNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public String getZClassInfoForDoc(XClassDoc classDocNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public String getZMethodNameForDoc(XMethodDoc methodDocNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public String componentAttribute() {
        throw new UnExpectedProcessException();
    }

    @Override
    public String getZMethodInfoForDoc(XMethodDoc methodDocNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public List<String> getMethodDocNameAndInfo(XMethodDoc methodDocNode) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getFieldInfo(XFieldDoc fieldDoc) {
        StringBuilder sb = new StringBuilder();
        sb.append(fieldDoc.getDisplayGInfo());
        sb.append(fieldDoc.getDisplayHInfo());
        return sb.toString();
    }
}
