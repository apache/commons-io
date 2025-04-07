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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.BeforeEach;

/**
 * Tests {@link ReadAheadInputStream}.
 *
 * This class was ported and adapted from Apache Spark commit 933dc6cb7b3de1d8ccaf73d124d6eb95b947ed19 where it was called {@code ReadAheadInputStreamSuite}.
 */
public class ReadAheadInputStreamTest extends AbstractInputStreamTest {

    @SuppressWarnings("resource")
    @BeforeEach
    public void setUpInputStreams() throws IOException {
        inputStreams = new InputStream[] {
                // Tests equal and aligned buffers of wrapped an outer stream.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 8 * 1024), 8 * 1024),
                // Tests aligned buffers, wrapped bigger than outer.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 3 * 1024), 2 * 1024),
                // Tests aligned buffers, wrapped smaller than outer.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 2 * 1024), 3 * 1024),
                // Tests unaligned buffers, wrapped bigger than outer.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 321), 123),
                // Tests unaligned buffers, wrapped smaller than outer.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 123), 321),
                //
                // Tests equal and aligned buffers of wrapped an outer stream.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 8 * 1024)).setBufferSize(8 * 1024).get(),
                // Tests aligned buffers, wrapped bigger than outer.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 3 * 1024)).setBufferSize(2 * 1024).get(),
                // Tests aligned buffers, wrapped smaller than outer.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 2 * 1024)).setBufferSize(3 * 1024).get(),
                // Tests unaligned buffers, wrapped bigger than outer.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 321)).setBufferSize(123).get(),
                // Tests unaligned buffers, wrapped smaller than outer.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 123)).setBufferSize(321).get(),
                ReadAheadInputStream.builder().setPath(InputPath).setOpenOptions(StandardOpenOption.READ).get() };
    }

}
