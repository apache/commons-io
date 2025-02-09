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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.junit.jupiter.api.Test;

/**
 * Test {@link ProxyWriter}.
 */
public class ProxyWriterTest {

    @Test
    public void testAppendChar() throws Exception {
        try (StringBuilderWriter writer = new StringBuilderWriter();
                ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.append('c');
            assertEquals("c", writer.toString());
        }
    }

    @Test
    public void testAppendCharSequence() throws Exception {
        try (StringBuilderWriter writer = new StringBuilderWriter();
                ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.append("ABC");
            assertEquals("ABC", writer.toString());
        }
    }

    @Test
    public void testAppendCharSequence_with_offset() throws Exception {
        try (StringBuilderWriter writer = new StringBuilderWriter();
                ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.append("ABC", 1, 3);
            proxy.flush();
            assertEquals("BC", writer.toString());
        }
    }

    @Test
    public void testExceptions_in_append_char() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(baos) {
                    @Override
                    public void write(final int c) throws IOException {
                        throw new UnsupportedEncodingException("Bah");
                    }
                }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.append('c'));
            }
        }
    }

    @Test
    public void testExceptions_in_append_charSequence() throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public Writer append(final CharSequence csq) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.append("ABCE"));
            }
        }
    }

    @Test
    public void testExceptions_in_append_charSequence_offset() throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.append("ABCE", 1, 2));
            }
        }
    }

    @Test
    public void testExceptions_in_close() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
                @Override
                public void close() throws IOException {
                    throw new UnsupportedEncodingException("Bah");
                }
            }) {
                try (ProxyWriter proxy = new ProxyWriter(osw)) {
                    // noop
                }
            }
        });
    }

    @Test
    public void testExceptions_in_flush() throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void flush() throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, proxy::flush);
            }
        }
    }

    @Test
    public void testExceptions_in_write_char_array() throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final char[] cbuf) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write("ABCE".toCharArray()));
            }
        }
    }

    @Test
    public void testExceptions_in_write_int() throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final int c) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write('a'));
            }
        }
    }

    @Test
    public void testExceptions_in_write_offset_char_array() throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final char[] cbuf, final int off, final int len) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write("ABCE".toCharArray(), 2, 3));
            }
        }
    }

    @Test
    public void testExceptions_in_write_string() throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final String str) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write("ABCE"));
            }
        }
    }

    @Test
    public void testExceptions_in_write_string_offset() throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final String str, final int off, final int len) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write("ABCE", 1, 3));
            }
        }
    }

    @Test
    public void testNullCharArray() throws Exception {
        try (ProxyWriter proxy = new ProxyWriter(NullWriter.INSTANCE)) {
            proxy.write((char[]) null);
            proxy.write((char[]) null, 0, 0);
        }
    }

    @Test
    public void testNullCharSequence() throws Exception {
        try (ProxyWriter proxy = new ProxyWriter(NullWriter.INSTANCE)) {
            proxy.append(null);
        }
    }

    @Test
    public void testNullString() throws Exception {
        try (ProxyWriter proxy = new ProxyWriter(NullWriter.INSTANCE)) {
            proxy.write((String) null);
            proxy.write((String) null, 0, 0);
        }
    }

    @Test
    public void testWriteCharArray() throws Exception {
        try (StringBuilderWriter writer = new StringBuilderWriter();
                ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write(new char[] { 'A', 'B', 'C' });
            assertEquals("ABC", writer.toString());
        }
    }

    @Test
    public void testWriteCharArrayPartial() throws Exception {
        try (StringBuilderWriter writer = new StringBuilderWriter();
                ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write(new char[] { 'A', 'B', 'C' }, 1, 2);
            assertEquals("BC", writer.toString());
        }
    }

    @Test
    public void testWriteInt() throws Exception {
        try (StringBuilderWriter writer = new StringBuilderWriter();
                ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write(65);
            assertEquals("A", writer.toString());
        }
    }

    @Test
    public void testWriteString() throws Exception {
        try (StringBuilderWriter writer = new StringBuilderWriter();
                ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write("ABC");
            assertEquals("ABC", writer.toString());
        }
    }

    @Test
    public void testWriteStringPartial() throws Exception {
        try (StringBuilderWriter writer = new StringBuilderWriter();
                ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write("ABC", 1, 2);
            assertEquals("BC", writer.toString());
        }
    }
}
