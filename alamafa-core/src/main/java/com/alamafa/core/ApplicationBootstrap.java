package com.alamafa.core;


import com.alamafa.core.health.HealthRegistry;
import com.alamafa.core.logging.AlamafaLogger;
import com.alamafa.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 在真正启动 {@link ApplicationLauncher} 之前统一扩充 {@link ApplicationContext}，
 * 让主题、配置、插件等扩展点能够通过上下文初始化器接入，并负责调度整个生命周期。
 */
public final class ApplicationBootstrap {
    private static final AlamafaLogger LOGGER = LoggerFactory.getLogger(ApplicationBootstrap.class);

    private final ContextAwareApplicationLauncher launcher;
    private final List<Consumer<ApplicationContext>> contextInitializers = new ArrayList<>();
    private final List<Lifecycle> lifecycleParticipants = new ArrayList<>();
    private final LifecycleErrorHandler dispatchingErrorHandler = this::dispatchError;
    private volatile LifecycleErrorHandler errorHandler = this::logFailure;

    /**
     * 使用具备上下文能力的启动器构造引导器
     */
    public ApplicationBootstrap(ContextAwareApplicationLauncher launcher) {
        this.launcher = Objects.requireNonNull(launcher, "launcher");
        ApplicationContext context = this.launcher.getContext();
        context.put(LifecycleErrorHandler.class, dispatchingErrorHandler);
        if (!context.contains(HealthRegistry.CONTEXT_KEY)) {
            HealthRegistry registry = new HealthRegistry(context);
            context.put(HealthRegistry.class, registry);
            context.put(HealthRegistry.CONTEXT_KEY, registry);
        }
    }

    /**
     * 注册上下文初始化器，会在 {@link Lifecycle#init(ApplicationContext)} 之前按顺序执行。
     */
    public ApplicationBootstrap addContextInitializer(Consumer<ApplicationContext> initializer) {
        contextInitializers.add(Objects.requireNonNull(initializer, "initializer"));
        return this;
    }

    /**
     * 注册额外的生命周期参与者，按注册顺序执行 init/start，stop 阶段则逆序执行。
     */
    public ApplicationBootstrap addLifecycleParticipant(Lifecycle lifecycle) {
        lifecycleParticipants.add(Objects.requireNonNull(lifecycle, "lifecycle"));
        return this;
    }

    /**
     * 指定生命周期阶段出错时的回调处理逻辑。
     */
    public ApplicationBootstrap onError(LifecycleErrorHandler handler) {
        this.errorHandler = Objects.requireNonNull(handler, "handler");
        return this;
    }

    /**
     * 暴露应用上下文，便于外部执行自定义配置。
     */
    public ApplicationContext getContext() {
        return launcher.getContext();
    }

    /**
     * 依次执行所有上下文初始化器后，通过启动器运行传入的生命周期对象。
     */
    public void launch(Lifecycle lifecycle) {
        ApplicationContext ctx = launcher.getContext();
        for (Consumer<ApplicationContext> initializer : contextInitializers) {
            initializer.accept(ctx);
        }
        launcher.launch(composeLifecycle(lifecycle));
    }

    /**
     * 组合主生命周期与所有附加参与者，确保阶段顺序与错误处理一致。
     */
    private Lifecycle composeLifecycle(Lifecycle primary) {
        return new Lifecycle() {
            @Override
            public void init(ApplicationContext ctx) throws Exception {
                for (Lifecycle participant : lifecycleParticipants) {
                    invokePhase(LifecyclePhase.INIT, participant, ctx);
                }
                invokePhase(LifecyclePhase.INIT, primary, ctx);
            }

            @Override
            public void start(ApplicationContext ctx) throws Exception {
                for (Lifecycle participant : lifecycleParticipants) {
                    invokePhase(LifecyclePhase.START, participant, ctx);
                }
                invokePhase(LifecyclePhase.START, primary, ctx);
            }

            @Override
            public void stop(ApplicationContext ctx) throws Exception {
                Exception firstError = invokeStop(primary, ctx, null);
                for (int i = lifecycleParticipants.size() - 1; i >= 0; i--) {
                    Lifecycle participant = lifecycleParticipants.get(i);
                    firstError = invokeStop(participant, ctx, firstError);
                }
                if (firstError != null) {
                    if (firstError instanceof LifecycleExecutionException) {
                        throw firstError;
                    }
                    throw new LifecycleExecutionException(LifecyclePhase.STOP,
                            describeTarget(primary), firstError);
                }
            }
        };
    }

    /**
     * 将生命周期异常统一交给外部注册的错误处理器。
     */
    private void dispatchError(LifecyclePhase phase, Exception exception) {
        try {
            errorHandler.onError(phase, exception);
        } catch (Exception handlerError) {
            LOGGER.warn("Error handler failed while processing " + phase + " phase exception", handlerError);
        }
    }

    /**
     * 按指定阶段调用生命周期对象，并在出现异常时包装统一异常类型。
     */
    private void invokePhase(LifecyclePhase phase, Lifecycle lifecycle, ApplicationContext ctx) throws Exception {
        try {
            switch (phase) {
                case INIT -> lifecycle.init(ctx);
                case START -> lifecycle.start(ctx);
                case STOP -> lifecycle.stop(ctx);
            }
        } catch (Exception ex) {
            throw wrapAndReport(phase, lifecycle, ex);
        }
    }

    /**
     * stop 阶段需要收集首个异常并持续附加后续异常，方便统一上抛。
     */
    private Exception invokeStop(Lifecycle lifecycle, ApplicationContext ctx, Exception firstError) {
        try {
            lifecycle.stop(ctx);
            return firstError;
        } catch (Exception ex) {
            Exception wrapped = wrapAndReport(LifecyclePhase.STOP, lifecycle, ex);
            if (firstError == null) {
                return wrapped;
            }
            firstError.addSuppressed(wrapped);
            return firstError;
        }
    }

    /**
     * 将任意异常包装成 {@link LifecycleExecutionException} 后进行上报。
     */
    private Exception wrapAndReport(LifecyclePhase phase, Lifecycle lifecycle, Exception ex) {
        Exception toThrow = ex instanceof LifecycleExecutionException
                ? ex
                : new LifecycleExecutionException(phase, describeTarget(lifecycle), ex);
        dispatchError(phase, toThrow);
        return toThrow;
    }

    /**
     * 默认的错误记录逻辑，便于在未注册自定义 handler 时仍能定位问题。
     */
    private void logFailure(LifecyclePhase phase, Exception exception) {
        if (exception instanceof LifecycleExecutionException executionException) {
            LOGGER.error("Lifecycle phase {} failed for {}", phase, executionException.target(), exception);
        } else {
            LOGGER.error("Lifecycle phase {} failed", phase, exception);
        }
    }

    /**
     * 生成生命周期实现类的描述信息，便于日志输出。
     */
    private String describeTarget(Lifecycle lifecycle) {
        return lifecycle == null ? "unknown lifecycle" : lifecycle.getClass().getName();
    }
}
