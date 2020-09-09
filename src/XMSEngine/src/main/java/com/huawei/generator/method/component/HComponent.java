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
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.mirror.KClass;
import com.huawei.generator.mirror.KClassReader;
import com.huawei.generator.mirror.KClassUtils;
import com.huawei.generator.utils.TypeUtils;
import com.huawei.generator.utils.XMSUtils;

import java.util.List;
import java.util.Map;

/**
 * Class for HComponent
 *
 * @since 2020-02-19
 */
public final class HComponent extends Component {
    public HComponent() {
        super("Z");
    }

    public HComponent(String identifier) {
        super(identifier);
    }

    @Override
    public String getXMethodName() {
        return AstConstants.GET_XMS_BY_HMS;
    }

    @Override
    public String toZMethodName(String xType) {
        return XMSUtils.xtoH(xType);
    }

    @Override
    public boolean isZType(String typeName) {
        return TypeUtils.isHmsType(typeName);
    }

    @Override
    public String x2Z(String xType) {
        TypeNode tn = TypeNode.create(XMSUtils.xtoH(xType));
        if (tn == null) {
            return null;
        }
        return tn.getTypeName();
    }

    @Override
    public boolean isH() {
        return true;
    }

    @Override
    public String getZClassWithXClass() {
        return AstConstants.GET_HMS_WITH_XMS;
    }

    @Override
    public String toX(String typeName) {
        return XMSUtils.htoX(typeName);
    }

    @Override
    public TypeNode toX(TypeNode zType) {
        String xType = toX(zType.getTypeName());
        return TypeNode.create(xType);
    }

    @Override
    public List<JMapping<JMethod>> wholeMapping(ClassNode xNode) {
        return KClassUtils.getHHierarchicalMethodMapping(xNode);
    }

    @Override
    public boolean isZImplMethod(JMapping<JMethod> mapping, JClass def) {
        return hasZ(mapping, def);
    }

    @Override
    public boolean hasZ(JMapping<JMethod> mapping, JClass def) {
        return mapping.isMatching() || mapping.isRedundant();
    }

    @Override
    public <T> T jMethod(JMapping<T> mapping) {
        return mapping.h();
    }

    @Override
    public Map<String, KClass> getZClassList() {
        return KClassReader.INSTANCE.getHClassList();
    }

    @Override
    public TypeNode getZType(ClassNode node) {
        return node.getHType();
    }

    @Override
    public String zName(JClass def) {
        return def.hName();
    }

    @Override
    public boolean isMatching(JMapping mapping) {
        return mapping.isMatching();
    }

    @Override
    public boolean isMatching(JClass def) {
        return !zName(def).isEmpty();
    }

    @Override
    public boolean needToDoBlockInZImpl() {
        return true;
    }

    @Override
    public String getInstancePrefix() {
        return "getHInstance";
    }

    @Override
    public String getZClassNameForDoc(XClassDoc classDocNode) {
        return classDocNode.getHClassName();
    }

    @Override
    public String getZClassInfoForDoc(XClassDoc classDocNode) {
        return classDocNode.getHClassInfo();
    }

    @Override
    public String getZMethodNameForDoc(XMethodDoc methodDocNode) {
        return methodDocNode.getHName();
    }

    @Override
    public String getZMethodInfoForDoc(XMethodDoc methodDocNode) {
        return methodDocNode.getHmsInfo();
    }

    @Override
    public String componentAttribute() {
        return "H";
    }

    @Override
    public String getFieldInfo(XFieldDoc fieldDoc) {
        return fieldDoc.getDisplayHInfo();
    }
}
