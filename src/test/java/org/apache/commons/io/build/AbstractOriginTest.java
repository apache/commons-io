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

import java.io.Closeable;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    protected AbstractOrigin<T, B> originRo;
    protected AbstractOrigin<T, B> originRw;

    @TempDir
    protected Path tempPath;

    @BeforeEach
    void beforeEach() throws IOException {
        setOriginRo(newOriginRo());
        resetOriginRw();
        setOriginRw(newOriginRw());
    }

    @AfterEach
    void cleanup() {
        final T originRo = getOriginRo().get();
        if (originRo instanceof Closeable) {
            IOUtils.closeQuietly((Closeable) originRo);
        }
        final T originRw = getOriginRw().get();
        if (originRw instanceof Closeable) {
            IOUtils.closeQuietly((Closeable) originRw);
        }
    }

    protected AbstractOrigin<T, B> getOriginRo() {
        return Objects.requireNonNull(originRo, "originRo");
    }

    protected AbstractOrigin<T, B> getOriginRw() {
        return Objects.requireNonNull(originRw, "originRw");
    }

    @SuppressWarnings("resource")
    private boolean isValid(final RandomAccessFile raf) throws IOException {
        return Objects.requireNonNull(raf).getFD().valid();
    }

    protected abstract B newOriginRo() throws IOException;

    protected abstract B newOriginRw() throws IOException;

    protected void setOriginRo(final AbstractOrigin<T, B> origin) {
        this.originRo = origin;
    }

    protected void setOriginRw(final AbstractOrigin<T, B> origin) {
        this.originRw = origin;
    }

    protected void resetOriginRw() throws IOException {
        // No-op
    }

    byte[] getFixtureByteArray() throws IOException {
        return IOUtils.resourceToByteArray(FILE_RES_RO);
    }

    String getFixtureString() throws IOException {
        return IOUtils.resourceToString(FILE_RES_RO, StandardCharsets.UTF_8);
    }

    @Test
    void testGetByteArray() throws IOException {
        assertArrayEquals(getFixtureByteArray(), getOriginRo().getByteArray());
    }

    @Test
    void testGetByteArrayAt_0_0() throws IOException {
        assertArrayEquals(new byte[] {}, getOriginRo().getByteArray(0, 0));
    }

    @Test
    void testGetByteArrayAt_0_1() throws IOException {
        assertArrayEquals(new byte[] { '1' }, getOriginRo().getByteArray(0, 1));
    }

    @Test
    void testGetByteArrayAt_1_1() throws IOException {
        assertArrayEquals(new byte[] { '2' }, getOriginRo().getByteArray(1, 1));
    }

    @Test
    void testGetCharSequence() throws IOException {
        final CharSequence charSequence = getOriginRo().getCharSequence(StandardCharsets.UTF_8);
        assertNotNull(charSequence);
        assertEquals(getFixtureString(), charSequence.toString());
    }

    @Test
    void testGetFile() throws IOException {
        testGetFile(getOriginRo().getFile(), RO_LENGTH);
        FileUtils.touch(getOriginRw().getFile());
        testGetFile(getOriginRw().getFile(), 0);
    }

    private void testGetFile(final File file, final long expectedLen) throws IOException {
        assertNotNull(file);
        assertTrue(file.exists(), () -> "File does not exist: " + file);
        final int length = FileUtils.readFileToByteArray(file).length;
        assertEquals(length, expectedLen, () -> String.format("File %s, actual length=%,d", file, length));
    }

    @Test
    void testGetInputStream() throws IOException {
        try (InputStream inputStream = getOriginRo().getInputStream()) {
            assertNotNull(inputStream);
            assertArrayEquals(getFixtureByteArray(), IOUtils.toByteArray(inputStream));
        }
    }

    @Test
    void testGetOutputStream() throws IOException {
        try (OutputStream output = getOriginRw().getOutputStream()) {
            assertNotNull(output);
        }
    }

    @Test
    void testGetPath() throws IOException {
        testGetPath(getOriginRo().getPath(), RO_LENGTH);
        FileUtils.touch(getOriginRw().getPath().toFile());
        testGetPath(getOriginRw().getPath(), 0);
    }

    private void testGetPath(final Path path, final long expectedLen) throws IOException {
        assertNotNull(path);
        assertTrue(Files.exists(path));
        final int length = Files.readAllBytes(path).length;
        assertEquals(length, expectedLen, () -> String.format("Path %s, actual length=%,d", path, length));
    }

    @Test
    void testGetRandomAccessFile() throws IOException {
        // Default
        try (RandomAccessFile raf = getOriginRo().getRandomAccessFile()) {
            assertNotNull(raf);
            assertTrue(isValid(raf));
        }
        final boolean isRafOriginRo = getOriginRo() instanceof RandomAccessFileOrigin;
        final boolean isRafOriginRw = getOriginRw() instanceof RandomAccessFileOrigin;
        // Same as above, but underlying resource is now closed.
        try (RandomAccessFile raf = getOriginRo().getRandomAccessFile()) {
            assertNotNull(raf);
            assertFalse(isRafOriginRo && isValid(raf));
        }
        // Read
        try (RandomAccessFile raf = getOriginRo().getRandomAccessFile(StandardOpenOption.READ)) {
            assertNotNull(raf);
            assertFalse(isRafOriginRo && isValid(raf));
        }
        // Write, first access
        try (RandomAccessFile raf = getOriginRw().getRandomAccessFile(StandardOpenOption.WRITE)) {
            assertNotNull(raf);
            if (isRafOriginRw || getOriginRw().getFile() != null) {
                assertTrue(isValid(raf), () -> getOriginRw().toString());
            } else {
                // Can't get there from here.
                assertFalse(isValid(raf), () -> getOriginRw().toString());
            }
        }
        // Read, Write, underlying resource is now closed.
        try (RandomAccessFile raf = getOriginRw().getRandomAccessFile(StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            assertNotNull(raf);
            assertFalse(isRafOriginRw && isValid(raf));
        }
    }

    @ParameterizedTest
    @EnumSource(StandardOpenOption.class)
    void testGetRandomAccessFile(final OpenOption openOption) throws IOException {
        // Default
        try (RandomAccessFile raf = getOriginRw().getRandomAccessFile()) {
            assertNotNull(raf);
            assertTrue(isValid(raf));
        }
        // Same as above, but underlying resource is now closed.
        final boolean isRafOrigin = getOriginRw() instanceof RandomAccessFileOrigin;
        try (RandomAccessFile raf = getOriginRw().getRandomAccessFile()) {
            assertNotNull(raf);
            assertFalse(isRafOrigin && isValid(raf));
        }
        try (RandomAccessFile raf = getOriginRw().getRandomAccessFile(openOption)) {
            assertNotNull(raf);
            assertFalse(isRafOrigin && isValid(raf));
        }
        try (RandomAccessFile raf = getOriginRw().getRandomAccessFile(openOption)) {
            assertNotNull(raf);
            assertFalse(isRafOrigin && isValid(raf));
        }
    }

    @Test
    void testGetReader() throws IOException {
        try (Reader reader = getOriginRo().getReader(Charset.defaultCharset())) {
            assertNotNull(reader);
        }
        setOriginRo(newOriginRo());
        try (Reader reader = getOriginRo().getReader(null)) {
            assertNotNull(reader);
        }
        setOriginRo(newOriginRo());
        try (Reader reader = getOriginRo().getReader(StandardCharsets.UTF_8)) {
            assertNotNull(reader);
            assertEquals(getFixtureString(), IOUtils.toString(reader));
        }
    }

    @Test
    void testGetWriter() throws IOException {
        try (Writer writer = getOriginRw().getWriter(Charset.defaultCharset())) {
            assertNotNull(writer);
        }
        setOriginRw(newOriginRw());
        try (Writer writer = getOriginRw().getWriter(null)) {
            assertNotNull(writer);
        }
    }

    @Test
    void testGetReadableByteChannel() throws IOException {
        try (ReadableByteChannel channel =
                getOriginRo().getChannel(ReadableByteChannel.class, StandardOpenOption.READ)) {
            final SeekableByteChannel seekable =
                    channel instanceof SeekableByteChannel ? (SeekableByteChannel) channel : null;
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

    private void checkRead(ReadableByteChannel channel) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(RO_LENGTH);
        int read = channel.read(buffer);
        assertEquals(RO_LENGTH, read);
        assertArrayEquals(getFixtureByteArray(), buffer.array());
        // Channel is at EOF
        buffer.clear();
        read = channel.read(buffer);
        assertEquals(-1, read);
    }

    @Test
    void testGetWritableByteChannel() throws IOException {
        final boolean supportsRead;
        try (WritableByteChannel channel =
                getOriginRw().getChannel(WritableByteChannel.class, StandardOpenOption.WRITE)) {
            supportsRead = channel instanceof ReadableByteChannel;
            final SeekableByteChannel seekable =
                    channel instanceof SeekableByteChannel ? (SeekableByteChannel) channel : null;
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
            setOriginRw(newOriginRw());
            try (ReadableByteChannel channel =
                    getOriginRw().getChannel(ReadableByteChannel.class, StandardOpenOption.READ)) {
                assertNotNull(channel);
                assertTrue(channel.isOpen());
                checkRead(channel);
            }
        }
        setOriginRw(newOriginRw());
        try (WritableByteChannel channel =
                getOriginRw().getChannel(WritableByteChannel.class, StandardOpenOption.WRITE)) {
            final SeekableByteChannel seekable =
                    channel instanceof SeekableByteChannel ? (SeekableByteChannel) channel : null;
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

    private void checkWrite(WritableByteChannel channel) throws IOException {
        final ByteBuffer buffer = ByteBuffer.wrap(getFixtureByteArray());
        final int written = channel.write(buffer);
        assertEquals(RO_LENGTH, written);
    }

    @Test
    void testSize() throws IOException {
        assertEquals(RO_LENGTH, getOriginRo().getByteArray().length);
    }
}
