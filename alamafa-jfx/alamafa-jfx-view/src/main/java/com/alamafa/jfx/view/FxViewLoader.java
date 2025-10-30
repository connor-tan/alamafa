package com.alamafa.jfx.view;

import com.alamafa.core.ApplicationContext;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.di.BeanRegistry;
import com.alamafa.jfx.view.annotation.FxViewSpec;
import com.alamafa.jfx.view.meta.FxViewDescriptor;
import com.alamafa.jfx.view.meta.FxViewRegistry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralised JavaFX view loader that integrates Alamafa's {@link ApplicationContext} and DI container.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Locate FXML resources via a pluggable {@link ResourceResolver}.</li>
 *     <li>Delegate controller instantiation to {@link BeanRegistry} when available.</li>
 *     <li>Expose {@link FxView} descriptors so callers can attach the view to the scene graph.</li>
 * </ul>
 */
public final class FxViewLoader {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(FxViewLoader.class);

    private final ApplicationContext context;
    private final BeanRegistry beanRegistry;
    private final ResourceResolver resolver;
    private final Supplier<ResourceBundle> bundles;
    private final FxViewRegistry viewRegistry;
    private final Map<Class<?>, FxView<?>> sharedViewCache = new ConcurrentHashMap<>();
    private final Map<String, ResourceBundle> bundleCache = new ConcurrentHashMap<>();

    public FxViewLoader(ApplicationContext context) {
        this(context, null, ResourceResolver.classpath(), () -> null);
    }

    public FxViewLoader(ApplicationContext context,
                        ResourceResolver resolver,
                        Supplier<ResourceBundle> bundles) {
        this(context, null, resolver, bundles);
    }

    public FxViewLoader(ApplicationContext context,
                        FxViewRegistry viewRegistry,
                        ResourceResolver resolver,
                        Supplier<ResourceBundle> bundles) {
        this.context = Objects.requireNonNull(context, "context");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.bundles = Objects.requireNonNull(bundles, "bundles");
        this.beanRegistry = context.get(BeanRegistry.class);
        this.viewRegistry = viewRegistry != null ? viewRegistry : context.get(FxViewRegistry.class);
    }

    public <T> FxView<T> load(String fxmlPath) {
        Objects.requireNonNull(fxmlPath, "fxmlPath");
        URL resource = resolver.resolve(fxmlPath);
        if (resource == null) {
            throw new ViewLoadingException("FXML resource not found: " + fxmlPath);
        }
        return loadFromUrl(resource, bundles.get(), null);
    }

    public FxViewDescriptor descriptor(Class<?> viewType) {
        return resolveDescriptor(Objects.requireNonNull(viewType, "viewType"));
    }

    public <T> FxView<T> load(Class<T> viewType) {
        Objects.requireNonNull(viewType, "viewType");
        FxViewDescriptor descriptor = descriptor(viewType);
        if (descriptor.shared()) {
            @SuppressWarnings("unchecked")
            FxView<T> cached = (FxView<T>) sharedViewCache.get(viewType);
            if (cached != null) {
                return cached;
            }
        }
        FxView<T> view = descriptor.fxmlOptional()
                .map(path -> loadByDescriptor(path, descriptor, viewType))
                .orElseGet(() -> instantiateView(viewType));
        if (descriptor.shared()) {
            sharedViewCache.putIfAbsent(viewType, view);
            @SuppressWarnings("unchecked")
            FxView<T> cached = (FxView<T>) sharedViewCache.get(viewType);
            return cached;
        }
        return view;
    }

    private <T> FxView<T> loadByDescriptor(String fxmlPath, FxViewDescriptor descriptor, Class<T> viewType) {
        URL resource = resolveResource(fxmlPath, viewType);
        ResourceBundle bundle = descriptor.bundleOptional()
                .map(this::loadBundle)
                .orElseGet(bundles);
        FxView<T> view = loadFromUrl(resource, bundle, viewType);
        applyStyles(view.root(), descriptor, viewType);
        return view;
    }

    private <T> FxView<T> instantiateView(Class<T> viewType) {
        T instance = resolveBean(viewType);
        if (instance instanceof Parent parent) {
            return new FxView<>(parent, instance);
        }
        throw new ViewLoadingException("View type " + viewType.getName()
                + " must either provide FXML (@FxViewSpec.fxml) or extend javafx.scene.Parent");
    }

