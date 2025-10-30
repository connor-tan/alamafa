package com.alamafa.jfx.viewmodel;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.BeanRegistry;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base class for JavaFX-oriented view models. It exposes the {@link ApplicationContext} supplied
 * by the framework and offers convenience helpers for retrieving collaborators lazily.
 *
 * Lifecycle contract:
 * <ul>
 *     <li>{@link #attach(ApplicationContext)} is invoked when the view model is created and wired.</li>
 *     <li>{@link #onActive()} is called once the view is displayed to the user.</li>
 *     <li>{@link #onInactive()} should release UI-bound resources.</li>
 *     <li>{@link #detach()} finalises the view model prior to disposal.</li>
 * </ul>
 */
public abstract class FxViewModel {
    private ApplicationContext context;
    private BeanRegistry registry;
    private boolean attached;

    public final void attach(ApplicationContext applicationContext) {
        if (attached) {
            return;
        }
        this.context = Objects.requireNonNull(applicationContext, "applicationContext");
        this.registry = context.get(BeanRegistry.class);
        attached = true;
        onAttach();
    }

    public final void detach() {
        if (!attached) {
            return;
        }
        try {
            onDetach();
        } finally {
            attached = false;
            context = null;
            registry = null;
        }
    }

    /**
     * Invoked right after the view model is associated with the Alamafa application context.
     */
    protected void onAttach() {
        // default no-op
    }

    /**
     * Called when the view is visible/active (should be overridden as needed).
     */
    public void onActive() {
        // default no-op
    }

    /**
     * Called when the view is hidden or being deactivated.
     */
    public void onInactive() {
        // default no-op
    }

    /**
     * Invoked before the view model is disposed and the context reference is cleared.
     */
    protected void onDetach() {
        // default no-op
    }

    protected final ApplicationContext context() {
        ensureAttached();
        return context;
    }

    protected final <T> T bean(Class<T> type) {
        ensureAttached();
        if (registry != null && registry.hasBeanDefinition(type)) {
            return registry.get(type);
        }
        T candidate = context.get(type);
        if (candidate == null) {
            throw new IllegalStateException("No bean of type " + type.getName() + " available");
        }
        return candidate;
    }

    protected final <T> Supplier<T> lazyBean(Class<T> type) {
        return () -> bean(type);
    }

    private void ensureAttached() {
        if (!attached || context == null) {
            throw new IllegalStateException("ViewModel not attached to an ApplicationContext");
        }
    }
}
