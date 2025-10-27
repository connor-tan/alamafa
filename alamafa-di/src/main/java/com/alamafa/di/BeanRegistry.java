package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.internal.ConfigurationProcessor;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 极简 DI 注册表，负责管理 Bean 定义、创建逻辑以及单例缓存。
 */
public final class BeanRegistry {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(BeanRegistry.class);

    private final Map<Class<?>, CopyOnWriteArrayList<BeanDefinition<?>>> definitions = new ConcurrentHashMap<>();
    private final Map<String, BeanDefinition<?>> namedDefinitions = new ConcurrentHashMap<>();
    private final Map<BeanDefinition<?>, Object> singletonCache = new ConcurrentHashMap<>();
    private final Map<Object, List<Method>> preDestroyCallbacks = new ConcurrentHashMap<>();
    private final ApplicationContext context;
    private final BeanPostProcessorChain postProcessors = new BeanPostProcessorChain();
    private final ConfigurationProcessor configurationProcessor;
    private final ThreadLocal<Deque<Class<?>>> creationStack = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 绑定应用上下文并准备配置处理器与默认后处理器。
     */
    public BeanRegistry(ApplicationContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.configurationProcessor = new ConfigurationProcessor(this);
    }

    /**
     * 以类型为 key 注册 Bean 定义。
     */
    public <T> void register(Class<T> type, BeanDefinition<T> definition) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(definition, "definition");
        definitions.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>())
                .addIfAbsent(definition);
        LOGGER.debug("Registered bean {} with scope {} primary={} lazy={}", type.getName(),
                definition.scope(), definition.primary(), definition.lazy());
    }

    /**
     * 以名称为 key 注册 Bean 定义。
     */
    public <T> void register(String name, BeanDefinition<T> definition) {
        Objects.requireNonNull(definition, "definition");
        String key = Objects.requireNonNull(name, "name").trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Bean name must not be blank");
        }
        BeanDefinition<?> existing = namedDefinitions.putIfAbsent(key, definition);
        if (existing != null && existing != definition) {
            throw new BeanResolutionException("Bean name '" + key + "' already registered");
        }
        LOGGER.debug("Registered bean {} with name {} scope {} primary={} lazy={}",
                definition.type().getName(), key, definition.scope(), definition.primary(), definition.lazy());
    }

    /**
     * 根据类型获取 Bean，单例会被缓存。
     */
    public <T> T get(Class<T> type) {
        Objects.requireNonNull(type, "type");
        BeanDefinition<T> definition = resolveDefinition(type);
        return type.cast(getBeanInstance(definition));
    }

    /**
     * 根据名称获取 Bean，处理命名单例与原型。
     */
    public Object get(String name) {
        Objects.requireNonNull(name, "name");
        BeanDefinition<?> definition = namedDefinitions.get(name);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown bean name: " + name);
        }
        return getBeanInstance(definition);
    }

    private Object getBeanInstance(BeanDefinition<?> definition) {
        return definition.scope() == BeanDefinition.Scope.SINGLETON
                ? obtainSingleton(definition)
                : create(cast(definition));
    }

    /**
     * 获取或创建单例实例，确保所有别名共享同一对象。
     */
    private Object obtainSingleton(BeanDefinition<?> definition) {
        return singletonCache.computeIfAbsent(definition, key -> create(cast(key)));
    }

    @SuppressWarnings("unchecked")
    private static <T> BeanDefinition<T> cast(BeanDefinition<?> definition) {
        return (BeanDefinition<T>) definition;
    }

    /**
     * 判断是否存在指定类型的 Bean 定义。
     */
    public boolean hasBeanDefinition(Class<?> type) {
        CopyOnWriteArrayList<BeanDefinition<?>> defs = definitions.get(type);
        return defs != null && !defs.isEmpty();
    }

    /**
     * 判断是否存在指定名称的 Bean 定义。
     */
    public boolean hasBeanName(String name) {
        return namedDefinitions.containsKey(name);
    }

    /**
     * 记录单例的 @PreDestroy 方法，供关闭时调用。
     */
    public void registerPreDestroy(Object instance, List<Method> methods) {
        if (instance == null || methods == null || methods.isEmpty()) {
            return;
        }
        preDestroyCallbacks.put(instance, List.copyOf(methods));
    }

    /**
     * 创建 Bean 实例并执行后处理链，包含循环依赖检测。
     */
    private <T> T create(BeanDefinition<T> definition) {
        Deque<Class<?>> stack = creationStack.get();
        if (stack.contains(definition.type())) {
            throw new BeanResolutionException("Detected circular dependency while creating "
                    + definition.type().getName());
        }
        stack.push(definition.type());
        try {
            T instance = definition.supplier().get();
            postProcessors.apply(instance, context);
            registerPostProcessorIfNecessary(definition, instance);
            return instance;
        } catch (Exception ex) {
            throw new BeanResolutionException("Failed to create bean " + definition.type().getName(), ex);
        } finally {
            stack.pop();
            if (stack.isEmpty()) {
                creationStack.remove();
            }
        }
    }

    private void registerPostProcessorIfNecessary(BeanDefinition<?> definition, Object instance) {
        if (definition.scope() != BeanDefinition.Scope.SINGLETON) {
            return;
        }
        if (instance instanceof BeanPostProcessor processor) {
            postProcessors.add(processor);
        }
    }

    /**
     * 查询类型对应的 Bean 定义（仅当可唯一确定时返回）。
     */
    public <T> Optional<BeanDefinition<T>> definition(Class<T> type) {
        Objects.requireNonNull(type, "type");
        List<BeanDefinition<?>> candidates = definitions.get(type);
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(cast(selectCandidate(type, candidates)));
        } catch (BeanResolutionException ex) {
            return Optional.empty();
        }
    }

    /**
     * 返回底层的应用上下文。
     */
    public ApplicationContext context() {
        return context;
    }

    /**
     * 注册新的 BeanPostProcessor。
     */
    public void addPostProcessor(BeanPostProcessor processor) {
        postProcessors.add(processor);
    }

    /**
     * 注册配置类并触发其 Bean 定义。
     */
    public void registerConfigurations(Class<?>... configurationClasses) {
        if (configurationClasses == null) {
            return;
        }
        for (Class<?> configurationClass : configurationClasses) {
            if (configurationClass != null) {
                configurationProcessor.processConfiguration(configurationClass);
            }
        }
        configurationProcessor.registerComponents();
    }

    /**
     * 扫描指定包并注册发现的组件。
     */
    public void scanPackages(String... basePackages) {
        configurationProcessor.scanPackages(basePackages);
        configurationProcessor.registerComponents();
    }

    /**
     * 根据 class 推导包名进行扫描注册。
     */
    public void scanPackageClasses(Class<?>... basePackageClasses) {
        if (basePackageClasses == null) {
            return;
        }
        String[] packages = Arrays.stream(basePackageClasses)
                .filter(Objects::nonNull)
                .map(Class::getPackageName)
                .toArray(String[]::new);
        configurationProcessor.scanPackages(packages);
        configurationProcessor.registerComponents();
    }

    /**
     * 调用所有 @PreDestroy 方法并清空单例缓存。
     */
    public void destroySingletons() {
        for (Map.Entry<Object, List<Method>> entry : preDestroyCallbacks.entrySet()) {
            Object instance = entry.getKey();
            for (Method method : entry.getValue()) {
                try {
                    method.invoke(instance);
                } catch (Exception ex) {
                    LOGGER.warn("Failed to invoke @PreDestroy method {} on {}", method.getName(),
                            instance.getClass().getName(), ex);
                }
            }
        }
        preDestroyCallbacks.clear();
        singletonCache.clear();
    }

    /**
     * 初始化所有单例 BeanPostProcessor，确保在其他 Bean 创建前就已注册。
     */
    public void initializeSingletonPostProcessors() {
        Set<BeanDefinition<?>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        definitions.values().forEach(defs -> defs.forEach(definition -> {
            if (!seen.add(definition)) {
                return;
            }
            if (definition.scope() == BeanDefinition.Scope.SINGLETON
                    && BeanPostProcessor.class.isAssignableFrom(definition.type())) {
                get(definition.type());
            }
        }));
    }

    /**
     * 返回所有符合指定类型的 Bean 实例集合。
     */
    public <T> List<T> getBeansOfType(Class<T> type) {
        Objects.requireNonNull(type, "type");
        Set<BeanDefinition<?>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<T> beans = new ArrayList<>();
        definitions.forEach((registeredType, defs) -> {
            if (!type.isAssignableFrom(registeredType)) {
                return;
            }
            for (BeanDefinition<?> definition : defs) {
                if (seen.add(definition)) {
                    beans.add(type.cast(getBeanInstance(definition)));
                }
            }
        });
        return List.copyOf(beans);
    }

    /**
     * 返回某个类型下所有 BeanDefinition（只读）。
     */
    public List<BeanDefinition<?>> definitionsFor(Class<?> type) {
        List<BeanDefinition<?>> defs = definitions.get(type);
        return defs == null ? List.of() : List.copyOf(defs);
    }

    private <T> BeanDefinition<T> resolveDefinition(Class<T> type) {
        List<BeanDefinition<?>> candidates = definitions.get(type);
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("Unknown bean type: " + type.getName());
        }
        return cast(selectCandidate(type, candidates));
    }

    private BeanDefinition<?> selectCandidate(Class<?> requestedType, List<BeanDefinition<?>> candidates) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        List<BeanDefinition<?>> primaries = candidates.stream()
                .filter(BeanDefinition::primary)
                .toList();
        if (primaries.size() == 1) {
            return primaries.get(0);
        }
        if (primaries.size() > 1) {
            throw new BeanResolutionException("Multiple primary beans for type " + requestedType.getName());
        }
        throw new BeanResolutionException("Multiple beans of type " + requestedType.getName()
                + " found but none marked as primary");
    }
}
