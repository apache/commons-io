/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.zip;

import junit.framework.TestCase;
import org.apache.excalibur.zip.AsiExtraField;
import org.apache.excalibur.zip.ExtraFieldUtils;
import org.apache.excalibur.zip.UnixStat;
import org.apache.excalibur.zip.UnrecognizedExtraField;
import org.apache.excalibur.zip.ZipExtraField;
import org.apache.excalibur.zip.ZipShort;

/**
 * JUnit testcases ExtraFieldUtils.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 */
public class ExtraFieldUtilsTestCase
    extends TestCase
    implements UnixStat
{
    private AsiExtraField m_field;
    private UnrecognizedExtraField m_dummy;
    private byte[] m_data;
    private byte[] m_local;

    public ExtraFieldUtilsTestCase( final String name )
    {
        super( name );
    }

    public void setUp()
    {
        m_field = new AsiExtraField();
        m_field.setMode( 0755 );
        m_field.setDirectory( true );
        m_dummy = new UnrecognizedExtraField();
        m_dummy.setHeaderId( new ZipShort( 1 ) );
        m_dummy.setLocalFileDataData( new byte[ 0 ] );
        m_dummy.setCentralDirectoryData( new byte[]{0} );

        m_local = m_field.getLocalFileDataData();
        final byte[] dummyLocal = m_dummy.getLocalFileDataData();
        m_data = new byte[ 4 + m_local.length + 4 + dummyLocal.length ];
        System.arraycopy( m_field.getHeaderID().getBytes(), 0, m_data, 0, 2 );
        System.arraycopy( m_field.getLocalFileDataLength().getBytes(), 0, m_data, 2, 2 );
        System.arraycopy( m_local, 0, m_data, 4, m_local.length );
        System.arraycopy( m_dummy.getHeaderID().getBytes(), 0, m_data,
                          4 + m_local.length, 2 );
        System.arraycopy( m_dummy.getLocalFileDataLength().getBytes(), 0, m_data,
                          4 + m_local.length + 2, 2 );
        System.arraycopy( dummyLocal, 0, m_data,
                          4 + m_local.length + 4, dummyLocal.length );

    }

    /**
     * test parser.
     */
    public void testParse() throws Exception
    {
        final ZipExtraField[] extraField = ExtraFieldUtils.parse( m_data );
        assertEquals( "number of fields", 2, extraField.length );
        assertTrue( "type field 1", extraField[ 0 ] instanceof AsiExtraField );
        assertEquals( "mode field 1", 040755,
                      ( (AsiExtraField)extraField[ 0 ] ).getMode() );
        assertTrue( "type field 2", extraField[ 1 ] instanceof UnrecognizedExtraField );
        assertEquals( "data length field 2", 0,
                      extraField[ 1 ].getLocalFileDataLength().getValue() );

        final byte[] data2 = new byte[ m_data.length - 1 ];
        System.arraycopy( m_data, 0, data2, 0, data2.length );
        try
        {
            ExtraFieldUtils.parse( data2 );
            fail( "data should be invalid" );
        }
        catch( Exception e )
        {
            assertEquals( "message",
                          "data starting at " + ( 4 + m_local.length ) + " is in unknown format",
                          e.getMessage() );
        }
    }

    /**
     * Test merge methods
     */
    public void testMerge()
    {
        final byte[] local =
            ExtraFieldUtils.mergeLocalFileDataData( new ZipExtraField[]{m_field, m_dummy} );
        assertEquals( "local length", m_data.length, local.length );
        for( int i = 0; i < local.length; i++ )
        {
            assertEquals( "local byte " + i, m_data[ i ], local[ i ] );
        }

        final byte[] dummyCentral = m_dummy.getCentralDirectoryData();
        final byte[] data2 = new byte[ 4 + m_local.length + 4 + dummyCentral.length ];
        System.arraycopy( m_data, 0, data2, 0, 4 + m_local.length + 2 );
        System.arraycopy( m_dummy.getCentralDirectoryLength().getBytes(), 0,
                          data2, 4 + m_local.length + 2, 2 );
        System.arraycopy( dummyCentral, 0, data2,
                          4 + m_local.length + 4, dummyCentral.length );

        final byte[] central =
            ExtraFieldUtils.mergeCentralDirectoryData( new ZipExtraField[]{m_field, m_dummy} );
        assertEquals( "central length", data2.length, central.length );
        for( int i = 0; i < central.length; i++ )
        {
            assertEquals( "central byte " + i, data2[ i ], central[ i ] );
        }
    }
}
