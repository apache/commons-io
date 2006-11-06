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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Tests that files really lock, although no writing is done as 
 * the locking is tested only on construction. 
 *
 * @version $Revision$ $Date$
 */
public class LockableFileWriterTest extends FileBasedTestCase {

    private File file;
    private File lockDir;
    private File lockFile;
    private File altLockDir;
    private File altLockFile;

    public LockableFileWriterTest(String name) {
        super(name);
    }

    public void setUp() {
        file = new File(getTestDirectory(), "testlockfile");
        lockDir = new File(System.getProperty("java.io.tmpdir"));
        lockFile = new File(lockDir, file.getName() + ".lck");
        altLockDir = getTestDirectory();
        altLockFile = new File(altLockDir, file.getName() + ".lck");
    }

    public void tearDown() {
        file.delete();
        lockFile.delete();
        altLockFile.delete();
    }

    //-----------------------------------------------------------------------
    public void testFileLocked() throws IOException {
        LockableFileWriter lfw1 = null;
        LockableFileWriter lfw2 = null;
        LockableFileWriter lfw3 = null;
        try {
            // open a valid locakable writer
            lfw1 = new LockableFileWriter(file);
            assertEquals(true, file.exists());
            assertEquals(true, lockFile.exists());
            
            // try to open a second writer
            try {
                lfw2 = new LockableFileWriter(file);
                fail("Somehow able to open a locked file. ");
            } catch(IOException ioe) {
                String msg = ioe.getMessage();
                assertTrue( "Exception message does not start correctly. ", 
                            msg.startsWith("Can't write file, lock ") );
                assertEquals(true, file.exists());
                assertEquals(true, lockFile.exists());
            }
            
            // try to open a third writer
            try {
                lfw3 = new LockableFileWriter(file);
                fail("Somehow able to open a locked file. ");
            } catch(IOException ioe) {
                String msg = ioe.getMessage();
                assertTrue( "Exception message does not start correctly. ", 
                            msg.startsWith("Can't write file, lock ") );
                assertEquals(true, file.exists());
                assertEquals(true, lockFile.exists());
            }
            
        } finally {
            IOUtils.closeQuietly(lfw1);
            IOUtils.closeQuietly(lfw2);
            IOUtils.closeQuietly(lfw3);
        }
        assertEquals(true, file.exists());
        assertEquals(false, lockFile.exists());
    }

    //-----------------------------------------------------------------------
    public void testAlternateLockDir() throws IOException {
        LockableFileWriter lfw1 = null;
        LockableFileWriter lfw2 = null;
        try {
            // open a valid locakable writer
            lfw1 = new LockableFileWriter(file, true, altLockDir.getAbsolutePath());
            assertEquals(true, file.exists());
            assertEquals(true, altLockFile.exists());
            
            // try to open a second writer
            try {
                lfw2 = new LockableFileWriter(file, true, altLockDir.getAbsolutePath());
                fail("Somehow able to open a locked file. ");
            } catch(IOException ioe) {
                String msg = ioe.getMessage();
                assertTrue( "Exception message does not start correctly. ", 
                            msg.startsWith("Can't write file, lock ") );
                assertEquals(true, file.exists());
                assertEquals(true, altLockFile.exists());
            }
            
        } finally {
            IOUtils.closeQuietly(lfw1);
            IOUtils.closeQuietly(lfw2);
        }
        assertEquals(true, file.exists());
        assertEquals(false, altLockFile.exists());
    }

    //-----------------------------------------------------------------------
    public void testFileNotLocked() throws IOException {
        // open a valid locakable writer
        LockableFileWriter lfw1 = null;
        try {
            lfw1 = new LockableFileWriter(file);
            assertEquals(true, file.exists());
            assertEquals(true, lockFile.exists());
        } finally {
            IOUtils.closeQuietly(lfw1);
        }
        assertEquals(true, file.exists());
        assertEquals(false, lockFile.exists());
        
        // open a second valid writer on the same file
        LockableFileWriter lfw2 = null;
        try {
            lfw2 = new LockableFileWriter(file);
            assertEquals(true, file.exists());
            assertEquals(true, lockFile.exists());
        } finally {
            IOUtils.closeQuietly(lfw2);
        }
        assertEquals(true, file.exists());
        assertEquals(false, lockFile.exists());
    }

    //-----------------------------------------------------------------------
    public void testConstructor_File_encoding_badEncoding() throws IOException {
        Writer writer = null;
        try {
            writer = new LockableFileWriter(file, "BAD-ENCODE");
            fail();
        } catch (IOException ex) {
            // expected
            assertEquals(false, file.exists());
            assertEquals(false, lockFile.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertEquals(false, file.exists());
        assertEquals(false, lockFile.exists());
    }

    //-----------------------------------------------------------------------
    public void testConstructor_File_directory() throws IOException {
        Writer writer = null;
        try {
            writer = new LockableFileWriter(getTestDirectory());
            fail();
        } catch (IOException ex) {
            // expected
            assertEquals(false, file.exists());
            assertEquals(false, lockFile.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertEquals(false, file.exists());
        assertEquals(false, lockFile.exists());
    }

    //-----------------------------------------------------------------------
    public void testConstructor_File_nullFile() throws IOException {
        Writer writer = null;
        try {
            writer = new LockableFileWriter((File) null);
            fail();
        } catch (NullPointerException ex) {
            // expected
            assertEquals(false, file.exists());
            assertEquals(false, lockFile.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertEquals(false, file.exists());
        assertEquals(false, lockFile.exists());
    }

    //-----------------------------------------------------------------------
    public void testConstructor_fileName_nullFile() throws IOException {
        Writer writer = null;
        try {
            writer = new LockableFileWriter((String) null);
            fail();
        } catch (NullPointerException ex) {
            // expected
            assertEquals(false, file.exists());
            assertEquals(false, lockFile.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertEquals(false, file.exists());
        assertEquals(false, lockFile.exists());
    }

}
