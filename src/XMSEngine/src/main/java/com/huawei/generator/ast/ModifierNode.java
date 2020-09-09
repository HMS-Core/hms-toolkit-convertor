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

package com.huawei.generator.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Modifier Node
 *
 * @since 2020-01-17
 */
public final class ModifierNode extends AstNode {
    private static final Map<String, Integer> MODIFIER_PRIORITY = new HashMap<>();

    private static final List<String> MODIFIER_BY_PRIORITY = Arrays.asList("public", "private", "protected", "abstract",
            "static", "final", "transient", "volatile", "default", "synchronized", "native", "strictfp");

    private List<String> modifiers = new ArrayList<>();

    private ModifierNode() {
    }

    static {
        for (int i = 0; i < MODIFIER_BY_PRIORITY.size(); i++) {
            MODIFIER_PRIORITY.put(MODIFIER_BY_PRIORITY.get(i), i);
        }
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public void add(String mod) {
        if (!MODIFIER_PRIORITY.containsKey(mod)) {
            throw new IllegalArgumentException("Illegal modifier: " + mod);
        }
        if (modifiers.contains(mod)) {
            return;
        }
        modifiers.add(mod);
    }

    public void addAll(List<String> mods) {
        for (String mod : mods) {
            this.add(mod);
        }
    }

    public void clear() {
        modifiers.clear();
    }

    public void remove(String mod) {
        modifiers.remove(mod);
    }

    public boolean contains(String mod) {
        return modifiers.contains(mod);
    }

    public void sortModifiers() {
        if (modifiers.isEmpty()) {
            modifiers.add("public");
            return;
        }
        if (!modifiers.contains("public") && !modifiers.contains("private") && !modifiers.contains("protected")
            && !modifiers.contains("default")) {
            modifiers.add("public");
        }
        this.modifiers = modifiers.stream()
            .map(MODIFIER_PRIORITY::get)
            .distinct()
            .sorted()
            .map(MODIFIER_BY_PRIORITY::get)
            .collect(Collectors.toList());
    }

    public String printModifiers() {
        return String.join(" ", modifiers);
    }

    public static ModifierNode create(List<String> modifiers) {
        ModifierNode node = new ModifierNode();
        node.addAll(modifiers);
        return node;
    }
}
