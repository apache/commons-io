/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/tar/Attic/TarConstants.java,v 1.2 2002/07/13 22:37:46 nicolaken Exp $
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
package org.apache.commons.io.compress.tar;

/**
 * This interface contains all the definitions used in the package.
 *
 * @author <a href="mailto:time@ice.com">Timothy Gerard Endres</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/13 22:37:46 $
 */
interface TarConstants
{
    /**
     * The length of the mode field in a header buffer.
     */
    int MODELEN = 8;

    /**
     * The length of the user id field in a header buffer.
     */
    int UIDLEN = 8;

    /**
     * The length of the group id field in a header buffer.
     */
    int GIDLEN = 8;

    /**
     * The length of the checksum field in a header buffer.
     */
    int CHKSUMLEN = 8;

    /**
     * The length of the size field in a header buffer.
     */
    int SIZELEN = 12;

    /**
     * The length of the magic field in a header buffer.
     */
    int MAGICLEN = 8;

    /**
     * The length of the modification time field in a header buffer.
     */
    int MODTIMELEN = 12;

    /**
     * The length of the user name field in a header buffer.
     */
    int UNAMELEN = 32;

    /**
     * The length of the group name field in a header buffer.
     */
    int GNAMELEN = 32;

    /**
     * The length of the devices field in a header buffer.
     */
    int DEVLEN = 8;

    /**
     * LF_ constants represent the "link flag" of an entry, or more commonly,
     * the "entry type". This is the "old way" of indicating a normal file.
     */
    byte LF_OLDNORM = 0;

    /**
     * Normal file type.
     */
    byte LF_NORMAL = (byte)'0';

    /**
     * Link file type.
     */
    byte LF_LINK = (byte)'1';

    /**
     * Symbolic link file type.
     */
    byte LF_SYMLINK = (byte)'2';

    /**
     * Character device file type.
     */
    byte LF_CHR = (byte)'3';

    /**
     * Block device file type.
     */
    byte LF_BLK = (byte)'4';

    /**
     * Directory file type.
     */
    byte LF_DIR = (byte)'5';

    /**
     * FIFO (pipe) file type.
     */
    byte LF_FIFO = (byte)'6';

    /**
     * Contiguous file type.
     */
    byte LF_CONTIG = (byte)'7';

    /**
     * The magic tag representing a POSIX tar archive.
     */
    String TMAGIC = "ustar";

    /**
     * The magic tag representing a GNU tar archive.
     */
    String GNU_TMAGIC = "ustar  ";

    /**
     * The namr of the GNU tar entry which contains a long name.
     */
    String GNU_LONGLINK = "././@LongLink";

    /**
     * Identifies the *next* file on the tape as having a long name.
     */
    byte LF_GNUTYPE_LONGNAME = (byte)'L';
}
