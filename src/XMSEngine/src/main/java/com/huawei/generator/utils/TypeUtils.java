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

import static com.huawei.generator.utils.XMSUtils.deArray;
import static com.huawei.generator.utils.XMSUtils.degenerify;

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.mirror.KClass;
import com.huawei.generator.mirror.KClassReader;
import com.huawei.generator.mirror.SupersVisitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Type utils
 *
 * @since 2019-12-05
 */
public class TypeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeUtils.class);

    private static final String BOOLEAN_TYPE = "boolean";

    private static final String BYTE_TYPE = "byte";

    private static final String CHAR_TYPE = "char";

    private static final String SHORT_TYPE = "short";

    private static final String INT_TYPE = "int";

    private static final String FLOAT_TYPE = "float";

    private static final String DOUBLE_TYPE = "double";

    private static final String LONG_TYPE = "long";

    private static final String VOID_TYPE = "void";

    private static final List<String> PRIMITIVE_TYPES =
        Arrays.asList(BOOLEAN_TYPE, BYTE_TYPE, CHAR_TYPE, SHORT_TYPE, INT_TYPE, FLOAT_TYPE, DOUBLE_TYPE, LONG_TYPE);

    private static KClassReader util = KClassReader.INSTANCE;

    public static List<String> getPrimitiveTypes() {
        return PRIMITIVE_TYPES;
    }

    /**
     * Determine if it is a GMS type.
     * 
     * @param target target class name
     * @return if is gms type return true, otherwise false.
     */
    public static boolean isGmsType(String target) {
        String className = deArray(degenerify(target));
        if (util.getGClassList() == null) {
            LOGGER.error("gClassList is null, Please initialize!");
        }
        // avoid incomplete gms.json
        if (util.getGClassList()
            .containsKey(className) != (className.startsWith("com.google.android.gms")
                || className.startsWith("com.google.firebase") || className.startsWith("com.google.ads")
                || className.startsWith("com.android.installreferrer")
                || className.startsWith("com.google.android.libraries") || className.startsWith("com.google.api"))) {
            throw new IllegalStateException("could not find class: " + className);
        }
        return util.getGClassList().containsKey(className);
    }

    /**
     * Determine if it is a HMS type.
     *
     * @param target target class name
     * @return if is hms type return true, otherwise false.
     */
    public static boolean isHmsType(String target) {
        String className = deArray(degenerify(target));
        // avoid incomplete hms.json
        if (util.getHClassList().containsKey(className) != (className.startsWith("com.huawei.hms")
            || className.startsWith("com.huawei.hmf") || className.startsWith("com.huawei.agconnect"))) {
            throw new IllegalStateException("could not find class: " + className);
        }
        return util.getHClassList().containsKey(className);
    }

    /**
     * Determine if it is a android type include jdk type
     *
     * @param target target class name
     * @return if is android type return true, otherwise false.
     */
    public static boolean isAndroidType(String target) {
        String className = deArray(degenerify(target));
        // avoid incomplete android.json
        if (util.getAndroidClassList()
            .containsKey(className) != (className.startsWith("java") || className.startsWith("android"))) {
            throw new IllegalStateException("could not find class: " + className);
        }
        return util.getAndroidClassList().containsKey(className);
    }

    /**
     * Determine if it is a GMS interface class.
     *
     * @param target target class name
     * @return if is a GMS interface class return true, otherwise false.
     */
    public static boolean isGmsInterface(String target) {
        String className = deArray(degenerify(target));
        if (!isGmsType(className)) {
            return false;
        }
        return util.getGClassList().get(className).isInterface();
    }

    /**
     * Determine if it is a HMS interface class.
     *
     * @param target target class name
     * @return if is a HMS interface class return true, otherwise false.
     */
    public static boolean isHmsInterface(String target) {
        String className = deArray(degenerify(target));
        if (!isHmsType(className)) {
            return false;
        }
        return util.getHClassList().get(className).isInterface();
    }

    /**
     * Determine if it is a interface class.
     *
     * @param target target class name
     * @return if is a interface class return true, otherwise false.
     */
    public static boolean isInterface(String target) {
        String className = deArray(degenerify(target));
        if (isHmsType(className)) {
            return util.getHClassList().get(className).isInterface();
        } else if (isGmsType(className)) {
            return util.getGClassList().get(className).isInterface();
        } else if (isAndroidType(className)) {
            return util.getAndroidClassList().get(className).isInterface();
        } else if (XMSUtils.isX(className)) {
            return util.getGClassList().get(XMSUtils.xtoG(className)).isInterface();
        } else {
            throw new IllegalStateException("Missing type " + className + " in json");
        }
    }

    /**
     * Determine if it is a abstract class.
     *
     * @param target target class name
     * @return if is a interface class return true, otherwise false.
     */
    public static boolean isAbstract(String target) {
        String className = deArray(degenerify(target));
        if (isHmsType(className)) {
            return util.getHClassList().get(className).isAbstract();
        } else if (isGmsType(className)) {
            return util.getGClassList().get(className).isAbstract();
        } else if (isAndroidType(className)) {
            return util.getAndroidClassList().get(className).isAbstract();
        } else {
            throw new IllegalStateException("Missing type " + className + " in json");
        }
    }

    /**
     * Determine if it is a GMS abstract class.
     *
     * @param target target class name
     * @return if is a GMS abstract class return true, otherwise false.
     */
    public static boolean isGmsAbstract(String target) {
        String className = deArray(degenerify(target));
        if (!isGmsType(className)) {
            return false;
        }
        return util.getGClassList().get(className).isAbstract();
    }

    /**
     * Determine if it is a HMS abstract class.
     *
     * @param target target class name
     * @return if is a HMS abstract class return true, otherwise false.
     */
    public static boolean isHmsAbstract(String target) {
        String className = deArray(degenerify(target));
        if (!isHmsType(className)) {
            return false;
        }
        return util.getHClassList().get(className).isAbstract();
    }

    /**
     * Determine if it is a GMS normal class.
     *
     * @param target target class name
     * @return if is a GMS normal class return true, otherwise false.
     */
    public static boolean isGmsNormalClass(String target) {
        String className = deArray(degenerify(target));
        if (!isGmsType(className)) {
            return false;
        }
        KClass kClass = util.getGClassList().get(className);
        return !kClass.isAbstract() && !kClass.isInterface();
    }

    /**
     * Determine if it is a HMS normal class.
     *
     * @param target target class name
     * @return if is a HMS normal class return true, otherwise false.
     */
    public static boolean isHmsNormalClass(String target) {
        String className = deArray(degenerify(target));
        if (!isHmsType(className)) {
            return false;
        }
        KClass kClass = util.getHClassList().get(className);
        return !kClass.isAbstract() && !kClass.isInterface();
    }

    /**
     * Determine if it is a primitive type.
     *
     * @param type target type name
     * @return if is a primitive type return true, otherwise false.
     */
    public static boolean isPrimitiveType(String type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    /**
     * @param type target type name
     * @return if is a void type return true, otherwise false.
     */
    public static boolean isVoidType(String type) {
        return VOID_TYPE.equals(type);
    }

    /**
     * check typeName is PrimitiveTypeArray, such as byte[], int[], etc.
     *
     * @param typeName is name of type for checking
     * @return true is PrimitiveTypeArray otherwise return false.
     */
    public static boolean isPrimitiveTypeArray(String typeName) {
        for (String s : PRIMITIVE_TYPES) {
            if (typeName.trim().startsWith(s) && typeName.trim().endsWith("]")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if it is a boolean type.
     *
     * @param type target type name
     * @return if is a boolean type return true, otherwise false.
     */
    public static boolean isBooleanType(String type) {
        return BOOLEAN_TYPE.equals(type);
    }

    /**
     * check name is SparseArray.
     *
     * @param name is name of type for checking
     * @return true if is SparseArray otherwise return false.
     */
    public static boolean isSparseArray(String name) {
        return name.contains("SparseArray");
    }

    /**
     * check whether this type is generic.
     *
     * @param type is the type for checking
     * @return true is this is pure generic identifier, eg: E, XT, TResult
     */
    public static boolean isGenericIdentifier(TypeNode type) {
        String typeName = type.getTypeName().trim();
        return !TypeUtils.isPrimitiveType(typeName) && !TypeUtils.isPrimitiveTypeArray(typeName)
            && !TypeUtils.isVoidType(typeName) && !typeName.contains(".");
    }

    public static TypeNode h2X(TypeNode hType) {
        Map<String, GlobalMapping> hMappings = GlobalMapping.getHmappings();
        String hName = hType.getTypeName();
        if (!hMappings.containsKey(hName)) {
            throw new IllegalStateException("Missing " + hName + "in hMappings!");
        }
        GlobalMapping globalMapping = hMappings.get(hName);
        return TypeNode.create(globalMapping.getX());
    }

    public static TypeNode g2X(TypeNode gType) {
        return TypeNode.create(gType.getTypeName()).toX();
    }

    public static boolean isViewSubClass(TypeNode type, boolean isG) {
        Map<String, KClass> map;
        if (isG) {
            map = KClassReader.INSTANCE.getGClassList();
        } else {
            map = KClassReader.INSTANCE.getHClassList();
        }
        KClass kClass = map.get(type.getTypeName());
        List<KClass> classList = new SupersVisitor(kClass, map).visit();

        for (KClass cls : classList) {
            if (cls.getClassName().equals("android.view.View")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get upper bound type of array, List, Iterable,Set, SparseArray
     * 
     * @param paramType TypeNode of param
     * @param defTypes the definition info of generics
     * @return upper bound of generic type
     */
    public static String getUpperBound(TypeNode paramType, List<TypeNode> defTypes) {
        if (defTypes == null || defTypes.isEmpty()) {
            return AstConstants.OBJECT;
        } else {
            for (TypeNode defType : defTypes) {
                if (defType.getTypeName().equals(paramType.getTypeName())) {
                    if (defType.getSuperClass() == null || defType.getSuperClass().isEmpty()) {
                        return AstConstants.OBJECT;
                    } else {
                        return defType.getSuperClass().get(0).getTypeName();
                    }
                }
            }
            throw new IllegalStateException("No generic defined");
        }
    }

    public static boolean isSparseArray(TypeNode typeNode) {
        return typeNode.getTypeName().startsWith("android.util") && typeNode.getTypeName().contains("SparseArray");
    }

    public static boolean isList(TypeNode typeNode) {
        return typeNode.getTypeName().startsWith("java.util") && typeNode.getTypeName().contains("List");
    }

    public static boolean isSet(TypeNode typeNode) {
        return typeNode.getTypeName().startsWith("java.util") && typeNode.getTypeName().contains("Set");
    }

    /**
     * check name is Map.
     *
     * @param typeNode is name of type for checking
     * @return true if is map otherwise return false.
     */
    public static boolean isMap(TypeNode typeNode) {
        return typeNode.getTypeName().startsWith("java.util") && typeNode.getTypeName().contains("Map");
    }

    /**
     * check if this node is Iterable
     *
     * @param typeNode to be checked
     * @return true if this node is instance of Iterable
     */
    public static boolean isIterable(TypeNode typeNode) {
        return typeNode.getTypeName().startsWith("java.lang") && typeNode.getTypeName().contains("Iterable");
    }

    /**
     * check if this node is Iterator
     *
     * @param typeNode to be checked
     * @return true if this node is instance of Iterator
     */
    public static boolean isIterator(TypeNode typeNode) {
        return typeNode.getTypeName().startsWith("java.util") && typeNode.getTypeName().contains("Iterator");
    }

    /**
     * check one type whether is a Map, array, Set, List, SparseArray or Iterable
     *
     * @param typeNode to be checked
     * @return true if this TypeNode is Map, array, Set, List, SparseArray or Iterable
     */
    public static boolean needRemap(TypeNode typeNode) {
        if (isMap(typeNode)) {
            return isMapNeedRemap(typeNode);
        }
        if (typeNode.isArray()) {
            return isArrayNeedRemap(typeNode);
        }
        if (isSet(typeNode)) {
            return isSetNeedRemap(typeNode);
        }
        if (isList(typeNode)) {
            return isListNeedRemap(typeNode);
        }

        if (isSparseArray(typeNode)) {
            return isSparseArrayNeedRemap(typeNode);
        }
        if (isIterable(typeNode)) {
            return isIterableNeedRemap(typeNode);
        }
        if (isIterator(typeNode)) {
            return isIteratorNeedRemap(typeNode);
        }

        throw new IllegalStateException(typeNode.getTypeName() + " not supported now.");
    }

    private static boolean isArrayNeedRemap(TypeNode typeNode) {
        if (TypeUtils.isGenericIdentifier(typeNode)) {
            return true;
        } else {
            return XMSUtils.isX(typeNode.getTypeName());
        }
    }

    private static boolean isMapNeedRemap(TypeNode typeNode) {
        TypeNode valueType = typeNode.getGenericType().get(1);
        if (valueType.isArray()) {
            if (TypeUtils.isPrimitiveTypeArray(valueType.getTypeName())) {
                return false;
            }
            return XMSUtils.isX(valueType.getTypeName());
        } else {
            return XMSUtils.isX(valueType.getTypeName());
        }
    }

    private static boolean isSparseArrayNeedRemap(TypeNode typeNode) {
        TypeNode elementType = typeNode.getGenericType().get(0);
        if (TypeUtils.isGenericIdentifier(elementType)) {
            return true;
        } else {
            return XMSUtils.isX(elementType.getTypeName());
        }
    }

    private static boolean isListNeedRemap(TypeNode typeNode) {
        TypeNode elementType = typeNode.getGenericType().get(0);
        if (TypeUtils.isGenericIdentifier(elementType)) {
            return true;
        } else {
            return XMSUtils.isX(elementType.getTypeName());
        }
    }

    private static boolean isSetNeedRemap(TypeNode typeNode) {
        TypeNode elementType = typeNode.getGenericType().get(0);
        if (TypeUtils.isGenericIdentifier(elementType)) {
            return true;
        } else {
            return XMSUtils.isX(elementType.getTypeName());
        }
    }

    private static boolean isIterableNeedRemap(TypeNode typeNode) {
        TypeNode elementType = typeNode.getGenericType().get(0);
        if (TypeUtils.isGenericIdentifier(elementType)) {
            return true;
        } else {
            return XMSUtils.isX(elementType.getTypeName());
        }
    }

    private static boolean isIteratorNeedRemap(TypeNode typeNode) {
        TypeNode elementType = typeNode.getGenericType().get(0);
        if (TypeUtils.isGenericIdentifier(elementType)) {
            return true;
        } else {
            return XMSUtils.isX(elementType.getTypeName());
        }
    }

    /**
     * check one type whether is a collection or not
     *
     * @param typeNode to be checked
     * @return isContainer, true or false
     */
    public static boolean isCollection(TypeNode typeNode) {
        boolean isContainer = false;
        try {
            isContainer = Collection.class.isAssignableFrom(Class.forName(typeNode.getTypeName()));
            return isContainer;
        } catch (ClassNotFoundException e) {
            LOGGER.error("{} not found ", typeNode.getTypeName());
        }
        return isContainer;
    }

    /**
     * check one type whether is a type of container data structure or not
     *
     * @param typeNode to be checked
     * @return true if this node is a type of container data structure
     */
    public static boolean isNonSdkContainer(TypeNode typeNode) {
        if (typeNode.isArray()) {
            boolean isPrimitive = TypeUtils.isPrimitiveType(typeNode.getTypeName());
            if (isPrimitive) {
                return false;
            }
            String arrayElementType = typeNode.getTypeName();
            return !isSDKClass(arrayElementType);
        }
        if (typeNode.getGenericType() == null) {
            return false;
        }

        String tName = typeNode.getGenericType().get(0).getTypeName();
        if (typeNode.getTypeName().startsWith("java") && typeNode.getTypeName().contains("Map")) {
            tName = typeNode.getGenericType().get(1).getTypeName();
        }
        return (isList(typeNode) || isSet(typeNode) || isIterable(typeNode) || isSparseArray(typeNode)
            || isIterator(typeNode) || isMap(typeNode)) && !isSDKClass(tName);
    }

    public static boolean isNestedList(TypeNode typeNode) {
        if (typeNode.getGenericType() == null) {
            return false;
        }
        String tName = typeNode.getGenericType().get(0).getTypeName();
        if (typeNode.getTypeName().startsWith("java") && typeNode.getTypeName().contains("List")
            && typeNode.getGenericType().get(0).getTypeName().contains("java.util.List")) {
            tName = typeNode.getGenericType().get(0).getGenericType().get(0).getTypeName();
        }
        return typeNode.getTypeName().startsWith("java") && typeNode.getTypeName().contains("List")
            && typeNode.getGenericType().get(0).getTypeName().startsWith("java")
            && typeNode.getGenericType().get(0).getTypeName().contains("List") && !isSDKClass(tName);
    }

    private static boolean isSDKClass(String typeName) {
        return typeName.startsWith("java") || typeName.startsWith("javax") || typeName.startsWith("android")
            || typeName.startsWith("androidx");
    }
}
