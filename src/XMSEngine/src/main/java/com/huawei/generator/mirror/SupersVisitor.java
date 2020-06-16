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

package com.huawei.generator.mirror;

import static com.huawei.generator.utils.XMSUtils.degenerify;

import com.huawei.generator.gen.AstConstants;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * Visit all super classes and super interfaces, in an order same as resolving a symbolic instance method reference:
 * first current class, then super classes of current class, then for each previously visited class, visit its
 * interfaces, and super interfaces of each interface
 *
 * @since 2020-01-06
 */
public class SupersVisitor {
    private KClass start;

    private Map<String, KClass> graph;

    public SupersVisitor(KClass start, Map<String, KClass> graph) {
        this.start = start;
        this.graph = graph;
    }

    private KClass toKClass(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        KClass kClass = graph.get(degenerify(name));
        if (kClass == null) {
            // Perhaps it's an common class?
            kClass = KClassReader.INSTANCE.getAndroidClassList().get(degenerify(name));
        }

        if (kClass == null && !name.equals(AstConstants.OBJECT)) {
            throw new IllegalStateException("invalid kClass");
        }
        return kClass;
    }

    public List<KClass> visit() {
        Set<KClass> markList = new HashSet<>();
        List<KClass> resultList = new ArrayList<>();

        // First visit current class and all super classes
        for (KClass cur = start; cur != null; cur = toKClass(cur.getSuperClass())) {
            resultList.add(cur);
        }

        // Then interfaces
        List<KClass> superClasses = new ArrayList<>(resultList);
        for (KClass superClass : superClasses) {
            Queue<KClass> queue = new ArrayDeque<>();
            // Add all interfaces of this superClass, and mark them
            superClass.getInterfaces().stream().map(this::toKClass).filter(Objects::nonNull).forEach(iface -> {
                if (!markList.contains(iface)) {
                    markList.add(iface);
                    queue.add(iface);
                }
            });

            // Add super interfaces of each interface
            while (!queue.isEmpty()) {
                KClass iface2 = queue.poll();
                resultList.add(iface2);
                iface2.getInterfaces().stream().map(this::toKClass).filter(Objects::nonNull).forEach(iface -> {
                    if (!markList.contains(iface)) {
                        markList.add(iface);
                        queue.add(iface);
                    }
                });
            }
        }

        return resultList;
    }
}
