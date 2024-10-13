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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.OutputStream;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.build.AbstractOrigin.OutputStreamOrigin;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link OutputStreamOrigin}.
 *
 * A OutputStreamOrigin can convert into some of the other aspects.
 *
 * @see OutputStream
 */
public class OutputStreamOriginTest extends AbstractOriginTest<OutputStream, OutputStreamOrigin> {

    @Override
    protected OutputStreamOrigin newOriginRo() {
        return new OutputStreamOrigin(new ByteArrayOutputStream());
    }

    @Override
    protected OutputStreamOrigin newOriginRw() {
        return new OutputStreamOrigin(new ByteArrayOutputStream());
    }

    @Override
    @Test
    public void testGetByteArray() {
        // Cannot convert a OutputStream to a byte[].
        assertThrows(UnsupportedOperationException.class, super::testGetByteArray);
    }

    @Override
    @Test
    public void testGetByteArrayAt_0_0() {
        // Cannot convert a OutputStream to a byte[].
        assertThrows(UnsupportedOperationException.class, super::testGetByteArrayAt_0_0);
    }

    @Override
    @Test
    public void testGetByteArrayAt_0_1() {
        // Cannot convert a OutputStream to a byte[].
        assertThrows(UnsupportedOperationException.class, super::testGetByteArrayAt_0_1);
    }

    @Override
    @Test
    public void testGetByteArrayAt_1_1() {
        // Cannot convert a OutputStream to a byte[].
        assertThrows(UnsupportedOperationException.class, super::testGetByteArrayAt_1_1);
    }

    @Override
    @Test
    public void testGetCharSequence() {
        // Cannot convert a OutputStream to a CharSequence.
        assertThrows(UnsupportedOperationException.class, super::testGetCharSequence);
    }

    @Override
    @Test
    public void testGetFile() {
        // Cannot convert a OutputStream to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    public void testGetInputStream() {
        // Cannot convert a OutputStream to an InputStream.
        assertThrows(UnsupportedOperationException.class, super::testGetInputStream);
    }

    @Override
    @Test
    public void testGetPath() {
        // Cannot convert a OutputStream to a Path.
        assertThrows(UnsupportedOperationException.class, super::testGetPath);
    }

    @Override
    @Test
    public void testGetRandomAccessFile() {
        // Cannot convert a RandomAccessFile to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetRandomAccessFile);
    }

    @Override
    @ParameterizedTest
    @EnumSource(StandardOpenOption.class)
    public void testGetRandomAccessFile(final OpenOption openOption) {
        // Cannot convert a RandomAccessFile to a File.
        assertThrows(UnsupportedOperationException.class, () -> super.testGetRandomAccessFile(openOption));
    }

    @Override
    @Test
    public void testGetReader() {
        // Cannot convert a OutputStream to a Reader.
        assertThrows(UnsupportedOperationException.class, super::testGetReader);
    }

    @Override
    @Test
    public void testSize() {
        // Cannot convert a Writer to a size.
        assertThrows(UnsupportedOperationException.class, super::testSize);
    }

}
