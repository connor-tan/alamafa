package com.alamafa.jfx.vlcj.core;

import com.alamafa.jfx.vlcj.ipc.MediaEvent;
import com.alamafa.jfx.vlcj.ipc.MediaEventListener;

public interface MediaEventDispatcher {

    void addListener(MediaEventListener listener);

    void removeListener(MediaEventListener listener);

    void dispatch(MediaEvent event);
}
