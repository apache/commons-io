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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.build.AbstractOrigin.ReaderOrigin;
import org.apache.commons.io.input.CharSequenceReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link ReaderOrigin}.
 *
 * A ReaderOrigin can convert into some of the other aspects.
 *
 * @see Reader
 */
class ReaderOriginTest extends AbstractOriginTest<Reader, ReaderOrigin> {
    @Override
    protected void assertOpen(AbstractOrigin<Reader, ReaderOrigin> origin) {
        // If the underlying FileInputStream is closed, ready() calls available() which throws IOException.
        assertDoesNotThrow(() -> origin.get().ready(), "Reader not open");
    }

    @Override
    protected ReaderOrigin newOriginRo() throws FileNotFoundException {
        return new ReaderOrigin(new FileReader(FILE_NAME_RO));
    }

    @Override
    protected ReaderOrigin newOriginRw() {
        return new ReaderOrigin(new CharSequenceReader("World"));
    }

    @Test
    void testClosesOrigin() throws IOException {
        final Reader resource = mock(Reader.class);
        final ReaderOrigin origin = new ReaderOrigin(resource);

        origin.getInputStream().close();
        verify(resource, times(1)).close();

        origin.getReader(StandardCharsets.UTF_8).close();
        verify(resource, times(2)).close();

        origin.getChannel(ReadableByteChannel.class).close();
        verify(resource, times(3)).close();
    }

    @Override
    @Test
    void testGetFile() {
        // Cannot convert a Reader to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    void testGetOutputStream() {
        // Cannot convert a Reader to an OutputStream.
        assertThrows(UnsupportedOperationException.class, super::testGetOutputStream);
    }

    @Override
    @Test
    void testGetPath() {
        // Cannot convert a Reader to a Path.
        assertThrows(UnsupportedOperationException.class, super::testGetPath);
    }

    @Override
    @Test
    void testGetRandomAccessFile() {
        // Cannot convert a RandomAccessFile to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetRandomAccessFile);
    }

    @Override
    @ParameterizedTest
    @EnumSource(StandardOpenOption.class)
    void testGetRandomAccessFile(final OpenOption openOption) {
        // Cannot convert a RandomAccessFile to a File.
        assertThrows(UnsupportedOperationException.class, () -> super.testGetRandomAccessFile(openOption));
    }

    @Override
    @Test
    void testGetWritableByteChannel() throws IOException {
        // Cannot convert a InputStream to a WritableByteChannel.
        assertThrows(UnsupportedOperationException.class, super::testGetWritableByteChannel);
    }

    @Override
    @Test
    void testGetWriter() {
        // Cannot convert a Reader to a Writer.
        assertThrows(UnsupportedOperationException.class, super::testGetWriter);
    }

    @Override
    void testSize() throws IOException {
        // Size is unknown.
        assertThrows(UnsupportedOperationException.class, super::testSize);
    }
}
