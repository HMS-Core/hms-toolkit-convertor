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

package com.huawei.generator.method.exception;

import com.huawei.generator.ast.CatchNode;
import com.huawei.generator.ast.ExceptionNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TryNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.method.component.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps a try-catch block around a given piece of code.
 *
 * @since 2020-03-02
 */
public abstract class ExceptionHandler {
    protected MethodNode methodNode;

    protected Component component;

    protected List<TypeNode> exceptions;

    protected ExceptionHandler(MethodNode methodNode, Component component) {
        this.methodNode = methodNode;
        this.exceptions = methodNode.getExceptions();
        this.component = component;
    }

    /**
     * handle exceptions
     *
     * @param originalBlock the original method body
     * @return a new body wrapped with try-catch
     */
    public List<StatementNode> handleException(List<StatementNode> originalBlock) {
        List<TypeNode> xExceptions = exceptions == null ? Collections.emptyList() : findXExceptions();
        List<TypeNode> genericExceptions = exceptions == null ? Collections.emptyList() : findGenericExceptions();
        return wrapTryCatch(originalBlock, xExceptions, genericExceptions);
    }

    /**
     * Finds X exceptions.
     * 
     * @return a list of X exceptions
     */
    protected abstract List<TypeNode> findXExceptions();

    private List<TypeNode> findGenericExceptions() {
        return exceptions.stream().filter(t -> methodNode.isGeneric(t, true)).collect(Collectors.toList());
    }

    /**
     * Generates a CatchNode for exception type t
     * 
     * @param t exception type
     * @return a CatchNode for handling exceptions of type t
     */
    protected abstract CatchNode catchException(TypeNode t);

    /**
     * Generates a CatchNode for exception type t, where t represents a generic type.
     * 
     * @param t generic exception type
     * @return a CatchNode for handling exceptions of type t
     */
    protected abstract CatchNode catchGeneric(TypeNode t);

    private List<StatementNode> wrapTryCatch(List<StatementNode> originalBlock, List<TypeNode> xExceptions,
        List<TypeNode> genericExceptions) {
        List<StatementNode> catchNodes = new ArrayList<>();

        for (TypeNode e : xExceptions) {
            catchNodes.add(catchException(e));
        }

        for (TypeNode e : genericExceptions) {
            catchNodes.add(catchGeneric(e));
        }

        return catchNodes.isEmpty() ? originalBlock :
            Collections.singletonList(ExceptionNode.create(TryNode.create(originalBlock), catchNodes));
    }
}
