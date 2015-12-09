/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.testtools.FileBasedTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that files really lock, although no writing is done as
 * the locking is tested only on construction.
 *
 * @version $Id$
 */
public class LockableFileWriterTest extends FileBasedTestCase {

    private File file;
    private File lockDir;
    private File lockFile;
    private File altLockDir;
    private File altLockFile;

    @Before
    public void setUp() {
        file = new File(getTestDirectory(), "testlockfile");
        lockDir = new File(System.getProperty("java.io.tmpdir"));
        lockFile = new File(lockDir, file.getName() + ".lck");
        altLockDir = getTestDirectory();
        altLockFile = new File(altLockDir, file.getName() + ".lck");
    }

    @After
    public void tearDown() {
        file.delete();
        lockFile.delete();
        altLockFile.delete();
    }

    //-----------------------------------------------------------------------
    @Test public void testFileLocked() throws IOException {
        LockableFileWriter lfw1 = null;
        LockableFileWriter lfw2 = null;
        LockableFileWriter lfw3 = null;
        try {
            // open a valid locakable writer
            lfw1 = new LockableFileWriter(file);
            assertTrue(file.exists());
            assertTrue(lockFile.exists());

            // try to open a second writer
            try {
                lfw2 = new LockableFileWriter(file);
                fail("Somehow able to open a locked file. ");
            } catch(final IOException ioe) {
                final String msg = ioe.getMessage();
                assertTrue( "Exception message does not start correctly. ",
                            msg.startsWith("Can't write file, lock ") );
                assertTrue(file.exists());
                assertTrue(lockFile.exists());
            }

            // try to open a third writer
            try {
                lfw3 = new LockableFileWriter(file);
                fail("Somehow able to open a locked file. ");
            } catch(final IOException ioe) {
                final String msg = ioe.getMessage();
                assertTrue( "Exception message does not start correctly. ",
                            msg.startsWith("Can't write file, lock ") );
                assertTrue(file.exists());
                assertTrue(lockFile.exists());
            }

        } finally {
            IOUtils.closeQuietly(lfw1);
            IOUtils.closeQuietly(lfw2);
            IOUtils.closeQuietly(lfw3);
        }
        assertTrue(file.exists());
        assertFalse(lockFile.exists());
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("deprecation") // unavoidable until Java 7
    @Test public void testAlternateLockDir() throws IOException {
        LockableFileWriter lfw1 = null;
        LockableFileWriter lfw2 = null;
        try {
            // open a valid locakable writer
            lfw1 = new LockableFileWriter(file, "UTF-8" ,true, altLockDir.getAbsolutePath());
            assertTrue(file.exists());
            assertTrue(altLockFile.exists());

            // try to open a second writer
            try {
                lfw2 = new LockableFileWriter(file, Charsets.UTF_8, true, altLockDir.getAbsolutePath());
                fail("Somehow able to open a locked file. ");
            } catch(final IOException ioe) {
                final String msg = ioe.getMessage();
                assertTrue( "Exception message does not start correctly. ",
                            msg.startsWith("Can't write file, lock ") );
                assertTrue(file.exists());
                assertTrue(altLockFile.exists());
            }

        } finally {
            IOUtils.closeQuietly(lfw1);
            IOUtils.closeQuietly(lfw2);
        }
        assertTrue(file.exists());
        assertFalse(altLockFile.exists());
    }

    //-----------------------------------------------------------------------
    @Test public void testFileNotLocked() throws IOException {
        // open a valid locakable writer
        LockableFileWriter lfw1 = null;
        try {
            lfw1 = new LockableFileWriter(file);
            assertTrue(file.exists());
            assertTrue(lockFile.exists());
        } finally {
            IOUtils.closeQuietly(lfw1);
        }
        assertTrue(file.exists());
        assertFalse(lockFile.exists());

        // open a second valid writer on the same file
        LockableFileWriter lfw2 = null;
        try {
            lfw2 = new LockableFileWriter(file);
            assertTrue(file.exists());
            assertTrue(lockFile.exists());
        } finally {
            IOUtils.closeQuietly(lfw2);
        }
        assertTrue(file.exists());
        assertFalse(lockFile.exists());
    }

    //-----------------------------------------------------------------------
    @Test public void testConstructor_File_encoding_badEncoding() throws IOException {
        Writer writer = null;
        try {
            writer = new LockableFileWriter(file, "BAD-ENCODE");
            fail();
        } catch (final UnsupportedCharsetException ex) {
            // expected
            assertFalse(file.exists());
            assertFalse(lockFile.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
    }

    //-----------------------------------------------------------------------
    @Test public void testConstructor_File_directory() {
        Writer writer = null;
        try {
            writer = new LockableFileWriter(getTestDirectory());
            fail();
        } catch (final IOException ex) {
            // expected
            assertFalse(file.exists());
            assertFalse(lockFile.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
    }

    //-----------------------------------------------------------------------
    @Test public void testConstructor_File_nullFile() throws IOException {
        Writer writer = null;
        try {
            writer = new LockableFileWriter((File) null);
            fail();
        } catch (final NullPointerException ex) {
            // expected
            assertFalse(file.exists());
            assertFalse(lockFile.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
    }

    //-----------------------------------------------------------------------
    @Test public void testConstructor_fileName_nullFile() throws IOException {
        Writer writer = null;
        try {
            writer = new LockableFileWriter((String) null);
            fail();
        } catch (final NullPointerException ex) {
            // expected
            assertFalse(file.exists());
            assertFalse(lockFile.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
    }

}
