package com.alamafa.bootstrap;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Parses {@link AlamafaBootApplication} annotated classes into a {@link BootApplicationDescriptor}.
 */
public final class BootApplicationParser {
    private BootApplicationParser() {}

    public static BootApplicationDescriptor parse(Class<?> primarySource) {
        Objects.requireNonNull(primarySource, "primarySource");
        Set<String> basePackages = new LinkedHashSet<>();
        Set<Class<?>> moduleClasses = new LinkedHashSet<>();
        AlamafaBootApplication ann = primarySource.getAnnotation(AlamafaBootApplication.class);
        if (ann != null) {
            for (String pkg : ann.scanBasePackages()) {
                if (pkg != null && !pkg.isBlank()) {
                    basePackages.add(pkg.trim());
                }
            }
            for (Class<?> cls : ann.scanBasePackageClasses()) {
                if (cls != null) {
                    String pkgName = cls.getPackageName();
                    if (!pkgName.isBlank()) {
                        basePackages.add(pkgName);
                    }
                }
            }
            for (Class<?> module : ann.modules()) {
                if (module != null) {
                    moduleClasses.add(module);
                }
            }
        }
        if (basePackages.isEmpty()) {
            String primaryPkg = primarySource.getPackageName();
            if (!primaryPkg.isBlank()) {
                basePackages.add(primaryPkg);
            }
        }
        return new BootApplicationDescriptor(primarySource, basePackages, moduleClasses);
    }
}

