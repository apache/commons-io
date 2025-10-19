/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link LockableFileWriter}.
 * <p>
 * Tests that files really lock, although no writing is done as the locking is tested only on construction.
 * </p>
 */
class LockableFileWriterTest {

    @TempDir
    public File temporaryFolder;

    private File file;
    private File lockDir;
    private File lockFile;
    private File altLockDir;
    private File altLockFile;

    @BeforeEach
    public void setUp() {
        file = new File(temporaryFolder, "testlockfile");
        lockDir = FileUtils.getTempDirectory();
        lockFile = new File(lockDir, file.getName() + ".lck");
        altLockDir = temporaryFolder;
        altLockFile = new File(altLockDir, file.getName() + ".lck");
    }

    @Test
    void testAlternateLockDir() throws IOException {
        // open a valid lockable writer
        try (LockableFileWriter lfw1 = new LockableFileWriter(file, StandardCharsets.UTF_8, true, altLockDir.getAbsolutePath())) {
            testAlternateLockDir(lfw1);
        }
        assertTrue(file.exists());
        assertFalse(altLockFile.exists());
        //
        // open a valid lockable writer
        // @formatter:off
        try (LockableFileWriter lfw1 = LockableFileWriter.builder()
                .setFile(file)
                .setCharset(StandardCharsets.UTF_8)
                .setAppend(true)
                .setLockDirectory(altLockDir)
                .get()) {
            // @formatter:on
            testAlternateLockDir(lfw1);
        }
        assertTrue(file.exists());
        assertFalse(altLockFile.exists());
    }

    private void testAlternateLockDir(final LockableFileWriter lfw1) {
        assertNotNull(lfw1);
        assertTrue(file.exists());
        assertTrue(altLockFile.exists());

        // try to open a second writer
        try (LockableFileWriter lfw2 = new LockableFileWriter(file, StandardCharsets.UTF_8, true, altLockDir.getAbsolutePath())) {
            fail("Somehow able to open a locked file.");
        } catch (final IOException ioe) {
            final String msg = ioe.getMessage();
            assertTrue(msg.startsWith("Can't write file, lock "), "Exception message does not start correctly.");
            assertTrue(file.exists());
            assertTrue(altLockFile.exists());
        }
    }

    @Test
    void testConstructor_File_directory() {
        assertThrows(IOException.class, () -> new LockableFileWriter(temporaryFolder));
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
        // again
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
    }

    @Test
    void testConstructor_File_encoding_badEncoding() {
        assertThrows(UnsupportedCharsetException.class, () -> new LockableFileWriter(file, "BAD-ENCODE"));
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
        // again
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
        //
        assertThrows(UnsupportedCharsetException.class, () -> LockableFileWriter.builder().setFile(file).setCharset("BAD-ENCODE").get());
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
        // again
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
    }

    @Test
    void testConstructor_File_nullFile() {
        assertThrows(NullPointerException.class, () -> new LockableFileWriter((File) null));
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
        // again
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
        //
        assertThrows(IllegalStateException.class, () -> LockableFileWriter.builder().get());
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
        // again
        assertFalse(file.exists());
        assertFalse(lockFile.exists());

    }

    @Test
    void testConstructor_fileName_nullFile() {
        assertThrows(NullPointerException.class, () -> new LockableFileWriter((String) null));
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
        // again
        assertFalse(file.exists());
        assertFalse(lockFile.exists());
    }

    @Test
    void testFileLocked() throws IOException {

        // open a valid lockable writer
        try (LockableFileWriter lfw1 = new LockableFileWriter(file)) {
            assertTrue(file.exists());
            assertTrue(lockFile.exists());

            // try to open a second writer
            try (LockableFileWriter lfw2 = new LockableFileWriter(file)) {
                fail("Somehow able to open a locked file.");
            } catch (final IOException ioe) {
                final String msg = ioe.getMessage();
                assertTrue(msg.startsWith("Can't write file, lock "), "Exception message does not start correctly.");
                assertTrue(file.exists());
                assertTrue(lockFile.exists());
            }

            // try to open a third writer
            try (LockableFileWriter lfw3 = new LockableFileWriter(file)) {
                fail("Somehow able to open a locked file.");
            } catch (final IOException ioe) {
                final String msg = ioe.getMessage();
                assertTrue(msg.startsWith("Can't write file, lock "), "Exception message does not start correctly.");
                assertTrue(file.exists());
                assertTrue(lockFile.exists());
            }
        }
        assertTrue(file.exists());
        assertFalse(lockFile.exists());
    }

    @Test
    void testFileNotLocked() throws IOException {
        // open a valid lockable writer
        try (LockableFileWriter lfw1 = new LockableFileWriter(file)) {
            assertTrue(file.exists());
            assertTrue(lockFile.exists());
        }
        assertTrue(file.exists());
        assertFalse(lockFile.exists());

        // open a second valid writer on the same file
        try (LockableFileWriter lfw2 = new LockableFileWriter(file)) {
            assertTrue(file.exists());
            assertTrue(lockFile.exists());
        }
        assertTrue(file.exists());
        assertFalse(lockFile.exists());
    }

}
