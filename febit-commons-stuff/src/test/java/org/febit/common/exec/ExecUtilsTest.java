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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.SystemUtils;
import org.febit.lang.io.Lines;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ExecUtilsTest {

    static final int CODE_SIGTERM = 143;

    private static void counting(Consumer<ExecUtils.Launcher> customizer) throws ExecutionException, InterruptedException {
        if (!SystemUtils.IS_OS_LINUX && !SystemUtils.IS_OS_MAC) {
            log.warn("Skip running ping test on non-Linux/Mac OS");
            return;
        }
        var command = new CommandLine("sh")
                .addArgument(new File("./src/test/scripts/counting.sh").getAbsolutePath())
                .addArgument("10");
        var launcher = ExecUtils.launcher()
                .command(command);
        customizer.accept(launcher);
        var future = launcher.start();
        try {
            future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.info("Counting command timed out, cancelling...");
            future.cancel(true);
        }
        assertEquals(CODE_SIGTERM, future.get());
    }

    @Test
    void ping_lineStream() throws ExecutionException, InterruptedException {
        counting(launcher -> launcher
                .pipeLineStream(lines -> lines.forEach(line -> {
                    log.info("> {}", line);
                }))
        );
    }

    @Test
    void ping_lines() throws ExecutionException, InterruptedException {
        counting(launcher -> launcher
                .pipeLines(line -> {
                    log.info("> {}", line);
                })
        );
    }

    @Test
    void ping_std() throws ExecutionException, InterruptedException {
        counting(launcher -> launcher
                .stderr(Lines.asUtf8OutputStream(line -> {
                    log.info("stderr: {}", line);
                }))
                .stdout(Lines.asUtf8OutputStream(line -> {
                    log.info("stdout: {}", line);
                }))
        );
    }
}
