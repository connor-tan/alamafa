package com.alamafa.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

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
            findPropertyValue(source, normalizedPrefix, field)
                    .ifPresent(value -> applyValue(target, field, value));
        }
    }

    private static Optional<String> findPropertyValue(Configuration source,
                                                      String normalizedPrefix,
                                                      Field field) {
        for (String candidate : propertyNameCandidates(normalizedPrefix, field.getName())) {
            Optional<String> value = source.get(candidate);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private static List<String> propertyNameCandidates(String normalizedPrefix, String fieldName) {
        List<String> candidates = new ArrayList<>(6);
        String dottedPrefix = normalizedPrefix == null ? "" : normalizedPrefix;
        String plainPrefix = dottedPrefix.endsWith(".") && dottedPrefix.length() > 0
                ? dottedPrefix.substring(0, dottedPrefix.length() - 1)
                : dottedPrefix;
        addCandidate(candidates, dottedPrefix, toPropertyName(fieldName));
        addCandidate(candidates, dottedPrefix, toKebabCase(fieldName));
        addCandidate(candidates, dottedPrefix, fieldName);
        if (!plainPrefix.isEmpty()) {
            String kebabPrefix = plainPrefix.replace('.', '-');
            addCandidate(candidates, kebabPrefix.isEmpty() ? "" : kebabPrefix + '-', toKebabCase(fieldName));
            addCandidate(candidates, kebabPrefix.isEmpty() ? "" : kebabPrefix + '-', fieldName);
        }
        // Ensure direct field name without prefix is the last fallback (useful for prefix="")
        if (dottedPrefix.isEmpty()) {
            addCandidate(candidates, "", toPropertyName(fieldName));
            addCandidate(candidates, "", toKebabCase(fieldName));
            addCandidate(candidates, "", fieldName);
        }
        return candidates;
    }

    private static void addCandidate(List<String> target, String prefix, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String normalizedPrefix = prefix == null ? "" : prefix;
        String candidate;
        if (normalizedPrefix.isEmpty()) {
            candidate = value;
        } else if (normalizedPrefix.endsWith(".") || normalizedPrefix.endsWith("-")) {
            candidate = normalizedPrefix + value;
        } else {
            candidate = normalizedPrefix + value;
        }
        if (!target.contains(candidate)) {
            target.add(candidate);
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

    private static String toKebabCase(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = fieldName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            if (current == '_') {
                if (!endsWith(builder, '-')) {
                    builder.append('-');
                }
                continue;
            }
            if (Character.isUpperCase(current)) {
                boolean nextIsLower = (i + 1 < chars.length) && Character.isLowerCase(chars[i + 1]);
                boolean prevIsLowerOrDigit = (i > 0) && (Character.isLowerCase(chars[i - 1]) || Character.isDigit(chars[i - 1]));
                boolean prevIsUpper = (i > 0) && Character.isUpperCase(chars[i - 1]);
                if (builder.length() > 0
                        && !endsWith(builder, '-')
                        && (prevIsLowerOrDigit || (prevIsUpper && nextIsLower))) {
                    builder.append('-');
                }
                builder.append(Character.toLowerCase(current));
            } else {
                if (current == '-') {
                    if (!endsWith(builder, '-')) {
                        builder.append('-');
                    }
                } else {
                    builder.append(Character.toLowerCase(current));
                }
            }
        }
        int length = builder.length();
        while (length > 0 && builder.charAt(length - 1) == '-') {
            builder.setLength(length - 1);
            length = builder.length();
        }
        return builder.toString();
    }

    private static boolean endsWith(StringBuilder builder, char character) {
        return builder.length() > 0 && builder.charAt(builder.length() - 1) == character;
    }
}
