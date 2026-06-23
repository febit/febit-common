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

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HelpCommandTest {

    @Test
    void shouldHaveCorrectKeys() {
        assertThat(HelpCommand.INSTANCE.keys()).containsExactly(HelpCommand.CMD);
    }

    @Test
    void shouldCreateHelpOptions() {
        var options = HelpCommand.INSTANCE.newOptions();
        assertThat(options).isNotNull();
        assertThat(options).isInstanceOf(HelpOptions.class);
    }

    @Test
    void shouldImplementICommand() {
        assertThat(HelpCommand.INSTANCE).isInstanceOf(ICommand.class);
    }

    @Test
    void execShouldCallJcommanderUsage() {
        var jcommander = new JCommander();
        var jcommanderSpy = spy(jcommander);

        var context = mock(Context.class);
        when(context.jcommander()).thenReturn(jcommanderSpy);

        HelpCommand.INSTANCE.exec(context);

        verify(jcommanderSpy).usage();
    }
}
