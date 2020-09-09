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

import static com.huawei.generator.utils.XMSUtils.degenerify;
import static com.huawei.generator.utils.XMSUtils.outerClassOf;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.custom.XAdapterClassNode;
import com.huawei.generator.classes.XClassFactory;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.utils.G2HTables;
import com.huawei.generator.utils.GlobalMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TaskManager for Generation
 *
 * @since 2019-11-14
 */
public class GenerationTaskManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerationTaskManager.class);

    private Map<String, JClass> definitions = new HashMap<>();

    private Map<String, XAdapterClassNode> outerTasks = new HashMap<>();

    private Set<XAdapterClassNode> innerTasks = new HashSet<>();

    private Map<String, XAdapterClassNode> allClasses = new HashMap<>();

    private XClassFactory xClassFactory;

    private GenerationTaskManager(XClassFactory factory) {
        this.xClassFactory = factory;
    }

    public static GenerationTaskManager create(MethodGeneratorFactory methodFactory) {
        XClassFactory factory = new XClassFactory(methodFactory);
        return new GenerationTaskManager(factory);
    }

    public static GenerationTaskManager create(MethodGeneratorFactory methodFactory, String pluginPath) {
        XClassFactory factory = new XClassFactory(methodFactory, pluginPath);
        return new GenerationTaskManager(factory);
    }

    /**
     * return API conversion statistics
     *
     * @return API conversion statistics
     */
    ApiStats initClasses() {
        // API means the public methods and fields
        ApiStats stats = new ApiStats();
        int totalApis = definitions.values()
            .stream()
            .map(jClass -> jClass.methods().size() + jClass.fields().size())
            .reduce(0, Integer::sum);
        LOGGER.warn("total apis to be transformed: {}", totalApis);
        stats.setTotal(totalApis);
        // process all outer classes first
        List<JClass> outClasses = definitions.values()
            .stream()
            .filter(def -> !def.isInnerClass())
            .filter(def -> !G2HTables.inBlockList(TypeNode.create(def.gName()).toX().toString(), "*"))
            .collect(Collectors.toList());
        outClasses.forEach(def -> {
            XAdapterClassNode node = xClassFactory.from(def);
            String name = degenerify(TypeNode.create(def.gName()).toX().toString());
            allClasses.put(name, node);
            outerTasks.put(name, node);
        });

        int outApis = computeApis(outClasses);

        List<JClass> innerClasses = definitions.values()
            .stream()
            .filter(JClass::isInnerClass)
            .filter(def -> !G2HTables.inBlockList(TypeNode.create(def.gName()).toX().toString(), "*"))
            .collect(Collectors.toList());
        innerClasses.forEach(def -> {
            XAdapterClassNode node = xClassFactory.from(def);
            String name = degenerify(TypeNode.create(def.gName()).toX().toString());
            allClasses.put(name, node);
            innerTasks.add(node);
        });
        int innerApis = computeApis(innerClasses);
        stats.setFilterByWild(totalApis - (outApis + innerApis));

        int fakeApis = computeFakeApis(outClasses) + computeFakeApis(innerClasses);
        stats.setFakeApis(fakeApis);

        // resolve all inner classes, put them into their outer classes
        allClasses.values().stream().filter(ClassNode::isInner).forEach(classNode -> {
            String fullName = classNode.fullName();
            String outerClassName = outerClassOf(degenerify(fullName));
            ClassNode outerClassNode = allClasses.get(outerClassName);
            if (outerClassNode == null) {
                LOGGER.error("Can't find class node: {}, maybe json is missing", outerClassName);
                return;
            }
            outerClassNode.innerClasses().add(classNode);
        });

        // set package name of all inner classes
        outerTasks.values().forEach(classNode -> {
            classNode.innerClasses().forEach(c -> c.setPackageName(classNode.packageName()));
        });
        return stats;
    }

    private int computeApis(List<JClass> classes) {
        return classes.stream().map(jClass -> jClass.methods().size() + jClass.fields().size()).reduce(0, Integer::sum);
    }

    /**
     * @param classes API collection after block list filtering, outer classes or inner classes
     * @return the number of APIs with notsupport and isdecrecrected status in the API after block list filtering
     */
    private int computeFakeApis(List<JClass> classes) {
        int apis = 0;
        for (JClass def : classes) {
            for (JMapping<JFieldOrMethod> field : def.fields()) {
                if (isFakeApi(field.status())) {
                    apis++;
                }
            }
            for (JMapping<JMethod> method : def.methods()) {
                if (isFakeApi(method.status())) {
                    apis++;
                }
            }
        }
        return apis;
    }

    /**
     * check if the status of the API is notSupport or isDeprecated or developerManual
     *
     * @param status api status
     * @return return boolean
     */
    private boolean isFakeApi(String status) {
        return Arrays.asList(JMapping.STATUS_UNSUPPORTED, JMapping.STATUS_DEVELOPER_MANUAL, JMapping.STATUS_DUMMY)
            .contains(status);
    }

    void addDefinition(JClass def) {
        definitions.put(TypeNode.create(def.gName()).toX().toString(), def);
    }

    void setAllClassMapping() {
        definitions.values().forEach(jClass -> {
            GlobalMapping map = new GlobalMapping();
            map.setG(jClass.gName());
            map.setH(jClass.hName());
            String xName = TypeNode.create(jClass.gName()).toX().toString();
            map.setX(xName);
            GlobalMapping.getXmappings().put(degenerify(xName), map);
            if (!jClass.hName().equals("java.lang.Object")) {
                GlobalMapping.getHmappings().put(degenerify(jClass.hName()), map);
            }
        });
        definitions.keySet()
            .forEach(xms -> GlobalMapping.getDegenerigyMap().put(degenerify(xms), definitions.get(xms)));
    }

    Collection<ClassNode> generateAll() {
        innerTasks.forEach(xClassFactory::populate);
        return outerTasks.values().stream().map(xClassFactory::populate).collect(Collectors.toList());
    }
}
