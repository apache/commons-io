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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This class ensure the correctness of {@link FileUtils#copyDirectoryToDirectory(File, File)} (File,File)}.
 * TODO: currently does not cover happy cases
 *
 * @see FileUtils#copyDirectoryToDirectory(File, File)
 */
public class FileUtilsCopyDirectoryToDirectoryTestCase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void copyDirectoryToDirectoryThrowsIllegalExceptionWithCorrectMessageWhenSrcDirIsNotDirectory() throws IOException {
        File srcDir = temporaryFolder.newFile("notadirectory");
        File destDir = temporaryFolder.newFolder("destinationDirectory");
        String expectedMessage = String.format("Source '%s' is not a directory", srcDir);
        assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsIllegalArgumentExceptionWithCorrectMessageWhenDstDirIsNotDirectory() throws IOException {
        File srcDir = temporaryFolder.newFolder("sourceDirectory");
        File destDir =  temporaryFolder.newFile("notadirectory");
        String expectedMessage = String.format("Destination '%s' is not a directory", destDir);
        assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsNullPointerExceptionWithCorrectMessageWhenSrcDirIsNull() throws IOException {
        File srcDir = null;
        File destinationDirectory =  temporaryFolder.newFolder("destinationDirectory");
        String expectedMessage = "Source must not be null";
        assertExceptionTypeAndMessage(srcDir, destinationDirectory, NullPointerException.class,  expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsNullPointerExceptionWithCorrectMessageWhenDstDirIsNull() throws IOException {
        File srcDir = temporaryFolder.newFolder("sourceDirectory");
        File destDir =  null;
        String expectedMessage = "Destination must not be null";
        assertExceptionTypeAndMessage(srcDir, destDir, NullPointerException.class, expectedMessage);
    }

    private static void assertExceptionTypeAndMessage(File srcDir, File destDir, Class expectedExceptionType, String expectedMessage) {
        try {
            FileUtils.copyDirectoryToDirectory(srcDir, destDir);
        } catch (Exception e) {
            String msg = e.getMessage();
            assertEquals(expectedExceptionType, e.getClass());
            assertEquals(expectedMessage, msg);
            return;
        }
        fail();

    }
}
