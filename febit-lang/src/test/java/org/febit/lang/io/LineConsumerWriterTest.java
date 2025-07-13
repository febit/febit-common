package org.febit.lang.io;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LineConsumerWriterTest {

    @Test
    void illegalArguments() {
        @SuppressWarnings("unchecked")
        var sink = (Consumer<String>) Mockito.mock(Consumer.class);
        try (var writer = LineConsumerWriter.create(sink)) {
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(new char[0], -1, 0));
            assertThrows(IllegalArgumentException.class, () -> writer.write(new char[0], 0, -1));
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(new char[0], 0, 1));
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(new char[0], 1, 0));

            assertDoesNotThrow(() -> writer.write(new char[0], 0, 0));
            assertDoesNotThrow(() -> writer.write(new char[1], 1, 0));
        }
    }

    @Test
    void empty() throws IOException {
        var sink = new ArrayList<String>();
        //noinspection EmptyTryBlock
        try (var writer = LineConsumerWriter.create(sink::add)) {
            // No content to write
        }
        assertTrue(sink.isEmpty(), "sink should be empty");

        try (var writer = LineConsumerWriter.create(sink::add)) {
            writer.write("");
        }
        assertTrue(sink.isEmpty(), "sink should be empty");
    }

    @Test
    @SuppressWarnings("unchecked")
    void step() throws IOException {
        var sink = (Consumer<String>) Mockito.mock(Consumer.class);
        try (var writer = LineConsumerWriter.create(sink)) {

            reset(sink);
            writer.write("Hello");
            verify(sink, never()).accept(anyString());
            writer.write("\r");
            verify(sink, never()).accept(anyString());

            reset(sink);
            writer.write("World\n");
            verify(sink).accept("Hello\r");
            verify(sink).accept("World\n");

            reset(sink);
            writer.write("Hello");
            verify(sink, never()).accept(anyString());
            writer.write("\n");
            verify(sink).accept("Hello\n");

            reset(sink);
            writer.write("abc\n\n\n\n\n");
            verify(sink).accept("abc\n");
            verify(sink, times(4)).accept("\n");

            // Final string without line break
            reset(sink);
            writer.write("Final");
            verify(sink, never()).accept(anyString());
        }
        verify(sink).accept("Final");
    }

    @Test
    void single() throws IOException {
        var line = "Hello World\n";
        var sink = new ArrayList<String>();
        try (var writer = LineConsumerWriter.create(sink::add)) {
            writer.write(line);
        }
        assertEquals(List.of(line), sink);

        sink.clear();
        try (var writer = LineConsumerWriter.create(sink::add)) {
            line.chars().forEach(c -> {
                try {
                    writer.write(c);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        assertEquals(List.of(line), sink);
    }

    @Test
    void crlf() throws IOException {
        var line = "\r\t\f\n\rHello\r\n\n\rWorld\r";
        var expected = List.of(
                "\r", "\t\f\n", "\r", "Hello\r\n", "\n", "\r", "World\r"
        );

        var sink = new ArrayList<String>();
        try (var writer = LineConsumerWriter.create(sink::add)) {
            writer.write(line);
        }
        assertEquals(expected, sink);

        sink.clear();
        try (var writer = LineConsumerWriter.create(sink::add)) {
            line.chars().forEach(c -> {
                try {
                    writer.write(c);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        assertEquals(expected, sink);

        sink.clear();
        try (var writer = LineConsumerWriter.create(sink::add)) {
            expected.forEach(c -> {
                try {
                    writer.write(c);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        assertEquals(expected, sink);
    }

}
