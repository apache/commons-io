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
 * @author Stephen Colebourne
 * @version $Id$
 */
public class LockableFileWriter extends Writer {

    /** The extension for the lock file. */
    private static final String LCK = ".lck";

    /** The lock file. */
    private File lockFile;
    /** The write used to write to the file. */
    private Writer writer;
    /** Should we append to the file or not. */
    private boolean append;

    /**
     * Constructs a LockableFileWriter.
     * If the file exists, it is overwritten.
     *
     * @param fileName  the file to write to
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(String fileName) throws IOException {
        this(fileName, false, null);
    }

    /**
     * Constructs a LockableFileWriter.
     *
     * @param fileName  file to write to
     * @param append  true if content should be appended, false to overwrite
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(String fileName, boolean append) throws IOException {
        this(fileName, append, null);
    }

    /**
     * Constructs a LockableFileWriter.
     *
     * @param fileName  the file to write to
     * @param append  true if content should be appended, false to overwrite
     * @param lockDir  the directory in which the lock file should be held
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(String fileName, boolean append, String lockDir) throws IOException {
        this(new File(fileName), append, lockDir);
    }

    /**
     * Constructs a LockableFileWriter.
     * If the file exists, it is overwritten.
     *
     * @param file  the file to write to
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(File file) throws IOException {
        this(file, false, null);
    }

    /**
     * Constructs a LockableFileWriter.
     *
     * @param file  the file to write to
     * @param append  true if content should be appended, false to overwrite
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(File file, boolean append) throws IOException {
        this(file, append, null);
    }

    /**
     * Constructs a LockableFileWriter.
     *
     * @param file  the file to write to
     * @param append  true if content should be appended, false to overwrite
     * @param lockDir  the directory in which the lock file should be held
     * @throws IOException in case of an I/O error
     */
    public LockableFileWriter(File file, boolean append, String lockDir) throws IOException {
        this.append = append;

        if (lockDir == null) {
            lockDir = System.getProperty("java.io.tmpdir");
        }
        testLockDir(new File(lockDir));
        this.lockFile = new File(lockDir, file.getName() + LCK);
        createLock();

        this.writer = new FileWriter(file.getAbsolutePath(), this.append);
    }

    //-----------------------------------------------------------------------
    /**
     * Tests that we can write to the lock directory.
     *
     * @param lockDir  the File representing the lock directory
     * @throws IOException if we cannot write to the lock directory
     * @throws IOException if we cannot find the lock file
     */
    private void testLockDir(File lockDir) throws IOException {
        if (!lockDir.exists()) {
            throw new IOException(
                    "Could not find lockDir: " + lockDir.getAbsolutePath());
        }
        if (!lockDir.canWrite()) {
            throw new IOException(
                    "Could not write to lockDir: " + lockDir.getAbsolutePath());
        }
    }

    /**
     * Creates the lock file.
     *
     * @throws IOException if we cannot create the file
     */
    private void createLock() throws IOException {
        synchronized (LockableFileWriter.class) {
            if (!lockFile.createNewFile()) {
                throw new IOException("Can't write file, lock " +
                        lockFile.getAbsolutePath() + " exists");
            }
            lockFile.deleteOnExit();
        }
    }

    /**
     * Closes the file writer.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        try {
            writer.close();
        } finally {
            lockFile.delete();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Write a portion of a string.
     *
     * @param  cbuf  The characters to write
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    /**
     * Flushes the file writer.
     */
    public void flush() throws IOException {
        writer.flush();
    }

}
