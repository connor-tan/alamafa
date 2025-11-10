package com.alamafa.jfx.vlcj.embedded;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PixelBufferVideoSurfaceFactory {

    private PixelBufferVideoSurfaceFactory() {
    }

    public static VideoSurface videoSurfaceFor(ImageView imageView) {
        PixelBufferCallbacks callbacks = new PixelBufferCallbacks(imageView);
        return new CallbackVideoSurface(callbacks, callbacks, false, VideoSurfaceAdapters.getVideoSurfaceAdapter());
    }

    private static final class PixelBufferCallbacks implements BufferFormatCallback, RenderCallback {

        private final ImageView imageView;
        private final AtomicBoolean pendingUpdate = new AtomicBoolean(false);
        private final Object bufferLock = new Object();

        private PixelBuffer<ByteBuffer> pixelBuffer;
        private WritableImage writableImage;
        private ByteBuffer backingBuffer;
        private int width;
        private int height;

        PixelBufferCallbacks(ImageView imageView) {
            this.imageView = Objects.requireNonNull(imageView, "imageView");
        }

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            ensurePixelBuffer(sourceWidth, sourceHeight);
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

        @Override
        public void newFormatSize(int width, int height, int pitch, int lines) {
            ensurePixelBuffer(width, height);
        }

        @Override
        public void allocatedBuffers(ByteBuffer[] buffers) {
            // no-op
        }

        @Override
        public void lock(MediaPlayer mediaPlayer) {
            // no-op
        }

        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat, int width, int height) {
            if (nativeBuffers == null || nativeBuffers.length == 0 || pixelBuffer == null) {
                return;
            }
            ByteBuffer source = nativeBuffers[0];
            synchronized (bufferLock) {
                copyFrame(source, bufferFormat);
            }
            requestUiUpdate();
        }

        @Override
        public void unlock(MediaPlayer mediaPlayer) {
            // no-op
        }

        private void ensurePixelBuffer(int width, int height) {
            if (width <= 0 || height <= 0) {
                return;
            }
            if (pixelBuffer != null && this.width == width && this.height == height) {
                return;
            }
            this.width = width;
            this.height = height;
            backingBuffer = ByteBuffer.allocateDirect(width * height * 4);
            PixelFormat<ByteBuffer> format = PixelFormat.getByteBgraPreInstance();
            pixelBuffer = new PixelBuffer<>(width, height, backingBuffer, format);
            writableImage = new WritableImage(pixelBuffer);
            Platform.runLater(() -> imageView.setImage(writableImage));
        }

        private void copyFrame(ByteBuffer source, BufferFormat format) {
            int[] pitches = format.getPitches();
            int pitch = pitches != null && pitches.length > 0 ? pitches[0] : format.getWidth() * 4;
            int rowSize = format.getWidth() * 4;
            if (backingBuffer == null || backingBuffer.capacity() < rowSize * format.getHeight()) {
                ensurePixelBuffer(format.getWidth(), format.getHeight());
            }
            ByteBuffer dest = backingBuffer.duplicate();
            dest.clear();
            for (int y = 0; y < format.getHeight(); y++) {
                int srcPos = y * pitch;
                int dstPos = y * rowSize;
                ByteBuffer srcRow = source.duplicate();
                srcRow.position(srcPos);
                srcRow.limit(srcPos + rowSize);
                ByteBuffer dstRow = dest.duplicate();
                dstRow.position(dstPos);
                dstRow.put(srcRow);
            }
            dest.flip();
            source.rewind();
        }

        private void requestUiUpdate() {
            if (pendingUpdate.compareAndSet(false, true)) {
                Platform.runLater(() -> {
                    try {
                        if (pixelBuffer != null) {
                            pixelBuffer.updateBuffer(pb -> null);
                        }
                    } finally {
                        pendingUpdate.set(false);
                    }
                });
            }
        }
    }
}
