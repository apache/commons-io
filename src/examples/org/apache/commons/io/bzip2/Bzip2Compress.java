/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.commons.io.compress.bzip2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This simple example shows how to use the Bzip2 classes to compress a file.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/09 14:55:32 $
 */
public class Bzip2Compress
{
    public static void main( final String[] args )
        throws Exception
    {
        if( 2 != args.length )
        {
            System.out.println( "java Bzip2Compress <input> <output>" );
            System.exit( 1 );
        }

        final File source = new File( args[ 0 ] );
        final File destination = new File( args[ 1 ] );
        final CBZip2OutputStream output =
            new CBZip2OutputStream( new FileOutputStream( destination ) );
        final FileInputStream input = new FileInputStream( source );
        copy( input, output );
        input.close();
        output.close();
    }

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     */
    private static void copy( final InputStream input,
                              final OutputStream output )
        throws IOException
    {
        final byte[] buffer = new byte[ 8024 ];
        int n = 0;
        while( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
    }
}
