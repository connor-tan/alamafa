package com.alamafa.sample.jfx;

import com.alamafa.jfx.viewmodel.AsyncFxCommand;
import com.alamafa.jfx.viewmodel.FxCommand;
import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelSpec;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@FxViewModelSpec(scope = FxViewModelScope.APPLICATION)
public class MainViewModel extends FxViewModel {
    private final StringProperty message = new SimpleStringProperty("Welcome to Alamafa JavaFX!");
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "sample-refresh-command");
        thread.setDaemon(true);
        return thread;
    });
    private final FxCommand refreshCommand;

    public MainViewModel() {
        refreshCommand = new AsyncFxCommand(executor, () -> {
            Thread.sleep(400);
            Platform.runLater(() -> message.set("Updated at " + LocalDateTime.now()));
            return null;
        }, throwable -> message.set("Failed: " + throwable.getMessage()));
    }

    public StringProperty messageProperty() {
        return message;
    }

    public FxCommand refreshCommand() {
        return refreshCommand;
    }

    @Override
    protected void onDetach() {
        executor.shutdownNow();
    }
}
