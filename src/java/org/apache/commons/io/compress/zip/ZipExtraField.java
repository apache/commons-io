/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/zip/Attic/ZipExtraField.java,v 1.2 2002/07/13 22:37:46 nicolaken Exp $
 * $Revision: 1.2 $
 * $Date: 2002/07/13 22:37:46 $
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import java.util.zip.ZipException;

/**
 * General format of extra field data. <p>
 *
 * Extra fields usually appear twice per file, once in the local file data and
 * once in the central directory. Usually they are the same, but they don't have
 * to be. {@link java.util.zip.ZipOutputStream java.util.zip.ZipOutputStream}
 * will only use the local file data in both places.</p>
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.2 $
 */
public interface ZipExtraField
{
    /**
     * The Header-ID.
     *
     * @return The HeaderId value
     * @since 1.1
     */
    ZipShort getHeaderID();

    /**
     * Length of the extra field in the local file data - without Header-ID or
     * length specifier.
     *
     * @return The LocalFileDataLength value
     * @since 1.1
     */
    ZipShort getLocalFileDataLength();

    /**
     * Length of the extra field in the central directory - without Header-ID or
     * length specifier.
     *
     * @return The CentralDirectoryLength value
     * @since 1.1
     */
    ZipShort getCentralDirectoryLength();

    /**
     * The actual data to put into local file data - without Header-ID or length
     * specifier.
     *
     * @return The LocalFileDataData value
     * @since 1.1
     */
    byte[] getLocalFileDataData();

    /**
     * The actual data to put central directory - without Header-ID or length
     * specifier.
     *
     * @return The CentralDirectoryData value
     * @since 1.1
     */
    byte[] getCentralDirectoryData();

    /**
     * Populate data from this array as if it was in local file data.
     *
     * @param buffer the buffer to read data from
     * @param offset offset into buffer to read data
     * @param length the length of data
     * @exception ZipException on error
     * @since 1.1
     */
    void parseFromLocalFileData( byte[] buffer, int offset, int length )
        throws ZipException;
}
