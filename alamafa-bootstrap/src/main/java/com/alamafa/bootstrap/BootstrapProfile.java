package com.alamafa.bootstrap;

import com.alamafa.core.ApplicationBootstrap;

/**
 * Contract for composing application bootstraps.
 */
public interface BootstrapProfile {
    ApplicationBootstrap apply(ApplicationBootstrap bootstrap);

    default ApplicationBootstrap createBootstrap() {
        return null;
    }

    default ApplicationBootstrap bootstrap() {
        ApplicationBootstrap bootstrap = createBootstrap();
        if (bootstrap == null) {
            throw new IllegalStateException("Profile did not supply a bootstrap. Provide one via apply(...)");
        }
        return apply(bootstrap);
    }
}
