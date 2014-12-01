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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WindowsLineEndingInputStreamTest {
    @Test
    public void simpleString() throws Exception {
        assertEquals( "abc\r\n", roundtrip( "abc" ) );
    }

    @Test
    public void inTheMiddleOfTheLine() throws Exception {
        assertEquals( "a\r\nbc\r\n", roundtrip( "a\r\nbc" ) );
    }

    @Test
    public void multipleBlankLines() throws Exception {
        assertEquals( "a\r\n\r\nbc\r\n", roundtrip( "a\r\n\r\nbc" ) );
    }

    @Test
    public void twoLinesAtEnd() throws Exception {
        assertEquals( "a\r\n\r\n", roundtrip( "a\r\n\r\n" ) );
    }

    @Test
    public void linuxLinefeeds() throws Exception {
        final String roundtrip = roundtrip( "ab\nc", false );
        assertEquals( "ab\r\nc", roundtrip );
    }


    @Test
    public void malformed() throws Exception {
        assertEquals( "a\rbc", roundtrip( "a\rbc", false ) );
    }

    @Test
    public void retainLineFeed() throws Exception {
        assertEquals( "a\r\n\r\n", roundtrip( "a\r\n\r\n", false ) );
        assertEquals( "a", roundtrip( "a", false ) );
    }

    private String roundtrip( String msg ) throws IOException {
        return roundtrip( msg, true );
    }

    private String roundtrip( String msg, boolean ensure ) throws IOException {
        ByteArrayInputStream baos = new ByteArrayInputStream( msg.getBytes( "UTF-8" ) );
        WindowsLineEndingInputStream lf = new WindowsLineEndingInputStream( baos, ensure );
        byte[] buf = new byte[100];
        final int read = lf.read( buf );
        lf.close();
        return new String( buf, 0, read, "UTF-8" );
    }
}