    private <T> FxView<T> loadFromUrl(URL resource, ResourceBundle bundle, Class<?> viewType) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resource);
        loader.setResources(bundle);
        if (beanRegistry != null) {
            loader.setControllerFactory(this::resolveController);
        }
        try (InputStream input = resource.openStream()) {
            Parent root = loader.load(input);
            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            return new FxView<>(root, controller);
        } catch (IOException ex) {
            throw new ViewLoadingException("Failed to load FXML " + resource, ex);
        }
    }

    private void applyStyles(Parent root, FxViewDescriptor descriptor, Class<?> viewType) {
        if (root == null) {
            return;
        }
        List<String> styles = descriptor.styles();
        if (styles.isEmpty()) {
            return;
        }
        List<String> resolved = new ArrayList<>();
        for (String style : styles) {
            if (style == null || style.isBlank()) {
                continue;
            }
            String external = resolveStylesheet(style, viewType);
            if (external != null) {
                resolved.add(external);
            }
        }
        root.getStylesheets().addAll(resolved);
    }

    private ResourceBundle loadBundle(String baseName) {
        return bundleCache.computeIfAbsent(baseName, key -> {
            try {
                return ResourceBundle.getBundle(key, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
            } catch (Exception ex) {
                throw new ViewLoadingException("Failed to load resource bundle " + key, ex);
            }
        });
    }

    private URL resolveResource(String path, Class<?> viewType) {
        URL resource = resolver.resolve(path);
        if (resource != null) {
            return resource;
        }
        if (viewType != null) {
            resource = viewType.getResource(path);
            if (resource == null) {
                resource = viewType.getResource(path.startsWith("/") ? path : "/" + path);
            }
            if (resource != null) {
                return resource;
            }
            ClassLoader loader = viewType.getClassLoader();
            if (loader != null) {
                resource = loader.getResource(path.startsWith("/") ? path.substring(1) : path);
            }
        }
        if (resource == null) {
            throw new ViewLoadingException("Resource not found: " + path);
        }
        return resource;
    }

    private String resolveStylesheet(String path, Class<?> viewType) {
        try {
            URL resource = resolveResource(path, viewType);
            return resource != null ? resource.toExternalForm() : null;
        } catch (ViewLoadingException ex) {
            LOGGER.warn("Stylesheet {} not found for view {}", path, viewType != null ? viewType.getName() : "unknown", ex);
            return null;
        }
    }

    private Object resolveController(Class<?> type) {
        if (beanRegistry != null && beanRegistry.hasBeanDefinition(type)) {
            try {
                return beanRegistry.get(type);
            } catch (Exception ex) {
                LOGGER.warn("Failed to obtain controller {} from BeanRegistry, falling back to reflection", type, ex);
            }
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new ViewLoadingException("Failed to instantiate controller " + type.getName(), ex);
        }
    }

    private <T> T resolveBean(Class<T> type) {
        if (beanRegistry != null && beanRegistry.hasBeanDefinition(type)) {
            return beanRegistry.get(type);
        }
        T candidate = context.get(type);
        if (candidate != null) {
            return candidate;
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new ViewLoadingException("Failed to instantiate view " + type.getName(), ex);
        }
    }

    private FxViewDescriptor resolveDescriptor(Class<?> viewType) {
        FxViewDescriptor descriptor = null;
        if (viewRegistry != null) {
            descriptor = viewRegistry.find(viewType).orElse(null);
        }
        if (descriptor == null) {
            FxViewSpec spec = viewType.getAnnotation(FxViewSpec.class);
            if (spec != null) {
                descriptor = FxViewDescriptor.of(viewType,
                        spec.value(),
                        spec.fxml(),
                        spec.styles(),
                        spec.bundle(),
                        spec.viewModel(),
                        spec.shared(),
                        spec.primary(),
                        spec.title(),
                        spec.width(),
                        spec.height(),
                        spec.resizable());
                if (viewRegistry != null) {
                    viewRegistry.register(descriptor);
                }
            }
        }
        if (descriptor == null) {
            throw new ViewLoadingException("No @FxViewSpec metadata registered for " + viewType.getName());
        }
        return descriptor;
    }
}
