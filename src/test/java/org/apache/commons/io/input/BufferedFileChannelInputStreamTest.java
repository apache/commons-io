/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests functionality of {@link BufferedFileChannelInputStream}.
 *
 * This class was ported and adapted from Apache Spark commit 933dc6cb7b3de1d8ccaf73d124d6eb95b947ed19 where it was called
 * {@code BufferedFileChannelInputStreamSuite}.
 */
public class BufferedFileChannelInputStreamTest extends AbstractInputStreamTest {

    @SuppressWarnings("resource")
    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        // @formatter:off
        inputStreams = new InputStream[] {
            new BufferedFileChannelInputStream(inputFile), // default
            new BufferedFileChannelInputStream(inputFile, 123), // small, unaligned buffer size
            BufferedFileChannelInputStream.builder().setPath(inputFile).get(), // default
            BufferedFileChannelInputStream.builder().setPath(inputFile).setBufferSize(123).get(), // small, unaligned buffer size
            BufferedFileChannelInputStream.builder().setURI(inputFile.toUri()).setBufferSize(1024).get(), // URI and buffer size
            BufferedFileChannelInputStream.builder().setPath(inputFile).setOpenOptions(StandardOpenOption.READ).get(), // open options
        };
        //@formatter:on
    }

    @Test
    public void testAvailableAfterClose() throws Exception {
        for (final InputStream inputStream : inputStreams) {
            inputStream.close();
            assertEquals(0, inputStream.available());
        }
    }

    @Test
    public void testAvailableAfterOpen() throws Exception {
        for (final InputStream inputStream : inputStreams) {
            assertTrue(inputStream.available() > 0);
        }
    }

    @Test
    public void testAvailableAfterRead() throws Exception {
        for (final InputStream inputStream : inputStreams) {
            assertNotEquals(IOUtils.EOF, inputStream.read());
            assertTrue(inputStream.available() > 0);
        }
    }

    @Test
    public void testBuilderGet() {
        // java.lang.IllegalStateException: origin == null
        assertThrows(IllegalStateException.class, () -> BufferedFileChannelInputStream.builder().get());
    }

    @Test
    public void testReadAfterClose() throws Exception {
        for (final InputStream inputStream : inputStreams) {
            inputStream.close();
            assertEquals(IOUtils.EOF, inputStream.read());
        }
    }


}
