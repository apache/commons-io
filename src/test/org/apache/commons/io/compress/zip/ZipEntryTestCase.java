/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.zip;

import java.util.NoSuchElementException;
import junit.framework.TestCase;
import org.apache.excalibur.zip.AsiExtraField;
import org.apache.excalibur.zip.UnrecognizedExtraField;
import org.apache.excalibur.zip.ZipEntry;
import org.apache.excalibur.zip.ZipExtraField;
import org.apache.excalibur.zip.ZipShort;

/**
 * JUnit testcases ZipEntry.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 */
public class ZipEntryTestCase
    extends TestCase
{
    public ZipEntryTestCase( final String name )
    {
        super( name );
    }

    /**
     * test handling of extra fields
     */
    public void testExtraFields()
    {
        final AsiExtraField field = createField();
        final UnrecognizedExtraField extraField = createExtraField();

        final ZipEntry entry = new ZipEntry( "test/" );
        entry.setExtraFields( new ZipExtraField[]{field, extraField} );
        final byte[] data1 = entry.getExtra();
        ZipExtraField[] result = entry.getExtraFields();
        assertEquals( "first pass", 2, result.length );
        assertSame( field, result[ 0 ] );
        assertSame( extraField, result[ 1 ] );

        UnrecognizedExtraField u2 = new UnrecognizedExtraField();
        u2.setHeaderId( new ZipShort( 1 ) );
        u2.setLocalFileDataData( new byte[]{1} );

        entry.addExtraField( u2 );
        byte[] data2 = entry.getExtra();
        result = entry.getExtraFields();
        assertEquals( "second pass", 2, result.length );
        assertSame( field, result[ 0 ] );
        assertSame( u2, result[ 1 ] );
        assertEquals( "length second pass", data1.length + 1, data2.length );

        UnrecognizedExtraField u3 = new UnrecognizedExtraField();
        u3.setHeaderId( new ZipShort( 2 ) );
        u3.setLocalFileDataData( new byte[]{1} );
        entry.addExtraField( u3 );
        result = entry.getExtraFields();
        assertEquals( "third pass", 3, result.length );

        entry.removeExtraField( new ZipShort( 1 ) );
        byte[] data3 = entry.getExtra();
        result = entry.getExtraFields();
        assertEquals( "fourth pass", 2, result.length );
        assertSame( field, result[ 0 ] );
        assertSame( u3, result[ 1 ] );
        assertEquals( "length fourth pass", data2.length, data3.length );

        try
        {
            entry.removeExtraField( new ZipShort( 1 ) );
            fail( "should be no such element" );
        }
        catch( final NoSuchElementException nse )
        {
        }
    }

    private UnrecognizedExtraField createExtraField()
    {
        UnrecognizedExtraField extraField = new UnrecognizedExtraField();
        extraField.setHeaderId( new ZipShort( 1 ) );
        extraField.setLocalFileDataData( new byte[ 0 ] );
        return extraField;
    }

    private AsiExtraField createField()
    {
        final AsiExtraField field = new AsiExtraField();
        field.setDirectory( true );
        field.setMode( 0755 );
        return field;
    }
}
