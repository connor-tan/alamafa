package com.alamafa.di.internal;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 表示一次依赖注入的目标（字段/参数）及其可选性。
 */
final class InjectionPoint {
    private final Type declaredType;
    private final Type elementType;
    private final Class<?> rawType;
    private final Class<?> collectionRawType;
    private final boolean optional;
    private final boolean wrapsOptional;
    private final String qualifier;
    private final String description;

    /**
     * 记录注入点类型、是否可选以及是否包裹 Optional 类型。
     */
    InjectionPoint(Type declaredType,
                   Type elementType,
                   Class<?> rawType,
                   Class<?> collectionRawType,
                   boolean optional,
                   boolean wrapsOptional,
                   String qualifier,
                   String description) {
        this.declaredType = Objects.requireNonNull(declaredType, "declaredType");
        this.elementType = Objects.requireNonNull(elementType, "elementType");
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.collectionRawType = collectionRawType;
        this.optional = optional;
        this.wrapsOptional = wrapsOptional;
        this.qualifier = qualifier == null ? null : qualifier.trim();
        this.description = Objects.requireNonNull(description, "description");
    }

    /** 返回声明的原始参数/字段类型。 */
    Type declaredType() {
        return declaredType;
    }

    /** 返回待解析的目标类型或集合元素类型。 */
    Type elementType() {
        return elementType;
    }

    /** 返回声明的原始类型。 */
    Class<?> rawType() {
        return rawType;
    }

    /** 若为集合注入，返回集合的原始类型，否则为 null。 */
    Class<?> collectionRawType() {
        return collectionRawType;
    }

    /** 是否标记为可选依赖。 */
    boolean optional() {
        return optional;
    }

    /** 注入点本身是否为 Optional。 */
    boolean wrapsOptional() {
        return wrapsOptional;
    }

    /** Qualifier 名称，可为空。 */
    String qualifier() {
        return qualifier;
    }

    /** 友好描述，用于错误信息。 */
    String description() {
        return description;
    }
}
