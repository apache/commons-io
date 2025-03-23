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
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.input.BrokenInputStream;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.io.input.CircularInputStream;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.input.NullReader;
import org.apache.commons.io.output.AppendableWriter;
import org.apache.commons.io.output.BrokenOutputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.io.test.ThrowOnCloseReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This is used to test {@link IOUtils} for correctness. The following checks are performed:
 * <ul>
 * <li>The return must not be null, must be the same type and equals() to the method's second arg</li>
 * <li>All bytes must have been read from the source (available() == 0)</li>
 * <li>The source and destination content must be identical (byte-wise comparison check)</li>
 * <li>The output stream must not have been closed (a byte/char is written to test this, and subsequent size
 * checked)</li>
 * </ul>
 * Due to interdependencies in IOUtils and IOUtilsTest, one bug may cause multiple tests to fail.
 */
@SuppressWarnings("deprecation") // deliberately testing deprecated code
public class IOUtilsTest {

    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    private static final int FILE_SIZE = 1024 * 4 + 1;

    /** Determine if this is windows. */
    private static final boolean WINDOWS = File.separatorChar == '\\';

    /*
     * Note: this is not particularly beautiful code. A better way to check for flush and close status would be to
     * implement "trojan horse" wrapper implementations of the various stream classes, which set a flag when relevant
     * methods are called. (JT)
     */

    @BeforeAll
    @AfterAll
    public static void beforeAll() {
        // Not required, just to exercise the method and make sure there are no adverse side-effect when recycling thread locals.
        IO.clear();
    }

    @TempDir
    public File temporaryFolder;

    private char[] carr;

    private byte[] iarr;

    private File testFile;

    /**
     * Path constructed from {@code testFile}.
     */
    private Path testFilePath;

    /** Assert that the contents of two byte arrays are the same. */
    private void assertEqualContent(final byte[] b0, final byte[] b1) {
        assertArrayEquals(b0, b1, "Content not equal according to java.util.Arrays#equals()");
    }

    @BeforeEach
    public void setUp() {
        try {
            testFile = new File(temporaryFolder, "file2-test.txt");
            testFilePath = testFile.toPath();

            if (!testFile.getParentFile().exists()) {
                throw new IOException("Cannot create file " + testFile + " as the parent directory does not exist");
            }
            try (BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(testFilePath))) {
                TestUtils.generateTestData(output, FILE_SIZE);
            }
        } catch (final IOException e) {
            fail("Can't run this test because the environment could not be built: " + e.getMessage());
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
        final String npeExpectedMessage = "Expected NullPointerException";
        assertThrows(NullPointerException.class, () -> IOUtils.buffer((InputStream) null), npeExpectedMessage);
        assertThrows(NullPointerException.class, () -> IOUtils.buffer((OutputStream) null), npeExpectedMessage);
        assertThrows(NullPointerException.class, () -> IOUtils.buffer((Reader) null), npeExpectedMessage);
        assertThrows(NullPointerException.class, () -> IOUtils.buffer((Writer) null), npeExpectedMessage);
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
        final Writer nullWriter = NullWriter.INSTANCE;
        final BufferedWriter bis = IOUtils.buffer(nullWriter);
        assertNotSame(nullWriter, bis);
        assertSame(bis, IOUtils.buffer(bis));
    }

    @Test
    public void testAsBufferedWriterWithBufferSize() {
        final Writer nullWriter = NullWriter.INSTANCE;
        final BufferedWriter bw = IOUtils.buffer(nullWriter, 2024);
        assertNotSame(nullWriter, bw);
        assertSame(bw, IOUtils.buffer(bw));
        assertSame(bw, IOUtils.buffer(bw, 1024));
    }

    @Test
    public void testAsWriterAppendable() throws IOException {
        final Appendable a = new StringBuffer();
        try (Writer w = IOUtils.writer(a)) {
            assertNotSame(w, a);
            assertEquals(AppendableWriter.class, w.getClass());
            assertSame(w, IOUtils.writer(w));
        }
    }

    @Test
    public void testAsWriterNull() {
        assertThrows(NullPointerException.class, () -> IOUtils.writer(null));
    }

    @Test
    public void testAsWriterStringBuilder() throws IOException {
        final Appendable a = new StringBuilder();
        try (Writer w = IOUtils.writer(a)) {
            assertNotSame(w, a);
            assertEquals(StringBuilderWriter.class, w.getClass());
            assertSame(w, IOUtils.writer(w));
        }
    }

