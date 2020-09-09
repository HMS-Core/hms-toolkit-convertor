package com.huawei.codebot.analyzer.x2y.java.field.access.codegen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The map of Type of name and type
 *
 * @since 2020-05-20
 */

public class Mapping {
    public static final Map<String, String> TYPE_OF_NAME = Collections.unmodifiableMap(new HashMap() {
        {
            put("#BUILT_IN.int", "INT");
            put("#BUILT_IN.char", "CHAR");
            put("#BUILT_IN.byte", "BYTE");
            put("#BUILT_IN.short", "SHORT");
            put("#BUILT_IN.string", "STRING");
            put("java.lang.String", "PACKAGE_STRING");
            put("java.lang.Enum", "PACKAGE_ENUM");
        }
    });

    public static final Map<String, String> TYPE_OF_TYPE = Collections.unmodifiableMap(new HashMap() {
        {
            put("#BUILT_IN.int", "int");
            put("#BUILT_IN.char", "char");
            put("#BUILT_IN.byte", "byte");
            put("#BUILT_IN.short", "short");
            put("#BUILT_IN.string", "string");
            put("java.lang.String", "String");
            put("java.lang.Enum", "Enum");
        }
    });
}
