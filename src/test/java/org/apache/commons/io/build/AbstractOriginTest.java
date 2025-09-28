/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.build;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractOrigin.RandomAccessFileOrigin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link AbstractOrigin} and subclasses.
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 */
public abstract class AbstractOriginTest<T, B extends AbstractOrigin<T, B>> {

    protected static final String FILE_RES_RO = "/org/apache/commons/io/test-file-20byteslength.bin";
    protected static final String FILE_NAME_RO = "src/test/resources" + FILE_RES_RO;
    protected static final String FILE_NAME_RW = AbstractOriginTest.class.getSimpleName() + ".txt";
    private static final int RO_LENGTH = 20;

    @TempDir
    protected Path tempPath;

    /**
     * Asserts that the origin is open.
     *
     * @param origin The origin to test.
     */
    protected void assertOpen(final AbstractOrigin<T, B> origin) {
        // No-op
    }

    private void checkRead(final ReadableByteChannel channel) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(RO_LENGTH);
        int read = channel.read(buffer);
        assertEquals(RO_LENGTH, read);
        assertArrayEquals(getFixtureByteArray(), buffer.array());
        // Channel is at EOF
        buffer.clear();
        read = channel.read(buffer);
        assertEquals(-1, read);
    }

    private void checkWrite(final WritableByteChannel channel) throws IOException {
        final ByteBuffer buffer = ByteBuffer.wrap(getFixtureByteArray());
        final int written = channel.write(buffer);
        assertEquals(RO_LENGTH, written);
    }

    byte[] getFixtureByteArray() throws IOException {
        return IOUtils.resourceToByteArray(FILE_RES_RO);
    }

    String getFixtureString() throws IOException {
        return IOUtils.resourceToString(FILE_RES_RO, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("resource")
    private boolean isValid(final RandomAccessFile raf) throws IOException {
        return Objects.requireNonNull(raf).getFD().valid();
    }

    protected abstract B newOriginRo() throws IOException;

    protected abstract B newOriginRw() throws IOException;

    @Test
    void testGetByteArray() throws IOException {
        try (AbstractOrigin<T, B> originRo = newOriginRo()) {
            assertArrayEquals(getFixtureByteArray(), originRo.getByteArray());
            assertOpen(originRo);
        }
    }

    @Test
    void testGetByteArrayAt_0_0() throws IOException {
        try (AbstractOrigin<T, B> originRo = newOriginRo()) {
            assertArrayEquals(new byte[] {}, originRo.getByteArray(0, 0));
            assertOpen(originRo);
        }
    }

    @Test
    void testGetByteArrayAt_0_1() throws IOException {
        try (AbstractOrigin<T, B> originRo = newOriginRo()) {
            assertArrayEquals(new byte[] { '1' }, originRo.getByteArray(0, 1));
            assertOpen(originRo);
        }
    }

    @Test
    void testGetByteArrayAt_1_1() throws IOException {
        try (AbstractOrigin<T, B> originRo = newOriginRo()) {
            assertArrayEquals(new byte[] { '2' }, originRo.getByteArray(1, 1));
            assertOpen(originRo);
        }
    }

    @Test
    void testGetCharSequence() throws IOException {
        try (AbstractOrigin<T, B> originRo = newOriginRo()) {
            final CharSequence charSequence = originRo.getCharSequence(StandardCharsets.UTF_8);
            assertNotNull(charSequence);
            assertEquals(getFixtureString(), charSequence.toString());
            assertOpen(originRo);
        }
    }

    @Test
    void testGetFile() throws IOException {
        try (AbstractOrigin<T, B> originRo = newOriginRo();
             AbstractOrigin<T, B> originRw = newOriginRw()) {
            testGetFile(originRo.getFile(), RO_LENGTH);
            FileUtils.touch(originRw.getFile());
            testGetFile(originRw.getFile(), 0);
        }
    }

    private void testGetFile(final File file, final long expectedLen) throws IOException {
        assertNotNull(file);
        assertTrue(file.exists(), () -> "File does not exist: " + file);
        final int length = FileUtils.readFileToByteArray(file).length;
        assertEquals(length, expectedLen, () -> String.format("File %s, actual length=%,d", file, length));
    }

    @Test
    void testGetInputStream() throws IOException {
        try (InputStream inputStream = newOriginRo().getInputStream()) {
            assertNotNull(inputStream);
            assertArrayEquals(getFixtureByteArray(), IOUtils.toByteArray(inputStream));
        }
    }

    @Test
    void testGetOutputStream() throws IOException {
        try (OutputStream output = newOriginRw().getOutputStream()) {
            assertNotNull(output);
        }
    }

    @Test
    void testGetPath() throws IOException {
        try (AbstractOrigin<T, B> originRo = newOriginRo();
             AbstractOrigin<T, B> originRw = newOriginRw()) {
            testGetPath(originRo.getPath(), RO_LENGTH);
            FileUtils.touch(originRw.getPath().toFile());
            testGetPath(originRw.getPath(), 0);
        }
    }

    private void testGetPath(final Path path, final long expectedLen) throws IOException {
        assertNotNull(path);
        assertTrue(Files.exists(path));
        final int length = Files.readAllBytes(path).length;
        assertEquals(length, expectedLen, () -> String.format("Path %s, actual length=%,d", path, length));
    }

    @Test
    void testGetRandomAccessFile() throws IOException {
        final AbstractOrigin<T, B> originRo = newOriginRo();
        final AbstractOrigin<T, B> originRw = newOriginRw();
        // Default
        try (RandomAccessFile raf = originRo.getRandomAccessFile()) {
            assertNotNull(raf);
            assertTrue(isValid(raf));
        }
        final boolean isRafOriginRo = originRo instanceof RandomAccessFileOrigin;
        final boolean isRafOriginRw = originRw instanceof RandomAccessFileOrigin;
        // Same as above, but underlying resource is now closed.
        try (RandomAccessFile raf = originRo.getRandomAccessFile()) {
            assertNotNull(raf);
            assertFalse(isRafOriginRo && isValid(raf));
        }
        // Read
        try (RandomAccessFile raf = originRo.getRandomAccessFile(StandardOpenOption.READ)) {
            assertNotNull(raf);
            assertFalse(isRafOriginRo && isValid(raf));
        }
        // Write, first access
        try (RandomAccessFile raf = originRw.getRandomAccessFile(StandardOpenOption.WRITE)) {
            assertNotNull(raf);
            if (isRafOriginRw || originRw.getFile() != null) {
                assertTrue(isValid(raf), () -> originRw.toString());
            } else {
                // Can't get there from here.
                assertFalse(isValid(raf), () -> originRw.toString());
            }
        }
        // Read, Write, underlying resource is now closed.
        try (RandomAccessFile raf = originRw.getRandomAccessFile(StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            assertNotNull(raf);
            assertFalse(isRafOriginRw && isValid(raf));
        }
    }

    @ParameterizedTest
    @EnumSource(StandardOpenOption.class)
    void testGetRandomAccessFile(final OpenOption openOption) throws IOException {
        final AbstractOrigin<T, B> originRw = newOriginRw();
        // Default
        try (RandomAccessFile raf = originRw.getRandomAccessFile()) {
            assertNotNull(raf);
            assertTrue(isValid(raf));
        }
        // Same as above, but underlying resource is now closed.
        final boolean isRafOrigin = originRw instanceof RandomAccessFileOrigin;
        try (RandomAccessFile raf = originRw.getRandomAccessFile()) {
            assertNotNull(raf);
            assertFalse(isRafOrigin && isValid(raf));
        }
        try (RandomAccessFile raf = originRw.getRandomAccessFile(openOption)) {
            assertNotNull(raf);
            assertFalse(isRafOrigin && isValid(raf));
        }
        try (RandomAccessFile raf = originRw.getRandomAccessFile(openOption)) {
            assertNotNull(raf);
            assertFalse(isRafOrigin && isValid(raf));
        }
    }

    @Test
    void testGetReadableByteChannel() throws IOException {
        try (ReadableByteChannel channel = newOriginRo().getChannel(ReadableByteChannel.class, StandardOpenOption.READ)) {
            final SeekableByteChannel seekable = channel instanceof SeekableByteChannel ? (SeekableByteChannel) channel : null;
            assertNotNull(channel);
            assertTrue(channel.isOpen());
            if (seekable != null) {
                assertEquals(0, seekable.position());
                assertEquals(RO_LENGTH, seekable.size());
            }
            checkRead(channel);
            if (seekable != null) {
                assertEquals(RO_LENGTH, seekable.position());
            }
        }
    }

    @Test
    void testGetReader() throws IOException {
        try (Reader reader = newOriginRo().getReader(Charset.defaultCharset())) {
            assertNotNull(reader);
        }
        try (Reader reader = newOriginRo().getReader(null)) {
            assertNotNull(reader);
        }
        try (Reader reader = newOriginRo().getReader(StandardCharsets.UTF_8)) {
            assertNotNull(reader);
            assertEquals(getFixtureString(), IOUtils.toString(reader));
        }
    }

    @Test
    void testGetWritableByteChannel() throws IOException {
        final boolean supportsRead;
        try (WritableByteChannel channel = newOriginRw().getChannel(WritableByteChannel.class, StandardOpenOption.WRITE)) {
            supportsRead = channel instanceof ReadableByteChannel;
            final SeekableByteChannel seekable = channel instanceof SeekableByteChannel ? (SeekableByteChannel) channel : null;
            assertNotNull(channel);
            assertTrue(channel.isOpen());
            if (seekable != null) {
                assertEquals(0, seekable.position());
                assertEquals(0, seekable.size());
            }
            checkWrite(channel);
            if (seekable != null) {
                assertEquals(RO_LENGTH, seekable.position());
                assertEquals(RO_LENGTH, seekable.size());
            }
        }
        if (supportsRead) {
            try (ReadableByteChannel channel = newOriginRw().getChannel(ReadableByteChannel.class, StandardOpenOption.READ)) {
                assertNotNull(channel);
                assertTrue(channel.isOpen());
                checkRead(channel);
            }
        }
        try (WritableByteChannel channel = newOriginRw().getChannel(WritableByteChannel.class, StandardOpenOption.WRITE)) {
            final SeekableByteChannel seekable = channel instanceof SeekableByteChannel ? (SeekableByteChannel) channel : null;
            assertNotNull(channel);
            assertTrue(channel.isOpen());
            if (seekable != null) {
                seekable.position(RO_LENGTH);
                assertEquals(RO_LENGTH, seekable.position());
                assertEquals(RO_LENGTH, seekable.size());
                // Truncate
                final int newSize = RO_LENGTH / 2;
                seekable.truncate(newSize);
                assertEquals(newSize, seekable.position());
                assertEquals(newSize, seekable.size());
                // Rewind
                seekable.position(0);
                assertEquals(0, seekable.position());
            }
        }
    }

    @Test
    void testGetWriter() throws IOException {
        try (Writer writer = newOriginRw().getWriter(Charset.defaultCharset())) {
            assertNotNull(writer);
        }
        try (Writer writer = newOriginRw().getWriter(null)) {
            assertNotNull(writer);
        }
    }

    @Test
    void testSize() throws IOException {
        try (AbstractOrigin<T, B> originRo = newOriginRo()) {
            assertEquals(RO_LENGTH, originRo.getByteArray().length);
        }
    }
}
