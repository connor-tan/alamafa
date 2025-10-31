package com.alamafa.tower.client.session;

import com.alamafa.di.annotation.Component;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Component
public class UserSession {
    private final StringProperty username = new SimpleStringProperty("Guest");
    private final StringProperty avatarInitials = new SimpleStringProperty("G");

    public StringProperty usernameProperty() {
        return username;
    }

    public String getUsername() {
        return username.get();
    }

    public void updateUsername(String value) {
        username.set(value);
        avatarInitials.set(generateInitials(value));
    }

    public StringProperty avatarInitialsProperty() {
        return avatarInitials;
    }

    public String getAvatarInitials() {
        return avatarInitials.get();
    }

    private String generateInitials(String value) {
        if (value == null || value.isBlank()) {
            return "G";
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
            }
            if (builder.length() == 2) {
                break;
            }
        }
        return builder.toString();
    }
}
