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

package com.huawei.generator.gen.ContainerTransform;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Test for ContainerTransform
 *
 * @since 2019-12-01
 */
public class ContainerTransformTest {
    public static <T, R> T[] genericArrayCopy(R[] array, Class<T> type, Function<R, T> mapper) {
        T[] arr = (T[]) Array.newInstance(type, array.length);
        Object[] objects = Stream.of(array).map(mapper).toArray();
        System.arraycopy(objects, 0, arr, 0, arr.length);
        return arr;
    }

    private <R, T> Iterable<T> transformIterable(Iterable<R> iterable, Function<R, T> mapper) {
        Iterator<T> iterator = StreamSupport.stream(iterable.spliterator(), false).map(mapper).iterator();
        return () -> iterator;
    }

    public static X mapperH(H h) {
        return new X(null, h);
    }

    public static X mapperG(G g) {
        return new X(g, null);
    }

    public static <T, R> Collection<T> mapCollection(Collection<? extends R> collection,
        Function<R, T> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    @Test
    public void testExtractGH() {
        X[] xes = new X[3];
        X x1 = new X(new G(), new H());
        xes[0] = x1;
        X x2 = new X(new G(), new H());
        xes[1] = x2;
        X x3 = new X(new G(), new H());
        xes[2] = x3;
        H[] hes = genericArrayCopy(xes, H.class, X::getH);
        G[] ges = genericArrayCopy(xes, G.class, e -> (G) e.getG());
        Assert.assertSame(hes[0], xes[0].getH());
        Assert.assertSame(ges[0], xes[0].getG());
    }

    @Test
    public void testMapArray() {
        H[] hs = new H[3];
        hs[0] = new H();
        hs[1] = new H();
        hs[2] = new H();
        X[] xes = genericArrayCopy(hs, X.class, ContainerTransformTest::mapperH);
        Assert.assertEquals(3, xes.length);
    }

    @Test
    public void testHListArray() {
        List<H> hList = new ArrayList<>();
        hList.add(new H());
        hList.add(new H());
        hList.add(new H());
        Collection<X> xes = mapCollection(hList, new Function<H, X>() {
            @Override
            public X apply(H h) {
                return new X(null, h);
            }
        });

        Assert.assertEquals(3, xes.size());
        List<X> list = (List<X>) xes;
        Assert.assertSame(list.get(0), ((List<X>) xes).get(0));
    }

    @Test
    public void testGListArray() {
        List<G> hList = new ArrayList<>();
        hList.add(new G());
        hList.add(new G());
        hList.add(new G());
        Collection<X> xes = mapCollection(hList, ContainerTransformTest::mapperG);
        Assert.assertEquals(3, xes.size());
        if (xes instanceof List) {
            List<X> list = (List<X>) xes;
            Assert.assertSame(list.get(0), ((List<X>) xes).get(0));
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Test
    public void streamIterable() {
        Set<String> data = new HashSet<>();
        data.add("HMS");
        data.add("GMS");
        Iterable<String> iterable = Arrays.asList("hms", "gms");
        Iterable<String> it = transformIterable(iterable, String::toUpperCase);
        while (it.iterator().hasNext()) {
            Assert.assertTrue(data.contains(it.iterator().next()));
        }
    }

    @Test
    public void nestedList() {
        List<List<String>> lists = Arrays.asList(Arrays.asList("g", "h"), Arrays.asList("hms", "gms"));
        List<List<String>> transformed = lists.stream()
            .map(strings -> strings.stream().map(String::toUpperCase).collect(Collectors.toList()))
            .collect(Collectors.toList());
        Assert.assertEquals(2, transformed.size());
        Assert.assertTrue(transformed.get(1).contains("HMS"));
        Assert.assertTrue(transformed.get(1).contains("GMS"));
    }
}
