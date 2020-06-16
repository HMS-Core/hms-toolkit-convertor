package com.huawei.generator.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum for string constant frequently used in modifiers
 *
 * @since 2020-05-12
 */
public enum Modifier {
    ABSTRACT("abstract"),

    FINAL("final"),

    STATIC("static"),

    PUBLIC("public"),

    PRIVATE("private"),

    PROTECTED("protected"),

    DEFAULT("default"),

    SYNCHRONIZED("synchronized"),

    TRANSIENT("transient"),

    VOLATILE("volatile"),

    NATIVE("native"),

    STRICTFP("strictfp");

    private String name;

    Modifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getModifierNames() {
        return Arrays.stream(values()).map(Modifier::getName).collect(Collectors.toList());
    }
}
