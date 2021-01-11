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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.AppendableWriter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.io.test.ThrowOnCloseReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This is used to test IOUtils for correctness. The following checks are performed:
 * <ul>
 * <li>The return must not be null, must be the same type and equals() to the method's second arg</li>
 * <li>All bytes must have been read from the source (available() == 0)</li>
 * <li>The source and destination content must be identical (byte-wise comparison check)</li>
 * <li>The output stream must not have been closed (a byte/char is written to test this, and subsequent size
 * checked)</li>
 * </ul>
 * Due to interdependencies in IOUtils and IOUtilsTestlet, one bug may cause multiple tests to fail.
 */
@SuppressWarnings("deprecation") // deliberately testing deprecated code
public class IOUtilsTestCase {

    private static final int FILE_SIZE = 1024 * 4 + 1;

    /** Determine if this is windows. */
    private static final boolean WINDOWS = File.separatorChar == '\\';
    /*
     * Note: this is not particularly beautiful code. A better way to check for flush and close status would be to
     * implement "trojan horse" wrapper implementations of the various stream classes, which set a flag when relevant
     * methods are called. (JT)
     */

    @TempDir
    public File temporaryFolder;

    private char[] carr = null;

    private byte[] iarr = null;

    private File m_testFile;

    /** Assert that the contents of two byte arrays are the same. */
    private void assertEqualContent(final byte[] b0, final byte[] b1) {
        assertTrue(Arrays.equals(b0, b1), "Content not equal according to java.util.Arrays#equals()");
    }

    @BeforeEach
    public void setUp() {
        try {
            m_testFile = new File(temporaryFolder, "file2-test.txt");

            if (!m_testFile.getParentFile().exists()) {
                throw new IOException("Cannot create file " + m_testFile + " as the parent directory does not exist");
            }
            final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(m_testFile));
            try {
                TestUtils.generateTestData(output, FILE_SIZE);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } catch (final IOException ioe) {
            throw new RuntimeException(
                "Can't run this test because the environment could not be built: " + ioe.getMessage());
        }
        // Create and init a byte array as input data
        iarr = new byte[200];
        Arrays.fill(iarr, (byte) -1);
        for (int i = 0; i < 80; i++) {
            iarr[i] = (byte) i;
        }
        carr = new char[200];
        Arrays.fill(carr, (char) -1);
        for (int i = 0; i < 80; i++) {
            carr[i] = (char) i;
        }
    }

    @Test
    public void testAsBufferedInputStream() {
        final InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        final BufferedInputStream bis = IOUtils.buffer(is);
        assertNotSame(is, bis);
        assertSame(bis, IOUtils.buffer(bis));
    }

    @Test
    public void testAsBufferedInputStreamWithBufferSize() {
        final InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        final BufferedInputStream bis = IOUtils.buffer(is, 2048);
        assertNotSame(is, bis);
        assertSame(bis, IOUtils.buffer(bis));
        assertSame(bis, IOUtils.buffer(bis, 1024));
    }

    @Test
    public void testAsBufferedNull() {
        try {
            IOUtils.buffer((InputStream) null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException npe) {
            // expected
        }
        try {
            IOUtils.buffer((OutputStream) null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException npe) {
            // expected
        }
        try {
            IOUtils.buffer((Reader) null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException npe) {
            // expected
        }
        try {
            IOUtils.buffer((Writer) null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException npe) {
            // expected
        }
    }

    @Test
    public void testAsBufferedOutputStream() {
        final OutputStream is = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
            }
        };
        final BufferedOutputStream bis = IOUtils.buffer(is);
        assertNotSame(is, bis);
        assertSame(bis, IOUtils.buffer(bis));
    }

    @Test
    public void testAsBufferedOutputStreamWithBufferSize() {
        final OutputStream os = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
            }
        };
        final BufferedOutputStream bos = IOUtils.buffer(os, 2048);
        assertNotSame(os, bos);
        assertSame(bos, IOUtils.buffer(bos));
        assertSame(bos, IOUtils.buffer(bos, 1024));
    }

