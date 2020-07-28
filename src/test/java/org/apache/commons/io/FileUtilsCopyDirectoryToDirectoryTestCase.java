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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This class ensure the correctness of {@link FileUtils#copyDirectoryToDirectory(File, File)}. TODO: currently does not
 * cover happy cases
 *
 * @see FileUtils#copyDirectoryToDirectory(File, File)
 */
public class FileUtilsCopyDirectoryToDirectoryTestCase {

    @TempDir
    public File temporaryFolder;
    
    
   private static final String TMP_PREFIX = "junit";
   private static final int TEMP_DIR_ATTEMPTS = 10000;


    @Test
    public void copyDirectoryToDirectoryThrowsIllegalExceptionWithCorrectMessageWhenSrcDirIsNotDirectory()
        throws IOException {
        final File srcDir = File.createTempFile("notadireotry", null, temporaryFolder);
        final File destDir = new File(temporaryFolder, "destinationDirectory");
        destDir.mkdirs();
        final String expectedMessage = String.format("Source '%s' is not a directory", srcDir);
        assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsIllegalArgumentExceptionWithCorrectMessageWhenDstDirIsNotDirectory()
        throws IOException {
        final File srcDir = new File(temporaryFolder, "sourceDirectory");
        srcDir.mkdir();
        final File destDir = new File(temporaryFolder, "notadirectory");
        destDir.createNewFile();
        String expectedMessage = String.format("Destination '%s' is not a directory", destDir);
        assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsNullPointerExceptionWithCorrectMessageWhenSrcDirIsNull() {
        final File srcDir = null;
        final File destinationDirectory = new File(temporaryFolder, "destinationDirectory");
        destinationDirectory.mkdir();
        assertExceptionTypeAndMessage(srcDir, destinationDirectory, NullPointerException.class, "sourceDir");
    }

    @Test
    public void copyDirectoryToDirectoryThrowsNullPointerExceptionWithCorrectMessageWhenDstDirIsNull() {
        final File srcDir = new File(temporaryFolder, "sourceDirectory");
        srcDir.mkdir();
        final File destDir = null;
        assertExceptionTypeAndMessage(srcDir, destDir, NullPointerException.class, "destinationDir");
    }


    @Test
    public void copyFileWrongPermissions() throws IOException {
        
    	 
        final File destDir = createTemporaryFolderIn(null);
        final  Path srcFile = Files.createTempFile("tmp-output", ".xml");
        final Path path = Paths.get(destDir.getAbsolutePath(), "newFile.xml");

        try {
            FileUtils.copyFile(srcFile.toFile(), path.toFile());
        } catch (IllegalArgumentException iae) {
        	iae.printStackTrace();
        }
        
        assertTrue(Files.getPosixFilePermissions(path).contains(PosixFilePermission.OTHERS_READ), Files.getPosixFilePermissions(path).toString());

    }
    
    
    
    private File createTemporaryFolderIn(File parentFolder) throws IOException {
        File createdFolder = null;
        for (int i = 0; i < TEMP_DIR_ATTEMPTS; ++i) {
            // Use createTempFile to get a suitable folder name.
            String suffix = ".tmp";
            File tmpFile = File.createTempFile(TMP_PREFIX, suffix, parentFolder);
            String tmpName = tmpFile.toString();
            // Discard .tmp suffix of tmpName.
            String folderName = tmpName.substring(0, tmpName.length() - suffix.length());
            createdFolder = new File(folderName);
            if (createdFolder.mkdir()) {
                tmpFile.delete();
                return createdFolder;
            }
            tmpFile.delete();
        }
        throw new IOException("Unable to create temporary directory in: "
            + parentFolder.toString() + ". Tried " + TEMP_DIR_ATTEMPTS + " times. "
            + "Last attempted to create: " + createdFolder.toString());
    }
    
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
}
