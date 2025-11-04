package com.alamafa.tower.client.session;

import com.alamafa.di.annotation.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Stores login credentials locally for the "Remember me" feature.
 */
@Component
public class CredentialsStore {

    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private final Preferences preferences;

    public CredentialsStore() {
        this(Preferences.userNodeForPackage(CredentialsStore.class));
    }

    CredentialsStore(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences must not be null");
    }

    public Optional<Credentials> load() {
        String username = preferences.get(KEY_USERNAME, null);
        String password = preferences.get(KEY_PASSWORD, null);
        if (isBlank(username) || isBlank(password)) {
            return Optional.empty();
        }
        return Optional.of(new Credentials(username, password));
    }

    public void save(String username, String password) {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        preferences.put(KEY_USERNAME, username);
        preferences.put(KEY_PASSWORD, password);
        flushQuietly();
    }

    public void clear() {
        preferences.remove(KEY_USERNAME);
        preferences.remove(KEY_PASSWORD);
        flushQuietly();
    }

    private void flushQuietly() {
        try {
            preferences.flush();
        } catch (BackingStoreException ignored) {
            // non-critical: preference flush failures are ignored
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record Credentials(String username, String password) {
    }
}
