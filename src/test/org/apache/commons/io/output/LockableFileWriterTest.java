/*
 * Copyright 2004 The Apache Software Foundation.
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
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Tests that files really lock, although no writing is done as 
 * the locking is tested only on construction. 
 *
 * @author Henri Yandell (bayard at apache dot org)
 * @version $Revision$ $Date$
 */

public class LockableFileWriterTest extends TestCase {

    private File file;
    private File lockDir;
    private File lockFile;

    public LockableFileWriterTest(String name) {
        super(name);
    }

    public void setUp() {
        file = new File("testlockfile");
        lockDir = new File(System.getProperty("java.io.tmpdir"));
        lockFile = new File(lockDir, file.getName() + ".lck");
    }

    public void tearDown() {
        file.delete();
        lockFile.delete();
    }

    //-----------------------------------------------------------------------
    public void testFileLocked() throws IOException {
        LockableFileWriter lfw = new LockableFileWriter(this.file);
        try {
            new LockableFileWriter(this.file);
            fail("Somehow able to open a locked file. ");
        } catch(IOException ioe) {
            String msg = ioe.getMessage();
            assertTrue( "Exception message does not start correctly. ", 
                        msg.startsWith("Can't write file, lock ") );
        } finally {
            lfw.close();
        }
        assertEquals(false, lockFile.exists());
    }

    public void testFileNotLocked() throws IOException {
        LockableFileWriter lfw = new LockableFileWriter(this.file);
        lfw.close();
        try {
            LockableFileWriter lfw2 = new LockableFileWriter(this.file);
            lfw2.close();
        } catch(IOException ioe) {
            String msg = ioe.getMessage();
            if( msg.startsWith("Can't write file, lock ") ) {
                fail("Somehow unable to open a unlocked file. ");
            }
        }
        assertEquals(false, lockFile.exists());
    }

    public void testConstructor_File_encoding_badEncoding() throws IOException {
        try {
            new LockableFileWriter(file, "BAD-ENCODE");
            fail();
        } catch (IOException ex) {}
        assertEquals(false, lockFile.exists());
    }

    public void testConstructor_File_nullFile() throws IOException {
        try {
            new LockableFileWriter((File) null);
            fail();
        } catch (NullPointerException ex) {}
        assertEquals(false, lockFile.exists());
    }

    public void testConstructor_fileName_nullFile() throws IOException {
        try {
            new LockableFileWriter((String) null);
            fail();
        } catch (NullPointerException ex) {}
        assertEquals(false, lockFile.exists());
    }

}
