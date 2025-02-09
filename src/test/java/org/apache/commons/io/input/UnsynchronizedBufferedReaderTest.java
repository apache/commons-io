/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link UnsynchronizedBufferedReader}.
 * <p>
 * Provenance: Apache Harmony {@code BufferedReaderTest}, copied, and modified.
 * </p>
 */
public class UnsynchronizedBufferedReaderTest {

    private UnsynchronizedBufferedReader br;

    private final String testString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\n"
            + "Test_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\n"
            + "Test_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\n"
            + "Test_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\n"
            + "Test_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\n"
            + "Test_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\n"
            + "Test_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\n"
            + "Test_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\n"
            + "Test_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\n"
            + "Test_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\n"
            + "Test_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\n"
            + "Test_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\n"
            + "Test_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\n"
            + "Test_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\n"
            + "Test_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\n"
            + "Test_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\n"
            + "Test_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\n"
            + "Test_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\n"
            + "Test_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\n"
            + "Test_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\n"
            + "Test_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\n"
            + "Test_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\n"
            + "Test_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\n"
            + "Test_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\n"
            + "Test_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\n"
            + "Test_java_util_tm\nTest_java_util_Vector\n";

    /**
     * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
     */
    @AfterEach
    protected void afterEach() {
        IOUtils.closeQuietly(br);
    }

    private void assertLines(final String input, final String... lines) throws IOException {
        assertReadLines(input, lines);
        assertPeek(input, lines);
    }

    private void assertPeek(final String input, final String... lines) throws IOException {
        try (UnsynchronizedBufferedReader bufferedReader = new UnsynchronizedBufferedReader(new StringReader(input))) {
            for (final String line : lines) {
                // all
                final char[] bufAFull = new char[line.length()];
                assertEquals(bufAFull.length, bufferedReader.peek(bufAFull));
                assertArrayEquals(line.toCharArray(), bufAFull);
                if (!line.isEmpty()) {
                    // one
                    assertEquals(line.charAt(0), bufferedReader.peek());
                    // array
                    for (int peekLen = 0; peekLen < line.length(); peekLen++) {
                        assertPeekArray(bufferedReader, peekLen, line);
                    }
                }
                // move test to the next fixture
                assertEquals(line, bufferedReader.readLine());
            }
            assertNull(bufferedReader.readLine());
        }
    }

    private void assertPeekArray(final UnsynchronizedBufferedReader bufferedReader, final int peekLen, final String line) throws IOException {
        final char[] expectedBuf = new char[peekLen];
        final int srcPeekLen = Math.min(peekLen, line.length());
        line.getChars(0, srcPeekLen, expectedBuf, 0);
        final char[] actualBuf = new char[peekLen];
        final Supplier<String> msg = () -> String.format("len=%,d, line='%s'", peekLen, line);
        assertEquals(actualBuf.length, bufferedReader.peek(actualBuf), msg);
        assertArrayEquals(expectedBuf, actualBuf, msg);
    }

    private void assertReadLines(final String input, final String... lines) throws IOException {
        try (UnsynchronizedBufferedReader bufferedReader = new UnsynchronizedBufferedReader(new StringReader(input))) {
            for (final String line : lines) {
                assertEquals(line, bufferedReader.readLine());
            }
            assertNull(bufferedReader.readLine());
        }
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#close()}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testClose() throws IOException {
        // Test for method void UnsynchronizedBufferedReader.close()
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        br.close();
        assertThrows(IOException.class, br::read);
    }

