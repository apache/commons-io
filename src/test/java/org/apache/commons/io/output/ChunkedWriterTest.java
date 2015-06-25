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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos) {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                numWrites.incrementAndGet();
                super.write(cbuf, off, len);
            }
        };

        ChunkedWriter chunked = new ChunkedWriter(osw, 10);
        chunked.write("0123456789012345678901234567891".toCharArray());
        chunked.flush();
        assertEquals(4, numWrites.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negative_chunksize_not_permitted() {
        new ChunkedWriter(new OutputStreamWriter(new ByteArrayOutputStream()), 0);
    }
}
