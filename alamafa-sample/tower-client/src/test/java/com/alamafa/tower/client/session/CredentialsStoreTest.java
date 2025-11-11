package com.alamafa.tower.client.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialsStoreTest {

    private Preferences preferences;
    private CredentialsStore store;

    @BeforeEach
    void setUp() {
        preferences = new InMemoryPreferences();
        store = new CredentialsStore(preferences);
    }

    @Test
    void saveShouldEncryptStoredValues() {
        store.save("user", "secret");

        String raw = preferences.get("password", null);
        assertNotEquals("secret", raw);
    }

    @Test
    void loadShouldDecryptValues() {
        store.save("alice", "wonderland");

        Optional<CredentialsStore.Credentials> credentials = store.load();
        assertTrue(credentials.isPresent());
        assertEquals("alice", credentials.get().username());
        assertEquals("wonderland", credentials.get().password());
    }

    @Test
    void clearShouldRemoveCredentials() {
        store.save("bob", "builder");
        store.clear();

        assertTrue(store.load().isEmpty());
    }

    private static final class InMemoryPreferences extends AbstractPreferences {
        private final java.util.Map<String, String> values = new java.util.HashMap<>();
        private final java.util.Map<String, InMemoryPreferences> children = new java.util.HashMap<>();

        InMemoryPreferences() {
            this(null, "");
        }

        private InMemoryPreferences(AbstractPreferences parent, String name) {
            super(parent, name);
        }

        @Override
        protected void putSpi(String key, String value) {
            values.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return values.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            values.remove(key);
        }

        @Override
        protected void removeNodeSpi() {
            values.clear();
            children.values().forEach(child -> {
                try {
                    child.removeNode();
                } catch (BackingStoreException ignored) {
                }
            });
            children.clear();
        }

        @Override
        protected String[] keysSpi() {
            return values.keySet().toArray(String[]::new);
        }

        @Override
        protected String[] childrenNamesSpi() {
            return children.keySet().toArray(String[]::new);
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            return children.computeIfAbsent(name, key -> new InMemoryPreferences(this, key));
        }

        @Override
        protected void syncSpi() {
        }

        @Override
        protected void flushSpi() {
        }

        @Override
        public boolean isUserNode() {
            return true;
        }
    }
}
