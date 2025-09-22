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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.build.AbstractOrigin.WriterOrigin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link WriterOrigin}.
 *
 * A WriterOrigin can convert into some of the other aspects.
 *
 * @see Writer
 */
class WriterStreamOriginTest extends AbstractOriginTest<Writer, WriterOrigin> {

    @Override
    protected WriterOrigin newOriginRo() {
        return newOriginRw();
    }

    @Override
    protected WriterOrigin newOriginRw() {
        return new WriterOrigin(new StringWriter());
    }

    @Override
    @Test
    void testGetByteArray() {
        // Cannot convert a Writer to a byte[].
        assertThrows(UnsupportedOperationException.class, super::testGetByteArray);
    }

    @Override
    @Test
    void testGetByteArrayAt_0_0() {
        // Cannot convert a OutputStream to a byte[].
        assertThrows(UnsupportedOperationException.class, super::testGetByteArrayAt_0_0);
    }

    @Override
    @Test
    void testGetByteArrayAt_0_1() {
        // Cannot convert a OutputStream to a byte[].
        assertThrows(UnsupportedOperationException.class, super::testGetByteArrayAt_0_1);
    }

    @Override
    @Test
    void testGetByteArrayAt_1_1() {
        // Cannot convert a OutputStream to a byte[].
        assertThrows(UnsupportedOperationException.class, super::testGetByteArrayAt_1_1);
    }

    @Override
    @Test
    void testGetCharSequence() {
        // Cannot convert a Writer to a CharSequence.
        assertThrows(UnsupportedOperationException.class, super::testGetCharSequence);
    }

    @Override
    @Test
    void testGetFile() {
        // Cannot convert a Writer to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    void testGetPath() {
        // Cannot convert a Writer to a Path.
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
    void testGetInputStream() {
        // Cannot convert a Writer to an InputStream.
        assertThrows(UnsupportedOperationException.class, super::testGetInputStream);
    }

    @Override
    @Test
    void testGetReader() {
        // Cannot convert a Writer to a Reader.
        assertThrows(UnsupportedOperationException.class, super::testGetReader);
    }

    @Override
    void testGetReadableByteChannel() throws IOException {
        // Cannot convert a Writer to a ReadableByteChannel.
        assertThrows(UnsupportedOperationException.class, super::testGetReadableByteChannel);
    }

    @Override
    @Test
    void testSize() {
        // Cannot convert a Writer to a size.
        assertThrows(UnsupportedOperationException.class, super::testSize);
    }

}
