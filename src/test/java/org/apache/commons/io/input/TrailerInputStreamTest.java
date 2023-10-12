/*
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
package org.apache.commons.io.input;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TrailerInputStreamTest {

    private static class ChunkInputStream extends InputStream {

        private final Iterator<byte []> chunks;

        public ChunkInputStream(final Iterator<byte []> chunks) {
            this.chunks = chunks;
        }

        @Override
        public int read() throws IOException {
            final byte[] buffer = new byte[1];
            final int read = this.read(buffer);
            if (read == IOUtils.EOF) {
                return IOUtils.EOF;
            }
            return buffer[0];
        }

        @Override
        public int read(final byte[] b, final int off, final int len) {
            final byte[] chunk;
            try {
                chunk = this.chunks.next();
            } catch (
                    @SuppressWarnings("unused")
                    final NoSuchElementException unused) {
                return IOUtils.EOF;
            }
            Assertions.assertNotEquals(0, chunk.length);
            Assertions.assertTrue(chunk.length <= len);
            if (this.chunks.hasNext()) {
                Assertions.assertEquals(chunk.length, len);
            }
            final int read = Math.min(chunk.length, len);
            System.arraycopy(chunk, 0, b, off, read);
            return read;
        }

        @Override
        public void close() {
            Assertions.assertFalse(this.chunks.hasNext());
        }
    }

    public static Stream<String> createTestStringChunkStream(
            final int trailerLength,
            final int chunkLength,
            final int chunks,
            final int lastChunkReduction) {
        final List<String> cs = new ArrayList<>();
        char c = 'a';
        if (trailerLength > 0) {
            cs.add(StringUtils.repeat(c++, trailerLength));
        }
        for (int i = 0; i < chunks; i++) {
            int cl = chunkLength;
            if (i == chunkLength - 1) {
                cl -= lastChunkReduction;
            }
            if (cl <= trailerLength || trailerLength == 0) {
                cs.add(StringUtils.repeat(c++, cl));
            } else {
                cs.add(StringUtils.repeat(c++, cl - trailerLength));
                cs.add(StringUtils.repeat(c++, trailerLength));
            }
            Assertions.assertTrue(c <= 'z');
        }
        return cs.stream();
    }

    public static Stream<byte []> createTestBytesChunkStream(
            final int trailerLength,
            final int chunkLength,
            final int chunks,
            final int lastChunkReduction) {
        return TrailerInputStreamTest.createTestStringChunkStream(
                        trailerLength, chunkLength, chunks, lastChunkReduction)
                .map(s -> s.getBytes(StandardCharsets.UTF_8));
    }

    public static InputStream createTestInputStream(
            final int trailerLength,
            final int chunkLength,
            final int chunks,
            final int lastChunkReduction) {
        return new ChunkInputStream(
                TrailerInputStreamTest.createTestBytesChunkStream(
                                trailerLength, chunkLength, chunks, lastChunkReduction)
                        .iterator());
    }

    public static String utf8String(
            final IOConsumer<? super OutputStream> consumer) throws IOException {
        try (StringWriter sw = new StringWriter();
                WriterOutputStream wos = WriterOutputStream.builder().setCharset(StandardCharsets.UTF_8).setWriter(sw).get()) {
            consumer.accept(wos);
            wos.flush();
            sw.flush();
            return sw.toString();
        }
    }

    public static void assertDataTrailer(
            final int trailerLength,
            final int chunkLength,
            final int chunks,
            final int lastChunkReduction,
            final ByteArrayOutputStream os,
            final TrailerInputStream tis)
            throws IOException {
        final String d =
                TrailerInputStreamTest.createTestStringChunkStream(
                                trailerLength, chunkLength, chunks, lastChunkReduction)
                        .collect(Collectors.joining());
        final String data = d.substring(0, d.length() - trailerLength);
        final String trailer = d.substring(d.length() - trailerLength);
        os.flush();
        Assertions.assertAll(
                () -> Assertions.assertEquals(d, data + trailer, "Generation of expectation"),
                () -> Assertions.assertEquals(trailerLength, trailer.length(), "Trailer length"),
                () -> Assertions.assertEquals(data, utf8String(os::writeTo), "Data content"),
                () -> Assertions.assertEquals(
                                trailer, utf8String(tis::copyTrailer), "Trailer content"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 7, 10})
    public void testReadBytewise(final int trailerLength) throws IOException {
        final int chunkLength = 1;
        final int chunks = 5;
        final int lastChunkReduction = 0;
        try (InputStream is =
                        TrailerInputStreamTest.createTestInputStream(
                                trailerLength, chunkLength, chunks, lastChunkReduction);
                TrailerInputStream tis = new TrailerInputStream(is, trailerLength);
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Assertions.assertEquals(
                    StringUtils.repeat('a', trailerLength), utf8String(tis::copyTrailer));
            int read;
            while ((read = tis.read()) != IOUtils.EOF) {
                os.write(read);
            }
            assertDataTrailer(trailerLength, chunkLength, chunks, lastChunkReduction, os, tis);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 7, 10})
    public void testReadWholeBlocks(final int trailerLength) throws IOException {
        final int chunkLength = 7;
        final int chunks = 5;
        final int lastChunkReduction = 0;
        try (InputStream is =
                        TrailerInputStreamTest.createTestInputStream(
                                trailerLength, chunkLength, chunks, lastChunkReduction);
                TrailerInputStream tis = new TrailerInputStream(is, trailerLength);
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Assertions.assertEquals(
                    StringUtils.repeat('a', trailerLength), utf8String(tis::copyTrailer));
            final byte[] buffer = new byte[chunkLength];
            int read;
            while ((read = tis.read(buffer)) != IOUtils.EOF) {
                os.write(buffer, 0, read);
            }
            assertDataTrailer(trailerLength, chunkLength, chunks, lastChunkReduction, os, tis);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 7, 10})
    public void testReadLastBlockAlmostFull(final int trailerLength) throws IOException {
        final int chunkLength = 7;
        final int chunks = 5;
        final int lastChunkReduction = 1;
        try (InputStream is =
                        TrailerInputStreamTest.createTestInputStream(
                                trailerLength, chunkLength, chunks, lastChunkReduction);
                TrailerInputStream tis = new TrailerInputStream(is, trailerLength);
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Assertions.assertEquals(
                    StringUtils.repeat('a', trailerLength), utf8String(tis::copyTrailer));
            final byte[] buffer = new byte[chunkLength + 3 * chunks];
            int offset = chunks;
            while (true) {
                Arrays.fill(buffer, (byte) '?');
                final int read = tis.read(buffer, offset, chunkLength);
                if (read == IOUtils.EOF) {
                    break;
                }
                os.write(buffer, offset, read);
                offset++;
            }
            assertDataTrailer(trailerLength, chunkLength, chunks, lastChunkReduction, os, tis);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 7, 10})
    public void testReadLastBlockAlmostEmpty(final int trailerLength) throws IOException {
        final int chunkLength = 7;
        final int chunks = 5;
        final int lastChunkReduction = chunkLength - 1;
        try (InputStream is =
                        TrailerInputStreamTest.createTestInputStream(
                                trailerLength, chunkLength, chunks, lastChunkReduction);
                TrailerInputStream tis = new TrailerInputStream(is, trailerLength);
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Assertions.assertEquals(
                    StringUtils.repeat('a', trailerLength), utf8String(tis::copyTrailer));
            final byte[] buffer = new byte[chunkLength + 3 * chunks];
            int offset = chunks;
            while (true) {
                Arrays.fill(buffer, (byte) '?');
                final int read = tis.read(buffer, offset, chunkLength);
                if (read == IOUtils.EOF) {
                    break;
                }
                os.write(buffer, offset, read);
                offset++;
            }
            assertDataTrailer(trailerLength, chunkLength, chunks, lastChunkReduction, os, tis);
        }
    }
}
