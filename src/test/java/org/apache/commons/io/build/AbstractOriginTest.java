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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.jupiter.api.Test;

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

    protected AbstractOrigin<T, B> originRo;
    protected AbstractOrigin<T, B> originRw;

    protected AbstractOrigin<T, B> getOriginRo() {
        return Objects.requireNonNull(originRo, "originRo");
    }

    protected AbstractOrigin<T, B> getOriginRw() {
        return Objects.requireNonNull(originRw, "originRw");
    }

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
    public void testGetFile() {
        assertNotNull(getOriginRo().getFile());
    }

    @Test
    public void testGetInputStream() throws IOException {
        try (final InputStream inputStream = getOriginRo().getInputStream()) {
            assertNotNull(inputStream);
        }
    }

    @Test
    public void testGetOutputStream() throws IOException {
        try (final OutputStream output = getOriginRw().getOutputStream()) {
            assertNotNull(output);
        }
    }

    @Test
    public void testGetPath() {
        assertNotNull(getOriginRo().getPath());
    }

    @Test
    public void testGetReader() throws IOException {
        try (final Reader reader = getOriginRo().getReader(Charset.defaultCharset())) {
            assertNotNull(reader);
        }
    }

    @Test
    public void testGetWriter() throws IOException {
        try (final Writer writer = getOriginRw().getWriter(Charset.defaultCharset())) {
            assertNotNull(writer);
        }
    }

    @Test
    public void testSize() throws IOException {
        assertEquals(Files.size(Paths.get(FILE_NAME_RO)), getOriginRo().getByteArray().length);
    }

}
