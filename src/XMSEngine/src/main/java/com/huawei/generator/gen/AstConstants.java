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

/**
 * AstConstants class
 *
 * @since 2019-11-18
 */
public class AstConstants {
    public static final String OBJECT = "java.lang.Object";

    public static final String GET_Z_INSTANCE = "getZInstance";

    /**
     * XMS package name
     */
    public static final String XMS_PACKAGE = "org.xms.g.utils";

    public static final String XMS_OBJECT = XMS_PACKAGE + ".XObject";

    public static final String XMS_INTERFACE = XMS_PACKAGE + ".XInterface";

    /**
     * XEnum
     */
    public static final String XMS_ENUM = XMS_PACKAGE + ".XEnum";

    /**
     * XGettable, to show that this is a class with concrete getG/HInstance.
     */
    public static final String XMS_GETTABLE = XMS_PACKAGE + ".XGettable";

    public static final String PARCELABLE_INTERFACE = "android.os.Parcelable";

    public static final String CREATOR_TYPE = PARCELABLE_INTERFACE + ".Creator";

    public static final String CREATOR = "CREATOR";

    public static final String XMS_UTILS = XMS_PACKAGE + ".Utils";

    /**
     * Define array copy method
     */
    public static final String GENERIC_ARRAY_COPY = "genericArrayCopy";

    /**
     * Transform iterable data structure
     */
    public static final String TRANSFORM_ITERABLE = "transformIterable";

    /**
     * Map List data structure, the element is Generic
     */
    public static final String MAP_LIST_TO_GH = "mapList2GH";

    /**
     * Map Z list to X list.
     */
    public static final String MAP_LIST_TO_X = "mapList2X";

    /**
     * Map Set data structure, the element is Generic
     */
    public static final String MAP_SET_TO_GH = "mapSet2GH";

    public static final String MAP_ARRAY_TO_GH = "mapArray2GH";

    /**
     * Define map copy method
     */
    public static final String CONVERT_MAP = "convertMap";

    /**
     * map collection
     */
    public static final String MAP_COLLECTION = "mapCollection";

    /**
     * map list
     */
    public static final String MAP_LIST = "mapList";

    /**
     * Using reflect to get G,H instance
     */
    public static final String GET_INSTANCE_INTERFACE = "getInstanceInInterface";

    /**
     * Using reflect to get G,H instance
     */
    public static final String GET_XMS_BY_HMS = "getXmsObjectWithHmsObject";

    /**
     * Using reflect to get G,H instance
     */
    public static final String GET_XMS_BY_GMS = "getXmsObjectWithGmsObject";

    public static final String WRAP_INST = "wrapInst";

    public static final String INNER_CLASS_NAME = "XImpl";

    public static final String EMPTY = "";

    /**
     * Define prefix for X generics
     */
    public static final String GENERIC_PREFIX = "X";

    /**
     * Method name for getting instance reflectively.
     */
    public static final String REF_GET_INST = "getInstanceInInterface";

    /**
     * Used for generate call super method name
     */
    public static final String CALL_SUPER = "CallSuper";

    /**
     * Field name for wrapper
     */
    public static final String WRAPPER_FIELD = "wrapper";

    /**
     * String of this
     */
    public static final String THIS = "this";

    /**
     * String of getContext
     */
    public static final String GET_CONTEXT = "getContext";

    public static final String COLLECTION = "java.util.Collection";

    public static final String LIST = "java.util.List";

    public static final String GET_GMS_WITH_XMS = "getGmsClassWithXmsClass";

    public static final String GET_HMS_WITH_XMS = "getHmsClassWithXmsClass";

    public static final String JAVA_LANG_CLASS = "java.lang.Class";

    public static final String RUNTIME_EXCEPTION = "java.lang.RuntimeException";

    public static final String XMS_BOX = XMS_PACKAGE + ".XBox";

    public static final String IMPL = "Impl";
}
