package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Component;
import com.alamafa.di.annotation.Inject;
import com.alamafa.di.annotation.Qualifier;
import com.alamafa.di.BeanPostProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanInjectionFeaturesTest {

    @Test
    void supportsCollectionAndQualifiedInjection() throws Exception {
        RecorderPostProcessor.processed.clear();
        ApplicationContext context = new ApplicationContext();
        DiRuntimeBootstrap bootstrap = DiRuntimeBootstrap.builder()
                .scanPackages(getClass().getPackageName())
                .build();

        bootstrap.init(context);

        BeanRegistry registry = context.get(BeanRegistry.class);
        Service primary = registry.get(Service.class);
        assertTrue(primary instanceof PrimaryService);

        InjectionClient client = registry.get(InjectionClient.class);
        assertTrue(client.secondary instanceof SecondaryService);
        assertEquals(3, client.services.size());
        assertEquals(client.services.size(), client.serviceSet.size());
        assertTrue(client.missing.isEmpty());
        assertTrue(RecorderPostProcessor.processed.contains(InjectionClient.class));
    }

    interface Service {
        String name();
    }

    @Component(primary = true)
    static class PrimaryService implements Service {
        @Override
        public String name() {
            return "primary";
        }
    }

    @Component
    static class SecondaryService implements Service {
        @Override
        public String name() {
            return "secondary";
        }
    }

    @Component
    static class ThirdService implements Service {
        @Override
        public String name() {
            return "third";
        }
    }

    @Component
    static class InjectionClient {
        final List<Service> services;
        final Service secondary;
        final Optional<MissingService> missing;
        final Set<Service> serviceSet;

        @Inject
        InjectionClient(List<Service> services,
                        @Qualifier("secondaryService") Service secondary,
                        Optional<MissingService> missing,
                        Set<Service> serviceSet) {
            this.services = services;
            this.secondary = secondary;
            this.missing = missing;
            this.serviceSet = serviceSet;
        }
    }

    interface MissingService { }

    @Component
    static class RecorderPostProcessor implements BeanPostProcessor {
        static final java.util.List<Class<?>> processed = new java.util.ArrayList<>();

        @Override
        public void postProcess(Object bean, ApplicationContext context) {
            if (bean instanceof InjectionClient) {
                processed.add(bean.getClass());
            }
        }
    }
}
