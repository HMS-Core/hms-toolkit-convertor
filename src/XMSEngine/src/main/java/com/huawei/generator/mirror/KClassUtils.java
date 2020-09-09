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

import static com.huawei.generator.json.JMapping.STATUS_MATCHING;
import static com.huawei.generator.utils.XMSUtils.degenerify;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.json.JParameter;
import com.huawei.generator.utils.GlobalMapping;
import com.huawei.generator.utils.TypeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utils ofr KClass
 *
 * @since 2019-12-09
 */
public class KClassUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(KClassUtils.class);

    private static boolean isAbstractMethod(KClass kClass, JMethod kMethod) {
        if (!kClass.contains(kMethod)) {
            throw new IllegalArgumentException("kMethod must be declared in kClass");
        }
        return kMethod.modifiers().contains("abstract");
    }

    // Check whether method is implemented in any class in kClassList
    private static boolean methodImplementedIn(JMethod method, List<KClass> kClassList) {
        return kClassList.stream().anyMatch(clazz -> clazz.hasImplemented(method));
    }

    /**
     * Fetch all abstract methods including those inherited from super interfaces or abstract classes.
     * 
     * @param kClass the target class
     * @param kClassMap the class hierarchy, given as a map
     * @param excludeImplemented whether to exclude the implemented (overridden) method from the results
     * @param includeSelf whether to include the given 'kClass' in the searching class list
     * @return list of abstract methods
     */
    private static List<JMethod> allAbstractMethods(KClass kClass, Map<String, KClass> kClassMap,
        boolean excludeImplemented, boolean includeSelf) {
        List<JMethod> resultList = new ArrayList<>();
        List<KClass> classList = new SupersVisitor(kClass, kClassMap).visit();
        if (!includeSelf) {
            classList.remove(kClass);
        }

        // classList should have contained all the super classes and super interfaces
        for (KClass clz : classList) {
            for (JMethod method : clz.getMethods()) {
                if (!isAbstractMethod(clz, method) || (excludeImplemented && methodImplementedIn(method, classList))) {
                    continue;
                }
                // Abstract method may be declared for multiple times. So avoid adding duplicated method
                if (resultList.stream().anyMatch(methodAdded -> methodAdded.sameAs(method))) {
                    continue;
                }
                resultList.add(method);
            }
        }

        return resultList;
    }

    /**
     * Get the JMapping for a method
     *
     * @param method A method
     * @param gORh indicates G or H method
     * @return The mapping for this method
     */
    private static JMapping<JMethod> toJMapping(JMethod method, String gORh) {
        boolean isG = gORh.equals("G");
        boolean isH = gORh.equals("H");
        String kClassName = method.getKClass().getClassName();
        String xClassName = null;
        if (isG) {
            // G class name to X class name
            xClassName = TypeNode.create(kClassName).toX().toString();
        }
        if (isH) {
            // H class name to X class name
            GlobalMapping globalMapping = GlobalMapping.getHmappings().get(degenerify(kClassName));
            if (globalMapping == null) {
                return null;
            }
            xClassName = globalMapping.getX();
        }
        JClass xJClass = GlobalMapping.getDegenerigyMap().get(degenerify(xClassName));
        if (xJClass == null) {
            return null;
        }
        for (JMapping<JMethod> jMapping : xJClass.methods()) {
            if (isG && method.sameAs(jMapping.g())) {
                return jMapping;
            }
            if (isH && method.sameAs(jMapping.h())) {
                return jMapping;
            }
        }
        return null;
    }

    /**
     * Get the collection of abstract method to be implemented in XImpl class.
     * 
     * @param def target JClass
     * @param xNode target classNode
     * @return Get the collection of method mapping
     */
    public static List<JMapping<JMethod>> getXImplMethods(JClass def, ClassNode xNode) {
        // methods need to be implemented in inheritance chain
        List<JMapping<JMethod>> inheritedAbstractMethods =
            KClassUtils.getHierarchicalAbstractMethodMappings(xNode, true, false);
        // abstract method in xms class
        List<JMapping<JMethod>> abstractMethods = new ArrayList<>();
        // implement method in xms class
        List<JMapping<JMethod>> implementMethods = new ArrayList<>();
        def.methods().forEach(jMapping -> {
            if (jMapping.g() == null) {
                return;
            }
            if (jMapping.g().modifiers().contains("abstract")
                || (xNode.isInterface() && !jMapping.g().modifiers().contains("default"))) {
                abstractMethods.add(jMapping);
            } else {
                implementMethods.add(jMapping);
            }
        });
        // add abstract method in xms class to result
        inheritedAbstractMethods.stream()
            .filter(jMapping -> !containsMethod(abstractMethods, jMapping.g(), true))
            .forEach(abstractMethods::add);
        // remove method implemented in xms class from result
        List<JMapping<JMethod>> resultMapping = new ArrayList<>();
        abstractMethods.forEach(jMapping -> {
            if (!containsMethod(implementMethods, jMapping.g(), true)) {
                resultMapping.add(jMapping);
            }
        });
        return resultMapping;
    }

    /**
     * Get the collection of abstract method to be completed in XImpl or anonymous class, including all abstract method
     * in the class and all abstract method in the superclass, interfaces.
     *
     * @param xNode xms class node
     * @param excludeImplemented true if you want to get a collection of methods that are not implemented, otherwise
     *        false
     * @param includeSelf true if include xNode, otherwise false
     * @return the collection of method mapping
     */
    public static List<JMapping<JMethod>> getHierarchicalAbstractMethodMappings(ClassNode xNode,
        boolean excludeImplemented, boolean includeSelf) {
        List<JMapping<JMethod>> resultMappings = new ArrayList<>();

        getMappingsImpl(resultMappings, xNode, excludeImplemented, includeSelf);

        return resultMappings;
    }

    /**
     * Get the collection of method to be completed in a non-abstract class, including all methods in this class and
     * all abstract methods inherited from the superclass, interfaces.
     *
     * @param xNode xms class node
     * @return the collection of method mapping
     */
    public static List<JMapping<JMethod>> getHierarchicalMethodMappings(ClassNode xNode) {
        // First include all method-mappings of this class
        List<JMapping<JMethod>> resultMappings = new ArrayList<>(xNode.getJClass().methods());

        getMappingsImpl(resultMappings, xNode, true, true);

        return resultMappings;
    }

    /**
     * Get the collection of method to be completed in a non-abstract class for GImpl, including all methods in this
     * class and
     * all abstract methods inherited from the superclass, interfaces.
     * 
     * @param xNode xms class node
     * @return the collection of method mapping
     */
    public static List<JMapping<JMethod>> getGHierarchicalMethodMapping(ClassNode xNode) {
        List<JMapping<JMethod>> resultMappings = new ArrayList<>(xNode.getJClass().methods());

        getGMappingsImpl(resultMappings, xNode, true, true);

        return resultMappings;
    }

    /**
     * Get the collection of method to be completed in a non-abstract class for HImpl, including all methods in this
     * class and
     * all abstract methods inherited from the superclass, interfaces.
     *
     * @param xNode xms class node
     * @return the collection of method mapping
     */
    public static List<JMapping<JMethod>> getHHierarchicalMethodMapping(ClassNode xNode) {
        List<JMapping<JMethod>> resultMappings = new ArrayList<>(xNode.getJClass().methods());

        getHMappingsImpl(resultMappings, xNode, true, true);

        return resultMappings;
    }

    /**
     * Checks whether a list of methods contains a specific method
     *
     * @param list The list of method-mappings
     * @param method Method to be checked
     * @param isG G or H
     * @return boolean
     */
    private static boolean containsMethod(List<JMapping<JMethod>> list, JMethod method, boolean isG) {
        return list.stream()
            .anyMatch(mapping -> isG ? (mapping.g() != null && mapping.g().sameAs(method))
                : (mapping.h() != null && mapping.h().sameAs(method)));
    }

    private static void addToMappings(ClassNode node, JMethod method, List<JMapping<JMethod>> mappings, String world) {
        if (!world.equals("G") && !world.equals("H") && !world.equals("A")) {
            throw new IllegalArgumentException("world must be G or H or A");
        }
        // Generator will traverse the inheritance tree to collect all the abstract methods.
        // For enums, compareTo is unnecessary because it's implemented by Enum.class, which is
        // the super class of all enum classes.
        if (node.isEnum() && method.name().equals("compareTo")) {
            return;
        }

        if (world.equals("A")) {
            mappings.add(JMapping.create(method, method, STATUS_MATCHING));
            return;
        }

        JMapping<JMethod> mapping = toJMapping(method, world);

        // figure out generic defines
        mapping = new GenericInheritResolver(node, method, mapping, world.equals("H")).resolveMapping();

        if (!mappings.contains(mapping)) {
            // Avoid add duplicate method.
            // Because SuperVisitor adds functions in the order of hierarchical traversal, the repeated methods in the
            // list will be overwritten by the previous methods, so only need to check whether the newly added
            // method already exists in the result.
            if (world.equals("G")) {
                for (JMapping<JMethod> jMethodJMapping : mappings) {
                    if (jMethodJMapping.g() == mapping.g()
                        || (jMethodJMapping.g() != null && jMethodJMapping.g().sameAs(mapping.g()))) {
                        return;
                    }
                }
            } else {
                for (JMapping<JMethod> jMethodJMapping : mappings) {
                    if (jMethodJMapping.h() == mapping.h()
                        || (jMethodJMapping.h() != null && jMethodJMapping.h().sameAs(mapping.h()))) {
                        return;
                    }
                }
            }
            mappings.add(mapping);
        }
    }

    /**
     * The helper function to collect inherited methods from G & H's class hierarchy, and build the mappings.
     * 
     * @param resultMappings the result mappings of the collected methods, may be not empty at the entry
     * @param xNode the class node of an X type
     * @param excludeImplemented param passed to allAbstractMethods
     * @param includeSelf param passed to allAbstractMethods
     */
    private static void getMappingsImpl(List<JMapping<JMethod>> resultMappings, ClassNode xNode,
        boolean excludeImplemented, boolean includeSelf) {
        KClass gClass = KClassReader.INSTANCE.getGClassList().get(xNode.getGType().getTypeName());
        if (gClass == null || gClass.getClassName().equals("")) {
            throw new IllegalStateException("Missing " + xNode.getGType().getTypeName() + " in gms.json");
        }
        List<JMethod> gAbstractMethods =
            allAbstractMethods(gClass, KClassReader.INSTANCE.getGClassList(), excludeImplemented, includeSelf);

        KClass hClass = KClassReader.INSTANCE.getHClassList().get(xNode.getHType().getTypeName());

        if (hClass == null) {
            if (!xNode.getHType().getTypeName().equals(AstConstants.OBJECT)) {
                LOGGER.info("Missing {} in hms.json", xNode.getHType().getTypeName());
            }
            // Just return the abstract method mappings from G
            gAbstractMethods.forEach(abstractMethod -> addToMappings(xNode, abstractMethod, resultMappings, "G"));
            return;
        }
        List<JMethod> hAbstractMethods =
            allAbstractMethods(hClass, KClassReader.INSTANCE.getHClassList(), excludeImplemented, includeSelf);

        List<JMethod> commonMethods = getCommonMethods(resultMappings, xNode, gAbstractMethods, hAbstractMethods);
        addGMapping(commonMethods, resultMappings, xNode, gAbstractMethods);
        addHMapping(commonMethods, resultMappings, xNode, hAbstractMethods);
    }

    private static void getGMappingsImpl(List<JMapping<JMethod>> resultMappings, ClassNode xNode,
        boolean excludeImplemented, boolean includeSelf) {
        KClass gClass = KClassReader.INSTANCE.getGClassList().get(xNode.getGType().getTypeName());
        if (gClass == null || gClass.getClassName().equals("")) {
            throw new IllegalStateException("Missing " + xNode.getGType().getTypeName() + " in gms.json");
        }
        List<JMethod> gAbstractMethods =
            allAbstractMethods(gClass, KClassReader.INSTANCE.getGClassList(), excludeImplemented, includeSelf);

        KClass hClass = KClassReader.INSTANCE.getHClassList().get(xNode.getHType().getTypeName());

        if (hClass == null) {
            if (!xNode.getHType().getTypeName().equals(AstConstants.OBJECT)) {
                LOGGER.info("Missing {} in hms.json", xNode.getHType().getTypeName());
            }
            // Just return the abstract method mappings from G
            gAbstractMethods.forEach(abstractMethod -> addToMappings(xNode, abstractMethod, resultMappings, "G"));
            return;
        }
        List<JMethod> hAbstractMethods =
            allAbstractMethods(hClass, KClassReader.INSTANCE.getHClassList(), excludeImplemented, includeSelf);

        List<JMethod> commonMethods = getCommonMethods(resultMappings, xNode, gAbstractMethods, hAbstractMethods);
        addGMapping(commonMethods, resultMappings, xNode, gAbstractMethods);
    }

    private static void getHMappingsImpl(List<JMapping<JMethod>> resultMappings, ClassNode xNode,
        boolean excludeImplemented, boolean includeSelf) {
        KClass gClass = KClassReader.INSTANCE.getGClassList().get(xNode.getGType().getTypeName());
        if (gClass == null || gClass.getClassName().equals("")) {
            throw new IllegalStateException("Missing " + xNode.getGType().getTypeName() + " in gms.json");
        }
        List<JMethod> gAbstractMethods =
            allAbstractMethods(gClass, KClassReader.INSTANCE.getGClassList(), excludeImplemented, includeSelf);

        KClass hClass = KClassReader.INSTANCE.getHClassList().get(xNode.getHType().getTypeName());

        if (hClass == null) {
            if (!xNode.getHType().getTypeName().equals(AstConstants.OBJECT)) {
                LOGGER.info("Missing {} in hms.json", xNode.getHType().getTypeName());
            }
            // Just return the abstract method mappings from G
            return;
        }
        List<JMethod> hAbstractMethods =
            allAbstractMethods(hClass, KClassReader.INSTANCE.getHClassList(), excludeImplemented, includeSelf);

        List<JMethod> commonMethods = getCommonMethods(resultMappings, xNode, gAbstractMethods, hAbstractMethods);
        addHMapping(commonMethods, resultMappings, xNode, hAbstractMethods);
    }

    private static void addGMapping(List<JMethod> commonMethods, List<JMapping<JMethod>> resultMappings,
        ClassNode xNode, List<JMethod> methods) {
        methods.stream()
            .filter(method -> !commonMethods.contains(method))
            .forEach(abstractMethod -> addToMappings(xNode, abstractMethod, resultMappings, "G"));
    }

    private static void addHMapping(List<JMethod> commonMethods, List<JMapping<JMethod>> resultMappings,
        ClassNode xNode, List<JMethod> hAbstractMethods) {
        // Add methods only in H
        hAbstractMethods.stream()
            .filter(method -> !commonMethods.contains(method))
            // might have been added through G, so avoid duplication
            .filter(abstractMethod -> !containsMethod(resultMappings, abstractMethod, false))
            .forEach(abstractMethod -> addToMappings(xNode, abstractMethod, resultMappings, "H"));
    }

    private static List<JMethod> getCommonMethods(List<JMapping<JMethod>> resultMappings, ClassNode xNode,
        List<JMethod> gAbstractMethods, List<JMethod> hAbstractMethods) {
        // Add all inherited H abstract methods. But check for existence of H first
        if (xNode.getHType() == null) {
            throw new IllegalArgumentException("empty h type will be replaced with java.lang.Object");
        }

        // There may be common methods contained in gAbstractMethods and hAbstractMethods, such as those inherited
        // from Android. They are not recorded in JClass's mappings, but they are matching.
        List<JMethod> commonMethods = gAbstractMethods.stream()
            .filter(gMethod -> hAbstractMethods.stream()
                .anyMatch(hMethod -> (hMethod.getKClass() == gMethod.getKClass() && hMethod.sameAs(gMethod))))
            .collect(Collectors.toList());

        // Add these commonMethods as matching status to resultMappings
        commonMethods.forEach(method -> addToMappings(xNode, method, resultMappings, "A"));
        return commonMethods;
    }

    /**
     * Determine whether there is an inheritance relationship
     * 
     * @param def target JClass
     * @param superClassName superClass name
     * @param isG whether is gms type
     * @return whether there is an inheritance relationship
     */
    public static boolean hasInheritance(JClass def, String superClassName, boolean isG) {
        KClass superKClass = KClassReader.INSTANCE.getAndroidClassList().get(degenerify(superClassName));
        List<KClass> classList;
        if (isG) {
            KClass kClass = KClassReader.INSTANCE.getGClassList().get(degenerify(def.gName()));
            classList = new SupersVisitor(kClass, KClassReader.INSTANCE.getGClassList()).visit();
        } else {
            KClass kClass = KClassReader.INSTANCE.getHClassList().get(degenerify(def.hName()));
            if (kClass == null) {
                return false;
            }
            classList = new SupersVisitor(kClass, KClassReader.INSTANCE.getHClassList()).visit();
        }
        return classList.contains(superKClass);
    }

    /**
     * Get hms constructor list from hms.json.
     *
     * @param className target class name
     * @param classList Collection of z KClasses
     * @return collection of constructor list
     */
    public static List<JMethod> getConstructorList(String className, Map<String, KClass> classList) {
        TypeNode node = TypeNode.create(className);
        KClass kClass = classList.get(node.getTypeName());
        if (kClass == null) {
            kClass = KClassReader.INSTANCE.getAndroidClassList().get(node.getTypeName());
        }
        List<JMethod> methods = kClass.getMethods();
        String shortName = node.getTypeName().substring(node.getTypeName().lastIndexOf(".") + 1);
        List<JMethod> constructorList = new ArrayList<>();

        // add for error in hms.json
        for (JMethod method : methods) {
            if (method.isPrivate()) {
                continue;
            }
            if (method.name().equals(shortName)) {
                boolean right = true;
                for (JParameter parameter : method.parameterTypes()) {
                    if (parameter.type().contains(".1")) {
                        right = false;
                        break;
                    }
                }
                if (right) {
                    constructorList.add(method);
                }
            }
        }
        return constructorList;
    }

    /**
     * Whether has XImpl on inheritance chain.
     * 
     * @param gType type node of g
     * @return true if has, otherwise false
     */
    public static boolean hasXImplOnInheritance(TypeNode gType) {
        Map<String, KClass> map = KClassReader.INSTANCE.getGClassList();
        KClass kClass = map.get(gType.getTypeName());
        List<KClass> classList = new SupersVisitor(kClass, map).visit();
        classList.remove(kClass);
        for (KClass cls : classList) {
            if (TypeUtils.isGmsType(TypeNode.create(cls.getClassName()).getTypeName())
                && (cls.isAbstract() || cls.isInterface())) {
                return true;
            }
        }
        return false;
    }
}
