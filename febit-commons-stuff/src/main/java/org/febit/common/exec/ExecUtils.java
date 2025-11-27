/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.common.exec;

import jakarta.annotation.Nullable;
import lombok.Singular;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.launcher.CommandLauncherFactory;
import org.apache.commons.io.IOUtils;
import org.febit.lang.io.Lines;
import org.febit.lang.io.MpscPipeImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class ExecUtils {

    private static final int STREAM_LINES_DEFAULT_BUF_SIZE = 256;

    private static class DefaultThreadFactoryHolder {
        private static final ThreadFactory INSTANCE = Executors.defaultThreadFactory();
    }

    public static class DefaultExecutor implements Executor {
        public static final DefaultExecutor INSTANCE = new DefaultExecutor();

        @Override
        public void execute(Runnable runnable) {
            DefaultThreadFactoryHolder.INSTANCE.newThread(runnable).start();
        }
    }

    @SuppressWarnings({
            "UnusedReturnValue"
    })
    public static class Launcher {

        public Launcher stdout(OutputStream stdout) {
            return handler(createPumpStreamHandler(
                    Process::getInputStream, stdout
            ));
        }

        public Launcher workingDir(@Nullable File dir) {
            Path path = dir == null ? null : dir.toPath();
            return workingDir(path);
        }

        public Launcher workingDir(@Nullable Path dir) {
            this.workingDir = dir;
            return this;
        }

        public Launcher stdout(
                Consumer<String> sink
        ) {
            return stdout(Lines.asUtf8OutputStream(sink));
        }

        public Launcher stderr(OutputStream stderr) {
            return handler(createPumpStreamHandler(
                    Process::getErrorStream, stderr
            ));
        }

        public Launcher stderr(
                Consumer<String> sink
        ) {
            return stderr(Lines.asUtf8OutputStream(sink));
        }

        public Launcher pipeLines(
                Consumer<String> sink
        ) {
            // TODO: will close twice if sink is closable
            var out = Lines.asUtf8OutputStream(sink);
            var err = Lines.asUtf8OutputStream(sink);
            return stdout(out).stderr(err);
        }

        public Launcher pipeLineStream(
                Consumer<Stream<String>> sink,
                int bufferSize
        ) {
            var pipe = MpscPipeImpl.<String>ofBounded(bufferSize);
            handler(process -> () -> {
                sink.accept(pipe.stream());
            });
            return stdout(pipe.createProducer())
                    .stderr(pipe.createProducer());
        }

        public Launcher pipeLineStream(
                Consumer<Stream<String>> sink
        ) {
            return pipeLineStream(sink, STREAM_LINES_DEFAULT_BUF_SIZE);
        }

    }

    /**
     * @param command      the command to execute.
     * @param environments the environment variables to set for the command.
     * @param workingDir   the working directory to execute the command in.
     * @param executor     the executor to use for executing the command and handling asynchronous handlers.
     * @return a {@link ProcessFuture} that can be used to wait for the process to complete.
     */
    @lombok.Builder(
            builderClassName = "Launcher",
            builderMethodName = "launcher",
            buildMethodName = "start"
    )
    private static ProcessFuture start(
            @SuppressWarnings("NullableProblems")
            @lombok.NonNull CommandLine command,

            @Singular("env") Map<String, String> environments,
            @Singular List<Function<Process, Runnable>> handlers,
            @Nullable Path workingDir,
            @Nullable Executor executor
    ) {
        if (executor == null) {
            executor = DefaultExecutor.INSTANCE;
        }
        var started = start(executor, command, environments, workingDir);
        var completed = started.handleAsync(ExecUtils::handleProcess, executor);

        var combined = handlers.isEmpty() ? completed
                : CompletableFuture.allOf(submitHandlers(
                        handlers, started, executor
                ))
                .thenCombine(
                        completed,
                        (ignored, exitValue) -> exitValue
                );
        return ProcessFutureImpl.of(started, combined);
    }

    private static CompletableFuture<?>[] submitHandlers(
            List<Function<Process, Runnable>> handlers,
            CompletableFuture<Process> started,
            Executor executor
    ) {
        return handlers.stream()
                .map(handler ->
                        started.thenRunAsync(handler.apply(started.join()), executor)
                )
                .toArray(CompletableFuture[]::new);
    }

    private static CompletableFuture<Process> start(
            Executor executor,
            CommandLine command,
            @Nullable Map<String, String> env,
            @Nullable Path workingDir
    ) {
        var started = new CompletableFuture<Process>();
        executor.execute(() -> {
            try {
                var launcher = CommandLauncherFactory.createVMLauncher();
                var process = launcher.exec(command, env, workingDir);
                started.complete(process);
            } catch (Throwable e) {
                started.completeExceptionally(e);
            }
        });
        return started;
    }

    private static Function<Process, Runnable> createPumpStreamHandler(
            Function<Process, InputStream> source, OutputStream target) {
        return process -> () -> {
            try (var in = source.apply(process); target) {
                IOUtils.copy(in, target);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            log.trace("Process stream pumping completed");
        };
    }

    private static Integer handleProcess(@Nullable Process process, @Nullable Throwable throwable) {
        if (throwable != null) {
            throw throwable instanceof RuntimeException r
                    ? r : new RuntimeException(throwable);
        }
        if (process == null) {
            throw new UncheckedIOException(new IOException("Process is null"));
        }
        int exitValue;
        try {
            exitValue = process.waitFor();
        } catch (final InterruptedException e) {
            process.destroy();
            exitValue = process.exitValue();
        } finally {
            @SuppressWarnings("unused")
            var ignored = Thread.interrupted();
        }
        return exitValue;
    }

}
