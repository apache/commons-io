/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/filefilter/FileFilterTestCase.java,v 1.7 2003/02/25 09:56:28 tobrien Exp $
 * $Revision: 1.7 $
 * $Date: 2003/02/25 09:56:28 $
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

package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.IOException;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Used to test an unknown FileFilter
 */
public final class FileFilterTestCase
    extends TestCase
{

    public void setUp()
    {
    }

    public void tearDown()
    {
    }

    public FileFilterTestCase( String name )
    {
        super( name );
    }

    public void assertFiltering(FileFilter filter, File file, boolean expected)
        throws Exception
    {
       // Note. This only tests the (File, String) version if the parent of 
       //       the File passed in is not null
       assertTrue(
            "Filter(File) "+filter.getClass().getName()+" not "+expected+" for "+file, 
               (filter.accept(file) == expected) 
               );

       if(file != null && file.getParentFile() != null) {
           assertTrue(
                "Filter(File, String) "+filter.getClass().getName()+" not "+expected+" for "+file, 
                (filter.accept(file.getParentFile(), file.getName()) == expected)
           );
       } else if( file == null ) {
	   assertTrue(
		      "Filter(File, String) "+filter.getClass().getName()+" not "+expected+" for null",
		      filter.accept( file ) == expected );
       }
    }

    public void testExtension() throws Exception {
        FileFilter filter = new ExtensionFileFilter( new String[] { "tes", "est" } ); 
        assertFiltering( filter, new File("fred.tes"), true);
        assertFiltering( filter, new File("fred.est"), true);
        assertFiltering( filter, new File("fred.exe"), false);
        assertFiltering( filter, new File("fred"), false);

        // SHOULD THESE WORK???
        assertFiltering( filter, new File(".tes"), true);
        assertFiltering( filter, new File("fred.test"), true);
    }

    public void testDirectory() throws Exception {
        FileFilter filter = new DirectoryFileFilter();

        assertFiltering( filter, new File("src/"), true);
        assertFiltering( filter, new File("src/java/"), true);
        
        assertFiltering( filter, new File("project.xml"), false);
	
	assertFiltering( filter, new File("imaginary"), false);
        assertFiltering( filter, new File("imaginary/"), false);

        assertFiltering( filter, new File("STATUS.html"), false);
    }

    public void testPrefix() throws Exception {
        FileFilter filter = new PrefixFileFilter( new String[] { "foo", "bar" } );
        assertFiltering( filter, new File("foo.test"), true);
        assertFiltering( filter, new File("foo"), true);
        assertFiltering( filter, new File("bar"), true);
        assertFiltering( filter, new File("food/"), true);
        assertFiltering( filter, new File("barred\\"), true);
        assertFiltering( filter, new File("test"), false);
        assertFiltering( filter, new File("fo_o.test"), false);
        assertFiltering( filter, new File("abar.exe"), false);
    }

    public void testNull() throws Exception {
        FileFilter filter = FileFilterUtils.nullFileFilter();
        assertFiltering( filter, new File("foo.test"), false);
        assertFiltering( filter, new File("foo"), false);
        assertFiltering( filter, null, true);
    }

}
