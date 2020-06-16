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

import static com.huawei.generator.utils.XMSUtils.shouldNotReachHere;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.mirror.KClass;

import java.util.List;
import java.util.Map;

/**
 * Component representing Obj, is used for G+Obj generation.
 *
 * @since 2020-03-25
 */
public class ObjComponent extends Component {
    public ObjComponent() {
        this("H");
    }

    private ObjComponent(String identifier) {
        super(identifier);
    }

    @Override
    public String getXMethodName() {
        throw shouldNotReachHere();
    }

    @Override
    public String toZMethodName(String xType) {
        throw shouldNotReachHere();
    }

    @Override
    public boolean isZType(String typeName) {
        throw shouldNotReachHere();
    }

    @Override
    public String x2Z(String xType) {
        throw shouldNotReachHere();
    }

    @Override
    public boolean isH() {
        return true;
    }

    @Override
    public String getZClassWithXClass() {
        throw shouldNotReachHere();
    }

    @Override
    public String toX(String typeName) {
        throw shouldNotReachHere();
    }

    @Override
    public TypeNode toX(TypeNode zType) {
        throw shouldNotReachHere();
    }

    @Override
    public List<JMapping<JMethod>> wholeMapping(ClassNode xNode) {
        throw shouldNotReachHere();
    }

    @Override
    public boolean isZImplMethod(JMapping<JMethod> mapping, JClass def) {
        throw shouldNotReachHere();
    }

    @Override
    public boolean hasZ(JMapping<JMethod> mapping, JClass def) {
        return false;
    }

    @Override
    public <T> T jMethod(JMapping<T> mapping) {
        throw shouldNotReachHere();
    }

    @Override
    public Map<String, KClass> getZClassList() {
        throw shouldNotReachHere();
    }

    @Override
    public TypeNode getZType(ClassNode node) {
        return TypeNode.OBJECT_TYPE;
    }

    @Override
    public String zName(JClass def) {
        return AstConstants.OBJECT;
    }

    @Override
    public boolean isMatching(JMapping mapping) {
        return false;
    }

    @Override
    public boolean isMatching(JClass def) {
        return false;
    }

    @Override
    public boolean needToDoBlockInZImpl() {
        return false;
    }

    @Override
    public String getInstancePrefix() {
        return "getHInstance";
    }
}
