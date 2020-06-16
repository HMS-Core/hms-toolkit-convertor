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

package com.huawei.generator.ast.custom;

import static com.huawei.generator.gen.AstConstants.XMS_ENUM;
import static com.huawei.generator.gen.AstConstants.XMS_GETTABLE;
import static com.huawei.generator.gen.AstConstants.XMS_OBJECT;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A kind of ClassNode that represents a X adapter class.
 *
 * @since 2020-02-25
 */
public class XAdapterClassNode extends XClassNode {
    // A X adapter class corresponds to a json definition
    private JClass def;

    public void setDef(JClass def) {
        this.def = def;
    }

    public JClass getDefinition() {
        return def;
    }

    @Override
    public XAdapterClassNode normalize() {
        toX();
        normalizeXSupers();
        super.normalize();
        return this;
    }

    private void toX() {
        TypeNode superClass = TypeNode.create(superName(), false);
        if (superClass == null) {
            setSuperName("");
        } else if (getGType().getGenericType() != null) {
            setSuperName(superClass.toXWithGenerics(getGType().getGenericType()).toString());
        } else {
            setSuperName(superClass.toX().toString());
        }
        renameInterfaces();
    }

    private void normalizeXSupers() {
        if (isAnnotation()) {
            return;
        }
        if (isInterface()) {
            return;
        }
        if (isEnum()) {
            setSuperName(XMS_ENUM);
            return;
        }
        if (superName().isEmpty() || superName().equals(AstConstants.OBJECT)) {
            setSuperName(XMS_OBJECT);
            return;
        }
        if (!XMSUtils.isX(superName())) {
            interfaces().add(XMS_GETTABLE);
        }
    }

    private void renameInterfaces() {
        List<String> ret = new ArrayList<>();
        List<TypeNode> genericDefs = getGType().getGenericType();
        for (String i : interfaces()) {
            TypeNode tn = TypeNode.create(i, false);
            if (tn.getGenericType() != null) {
                if (genericDefs != null) {
                    ret.add(tn.toXWithGenerics(genericDefs).toString());
                } else {
                    ret.add(tn.toX().toString());
                }
            } else {
                ret.add(tn.toX().toString());
            }
        }
        setInterfaces(ret);
    }

    public static class Builder extends ClassNode.Builder {
        @Override
        protected ClassNode getClassNode() {
            return new XAdapterClassNode();
        }
    }
}
