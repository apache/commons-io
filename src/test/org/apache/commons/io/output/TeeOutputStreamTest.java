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
 * @version $Revision: 1.3 $ $Date: 2004/02/23 05:02:25 $
 */

public class TeeOutputStreamTest extends TestCase {

    public TeeOutputStreamTest(String name) {
        super(name);
    }

    public void testTee() throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        TeeOutputStream tos = new TeeOutputStream(baos1, baos2);
        for(int i = 0; i < 20; i++) {
            tos.write(i);
        }
        assertByteArrayEquals("TeeOutputStream.write(int)", baos1.toByteArray(), baos2.toByteArray() );

        byte[] array = new byte[10];
        for(int i = 20; i < 30; i++) {
            array[i-20] = (byte)i;
        }
        tos.write(array);
        assertByteArrayEquals("TeeOutputStream.write(byte[])", baos1.toByteArray(), baos2.toByteArray() );

        for(int i = 25; i < 35; i++) {
            array[i-25] = (byte)i;
        }
        tos.write(array, 5, 5);
        assertByteArrayEquals("TeeOutputStream.write(byte[], int, int)", baos1.toByteArray(), baos2.toByteArray() );
    }

    private void assertByteArrayEquals(String msg, byte[] array1, byte[] array2) {
        assertEquals(msg+": array size mismatch", array1.length, array2.length);
        for(int i=0; i<array1.length; i++) {
            assertEquals(msg+": array[ " + i + "] mismatch", array1[i], array2[i]);
        }
    }

}
