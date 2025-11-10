package com.alamafa.jfx.vlcj.ipc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class StdioEventChannel implements MediaEventChannel {

    private static final Logger log = LoggerFactory.getLogger(StdioEventChannel.class);

    private final InputStream inputStream;
    private final ObjectMapper mapper;
    private Thread readerThread;

    public StdioEventChannel(InputStream inputStream) {
        this(inputStream, defaultMapper());
    }

    public StdioEventChannel(InputStream inputStream, ObjectMapper mapper) {
        this.inputStream = Objects.requireNonNull(inputStream, "inputStream must not be null");
        this.mapper = mapper == null ? defaultMapper() : mapper;
    }

    @Override
    public void start(MediaEventListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    try {
                        MediaEvent event = mapper.readValue(trimmed, MediaEvent.class);
                        listener.onEvent(event);
                    } catch (IOException ex) {
                        log.warn("Failed to decode media event", ex);
                    }
                }
            } catch (IOException ex) {
                log.warn("Event channel closed", ex);
            }
        }, "vlcj-event-channel");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    @Override
    public void close() {
        if (readerThread != null) {
            readerThread.interrupt();
        }
    }

    private static ObjectMapper defaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
}
