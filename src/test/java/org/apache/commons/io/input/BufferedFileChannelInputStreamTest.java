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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

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
    @BeforeEach
    public void setUpInputStreams() throws IOException {
        // @formatter:off
        inputStreams = new InputStream[] {
            new BufferedFileChannelInputStream(InputPath), // default
            new BufferedFileChannelInputStream(InputPath, 123), // small, unaligned buffer size
            BufferedFileChannelInputStream.builder().setPath(InputPath).get(), // default
            BufferedFileChannelInputStream.builder().setPath(InputPath).setBufferSize(123).get(), // small, unaligned buffer size
            BufferedFileChannelInputStream.builder().setURI(InputPath.toUri()).setBufferSize(1024).get(), // URI and buffer size
            BufferedFileChannelInputStream.builder().setPath(InputPath).setOpenOptions(StandardOpenOption.READ).get(), // open options
            BufferedFileChannelInputStream.builder().setFileChannel(FileChannel.open(InputPath)).get(), // FileChannel
        };
        //@formatter:on
    }

    @Override
    @Test
    public void testAvailableAfterOpen() throws Exception {
        for (final InputStream inputStream : inputStreams) {
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
            assertThrows(IOException.class, inputStream::read);
        }
    }

}
