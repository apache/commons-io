/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.commons.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * General IO Stream manipulation.
 * <p>
 * This class provides static utility methods for input/output operations.
 * </p>
 * <p>The closeQuietly methods are expected to be used when an IOException 
 * would be meaningless. This is usually when in a catch block for an 
 * IOException. </p>
 * <p>The toString and toByteArray methods all rely on CopyUtils.copy 
 * methods in the current implementation. </p>
 *
 * <p>Origin of code: Apache Avalon (Excalibur)</p>
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @version CVS $Revision: 1.14 $ $Date: 2004/04/24 23:49:25 $
 */
public final class IOUtils
{
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public IOUtils() {}

    /**
     * Unconditionally close an <code>Reader</code>.
     * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
     *
     * @param input A (possibly null) Reader
     */
    public static void closeQuietly( Reader input )
    {
        if( input == null )
        {
            return;
        }

        try
        {
            input.close();
        }
        catch( IOException ioe )
        {
        }
    }

    /**
     * Unconditionally close an <code>Writer</code>.
     * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
     *
     * @param output A (possibly null) Writer
     */
    public static void closeQuietly( Writer output )
    {
        if( output == null )
        {
            return;
        }

        try
        {
            output.close();
        }
        catch( IOException ioe )
        {
        }
    }

    /**
     * Unconditionally close an <code>OutputStream</code>.
     * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
     * @param output A (possibly null) OutputStream
     */
    public static void closeQuietly( OutputStream output )
    {
        if( output == null )
        {
            return;
        }

        try
        {
            output.close();
        }
        catch( IOException ioe )
        {
        }
    }

    /**
     * Unconditionally close an <code>InputStream</code>.
     * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
     * @param input A (possibly null) InputStream
     */
    public static void closeQuietly( InputStream input )
    {
        if( input == null )
        {
            return;
        }

        try
        {
            input.close();
        }
        catch( IOException ioe )
        {
        }
    }

    /**
     * Get the contents of an <code>InputStream</code> as a String.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the <code>InputStream</code> to read from
     * @return the requested <code>String</code>
     * @throws IOException In case of an I/O problem
     */
    public static String toString( InputStream input )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw );
        return sw.toString();
    }

    /**
     * Get the contents of an <code>InputStream</code> as a String.
     * @param input the <code>InputStream</code> to read from
     * @param encoding The name of a supported character encoding. See the
     *   <a href="http://www.iana.org/assignments/character-sets">IANA
     *   Charset Registry</a> for a list of valid encoding types.
     * @return the requested <code>String</code>
     * @throws IOException In case of an I/O problem
     */
    public static String toString( InputStream input,
                                   String encoding )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw, encoding );
        return sw.toString();
    }

    ///////////////////////////////////////////////////////////////
    // InputStream -> byte[]

    /**
     * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
     * @param input the <code>InputStream</code> to read from
     * @return the requested byte array
     * @throws IOException In case of an I/O problem
     */
    public static byte[] toByteArray( InputStream input )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy( input, output );
        return output.toByteArray();
    }


    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // Reader -> *
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // Reader -> String
    /**
     * Get the contents of a <code>Reader</code> as a String.
     * @param input the <code>Reader</code> to read from
     * @return the requested <code>String</code>
     * @throws IOException In case of an I/O problem
     */
    public static String toString( Reader input )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw );
        return sw.toString();
    }


    ///////////////////////////////////////////////////////////////
    // Reader -> byte[]
    /**
     * Get the contents of a <code>Reader</code> as a <code>byte[]</code>.
     * @param input the <code>Reader</code> to read from
     * @return the requested byte array
     * @throws IOException In case of an I/O problem
     */
    public static byte[] toByteArray( Reader input )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy( input, output );
        return output.toByteArray();
    }


    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // String -> *
    ///////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////
    // String -> byte[]
    /**
     * Get the contents of a <code>String</code> as a <code>byte[]</code>.
     * @param input the <code>String</code> to convert
     * @return the requested byte array
     * @throws IOException In case of an I/O problem
     */
    public static byte[] toByteArray( String input )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CopyUtils.copy( input, output );
        return output.toByteArray();
    }


    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // byte[] -> *
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // byte[] -> String

    /**
     * Get the contents of a <code>byte[]</code> as a String.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the byte array to read from
     * @return the requested <code>String</code>
     * @throws IOException In case of an I/O problem
     */
    public static String toString( byte[] input )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw );
        return sw.toString();
    }


    /**
     * Get the contents of a <code>byte[]</code> as a String.
     * @param input the byte array to read from
     * @param encoding The name of a supported character encoding. See the
     *   <a href="http://www.iana.org/assignments/character-sets">IANA
     *   Charset Registry</a> for a list of valid encoding types.
     * @return the requested <code>String</code>
     * @throws IOException In case of an I/O problem
     */
    public static String toString( byte[] input,
                                   String encoding )
        throws IOException
    {
        StringWriter sw = new StringWriter();
        CopyUtils.copy( input, sw, encoding );
        return sw.toString();
    }


    /**
     * Compare the contents of two Streams to determine if they are equal or not.
     *
     * @param input1 the first stream
     * @param input2 the second stream
     * @return true if the content of the streams are equal or they both don't exist, false otherwise
     * @throws IOException In case of an I/O problem
     */
    public static boolean contentEquals( InputStream input1,
                                         InputStream input2 )
        throws IOException
    {
        InputStream bufferedInput1 = new BufferedInputStream( input1 );
        InputStream bufferedInput2 = new BufferedInputStream( input2 );

        int ch = bufferedInput1.read();
        while( -1 != ch )
        {
            int ch2 = bufferedInput2.read();
            if( ch != ch2 )
            {
                return false;
            }
            ch = bufferedInput1.read();
        }

        int ch2 = bufferedInput2.read();
        if( -1 != ch2 )
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
