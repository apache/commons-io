/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/FileUtilsFileNewerTestCase.java,v 1.4 2003/12/30 15:26:59 jeremias Exp $
 * $Revision: 1.4 $
 * $Date: 2003/12/30 15:26:59 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.commons.io;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.testtools.*;

/**
 * This is used to test FileUtils for correctness.
 *
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class FileUtilsFileNewerTestCase extends FileBasedTestCase {

    // Test data
    private static final int FILE1_SIZE = 1;
    private static final int FILE2_SIZE = 1024 * 4 + 1;

    private File m_testFile1;
    private File m_testFile2;

    public FileUtilsFileNewerTestCase(String name) {
        super(name);
        
        m_testFile1 = new File(getTestDirectory(), "file1-test.txt");
        m_testFile2 = new File(getTestDirectory(), "file2-test.txt");
    }

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        getTestDirectory().mkdirs();
        createFile(m_testFile1, FILE1_SIZE);
        createFile(m_testFile2, FILE2_SIZE);
    }

    /** @see junit.framework.TestCase#tearDown() */
    protected void tearDown() throws Exception {
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
    public void testIsFileNewer() {
        if (!m_testFile1.exists())
            throw new IllegalStateException("The m_testFile1 should exist");

        long fileLastModified = m_testFile1.lastModified();
        final long ONE_SECOND = 1000;

        testIsFileNewer("one second earlier is not newer" , m_testFile1, fileLastModified + ONE_SECOND, false);
        testIsFileNewer("same time is not newer" , m_testFile1, fileLastModified, false);
        testIsFileNewer("one second later is newer" , m_testFile1, fileLastModified - ONE_SECOND, true);
    }

    /**
     * Tests the <code>isFileNewer(File, *)</code> methods which a not existing file.
     *
     * @see FileUtils#isFileNewer(File, long)
     * @see FileUtils#isFileNewer(File, Date)
     * @see FileUtils#isFileNewer(File, File)
     */
    public void testIsFileNewerImaginaryFile() {
        File imaginaryFile = new File(getTestDirectory(), "imaginaryFile");
        if (imaginaryFile.exists())
            throw new IllegalStateException("The imaginary File exists");

        testIsFileNewer("imaginary file can be newer" , imaginaryFile, 0, false);
    }

    /**
     * Tests the <code>isFileNewer(File, *)</code> methods which the specified conditions.
     * <p/>
     * Creates :
     * <ul>
     * <li>a <code>Date</code> which represents the time reference</li>
     * <li>a temporary file with the same last modification date than the time reference</li>
     * </ul>
     * Then compares (with the needed <code>isFileNewer</code> method) the last modification date of 
     * the specified file with the specified time reference, the created <code>Date</code> and the temporary 
     * file.
     * <br/>
     * The test is successfull if the three comparaisons return the specified wanted result.
     *
     * @param description describes the tested situation
     * @param file the file of which the last modification date is compared
     * @param timeMillis the time reference measured in milliseconds since the epoch 
     *
     * @see FileUtils#isFileNewer(File, long)
     * @see FileUtils#isFileNewer(File, Date)
     * @see FileUtils#isFileNewer(File, File)
     */
    protected void testIsFileNewer(String description, File file, long time, boolean wantedResult)  {
        assertEquals(description + " - time", wantedResult, FileUtils.isFileNewer(file, time));
        assertEquals(description + " - date", wantedResult, FileUtils.isFileNewer(file, new Date(time)));
        
        File temporaryFile = m_testFile2;

        temporaryFile.setLastModified(time);
        if (temporaryFile.lastModified() != time)
            throw new IllegalStateException("The temporary file hasn't the right last modification date");
        assertEquals(description + " - file", wantedResult, FileUtils.isFileNewer(file, temporaryFile));
    }

    /**
     * Tests the <code>isFileNewer(File, long)</code> method without specifying a <code>File</code>.
     * <br/>
     * The test is successfull if the method throws an <code>IllegalArgumentException</code>. 
     */
    public void testIsFileNewerNoFile() {
        try {
            FileUtils.isFileNewer(null,0);
            fail("File not specified");
        } catch (IllegalArgumentException e) {}
    }

    /**
     * Tests the <code>isFileNewer(File, Date)</code> method without specifying a <code>Date</code>.
     * <br/>
     * The test is successfull if the method throws an <code>IllegalArgumentException</code>. 
     */
    public void testIsFileNewerNoDate() {
        try {
            FileUtils.isFileNewer(m_testFile1, (Date) null);
            fail("Date not specified");
        } catch (IllegalArgumentException e) {}
    }

    /**
     * Tests the <code>isFileNewer(File, File)</code> method without specifying a reference <code>File</code>.
     * <br/>
     * The test is successfull if the method throws an <code>IllegalArgumentException</code>. 
     */
    public void testIsFileNewerNoFileReference() {
        try {
            FileUtils.isFileNewer(m_testFile1, (File) null);
            fail("Reference file not specified");
        } catch (IllegalArgumentException e) {}
    }
}
