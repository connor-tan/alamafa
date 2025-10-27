package com.alamafa.helloworld.greeting;

/**
 * 简单的问候语模板。
 */
public final class GreetingTemplate {
    private final String pattern;

    public GreetingTemplate(String pattern) {
        this.pattern = pattern;
    }

    public String render(String target, String punctuation) {
        return pattern.formatted(target, punctuation);
    }
}
