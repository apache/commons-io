/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/zip/Attic/ZipEntry.java,v 1.3 2003/10/13 07:03:30 rdonkin Exp $
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.zip.ZipException;

/**
 * Extension that adds better handling of extra fields and provides access to
 * the internal and external file attributes.
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.3 $
 */
public class ZipEntry
    extends java.util.zip.ZipEntry
{
    /**
     * Helper for JDK 1.1
     *
     * @since 1.2
     */
    private static Method c_setCompressedSizeMethod;

    /**
     * Helper for JDK 1.1
     *
     * @since 1.2
     */
    private static final Object c_lockReflection = new Object();

    /**
     * Helper for JDK 1.1
     *
     * @since 1.2
     */
    private static boolean c_triedToGetMethod;

    private final ArrayList m_extraFields = new ArrayList();

    private int m_internalAttributes;
    private long m_externalAttributes;

    /**
     * Helper for JDK 1.1 <-> 1.2 incompatibility.
     *
     * @since 1.2
     */
    private Long m_compressedSize;

    /**
     * Creates a new zip entry with the specified name.
     *
     * @param name the name of entry
     * @since 1.1
     */
    public ZipEntry( final String name )
    {
        super( name );
    }

    /**
     * Creates a new zip entry with fields taken from the specified zip entry.
     *
     * @param entry the JDK ZipEntry to adapt
     * @exception ZipException if can not create entry
     * @since 1.1
     */
    public ZipEntry( java.util.zip.ZipEntry entry )
        throws ZipException
    {
        /*
         * REVISIT: call super(entry) instead of this stuff in Ant2,
         * "copy constructor" has not been available in JDK 1.1
         */
        super( entry.getName() );

        setComment( entry.getComment() );
        setMethod( entry.getMethod() );
        setTime( entry.getTime() );

        final long size = entry.getSize();
        if( size > 0 )
        {
            setSize( size );
        }

        final long cSize = entry.getCompressedSize();
        if( cSize > 0 )
        {
            setComprSize( cSize );
        }

        final long crc = entry.getCrc();
        if( crc > 0 )
        {
            setCrc( crc );
        }

        final byte[] extra = entry.getExtra();
        if( extra != null )
        {
            setExtraFields( ExtraFieldUtils.parse( extra ) );
        }
        else
        {
            // initializes extra data to an empty byte array
            setExtra();
        }
    }

    /**
     * Creates a new zip entry with fields taken from the specified zip entry.
     *
     * @param entry the entry to adapt
     * @exception ZipException if can not create entry
     * @since 1.1
     */
    public ZipEntry( final ZipEntry entry )
        throws ZipException
    {
        this( (java.util.zip.ZipEntry)entry );
        setInternalAttributes( entry.getInternalAttributes() );
        setExternalAttributes( entry.getExternalAttributes() );
        setExtraFields( entry.getExtraFields() );
    }

    /**
     * Try to get a handle to the setCompressedSize method.
     *
     * @since 1.2
     */
    private static void checkSCS()
    {
        if( !c_triedToGetMethod )
        {
            synchronized( c_lockReflection )
            {
                c_triedToGetMethod = true;
                try
                {
                    c_setCompressedSizeMethod =
                        java.util.zip.ZipEntry.class.getMethod( "setCompressedSize",
                                                                new Class[]{Long.TYPE} );
                }
                catch( NoSuchMethodException nse )
                {
                }
            }
        }
    }

    /**
     * Are we running JDK 1.2 or higher?
     *
     * @return Description of the Returned Value
     * @since 1.2
     */
    private static boolean haveSetCompressedSize()
    {
        checkSCS();
        return c_setCompressedSizeMethod != null;
    }

    /**
     * Invoke setCompressedSize via reflection.
     *
     * @param entry Description of Parameter
     * @param size Description of Parameter
     * @since 1.2
     */
    private static void performSetCompressedSize( final ZipEntry entry,
                                                  final long size )
    {
        final Long[] s = {new Long( size )};
        try
        {
            c_setCompressedSizeMethod.invoke( entry, s );
        }
        catch( final InvocationTargetException ite )
        {
            final Throwable nested = ite.getTargetException();
            final String message = "Exception setting the compressed size " +
                "of " + entry + ": " + nested.getMessage();
            throw new RuntimeException( message );
        }
        catch( final Throwable t )
        {
            final String message = "Exception setting the compressed size " +
                "of " + entry + ": " + t.getMessage();
            throw new RuntimeException( message );
        }
    }

    /**
     * Make this class work in JDK 1.1 like a 1.2 class. <p>
     *
     * This either stores the size for later usage or invokes setCompressedSize
     * via reflection.</p>
     *
     * @param size The new ComprSize value
     * @since 1.2
     */
    public void setComprSize( final long size )
    {
        if( haveSetCompressedSize() )
        {
            performSetCompressedSize( this, size );
        }
        else
        {
            m_compressedSize = new Long( size );
        }
    }

    /**
     * Sets the external file attributes.
     *
     * @param externalAttributes The new ExternalAttributes value
     * @since 1.1
     */
    public void setExternalAttributes( final long externalAttributes )
    {
        m_externalAttributes = externalAttributes;
    }

    /**
     * Throws an Exception if extra data cannot be parsed into extra fields.
     *
     * @param extra The new Extra value
     * @throws RuntimeException if fail to set extra data
     * @since 1.1
     */
    public void setExtra( final byte[] extra )
        throws RuntimeException
    {
        try
        {
            setExtraFields( ExtraFieldUtils.parse( extra ) );
        }
        catch( final Exception e )
        {
            throw new RuntimeException( e.getMessage() );
        }
    }

    /**
     * Replaces all currently attached extra fields with the new array.
     *
     * @param fields The new ExtraFields value
     * @since 1.1
     */
    public void setExtraFields( final ZipExtraField[] fields )
    {
        m_extraFields.clear();
        for( int i = 0; i < fields.length; i++ )
        {
            m_extraFields.add( fields[ i ] );
        }
        setExtra();
    }

    /**
     * Sets the internal file attributes.
     *
     * @param value The new InternalAttributes value
     * @since 1.1
     */
    public void setInternalAttributes( final int value )
    {
        m_internalAttributes = value;
    }

    /**
     * Retrieves the extra data for the central directory.
     *
     * @return The CentralDirectoryExtra value
     * @since 1.1
     */
    public byte[] getCentralDirectoryExtra()
    {
        return ExtraFieldUtils.mergeCentralDirectoryData( getExtraFields() );
    }

    /**
     * Override to make this class work in JDK 1.1 like a 1.2 class.
     *
     * @return The CompressedSize value
     * @since 1.2
     */
    public long getCompressedSize()
    {
        if( m_compressedSize != null )
        {
            // has been set explicitly and we are running in a 1.1 VM
            return m_compressedSize.longValue();
        }
        return super.getCompressedSize();
    }

    /**
     * Retrieves the external file attributes.
     *
     * @return The ExternalAttributes value
     * @since 1.1
     */
    public long getExternalAttributes()
    {
        return m_externalAttributes;
    }

    /**
     * Retrieves extra fields.
     *
     * @return The ExtraFields value
     * @since 1.1
     */
    public ZipExtraField[] getExtraFields()
    {
        final ZipExtraField[] result = new ZipExtraField[ m_extraFields.size() ];
        return (ZipExtraField[])m_extraFields.toArray( result );
    }

    /**
     * Retrieves the internal file attributes.
     *
     * @return The InternalAttributes value
     * @since 1.1
     */
    public int getInternalAttributes()
    {
        return m_internalAttributes;
    }

    /**
     * Retrieves the extra data for the local file data.
     *
     * @return The LocalFileDataExtra value
     * @since 1.1
     */
    public byte[] getLocalFileDataExtra()
    {
        byte[] extra = getExtra();
        return extra != null ? extra : new byte[ 0 ];
    }

    /**
     * Adds an extra fields - replacing an already present extra field of the
     * same type.
     *
     * @param extraField The feature to be added to the ExtraField attribute
     * @since 1.1
     */
    public void addExtraField( final ZipExtraField extraField )
    {
        final ZipShort type = extraField.getHeaderID();
        boolean done = false;
        for( int i = 0; !done && i < m_extraFields.size(); i++ )
        {
            final ZipExtraField other = (ZipExtraField)m_extraFields.get( i );
            if( other.getHeaderID().equals( type ) )
            {
                m_extraFields.set( i, extraField );
                done = true;
            }
        }
        if( !done )
        {
            m_extraFields.add( extraField );
        }
        setExtra();
    }

    /**
     * Overwrite clone
     *
     * @return Description of the Returned Value
     * @since 1.1
     */
    public Object clone()
    {
        ZipEntry entry = null;
        try
        {
            entry = new ZipEntry( (java.util.zip.ZipEntry)super.clone() );
        }
        catch( final Exception e )
        {
            // impossible as extra data is in correct format
            e.printStackTrace();
            return null;
        }

        entry.setInternalAttributes( getInternalAttributes() );
        entry.setExternalAttributes( getExternalAttributes() );
        entry.setExtraFields( getExtraFields() );
        return entry;
    }

    /**
     * Remove an extra fields.
     *
     * @param type Description of Parameter
     * @since 1.1
     */
    public void removeExtraField( final ZipShort type )
    {
        boolean done = false;
        for( int i = 0; !done && i < m_extraFields.size(); i++ )
        {
            if( ( (ZipExtraField)m_extraFields.get( i ) ).getHeaderID().equals( type ) )
            {
                m_extraFields.remove( i );
                done = true;
            }
        }
        if( !done )
        {
            throw new java.util.NoSuchElementException();
        }
        setExtra();
    }

    /**
     * Unfortunately {@link java.util.zip.ZipOutputStream
     * java.util.zip.ZipOutputStream} seems to access the extra data directly,
     * so overriding getExtra doesn't help - we need to modify super's data
     * directly.
     *
     * @since 1.1
     */
    protected void setExtra()
    {
        super.setExtra( ExtraFieldUtils.mergeLocalFileDataData( getExtraFields() ) );
    }
}
