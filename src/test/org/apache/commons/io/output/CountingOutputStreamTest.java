/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;


/**
 * @author Henri Yandell (bayard at apache dot org)
 * @version $Revision$ $Date$
 */

public class CountingOutputStreamTest extends TestCase {

    public CountingOutputStreamTest(String name) {
        super(name);
    }

    public void testCounting() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CountingOutputStream cos = new CountingOutputStream(baos);

        for(int i = 0; i < 20; i++) {
            cos.write(i);
        }
        assertByteArrayEquals("CountingOutputStream.write(int)", baos.toByteArray(), 0, 20);
        assertEquals("CountingOutputStream.getCount()", cos.getCount(), 20);

        byte[] array = new byte[10];
        for(int i = 20; i < 30; i++) {
            array[i-20] = (byte)i;
        }
        cos.write(array);
        assertByteArrayEquals("CountingOutputStream.write(byte[])", baos.toByteArray(), 0, 30);
        assertEquals("CountingOutputStream.getCount()", cos.getCount(), 30);

        for(int i = 25; i < 35; i++) {
            array[i-25] = (byte)i;
        }
        cos.write(array, 5, 5);
        assertByteArrayEquals("CountingOutputStream.write(byte[], int, int)", baos.toByteArray(), 0, 35);
        assertEquals("CountingOutputStream.getCount()", cos.getCount(), 35);

        int count = cos.resetCount();
        assertEquals("CountingOutputStream.resetCount()", count, 35);

        for(int i = 0; i < 10; i++) {
            cos.write(i);
        }
        assertByteArrayEquals("CountingOutputStream.write(int)", baos.toByteArray(), 35, 45);
        assertEquals("CountingOutputStream.getCount()", cos.getCount(), 10);

    }

    private void assertByteArrayEquals(String msg, byte[] array, int start, int end) {
        for (int i = start; i < end; i++) {
            assertEquals(msg+": array[" + i + "] mismatch", array[i], i-start);
        }
    }

}
