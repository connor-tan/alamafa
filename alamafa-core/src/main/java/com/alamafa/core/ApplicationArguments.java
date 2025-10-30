package com.alamafa.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates raw command-line arguments and exposes utility accessors.
 */
public final class ApplicationArguments {
    private final String[] sourceArgs;
    private final List<String> arguments;

    /**
     * Creates an immutable snapshot of the provided arguments.
     */
    public ApplicationArguments(String... args) {
        if (args == null || args.length == 0) {
            this.sourceArgs = new String[0];
            this.arguments = List.of();
            return;
        }
        this.sourceArgs = Arrays.copyOf(args, args.length);
        this.arguments = Collections.unmodifiableList(Arrays.asList(this.sourceArgs));
    }

    /**
     * Returns a defensive copy of the raw arguments.
     */
    public String[] toArray() {
        return Arrays.copyOf(sourceArgs, sourceArgs.length);
    }

    /**
     * Returns the arguments as an immutable list.
     */
    public List<String> asList() {
        return arguments;
    }

    /**
     * Returns the first option value matching {@code --name=value} semantics.
     */
    public String getOption(String name) {
        Objects.requireNonNull(name, "name");
        String prefix = "--" + name + "=";
        for (String argument : arguments) {
            if (argument.startsWith(prefix)) {
                return argument.substring(prefix.length());
            }
        }
        return null;
    }

    /**
     * Returns whether the bare option {@code --name} is present.
     */
    public boolean hasFlag(String name) {
        Objects.requireNonNull(name, "name");
        String flag = "--" + name;
        return arguments.stream().anyMatch(flag::equals);
    }
}

