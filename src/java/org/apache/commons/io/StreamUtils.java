package org.apache.commons.io;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 */

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.StringBuffer;

/**
 * Methods for manipulating streams.
 *
 * Borrowed from the commons-util repo.
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:leonardr@collab.net">Leonard Richardson</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: StreamUtils.java,v 1.1 2002/01/26 02:47:42 sanders Exp $
 */
public class StreamUtils
{
    /**
     * Buffer size to use if one is not specified.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * Reads from a stream until EOF, and returns everything read as a
     * string. Uses the default buffer size, and the platform's
     * default character encoding.
     *
     * @param toRead Stream to use as source.
     * @return A String version of the contents of <code>toRead</code>.
     */
    public static String streamAsString(InputStream toRead)
        throws IOException
    {
        return streamAsString(toRead, DEFAULT_BUFFER_SIZE, null);
    }

    /**
     * Reads from a stream until EOF, and returns everything read as a
     * string.
     *
     * @param toRead     Stream to use as source.
     * @param bufferSize Size of buffer to use when reading from source.
     * @param encoding   Encoding to use when converting bytes to
     *                   characters. A null value means to use the
     *                   platforms's default character encoding.
     * @return A String version of the contents of <code>toRead</code>.
     */
    public static String streamAsString(InputStream toRead, int bufferSize,
                                        String encoding)
        throws IOException
    {
        ByteArrayOutputStream contents = readStream(toRead, bufferSize);
        return (encoding == null ? contents.toString() :
                contents.toString(encoding));
    }

    /**
     * Reads from a stream until EOF, and returns the bytes read.
     *
     * @param toRead     Stream to use as source.
     * @param bufferSize Size of buffer to use when reading from source.
     * @return The contents of <code>toRead</code>.
     */
    public static byte[] streamAsBytes(InputStream toRead, int bufferSize)
        throws IOException
    {
        ByteArrayOutputStream contents = readStream(toRead, bufferSize);
        return contents.toByteArray();
    }

    /**
     * Reads from a stream util EOF, placing the resulting data into a
     * <code>ByteArrayOutputStream</code> which is subsequently returned.
     *
     * @param toRead     Stream to use as source.
     * @param bufferSize Size of buffer to use when reading from source.
     *
     * @return a <code>ByteArrayOutputStream</code> containing the
     * contents of <code>toRead</code>.
     */
    protected static ByteArrayOutputStream readStream(InputStream toRead,
                                                      int bufferSize)
       throws IOException
     {
        ByteArrayOutputStream contents = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        while ( (bytesRead = toRead.read(buffer)) != -1 )
        {
            contents.write(buffer, 0, bytesRead);
        }

        return contents;
    }

    /**
     * Pipes the contents of an InputStream into an OutputStream.
     * Uses the default buffer size. Note that you still need to close
     * the OutputStream.
     *
     * @param toRead  Stream to use as source.
     * @param toWrite Stream to use as sink.
     */
    public static void pipe(InputStream toRead, OutputStream toWrite)
        throws IOException
    {
        pipe(toRead, toWrite, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Pipes the contents of an InputStream into an OutputStream.
     * Note that you still need to close the OutputStream.
     *
     * @param toRead   Stream to use as source.
     * @param toWrite  Stream to use as sink.
     * @param bufferSize Size of buffer to use when reading from source.
     */
    public static void pipe(InputStream toRead, OutputStream toWrite,
                            int bufferSize)
        throws IOException
    {
        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        while ( (bytesRead = toRead.read(buffer)) != -1 )
        {
            toWrite.write(buffer, 0, bytesRead);
        }
    }
}
