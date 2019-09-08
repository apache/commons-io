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
 * This class ensure the correctness of {@link FileUtils#copyDirectoryToDirectory(File, File)}.
 * TODO: currently does not cover happy cases
 *
 * @see FileUtils#copyDirectoryToDirectory(File, File)
 */
public class FileUtilsCopyDirectoryToDirectoryTestCase {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void copyDirectoryToDirectoryThrowsIllegalExceptionWithCorrectMessageWhenSrcDirIsNotDirectory() throws IOException {
        final File srcDir = temporaryFolder.newFile("notadirectory");
        final File destDir = temporaryFolder.newFolder("destinationDirectory");
        final String expectedMessage = String.format("Source '%s' is not a directory", srcDir);
        assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsIllegalArgumentExceptionWithCorrectMessageWhenDstDirIsNotDirectory() throws IOException {
        final File srcDir = temporaryFolder.newFolder("sourceDirectory");
        final File destDir =  temporaryFolder.newFile("notadirectory");
        String expectedMessage = String.format("Destination '%s' is not a directory", destDir);
        assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsNullPointerExceptionWithCorrectMessageWhenSrcDirIsNull() throws IOException {
        final File srcDir = null;
        final File destinationDirectory =  temporaryFolder.newFolder("destinationDirectory");
        final String expectedMessage = "Source must not be null";
        assertExceptionTypeAndMessage(srcDir, destinationDirectory, NullPointerException.class,  expectedMessage);
    }

    @Test
    public void copyDirectoryToDirectoryThrowsNullPointerExceptionWithCorrectMessageWhenDstDirIsNull() throws IOException {
        final File srcDir = temporaryFolder.newFolder("sourceDirectory");
        final File destDir =  null;
        final String expectedMessage = "Destination must not be null";
        assertExceptionTypeAndMessage(srcDir, destDir, NullPointerException.class, expectedMessage);
    }

    private static void assertExceptionTypeAndMessage(final File srcDir, final File destDir, final Class expectedExceptionType, final String expectedMessage) {
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
