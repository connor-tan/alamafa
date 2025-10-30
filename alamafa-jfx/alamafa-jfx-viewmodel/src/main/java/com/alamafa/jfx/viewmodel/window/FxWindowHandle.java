package com.alamafa.jfx.viewmodel.window;

import com.alamafa.jfx.view.FxView;
import com.alamafa.jfx.view.meta.FxViewDescriptor;
import com.alamafa.jfx.viewmodel.FxViewModel;
import javafx.stage.Stage;

public record FxWindowHandle(Stage stage,
                             FxView<?> view,
                             FxViewModel viewModel,
                             Object controller,
                             FxViewDescriptor descriptor) {
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
