/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.zip;

import junit.framework.TestCase;
import org.apache.excalibur.zip.ZipLong;

/**
 * JUnit 3 testcases for org.apache.tools.zip.ZipLong.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 */
public class ZipLongTestCase
    extends TestCase
{

    public ZipLongTestCase( final String name )
    {
        super( name );
    }

    /**
     * Test conversion to bytes.
     */
    public void testToBytes()
    {
        final ZipLong zipLong = new ZipLong( 0x12345678 );
        final byte[] result = zipLong.getBytes();
        assertEquals( "length getBytes", 4, result.length );
        assertEquals( "first byte getBytes", 0x78, result[ 0 ] );
        assertEquals( "second byte getBytes", 0x56, result[ 1 ] );
        assertEquals( "third byte getBytes", 0x34, result[ 2 ] );
        assertEquals( "fourth byte getBytes", 0x12, result[ 3 ] );
    }

    /**
     * Test conversion from bytes.
     */
    public void testFromBytes()
    {
        final byte[] value = new byte[]{0x78, 0x56, 0x34, 0x12};
        final ZipLong zipLong = new ZipLong( value );
        assertEquals( "value from bytes", 0x12345678, zipLong.getValue() );
    }

    /**
     * Test the contract of the equals method.
     */
    public void testEquals()
    {
        final ZipLong zipLong1 = new ZipLong( 0x12345678 );
        final ZipLong zipLong2 = new ZipLong( 0x12345678 );
        final ZipLong zipLong3 = new ZipLong( 0x87654321 );

        assertTrue( "reflexive", zipLong1.equals( zipLong1 ) );

        assertTrue( "works", zipLong1.equals( zipLong2 ) );
        assertTrue( "works, part two", !zipLong1.equals( zipLong3 ) );

        assertTrue( "symmetric", zipLong2.equals( zipLong1 ) );

        assertTrue( "null handling", !zipLong1.equals( null ) );
        assertTrue( "non ZipLong handling", !zipLong1.equals( new Integer( 0x1234 ) ) );
    }

    /**
     * Test sign handling.
     */
    public void testSign()
    {
        final ZipLong zipLong =
            new ZipLong( new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF} );
        assertEquals( 0x00000000FFFFFFFFl, zipLong.getValue() );
    }
}
