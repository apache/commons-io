package org.apache.commons.io.input;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UnixLineEndingInputStreamTest
{

    @Test
    public void simpleString() throws Exception {
        assertEquals( "abc\n", roundtrip( "abc" ) );
    }

    @Test
    public void inTheMiddleOfTheLine() throws Exception {
        assertEquals( "a\nbc\n", roundtrip( "a\r\nbc" ) );
    }

    @Test
    public void multipleBlankLines() throws Exception {
        assertEquals( "a\n\nbc\n", roundtrip( "a\r\n\r\nbc" ) );
    }

    @Test
    public void twoLinesAtEnd() throws Exception {
        assertEquals( "a\n\n", roundtrip( "a\r\n\r\n" ) );
    }

    @Test
    public void malformed() throws Exception {
        assertEquals( "abc", roundtrip( "a\rbc", false ) );
    }

    @Test
    public void retainLineFeed() throws Exception {
        assertEquals( "a\n\n", roundtrip( "a\r\n\r\n", false ) );
        assertEquals( "a", roundtrip( "a", false ) );
    }

    private String roundtrip( String msg ) throws IOException {
        return roundtrip( msg, true );
    }

    private String roundtrip( String msg, boolean ensure ) throws IOException {
        ByteArrayInputStream baos = new ByteArrayInputStream( msg.getBytes( "UTF-8" ) );
        UnixLineEndingInputStream lf = new UnixLineEndingInputStream( baos, ensure );
        byte[] buf = new byte[100];
        final int read = lf.read( buf );
        return new String( buf, 0, read, "UTF-8" );
    }

}