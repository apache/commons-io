/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/zip/Attic/UnixStat.java,v 1.2 2002/07/13 22:37:46 nicolaken Exp $
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

/**
 * Constants from stat.h on Unix systems.
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.2 $
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
