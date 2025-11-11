package com.alamafa.tower.client.session;

import com.alamafa.di.annotation.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Stores login credentials locally for the "Remember me" feature.
 */
@Component
public class CredentialsStore {

    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private final Preferences preferences;
    private volatile SecretKeySpec cachedKey;

    public CredentialsStore() {
        this(Preferences.userNodeForPackage(CredentialsStore.class));
    }

    public CredentialsStore(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences must not be null");
    }

    public Optional<Credentials> load() {
        String username = preferences.get(KEY_USERNAME, null);
        String password = preferences.get(KEY_PASSWORD, null);
        if (isBlank(username) || isBlank(password)) {
            return Optional.empty();
        }
        return Optional.of(new Credentials(decrypt(username), decrypt(password)));
    }

    public void save(String username, String password) {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        preferences.put(KEY_USERNAME, encrypt(username));
        preferences.put(KEY_PASSWORD, encrypt(password));
        flushOrThrow();
    }

    public void clear() {
        preferences.remove(KEY_USERNAME);
        preferences.remove(KEY_PASSWORD);
        flushOrThrow();
    }

    private void flushOrThrow() {
        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            throw new IllegalStateException("Failed to persist credentials", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String encrypt(String value) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            ThreadLocalRandom.current().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to encrypt credentials", ex);
        }
    }

    private String decrypt(String value) {
        try {
            byte[] combined = Base64.getDecoder().decode(value);
            if (combined.length <= IV_LENGTH) {
                return value;
            }
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decoded = cipher.doFinal(ciphertext);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            return value;
        }
    }

    private SecretKeySpec secretKey() {
        SecretKeySpec key = cachedKey;
        if (key != null) {
            return key;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String material = System.getProperty("user.name", "user")
                    + '|' + System.getProperty("os.name", "os")
                    + '|' + System.getProperty("user.home", "home");
            byte[] hash = digest.digest(material.getBytes(StandardCharsets.UTF_8));
            cachedKey = new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
            return cachedKey;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    public record Credentials(String username, String password) {
    }
}
