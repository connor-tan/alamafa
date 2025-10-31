package com.alamafa.jfx.viewmodel.window;

import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public final class FxWindowOptions {
    private final String title;
    private final Double width;
    private final Double height;
    private final Boolean resizable;
    private final Window owner;
    private final Modality modality;
    private final StageStyle stageStyle;
    private final boolean showAndWait;
    private final boolean centerOnScreen;

    private FxWindowOptions(Builder builder) {
        this.title = builder.title;
        this.width = builder.width;
        this.height = builder.height;
        this.resizable = builder.resizable;
        this.owner = builder.owner;
        this.modality = builder.modality;
        this.stageStyle = builder.stageStyle;
        this.showAndWait = builder.showAndWait;
        this.centerOnScreen = builder.centerOnScreen;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FxWindowOptions modal(Window owner) {
        return builder()
                .owner(owner)
                .modality(Modality.WINDOW_MODAL)
                .showAndWait(true)
                .build();
    }

    public String title() {
        return title;
    }

    public Double width() {
        return width;
    }

    public Double height() {
        return height;
    }

    public Boolean resizable() {
        return resizable;
    }

    public Window owner() {
        return owner;
    }

    public Modality modality() {
        return modality;
    }

    public StageStyle stageStyle() {
        return stageStyle;
    }

    public boolean showAndWait() {
        return showAndWait;
    }

    public boolean centerOnScreen() {
        return centerOnScreen;
    }

    public static final class Builder {
        private String title;
        private Double width;
        private Double height;
        private Boolean resizable;
        private Window owner;
        private Modality modality = Modality.NONE;
        private StageStyle stageStyle = StageStyle.DECORATED;
        private boolean showAndWait;
        private boolean centerOnScreen = true;

        private Builder() {
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder width(Double width) {
            this.width = width;
            return this;
        }

        public Builder height(Double height) {
            this.height = height;
            return this;
        }

        public Builder resizable(Boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public Builder owner(Window owner) {
            this.owner = owner;
            return this;
        }

        public Builder modality(Modality modality) {
            this.modality = modality == null ? Modality.NONE : modality;
            return this;
        }

        public Builder stageStyle(StageStyle stageStyle) {
            this.stageStyle = stageStyle == null ? StageStyle.DECORATED : stageStyle;
            return this;
        }

        public Builder showAndWait(boolean showAndWait) {
            this.showAndWait = showAndWait;
            return this;
        }

        public Builder centerOnScreen(boolean centerOnScreen) {
            this.centerOnScreen = centerOnScreen;
            return this;
        }

        public FxWindowOptions build() {
            return new FxWindowOptions(this);
        }
    }
}