    @Test
    public void testByteArrayWithNegativeSize() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtils.byteArray(-1));
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> IOUtils.close((Closeable) null));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s")));
        assertThrows(IOException.class, () -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s"))));
    }

    @Test
    public void testCloseConsumer() {
        // null consumer
        final Closeable nullCloseable = null;
        assertDoesNotThrow(() -> IOUtils.close(nullCloseable, null));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), null));
        assertDoesNotThrow(() -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), null));
        // null consumer doesn't throw
        final IOConsumer<IOException> nullConsumer = null;
        assertDoesNotThrow(() -> IOUtils.close(nullCloseable, nullConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), nullConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), nullConsumer));
        // noop consumer doesn't throw
        final IOConsumer<IOException> silentConsumer = IOConsumer.noop();
        assertDoesNotThrow(() -> IOUtils.close(nullCloseable, silentConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), silentConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), silentConsumer));
        // consumer passes on the throw
        final IOConsumer<IOException> noisyConsumer = ExceptionUtils::rethrow;
        // no throw
        assertDoesNotThrow(() -> IOUtils.close(nullCloseable, noisyConsumer));
        // no throw
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), noisyConsumer));
        // closeable throws
        assertThrows(IOException.class, () -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), noisyConsumer));
        // consumes other than IOException
        final AtomicBoolean b = new AtomicBoolean();
        final IOConsumer<IOException> consumer = e -> b.set(true);
        // IOException subclass
        assertDoesNotThrow(() -> IOUtils.close(new BrokenOutputStream((Throwable) new EOFException()), consumer));
        assertTrue(b.get());
        b.set(false);
        // RuntimeException
        assertDoesNotThrow(() -> IOUtils.close(new BrokenOutputStream(new RuntimeException()), consumer));
        assertTrue(b.get());
        b.set(false);
        // RuntimeException subclass
        assertDoesNotThrow(() -> IOUtils.close(new BrokenOutputStream(new UnsupportedOperationException()), consumer));
        assertTrue(b.get());
    }

    @Test
    public void testCloseMulti() {
        final Closeable nullCloseable = null;
        final Closeable[] closeables = {null, null};
        assertDoesNotThrow(() -> IOUtils.close(nullCloseable, nullCloseable));
        assertDoesNotThrow(() -> IOUtils.close(closeables));
        assertDoesNotThrow(() -> IOUtils.close((Closeable[]) null));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), nullCloseable));
        assertThrows(IOException.class, () -> IOUtils.close(nullCloseable, new ThrowOnCloseReader(new StringReader("s"))));
    }

    @Test
    public void testCloseQuietly_AllCloseableIOException() {
        final Closeable closeable = BrokenInputStream.INSTANCE;
        assertDoesNotThrow(() -> IOUtils.closeQuietly(closeable, null, closeable));
        assertDoesNotThrow(() -> IOUtils.closeQuietly(Arrays.asList(closeable, null, closeable)));
        assertDoesNotThrow(() -> IOUtils.closeQuietly(Stream.of(closeable, null, closeable)));
        assertDoesNotThrow(() -> IOUtils.closeQuietly((Iterable<Closeable>) null));
    }

    @Test
    public void testCloseQuietly_CloseableException() {
        // IOException
        assertDoesNotThrow(() -> IOUtils.closeQuietly(BrokenInputStream.INSTANCE));
        assertDoesNotThrow(() -> IOUtils.closeQuietly(BrokenOutputStream.INSTANCE));
        // IOException subclass
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new BrokenOutputStream((Throwable) new EOFException())));
        // RuntimeException
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new BrokenOutputStream(new RuntimeException())));
        // RuntimeException subclass
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new BrokenOutputStream(new UnsupportedOperationException())));
    }

    @Test
    public void testCloseQuietly_CloseableExceptionConsumer() {
        final AtomicBoolean b = new AtomicBoolean();
        final Consumer<Exception> consumer = e -> b.set(true);
        // IOException
        assertDoesNotThrow(() -> IOUtils.closeQuietly(BrokenInputStream.INSTANCE, consumer));
        assertTrue(b.get());
        b.set(false);
        assertDoesNotThrow(() -> IOUtils.closeQuietly(BrokenOutputStream.INSTANCE, consumer));
        assertTrue(b.get());
        b.set(false);
        // IOException subclass
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new BrokenOutputStream((Throwable) new EOFException()), consumer));
        assertTrue(b.get());
        b.set(false);
        // RuntimeException
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new BrokenOutputStream(new RuntimeException()), consumer));
        assertTrue(b.get());
        b.set(false);
        // RuntimeException subclass
        assertDoesNotThrow(() -> IOUtils.closeQuietly(new BrokenOutputStream(new UnsupportedOperationException()), consumer));
        assertTrue(b.get());
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
    public void testCloseURLConnection() {
        assertDoesNotThrow(() -> IOUtils.close((URLConnection) null));
        assertDoesNotThrow(() -> IOUtils.close(new URL("https://www.apache.org/").openConnection()));
        assertDoesNotThrow(() -> IOUtils.close(new URL("file:///").openConnection()));
    }

    @Test
    public void testConstants() {
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
    public void testConsumeInputStream() throws Exception {
        final long size = (long) Integer.MAX_VALUE + (long) 1;
        final NullInputStream in = new NullInputStream(size);
        final OutputStream out = NullOutputStream.INSTANCE;

        // Test copy() method
        assertEquals(-1, IOUtils.copy(in, out));

        // reset the input
        in.init();

        // Test consume() method
        assertEquals(size, IOUtils.consume(in), "consume()");
    }

    @Test
    public void testConsumeReader() throws Exception {
        final long size = (long) Integer.MAX_VALUE + (long) 1;
        final Reader in = new NullReader(size);
        final Writer out = NullWriter.INSTANCE;

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
        final byte[] dataEmpty = "".getBytes(StandardCharsets.UTF_8);
        final byte[] dataAbc = "ABC".getBytes(StandardCharsets.UTF_8);
        final byte[] dataAbcd = "ABCD".getBytes(StandardCharsets.UTF_8);
        {
            final ByteArrayInputStream input1 = new ByteArrayInputStream(dataEmpty);
            assertFalse(IOUtils.contentEquals(input1, null));
        }
        {
            final ByteArrayInputStream input1 = new ByteArrayInputStream(dataEmpty);
            assertFalse(IOUtils.contentEquals(null, input1));
        }
        {
            final ByteArrayInputStream input1 = new ByteArrayInputStream(dataEmpty);
            assertTrue(IOUtils.contentEquals(input1, input1));
        }
        {
            final ByteArrayInputStream input1 = new ByteArrayInputStream(dataAbc);
            assertTrue(IOUtils.contentEquals(input1, input1));
        }
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(dataEmpty), new ByteArrayInputStream(dataEmpty)));
        assertTrue(IOUtils.contentEquals(new BufferedInputStream(new ByteArrayInputStream(dataEmpty)),
            new BufferedInputStream(new ByteArrayInputStream(dataEmpty))));
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(dataAbc), new ByteArrayInputStream(dataAbc)));
        assertFalse(IOUtils.contentEquals(new ByteArrayInputStream(dataAbcd), new ByteArrayInputStream(dataAbc)));
        assertFalse(IOUtils.contentEquals(new ByteArrayInputStream(dataAbc), new ByteArrayInputStream(dataAbcd)));
        assertFalse(IOUtils.contentEquals(new ByteArrayInputStream("apache".getBytes(StandardCharsets.UTF_8)),
                new ByteArrayInputStream("apacha".getBytes(StandardCharsets.UTF_8))));
        // Tests with larger inputs that DEFAULT_BUFFER_SIZE in case internal buffers are used.
        final byte[] bytes2XDefaultA = new byte[IOUtils.DEFAULT_BUFFER_SIZE * 2];
        final byte[] bytes2XDefaultB = new byte[IOUtils.DEFAULT_BUFFER_SIZE * 2];
        final byte[] bytes2XDefaultA2 = new byte[IOUtils.DEFAULT_BUFFER_SIZE * 2];
        Arrays.fill(bytes2XDefaultA, (byte) 'a');
        Arrays.fill(bytes2XDefaultB, (byte) 'b');
        Arrays.fill(bytes2XDefaultA2, (byte) 'a');
        bytes2XDefaultA2[bytes2XDefaultA2.length - 1] = 'd';
        assertFalse(IOUtils.contentEquals(new ByteArrayInputStream(bytes2XDefaultA),
            new ByteArrayInputStream(bytes2XDefaultB)));
        assertFalse(IOUtils.contentEquals(new ByteArrayInputStream(bytes2XDefaultA),
            new ByteArrayInputStream(bytes2XDefaultA2)));
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(bytes2XDefaultA),
            new ByteArrayInputStream(bytes2XDefaultA)));
        // FileInputStream a bit more than 16 k.
        try (
            FileInputStream input1 = new FileInputStream(
                "src/test/resources/org/apache/commons/io/abitmorethan16k.txt");
            FileInputStream input2 = new FileInputStream(
                "src/test/resources/org/apache/commons/io/abitmorethan16kcopy.txt")) {
            assertTrue(IOUtils.contentEquals(input1, input1));
        }
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
        assertFalse(IOUtils.contentEquals(new StringReader("apache"), new StringReader("apacha")));
    }

    @Test
    public void testContentEqualsIgnoreEOL() throws Exception {
        {
            assertTrue(IOUtils.contentEqualsIgnoreEOL(null, null));
        }
        final char[] empty = {};
        {
            final Reader input1 = new CharArrayReader(empty);
            assertFalse(IOUtils.contentEqualsIgnoreEOL(null, input1));
        }
        {
            final Reader input1 = new CharArrayReader(empty);
            assertFalse(IOUtils.contentEqualsIgnoreEOL(input1, null));
        }
        {
            final Reader input1 = new CharArrayReader(empty);
            assertTrue(IOUtils.contentEqualsIgnoreEOL(input1, input1));
        }
        {
            final Reader input1 = new CharArrayReader("321\r\n".toCharArray());
            assertTrue(IOUtils.contentEqualsIgnoreEOL(input1, input1));
        }

        testSingleEOL("", "", true);
        testSingleEOL("", "\n", false);
        testSingleEOL("", "\r", false);
        testSingleEOL("", "\r\n", false);
        testSingleEOL("", "\r\r", false);
        testSingleEOL("", "\n\n", false);
        testSingleEOL("1", "1", true);
        testSingleEOL("1", "2", false);
        testSingleEOL("123\rabc", "123\nabc", true);
        testSingleEOL("321", "321\r\n", true);
        testSingleEOL("321", "321\r\naabb", false);
        testSingleEOL("321", "321\n", true);
        testSingleEOL("321", "321\r", true);
        testSingleEOL("321", "321\r\n", true);
        testSingleEOL("321", "321\r\r", false);
        testSingleEOL("321", "321\n\r", false);
        testSingleEOL("321\n", "321", true);
        testSingleEOL("321\n", "321\n\r", false);
        testSingleEOL("321\n", "321\r\n", true);
        testSingleEOL("321\r", "321\r\n", true);
        testSingleEOL("321\r\n", "321\r\n\r", false);
        testSingleEOL("123", "1234", false);
        testSingleEOL("1235", "1234", false);
    }

    @Test
    public void testContentEqualsSequenceInputStream() throws Exception {
        // https://issues.apache.org/jira/browse/IO-866
        // not equals
        // @formatter:off
        assertFalse(IOUtils.contentEquals(
                new ByteArrayInputStream("ab".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("a".getBytes()),
                    new ByteArrayInputStream("b-".getBytes()))));
        assertFalse(IOUtils.contentEquals(
                new ByteArrayInputStream("ab".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("a-".getBytes()),
                    new ByteArrayInputStream("b".getBytes()))));
        assertFalse(IOUtils.contentEquals(
                new ByteArrayInputStream("ab-".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("a".getBytes()),
                    new ByteArrayInputStream("b".getBytes()))));
        assertFalse(IOUtils.contentEquals(
                new ByteArrayInputStream("".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("a".getBytes()),
                    new ByteArrayInputStream("b".getBytes()))));
        assertFalse(IOUtils.contentEquals(
                new ByteArrayInputStream("".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("".getBytes()),
                    new ByteArrayInputStream("b".getBytes()))));
        assertFalse(IOUtils.contentEquals(
                new ByteArrayInputStream("ab".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("".getBytes()),
                    new ByteArrayInputStream("".getBytes()))));
        // equals
        assertTrue(IOUtils.contentEquals(
                new ByteArrayInputStream("".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("".getBytes()),
                    new ByteArrayInputStream("".getBytes()))));
        assertTrue(IOUtils.contentEquals(
                new ByteArrayInputStream("ab".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("a".getBytes()),
                    new ByteArrayInputStream("b".getBytes()))));
        assertTrue(IOUtils.contentEquals(
                new ByteArrayInputStream("ab".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("ab".getBytes()),
                    new ByteArrayInputStream("".getBytes()))));
        assertTrue(IOUtils.contentEquals(
                new ByteArrayInputStream("ab".getBytes()),
                new SequenceInputStream(
                    new ByteArrayInputStream("".getBytes()),
                    new ByteArrayInputStream("ab".getBytes()))));
        // @formatter:on
        final byte[] prefixLen32 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2 };
        final byte[] suffixLen2 = { 1, 2 };
        final byte[] fileContents = "someTexts".getBytes(StandardCharsets.UTF_8);
        Files.write(testFile.toPath(), fileContents);
        final byte[] expected = new byte[prefixLen32.length + fileContents.length + suffixLen2.length];
        System.arraycopy(prefixLen32, 0, expected, 0, prefixLen32.length);
        System.arraycopy(fileContents, 0, expected, prefixLen32.length, fileContents.length);
        System.arraycopy(suffixLen2, 0, expected, prefixLen32.length + fileContents.length, suffixLen2.length);
        // @formatter:off
        assertTrue(IOUtils.contentEquals(
                new ByteArrayInputStream(expected),
                new SequenceInputStream(
                    Collections.enumeration(
                        Arrays.asList(
                            new ByteArrayInputStream(prefixLen32),
                            new FileInputStream(testFile),
                            new ByteArrayInputStream(suffixLen2))))));
        // @formatter:on
    }

    @Test
    public void testCopy_ByteArray_OutputStream() throws Exception {
        final File destination = TestUtils.newFile(temporaryFolder, "copy8.txt");
        final byte[] in;
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            in = IOUtils.toByteArray(fin);
        }

        try (OutputStream fout = Files.newOutputStream(destination.toPath())) {
            CopyUtils.copy(in, fout);

            fout.flush();

            TestUtils.checkFile(destination, testFile);
            TestUtils.checkWrite(fout);
        }
        TestUtils.deleteFile(destination);
    }

    @Test
    public void testCopy_ByteArray_Writer() throws Exception {
        final File destination = TestUtils.newFile(temporaryFolder, "copy7.txt");
        final byte[] in;
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            in = IOUtils.toByteArray(fin);
        }

        try (Writer fout = Files.newBufferedWriter(destination.toPath())) {
            CopyUtils.copy(in, fout);
            fout.flush();
            TestUtils.checkFile(destination, testFile);
            TestUtils.checkWrite(fout);
        }
        TestUtils.deleteFile(destination);
    }

    @Test
    public void testCopy_String_Writer() throws Exception {
        final File destination = TestUtils.newFile(temporaryFolder, "copy6.txt");
        final String str;
        try (Reader fin = Files.newBufferedReader(testFilePath)) {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            str = IOUtils.toString(fin);
        }

        try (Writer fout = Files.newBufferedWriter(destination.toPath())) {
            CopyUtils.copy(str, fout);
            fout.flush();

            TestUtils.checkFile(destination, testFile);
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
    public void testCopyLarge_CharSkipInvalid() {
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
        final FileInputStream fileInputStream = new FileInputStream(testFile);
        final FileChannel input = fileInputStream.getChannel();
        try {
            assertEquals(FILE_SIZE, IOUtils.read(input, buffer));
            assertEquals(0, IOUtils.read(input, buffer));
            assertEquals(0, buffer.remaining());
            assertEquals(0, input.read(buffer));
            buffer.clear();
            assertThrows(EOFException.class, () -> IOUtils.readFully(input, buffer), "Should have failed with EOFException");
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

        assertThrows(IllegalArgumentException.class, () -> IOUtils.readFully(input, buffer, 0, -1), "Should have failed with IllegalArgumentException");

        IOUtils.readFully(input, buffer, 0, 0);
        IOUtils.readFully(input, buffer, 0, size - 1);
        assertThrows(EOFException.class, () -> IOUtils.readFully(input, buffer, 0, 2), "Should have failed with EOFException");
        IOUtils.closeQuietly(input);
    }

    @Test
    public void testReadFully_InputStream_Offset() throws Exception {
        final InputStream stream = CharSequenceInputStream.builder().setCharSequence("abcd1234").setCharset(StandardCharsets.UTF_8).get();
        final byte[] buffer = "wx00000000".getBytes(StandardCharsets.UTF_8);
        IOUtils.readFully(stream, buffer, 2, 8);
        assertEquals("wxabcd1234", new String(buffer, 0, buffer.length, StandardCharsets.UTF_8));
        IOUtils.closeQuietly(stream);
    }

    @Test
    public void testReadFully_ReadableByteChannel() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocate(FILE_SIZE);
        final FileInputStream fileInputStream = new FileInputStream(testFile);
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
            assertThrows(EOFException.class, () -> IOUtils.readFully(input, buffer), "Should have failed with EOFxception");
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
        assertThrows(IllegalArgumentException.class, () -> IOUtils.readFully(input, buffer, 0, -1), "Should have failed with IllegalArgumentException");
        assertThrows(EOFException.class, () -> IOUtils.readFully(input, buffer, 0, 5), "Should have failed with EOFException");
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
    public void testReadLines_CharSequence() throws IOException {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        CharSequence csq = null;
        try {
            final String[] data = {"hello", "\u1234", "", "this is", "some text"};
            TestUtils.createLineFileUtf8(file, data);
            csq = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            final List<String> lines = IOUtils.readLines(csq);
            assertEquals(Arrays.asList(data), lines);
        } finally {
            TestUtils.deleteFile(file);
        }
    }

    @Test
    public void testReadLines_CharSequenceAsStringBuilder() throws IOException {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        StringBuilder csq = null;
        try {
            final String[] data = {"hello", "\u1234", "", "this is", "some text"};
            TestUtils.createLineFileUtf8(file, data);
            csq = new StringBuilder(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            final List<String> lines = IOUtils.readLines(csq);
            assertEquals(Arrays.asList(data), lines);
        } finally {
            TestUtils.deleteFile(file);
        }
    }

    @Test
    public void testReadLines_InputStream() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        InputStream in = null;
        try {
            final String[] data = {"hello", "world", "", "this is", "some text"};
            TestUtils.createLineFileUtf8(file, data);

            in = Files.newInputStream(file.toPath());
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
            final String[] data = { "\u4f60\u597d", "hello", "\u1234", "", "this is", "some text" };
            TestUtils.createLineFileUtf8(file, data);
            in = Files.newInputStream(file.toPath());
            final List<String> lines = IOUtils.readLines(in, UTF_8);
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
            // Don't use non-ASCII in this test fixture because this test uses the default platform encoding.
            final String[] data = {"hello", "1234", "", "this is", "some text"};
            TestUtils.createLineFileUtf8(file, data);
            in = new InputStreamReader(Files.newInputStream(file.toPath()));
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

    public void testSingleEOL(final String s1, final String s2, final boolean ifEquals) {
        assertEquals(ifEquals, IOUtils.contentEqualsIgnoreEOL(
                new CharArrayReader(s1.toCharArray()),
                new CharArrayReader(s2.toCharArray())
        ), "failed at :{" + s1 + "," + s2 + "}");
        assertEquals(ifEquals, IOUtils.contentEqualsIgnoreEOL(
                new CharArrayReader(s2.toCharArray()),
                new CharArrayReader(s1.toCharArray())
        ), "failed at :{" + s2 + "," + s1 + "}");
        assertTrue(IOUtils.contentEqualsIgnoreEOL(
                new CharArrayReader(s1.toCharArray()),
                new CharArrayReader(s1.toCharArray())
        ), "failed at :{" + s1 + "," + s1 + "}");
        assertTrue(IOUtils.contentEqualsIgnoreEOL(
                new CharArrayReader(s2.toCharArray()),
                new CharArrayReader(s2.toCharArray())
        ), "failed at :{" + s2 + "," + s2 + "}");
    }

    @Test
    public void testSkip_FileReader() throws Exception {
        try (Reader in = Files.newBufferedReader(testFilePath)) {
            assertEquals(FILE_SIZE - 10, IOUtils.skip(in, FILE_SIZE - 10));
            assertEquals(10, IOUtils.skip(in, 20));
            assertEquals(0, IOUtils.skip(in, 10));
        }
    }

    @Test
    public void testSkip_InputStream() throws Exception {
        try (InputStream in = Files.newInputStream(testFilePath)) {
            assertEquals(FILE_SIZE - 10, IOUtils.skip(in, FILE_SIZE - 10));
            assertEquals(10, IOUtils.skip(in, 20));
            assertEquals(0, IOUtils.skip(in, 10));
        }
    }

    @Test
    public void testSkip_ReadableByteChannel() throws Exception {
        final FileInputStream fileInputStream = new FileInputStream(testFile);
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

        try (InputStream input = new ByteArrayInputStream(new byte[size])) {
            assertThrows(IllegalArgumentException.class, () -> IOUtils.skipFully(input, -1), "Should have failed with IllegalArgumentException");

            IOUtils.skipFully(input, 0);
            IOUtils.skipFully(input, size - 1);
            assertThrows(IOException.class, () -> IOUtils.skipFully(input, 2), "Should have failed with IOException");
        }
    }

    @Test
    public void testSkipFully_InputStream_Buffer_New_bytes() throws Exception {
        final int size = 1027;
        final Supplier<byte[]> bas = () -> new byte[size];
        try (InputStream input = new ByteArrayInputStream(new byte[size])) {
            assertThrows(IllegalArgumentException.class, () -> IOUtils.skipFully(input, -1, bas), "Should have failed with IllegalArgumentException");

            IOUtils.skipFully(input, 0, bas);
            IOUtils.skipFully(input, size - 1, bas);
            assertThrows(IOException.class, () -> IOUtils.skipFully(input, 2, bas), "Should have failed with IOException");
        }
    }

    @Test
    public void testSkipFully_InputStream_Buffer_Reuse_bytes() throws Exception {
        final int size = 1027;
        final byte[] ba = new byte[size];
        final Supplier<byte[]> bas = () -> ba;
        try (InputStream input = new ByteArrayInputStream(new byte[size])) {
            assertThrows(IllegalArgumentException.class, () -> IOUtils.skipFully(input, -1, bas), "Should have failed with IllegalArgumentException");

            IOUtils.skipFully(input, 0, bas);
            IOUtils.skipFully(input, size - 1, bas);
            assertThrows(IOException.class, () -> IOUtils.skipFully(input, 2, bas), "Should have failed with IOException");
        }
    }

    @Test
    public void testSkipFully_InputStream_Buffer_Reuse_ThreadLocal() throws Exception {
        final int size = 1027;
        final ThreadLocal<byte[]> tl = ThreadLocal.withInitial(() -> new byte[size]);
        try (InputStream input = new ByteArrayInputStream(new byte[size])) {
            assertThrows(IllegalArgumentException.class, () -> IOUtils.skipFully(input, -1, tl::get), "Should have failed with IllegalArgumentException");

            IOUtils.skipFully(input, 0, tl::get);
            IOUtils.skipFully(input, size - 1, tl::get);
            assertThrows(IOException.class, () -> IOUtils.skipFully(input, 2, tl::get), "Should have failed with IOException");
        }
    }

    @Test
    public void testSkipFully_ReadableByteChannel() throws Exception {
        final FileInputStream fileInputStream = new FileInputStream(testFile);
        final FileChannel fileChannel = fileInputStream.getChannel();
        try {
            assertThrows(IllegalArgumentException.class, () -> IOUtils.skipFully(fileChannel, -1), "Should have failed with IllegalArgumentException");
            IOUtils.skipFully(fileChannel, 0);
            IOUtils.skipFully(fileChannel, FILE_SIZE - 1);
            assertThrows(IOException.class, () -> IOUtils.skipFully(fileChannel, 2), "Should have failed with IOException");
        } finally {
            IOUtils.closeQuietly(fileChannel, fileInputStream);
        }
    }

    @Test
    public void testSkipFully_Reader() throws Exception {
        final int size = 1027;
        try (Reader input = new CharArrayReader(new char[size])) {
            IOUtils.skipFully(input, 0);
            IOUtils.skipFully(input, size - 3);
            assertThrows(IllegalArgumentException.class, () -> IOUtils.skipFully(input, -1), "Should have failed with IllegalArgumentException");
            assertThrows(IOException.class, () -> IOUtils.skipFully(input, 5), "Should have failed with IOException");
        }
    }

    @Test
    public void testStringToOutputStream() throws Exception {
        final File destination = TestUtils.newFile(temporaryFolder, "copy5.txt");
        final String str;
        try (Reader fin = Files.newBufferedReader(testFilePath)) {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            str = IOUtils.toString(fin);
        }

        try (OutputStream fout = Files.newOutputStream(destination.toPath())) {
            CopyUtils.copy(str, fout);
            // Note: this method *does* flush. It is equivalent to:
            // OutputStreamWriter _out = new OutputStreamWriter(fout);
            // CopyUtils.copy( str, _out, 4096 ); // copy( Reader, Writer, int );
            // _out.flush();
            // out = fout;
            // note: we don't flush here; this IOUtils method does it for us

            TestUtils.checkFile(destination, testFile);
            TestUtils.checkWrite(fout);
        }
        TestUtils.deleteFile(destination);
    }

    @Test
    public void testToBufferedInputStream_InputStream() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final InputStream in = IOUtils.toBufferedInputStream(fin);
            final byte[] out = IOUtils.toByteArray(in);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, testFile);
        }
    }

    @Test
    public void testToBufferedInputStreamWithBufferSize_InputStream() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final InputStream in = IOUtils.toBufferedInputStream(fin, 2048);
            final byte[] out = IOUtils.toByteArray(in);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, testFile);
        }
    }

    @Test
    public void testToByteArray_InputStream() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final byte[] out = IOUtils.toByteArray(fin);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, testFile);
        }
    }

    @Test
    @Disabled("Disable by default as it uses too much memory and can cause builds to fail.")
    public void testToByteArray_InputStream_LongerThanIntegerMaxValue() throws Exception {
        final CircularInputStream cin = new CircularInputStream(IOUtils.byteArray(), Integer.MAX_VALUE + 1L);
        assertThrows(IllegalArgumentException.class, () -> IOUtils.toByteArray(cin));
    }

    @Test
    public void testToByteArray_InputStream_NegativeSize() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final IllegalArgumentException exc = assertThrows(IllegalArgumentException.class, () -> IOUtils.toByteArray(fin, -1),
                    "Should have failed with IllegalArgumentException");
            assertTrue(exc.getMessage().startsWith("Size must be equal or greater than zero"),
                    "Exception message does not start with \"Size must be equal or greater than zero\"");
        }
    }

    @Test
    public void testToByteArray_InputStream_Size() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final byte[] out = IOUtils.toByteArray(fin, testFile.length());
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size: out.length=" + out.length + "!=" + FILE_SIZE);
            TestUtils.assertEqualContent(out, testFile);
        }
    }

    @Test
    public void testToByteArray_InputStream_SizeIllegal() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final IOException exc = assertThrows(IOException.class, () -> IOUtils.toByteArray(fin, testFile.length() + 1),
                    "Should have failed with IOException");
            assertTrue(exc.getMessage().startsWith("Unexpected read size"), "Exception message does not start with \"Unexpected read size\"");
        }
    }

    @Test
    public void testToByteArray_InputStream_SizeLong() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final IllegalArgumentException exc = assertThrows(IllegalArgumentException.class, () -> IOUtils.toByteArray(fin, (long) Integer.MAX_VALUE + 1),
                    "Should have failed with IllegalArgumentException");
            assertTrue(exc.getMessage().startsWith("Size cannot be greater than Integer max value"),
                    "Exception message does not start with \"Size cannot be greater than Integer max value\"");
        }
    }

    @Test
    public void testToByteArray_InputStream_SizeOne() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final byte[] out = IOUtils.toByteArray(fin, 1);
            assertNotNull(out, "Out cannot be null");
            assertEquals(1, out.length, "Out length must be 1");
        }
    }

    @Test
    public void testToByteArray_InputStream_SizeZero() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final byte[] out = IOUtils.toByteArray(fin, 0);
            assertNotNull(out, "Out cannot be null");
            assertEquals(0, out.length, "Out length must be 0");
        }
    }

    @Test
    public void testToByteArray_Reader() throws IOException {
        final String charsetName = UTF_8;
        final byte[] expected = charsetName.getBytes(charsetName);
        byte[] actual = IOUtils.toByteArray(new InputStreamReader(new ByteArrayInputStream(expected)));
        assertArrayEquals(expected, actual);
        actual = IOUtils.toByteArray(new InputStreamReader(new ByteArrayInputStream(expected)), charsetName);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testToByteArray_String() throws Exception {
        try (Reader fin = Files.newBufferedReader(testFilePath)) {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            final String str = IOUtils.toString(fin);

            final byte[] out = IOUtils.toByteArray(str);
            assertEqualContent(str.getBytes(), out);
        }
    }

    @Test
    public void testToByteArray_URI() throws Exception {
        final URI url = testFile.toURI();
        final byte[] actual = IOUtils.toByteArray(url);
        assertEquals(FILE_SIZE, actual.length);
    }

    @Test
    public void testToByteArray_URL() throws Exception {
        final URL url = testFile.toURI().toURL();
        final byte[] actual = IOUtils.toByteArray(url);
        assertEquals(FILE_SIZE, actual.length);
    }

    @Test
    public void testToByteArray_URLConnection() throws Exception {
        final byte[] actual;
        try (CloseableURLConnection urlConnection = CloseableURLConnection.open(testFile.toURI())) {
            actual = IOUtils.toByteArray(urlConnection);
        }
        assertEquals(FILE_SIZE, actual.length);
    }

    @Test
    public void testToCharArray_InputStream() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final char[] out = IOUtils.toCharArray(fin);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all chars were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, testFile);
        }
    }

    @Test
    public void testToCharArray_InputStream_CharsetName() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final char[] out = IOUtils.toCharArray(fin, UTF_8);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all chars were read");
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, testFile);
        }
    }

    @Test
    public void testToCharArray_Reader() throws Exception {
        try (Reader fr = Files.newBufferedReader(testFilePath)) {
            final char[] out = IOUtils.toCharArray(fr);
            assertNotNull(out);
            assertEquals(FILE_SIZE, out.length, "Wrong output size");
            TestUtils.assertEqualContent(out, testFile);
        }
    }

    /**
     * Test for {@link IOUtils#toInputStream(CharSequence)} and {@link IOUtils#toInputStream(CharSequence, String)}.
     * Note, this test utilizes on {@link IOUtils#toByteArray(InputStream)} and so relies on
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
        inStream = IOUtils.toInputStream(csq, UTF_8);
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(csq.toString().getBytes(StandardCharsets.UTF_8), bytes);
    }

    /**
     * Test for {@link IOUtils#toInputStream(String)} and {@link IOUtils#toInputStream(String, String)}. Note, this test
     * utilizes on {@link IOUtils#toByteArray(InputStream)} and so relies on
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
        inStream = IOUtils.toInputStream(str, UTF_8);
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(str.getBytes(StandardCharsets.UTF_8), bytes);
    }

    @Test
    public void testToString_ByteArray() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final byte[] in = IOUtils.toByteArray(fin);
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            final String str = IOUtils.toString(in);
            assertEqualContent(in, str.getBytes());
        }
    }

    @Test
    public void testToString_InputStream() throws Exception {
        try (InputStream fin = Files.newInputStream(testFilePath)) {
            final String out = IOUtils.toString(fin);
            assertNotNull(out);
            assertEquals(0, fin.available(), "Not all bytes were read");
            assertEquals(FILE_SIZE, out.length(), "Wrong output size");
        }
    }

    @Test
    public void testToString_InputStreamSupplier() throws Exception {
        final String out = IOUtils.toString(() -> Files.newInputStream(testFilePath), Charset.defaultCharset());
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
        assertNull(IOUtils.toString(null, Charset.defaultCharset(), () -> null));
        assertNull(IOUtils.toString(() -> null, Charset.defaultCharset(), () -> null));
        assertEquals("A", IOUtils.toString(null, Charset.defaultCharset(), () -> "A"));
    }

    @Test
    public void testToString_Reader() throws Exception {
        try (Reader fin = Files.newBufferedReader(testFilePath)) {
            final String out = IOUtils.toString(fin);
            assertNotNull(out);
            assertEquals(FILE_SIZE, out.length(), "Wrong output size");
        }
    }

    @Test
    public void testToString_URI() throws Exception {
        final URI url = testFile.toURI();
        final String out = IOUtils.toString(url);
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
    }

    private void testToString_URI(final String encoding) throws Exception {
        final URI uri = testFile.toURI();
        final String out = IOUtils.toString(uri, encoding);
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
    }

    @Test
    public void testToString_URI_CharsetName() throws Exception {
        testToString_URI(StandardCharsets.US_ASCII.name());
    }

    @Test
    public void testToString_URI_CharsetNameNull() throws Exception {
        testToString_URI(null);
    }

    @Test
    public void testToString_URL() throws Exception {
        final URL url = testFile.toURI().toURL();
        final String out = IOUtils.toString(url);
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
    }

    private void testToString_URL(final String encoding) throws Exception {
        final URL url = testFile.toURI().toURL();
        final String out = IOUtils.toString(url, encoding);
        assertNotNull(out);
        assertEquals(FILE_SIZE, out.length(), "Wrong output size");
    }

    @Test
    public void testToString_URL_CharsetName() throws Exception {
        testToString_URL(StandardCharsets.US_ASCII.name());
    }

    @Test
    public void testToString_URL_CharsetNameNull() throws Exception {
        testToString_URL(null);
    }

    /**
     * IO-764 IOUtils.write() throws NegativeArraySizeException while writing big strings.
     * <pre>
     * java.lang.OutOfMemoryError: Java heap space
     *     at java.lang.StringCoding.encode(StringCoding.java:350)
     *     at java.lang.String.getBytes(String.java:941)
     *     at org.apache.commons.io.IOUtils.write(IOUtils.java:3367)
     *     at org.apache.commons.io.IOUtilsTest.testBigString(IOUtilsTest.java:1659)
     * </pre>
     */
    @Test
    public void testWriteBigString() throws IOException {
        // 3_000_000 is a size that we can allocate for the test string with Java 8 on the command line as:
        // mvn clean test -Dtest=IOUtilsTest -DtestBigString=3000000
        // 6_000_000 failed with the above
        //
        // TODO Can we mock the test string for this test to pretend to be larger?
        // Mocking the length seems simple but how about the data?
        final int repeat = Integer.getInteger("testBigString", 3_000_000);
        final String data;
        try {
            data = StringUtils.repeat("\uD83D", repeat);
        } catch (final OutOfMemoryError e) {
            System.err.printf("Don't fail the test if we cannot build the fixture, just log, fixture size = %,d%n.", repeat);
            e.printStackTrace();
            return;
        }
        try (CountingOutputStream os = new CountingOutputStream(NullOutputStream.INSTANCE)) {
            IOUtils.write(data, os, StandardCharsets.UTF_8);
            assertEquals(repeat, os.getByteCount());
        }
    }

    @Test
    public void testWriteLines() throws IOException {
        final String[] data = {"The", "quick"};
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.writeLines(Arrays.asList(data), "\n", out, StandardCharsets.UTF_16.name());
        final String result = new String(out.toByteArray(), StandardCharsets.UTF_16);
        assertEquals("The\nquick\n", result);
    }

    @Test
    public void testWriteLittleString() throws IOException {
        final String data = "\uD83D";
        // White-box test to check that not closing the internal channel is not a problem.
        for (int i = 0; i < 1_000_000; i++) {
            try (CountingOutputStream os = new CountingOutputStream(NullOutputStream.INSTANCE)) {
                IOUtils.write(data, os, StandardCharsets.UTF_8);
                assertEquals(data.length(), os.getByteCount());
            }
        }
    }

}
