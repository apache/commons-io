/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/FileUtilsListFilesTestCase.java,v 1.4 2003/12/30 15:26:16 jeremias Exp $
 * $Revision: 1.4 $
 * $Date: 2003/12/30 15:26:16 $
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
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Test cases for FileUtils.listFiles() methods.
 */
public class FileUtilsListFilesTestCase extends FileBasedTestCase {

    public FileUtilsListFilesTestCase(String name) {
        super(name);
    }
    
    private File getLocalTestDirectory() {
        return new File(getTestDirectory(), "list-files");
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        File dir = getLocalTestDirectory();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdirs();
        File file = new File(dir, "dummy-build.xml");
        FileUtils.touch(file);
        file = new File(dir, "README");
        FileUtils.touch(file);
        
        dir = new File(dir, "subdir1");
        dir.mkdirs();
        file = new File(dir, "dummy-build.xml");
        FileUtils.touch(file);
        file = new File(dir, "dummy-readme.txt");
        FileUtils.touch(file);
        
        dir = new File(dir, "subsubdir1");
        dir.mkdirs();
        file = new File(dir, "dummy-file.txt");
        FileUtils.touch(file);
        file = new File(dir, "dummy-index.html");
        FileUtils.touch(file);
        
        dir = dir.getParentFile();
        dir = new File(dir, "CVS");
        dir.mkdirs();
        file = new File(dir, "Entries");
        FileUtils.touch(file);
        file = new File(dir, "Repository");
        FileUtils.touch(file);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        File dir = getLocalTestDirectory();
        FileUtils.deleteDirectory(dir);
    }
    
    private Collection filesToFilenames(Collection files) {
        Collection filenames = new java.util.ArrayList(files.size());
        Iterator i = files.iterator();
        while (i.hasNext()) {
            filenames.add(((File)i.next()).getName());
        }
        return filenames;
    }
    
    public void testListFilesByExtension() throws Exception {
        String[] extensions = {"xml", "txt"};
        
        Collection files = FileUtils.listFiles(getLocalTestDirectory(), extensions, false);
        assertEquals(1, files.size());
        Collection filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"));
        assertFalse(filenames.contains("README"));
        assertFalse(filenames.contains("dummy-file.txt"));
        
        files = FileUtils.listFiles(getLocalTestDirectory(), extensions, true);
        filenames = filesToFilenames(files);
        assertEquals(4, filenames.size());
        assertTrue(filenames.contains("dummy-file.txt"));
        assertFalse(filenames.contains("dummy-index.html"));
        
        files = FileUtils.listFiles(getLocalTestDirectory(), null, false);
        assertEquals(2, files.size());
        filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"));
        assertTrue(filenames.contains("README"));
        assertFalse(filenames.contains("dummy-file.txt"));
        
    }

    public void testListFiles() throws Exception {
        Collection files;
        Collection filenames;
        IOFileFilter fileFilter;
        IOFileFilter dirFilter;
        
        //First, find non-recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        files = FileUtils.listFiles(getLocalTestDirectory(), fileFilter, null);
        filenames = filesToFilenames(files);
        assertTrue("'dummy-build.xml' is missing", filenames.contains("dummy-build.xml"));
        assertFalse("'dummy-index.html' shouldn't be found", filenames.contains("dummy-index.html"));
        assertFalse("'Entries' shouldn't be found", filenames.contains("Entries"));
        
        //Second, find recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("CVS"));
        files = FileUtils.listFiles(getLocalTestDirectory(), fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue("'dummy-build.xml' is missing", filenames.contains("dummy-build.xml"));
        assertTrue("'dummy-index.html' is missing", filenames.contains("dummy-index.html"));
        assertFalse("'Entries' shouldn't be found", filenames.contains("Entries"));
        
        //Do the same as above but now with the filter coming from FileFilterUtils
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.makeCVSAware(null);
        files = FileUtils.listFiles(getLocalTestDirectory(), fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue("'dummy-build.xml' is missing", filenames.contains("dummy-build.xml"));
        assertTrue("'dummy-index.html' is missing", filenames.contains("dummy-index.html"));
        assertFalse("'Entries' shouldn't be found", filenames.contains("Entries"));

        //Again with the CVS filter but now with a non-null parameter
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.prefixFileFilter("sub");
        dirFilter = FileFilterUtils.makeCVSAware(dirFilter);
        files = FileUtils.listFiles(getLocalTestDirectory(), fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue("'dummy-build.xml' is missing", filenames.contains("dummy-build.xml"));
        assertTrue("'dummy-index.html' is missing", filenames.contains("dummy-index.html"));
        assertFalse("'Entries' shouldn't be found", filenames.contains("Entries"));

        try {
            FileUtils.listFiles(getLocalTestDirectory(), null, null);
            fail("Expected error about null parameter");
        } catch (Exception e) {
            //fine
        }
    }


}
