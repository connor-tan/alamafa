package com.alamafa.jfx.vlcj.external;

import com.alamafa.di.annotation.Component;
import com.alamafa.di.annotation.Inject;
import com.alamafa.jfx.vlcj.core.MediaEndpoint;
import com.alamafa.jfx.vlcj.core.MediaEndpointFactory;
import com.alamafa.jfx.vlcj.core.MediaEventDispatcher;
import com.alamafa.jfx.vlcj.core.MediaSource;
import com.alamafa.jfx.vlcj.core.PlayerLaunchRequest;
import com.alamafa.jfx.vlcj.core.PlayerProperties;
import com.alamafa.jfx.vlcj.ipc.MediaCommandChannel;
import com.alamafa.jfx.vlcj.ipc.MediaEventChannel;
import com.alamafa.jfx.vlcj.ipc.StdioCommandChannel;
import com.alamafa.jfx.vlcj.ipc.StdioEventChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责创建承载 vlcj 播放窗口的外部 Java 进程。
 */
@Component
public class ExternalPlayerLauncher implements MediaEndpointFactory {

    private static final Logger log = LoggerFactory.getLogger(ExternalPlayerLauncher.class);

    @Inject
    private MediaEventDispatcher eventDispatcher;

    @Inject
    private PlayerProperties playerProperties;

    @Inject
    private ExternalProcessRegistry processRegistry;

    @Override
    public MediaEndpoint launch(PlayerLaunchRequest request) {
        List<String> command = buildCommand(request);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(System.getProperty("user.dir")));
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            ExternalPlayerHandle handle = new ExternalPlayerHandle(request.getPlayerId(), process);
            handle.pipeOutputAsync(line -> log.debug("[player:{}] {}", request.getPlayerId(), line));
            log.info("Started external player {} (pid={} media={})", request.getPlayerId(), process.pid(), request.getMediaUrl());
            processRegistry.register(handle);
            MediaCommandChannel commandChannel = new StdioCommandChannel(process.getOutputStream());
            MediaEventChannel eventChannel = new StdioEventChannel(process.getErrorStream());
            ExternalProcessEndpoint endpoint = new ExternalProcessEndpoint(handle, commandChannel, eventChannel, eventDispatcher, playerProperties, processRegistry);
            if (!request.getMediaUrl().isBlank()) {
                endpoint.play(MediaSource.of(request.getMediaUrl()));
            }
            return endpoint;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to launch external player process", ex);
        }
    }

    private List<String> buildCommand(PlayerLaunchRequest request) {
        List<String> command = new ArrayList<>();
        command.add(resolveJavaExecutable());
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add("com.alamafa.jfx.vlcj.host.PlayerHostLauncher");
        command.add("--playerId=" + request.getPlayerId());
        command.add("--width=" + request.getWidth());
        command.add("--height=" + request.getHeight());
        if (!request.getWindowTitle().isBlank()) {
            command.add("--title=" + request.getWindowTitle());
        }
        if (!request.getMediaUrl().isBlank()) {
            command.add("--media=" + request.getMediaUrl());
        }
        return command;
    }

    private String resolveJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        String exec = javaHome + File.separator + "bin" + File.separator + "java";
        if (isWindows()) {
            exec += ".exe";
        }
        return exec;
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }
}
