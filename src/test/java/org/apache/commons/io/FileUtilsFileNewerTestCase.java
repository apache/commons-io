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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This is used to test FileUtils for correctness.
 */
public class FileUtilsFileNewerTestCase {

    @TempDir
    public File temporaryFolder;

    // Test data
    private static final int FILE1_SIZE = 1;
    private static final int FILE2_SIZE = 1024 * 4 + 1;

    private File testFile1;
    private File testFile2;

    @BeforeEach
    public void setUp() throws Exception {
        testFile1 = new File(temporaryFolder, "file1-test.txt");
        testFile2 = new File(temporaryFolder, "file2-test.txt");
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 =
                new BufferedOutputStream(Files.newOutputStream(testFile1.toPath()))) {
            TestUtils.generateTestData(output1, FILE1_SIZE);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testFile2.toPath()))) {
            TestUtils.generateTestData(output, FILE2_SIZE);
        }
    }

    /**
     * Tests the {@code isFileNewer(File, *)} methods which a "normal" file.
     * @throws IOException
     *
     * @see FileUtils#isFileNewer(File, long)
     * @see FileUtils#isFileNewer(File, Date)
     * @see FileUtils#isFileNewer(File, File)
     */
    @Test
    public void testIsFileNewer() throws IOException {
        if (!testFile1.exists()) {
            throw new IllegalStateException("The testFile1 should exist");
        }

        final long fileLastModified = FileUtils.lastModified(testFile1);
        final long TWO_SECOND = 2000;

        testIsFileNewer("two second earlier is not newer" , testFile1, fileLastModified + TWO_SECOND, false);
        testIsFileNewer("same time is not newer" , testFile1, fileLastModified, false);
        testIsFileNewer("two second later is newer" , testFile1, fileLastModified - TWO_SECOND, true);
    }

    /**
     * Tests the {@code isFileNewer(File, *)} methods which a not existing file.
     * @throws IOException if an I/O error occurs.
     *
     * @see FileUtils#isFileNewer(File, long)
     * @see FileUtils#isFileNewer(File, Date)
     * @see FileUtils#isFileNewer(File, File)
     */
    @Test
    public void testIsFileNewerImaginaryFile() throws IOException {
        final File imaginaryFile = new File(temporaryFolder, "imaginaryFile");
        if (imaginaryFile.exists()) {
            throw new IllegalStateException("The imaginary File exists");
        }

        testIsFileNewer("imaginary file can be newer" , imaginaryFile, FileUtils.lastModified(testFile2), false);
    }

    /**
     * Tests the {@code isFileNewer(File, *)} methods which the specified conditions.
     *
     * Creates :
     * <ul>
     * <li>a {@code Date} which represents the time reference</li>
     * <li>a temporary file with the same last modification date as the time reference</li>
     * </ul>
     * Then compares (with the needed {@code isFileNewer} method) the last modification date of
     * the specified file with the specified time reference, the created {@code Date} and the temporary
     * file.
     * <br>
     * The test is successful if the three comparisons return the specified wanted result.
     *
     * @param description describes the tested situation
     * @param file the file of which the last modification date is compared
     * @param time the time reference measured in milliseconds since the epoch
     * @param wantedResult the expected result
     * @throws IOException if an I/O error occurs.
     *
     * @see FileUtils#isFileNewer(File, long)
     * @see FileUtils#isFileNewer(File, Date)
     * @see FileUtils#isFileNewer(File, File)
     */
    protected void testIsFileNewer(final String description, final File file, final long time, final boolean wantedResult) throws IOException  {
        assertEquals(wantedResult, FileUtils.isFileNewer(file, time), description + " - time");
        assertEquals(wantedResult, FileUtils.isFileNewer(file, new Date(time)), description + " - date");

        final File temporaryFile = testFile2;

        temporaryFile.setLastModified(time);
        assertEquals(time, FileUtils.lastModified(temporaryFile), "The temporary file hasn't the right last modification date");
        assertEquals(wantedResult, FileUtils.isFileNewer(file, temporaryFile), description + " - file");
    }

    /**
     * Tests the {@code isFileNewer(File, long)} method without specifying a {@code File}.
     * <br>
     * The test is successful if the method throws an {@code IllegalArgumentException}.
     */
    @Test
    public void testIsFileNewerNoFile() {
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(null,0),
                "file");
    }

    /**
     * Tests the {@code isFileNewer(File, Date)} method without specifying a {@code Date}.
     * <br>
     * The test is successful if the method throws an {@code IllegalArgumentException}.
     */
    @Test
    public void testIsFileNewerNoDate() {
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(testFile1, (Date) null),
                "date");
    }

    /**
     * Tests the {@code isFileNewer(File, File)} method without specifying a reference {@code File}.
     * <br>
     * The test is successful if the method throws an {@code IllegalArgumentException}.
     */
    @Test
    public void testIsFileNewerNoFileReference() {
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(testFile1, (File) null),
                "reference");
    }
}
