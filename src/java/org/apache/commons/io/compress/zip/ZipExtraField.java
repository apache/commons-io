/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
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
 * @version $Revision: 1.1 $
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
