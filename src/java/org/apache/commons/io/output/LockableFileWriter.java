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
package org.apache.commons.io.output;

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
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:ms@collab.net">Michael Salmon</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: LockableFileWriter.java,v 1.7 2004/02/23 04:40:29 bayard Exp $
 */
public class LockableFileWriter extends Writer {

    private static final String LCK = ".lck";

    private File lockFile = null;

    private FileWriter writer = null;

    private boolean append = false;

    /**
     * Constructs a LockableFileWriter. If the file exists, it is overwritten.
     * @param fileName file to write to
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(String fileName)
            throws IOException {
        this(fileName, false, null);
    }

    /**
     * Constructs a LockableFileWriter.
     * @param fileName file to write to
     * @param append true if content should be appended (default is to overwrite).
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(String fileName, boolean append)
            throws IOException {
        this(fileName, append, null);
    }

    /**
     * Constructs a LockableFileWriter.
     * @param fileName file to write to
     * @param append true if content should be appended (default is to overwrite).
     * @param lockDir Specifies the directory in which the lock file should be held.
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(String fileName, boolean append, String lockDir)
            throws IOException {
        this(new File(fileName), append, lockDir);
    }

    /**
     * Constructs a LockableFileWriter. If the file exists, it is overwritten.
     * @param file file to write to
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(File file)
            throws IOException {
        this(file, false, null);
    }

    /**
     * Constructs a LockableFileWriter.
     * @param file file to write to
     * @param append true if content should be appended (default is to overwrite).
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(File file, boolean append)
            throws IOException {
        this(file, append, null);
    }

    /**
     * Constructs a LockableFileWriter.
     * @param file file to write to
     * @param append true if content should be appended (default is to overwrite).
     * @param lockDir Specifies the directory in which the lock file should be held.
     * @throws IOException in case of an I/O error
     */
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

    /** @see java.io.Writer#close() */
    public void close()
            throws IOException {
        try {
            writer.close();
        } finally {
            lockFile.delete();
        }
    }

    /** @see java.io.Writer#write(char[], int, int) */
    public void write(char[] cbuf, int off, int len)
            throws IOException {
        writer.write(cbuf, off, len);
    }

    /** @see java.io.Writer#flush() */
    public void flush()
            throws IOException {
        writer.flush();
    }
}
