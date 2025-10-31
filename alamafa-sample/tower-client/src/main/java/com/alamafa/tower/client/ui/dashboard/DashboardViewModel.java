package com.alamafa.tower.client.ui.dashboard;

import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelSpec;
import com.alamafa.tower.client.session.UserSession;
import com.alamafa.di.annotation.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@FxViewModelSpec(lazy = false)
public class DashboardViewModel extends FxViewModel {
    private final StringProperty displayName = new SimpleStringProperty("Guest");
    private final StringProperty avatarInitials = new SimpleStringProperty("G");

    @Inject
    private UserSession userSession;

    @Override
    protected void onAttach() {
        if (userSession != null) {
            displayName.bind(userSession.usernameProperty());
            avatarInitials.bind(userSession.avatarInitialsProperty());
        }
    }

    @Override
    protected void onDetach() {
        displayName.unbind();
        avatarInitials.unbind();
    }

    public StringProperty displayNameProperty() {
        return displayName;
    }

    public StringProperty avatarInitialsProperty() {
        return avatarInitials;
    }
}
