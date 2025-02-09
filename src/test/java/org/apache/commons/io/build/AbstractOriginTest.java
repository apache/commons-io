/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.build.AbstractOrigin.RandomAccessFileOrigin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    protected static final String FILE_NAME_RW = "target/" + AbstractOriginTest.class.getSimpleName() + ".txt";
    private static final int RO_LENGTH = 20;

    protected AbstractOrigin<T, B> originRo;
    protected AbstractOrigin<T, B> originRw;

    @BeforeEach
    public void beforeEach() throws IOException {
        setOriginRo(newOriginRo());
        setOriginRw(newOriginRw());
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

    @Test
    public void testGetByteArray() throws IOException {
        assertArrayEquals(Files.readAllBytes(Paths.get(FILE_NAME_RO)), getOriginRo().getByteArray());
    }

    @Test
    public void testGetByteArrayAt_0_0() throws IOException {
        assertArrayEquals(new byte[] {}, getOriginRo().getByteArray(0, 0));
    }

    @Test
    public void testGetByteArrayAt_0_1() throws IOException {
        assertArrayEquals(new byte[] { '1' }, getOriginRo().getByteArray(0, 1));
    }

    @Test
    public void testGetByteArrayAt_1_1() throws IOException {
        assertArrayEquals(new byte[] { '2' }, getOriginRo().getByteArray(1, 1));
    }

    @Test
    public void testGetCharSequence() throws IOException {
        assertNotNull(getOriginRo().getCharSequence(Charset.defaultCharset()));
    }

    @Test
    public void testGetFile() throws IOException {
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
    public void testGetInputStream() throws IOException {
        try (InputStream inputStream = getOriginRo().getInputStream()) {
            assertNotNull(inputStream);
        }
    }

    @Test
    public void testGetOutputStream() throws IOException {
        try (OutputStream output = getOriginRw().getOutputStream()) {
            assertNotNull(output);
        }
    }

    @Test
    public void testGetPath() throws IOException {
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
    public void testGetRandomAccessFile() throws IOException {
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
    public void testGetRandomAccessFile(final OpenOption openOption) throws IOException {
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
    public void testGetReader() throws IOException {
        try (Reader reader = getOriginRo().getReader(Charset.defaultCharset())) {
            assertNotNull(reader);
        }
    }

    @Test
    public void testGetWriter() throws IOException {
        try (Writer writer = getOriginRw().getWriter(Charset.defaultCharset())) {
            assertNotNull(writer);
        }
    }

    @Test
    public void testSize() throws IOException {
        assertEquals(Files.size(Paths.get(FILE_NAME_RO)), getOriginRo().getByteArray().length);
    }
}
