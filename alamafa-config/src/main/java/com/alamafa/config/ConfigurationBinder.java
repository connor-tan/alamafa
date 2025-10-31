package com.alamafa.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Objects;

/**
 * 简单的属性绑定器，将 {@link Configuration} 数据映射到 POJO。
 */
public final class ConfigurationBinder {

    private ConfigurationBinder() {
    }

    /**
     * 将配置绑定到指定类型的新实例。
     */
    public static <T> T bind(Configuration source, Class<T> type) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");
        ConfigurationProperties properties = type.getAnnotation(ConfigurationProperties.class);
        String prefix = properties != null ? properties.prefix() : "";
        return bind(source, prefix, type);
    }

    /**
     * 指定前缀绑定到类型的新实例。
     */
    public static <T> T bind(Configuration source, String prefix, Class<T> type) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            bind(source, prefix, instance);
            return instance;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to instantiate configuration properties for " + type.getName(), ex);
        }
    }

    /**
     * 将配置绑定到现有对象。
     */
    public static void bind(Configuration source, String prefix, Object target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        String normalizedPrefix = normalizePrefix(prefix);
        Class<?> type = target.getClass();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String key = normalizedPrefix + toPropertyName(field.getName());
            source.get(key).ifPresent(value -> applyValue(target, field, value));
        }
    }

    private static void applyValue(Object target, Field field, String value) {
        Class<?> fieldType = field.getType();
        Object converted = convert(value, fieldType, field);
        Method setter = findSetter(target.getClass(), field);
        try {
            if (setter != null) {
                if (!setter.canAccess(target)) {
                    setter.setAccessible(true);
                }
                setter.invoke(target, converted);
            } else {
                boolean accessible = field.canAccess(target);
                if (!accessible) {
                    field.setAccessible(true);
                }
                field.set(target, converted);
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to bind property " + field.getName() + " on "
                    + target.getClass().getName(), ex);
        }
    }

    private static Method findSetter(Class<?> type, Field field) {
        String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        try {
            return type.getMethod(setterName, field.getType());
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Object convert(String value, Class<?> targetType, Field field) {
        if (targetType.equals(String.class)) {
            return value;
        }
        if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
            return Integer.parseInt(value);
        }
        if (targetType.equals(long.class) || targetType.equals(Long.class)) {
            return Long.parseLong(value);
        }
        if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        if (targetType.equals(double.class) || targetType.equals(Double.class)) {
            return Double.parseDouble(value);
        }
        if (targetType.equals(float.class) || targetType.equals(Float.class)) {
            return Float.parseFloat(value);
        }
        if (targetType.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
            return Enum.valueOf(enumType, value.trim());
        }
        throw new IllegalStateException("Unsupported configuration property type "
                + targetType.getName() + " for field " + field.getName());
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        String trimmed = prefix.trim();
        if (trimmed.endsWith(".")) {
            return trimmed;
        }
        return trimmed + ".";
    }

    private static String toPropertyName(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = fieldName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            if (current == '_') {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '.') {
                    builder.append('.');
                }
                continue;
            }
            if (Character.isUpperCase(current)) {
                boolean nextIsLower = (i + 1 < chars.length) && Character.isLowerCase(chars[i + 1]);
                boolean prevIsLowerOrDigit = (i > 0) && (Character.isLowerCase(chars[i - 1]) || Character.isDigit(chars[i - 1]));
                boolean prevIsUpper = (i > 0) && Character.isUpperCase(chars[i - 1]);
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '.'
                        && (prevIsLowerOrDigit || (prevIsUpper && nextIsLower))) {
                    builder.append('.');
                }
                builder.append(Character.toLowerCase(current));
            } else {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '.' && current == '.') {
                    continue;
                }
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '.' && current == '.') {
                    builder.append('.');
                } else {
                    builder.append(Character.toLowerCase(current));
                }
            }
        }
        int length = builder.length();
        while (length > 0 && builder.charAt(length - 1) == '.') {
            builder.setLength(length - 1);
            length = builder.length();
        }
        return builder.toString();
    }
}
