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
package org.apache.commons.io.output;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ChunkedWriterTest {
    @Test
    public void write_four_chunks() throws Exception {
        final AtomicInteger numWrites = new AtomicInteger();
        OutputStreamWriter osw = getOutputStreamWriter(numWrites);

        ChunkedWriter chunked = new ChunkedWriter(osw, 10);
        chunked.write("0123456789012345678901234567891".toCharArray());
        chunked.flush();
        assertEquals(4, numWrites.get());
        chunked.close();
    }

    @Test
    public void write_two_chunks_default_constructor() throws Exception {
        final AtomicInteger numWrites = new AtomicInteger();
        OutputStreamWriter osw = getOutputStreamWriter(numWrites);

        ChunkedWriter chunked = new ChunkedWriter(osw);
        chunked.write(new char[1024 * 4 + 1]);
        chunked.flush();
        assertEquals(2, numWrites.get());
        chunked.close();
    }

    private OutputStreamWriter getOutputStreamWriter(final AtomicInteger numWrites) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new OutputStreamWriter(baos) {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                numWrites.incrementAndGet();
                super.write(cbuf, off, len);
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void negative_chunksize_not_permitted() throws Exception {
        (new ChunkedWriter(new OutputStreamWriter(new ByteArrayOutputStream()), 0)).close();;
    }
}
