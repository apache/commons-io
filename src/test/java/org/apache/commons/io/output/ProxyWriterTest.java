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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.junit.Test;

/**
 * Test {@link ProxyWriter}.
 *
 * @version $Id$
 */
public class ProxyWriterTest {

    @Test
    public void appendCharSequence() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.append("ABC");
        } catch (final Exception e) {
            fail("Appending CharSequence threw " + e);
        }
        assertEquals("ABC", writer.toString());
        proxy.close();
    }

    @Test
    public void appendCharSequence_with_offset() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        proxy.append("ABC", 1, 3);
        proxy.flush();
        assertEquals("BC", writer.toString());
        proxy.close();
    }

    @Test
    public void appendChar() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        proxy.append('c');
        assertEquals("c", writer.toString());
        proxy.close();
    }

    @Test
    public void writeString() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write("ABC");
        } catch (final Exception e) {
            fail("Writing String threw " + e);
        }
        assertEquals("ABC", writer.toString());
        proxy.close();
    }

    @Test
    public void writeStringPartial() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write("ABC", 1, 2);
        } catch (final Exception e) {
            fail("Writing String threw " + e);
        }
        assertEquals("BC", writer.toString());
        proxy.close();
    }

    @Test
    public void writeCharArray() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write(new char[]{'A', 'B', 'C'});
        } catch (final Exception e) {
            fail("Writing char[] threw " + e);
        }
        assertEquals("ABC", writer.toString());
        proxy.close();
    }

    @Test
    public void writeInt() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write(65);
        } catch (final Exception e) {
            fail("Writing char[] threw " + e);
        }
        assertEquals("A", writer.toString());
        proxy.close();
    }

    @Test
    public void writeCharArrayPartial() throws Exception {
        final StringBuilderWriter writer = new StringBuilderWriter();
        final ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write(new char[]{'A', 'B', 'C'}, 1, 2);
        } catch (final Exception e) {
            fail("Writing char[] threw " + e);
        }
        assertEquals("BC", writer.toString());
        proxy.close();
    }

    @Test
    public void nullString() throws Exception {

        final ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.write((String) null);
        } catch (final Exception e) {
            fail("Writing null String threw " + e);
        }

        try {
            proxy.write((String) null, 0, 0);
        } catch (final Exception e) {
            fail("Writing null String threw " + e);
        }
        proxy.close();
    }

    @Test
    public void nullCharArray() throws Exception {

        final ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.write((char[]) null);
        } catch (final Exception e) {
            fail("Writing null char[] threw " + e);
        }

        try {
            proxy.write((char[]) null, 0, 0);
        } catch (final Exception e) {
            fail("Writing null char[] threw " + e);
        }
        proxy.close();
    }

    @Test
    public void nullCharSequencec() throws Exception {

        final ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.append(null);
        } catch (final Exception e) {
            fail("Appending null CharSequence threw " + e);
        }
        proxy.close();
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_append_char() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(int c) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.append('c');
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_append_charSequence() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public Writer append(CharSequence csq) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.append("ABCE");
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_append_charSequence_offset() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public Writer append(CharSequence csq, int start, int end) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.append("ABCE", 1, 2);
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_write_int() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(int c) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.write((int) 'a');
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_write_char_array() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(char[] cbuf) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.write("ABCE".toCharArray());
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_write_offset_char_array() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.write("ABCE".toCharArray(), 2, 3);
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_write_string() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(String str) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.write("ABCE");
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_write_string_offset() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(String str, int off, int len) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.write("ABCE", 1, 3);
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_flush() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void flush() throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        try {
            proxy.flush();
        } finally {
            proxy.close();
        }
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void exceptions_in_close() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        };
        final ProxyWriter proxy = new ProxyWriter(osw);
        proxy.close();
    }
}