    @Test
    public void testAsBufferedReader() {
        final Reader is = new Reader() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public int read(final char[] cbuf, final int off, final int len) throws IOException {
                return 0;
            }
        };
        final BufferedReader bis = IOUtils.buffer(is);
        assertNotSame(is, bis);
        assertSame(bis, IOUtils.buffer(bis));
    }

    @Test
    public void testAsBufferedReaderWithBufferSize() {
        final Reader r = new Reader() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public int read(final char[] cbuf, final int off, final int len) throws IOException {
                return 0;
            }
        };
        final BufferedReader br = IOUtils.buffer(r, 2048);
        assertNotSame(r, br);
        assertSame(br, IOUtils.buffer(br));
        assertSame(br, IOUtils.buffer(br, 1024));
    }

    @Test
    public void testAsBufferedWriter() {
        final Writer is = new Writer() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void write(final char[] cbuf, final int off, final int len) throws IOException {
            }

            @Override
            public void write(final int b) throws IOException {
            }
        };
        final BufferedWriter bis = IOUtils.buffer(is);
        assertNotSame(is, bis);
        assertSame(bis, IOUtils.buffer(bis));
    }

    @Test
    public void testAsBufferedWriterWithBufferSize() {
        final Writer w = new Writer() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void write(final char[] cbuf, final int off, final int len) throws IOException {
            }

            @Override
            public void write(final int b) throws IOException {
            }
        };
        final BufferedWriter bw = IOUtils.buffer(w, 2024);
        assertNotSame(w, bw);
        assertSame(bw, IOUtils.buffer(bw));
        assertSame(bw, IOUtils.buffer(bw, 1024));
    }

    @Test
    public void testAsWriterAppendable() {
        final Appendable a = new StringBuffer();
        final Writer w = IOUtils.writer(a);
        assertNotSame(w, a);
        assertEquals(AppendableWriter.class, w.getClass());
        assertSame(w, IOUtils.writer(w));
    }

    @Test
    public void testAsWriterNull() {
        assertThrows(NullPointerException.class, () -> IOUtils.writer(null));
    }

    @Test
    public void testAsWriterStringBuilder() {
        final Appendable a = new StringBuilder();
        final Writer w = IOUtils.writer(a);
        assertNotSame(w, a);
        assertEquals(StringBuilderWriter.class, w.getClass());
        assertSame(w, IOUtils.writer(w));
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> IOUtils.close((Closeable) null));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s")));
        assertThrows(IOException.class, () -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s"))));
    }

    @Test
    public void testCloseConsumer() {
        final Closeable nulCloseable = null;
        assertDoesNotThrow(() -> IOUtils.close(nulCloseable, null)); // null consumer
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), null)); // null consumer
        assertDoesNotThrow(() -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), null)); // null consumer

        final IOConsumer<IOException> nullConsumer = null; // null consumer doesn't throw
        assertDoesNotThrow(() -> IOUtils.close(nulCloseable, nullConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), nullConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), nullConsumer));

        final IOConsumer<IOException> silentConsumer = i -> {}; // silent consumer doesn't throw
        assertDoesNotThrow(() -> IOUtils.close(nulCloseable, silentConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), silentConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), silentConsumer));

        final IOConsumer<IOException> noisyConsumer = i -> {
            throw i;
        }; // consumer passes on the throw
        assertDoesNotThrow(() -> IOUtils.close(nulCloseable, noisyConsumer)); // no throw
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), noisyConsumer)); // no throw
        assertThrows(IOException.class,
            () -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), noisyConsumer)); // closeable throws
    }

    @Test
    public void testCloseMulti() {
        final Closeable nulCloseable = null;
        final Closeable[] closeables = {null, null};
        assertDoesNotThrow(() -> IOUtils.close(nulCloseable, nulCloseable));
        assertDoesNotThrow(() -> IOUtils.close(closeables));
        assertDoesNotThrow(() -> IOUtils.close((Closeable[]) null));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), nulCloseable));
        assertThrows(IOException.class,
            () -> IOUtils.close(nulCloseable, new ThrowOnCloseReader(new StringReader("s"))));
    }

    @Test
    public void testCloseQuietly_AllCloseableIOException() {
        final Closeable closeable = () -> {
            throw new IOException();
        };
        assertDoesNotThrow(() -> IOUtils.closeQuietly(closeable, null, closeable));
    }

    @Test
    public void testCloseQuietly_CloseableIOException() {
        assertDoesNotThrow(() -> {
            IOUtils.closeQuietly((Closeable) () -> {
                throw new IOException();
            });
        });
    }

    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    @Test
    public void testCloseQuietly_Selector() {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (final IOException ignore) {
        } finally {
            IOUtils.closeQuietly(selector);
        }
    }

    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    @Test
    public void testCloseQuietly_SelectorIOException() {
        final Selector selector = new SelectorAdapter() {
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };
        IOUtils.closeQuietly(selector);
    }

    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    @Test
    public void testCloseQuietly_SelectorNull() {
        final Selector selector = null;
        IOUtils.closeQuietly(selector);
    }

    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    @Test
    public void testCloseQuietly_SelectorTwice() {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (final IOException ignore) {
        } finally {
            IOUtils.closeQuietly(selector);
            IOUtils.closeQuietly(selector);
        }
    }

    @Test
    public void testCloseQuietly_ServerSocket() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((ServerSocket) null));
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new ServerSocket()));
    }

    @Test
    public void testCloseQuietly_ServerSocketIOException() {
        assertDoesNotThrow(() -> {
            IOUtils.closeQuietly(new ServerSocket() {
                @Override
                public void close() throws IOException {
                    throw new IOException();
                }
            });
        });
    }

    @Test
    public void testCloseQuietly_Socket() {
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Socket) null));
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new Socket()));
    }

    @Test
    public void testCloseQuietly_SocketIOException() {
        assertDoesNotThrow(() -> {
            IOUtils.closeQuietly(new Socket() {
                @Override
                public synchronized void close() throws IOException {
                    throw new IOException();
                }
            });
        });
    }

    @Test
    public void testConstants() throws Exception {
        assertEquals('/', IOUtils.DIR_SEPARATOR_UNIX);
        assertEquals('\\', IOUtils.DIR_SEPARATOR_WINDOWS);
        assertEquals("\n", IOUtils.LINE_SEPARATOR_UNIX);
        assertEquals("\r\n", IOUtils.LINE_SEPARATOR_WINDOWS);
        if (WINDOWS) {
            assertEquals('\\', IOUtils.DIR_SEPARATOR);
            assertEquals("\r\n", IOUtils.LINE_SEPARATOR);
        } else {
            assertEquals('/', IOUtils.DIR_SEPARATOR);
            assertEquals("\n", IOUtils.LINE_SEPARATOR);
        }
        assertEquals('\r', IOUtils.CR);
        assertEquals('\n', IOUtils.LF);
        assertEquals(-1, IOUtils.EOF);
    }

    @Test
    public void testConsume() throws Exception {
        final long size = (long) Integer.MAX_VALUE + (long) 1;
        final InputStream in = new NullInputStream(size);
        final OutputStream out = NullOutputStream.NULL_OUTPUT_STREAM;

        // Test copy() method
        assertEquals(-1, IOUtils.copy(in, out));

        // reset the input
        in.close();

        // Test consume() method
        assertEquals(size, IOUtils.consume(in), "consume()");
    }

    @Test
    public void testContentEquals_InputStream_InputStream() throws Exception {
        {
            assertTrue(IOUtils.contentEquals((InputStream) null, null));
        }
        {
            final ByteArrayInputStream input1 = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            assertFalse(IOUtils.contentEquals(input1, null));
        }
        {
            final ByteArrayInputStream input1 = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            assertFalse(IOUtils.contentEquals(null, input1));
        }
        {
            final ByteArrayInputStream input1 = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            assertTrue(IOUtils.contentEquals(input1, input1));
        }
        {
            final ByteArrayInputStream input1 = new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8));
            assertTrue(IOUtils.contentEquals(input1, input1));
        }
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))));
        assertTrue(IOUtils.contentEquals(
            new BufferedInputStream(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))),
            new BufferedInputStream(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)))));
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8)),
            new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8))));
        assertFalse(IOUtils.contentEquals(new ByteArrayInputStream("ABCD".getBytes(StandardCharsets.UTF_8)),
            new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8))));
        assertFalse(IOUtils.contentEquals(new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8)),
            new ByteArrayInputStream("ABCD".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testContentEquals_Reader_Reader() throws Exception {
        {
            assertTrue(IOUtils.contentEquals((Reader) null, null));
        }
        {
            final StringReader input1 = new StringReader("");
            assertFalse(IOUtils.contentEquals(null, input1));
        }
        {
            final StringReader input1 = new StringReader("");
            assertFalse(IOUtils.contentEquals(input1, null));
        }
        {
            final StringReader input1 = new StringReader("");
            assertTrue(IOUtils.contentEquals(input1, input1));
        }
        {
            final StringReader input1 = new StringReader("ABC");
            assertTrue(IOUtils.contentEquals(input1, input1));
        }
        assertTrue(IOUtils.contentEquals(new StringReader(""), new StringReader("")));
        assertTrue(
            IOUtils.contentEquals(new BufferedReader(new StringReader("")), new BufferedReader(new StringReader(""))));
        assertTrue(IOUtils.contentEquals(new StringReader("ABC"), new StringReader("ABC")));
        assertFalse(IOUtils.contentEquals(new StringReader("ABCD"), new StringReader("ABC")));
        assertFalse(IOUtils.contentEquals(new StringReader("ABC"), new StringReader("ABCD")));
    }

    @Test
    public void testContentEqualsIgnoreEOL() throws Exception {
        {
            assertTrue(IOUtils.contentEqualsIgnoreEOL((Reader) null, null));
        }
        {
            final Reader input1 = new CharArrayReader("".toCharArray());
            assertFalse(IOUtils.contentEqualsIgnoreEOL(null, input1));
        }
        {
            final Reader input1 = new CharArrayReader("".toCharArray());
            assertFalse(IOUtils.contentEqualsIgnoreEOL(input1, null));
        }
        {
            final Reader input1 = new CharArrayReader("".toCharArray());
            assertTrue(IOUtils.contentEqualsIgnoreEOL(input1, input1));
        }
        {
            final Reader input1 = new CharArrayReader("321\r\n".toCharArray());
            assertTrue(IOUtils.contentEqualsIgnoreEOL(input1, input1));
        }

        Reader r1;
        Reader r2;

        r1 = new CharArrayReader("".toCharArray());
        r2 = new CharArrayReader("".toCharArray());
        assertTrue(IOUtils.contentEqualsIgnoreEOL(r1, r2));

        r1 = new CharArrayReader("1".toCharArray());
        r2 = new CharArrayReader("1".toCharArray());
        assertTrue(IOUtils.contentEqualsIgnoreEOL(r1, r2));

        r1 = new CharArrayReader("1".toCharArray());
        r2 = new CharArrayReader("2".toCharArray());
        assertFalse(IOUtils.contentEqualsIgnoreEOL(r1, r2));

        r1 = new CharArrayReader("123\rabc".toCharArray());
        r2 = new CharArrayReader("123\nabc".toCharArray());
        assertTrue(IOUtils.contentEqualsIgnoreEOL(r1, r2));

        r1 = new CharArrayReader("321".toCharArray());
        r2 = new CharArrayReader("321\r\n".toCharArray());
        assertTrue(IOUtils.contentEqualsIgnoreEOL(r1, r2));
    }

    @Test
    public void testCopy_ByteArray_OutputStream() throws Exception {
        final File destination = TestUtils.newFile(temporaryFolder, "copy8.txt");
        byte[] in;
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            in = IOUtils.toByteArray(fin);
        }

        try (FileOutputStream fout = new FileOutputStream(destination)) {
            CopyUtils.copy(in, fout);

            fout.flush();

            TestUtils.checkFile(destination, m_testFile);
            TestUtils.checkWrite(fout);
        }
        TestUtils.deleteFile(destination);
    }

    @Test
    public void testCopy_ByteArray_Writer() throws Exception {
        final File destination = TestUtils.newFile(temporaryFolder, "copy7.txt");
        byte[] in;
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            in = IOUtils.toByteArray(fin);
        }

        try (FileWriter fout = new FileWriter(destination)) {
            CopyUtils.copy(in, fout);
            fout.flush();
            TestUtils.checkFile(destination, m_testFile);
            TestUtils.checkWrite(fout);
        }
        TestUtils.deleteFile(destination);
    }

    @Test
    public void testCopy_String_Writer() throws Exception {
        final File destination = TestUtils.newFile(temporaryFolder, "copy6.txt");
        String str;
        try (FileReader fin = new FileReader(m_testFile)) {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            str = IOUtils.toString(fin);
        }

        try (FileWriter fout = new FileWriter(destination)) {
            CopyUtils.copy(str, fout);
            fout.flush();

            TestUtils.checkFile(destination, m_testFile);
            TestUtils.checkWrite(fout);
        }
        TestUtils.deleteFile(destination);
    }

    @Test
    public void testCopyLarge_CharExtraLength() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader(carr);
            os = new CharArrayWriter();

            // Test our copy method
            // for extra length, it reads till EOF
            assertEquals(200, IOUtils.copyLarge(is, os, 0, 2000));
            final char[] oarr = os.toCharArray();

            // check that output length is correct
            assertEquals(200, oarr.length);
            // check that output data corresponds to input data
            assertEquals(1, oarr[1]);
            assertEquals(79, oarr[79]);
            assertEquals((char) -1, oarr[80]);

        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    @Test
    public void testCopyLarge_CharFullLength() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader(carr);
            os = new CharArrayWriter();

            // Test our copy method
            assertEquals(200, IOUtils.copyLarge(is, os, 0, -1));
            final char[] oarr = os.toCharArray();

            // check that output length is correct
            assertEquals(200, oarr.length);
            // check that output data corresponds to input data
            assertEquals(1, oarr[1]);
            assertEquals(79, oarr[79]);
            assertEquals((char) -1, oarr[80]);

        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    @Test
    public void testCopyLarge_CharNoSkip() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader(carr);
            os = new CharArrayWriter();

            // Test our copy method
            assertEquals(100, IOUtils.copyLarge(is, os, 0, 100));
            final char[] oarr = os.toCharArray();

            // check that output length is correct
            assertEquals(100, oarr.length);
            // check that output data corresponds to input data
            assertEquals(1, oarr[1]);
            assertEquals(79, oarr[79]);
            assertEquals((char) -1, oarr[80]);

        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    @Test
    public void testCopyLarge_CharSkip() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader(carr);
            os = new CharArrayWriter();

            // Test our copy method
            assertEquals(100, IOUtils.copyLarge(is, os, 10, 100));
            final char[] oarr = os.toCharArray();

            // check that output length is correct
            assertEquals(100, oarr.length);
            // check that output data corresponds to input data
            assertEquals(11, oarr[1]);
            assertEquals(79, oarr[69]);
            assertEquals((char) -1, oarr[70]);

        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    @Test
    public void testCopyLarge_CharSkipInvalid() throws IOException {
        try (CharArrayReader is = new CharArrayReader(carr); CharArrayWriter os = new CharArrayWriter()) {
            assertThrows(EOFException.class, () -> IOUtils.copyLarge(is, os, 1000, 100));
        }
    }

    @Test
    public void testCopyLarge_ExtraLength() throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(iarr);
            ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // Create streams

            // Test our copy method
            // for extra length, it reads till EOF
            assertEquals(200, IOUtils.copyLarge(is, os, 0, 2000));
            final byte[] oarr = os.toByteArray();

            // check that output length is correct
            assertEquals(200, oarr.length);
            // check that output data corresponds to input data
            assertEquals(1, oarr[1]);
            assertEquals(79, oarr[79]);
            assertEquals(-1, oarr[80]);
        }
    }

    @Test
    public void testCopyLarge_FullLength() throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(iarr);
            ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // Test our copy method
            assertEquals(200, IOUtils.copyLarge(is, os, 0, -1));
            final byte[] oarr = os.toByteArray();

            // check that output length is correct
            assertEquals(200, oarr.length);
            // check that output data corresponds to input data
            assertEquals(1, oarr[1]);
            assertEquals(79, oarr[79]);
            assertEquals(-1, oarr[80]);
        }
    }

    @Test
    public void testCopyLarge_NoSkip() throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(iarr);
            ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // Test our copy method
            assertEquals(100, IOUtils.copyLarge(is, os, 0, 100));
            final byte[] oarr = os.toByteArray();

            // check that output length is correct
            assertEquals(100, oarr.length);
            // check that output data corresponds to input data
            assertEquals(1, oarr[1]);
            assertEquals(79, oarr[79]);
            assertEquals(-1, oarr[80]);
        }
    }

    @Test
    public void testCopyLarge_Skip() throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(iarr);
            ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // Test our copy method
            assertEquals(100, IOUtils.copyLarge(is, os, 10, 100));
            final byte[] oarr = os.toByteArray();

            // check that output length is correct
            assertEquals(100, oarr.length);
            // check that output data corresponds to input data
            assertEquals(11, oarr[1]);
            assertEquals(79, oarr[69]);
            assertEquals(-1, oarr[70]);
        }
    }

    @Test
    public void testCopyLarge_SkipInvalid() throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(iarr);
            ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // Test our copy method
            assertThrows(EOFException.class, () -> IOUtils.copyLarge(is, os, 1000, 100));
        }
    }

    @Test
    public void testCopyLarge_SkipWithInvalidOffset() throws IOException {
        ByteArrayInputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            // Create streams
            is = new ByteArrayInputStream(iarr);
            os = new ByteArrayOutputStream();

            // Test our copy method
            assertEquals(100, IOUtils.copyLarge(is, os, -10, 100));
            final byte[] oarr = os.toByteArray();

            // check that output length is correct
            assertEquals(100, oarr.length);
            // check that output data corresponds to input data
            assertEquals(1, oarr[1]);
            assertEquals(79, oarr[79]);
            assertEquals(-1, oarr[80]);

        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    @Test
    public void testRead_ReadableByteChannel() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocate(FILE_SIZE);
        final FileInputStream fileInputStream = new FileInputStream(m_testFile);
        final FileChannel input = fileInputStream.getChannel();
        try {
            assertEquals(FILE_SIZE, IOUtils.read(input, buffer));
            assertEquals(0, IOUtils.read(input, buffer));
            assertEquals(0, buffer.remaining());
            assertEquals(0, input.read(buffer));
            buffer.clear();
            try {
                IOUtils.readFully(input, buffer);
                fail("Should have failed with EOFxception");
            } catch (final EOFException expected) {
                // expected
            }
        } finally {
            IOUtils.closeQuietly(input, fileInputStream);
        }
    }

    @Test
    public void testReadFully_InputStream__ReturnByteArray() throws Exception {
        final byte[] bytes = "abcd1234".getBytes(StandardCharsets.UTF_8);
        final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        final byte[] result = IOUtils.readFully(stream, bytes.length);

        IOUtils.closeQuietly(stream);

        assertEqualContent(result, bytes);
    }

    @Test
    public void testReadFully_InputStream_ByteArray() throws Exception {
        final int size = 1027;

        final byte[] buffer = new byte[size];

        final InputStream input = new ByteArrayInputStream(new byte[size]);
        try {
            IOUtils.readFully(input, buffer, 0, -1);
            fail("Should have failed with IllegalArgumentException");
        } catch (final IllegalArgumentException expected) {
            // expected
        }
        IOUtils.readFully(input, buffer, 0, 0);
        IOUtils.readFully(input, buffer, 0, size - 1);
        try {
            IOUtils.readFully(input, buffer, 0, 2);
            fail("Should have failed with EOFxception");
        } catch (final EOFException expected) {
            // expected
        }
        IOUtils.closeQuietly(input);

    }

    @Test
    public void testReadFully_InputStream_Offset() throws Exception {
        final byte[] bytes = "abcd1234".getBytes(StandardCharsets.UTF_8);
        final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        final byte[] buffer = "wx00000000".getBytes(StandardCharsets.UTF_8);
        IOUtils.readFully(stream, buffer, 2, 8);
        assertEquals("wxabcd1234", new String(buffer, 0, buffer.length, StandardCharsets.UTF_8));
        IOUtils.closeQuietly(stream);
    }

    @Test
    public void testReadFully_ReadableByteChannel() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocate(FILE_SIZE);
        final FileInputStream fileInputStream = new FileInputStream(m_testFile);
        final FileChannel input = fileInputStream.getChannel();
        try {
            IOUtils.readFully(input, buffer);
            assertEquals(FILE_SIZE, buffer.position());
            assertEquals(0, buffer.remaining());
            assertEquals(0, input.read(buffer));
            IOUtils.readFully(input, buffer);
            assertEquals(FILE_SIZE, buffer.position());
            assertEquals(0, buffer.remaining());
            assertEquals(0, input.read(buffer));
            IOUtils.readFully(input, buffer);
            buffer.clear();
            try {
                IOUtils.readFully(input, buffer);
                fail("Should have failed with EOFxception");
            } catch (final EOFException expected) {
                // expected
            }
        } finally {
            IOUtils.closeQuietly(input, fileInputStream);
        }
    }

    @Test
    public void testReadFully_Reader() throws Exception {
        final int size = 1027;

        final char[] buffer = new char[size];

        final Reader input = new CharArrayReader(new char[size]);
        IOUtils.readFully(input, buffer, 0, 0);
        IOUtils.readFully(input, buffer, 0, size - 3);
        try {
            IOUtils.readFully(input, buffer, 0, -1);
            fail("Should have failed with IllegalArgumentException");
        } catch (final IllegalArgumentException expected) {
            // expected
        }
        try {
            IOUtils.readFully(input, buffer, 0, 5);
            fail("Should have failed with EOFException");
        } catch (final EOFException expected) {
            // expected
        }
        IOUtils.closeQuietly(input);
    }

    @Test
    public void testReadFully_Reader_Offset() throws Exception {
        final Reader reader = new StringReader("abcd1234");
        final char[] buffer = "wx00000000".toCharArray();
        IOUtils.readFully(reader, buffer, 2, 8);
        assertEquals("wxabcd1234", new String(buffer));
        IOUtils.closeQuietly(reader);
    }

    @Test
    public void testReadLines_InputStream() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        InputStream in = null;
        try {
            final String[] data = new String[] {"hello", "world", "", "this is", "some text"};
            TestUtils.createLineBasedFile(file, data);

            in = new FileInputStream(file);
            final List<String> lines = IOUtils.readLines(in);
            assertEquals(Arrays.asList(data), lines);
            assertEquals(-1, in.read());
        } finally {
            IOUtils.closeQuietly(in);
            TestUtils.deleteFile(file);
        }
    }

    @Test
    public void testReadLines_InputStream_String() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        InputStream in = null;
        try {
            final String[] data = new String[] {"hello", "/u1234", "", "this is", "some text"};
            TestUtils.createLineBasedFile(file, data);

            in = new FileInputStream(file);
            final List<String> lines = IOUtils.readLines(in, "UTF-8");
            assertEquals(Arrays.asList(data), lines);
            assertEquals(-1, in.read());
        } finally {
            IOUtils.closeQuietly(in);
            TestUtils.deleteFile(file);
        }
    }

    @Test
    public void testReadLines_Reader() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        Reader in = null;
        try {
            final String[] data = new String[] {"hello", "/u1234", "", "this is", "some text"};
            TestUtils.createLineBasedFile(file, data);

            in = new InputStreamReader(new FileInputStream(file));
            final List<String> lines = IOUtils.readLines(in);
            assertEquals(Arrays.asList(data), lines);
            assertEquals(-1, in.read());
        } finally {
            IOUtils.closeQuietly(in);
            TestUtils.deleteFile(file);
        }
    }

    @Test
    public void testResourceToByteArray_ExistingResourceAtRootPackage() throws Exception {
        final long fileSize = TestResources.getFile("test-file-utf8.bin").length();
        final byte[] bytes = IOUtils.resourceToByteArray("/org/apache/commons/io/test-file-utf8.bin");
        assertNotNull(bytes);
        assertEquals(fileSize, bytes.length);
    }

    @Test
    public void testResourceToByteArray_ExistingResourceAtRootPackage_WithClassLoader() throws Exception {
        final long fileSize = TestResources.getFile("test-file-utf8.bin").length();
        final byte[] bytes = IOUtils.resourceToByteArray("org/apache/commons/io/test-file-utf8.bin",
            ClassLoader.getSystemClassLoader());
        assertNotNull(bytes);
        assertEquals(fileSize, bytes.length);
    }

    @Test
    public void testResourceToByteArray_ExistingResourceAtSubPackage() throws Exception {
        final long fileSize = TestResources.getFile("FileUtilsTestDataCR.dat").length();
        final byte[] bytes = IOUtils.resourceToByteArray("/org/apache/commons/io/FileUtilsTestDataCR.dat");
        assertNotNull(bytes);
        assertEquals(fileSize, bytes.length);
    }

    @Test
    public void testResourceToByteArray_ExistingResourceAtSubPackage_WithClassLoader() throws Exception {
        final long fileSize = TestResources.getFile("FileUtilsTestDataCR.dat").length();
        final byte[] bytes = IOUtils.resourceToByteArray("org/apache/commons/io/FileUtilsTestDataCR.dat",
            ClassLoader.getSystemClassLoader());
        assertNotNull(bytes);
        assertEquals(fileSize, bytes.length);
    }

    @Test
    public void testResourceToByteArray_NonExistingResource() {
        assertThrows(IOException.class, () -> IOUtils.resourceToByteArray("/non-existing-file.bin"));
    }

    @Test
    public void testResourceToByteArray_NonExistingResource_WithClassLoader() {
        assertThrows(IOException.class,
            () -> IOUtils.resourceToByteArray("non-existing-file.bin", ClassLoader.getSystemClassLoader()));
    }

    @Test
    public void testResourceToByteArray_Null() {
        assertThrows(NullPointerException.class, () -> IOUtils.resourceToByteArray(null));
    }

    @Test
    public void testResourceToByteArray_Null_WithClassLoader() {
        assertThrows(NullPointerException.class,
            () -> IOUtils.resourceToByteArray(null, ClassLoader.getSystemClassLoader()));
    }

    @Test
    public void testResourceToString_ExistingResourceAtRootPackage() throws Exception {
        final long fileSize = TestResources.getFile("test-file-simple-utf8.bin").length();
        final String content = IOUtils.resourceToString("/org/apache/commons/io/test-file-simple-utf8.bin",
            StandardCharsets.UTF_8);

        assertNotNull(content);
        assertEquals(fileSize, content.getBytes().length);
    }

    @Test
    public void testResourceToString_ExistingResourceAtRootPackage_WithClassLoader() throws Exception {
        final long fileSize = TestResources.getFile("test-file-simple-utf8.bin").length();
        final String content = IOUtils.resourceToString("org/apache/commons/io/test-file-simple-utf8.bin",
            StandardCharsets.UTF_8, ClassLoader.getSystemClassLoader());

        assertNotNull(content);
        assertEquals(fileSize, content.getBytes().length);
    }

    @Test
    public void testResourceToString_ExistingResourceAtSubPackage() throws Exception {
        final long fileSize = TestResources.getFile("FileUtilsTestDataCR.dat").length();
        final String content = IOUtils.resourceToString("/org/apache/commons/io/FileUtilsTestDataCR.dat",
            StandardCharsets.UTF_8);

        assertNotNull(content);
        assertEquals(fileSize, content.getBytes().length);
    }

    // Tests from IO-305

    @Test
    public void testResourceToString_ExistingResourceAtSubPackage_WithClassLoader() throws Exception {
        final long fileSize = TestResources.getFile("FileUtilsTestDataCR.dat").length();
        final String content = IOUtils.resourceToString("org/apache/commons/io/FileUtilsTestDataCR.dat",
            StandardCharsets.UTF_8, ClassLoader.getSystemClassLoader());

        assertNotNull(content);
        assertEquals(fileSize, content.getBytes().length);
    }

    @Test
    public void testResourceToString_NonExistingResource() {
        assertThrows(IOException.class,
            () -> IOUtils.resourceToString("/non-existing-file.bin", StandardCharsets.UTF_8));
    }

    @Test
    public void testResourceToString_NonExistingResource_WithClassLoader() {
        assertThrows(IOException.class, () -> IOUtils.resourceToString("non-existing-file.bin", StandardCharsets.UTF_8,
            ClassLoader.getSystemClassLoader()));
    }

    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    @Test
    public void testResourceToString_NullCharset() throws Exception {
        IOUtils.resourceToString("/org/apache/commons/io//test-file-utf8.bin", null);
    }

    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    @Test
    public void testResourceToString_NullCharset_WithClassLoader() throws Exception {
        IOUtils.resourceToString("org/apache/commons/io/test-file-utf8.bin", null, ClassLoader.getSystemClassLoader());
    }

    @Test
    public void testResourceToString_NullResource() {
        assertThrows(NullPointerException.class, () -> IOUtils.resourceToString(null, StandardCharsets.UTF_8));
    }

    @Test
    public void testResourceToString_NullResource_WithClassLoader() {
        assertThrows(NullPointerException.class,
            () -> IOUtils.resourceToString(null, StandardCharsets.UTF_8, ClassLoader.getSystemClassLoader()));
    }

    @Test
    public void testResourceToURL_ExistingResourceAtRootPackage() throws Exception {
        final URL url = IOUtils.resourceToURL("/org/apache/commons/io/test-file-utf8.bin");
        assertNotNull(url);
        assertTrue(url.getFile().endsWith("/test-file-utf8.bin"));
    }

    @Test
    public void testResourceToURL_ExistingResourceAtRootPackage_WithClassLoader() throws Exception {
        final URL url = IOUtils.resourceToURL("org/apache/commons/io/test-file-utf8.bin",
            ClassLoader.getSystemClassLoader());
        assertNotNull(url);
        assertTrue(url.getFile().endsWith("/org/apache/commons/io/test-file-utf8.bin"));
    }

    @Test
    public void testResourceToURL_ExistingResourceAtSubPackage() throws Exception {
        final URL url = IOUtils.resourceToURL("/org/apache/commons/io/FileUtilsTestDataCR.dat");
        assertNotNull(url);
        assertTrue(url.getFile().endsWith("/org/apache/commons/io/FileUtilsTestDataCR.dat"));
    }

    @Test
    public void testResourceToURL_ExistingResourceAtSubPackage_WithClassLoader() throws Exception {
        final URL url = IOUtils.resourceToURL("org/apache/commons/io/FileUtilsTestDataCR.dat",
            ClassLoader.getSystemClassLoader());

        assertNotNull(url);
        assertTrue(url.getFile().endsWith("/org/apache/commons/io/FileUtilsTestDataCR.dat"));
    }

    @Test
    public void testResourceToURL_NonExistingResource() {
        assertThrows(IOException.class, () -> IOUtils.resourceToURL("/non-existing-file.bin"));
    }

    @Test
    public void testResourceToURL_NonExistingResource_WithClassLoader() {
        assertThrows(IOException.class,
            () -> IOUtils.resourceToURL("non-existing-file.bin", ClassLoader.getSystemClassLoader()));
    }

    @Test
    public void testResourceToURL_Null() {
        assertThrows(NullPointerException.class, () -> IOUtils.resourceToURL(null));
    }

    @Test
    public void testResourceToURL_Null_WithClassLoader() {
        assertThrows(NullPointerException.class, () -> IOUtils.resourceToURL(null, ClassLoader.getSystemClassLoader()));
    }

    @Test
    public void testSkip_FileReader() throws Exception {
        try (FileReader in = new FileReader(m_testFile)) {
            assertEquals(FILE_SIZE - 10, IOUtils.skip(in, FILE_SIZE - 10));
            assertEquals(10, IOUtils.skip(in, 20));
            assertEquals(0, IOUtils.skip(in, 10));
        }
    }

    @Test
    public void testSkip_InputStream() throws Exception {
        try (InputStream in = new FileInputStream(m_testFile)) {
            assertEquals(FILE_SIZE - 10, IOUtils.skip(in, FILE_SIZE - 10));
            assertEquals(10, IOUtils.skip(in, 20));
            assertEquals(0, IOUtils.skip(in, 10));
        }
    }

    @Test
    public void testSkip_ReadableByteChannel() throws Exception {
        final FileInputStream fileInputStream = new FileInputStream(m_testFile);
        final FileChannel fileChannel = fileInputStream.getChannel();
        try {
            assertEquals(FILE_SIZE - 10, IOUtils.skip(fileChannel, FILE_SIZE - 10));
            assertEquals(10, IOUtils.skip(fileChannel, 20));
            assertEquals(0, IOUtils.skip(fileChannel, 10));
        } finally {
            IOUtils.closeQuietly(fileChannel, fileInputStream);
        }
    }

    @Test
    public void testSkipFully_InputStream() throws Exception {
        final int size = 1027;

        final InputStream input = new ByteArrayInputStream(new byte[size]);
        try {
            IOUtils.skipFully(input, -1);
            fail("Should have failed with IllegalArgumentException");
        } catch (final IllegalArgumentException expected) {
            // expected
        }
        IOUtils.skipFully(input, 0);
        IOUtils.skipFully(input, size - 1);
        try {
            IOUtils.skipFully(input, 2);
            fail("Should have failed with IOException");
        } catch (final IOException expected) {
            // expected
        }
        IOUtils.closeQuietly(input);

    }

    @Test
    public void testSkipFully_ReadableByteChannel() throws Exception {
        final FileInputStream fileInputStream = new FileInputStream(m_testFile);
        final FileChannel fileChannel = fileInputStream.getChannel();
        try {
            try {
                IOUtils.skipFully(fileChannel, -1);
                fail("Should have failed with IllegalArgumentException");
            } catch (final IllegalArgumentException expected) {
                // expected
            }
            IOUtils.skipFully(fileChannel, 0);
            IOUtils.skipFully(fileChannel, FILE_SIZE - 1);
            try {
                IOUtils.skipFully(fileChannel, 2);
                fail("Should have failed with IOException");
            } catch (final IOException expected) {
                // expected
            }
        } finally {
            IOUtils.closeQuietly(fileChannel, fileInputStream);
        }
    }

    @Test
    public void testSkipFully_Reader() throws Exception {
        final int size = 1027;

        final Reader input = new CharArrayReader(new char[size]);
        IOUtils.skipFully(input, 0);
        IOUtils.skipFully(input, size - 3);
        try {
            IOUtils.skipFully(input, -1);
            fail("Should have failed with IllegalArgumentException");
        } catch (final IllegalArgumentException expected) {
            // expected
        }
        try {
            IOUtils.skipFully(input, 5);
            fail("Should have failed with IOException");
        } catch (final IOException expected) {
            // expected
        }
        IOUtils.closeQuietly(input);
    }

    @Test
    public void testStringToOutputStream() throws Exception {
        final File destination = TestUtils.newFile(temporaryFolder, "copy5.txt");
        String str;
        try (FileReader fin = new FileReader(m_testFile)) {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            str = IOUtils.toString(fin);
        }

        try (FileOutputStream fout = new FileOutputStream(destination)) {
            CopyUtils.copy(str, fout);
            // Note: this method *does* flush. It is equivalent to:
            // OutputStreamWriter _out = new OutputStreamWriter(fout);
            // CopyUtils.copy( str, _out, 4096 ); // copy( Reader, Writer, int );
            // _out.flush();
            // out = fout;
            // note: we don't flush here; this IOUtils method does it for us

            TestUtils.checkFile(destination, m_testFile);
            TestUtils.checkWrite(fout);
        }
        TestUtils.deleteFile(destination);
    }

    @Test
    public void testToBufferedInputStream_InputStream() throws Exception {
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final InputStream in = IOUtils.toBufferedInputStream(fin);
            final byte[] out = IOUtils.toByteArray(in);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, m_testFile);
        }
    }

    @Test
    public void testToBufferedInputStreamWithBufferSize_InputStream() throws Exception {
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final InputStream in = IOUtils.toBufferedInputStream(fin, 2048);
            final byte[] out = IOUtils.toByteArray(in);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, m_testFile);
        }
    }

    @Test
    public void testToByteArray_InputStream() throws Exception {
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final byte[] out = IOUtils.toByteArray(fin);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, m_testFile);
        }
    }

    @Test
    public void testToByteArray_InputStream_NegativeSize() throws Exception {

        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            IOUtils.toByteArray(fin, -1);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException exc) {
            assertTrue(exc.getMessage().startsWith("Size must be equal or greater than zero"),
                "Exception message does not start with \"Size must be equal or greater than zero\"");
        }

    }

    @Test
    public void testToByteArray_InputStream_Size() throws Exception {
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final byte[] out = IOUtils.toByteArray(fin, m_testFile.length());
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size: out.length=" + out.length + "!=" + FILE_SIZE);
            TestUtils.assertEqualContent(out, m_testFile);
        }
    }

    @Test
    public void testToByteArray_InputStream_SizeIllegal() throws Exception {

        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            IOUtils.toByteArray(fin, m_testFile.length() + 1);
            fail("IOException expected");
        } catch (final IOException exc) {
            assertTrue(exc.getMessage().startsWith("Unexpected read size"),
                "Exception message does not start with \"Unexpected read size\"");
        }

    }

    @Test
    public void testToByteArray_InputStream_SizeLong() throws Exception {

        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            IOUtils.toByteArray(fin, (long) Integer.MAX_VALUE + 1);
            fail("IOException expected");
        } catch (final IllegalArgumentException exc) {
            assertTrue(exc.getMessage().startsWith("Size cannot be greater than Integer max value"),
                "Exception message does not start with \"Size cannot be greater than Integer max value\"");
        }

    }

    @Test
    public void testToByteArray_InputStream_SizeZero() throws Exception {

        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final byte[] out = IOUtils.toByteArray(fin, 0);
            assertNotNull(out, "Out cannot be null");
            assertEquals(0, out.length, "Out length must be 0");
        }
    }

    @Test
    public void testToByteArray_InputStream_SizeOne() throws Exception {

        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final byte[] out = IOUtils.toByteArray(fin, 1);
            assertNotNull(out, "Out cannot be null");
            assertEquals(1, out.length, "Out length must be 1");
        }
    }

    @Test
    public void testToByteArray_Reader() throws IOException {
        final String charsetName = "UTF-8";
        final byte[] expecteds = charsetName.getBytes(charsetName);
        byte[] actuals = IOUtils.toByteArray(new InputStreamReader(new ByteArrayInputStream(expecteds)));
        assertArrayEquals(expecteds, actuals);
        actuals = IOUtils.toByteArray(new InputStreamReader(new ByteArrayInputStream(expecteds)), charsetName);
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testToByteArray_String() throws Exception {
        try (FileReader fin = new FileReader(m_testFile)) {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            final String str = IOUtils.toString(fin);

            final byte[] out = IOUtils.toByteArray(str);
            assertEqualContent(str.getBytes(), out);
        }
    }

    @Test
    public void testToByteArray_URI() throws Exception {
        final URI url = m_testFile.toURI();
        final byte[] actual = IOUtils.toByteArray(url);
        assertEquals(FILE_SIZE, actual.length);
    }

    @Test
    public void testToByteArray_URL() throws Exception {
        final URL url = m_testFile.toURI().toURL();
        final byte[] actual = IOUtils.toByteArray(url);
        assertEquals(FILE_SIZE, actual.length);
    }

    @Test
    public void testToByteArray_URLConnection() throws Exception {
        final URLConnection urlConn = m_testFile.toURI().toURL().openConnection();
        byte[] actual;
        try {
            actual = IOUtils.toByteArray(urlConn);
        } finally {
            IOUtils.close(urlConn);
        }
        assertEquals(FILE_SIZE, actual.length);
    }

    @Test
    public void testToCharArray_InputStream() throws Exception {
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final char[] out = IOUtils.toCharArray(fin);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all chars were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, m_testFile);
        }
    }

    @Test
    public void testToCharArray_InputStream_CharsetName() throws Exception {
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final char[] out = IOUtils.toCharArray(fin, "UTF-8");
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all chars were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, m_testFile);
        }
    }

    @Test
    public void testToCharArray_Reader() throws Exception {
        try (FileReader fr = new FileReader(m_testFile)) {
            final char[] out = IOUtils.toCharArray(fr);
            assertNotNull(out);
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, m_testFile);
        }
    }

    /**
     * Test for {@link IOUtils#toInputStream(CharSequence)} and {@link IOUtils#toInputStream(CharSequence, String)}.
     * Note, this test utilizes on {@link IOUtils#toByteArray(java.io.InputStream)} and so relies on
     * {@link #testToByteArray_InputStream()} to ensure this method functions correctly.
     *
     * @throws Exception on error
     */
    @Test
    public void testToInputStream_CharSequence() throws Exception {
        final CharSequence csq = new StringBuilder("Abc123Xyz!");
        InputStream inStream = IOUtils.toInputStream(csq); // deliberately testing deprecated method
        byte[] bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(csq.toString().getBytes(), bytes);
        inStream = IOUtils.toInputStream(csq, (String) null);
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(csq.toString().getBytes(), bytes);
        inStream = IOUtils.toInputStream(csq, "UTF-8");
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(csq.toString().getBytes(StandardCharsets.UTF_8), bytes);
    }

    /**
     * Test for {@link IOUtils#toInputStream(String)} and {@link IOUtils#toInputStream(String, String)}. Note, this test
     * utilizes on {@link IOUtils#toByteArray(java.io.InputStream)} and so relies on
     * {@link #testToByteArray_InputStream()} to ensure this method functions correctly.
     *
     * @throws Exception on error
     */
    @Test
    public void testToInputStream_String() throws Exception {
        final String str = "Abc123Xyz!";
        InputStream inStream = IOUtils.toInputStream(str);
        byte[] bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(str.getBytes(), bytes);
        inStream = IOUtils.toInputStream(str, (String) null);
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(str.getBytes(), bytes);
        inStream = IOUtils.toInputStream(str, "UTF-8");
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(str.getBytes(StandardCharsets.UTF_8), bytes);
    }

    @Test
    public void testToString_ByteArray() throws Exception {
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final byte[] in = IOUtils.toByteArray(fin);
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            final String str = IOUtils.toString(in);
            assertEqualContent(in, str.getBytes());
        }
    }

    @Test
    public void testToString_InputStream() throws Exception {
        try (FileInputStream fin = new FileInputStream(m_testFile)) {
            final String out = IOUtils.toString(fin);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length(), "Wrong output size");
        }
    }

    @Test
    public void testToString_Reader() throws Exception {
        try (FileReader fin = new FileReader(m_testFile)) {
            final String out = IOUtils.toString(fin);
            assertNotNull(out);
            assertEquals(FILE_SIZE, out.length(), "Wrong output size");
        }
    }

    @Test
    public void testToString_URI() throws Exception {
        final URI url = m_testFile.toURI();
        final String out = IOUtils.toString(url);
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
    }

    private void testToString_URI(final String encoding) throws Exception {
        final URI uri = m_testFile.toURI();
        final String out = IOUtils.toString(uri, encoding);
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
    }

    @Test
    public void testToString_URI_CharsetName() throws Exception {
        testToString_URI("US-ASCII");
    }

    @Test
    public void testToString_URI_CharsetNameNull() throws Exception {
        testToString_URI(null);
    }

    @Test
    public void testToString_URL() throws Exception {
        final URL url = m_testFile.toURI().toURL();
        final String out = IOUtils.toString(url);
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
    }

    private void testToString_URL(final String encoding) throws Exception {
        final URL url = m_testFile.toURI().toURL();
        final String out = IOUtils.toString(url, encoding);
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
    }

    @Test
    public void testToString_URL_CharsetName() throws Exception {
        testToString_URL("US-ASCII");
    }

    @Test
    public void testToString_URL_CharsetNameNull() throws Exception {
        testToString_URL(null);
    }

}
