/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/Attic/IOTestCase.java,v 1.3 2002/01/28 05:01:49 sanders Exp $
 * $Revision: 1.3 $
 * $Date: 2002/01/28 05:01:49 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 *
 */

package org.apache.commons.io;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * <p>
 *  Test Case for the IO classes. </p>
 *
 * <p>
 *  Template for this stolen from Craigs BeanUtils PropertyUtilsTestCase
 * </p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @version $Revision: 1.3 $
 */

public class IOTestCase extends TestCase {

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public IOTestCase(String name) {
        super(name);
    }


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(IOTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
    }


    /**
     *  Test the FileUtils implementation.
     */
    public void testFileUtils() {
        String filename = "src/test/org/apache/commons/io/test.txt";
        String filename2 = "src/test/org/apache/commons/io/test2.txt";
        assertTrue("test.txt extension == \"txt\"", FileUtils.extension(filename).equals("txt"));
        assertTrue("Test file exists", FileUtils.fileExists(filename));
        assertTrue("Second test file does not exist", !FileUtils.fileExists(filename2));
        try {
            FileUtils.fileWrite(filename2, filename);
            assertTrue("Second file was written", FileUtils.fileExists(filename2));
            String file2contents = FileUtils.fileRead(filename2);
            assertTrue("Second file's contents correct", FileUtils.fileRead(filename2).equals(file2contents));
            FileUtils.fileDelete(filename2);
            assertTrue("Second test file does not exist", !FileUtils.fileExists(filename2));
        } catch (Exception e) {
            fail("Error reading or writing second test file: " + filename);
        }

        try {
            String contents = FileUtils.fileRead(filename);
            assertTrue("FileUtils.fileRead()", contents.equals("This is a test"));
        } catch (Exception e) {
            fail("Error loading file: " + filename);
        }
    }


}

