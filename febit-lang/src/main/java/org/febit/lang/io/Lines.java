package org.febit.lang.io;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@UtilityClass
public class Lines {

    private static final int BUFFER_SIZE = 1024;

    public static Writer asWriter(Consumer<String> consumer) {
        return LineConsumerWriter.create(consumer);
    }

    public static OutputStream asUtf8OutputStream(Consumer<String> consumer) {
        return asOutputStream(consumer, StandardCharsets.UTF_8);
    }

    public static OutputStream asOutputStream(Consumer<String> consumer, Charset charset) {
        var writer = asWriter(consumer);
        try {
            return WriterOutputStream.builder()
                    .setWriter(writer)
                    .setCharset(charset)
                    .setBufferSize(BUFFER_SIZE)
                    .setWriteImmediately(true)
                    .get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
