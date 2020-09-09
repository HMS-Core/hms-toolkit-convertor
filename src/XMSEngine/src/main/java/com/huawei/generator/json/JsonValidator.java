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

package com.huawei.generator.json;

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.utils.TypeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JClass validator.
 *
 * @since 2019-12-05
 */
public final class JsonValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonValidator.class);

    private static final List<String> VALID_CLASS_TYPES = Arrays.asList("interface", "class", "enum", "annotation");

    private static final List<String> VALID_CLASS_MODIFIERS =
        Arrays.asList("public", "protected", "private", "final", "static", "abstract");

    private static final List<String> VALID_METHOD_MODIFIERS =
        Arrays.asList("public", "protected", "private", "final", "static", "abstract", "synchronized");

    private String path;

    private JClass def;

    private boolean result;

    /**
     * Validates a JClass.
     *
     * @param path json path
     * @param def JClass
     * @return whether the JClass is valid
     */
    public static boolean validate(String path, JClass def) {
        return new JsonValidator(path, def).validate().getResult();
    }

    private JsonValidator(String path, JClass def) {
        result = true;
        this.path = path;
        this.def = def;
    }

    private void ensure(boolean expr, String message) {
        if (expr) {
            return;
        }
        result = false;
        LOGGER.error("invalid json: {}", path);
        LOGGER.error("        cause: {}", message);
    }

    private <T> void ensureNonNull(T obj, String name) {
        ensure(obj != null, name + " is null");
    }

    private JsonValidator validate() {
        validateJClass();
        return this;
    }

    private void validateJClass() {
        ensureNonNull(def.gName(), "gName");
        ensureNonNull(def.hName(), "hName");
        ensureNonNull(def.modifiers(), "modifiers");
        ensureNonNull(def.type(), "type");
        ensureNonNull(def.interfaces(), "interfaces");
        ensureNonNull(def.superClass(), "superClass");
        ensureNonNull(def.methods(), "methods");
        ensureNonNull(def.fields(), "fields");

        ensure(!def.gName().isEmpty(), "gName is empty");
        ensure(isValidClassName(def.gName()), "gName is not a valid class name");

        // check class type is valid
        ensure(VALID_CLASS_TYPES.contains(def.type()), "unknown class type: " + def.type());

        // check class modifiers are all valid
        ensure(VALID_CLASS_MODIFIERS.containsAll(def.modifiers()),
            "unknown class modifier: " + def.modifiers()
                .stream()
                .filter(m -> !VALID_CLASS_MODIFIERS.contains(m))
                .collect(Collectors.joining(", ")));

        // check interfaces are all given as full name
        ensure(def.interfaces().stream().allMatch(this::isValidClassName), "interface is not valid class name: "
            + def.interfaces().stream().filter(it -> !isValidClassName(it)).collect(Collectors.joining(", ")));

        // check superClass is full name
        ensure(def.superClass().isEmpty() || isValidClassName(def.superClass()), "invalid superClass");

        if (def.type() != null && (def.type().equals("interface") || def.type().equals("annotation"))) {
            ensure(def.superClass().isEmpty(), "interfaces should not have super class");
        }

        def.methods().forEach(this::validateMethod);
        def.fields().forEach(this::validateField);

        doOtherChecks();
    }

    private void doOtherChecks() {
        checkAbstract();
        eliminateAndroidClass();
        eliminateDuplicateMethods();
    }

    private void validateMethod(JMapping<JMethod> map) {
        validateMapping(map);
        validateMethod(map.h());
        validateMethod(map.g());
        validateMethodReturnType(map);
    }

    private void validateField(JMapping<JFieldOrMethod> map) {
        validateMapping(map);
        if (map.g() != null) {
            ensure(map.g().isJField(), "g is not a field");
            validateField(map.g().asJField());
        }
        if (map.h() != null) {
            if (map.h().isJField()) {
                validateField(map.h().asJField());
            }
            // else?
        }
    }

    private void validateMapping(JMapping map) {
        ensureNonNull(map.status(), "mapping status");
        if (map.status() == null) {
            return;
        }
        if (map.isMatching()) {
            ensureNonNull(map.g(), "mapping.G ");
            ensureNonNull(map.h(), "mapping.H ");
        } else if (map.isRedundant()) {
            ensure(map.g() == null, "mapping with status=redundant got a G ");
            ensureNonNull(map.h(), "mapping.H ");
        } else {
            ensureNonNull(map.g(), "mapping.G ");
            ensure(map.h() == null, "mapping with status=todo got an H ");
        }
    }

    private void validateMethod(JMethod method) {
        if (method == null) {
            return;
        }
        ensureNonNull(method.exceptions, "method exceptions ");
        ensureNonNull(method.modifiers, "method modifiers ");
        ensureNonNull(method.name, "method name ");
        ensureNonNull(method.parameterTypes, "method parameters ");
        ensureNonNull(method.returnType, "method return type ");

        ensure(!method.name.isEmpty() || !method.returnType.isEmpty(),
            "method name and return type should not be both empty");

        ensure(VALID_METHOD_MODIFIERS.containsAll(method.modifiers),
            "invalid method modifier found: " + method.modifiers.stream()
                .filter(it -> !VALID_METHOD_MODIFIERS.contains(it))
                .collect(Collectors.joining(", ")));
        ensure(method.name.isEmpty() || method.name.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '_'),
            "invalid method name: " + method.name);
        if (!method.returnType.isEmpty()) {
            ensure(TypeNode.create(method.returnType).getTypeName() != null,
                "no return type specified: " + method.returnType);
        }
    }

    private void validateField(JField field) {
        ensure(TypeNode.create(field.type()) != null, "null field type");
        String type = TypeNode.create(field.type()).getTypeName();
        if (Character.isLowerCase(type.charAt(0)) && !type.contains(".")) {
            ensure(
                TypeUtils.isPrimitiveType(TypeNode.create(type).getTypeName())
                    || TypeUtils.isPrimitiveTypeArray(TypeNode.create(type).getTypeName()),
                "field type is not valid : " + type);
        }
    }

    private boolean isValidClassName(String s) {
        return s.contains(".");
    }

    private boolean getResult() {
        return result;
    }

    private void checkAbstract() {
        if (!def.type().equals("class")) {
            return;
        }
        if (!def.modifiers().contains("abstract")) {
            ensure(def.methods().stream().allMatch(m -> {
                boolean flag = true;
                if (m.g() != null) {
                    flag = !m.g().modifiers.contains("abstract");
                }
                return flag;
            }), "abstract method defined in non-abstract class");
        }
    }

    private void eliminateAndroidClass() {
        ensure(!def.gName().startsWith("java") && !def.gName().startsWith("android"),
            "Found android class in json definition");
    }

    private void eliminateDuplicateMethods() {
        List<JMapping<JMethod>> methodList = def.methods();
        for (int i = 0; i < methodList.size(); i++) {
            JMapping<JMethod> it = methodList.get(i);
            if (it.status() == null) {
                continue;
            }
            for (int j = i + 1; j < methodList.size(); j++) {
                JMapping<JMethod> other = methodList.get(j);
                if (other.status() == null) {
                    continue;
                }
                ensure(!isDuplicateMethods(it, other), "Found duplicate methods.");
            }
        }
    }

    private boolean isDuplicateMethods(JMapping<JMethod> one, JMapping<JMethod> other) {
        if (!one.status().equals(other.status())) {
            return false;
        }
        JMethod oneG = one.g();
        JMethod otherG = other.g();
        if (oneG != null && otherG != null && !oneG.sameAs(otherG)) {
            return false;
        }
        JMethod oneH = one.h();
        JMethod otherH = other.h();
        return oneH == null || otherH == null || oneH.sameAs(otherH);
    }

    private void validateMethodReturnType(JMapping<JMethod> mapping) {
        if (mapping.g() == null) {
            return;
        }
        int ltIndex = def.gName().indexOf('<');
        String classShortName = def.gName();
        if (ltIndex >= 0) {
            classShortName = classShortName.substring(0, ltIndex);
        }
        classShortName = classShortName.substring(1 + classShortName.lastIndexOf('.'));
        if (mapping.g().returnType.isEmpty()) {
            ensure(mapping.g().name.equals(classShortName),
                "        invalid return type in method \"" + mapping.g().name + "\"");
        } else {
            ensure(!mapping.g().name.equals(classShortName),
                "        found constructor with empty return type" + mapping.g().name);
        }
    }
}
