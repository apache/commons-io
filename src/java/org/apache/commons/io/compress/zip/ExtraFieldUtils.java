/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/zip/Attic/ExtraFieldUtils.java,v 1.3 2003/10/13 07:03:30 rdonkin Exp $
 * $Revision: 1.3 $
 * $Date: 2003/10/13 07:03:30 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.io.compress.zip;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.ZipException;

/**
 * ZipExtraField related methods
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.3 $
 */
public class ExtraFieldUtils
{
    /**
     * Static registry of known extra fields.
     *
     * @since 1.1
     */
    private static final Hashtable c_implementations;

    static
    {
        c_implementations = new Hashtable();
        register( AsiExtraField.class );
    }

    /**
     * Create an instance of the approriate ExtraField, falls back to {@link
     * UnrecognizedExtraField UnrecognizedExtraField}.
     *
     * Throws java.lang.IllegalAccessException if cant create implementation.
     *
     * @param headerID the header ID
     * @return the extra field implementation
     * @throws InstantiationException if cant create implementation
     * @throws IllegalAccessException if cant create implementation
     * @since 1.1
     */
    public static ZipExtraField createExtraField( final ZipShort headerID )
        throws InstantiationException, IllegalAccessException
    {
        final Class clazz =
            (Class)c_implementations.get( headerID );
        if( clazz != null )
        {
            return (ZipExtraField)clazz.newInstance();
        }
        final UnrecognizedExtraField unrecognized = new UnrecognizedExtraField();
        unrecognized.setHeaderID( headerID );
        return unrecognized;
    }

    /**
     * Merges the central directory fields of the given ZipExtraFields.
     *
     * @param data the central directory data
     * @return the merged data
     * @since 1.1
     */
    public static byte[] mergeCentralDirectoryData( final ZipExtraField[] data )
    {
        int sum = 4 * data.length;
        for( int i = 0; i < data.length; i++ )
        {
            sum += data[ i ].getCentralDirectoryLength().getValue();
        }
        byte[] result = new byte[ sum ];
        int start = 0;
        for( int i = 0; i < data.length; i++ )
        {
            System.arraycopy( data[ i ].getHeaderID().getBytes(),
                              0, result, start, 2 );
            System.arraycopy( data[ i ].getCentralDirectoryLength().getBytes(),
                              0, result, start + 2, 2 );
            byte[] local = data[ i ].getCentralDirectoryData();
            System.arraycopy( local, 0, result, start + 4, local.length );
            start += ( local.length + 4 );
        }
        return result;
    }

    /**
     * Merges the local file data fields of the given ZipExtraFields.
     *
     * @param data the data
     * @return the merged data
     * @since 1.1
     */
    public static byte[] mergeLocalFileDataData( final ZipExtraField[] data )
    {
        int sum = 4 * data.length;
        for( int i = 0; i < data.length; i++ )
        {
            sum += data[ i ].getLocalFileDataLength().getValue();
        }
        byte[] result = new byte[ sum ];
        int start = 0;
        for( int i = 0; i < data.length; i++ )
        {
            System.arraycopy( data[ i ].getHeaderID().getBytes(),
                              0, result, start, 2 );
            System.arraycopy( data[ i ].getLocalFileDataLength().getBytes(),
                              0, result, start + 2, 2 );
            byte[] local = data[ i ].getLocalFileDataData();
            System.arraycopy( local, 0, result, start + 4, local.length );
            start += ( local.length + 4 );
        }
        return result;
    }

    /**
     * Split the array into ExtraFields and populate them with the give data.
     *
     * @param data the data to parse
     * @return the parsed fields
     * @exception ZipException on error
     * @since 1.1
     */
    public static ZipExtraField[] parse( final byte[] data )
        throws ZipException
    {
        ArrayList v = new ArrayList();
        int start = 0;
        while( start <= data.length - 4 )
        {
            final ZipShort headerID = new ZipShort( data, start );
            int length = ( new ZipShort( data, start + 2 ) ).getValue();
            if( start + 4 + length > data.length )
            {
                throw new ZipException( "data starting at " + start + " is in unknown format" );
            }
            try
            {
                ZipExtraField ze = createExtraField( headerID );
                ze.parseFromLocalFileData( data, start + 4, length );
                v.add( ze );
            }
            catch( InstantiationException ie )
            {
                throw new ZipException( ie.getMessage() );
            }
            catch( IllegalAccessException iae )
            {
                throw new ZipException( iae.getMessage() );
            }
            start += ( length + 4 );
        }
        if( start != data.length )
        {// array not exhausted
            throw new ZipException( "data starting at " + start + " is in unknown format" );
        }

        final ZipExtraField[] result = new ZipExtraField[ v.size() ];
        return (ZipExtraField[])v.toArray( result );
    }

    /**
     * Register a ZipExtraField implementation. <p>
     *
     * The given class must have a no-arg constructor and implement the {@link
     * ZipExtraField ZipExtraField interface}.</p>
     *
     * @param clazz The Class for particular implementation
     * @since 1.1
     */
    public static void register( final Class clazz )
    {
        try
        {
            ZipExtraField ze = (ZipExtraField)clazz.newInstance();
            c_implementations.put( ze.getHeaderID(), clazz );
        }
        catch( ClassCastException cc )
        {
            throw new RuntimeException( clazz +
                                        " doesn\'t implement ZipExtraField" );
        }
        catch( InstantiationException ie )
        {
            throw new RuntimeException( clazz + " is not a concrete class" );
        }
        catch( IllegalAccessException ie )
        {
            throw new RuntimeException( clazz +
                                        "\'s no-arg constructor is not public" );
        }
    }
}
