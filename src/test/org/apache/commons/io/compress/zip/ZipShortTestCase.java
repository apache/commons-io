/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.zip;

import junit.framework.TestCase;

/**
 * JUnit 3 testcases for org.apache.tools.zip.ZipShort.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 */
public class ZipShortTestCase
    extends TestCase
{
    public ZipShortTestCase( String name )
    {
        super( name );
    }

    /**
     * Test conversion to bytes.
     */
    public void testToBytes()
    {
        final ZipShort zipShort = new ZipShort( 0x1234 );
        byte[] result = zipShort.getBytes();
        assertEquals( "length getBytes", 2, result.length );
        assertEquals( "first byte getBytes", 0x34, result[ 0 ] );
        assertEquals( "second byte getBytes", 0x12, result[ 1 ] );
    }

    /**
     * Test conversion from bytes.
     */
    public void testFromBytes()
    {
        byte[] val = new byte[]{0x34, 0x12};
        final ZipShort zipShort = new ZipShort( val );
        assertEquals( "value from bytes", 0x1234, zipShort.getValue() );
    }

    /**
     * Test the contract of the equals method.
     */
    public void testEquals()
    {
        final ZipShort zipShort = new ZipShort( 0x1234 );
        final ZipShort zipShort2 = new ZipShort( 0x1234 );
        final ZipShort zipShort3 = new ZipShort( 0x5678 );

        assertTrue( "reflexive", zipShort.equals( zipShort ) );

        assertTrue( "works", zipShort.equals( zipShort2 ) );
        assertTrue( "works, part two", !zipShort.equals( zipShort3 ) );

        assertTrue( "symmetric", zipShort2.equals( zipShort ) );

        assertTrue( "null handling", !zipShort.equals( null ) );
        assertTrue( "non ZipShort handling", !zipShort.equals( new Integer( 0x1234 ) ) );
    }

    /**
     * Test sign handling.
     */
    public void testSign()
    {
        final ZipShort zipShort = new ZipShort( new byte[]{(byte)0xFF, (byte)0xFF} );
        assertEquals( 0x0000FFFF, zipShort.getValue() );
    }
}