    @Test
    public void testEmptyInput() throws Exception {
        try (UnsynchronizedBufferedReader br = new UnsynchronizedBufferedReader(new StringReader(""))) {
            assertEquals(EOF, br.read());
            assertEquals(EOF, br.peek());
            assertNull(br.readLine());
            assertEquals(0, br.read(new char[10], 0, 0));
        }
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#mark(int)}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testMark() throws IOException {
        // Test for method void UnsynchronizedBufferedReader.mark(int)
        char[] buf = null;
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        br.skip(500);
        br.mark(1000);
        br.skip(250);
        br.reset();
        buf = new char[testString.length()];
        br.read(buf, 0, 500);
        assertTrue(testString.substring(500, 1000).equals(new String(buf, 0, 500)));

        br = new UnsynchronizedBufferedReader(new StringReader(testString), 800);
        br.skip(500);
        br.mark(250);
        br.read(buf, 0, 1000);
        assertThrows(IOException.class, br::reset);

        final char[] chars = new char[256];
        for (int i = 0; i < 256; i++) {
            chars[i] = (char) i;
        }

        try (Reader in = new UnsynchronizedBufferedReader(new StringReader(new String(chars)), 12)) {
            in.skip(6);
            in.mark(14);
            in.read(new char[14], 0, 14);
            in.reset();
            assertTrue(in.read() == (char) 6 && in.read() == (char) 7);
        }
        try (Reader in = new UnsynchronizedBufferedReader(new StringReader(new String(chars)), 12)) {
            in.skip(6);
            in.mark(8);
            in.skip(7);
            in.reset();
            assertTrue(in.read() == (char) 6 && in.read() == (char) 7);
        }
        try (UnsynchronizedBufferedReader br = new UnsynchronizedBufferedReader(new StringReader("01234"), 2)) {
            br.mark(3);
            final char[] carray = new char[3];
            final int result = br.read(carray);
            assertEquals(3, result);
            assertEquals('0', carray[0]);
            assertEquals('1', carray[1]);
            assertEquals('2', carray[2]);
            assertEquals('3', br.read());
        }
        try (UnsynchronizedBufferedReader br = new UnsynchronizedBufferedReader(new StringReader("01234"), 2)) {
            br.mark(3);
            final char[] carray = new char[4];
            final int result = br.read(carray);
            assertEquals(4, result);
            assertEquals('0', carray[0]);
            assertEquals('1', carray[1]);
            assertEquals('2', carray[2]);
            assertEquals('3', carray[3]);
            assertEquals('4', br.read());
            assertEquals(-1, br.read());
        }
        try (UnsynchronizedBufferedReader reader = new UnsynchronizedBufferedReader(new StringReader("01234"))) {
            reader.mark(Integer.MAX_VALUE);
            reader.read();
        }
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#markSupported()}.
     */
    @Test
    public void testMarkSupported() {
        // Test for method boolean UnsynchronizedBufferedReader.markSupported()
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        assertTrue(br.markSupported());
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#peek()}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testPeek() throws IOException {
        // Test for method int UnsynchronizedBufferedReader.read()
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        final int p = br.peek();
        assertEquals(testString.charAt(0), p);
        final int r = br.read();
        assertEquals(testString.charAt(0), r);
        br = new UnsynchronizedBufferedReader(new StringReader(new String(new char[] { '\u8765' })));
        assertEquals(br.peek(), '\u8765');
        assertEquals(br.read(), '\u8765');
        // chars '\0'...'\255'
        final char[] chars = new char[256];
        for (int i = 0; i < 256; i++) {
            chars[i] = (char) i;
        }
        try (UnsynchronizedBufferedReader in = new UnsynchronizedBufferedReader(new StringReader(new String(chars)), 12)) {
            assertEquals(0, in.peek()); // Fill the buffer
            assertEquals(0, in.read()); // Fill the buffer
            final char[] buf = new char[14];
            in.read(buf, 0, 14); // Read greater than the buffer
            assertTrue(new String(buf).equals(new String(chars, 1, 14)));
            assertEquals(15, in.peek()); // Check next byte
            assertEquals(15, in.read()); // Check next byte
        }
        //
        // regression test for HARMONY-841
        try (UnsynchronizedBufferedReader reader = new UnsynchronizedBufferedReader(new CharArrayReader(new char[5], 1, 0), 2)) {
            assertEquals(reader.peek(), -1);
            assertEquals(reader.read(), -1);
        }
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#peek()}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testPeekArray() throws IOException {
        // Test for method int UnsynchronizedBufferedReader.read()
        final char[] peekBuf1 = new char[1];
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        assertEquals(peekBuf1.length, br.peek(peekBuf1));
        assertEquals(testString.charAt(0), peekBuf1[0]);
        final int r = br.read();
        assertEquals(testString.charAt(0), r);
        br = new UnsynchronizedBufferedReader(new StringReader(new String(new char[] { '\u8765' })));
        assertEquals(peekBuf1.length, br.peek(peekBuf1));
        assertEquals(peekBuf1[0], '\u8765');
        assertEquals(br.read(), '\u8765');
        // chars '\0'...'\255'
        final char[] chars = new char[256];
        for (int i = 0; i < 256; i++) {
            chars[i] = (char) i;
        }
        try (UnsynchronizedBufferedReader in = new UnsynchronizedBufferedReader(new StringReader(new String(chars)), 12)) {
            assertEquals(peekBuf1.length, in.peek(peekBuf1)); // Fill the buffer
            assertEquals(peekBuf1[0], 0);
            assertEquals(0, in.read()); // Fill the buffer
            final char[] peekBuf14 = new char[14];
            assertEquals(peekBuf14.length, in.peek(peekBuf14)); // Peek greater than the buffer
            assertTrue(new String(peekBuf14).equals(new String(chars, 1, 14)));
            final char[] buf = new char[14];
            in.read(buf, 0, 14); // Read greater than the buffer
            assertTrue(new String(buf).equals(new String(chars, 1, 14)));
            assertEquals(peekBuf1.length, in.peek(peekBuf1)); // Check next byte
            assertEquals(15, peekBuf1[0]);
            assertEquals(15, in.read()); // Check next byte
        }
        //
        // regression test for HARMONY-841
        try (UnsynchronizedBufferedReader reader = new UnsynchronizedBufferedReader(new CharArrayReader(new char[5], 1, 0), 2)) {
            assertEquals(reader.peek(), -1);
            assertEquals(reader.read(), -1);
        }
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#read()}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testRead() throws IOException {
        // Test for method int UnsynchronizedBufferedReader.read()
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        final int r = br.read();
        assertEquals(testString.charAt(0), r);
        br = new UnsynchronizedBufferedReader(new StringReader(new String(new char[] { '\u8765' })));
        assertEquals(br.read(), '\u8765');
        //
        final char[] chars = new char[256];
        for (int i = 0; i < 256; i++) {
            chars[i] = (char) i;
        }
        try (Reader in = new UnsynchronizedBufferedReader(new StringReader(new String(chars)), 12)) {
            assertEquals(0, in.read()); // Fill the buffer
            final char[] buf = new char[14];
            in.read(buf, 0, 14); // Read greater than the buffer
            assertTrue(new String(buf).equals(new String(chars, 1, 14)));
            assertEquals(15, in.read()); // Check next byte
        }
        //
        // regression test for HARMONY-841
        try (Reader reader = new UnsynchronizedBufferedReader(new CharArrayReader(new char[5], 1, 0), 2)) {
            assertEquals(reader.read(), -1);
        }
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#read(char[], int, int)}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testReadArray() throws IOException {
        final char[] ca = new char[2];
        try (UnsynchronizedBufferedReader toRet = new UnsynchronizedBufferedReader(new InputStreamReader(new ByteArrayInputStream(new byte[0])))) {
            /* Null buffer should throw NPE even when len == 0 */
            assertThrows(NullPointerException.class, () -> toRet.read(null, 1, 0));
            toRet.close();
            assertThrows(IOException.class, () -> toRet.read(null, 1, 0));
            /* Closed reader should throw IOException reading zero bytes */
            assertThrows(IOException.class, () -> toRet.read(ca, 0, 0));
            /*
             * Closed reader should throw IOException in preference to index out of bounds
             */
            // Read should throw IOException before
            // ArrayIndexOutOfBoundException
            assertThrows(IOException.class, () -> toRet.read(ca, 1, 5));
        }
        // Test to ensure that a drained stream returns 0 at EOF
        try (UnsynchronizedBufferedReader toRet2 = new UnsynchronizedBufferedReader(new InputStreamReader(new ByteArrayInputStream(new byte[2])))) {
            assertEquals(2, toRet2.read(ca, 0, 2));
            assertEquals(-1, toRet2.read(ca, 0, 2));
            assertEquals(0, toRet2.read(ca, 0, 0));
        }

        // Test for method int UnsynchronizedBufferedReader.read(char [], int, int)
        final char[] buf = new char[testString.length()];
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        br.read(buf, 50, 500);
        assertTrue(new String(buf, 50, 500).equals(testString.substring(0, 500)));

        try (UnsynchronizedBufferedReader bufin = new UnsynchronizedBufferedReader(new Reader() {
            int size = 2;
            int pos;

            char[] contents = new char[size];

            @Override
            public void close() throws IOException {
                // Empty
            }

            @Override
            public int read() throws IOException {
                if (pos >= size) {
                    throw new IOException("Read past end of data");
                }
                return contents[pos++];
            }

            @Override
            public int read(final char[] buf, final int off, final int len) throws IOException {
                if (pos >= size) {
                    throw new IOException("Read past end of data");
                }
                int toRead = len;
                if (toRead > size - pos) {
                    toRead = size - pos;
                }
                System.arraycopy(contents, pos, buf, off, toRead);
                pos += toRead;
                return toRead;
            }

            @Override
            public boolean ready() throws IOException {
                return size - pos > 0;
            }
        })) {
            bufin.read();
            final int result = bufin.read(new char[2], 0, 2);
            assertEquals(result, 1);
        }
        // regression for HARMONY-831
        try (Reader reader = new UnsynchronizedBufferedReader(new PipedReader(), 9)) {
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(new char[] {}, 7, 0));
        }

        // Regression for HARMONY-54
        final char[] ch = {};
        @SuppressWarnings("resource")
        final UnsynchronizedBufferedReader reader = new UnsynchronizedBufferedReader(new CharArrayReader(ch));
        // Check exception thrown when the reader is open.
        assertThrows(NullPointerException.class, () -> reader.read(null, 1, 0));

        // Now check IOException is thrown in preference to
        // NullPointerexception when the reader is closed.
        reader.close();
        assertThrows(IOException.class, () -> reader.read(null, 1, 0));

        // And check that the IOException is thrown before
        // ArrayIndexOutOfBoundException
        assertThrows(IOException.class, () -> reader.read(ch, 0, 42));
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#read(char[], int, int)}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testReadArrayException() throws IOException {
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        final char[] nullCharArray = null;
        final char[] charArray = testString.toCharArray();
        assertThrows(IndexOutOfBoundsException.class, () -> br.read(nullCharArray, -1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> br.read(nullCharArray, -1, 0));
        assertThrows(NullPointerException.class, () -> br.read(nullCharArray, 0, -1));
        assertThrows(NullPointerException.class, () -> br.read(nullCharArray, 0, 0));
        assertThrows(NullPointerException.class, () -> br.read(nullCharArray, 0, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> br.read(charArray, -1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> br.read(charArray, -1, 0));

        br.read(charArray, 0, 0);
        br.read(charArray, 0, charArray.length);
        br.read(charArray, charArray.length, 0);

        assertThrows(IndexOutOfBoundsException.class, () -> br.read(charArray, charArray.length + 1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> br.read(charArray, charArray.length + 1, 1));

        br.close();

        assertThrows(IOException.class, () -> br.read(nullCharArray, -1, -1));
        assertThrows(IOException.class, () -> br.read(charArray, -1, 0));
        assertThrows(IOException.class, () -> br.read(charArray, 0, -1));
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#readLine()}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testReadLine() throws IOException {
        // Test for method java.lang.String UnsynchronizedBufferedReader.readLine()
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        final String r = br.readLine();
        assertEquals("Test_All_Tests", r);
    }

    /**
     * The spec says that BufferedReader.readLine() considers only "\r", "\n" and "\r\n" to be line separators. We must not permit additional separator
     * characters.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testReadLineIgnoresEbcdic85Characters() throws IOException {
        assertLines("A\u0085B", "A\u0085B");
    }

    @Test
    public void testReadLineSeparators() throws IOException {
        assertLines("A\nB\nC", "A", "B", "C");
        assertLines("A\rB\rC", "A", "B", "C");
        assertLines("A\r\nB\r\nC", "A", "B", "C");
        assertLines("A\n\rB\n\rC", "A", "", "B", "", "C");
        assertLines("A\n\nB\n\nC", "A", "", "B", "", "C");
        assertLines("A\r\rB\r\rC", "A", "", "B", "", "C");
        assertLines("A\n\n", "A", "");
        assertLines("A\n\r", "A", "");
        assertLines("A\r\r", "A", "");
        assertLines("A\r\n", "A");
        assertLines("A\r\n\r\n", "A", "");
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#ready()}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testReady() throws IOException {
        // Test for method boolean UnsynchronizedBufferedReader.ready()
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        assertTrue(br.ready());
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#reset()}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testReset() throws IOException {
        // Test for method void UnsynchronizedBufferedReader.reset()
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        br.skip(500);
        br.mark(900);
        br.skip(500);
        br.reset();
        final char[] buf = new char[testString.length()];
        br.read(buf, 0, 500);
        assertTrue(testString.substring(500, 1000).equals(new String(buf, 0, 500)));
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        br.skip(500);
        assertThrows(IOException.class, br::reset);
    }

    @Test
    public void testReset_IOException() throws Exception {
        final int[] expected = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', -1 };
        br = new UnsynchronizedBufferedReader(new StringReader("1234567890"), 9);
        br.mark(9);
        for (int i = 0; i < 11; i++) {
            assertEquals(expected[i], br.read());
        }
        assertThrows(IOException.class, br::reset);
        for (int i = 0; i < 11; i++) {
            assertEquals(-1, br.read());
        }

        br = new UnsynchronizedBufferedReader(new StringReader("1234567890"));
        br.mark(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(expected[i], br.read());
        }
        br.reset();
        for (int i = 0; i < 11; i++) {
            assertEquals(expected[i], br.read());
        }
    }

    /**
     * Tests {@link UnsynchronizedBufferedReader#skip(long)}.
     *
     * @throws IOException test failure.
     */
    @Test
    public void testSkip() throws IOException {
        // Test for method long UnsynchronizedBufferedReader.skip(long)
        br = new UnsynchronizedBufferedReader(new StringReader(testString));
        br.skip(500);
        final char[] buf = new char[testString.length()];
        br.read(buf, 0, 500);
        assertTrue(testString.substring(500, 1000).equals(new String(buf, 0, 500)));
    }
}
