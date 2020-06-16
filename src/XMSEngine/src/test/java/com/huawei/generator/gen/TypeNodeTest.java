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

package com.huawei.generator.gen;

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.utils.TypeUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for TypeNode
 *
 * @since 2019-11-20
 */
public class TypeNodeTest {
    @Test
    public void testTypeNode() {
        TypeNode tn = TypeNode.create("Map<java.lang.Long, ArrayList<String>>");
        Assert.assertEquals(tn.getTypeName(), "Map");
        Assert.assertEquals(tn.getGenericType().size(), 2);
        TypeNode gn = tn.getGenericType().get(0);
        Assert.assertEquals("java.lang.Long", gn.getTypeName());
        gn = tn.getGenericType().get(1);
        Assert.assertEquals("ArrayList", gn.getTypeName());
        Assert.assertEquals(1, gn.getGenericType().size());
        gn = gn.getGenericType().get(0);
        Assert.assertEquals("String", gn.getTypeName());

        tn = TypeNode.create("Map<K, V>");
        Assert.assertEquals(tn.getTypeName(), "Map");
        Assert.assertEquals(tn.getGenericType().size(), 2);
        gn = tn.getGenericType().get(0);
        Assert.assertEquals("K", gn.getTypeName());
        gn = tn.getGenericType().get(1);
        Assert.assertEquals("V", gn.getTypeName());

        tn = TypeNode.create("Task");
        Assert.assertEquals(tn.getTypeName(), "Task");
        Assert.assertNull(gn.getGenericType());
    }

    @Test
    public void testTypeNodeWithDefine() {
        TypeNode tn = TypeNode.create("<TContinuationResult> com.google.android.gms.tasks.Task<TContinuationResult>");
        Assert.assertEquals(tn.getTypeName(), "com.google.android.gms.tasks.Task");
        Assert.assertEquals(tn.getGenericType().size(), 1);
        Assert.assertEquals(1, tn.getDefTypes().size());
        Assert.assertEquals("TContinuationResult", tn.getDefTypes().get(0).getTypeName());
    }

    @Test
    public void testTypeNodeWithExtends() {
        TypeNode tn =
            TypeNode.create("<E extends Integer> com.google.android.gms.tasks.Task<T extends Object & String>", false);
        Assert.assertEquals(tn.getTypeName(), "com.google.android.gms.tasks.Task");
        Assert.assertEquals(tn.getGenericType().size(), 1);
        Assert.assertEquals(1, tn.getDefTypes().size());
        Assert.assertEquals("E", tn.getDefTypes().get(0).getTypeName());
        TypeNode defNode = tn.getDefTypes().get(0);
        Assert.assertEquals("Integer", defNode.getSuperClass().get(0).getTypeName());

        TypeNode genericNode = tn.getGenericType().get(0);
        Assert.assertEquals("T", genericNode.getTypeName());
        Assert.assertEquals(2, genericNode.getSuperClass().size());
        Assert.assertEquals("Object", genericNode.getSuperClass().get(0).getTypeName());
        Assert.assertEquals("String", genericNode.getSuperClass().get(1).getTypeName());
        Assert.assertEquals("com.google.android.gms.tasks.Task<T extends Object & String>", tn.toString());
    }

    @Test
    public void listInstanceOfTest() throws Exception {
        Class<?> aClass = Class.forName("java.util.Collection");
        boolean assignableFrom = aClass.isAssignableFrom(ArrayList.class);
        Assert.assertTrue(assignableFrom);
    }

    @Test
    public void elementTypeOfArray() {
        TypeNode typeNode = TypeNode.create("com.google.android.gms.tasks.Task[]");
        Assert.assertTrue(typeNode.isArray());
        Assert.assertEquals(typeNode.getTypeName(), "com.google.android.gms.tasks.Task");
    }

    @Test
    public void elementTypeOfGenericArray() {
        TypeNode typeNode = TypeNode.create("com.google.android.gms.tasks.Task<?>[]");
        Assert.assertTrue(typeNode.isArray());
        Assert.assertEquals(typeNode.getTypeName(), "com.google.android.gms.tasks.Task");
    }

    @Test
    public void elementTypeOfCollection() {
        TypeNode typeNode = TypeNode.create("java.util.List<com.google.android.gms.tasks.Task>");
        Assert.assertNotNull(typeNode.getGenericType());
        Assert.assertTrue(TypeUtils.isCollection(typeNode));
        typeNode = TypeNode.create("java.util.List<? extends com.google.android.gms.tasks.Task>");
        Assert.assertEquals("java.util.List", typeNode.getTypeName());
        Assert.assertTrue(TypeUtils.isCollection(typeNode));
        Assert.assertNotNull(typeNode.getGenericType());
        Assert.assertEquals(typeNode.getGenericType().get(0).getTypeName(),
            typeNode.getGenericType().get(0).getTypeNameWithoutPackage());
        Assert.assertEquals("com.google.android.gms.tasks.Task",
            typeNode.getGenericType().get(0).getSuperClass().get(0).getTypeName());
        typeNode = TypeNode.create("java.util.List<E extends com.google.android.gms.tasks.Task>");
        Assert.assertTrue(TypeUtils.isCollection(typeNode));
        Assert.assertNotNull(typeNode.getGenericType());
        Assert.assertEquals(typeNode.getGenericType().get(0).getTypeName(),
            typeNode.getGenericType().get(0).getTypeNameWithoutPackage());
        Assert.assertEquals("com.google.android.gms.tasks.Task",
            typeNode.getGenericType().get(0).getSuperClass().get(0).getTypeName());
    }

