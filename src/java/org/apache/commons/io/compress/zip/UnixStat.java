/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.zip;

/**
 * Constants from stat.h on Unix systems.
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.1 $
 */
public interface UnixStat
{
    /**
     * Bits used for permissions (and sticky bit)
     *
     * @since 1.1
     */
    int PERM_MASK = 07777;
    /**
     * Indicates symbolic links.
     *
     * @since 1.1
     */
    int LINK_FLAG = 0120000;
    /**
     * Indicates plain files.
     *
     * @since 1.1
     */
    int FILE_FLAG = 0100000;
    /**
     * Indicates directories.
     *
     * @since 1.1
     */
    int DIR_FLAG = 040000;

    // ----------------------------------------------------------
    // somewhat arbitrary choices that are quite common for shared
    // installations
    // -----------------------------------------------------------

    /**
     * Default permissions for symbolic links.
     *
     * @since 1.1
     */
    int DEFAULT_LINK_PERM = 0777;

    /**
     * Default permissions for directories.
     *
     * @since 1.1
     */
    int DEFAULT_DIR_PERM = 0755;

    /**
     * Default permissions for plain files.
     *
     * @since 1.1
     */
    int DEFAULT_FILE_PERM = 0644;
}
