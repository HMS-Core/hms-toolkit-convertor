package org.xms.adapter.utils;

import java.lang.reflect.Field;

public class ReflectionUtils {
    public static <T> T readField(Class<T> fieldType, Object obj, String fieldName) {
        Field field = getField(obj.getClass(), fieldName);
        try {
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object readField(Object obj, String fieldName) {
        Field field = getField(obj.getClass(), fieldName);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object readField(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getField(Class<?> cls, String fieldName) {
        Field field = null;
        for (Class<?> sup = cls; sup != null; sup = sup.getSuperclass()) {
            try {
                field = sup.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                // ignore
            }
        }

        // search in interfaces?
        return field;
    }

    public static boolean writeField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeField(Object target, String fieldName, Object value) {
        Field f = getField(target.getClass(), fieldName);
        return writeField(f, target, value);
    }

    public static void copyFieldValue(Object dest, Object src, String fieldName) {
        Field f = getField(src.getClass(), fieldName);
        if (f == null) {
            return;
        }
        Object o = readField(f, src);
        writeField(f, dest, o);
    }
}
