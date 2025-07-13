package org.febit.lang.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class LinesTest {

    @Test
    void asWriter() throws IOException {
        var consumer = new ArrayList<String>();
        try (var writer = Lines.asWriter(consumer::add)) {
            writer.write("Hello\nWorld");
        }
        assertEquals(List.of(
                "Hello\n",
                "World"
        ), consumer);
    }

    @Test
    void asOutputStream() throws IOException {
        var consumer = new ArrayList<String>();
        try (var out = Lines.asOutputStream(consumer::add, UTF_8)) {
            out.write("Hello\nWorld".getBytes(UTF_8));
        }
        assertEquals(List.of(
                "Hello\n",
                "World"
        ), consumer);
    }
}
