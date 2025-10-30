package com.alamafa.jfx.viewmodel.window;

import com.alamafa.config.Configuration;
import com.alamafa.core.ApplicationContext;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;
import com.alamafa.jfx.view.FxView;
import com.alamafa.jfx.view.FxViewLoader;
import com.alamafa.jfx.view.annotation.PostShow;
import com.alamafa.jfx.view.annotation.PreClose;
import com.alamafa.jfx.view.meta.FxViewDescriptor;
import com.alamafa.jfx.view.meta.FxViewRegistry;
import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.FxViewModelBinder;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class FxWindowManager {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(FxWindowManager.class);

    private final ApplicationContext context;
    private final FxViewLoader viewLoader;
    private final FxViewModelBinder binder;
    private final FxViewRegistry viewRegistry;
    private final Configuration configuration;
    private final ClassLoader classLoader;

    private final Map<Stage, FxWindowHandle> openWindows = new ConcurrentHashMap<>();
    private FxWindowHandle primaryHandle;

    public FxWindowManager(ApplicationContext context,
                           FxViewLoader viewLoader,
                           FxViewModelBinder binder,
                           FxViewRegistry viewRegistry) {
        this.context = Objects.requireNonNull(context, "context");
        this.viewLoader = Objects.requireNonNull(viewLoader, "viewLoader");
        this.binder = Objects.requireNonNull(binder, "binder");
        this.viewRegistry = Objects.requireNonNull(viewRegistry, "viewRegistry");
        this.configuration = context.get(com.alamafa.config.Configuration.class);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        this.classLoader = loader != null ? loader : FxWindowManager.class.getClassLoader();
    }

    public FxWindowHandle openWindow(Class<?> viewType, FxWindowOptions options) {
        Objects.requireNonNull(viewType, "viewType");
        FxWindowOptions effectiveOptions = options != null ? options : FxWindowOptions.builder().build();
        FxViewDescriptor descriptor = viewLoader.descriptor(viewType);
        FxView<?> view = viewLoader.load(viewType);
        Object controller = view.controller();
        FxViewModel viewModel = controller != null ? binder.bindToController(controller) : null;

        Stage stage = new Stage(resolveStageStyle(effectiveOptions, descriptor));
        configureModality(stage, effectiveOptions);
        if (effectiveOptions.owner() != null) {
            stage.initOwner(effectiveOptions.owner());
        }
        Scene scene = new Scene(view.root());
        stage.setScene(scene);
        applyWindowMetadata(stage, descriptor, effectiveOptions, view);
        registerLifecycleHandlers(stage, view, controller, viewModel);

        FxWindowHandle handle = new FxWindowHandle(stage, view, viewModel, controller, descriptor);
        openWindows.put(stage, handle);
        if (effectiveOptions.showAndWait()) {
            stage.showAndWait();
        } else {
            stage.show();
        }
        return handle;
    }

    public void closeWindow(FxWindowHandle handle) {
        if (handle != null) {
            handle.close();
        }
    }

    public void closeAll() {
        new ArrayList<>(openWindows.keySet()).forEach(Stage::close);
        if (primaryHandle != null && primaryHandle.stage() != null) {
            primaryHandle.stage().close();
        }
    }

    public void mountPrimaryStage(Stage stage) {
        Objects.requireNonNull(stage, "stage");
        Optional<FxViewDescriptor> descriptorOpt = resolvePrimaryDescriptor();
        if (descriptorOpt.isEmpty()) {
            LOGGER.debug("No primary FxViewSpec registered; skipping primary stage mount");
            return;
        }
        FxViewDescriptor descriptor = descriptorOpt.get();
        FxView<?> view = viewLoader.load(descriptor.type());
        Object controller = view.controller();
        FxViewModel viewModel = controller != null ? binder.bindToController(controller) : null;

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(view.root());
            stage.setScene(scene);
        } else {
            scene.setRoot(view.root());
        }
        applyWindowMetadata(stage, descriptor, FxWindowOptions.builder().build(), view);
        registerLifecycleHandlers(stage, view, controller, viewModel);
        primaryHandle = new FxWindowHandle(stage, view, viewModel, controller, descriptor);
        stage.show();
    }

    public void unmountPrimaryStage() {
        if (primaryHandle != null) {
            binder.unbind(primaryHandle.viewModel());
            primaryHandle = null;
        }
    }

    private void configureModality(Stage stage, FxWindowOptions options) {
        Modality modality = options.modality();
        if (modality != null) {
            stage.initModality(modality);
        }
    }

    private StageStyle resolveStageStyle(FxWindowOptions options, FxViewDescriptor descriptor) {
        StageStyle style = options.stageStyle();
        return style == null ? StageStyle.DECORATED : style;
    }

    private void applyWindowMetadata(Stage stage,
                                     FxViewDescriptor descriptor,
                                     FxWindowOptions options,
                                     FxView<?> view) {
        String title = firstNonBlank(options.title(), configuredValue(descriptor, "title"), descriptor.title());
        if (title != null && !title.isBlank()) {
            stage.setTitle(title);
        }

        Double width = chooseNumber(options.width(), configuredDouble(descriptor, "width"), descriptor.width());
        Double height = chooseNumber(options.height(), configuredDouble(descriptor, "height"), descriptor.height());
        Boolean resizable = chooseBoolean(options.resizable(), configuredBoolean(descriptor, "resizable"), descriptor.resizable());

        if (width != null && width > 0) {
            stage.setWidth(width);
        } else if (view.root() instanceof Region region && region.getPrefWidth() > 0) {
            stage.setWidth(region.getPrefWidth());
        }
        if (height != null && height > 0) {
            stage.setHeight(height);
        } else if (view.root() instanceof Region region && region.getPrefHeight() > 0) {
            stage.setHeight(region.getPrefHeight());
        }
        stage.setResizable(resizable != null ? resizable : descriptor.resizable());

        if ((width == null || width <= 0) && (height == null || height <= 0)) {
            stage.sizeToScene();
        }

        if (options.centerOnScreen()) {
            stage.centerOnScreen();
        }
    }

    private String configuredValue(FxViewDescriptor descriptor, String suffix) {
        if (configuration == null) {
            return null;
        }
        return configuration.get(configKey(descriptor, suffix)).orElse(null);
    }

    private Double configuredDouble(FxViewDescriptor descriptor, String suffix) {
        if (configuration == null) {
            return null;
        }
        return configuration.get(configKey(descriptor, suffix))
                .map(value -> {
                    try {
                        return Double.parseDouble(value.trim());
                    } catch (NumberFormatException ex) {
                        LOGGER.warn("Configuration key {} is not a valid number: {}", configKey(descriptor, suffix), value);
                        return null;
                    }
                })
                .orElse(null);
    }

    private Boolean configuredBoolean(FxViewDescriptor descriptor, String suffix) {
        if (configuration == null) {
            return null;
        }
        return configuration.get(configKey(descriptor, suffix))
                .map(value -> Boolean.parseBoolean(value.trim()))
                .orElse(null);
    }

    private String configKey(FxViewDescriptor descriptor, String suffix) {
        String name = descriptor.name();
        if (name == null || name.isBlank()) {
            name = descriptor.type().getSimpleName();
        }
        return "jfx.window." + name + '.' + suffix;
    }

    private void registerLifecycleHandlers(Stage stage,
                                           FxView<?> view,
                                           Object controller,
                                           FxViewModel viewModel) {
        EventHandler<WindowEvent> closeHandler = event -> {
            invokeLifecycleHooks(controller, PreClose.class, stage, view, viewModel, event);
            invokeLifecycleHooks(viewModel, PreClose.class, stage, view, viewModel, event);
            if (viewModel != null) {
                binder.unbind(viewModel);
            }
            openWindows.remove(stage);
            if (primaryHandle != null && primaryHandle.stage() == stage) {
                primaryHandle = null;
            }
        };
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, closeHandler);

        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<>() {
            @Override
            public void handle(WindowEvent event) {
                invokeLifecycleHooks(controller, PostShow.class, stage, view, viewModel, event);
                invokeLifecycleHooks(viewModel, PostShow.class, stage, view, viewModel, event);
                stage.removeEventHandler(WindowEvent.WINDOW_SHOWN, this);
            }
        });
    }

    private void invokeLifecycleHooks(Object target,
                                      Class<? extends Annotation> annotation,
                                      Stage stage,
                                      FxView<?> view,
                                      FxViewModel viewModel,
                                      WindowEvent event) {
        if (target == null) {
            return;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!method.isAnnotationPresent(annotation)) {
                continue;
            }
            Object[] args = buildArguments(method.getParameterTypes(), stage, view, viewModel, event);
            try {
                if (!method.canAccess(target)) {
                    method.setAccessible(true);
                }
                method.invoke(target, args);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.warn("Failed to invoke {} on {}", annotation.getSimpleName(), target.getClass().getName(), ex);
            }
        }
    }

    private Object[] buildArguments(Class<?>[] parameterTypes,
                                    Stage stage,
                                    FxView<?> view,
                                    FxViewModel viewModel,
                                    WindowEvent event) {
        if (parameterTypes.length == 0) {
            return new Object[0];
        }
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (Stage.class.isAssignableFrom(type)) {
                args[i] = stage;
            } else if (Scene.class.isAssignableFrom(type)) {
                args[i] = stage.getScene();
            } else if (Parent.class.isAssignableFrom(type)) {
                args[i] = view.root();
            } else if (FxView.class.isAssignableFrom(type)) {
                args[i] = view;
            } else if (FxViewModel.class.isAssignableFrom(type)) {
                args[i] = viewModel;
            } else if (WindowEvent.class.isAssignableFrom(type)) {
                args[i] = event;
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static Double chooseNumber(Double... values) {
        if (values == null) {
            return null;
        }
        for (Double value : values) {
            if (value != null && value > 0) {
                return value;
            }
        }
        return null;
    }

    private static Boolean chooseBoolean(Boolean... values) {
        if (values == null) {
            return null;
        }
        for (Boolean value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Optional<FxViewDescriptor> resolvePrimaryDescriptor() {
        Optional<FxViewDescriptor> descriptor = viewRegistry.primaryDescriptor();
        if (descriptor.isPresent()) {
            return descriptor;
        }
        String configured = configuration != null
                ? configuration.get("jfx.window.primary-view").orElse(null)
                : null;
        if (configured == null || configured.isBlank()) {
            return Optional.empty();
        }
        try {
            Class<?> viewType = Class.forName(configured.trim(), true, classLoader);
            return Optional.of(viewLoader.descriptor(viewType));
        } catch (ClassNotFoundException ex) {
            LOGGER.warn("Configured primary view class {} not found", configured, ex);
            return Optional.empty();
        } catch (Exception ex) {
            LOGGER.warn("Failed to resolve configured primary view {}", configured, ex);
            return Optional.empty();
        }
    }
}
