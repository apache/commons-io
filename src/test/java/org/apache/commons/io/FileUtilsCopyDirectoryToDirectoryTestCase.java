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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This class ensure the correctness of {@link FileUtils#copyDirectoryToDirectory(File, File)}. TODO: currently does not
 * cover happy cases
 *
 * @see FileUtils#copyDirectoryToDirectory(File, File)
 */
public class FileUtilsCopyDirectoryToDirectoryTestCase {

    private static void assertExceptionTypeAndMessage(final File srcDir, final File destDir,
        final Class<? extends Exception> expectedExceptionType, final String expectedMessage) {
        try {
            FileUtils.copyDirectoryToDirectory(srcDir, destDir);
        } catch (final Exception e) {
            final String msg = e.getMessage();
            assertEquals(expectedExceptionType, e.getClass());
            assertEquals(expectedMessage, msg);
            return;
        }
        fail();
    }

    /** Temporary folder managed by JUnit. */
    @TempDir
    public File temporaryFolder;

    private void assertAclEntryList(final Path sourcePath, final Path destPath) throws IOException {
        assertEquals(PathUtils.getAclEntryList(sourcePath), PathUtils.getAclEntryList(destPath));
    }

    @Test
    public void copyDirectoryToDirectoryThrowsIllegalArgumentExceptionWithCorrectMessageWhenDstDirIsNotDirectory()
        throws IOException {
        final File srcDir = new File(temporaryFolder, "sourceDirectory");
        srcDir.mkdir();
        final File destDir = new File(temporaryFolder, "notadirectory");
        destDir.createNewFile();
        final String expectedMessage = String.format("Parameter 'destinationDir' is not a directory: '%s'",
            destDir);
        assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsIllegalExceptionWithCorrectMessageWhenSrcDirIsNotDirectory()
        throws IOException {
        final File srcDir = File.createTempFile("notadireotry", null, temporaryFolder);
        final File destDir = new File(temporaryFolder, "destinationDirectory");
        destDir.mkdirs();
        final String expectedMessage = String.format("Parameter 'sourceDir' is not a directory: '%s'",
            srcDir);
        assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsNullPointerExceptionWithCorrectMessageWhenDstDirIsNull() {
        final File srcDir = new File(temporaryFolder, "sourceDirectory");
        srcDir.mkdir();
        final File destDir = null;
        assertExceptionTypeAndMessage(srcDir, destDir, NullPointerException.class, "destinationDir");
    }

    @Test
    public void copyDirectoryToDirectoryThrowsNullPointerExceptionWithCorrectMessageWhenSrcDirIsNull() {
        final File srcDir = null;
        final File destinationDirectory = new File(temporaryFolder, "destinationDirectory");
        destinationDirectory.mkdir();
        assertExceptionTypeAndMessage(srcDir, destinationDirectory, NullPointerException.class, "sourceDir");
    }

    @Test
    public void copyFileAndCheckAcl() throws IOException {
        final Path sourcePath = Files.createTempFile("TempOutput", ".bin");
        final Path destPath = Paths.get(temporaryFolder.getAbsolutePath(), "SomeFile.bin");
        // Test copy attributes without replace FIRST.
        FileUtils.copyFile(sourcePath.toFile(), destPath.toFile(), true, StandardCopyOption.COPY_ATTRIBUTES);
        assertAclEntryList(sourcePath, destPath);
        //
        FileUtils.copyFile(sourcePath.toFile(), destPath.toFile());
        assertAclEntryList(sourcePath, destPath);
        //
        FileUtils.copyFile(sourcePath.toFile(), destPath.toFile(), true, StandardCopyOption.REPLACE_EXISTING);
        assertAclEntryList(sourcePath, destPath);
        //
        FileUtils.copyFile(sourcePath.toFile(), destPath.toFile(), true, StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES);
        assertAclEntryList(sourcePath, destPath);
    }
}
