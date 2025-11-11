package com.alamafa.jfx.viewmodel.meta;

import com.alamafa.core.ApplicationContext;
import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class FxViewModelRegistryTest {

    @Test
    void shouldCreateNewInstancePerRequestWhenScopeDefaultsToView() {
        FxViewModelRegistry registry = new FxViewModelRegistry();
        ApplicationContext context = new ApplicationContext();

        TestViewModel first = registry.obtain(context, null, TestViewModel.class);
        TestViewModel second = registry.obtain(context, null, TestViewModel.class);

        assertNotSame(first, second);
    }

    @Test
    void shouldReuseInstanceForApplicationScopedModels() {
        FxViewModelRegistry registry = new FxViewModelRegistry();
        registry.register(new FxViewModelDescriptor(AppScopedViewModel.class, "appScoped", false, FxViewModelScope.APPLICATION));
        ApplicationContext context = new ApplicationContext();

        AppScopedViewModel first = registry.obtain(context, null, AppScopedViewModel.class);
        AppScopedViewModel second = registry.obtain(context, null, AppScopedViewModel.class);

        assertSame(first, second);
    }

    static class TestViewModel extends FxViewModel { }

    static class AppScopedViewModel extends FxViewModel { }
}
