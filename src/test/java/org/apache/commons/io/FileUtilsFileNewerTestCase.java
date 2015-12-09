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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.testtools.FileBasedTestCase;
import org.apache.commons.io.testtools.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This is used to test FileUtils for correctness.
 */
public class FileUtilsFileNewerTestCase extends FileBasedTestCase {

    // Test data
    private static final int FILE1_SIZE = 1;
    private static final int FILE2_SIZE = 1024 * 4 + 1;

    private final File m_testFile1;
    private final File m_testFile2;

    public FileUtilsFileNewerTestCase() {
        m_testFile1 = new File(getTestDirectory(), "file1-test.txt");
        m_testFile2 = new File(getTestDirectory(), "file2-test.txt");
    }

    /** @see junit.framework.TestCase#setUp() */
    @Before
    public void setUp() throws Exception {
        getTestDirectory().mkdirs();
        if (!m_testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + m_testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(m_testFile1));
        try {
            TestUtils.generateTestData(output1, (long) FILE1_SIZE);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!m_testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + m_testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(m_testFile2));
        try {
            TestUtils.generateTestData(output, (long) FILE2_SIZE);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    /** @see junit.framework.TestCase#tearDown() */
    @After
    public void tearDown() throws Exception {
        m_testFile1.delete();
        m_testFile2.delete();
    }

    /**
     * Tests the <code>isFileNewer(File, *)</code> methods which a "normal" file.
     *
     * @see FileUtils#isFileNewer(File, long)
     * @see FileUtils#isFileNewer(File, Date)
     * @see FileUtils#isFileNewer(File, File)
     */
    @Test
    public void testIsFileNewer() {
        if (!m_testFile1.exists()) {
            throw new IllegalStateException("The m_testFile1 should exist");
        }

        final long fileLastModified = m_testFile1.lastModified();
        final long TWO_SECOND = 2000;

        testIsFileNewer("two second earlier is not newer" , m_testFile1, fileLastModified + TWO_SECOND, false);
        testIsFileNewer("same time is not newer" , m_testFile1, fileLastModified, false);
        testIsFileNewer("two second later is newer" , m_testFile1, fileLastModified - TWO_SECOND, true);
    }

    /**
     * Tests the <code>isFileNewer(File, *)</code> methods which a not existing file.
     *
     * @see FileUtils#isFileNewer(File, long)
     * @see FileUtils#isFileNewer(File, Date)
     * @see FileUtils#isFileNewer(File, File)
     */
    @Test
    public void testIsFileNewerImaginaryFile() {
        final File imaginaryFile = new File(getTestDirectory(), "imaginaryFile");
        if (imaginaryFile.exists()) {
            throw new IllegalStateException("The imaginary File exists");
        }

        testIsFileNewer("imaginary file can be newer" , imaginaryFile, m_testFile2.lastModified(), false);
    }

    /**
     * Tests the <code>isFileNewer(File, *)</code> methods which the specified conditions.
     * 
     * Creates :
     * <ul>
     * <li>a <code>Date</code> which represents the time reference</li>
     * <li>a temporary file with the same last modification date than the time reference</li>
     * </ul>
     * Then compares (with the needed <code>isFileNewer</code> method) the last modification date of
     * the specified file with the specified time reference, the created <code>Date</code> and the temporary
     * file.
     * <br>
     * The test is successfull if the three comparaisons return the specified wanted result.
     *
     * @param description describes the tested situation
     * @param file the file of which the last modification date is compared
     * @param time the time reference measured in milliseconds since the epoch
     * @param wantedResult the expected result
     *
     * @see FileUtils#isFileNewer(File, long)
     * @see FileUtils#isFileNewer(File, Date)
     * @see FileUtils#isFileNewer(File, File)
     */
    protected void testIsFileNewer(final String description, final File file, final long time, final boolean wantedResult)  {
        assertEquals(description + " - time", wantedResult, FileUtils.isFileNewer(file, time));
        assertEquals(description + " - date", wantedResult, FileUtils.isFileNewer(file, new Date(time)));

        final File temporaryFile = m_testFile2;

        temporaryFile.setLastModified(time);
        assertEquals("The temporary file hasn't the right last modification date", time, temporaryFile.lastModified());
        assertEquals(description + " - file", wantedResult, FileUtils.isFileNewer(file, temporaryFile));
    }

    /**
     * Tests the <code>isFileNewer(File, long)</code> method without specifying a <code>File</code>.
     * <br>
     * The test is successfull if the method throws an <code>IllegalArgumentException</code>.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsFileNewerNoFile() {
        FileUtils.isFileNewer(null,0);
        fail("File not specified");
    }

    /**
     * Tests the <code>isFileNewer(File, Date)</code> method without specifying a <code>Date</code>.
     * <br>
     * The test is successfull if the method throws an <code>IllegalArgumentException</code>.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsFileNewerNoDate() {
        FileUtils.isFileNewer(m_testFile1, (Date) null);
        fail("Date not specified");
    }

    /**
     * Tests the <code>isFileNewer(File, File)</code> method without specifying a reference <code>File</code>.
     * <br>
     * The test is successfull if the method throws an <code>IllegalArgumentException</code>.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsFileNewerNoFileReference() {
        FileUtils.isFileNewer(m_testFile1, (File) null);
        fail("Reference file not specified");
    }
}
