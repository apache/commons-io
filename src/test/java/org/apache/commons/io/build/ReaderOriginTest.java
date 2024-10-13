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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
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
public class ReaderOriginTest extends AbstractOriginTest<Reader, ReaderOrigin> {

    @Override
    protected ReaderOrigin newOriginRo() throws FileNotFoundException {
        return new ReaderOrigin(new FileReader(FILE_NAME_RO));
    }

    @Override
    protected ReaderOrigin newOriginRw() {
        return new ReaderOrigin(new CharSequenceReader("World"));
    }

    @Override
    @Test
    public void testGetFile() {
        // Cannot convert a Reader to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    public void testGetOutputStream() {
        // Cannot convert a Reader to an OutputStream.
        assertThrows(UnsupportedOperationException.class, super::testGetOutputStream);
    }

    @Override
    @Test
    public void testGetPath() {
        // Cannot convert a Reader to a Path.
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
    public void testGetWriter() {
        // Cannot convert a Reader to a Writer.
        assertThrows(UnsupportedOperationException.class, super::testGetWriter);
    }

}
