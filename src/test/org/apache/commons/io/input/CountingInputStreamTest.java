/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

package org.apache.commons.io.input;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

/**
 * Tests the CountingInputStream.
 *
 * @author <a href="mailto:bayard@apache.org">Henri Yandell</a>
 */
public class CountingInputStreamTest extends TestCase {

    public CountingInputStreamTest(String name) {
        super(name);
    }

    public void testCounting() throws Exception {
        String text = "A piece of text";
        byte[] bytes = text.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        CountingInputStream cis = new CountingInputStream(bais);

        // have to declare this larger as we're going to read 
        // off the end of the stream and input stream seems 
        // to do bounds checking
        byte[] result = new byte[21];

        byte[] ba = new byte[5];
        int found = cis.read(ba);
        System.arraycopy(ba, 0, result, 0, 5);
        assertEquals( found, cis.getCount() );

        int value = cis.read();
        found++; 
        result[5] = (byte)value;
        assertEquals( found, cis.getCount() );

        found += cis.read(result, 6, 5);
        assertEquals( found, cis.getCount() );

        found += cis.read(result, 11, 10); // off the end
        assertEquals( found, cis.getCount() );

        // trim to get rid of the 6 empty values
        String textResult = new String(result).trim();
        assertEquals(textResult, text);
    }
}

