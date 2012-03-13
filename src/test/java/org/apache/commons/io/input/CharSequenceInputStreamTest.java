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
package org.apache.commons.io.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Random;

import org.junit.Test;

public class CharSequenceInputStreamTest {
    
    private static final String TEST_STRING = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";
    private static final String LARGE_TEST_STRING;
    
    static {
        StringBuilder buffer = new StringBuilder();
        for (int i=0; i<100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }
    
    private Random random = new Random();
    
    private void testWithSingleByteRead(String testString, String charsetName) throws IOException {
        byte[] bytes = testString.getBytes(charsetName);
        InputStream in = new CharSequenceInputStream(testString, charsetName, 512);
        for (byte b : bytes) {
            int read = in.read();
            assertTrue("read "+read+" >=0 ", read >= 0);
            assertTrue("read "+read+" <= 255", read <= 255);
            assertEquals("Should agree with input", b, (byte)read);
        }
        assertEquals(-1, in.read());
    }
    
    private void testWithBufferedRead(String testString, String charsetName) throws IOException {
        byte[] expected = testString.getBytes(charsetName);
        InputStream in = new CharSequenceInputStream(testString, charsetName, 512);
        byte[] buffer = new byte[128];
        int offset = 0;
        while (true) {
            int bufferOffset = random.nextInt(64);
            int bufferLength = random.nextInt(64);
            int read = in.read(buffer, bufferOffset, bufferLength);
            if (read == -1) {
                assertEquals("EOF: offset should equal length", expected.length, offset);
                break;
            } else {
                assertTrue("Read "+read+" <= "+bufferLength, read <= bufferLength);
                while (read > 0) {
                    assertTrue("offset "+offset+" < "+expected.length, offset < expected.length);
                    assertEquals("bytes should agree", expected[offset], buffer[bufferOffset]);
                    offset++;
                    bufferOffset++;
                    read--;
                }
            }
        }
    }
    
    @Test
    public void testUTF8WithSingleByteRead() throws IOException {
        testWithSingleByteRead(TEST_STRING, "UTF-8");
    }
    
    @Test
    public void testLargeUTF8WithSingleByteRead() throws IOException {
        testWithSingleByteRead(LARGE_TEST_STRING, "UTF-8");
    }
    
    @Test
    public void testUTF8WithBufferedRead() throws IOException {
        testWithBufferedRead(TEST_STRING, "UTF-8");
    }
    
    @Test
    public void testLargeUTF8WithBufferedRead() throws IOException {
        testWithBufferedRead(LARGE_TEST_STRING, "UTF-8");
    }
    
    @Test
    public void testUTF16WithSingleByteRead() throws IOException {
        testWithSingleByteRead(TEST_STRING, "UTF-16");
    }
    
    @Test
    public void testReadZero() throws Exception {
        InputStream r = new CharSequenceInputStream("test", "UTF-8");
        byte[] bytes = new byte[30];
        assertEquals(0, r.read(bytes, 0, 0));
    }

    @Test
    public void testReadZeroEmptyString() throws Exception {
        InputStream r = new CharSequenceInputStream("", "UTF-8");
        byte[] bytes = new byte[30];
        assertEquals(0, r.read(bytes, 0, 0));
    }
    
    @Test
    public void testCharsetMismatchInfiniteLoop() throws IOException {
        // Input is UTF-8 bytes: 0xE0 0xB2 0xA0
        char[] inputChars = new char[] { (char) 0xE0, (char) 0xB2, (char) 0xA0 };
        // Charset charset = Charset.forName("UTF-8"); // works
        Charset charset = Charset.forName("ASCII"); // infinite loop
        InputStream stream = new CharSequenceInputStream(new String(inputChars), charset, 512);
        try {
            while (stream.read() != -1) {
            }
        } finally {
            stream.close();
        }
    }

    @Test
    public void testSkip() throws Exception {
        InputStream r = new CharSequenceInputStream("test", "UTF-8");
        r.skip(1);
        r.skip(2);
        assertEquals('t', r.read());
        r.skip(100);
        assertEquals(-1, r.read());
    }

    @Test
    public void testMarkReset() throws Exception {
        InputStream r = new CharSequenceInputStream("test", "UTF-8");
        r.skip(2);
        r.mark(0);
        assertEquals('s', r.read());
        assertEquals('t', r.read());
        assertEquals(-1, r.read());
        r.reset();
        assertEquals('s', r.read());
        assertEquals('t', r.read());
        assertEquals(-1, r.read());
        r.reset();
        r.reset();
    }

    @Test
    public void testMarkSupported() throws Exception {
        InputStream r = new CharSequenceInputStream("test", "UTF-8");
        assertTrue(r.markSupported());
    }

}
