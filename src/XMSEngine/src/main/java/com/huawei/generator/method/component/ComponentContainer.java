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

import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Determines the construction of a X wrapper object.
 *
 * @since 2020-03-13
 */
public class ComponentContainer {
    public static final ComponentContainer EMPTY = new ComponentContainer();

    private List<Component> components;

    public ComponentContainer(Component... components) {
        this.components = Collections.unmodifiableList(Arrays.asList(components));
    }

    /**
     * Gets the components contained in this construction component.
     *
     * @return the components in this construction component.
     */
    public List<Component> components() {
        return components;
    }

    /**
     * Returns a list containing the return name of each component, e.g.: [gReturn, hReturn]
     *
     * @return the full list of return values.
     */
    public List<StatementNode> fullReturnParams() {
        return Collections.singletonList(NewNode.create(TypeNode.create(AstConstants.XMS_BOX),
            components.stream().map(it -> VarNode.create(it.retVarName())).collect(Collectors.toList())));
    }

    /**
     * Returns the parameter list used when invoking the constructor of a X class.
     * 
     * @param component one of the components of this construction component.
     * @param paramName the parameter name used for the specified component.
     * @return the parameter list for invocation of X constructors.
     */
    List<StatementNode> xWrapperParams(Component component, String paramName) {
        List<StatementNode> params = components.stream().map(it -> VarNode.create("null")).collect(Collectors.toList());
        int i = components.indexOf(component);
        if (i < 0) {
            throw new IllegalStateException("Construction component doesn't contain the given component");
        }
        params.set(i, VarNode.create(paramName));
        return params;
    }
}
