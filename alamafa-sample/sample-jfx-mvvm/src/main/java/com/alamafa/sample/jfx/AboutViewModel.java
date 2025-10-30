package com.alamafa.sample.jfx;

import com.alamafa.jfx.viewmodel.FxViewModel;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelScope;
import com.alamafa.jfx.viewmodel.annotation.FxViewModelSpec;

@FxViewModelSpec(scope = FxViewModelScope.VIEW)
public class AboutViewModel extends FxViewModel {

    private final String info = "Alamafa JavaFX MVVM sample.\n\n"
            + "Demonstrates FxWindowManager opening secondary windows.";

    public String getInfo() {
        return info;
    }
}
