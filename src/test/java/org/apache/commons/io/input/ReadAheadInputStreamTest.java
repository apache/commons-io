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

import org.junit.jupiter.api.BeforeEach;

/**
 * Tests {@link ReadAheadInputStream}.
 *
 * This class was ported and adapted from Apache Spark commit 933dc6cb7b3de1d8ccaf73d124d6eb95b947ed19 where it was
 * called {@code ReadAheadInputStreamSuite}.
 */
public class ReadAheadInputStreamTest extends AbstractInputStreamTest {

    @SuppressWarnings("resource")
    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        inputStreams = new InputStream[] {
            // Tests equal and aligned buffers of wrapped an outer stream.
            new ReadAheadInputStream(new BufferedFileChannelInputStream(inputFile, 8 * 1024), 8 * 1024),
            // Tests aligned buffers, wrapped bigger than outer.
            new ReadAheadInputStream(new BufferedFileChannelInputStream(inputFile, 3 * 1024), 2 * 1024),
            // Tests aligned buffers, wrapped smaller than outer.
            new ReadAheadInputStream(new BufferedFileChannelInputStream(inputFile, 2 * 1024), 3 * 1024),
            // Tests unaligned buffers, wrapped bigger than outer.
            new ReadAheadInputStream(new BufferedFileChannelInputStream(inputFile, 321), 123),
            // Tests unaligned buffers, wrapped smaller than outer.
            new ReadAheadInputStream(new BufferedFileChannelInputStream(inputFile, 123), 321)};
    }
}
