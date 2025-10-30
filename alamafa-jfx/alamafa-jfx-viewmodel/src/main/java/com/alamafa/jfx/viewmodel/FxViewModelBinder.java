package com.alamafa.jfx.viewmodel;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;
import com.alamafa.jfx.view.FxView;
import com.alamafa.jfx.view.meta.FxViewDescriptor;
import com.alamafa.jfx.view.meta.FxViewRegistry;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;
import com.alamafa.jfx.viewmodel.meta.FxViewModelRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Helper that resolves a {@link FxViewModel} from the application context and associates it with a view.
 */
public final class FxViewModelBinder {
    private final ApplicationContext context;
    private final BeanRegistry registry;
    private final FxViewRegistry viewRegistry;
    private final FxViewModelRegistry viewModelRegistry;

    public FxViewModelBinder(ApplicationContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.registry = context.get(BeanRegistry.class);
        this.viewRegistry = context.get(FxViewRegistry.class);
        this.viewModelRegistry = resolveViewModelRegistry();
    }

    public FxViewModelBinder(ApplicationContext context,
                              BeanRegistry registry,
                              FxViewRegistry viewRegistry,
                              FxViewModelRegistry viewModelRegistry) {
        this.context = Objects.requireNonNull(context, "context");
        this.registry = registry;
        this.viewRegistry = Objects.requireNonNull(viewRegistry, "viewRegistry");
        this.viewModelRegistry = Objects.requireNonNull(viewModelRegistry, "viewModelRegistry");
    }

    public FxViewModel bindToController(Object controller) {
        if (controller == null) {
            return null;
        }
        FxViewModel viewModel = bind(controller);
        injectViewModel(controller, viewModel);
        return viewModel;
    }

    public <T extends FxViewModel> T bind(FxView<?> view, Class<T> type) {
        Objects.requireNonNull(view, "view");
        Objects.requireNonNull(type, "type");
        T viewModel = resolve(type);
        viewModel.attach(context);
        viewModel.onActive();
        return viewModel;
    }

    public void unbind(FxViewModel viewModel) {
        if (viewModel == null) {
            return;
        }
        try {
            viewModel.onInactive();
        } finally {
            if (shouldDetach(viewModel.getClass())) {
                viewModel.detach();
            }
        }
    }

    public FxViewModel bind(Object viewBean) {
        Objects.requireNonNull(viewBean, "viewBean");
        FxViewDescriptor descriptor = findDescriptor(viewBean.getClass());
        Class<?> viewModelType = descriptor.viewModelOptional()
                .orElseThrow(() -> new IllegalStateException("View " + viewBean.getClass().getName()
                        + " does not declare a viewModel type"));
        @SuppressWarnings("unchecked")
        FxViewModel viewModel = resolve((Class<? extends FxViewModel>) viewModelType);
        viewModel.attach(context);
        viewModel.onActive();
        return viewModel;
    }

    public <T extends FxViewModel> T bind(Object viewBean, Class<T> expectedType) {
        FxViewModel model = bind(viewBean);
        if (!expectedType.isInstance(model)) {
            throw new IllegalStateException("View model for " + viewBean.getClass().getName()
                    + " is not of type " + expectedType.getName());
        }
        return expectedType.cast(model);
    }

    private FxViewDescriptor findDescriptor(Class<?> viewType) {
        if (viewRegistry != null) {
            return viewRegistry.find(viewType)
                    .orElseThrow(() -> new IllegalStateException("No FxView descriptor registered for " + viewType.getName()));
        }
        throw new IllegalStateException("FxViewRegistry not available in ApplicationContext");
    }

    private <T extends FxViewModel> T resolve(Class<T> type) {
        if (viewModelRegistry != null) {
            return viewModelRegistry.obtain(context, registry, type);
        }
        if (registry != null && registry.hasBeanDefinition(type)) {
            return registry.get(type);
        }
        T viewModel = context.get(type);
        if (viewModel != null) {
            return viewModel;
        }
        throw new IllegalStateException("No FxViewModel of type " + type.getName() + " registered");
    }

    private FxViewModelRegistry resolveViewModelRegistry() {
        if (registry != null && registry.hasBeanDefinition(FxViewModelRegistry.class)) {
            return registry.get(FxViewModelRegistry.class);
        }
        return context.get(FxViewModelRegistry.class);
    }

    private boolean shouldDetach(Class<?> viewModelType) {
        if (viewModelRegistry == null) {
            return true;
        }
        return viewModelRegistry.scopeOf(viewModelType)
                .map(scope -> scope == FxViewModelScope.VIEW)
                .orElse(true);
    }

    private void injectViewModel(Object controller, FxViewModel viewModel) {
        if (controller == null || viewModel == null) {
            return;
        }
        Method setter = findViewModelSetter(controller.getClass(), viewModel.getClass());
        if (setter == null) {
            return;
        }
        try {
            if (!setter.canAccess(controller)) {
                setter.setAccessible(true);
            }
            setter.invoke(controller, viewModel);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("Failed to inject view model into controller "
                    + controller.getClass().getName(), ex);
        }
    }

    private Method findViewModelSetter(Class<?> controllerType, Class<?> viewModelType) {
        for (Method method : controllerType.getMethods()) {
            if (!method.getName().equals("setViewModel")) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) {
                continue;
            }
            if (params[0].isAssignableFrom(viewModelType) || viewModelType.isAssignableFrom(params[0])) {
                return method;
            }
        }
        return null;
    }
}
