package com.alamafa.di;

import com.alamafa.core.ApplicationContext;
import com.alamafa.di.annotation.Component;
import com.alamafa.di.annotation.Inject;
import com.alamafa.di.annotation.Qualifier;
import com.alamafa.di.annotation.Service;
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
        BusinessService primary = registry.get(BusinessService.class);
        assertTrue(primary instanceof PrimaryService);

        InjectionClient client = registry.get(InjectionClient.class);
        assertTrue(client.secondary instanceof SecondaryService);
        assertEquals(3, client.services.size());
        assertEquals(client.services.size(), client.serviceSet.size());
        assertTrue(client.missing.isEmpty());
        assertTrue(RecorderPostProcessor.processed.contains(InjectionClient.class));
    }

    interface BusinessService {
        String name();
    }

    @Component(primary = true)
    static class PrimaryService implements BusinessService {
        @Override
        public String name() {
            return "primary";
        }
    }

    @Component
    static class SecondaryService implements BusinessService {
        @Override
        public String name() {
            return "secondary";
        }
    }

    @Service
    static class ThirdService implements BusinessService {
        @Override
        public String name() {
            return "third";
        }
    }

    @Component
    static class InjectionClient {
        final List<BusinessService> services;
        final BusinessService secondary;
        final Optional<MissingService> missing;
        final Set<BusinessService> serviceSet;

        @Inject
        InjectionClient(List<BusinessService> services,
                        @Qualifier("secondaryService") BusinessService secondary,
                        Optional<MissingService> missing,
                        Set<BusinessService> serviceSet) {
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
