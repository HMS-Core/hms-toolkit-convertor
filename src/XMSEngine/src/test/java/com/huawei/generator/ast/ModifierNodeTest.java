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

import com.huawei.generator.utils.Modifier;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test for modifier nodes
 *
 * @since 2020-01-17
 */
public class ModifierNodeTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void sortModifiers() {
        ModifierNode modifierNode = ModifierNode.create(Arrays.asList(Modifier.STATIC.getName(),
            Modifier.FINAL.getName(), Modifier.PUBLIC.getName()));
        modifierNode.sortModifiers();
        Assert.assertEquals(modifierNode.toString(), "public static final ");

        ModifierNode modifierNodeCase = ModifierNode.create(Arrays.asList(Modifier.PUBLIC.getName(),
            Modifier.STATIC.getName(), Modifier.ABSTRACT.getName()));
        modifierNodeCase.sortModifiers();
        Assert.assertEquals(modifierNodeCase.toString(), "public abstract static ");
    }

    @Test
    public void distinctTest() {
        ModifierNode modifierNode = ModifierNode.create(Arrays.asList(Modifier.STATIC.getName(),
            Modifier.FINAL.getName(),
            Modifier.PUBLIC.getName(),
            Modifier.PUBLIC.getName(),
            Modifier.FINAL.getName()));
        modifierNode.sortModifiers();
        Assert.assertEquals(modifierNode.toString(), "public static final ");
    }

    @Test
    public void addTest() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Illegal modifier: error");
        ModifierNode modifierNode =
            ModifierNode.create(Arrays.asList(Modifier.STATIC.getName(), Modifier.FINAL.getName(),
                Modifier.PUBLIC.getName(), Modifier.PUBLIC.getName(), Modifier.FINAL.getName(), "error"));
        modifierNode.sortModifiers();
    }

    @Test
    public void defaultPublicTest() {
        ModifierNode modifierNode = ModifierNode.create(
            Arrays.asList(Modifier.STATIC.getName(), Modifier.FINAL.getName()));
        modifierNode.sortModifiers();
        Assert.assertEquals(modifierNode.toString(), "public static final ");
    }

    @Test
    public void emptyTest() {
        ModifierNode modifierNode = ModifierNode.create(Collections.emptyList());
        modifierNode.sortModifiers();
        Assert.assertEquals(modifierNode.toString(), "public ");
    }
}