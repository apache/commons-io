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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractOrigin.CharSequenceOrigin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link CharSequenceOrigin}.
 *
 * A CharSequenceOrigin can convert into some of the other aspects.
 */
public class CharSequenceOriginTest extends AbstractOriginTest<CharSequence, CharSequenceOrigin> {

    @BeforeEach
    public void beforeEach() throws FileNotFoundException, IOException {
        setOriginRo(new CharSequenceOrigin(getFixtureStringFromFile()));
        setOriginRw(new CharSequenceOrigin("World"));
    }

    private String getFixtureStringFromFile() throws IOException {
        return IOUtils.resourceToString(FILE_RES_RO, StandardCharsets.UTF_8);
    }

    @Override
    @Test
    public void testGetFile() {
        // Cannot convert a CharSequence to a File.
        assertThrows(UnsupportedOperationException.class, super::testGetFile);
    }

    @Override
    @Test
    public void testGetOutputStream() {
        // Cannot convert a CharSequence to an OutputStream.
        assertThrows(UnsupportedOperationException.class, super::testGetOutputStream);
    }

    @Override
    @Test
    public void testGetPath() {
        // Cannot convert a CharSequence to a Path.
        assertThrows(UnsupportedOperationException.class, super::testGetPath);
    }

    @Test
    public void testGetReaderIgnoreCharset() throws IOException {
        // The CharSequenceOrigin ignores the given Charset.
        try (final Reader reader = getOriginRo().getReader(StandardCharsets.UTF_16LE)) {
            assertNotNull(reader);
            assertEquals(getFixtureStringFromFile(), IOUtils.toString(reader));
        }
    }

    @Override
    @Test
    public void testGetWriter() {
        // Cannot convert a CharSequence to a Writer.
        assertThrows(UnsupportedOperationException.class, super::testGetWriter);
    }

}
