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
package org.febit.common.jcommander;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.febit.common.jcommander.converter.DurationConverter;
import org.febit.lang.io.DiscardOutputStream;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CommandsTest {

    static final Console DISCART_CONSOLE = new DefaultConsole(new PrintStream(new DiscardOutputStream()));

    @Test
    void noOptions() {
        var out = new ByteArrayOutputStream();
        var console = new DefaultConsole(new PrintStream(out));
        var commands = Commands.create()
                .console(console);
        commands.exec();
        assertEquals("Usage: <main class>\n", out.toString());
    }

    @Test
    void ex_add() {
        var commands = Commands.create()
                .console(DISCART_CONSOLE);

        assertThrows(IllegalArgumentException.class,
                () -> commands.add(SpyCommand.of(List.of(), FooOptions::new, c -> {
                }))
        );

        assertThrows(NullPointerException.class,
                () -> commands.add(SpyCommand.of(List.of("test"), () -> null, c -> {
                }))
        );
    }

    @Test
    void parameterException() {
        var contextHolder = new AtomicReference<Context>();
        var clickhouse = spy(SpyCommand.of(
                List.of("clickhouse", "c"),
                ClickhouseOptions::new,
                contextHolder::set
        ));
        var foo = spy(SpyCommand.of(
                List.of("foo"),
                FooOptions::new,
                contextHolder::set
        ));
        var commands = Commands.create()
                .add(clickhouse, foo)
                .console(DISCART_CONSOLE);

        assertThrows(ParameterException.class, () -> commands.exec("help"));
        assertThrows(ParameterException.class, () -> commands.exec("--log-level"));
        assertThrows(ParameterException.class, () -> commands.exec("--log-level", "DEBUG"));

        assertThrows(ParameterException.class, () -> commands.exec("clickhouse", "--foo-user", "test"));
        assertThrows(ParameterException.class, () -> commands.exec("clickhouse", "--log-level", "DEBUG"));

        assertDoesNotThrow(() -> commands.exec("foo", "--foo-user", "test"));
        assertDoesNotThrow(() -> commands.exec("clickhouse", "--clickhouse-user", "clickhouse"));

        commands.acceptUnknownOptions(true);
        assertDoesNotThrow(() -> commands.exec("help"));
        assertDoesNotThrow(() -> commands.exec("--log-level"));
        assertDoesNotThrow(() -> commands.exec("--log-level", "DEBUG"));
    }

    @Test
    void help() {
        var commands = Commands.create()
                .console(DISCART_CONSOLE)
                .add(new HelpCommand());
        assertDoesNotThrow(() -> commands.exec("help"));
    }

    @Test
    void foo() {
        var console = spy(DISCART_CONSOLE);
        var foo = spy(new FooCommand());

        var commands = Commands.create()
                .console(console)
                .add(foo);

        verify(foo).keys();
        verify(foo).newOptions();

        reset(console, foo);
        commands.exec("foo");
        verify(foo).exec(any());
        verify(console).println("foo");

        reset(console, foo);
        commands.exec("f");
        verify(foo).exec(any());
        verify(console).println("foo");
    }

    @Test
    @SuppressWarnings("unchecked")
    void full() {
        var contextHolder = new AtomicReference<Context>();
        var clickhouse = spy(SpyCommand.of(
                List.of("clickhouse", "c"),
                ClickhouseOptions::new,
                contextHolder::set
        ));
        var foo = spy(SpyCommand.of(
                List.of("foo"),
                FooOptions::new,
                contextHolder::set
        ));

        var newCommands = (Supplier<Commands>) () -> Commands.create()
                .console(DISCART_CONSOLE)
                .addGlobal(new LoggerOptions())
                .add(clickhouse)
                .add(foo);

        contextHolder.set(null);
        reset(clickhouse, foo);
        newCommands.get().exec();
        verify(clickhouse, never()).exec(any());
        verify(foo, never()).exec(any());
        assertNull(contextHolder.get());

        contextHolder.set(null);
        reset(clickhouse, foo);
        newCommands.get().exec(
                "--log-level", "debug",
                "--log-duration", "10",
                "clickhouse",
                "--clickhouse-url", "http://localhost:8123",
                "--clickhouse-db", "default",
                "--clickhouse-user", "clickhouse",
                "--clickhouse-password", "123"
        );
        verify(clickhouse).exec(any());
        verify(foo, never()).exec(any());

        assertThat(contextHolder.get().requireBean(LoggerOptions.class))
                .returns(Duration.ofSeconds(10), LoggerOptions::getDuration)
                .returns(Level.DEBUG, LoggerOptions::getLevel)
        ;

        assertThat(contextHolder.get().requireBean(ClickhouseOptions.class))
                .returns("http://localhost:8123", ClickhouseOptions::getUrl)
                .returns("default", ClickhouseOptions::getDb)
                .returns("clickhouse", ClickhouseOptions::getUser)
                .returns("123", ClickhouseOptions::getPassword)
        ;

        assertThat(contextHolder.get().requireBean(FooOptions.class))
                .isNotNull()
                .returns(null, FooOptions::getUser);
    }

    @Data
    public static class LoggerOptions implements IOptions {

        @Parameter(
                names = {"--log-level"},
                description = "Logger level"
        )
        private Level level;

        @Parameter(
                names = {"--log-duration"},
                converter = DurationConverter.class,
                description = "Logger duration"
        )
        private Duration duration;
    }

    @Data
    public static class ClickhouseOptions implements IOptions {

        @Parameter(
                names = {"--clickhouse-url"},
                description = "Clickhouse url"
        )
        private String url;

        @Parameter(
                names = {"--clickhouse-db"},
                description = "Clickhouse db"
        )
        private String db;

        @Parameter(
                names = {"--clickhouse-user"},
                description = "Clickhouse user"
        )
        private String user;

        @Parameter(
                names = {"--clickhouse-password"},
                description = "Clickhouse password"
        )
        private String password;
    }

    @Data
    public static class FooOptions implements IOptions {

        @Parameter(
                names = {"--foo-user"}
        )
        private String user;
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class SpyCommand<T extends IOptions> implements ICommand<T> {

        private final List<String> keys;
        private final Supplier<T> optionsSupplier;
        private final Consumer<Context> contextConsumer;

        @Override
        public List<String> keys() {
            return keys;
        }

        @Override
        public T newOptions() {
            return optionsSupplier.get();
        }

        @Override
        public void exec(Context context) {
            contextConsumer.accept(context);
        }
    }

    private static class FooCommand implements ICommand<FooOptions> {

        @Override
        public List<String> keys() {
            return List.of("foo", "f");
        }

        @Override
        public FooOptions newOptions() {
            return new FooOptions();
        }

        @Override
        public void exec(Context context) {
            context.println("foo");
        }
    }

}
