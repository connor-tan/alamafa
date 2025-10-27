package com.alamafa.starter;

/**
 * @deprecated Use BootApplicationParser (bootstrap module) instead.
 */
@Deprecated
final class ApplicationMetadata {
    private ApplicationMetadata() {}
    static ApplicationMetadata from(Class<?> ignored) {
        throw new UnsupportedOperationException("ApplicationMetadata is deprecated. Use BootApplicationParser.parse(...)");
    }
}