    @Test
    public void elementTypeOfVarArg() {
        TypeNode typeNode = TypeNode.create("com.google.android.gms.tasks.Task...");
        Assert.assertTrue(typeNode.isVarArg());
        Assert.assertEquals(typeNode.getTypeName(), "com.google.android.gms.tasks.Task");
    }

    @Test
    public void testArrayType() {
        Map<String, Integer> types = new HashMap<>();
        types.put("T[][]", 2);
        types.put("Object[]", 1);
        types.put("java.util.List<T[][]>[]", 1);
        types.forEach((k, v) -> {
            TypeNode tn = TypeNode.create(k, false);
            Assert.assertTrue(tn.isArray());
            Assert.assertEquals(v.intValue(), tn.dimension());
            Assert.assertEquals(k, tn.toString());
        });
    }

    @Test
    public void mapContainer() {
        TypeNode typeNode = TypeNode.create("java.util.Map<java.lang.String, com.google.android.gms.tasks.Task");
        Assert.assertEquals("com.google.android.gms.tasks.Task", typeNode.getGenericType().get(1).getTypeName());
    }

    @Test
    public void isNonSdkContainer() {
        TypeNode array = TypeNode.create("com.google.android.gms.common.api.Scope[]");
        Assert.assertTrue(array.isArray());
        Assert.assertTrue(TypeUtils.isNonSdkContainer(array));
        Assert.assertFalse(TypeUtils.isNonSdkContainer(TypeNode.create("int[]")));
        Assert.assertFalse(TypeUtils.isNonSdkContainer(TypeNode.create("java.lang.String[]")));
        Assert.assertTrue(TypeUtils.isNonSdkContainer(TypeNode.create("XT[]")));
        Assert.assertTrue(TypeUtils.isNonSdkContainer(TypeNode.create("java.util.List<XT>")));
        Assert.assertTrue(TypeUtils.isNonSdkContainer(TypeNode.create("java.util.Set<XT>")));
        Assert.assertTrue(TypeUtils.isNonSdkContainer(TypeNode.create("java.util.Map<XT, XT>")));
        Assert.assertFalse(TypeUtils.isNonSdkContainer(TypeNode.create("org.xms.core.Task<XTResult>")));
    }

    @Test
    public void needRemap() {
        // Map
        Assert.assertFalse(TypeUtils.needRemap(TypeNode.create("java.util.Map<String, byte[]>)")));
        Assert.assertFalse(TypeUtils.needRemap(TypeNode.create("java.util.Map<String, java.lang.Byte[]>")));
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("java.util.Map<String, org.xms.g.location.Map[]>")));

        // Array
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("XE[]")));
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("org.xms.g.location.Model[]")));
        Assert.assertFalse(TypeUtils.needRemap(TypeNode.create("java.lang.String[]")));

        // SparseArray
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("android.util.SparseArray<XE>")));
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("android.util.SparseArray<org.xms.g.location.Model>")));
        Assert.assertFalse(TypeUtils.needRemap(TypeNode.create("android.util.SparseArray<android.core.DataBuffer>")));

        // List
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("java.util.List<XE>")));
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("java.util.List<org.xms.g.location.Model>")));
        Assert.assertFalse(TypeUtils.needRemap(TypeNode.create("java.util.List<android.core.DataBuffer>")));

        // Set
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("java.util.Set<XE>")));
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("java.util.Set<org.xms.g.location.Model>")));
        Assert.assertFalse(TypeUtils.needRemap(TypeNode.create("java.util.Set<android.core.DataBuffer>")));

        // Iterable
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("java.lang.Iterable<XE>")));
        Assert.assertTrue(TypeUtils.needRemap(TypeNode.create("java.lang.Iterable<org.xms.g.location.Model>")));
        Assert.assertFalse(TypeUtils.needRemap(TypeNode.create("java.lang.Iterable<android.core.DataBuffer>")));
    }

    @Test
    public void genericNode() {
        Assert.assertEquals("java.lang.Object", TypeUtils.getUpperBound(TypeNode.create("E[]"),
            TypeNode.create("<E> java.util.ArrayList<E>").getDefTypes()));
        String complex =
            "<T extends java.lang.Object, E extends com.google.android.gms.common.data.Freezable<T>> " +
                "java.util.ArrayList<T>";
        TypeNode typeNode = TypeNode.create(complex);
        String g = TypeUtils.getUpperBound(TypeNode.create("E[]"), typeNode.getDefTypes());
        Assert.assertEquals("com.google.android.gms.common.data.Freezable", g);
    }

    @Test
    public void renameGenerics() {
        TypeNode tn = TypeNode.create("<T, E extends java.util.List<T>> java.util.Map<K, E>", false);
        tn.renameAllGenerics(null);
        // expect: <XT, XE extends java.util.List<XT>> java.util.Map<K, XE>
        Assert.assertEquals("XT", tn.getDefTypes().get(0).toString());
        Assert.assertEquals("XE extends java.util.List<XT>", tn.getDefTypes().get(1).toString());
        Assert.assertEquals("java.util.Map<K, XE>", tn.toString());

        TypeNode tn2 = TypeNode.create("<T, E extends java.util.List<T>> java.util.Map<K, E>", false);
        List<TypeNode> defs = Arrays.asList(TypeNode.create("K extends String"));
        tn2.renameAllGenerics(defs);
        // expect: <XT, XE extends java.util.List<XT>> java.util.Map<XK, XE>
        Assert.assertEquals("XT", tn2.getDefTypes().get(0).toString());
        Assert.assertEquals("XE extends java.util.List<XT>", tn2.getDefTypes().get(1).toString());
        Assert.assertEquals("java.util.Map<XK, XE>", tn2.toString());
    }
}
