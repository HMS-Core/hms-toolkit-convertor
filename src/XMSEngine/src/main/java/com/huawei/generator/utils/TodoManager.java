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

package com.huawei.generator.utils;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.CompilationUnitNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.custom.CustomContentNode;
import com.huawei.generator.ast.custom.XClassNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.gen.GeneratorConfiguration;
import com.huawei.generator.method.gen.AbnormalBodyGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * TodoUtils
 *
 * @since 2019-11-19
 */
public class TodoManager {
    private static boolean enableOutputTodoComment;

    private boolean isEnabled;

    private TodoResourceLoader customCodeManager;

    static {
        Properties properties = new Properties();
        try (InputStream ins = TodoManager.class.getResourceAsStream("/generator.properties")) {
            properties.load(ins);
            enableOutputTodoComment = "true".equals(properties.getProperty("todo"));
        } catch (IOException e) {
            enableOutputTodoComment = false;
        }
    }

    public TodoManager(String jarPath, GeneratorConfiguration config) {
        this.isEnabled = config.isEnableTodo();
        this.customCodeManager = new TodoResourceLoader(jarPath, config.getStaticPath());
    }

    /**
     * Gets the descriptor of a class
     *
     * @param classNode classNode
     * @return descriptor
     */
    private static String getClassDescriptor(ClassNode classNode) {
        if (classNode == null) {
            throw new IllegalArgumentException("class node is null");
        }
        String fullClassName = classNode.fullName();
        if (fullClassName == null) {
            throw new IllegalArgumentException("class node's full name is null");
        }
        if (classNode.isInner() && fullClassName.endsWith(AstConstants.IMPL)) {
            List<String> interfaces = classNode.interfaces();
            if (interfaces.size() == 1) {
                fullClassName = interfaces.get(0) + "." + fullClassName;
            } else if (interfaces.size() == 0) {
                String superClassName = classNode.superName();
                if (superClassName == null) {
                    throw new IllegalArgumentException("XImpl or ZImpl has no super name");
                }
                fullClassName = superClassName + "." + fullClassName;
            } else {
                throw new IllegalArgumentException("XImpl or ZImpl can not implement multi interfaces");
            }
        }
        return fullClassName;
    }

    private static String getMethodShortDescriptor(MethodNode node) {
        if (node.name() == null) {
            throw new IllegalArgumentException("method name can not be null");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(node.name());
        stringBuilder.append("(");
        if (node.parameters() == null) {
            throw new IllegalArgumentException("method parameters can not be null");
        }
        for (TypeNode item : node.parameters()) {
            stringBuilder.append(item.toString());
        }
        stringBuilder.append(")");
        if (node.returnType() != null) {
            stringBuilder.append(node.returnType().toString());
        }
        return stringBuilder.toString();
    }

    /**
     * Gets the descriptor for a method
     *
     * @param node method node
     * @return descriptor
     */
    public static String getMethodDescriptor(MethodNode node) {
        StringBuilder stringBuilder = new StringBuilder();
        ClassNode classNode = node.parent();
        String fullClassName = getClassDescriptor(classNode);
        stringBuilder.append(fullClassName);
        stringBuilder.append(".");
        stringBuilder.append(getMethodShortDescriptor(node));
        return stringBuilder.toString();
    }

    /**
     * Creates to-do block for a method
     *
     * @param node method node
     * @return filled body
     */
    public static List<StatementNode> createTodoBlockFor(MethodNode node) {
        return createTodoBlockFor(node, "");
    }

    /**
     * Creates to-do block for a method
     * 
     * @param node method node
     * @param prefix prefix of the key for querying to-do entry
     */
    public static List<StatementNode> createTodoBlockFor(MethodNode node, String prefix) {
        String descriptor = prefix + TodoManager.getMethodDescriptor(node);
        // This is a hack and should be removed.
        node.setHasTodo(true);
        return Collections
            .singletonList(CustomContentNode.create(descriptor, AbnormalBodyGenerator.TO_DO_PLACEHOLDER.generate()));
    }

    /**
     * Creates to-do block for a class node
     * 
     * @param node class node
     */
    public static void createTodoBlockFor(ClassNode node) {
        if (node.hasTodo()) {
            String descriptor = TodoCommentConstants.USER_CUSTOM_CODE_IN_CLASS_BODY + getClassDescriptor(node);
            node.setCustomContentNode(CustomContentNode.create(descriptor, Collections.emptyList()));
        }
        for (ClassNode innerClassNode : node.innerClasses()) {
            if (innerClassNode instanceof XClassNode) {
                createTodoBlockFor(innerClassNode);
            }
        }
    }

    /**
     * Creates to-do block for a compilation unit node.
     *
     * @param node compilation unit node
     */
    public static void createTodoBlockFor(CompilationUnitNode node) {
        ClassNode firstClass = node.getClassNodes().get(0);
        String descriptor = TodoCommentConstants.USER_CUSTOM_CODE_OUT_CLASS_BODY + getClassDescriptor(firstClass);
        node.setCustomContentNode(CustomContentNode.create(descriptor, Collections.emptyList()));
    }

    public boolean shouldOutputTodoComment() {
        return isEnabled && enableOutputTodoComment;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Optional<String> getCustomCode(String key) {
        return customCodeManager.getContent(key);
    }
}