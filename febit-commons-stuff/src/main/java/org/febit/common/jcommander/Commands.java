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

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.internal.Console;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.febit.lang.util.Defaults.nvl;

@Slf4j
@Accessors(chain = true, fluent = true)
@RequiredArgsConstructor(staticName = "create")
public class Commands implements Context {

    @Getter
    private final JCommander jcommander = new JCommander();
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final Map<String, ICommand<?>> commands = new HashMap<>();

    @Getter
    @Setter
    private String defaultCommand = HelpCommand.CMD;

    @Getter
    @Setter
    private boolean acceptUnknownOptions = false;

    @Getter
    @Setter
    @Nullable
    private Console console = null;

    @Getter
    @Setter
    @Nullable
    private IDefaultProvider defaultProvider = null;

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T requireBean(Class<T> type) {
        var param = beans.get(type);
        Objects.requireNonNull(param, "Not found bean with type: " + type);
        return (T) param;
    }

    private void addBean(Object bean) {
        Objects.requireNonNull(bean);
        this.beans.put(bean.getClass(), bean);
    }

    public Commands addGlobal(IOptions options) {
        jcommander.addObject(options);
        addBean(options);
        return this;
    }

    public Commands add(ICommand<?> command) {
        var options = command.newOptions();
        var keys = command.keys();

        Objects.requireNonNull(options, "Null options for command: " + command.getClass().getName());
        Objects.requireNonNull(keys, "Null keys for command: " + command.getClass().getName());
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("At least one key required for command: "
                    + command.getClass().getName());
        }

        addBean(options);
        keys.forEach(k -> commands.put(k, command));

        var aliases = keys.subList(1, keys.size()).toArray(new String[0]);
        jcommander.addCommand(keys.get(0), options,
                aliases
        );
        return this;
    }

    public Commands add(ICommand<?>... commands) {
        Stream.of(commands).forEach(this::add);
        return this;
    }

    public void exec(String... args) {
        var cmd = this.jcommander;

        cmd.setConsole(this.console);
        cmd.setAcceptUnknownOptions(this.acceptUnknownOptions);
        cmd.setDefaultProvider(this.defaultProvider);

        cmd.parse(args);

        var key = nvl(jcommander().getParsedCommand(), this.defaultCommand);
        var command = commands.getOrDefault(key, HelpCommand.INSTANCE);
        command.exec(this);
    }

}
