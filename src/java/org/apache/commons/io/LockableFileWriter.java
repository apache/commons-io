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
 *
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * FileWriter that will create and honor lock files to allow simple
 * cross thread file lock handling.  If <code>Writer</code> attributes
 * are unspecified, the default behavior is to overwrite (rather than
 * to append), and to use the value of the system property
 * <code>java.io.tmpdir</code> for the lock file directory.
 *
 * Borrowed from the commons-util repo.
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:ms@collab.net">Michael Salmon</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: LockableFileWriter.java,v 1.2 2002/01/28 05:01:48 sanders Exp $
 */
public class LockableFileWriter extends Writer {

    private static final String LCK = ".lck";

    private File lockFile = null;

    private FileWriter writer = null;

    private boolean append = false;

    public LockableFileWriter(String fileName)
            throws IOException {
        this(fileName, false, null);
    }

    public LockableFileWriter(String fileName, boolean append)
            throws IOException {
        this(fileName, append, null);
    }

    public LockableFileWriter(String fileName, boolean append, String lockDir)
            throws IOException {
        this(new File(fileName), append, lockDir);
    }

    public LockableFileWriter(File file)
            throws IOException {
        this(file, false, null);
    }

    public LockableFileWriter(File file, boolean append)
            throws IOException {
        this(file, append, null);
    }

    public LockableFileWriter(File file, boolean append, String lockDir)
            throws IOException {
        this.append = append;

        if (lockDir == null) {
            lockDir = System.getProperty("java.io.tmpdir");
        }
        testLockDir(new File(lockDir));
        this.lockFile = new File(lockDir, file.getName() + LCK);
        createLock();

        this.writer = new FileWriter(file.getAbsolutePath(), this.append);
    }

    private void testLockDir(File lockDir)
            throws IOException {
        if (!lockDir.exists()) {
            throw new IOException(
                    "Could not find lockDir: " + lockDir.getAbsolutePath());
        }
        if (!lockDir.canWrite()) {
            throw new IOException(
                    "Could not write to lockDir: " + lockDir.getAbsolutePath());
        }
    }

    private void createLock()
            throws IOException {
        synchronized (LockableFileWriter.class) {
            if (!lockFile.createNewFile()) {
                throw new IOException("Can't write file, lock " +
                        lockFile.getAbsolutePath() + " exists");
            }
            lockFile.deleteOnExit();
        }
    }

    public void close()
            throws IOException {
        try {
            writer.close();
        } finally {
            lockFile.delete();
        }
    }

    public void write(char[] cbuf, int off, int len)
            throws IOException {
        writer.write(cbuf, off, len);
    }

    public void flush()
            throws IOException {
        writer.flush();
    }
}
