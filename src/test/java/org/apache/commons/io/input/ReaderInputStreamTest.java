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

import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import junit.framework.TestCase;

public class ReaderInputStreamTest extends TestCase {
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
        ReaderInputStream in = new ReaderInputStream(new StringReader(testString), charsetName);
        for (int i=0; i<bytes.length; i++) {
            int read = in.read();
            assertTrue(read >= 0);
            assertTrue(read <= 255);
            assertEquals(bytes[i], (byte)read);
        }
        assertEquals(-1, in.read());
    }
    
    private void testWithBufferedRead(String testString, String charsetName) throws IOException {
        byte[] expected = testString.getBytes(charsetName);
        ReaderInputStream in = new ReaderInputStream(new StringReader(testString), charsetName);
        byte[] buffer = new byte[128];
        int offset = 0;
        while (true) {
            int bufferOffset = random.nextInt(64);
            int bufferLength = random.nextInt(64);
            int read = in.read(buffer, bufferOffset, bufferLength);
            if (read == -1) {
                assertEquals(offset, expected.length);
                break;
            } else {
                assertTrue(read <= bufferLength);
                while (read > 0) {
                    assertTrue(offset < expected.length);
                    assertEquals(expected[offset], buffer[bufferOffset]);
                    offset++;
                    bufferOffset++;
                    read--;
                }
            }
        }
    }
    
    public void testUTF8WithSingleByteRead() throws IOException {
        testWithSingleByteRead(TEST_STRING, "UTF-8");
    }
    
    public void testLargeUTF8WithSingleByteRead() throws IOException {
        testWithSingleByteRead(LARGE_TEST_STRING, "UTF-8");
    }
    
    public void testUTF8WithBufferedRead() throws IOException {
        testWithBufferedRead(TEST_STRING, "UTF-8");
    }
    
    public void testLargeUTF8WithBufferedRead() throws IOException {
        testWithBufferedRead(LARGE_TEST_STRING, "UTF-8");
    }
    
    public void testUTF16WithSingleByteRead() throws IOException {
        testWithSingleByteRead(TEST_STRING, "UTF-16");
    }
    
    public void testReadZero() throws Exception {
        ReaderInputStream r = new ReaderInputStream(new StringReader("test"));
        byte[] bytes = new byte[30];
        assertEquals(0, r.read(bytes, 0, 0));
    }
}
