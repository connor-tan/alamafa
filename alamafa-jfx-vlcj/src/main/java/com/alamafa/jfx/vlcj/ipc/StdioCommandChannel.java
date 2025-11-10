package com.alamafa.jfx.vlcj.ipc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 基于进程标准输入输出的简单命令信道。
 */
public final class StdioCommandChannel implements MediaCommandChannel {

    private static final Logger log = LoggerFactory.getLogger(StdioCommandChannel.class);

    private final BufferedWriter writer;
    private final ObjectMapper mapper;

    public StdioCommandChannel(OutputStream outputStream) {
        this(outputStream, defaultMapper());
    }

    public StdioCommandChannel(OutputStream outputStream, ObjectMapper mapper) {
        Objects.requireNonNull(outputStream, "outputStream must not be null");
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        this.mapper = mapper == null ? defaultMapper() : mapper;
    }

    @Override
    public synchronized void send(MediaCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        try {
            String json = mapper.writeValueAsString(command);
            writer.write(json);
            writer.write('\n');
            writer.flush();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to send media command", ex);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException ex) {
            log.warn("Failed to close command channel", ex);
        }
    }

    private static ObjectMapper defaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
}
