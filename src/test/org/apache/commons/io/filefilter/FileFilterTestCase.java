/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.filefilter;

import java.io.File;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Used to test an unknown FileFilter
 */
public class FileFilterTestCase extends TestCase {

    public FileFilterTestCase(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static TestSuite suite() {
        return new TestSuite(FileFilterTestCase.class);
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public void assertFiltering(IOFileFilter filter, File file, boolean expected) throws Exception {
        // Note. This only tests the (File, String) version if the parent of 
        //       the File passed in is not null
        assertTrue(
            "Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file,
            (filter.accept(file) == expected));

        if (file != null && file.getParentFile() != null) {
            assertTrue(
                "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file,
                (filter.accept(file.getParentFile(), file.getName()) == expected));
        } else if (file == null) {
            assertTrue(
                "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for null",
                filter.accept(file) == expected);
        }
    }

    public void testSuffix() throws Exception {
        IOFileFilter filter = new SuffixFileFilter(new String[] { "tes", "est" });
        assertFiltering(filter, new File("fred.tes"), true);
        assertFiltering(filter, new File("fred.est"), true);
        assertFiltering(filter, new File("fred.EST"), false); //case-sensitive
        assertFiltering(filter, new File("fred.exe"), false);
        assertFiltering(filter, new File("fred"), false);
        assertFiltering(filter, new File(".tes"), true);
        assertFiltering(filter, new File("fred.test"), true);
        
        filter = new SuffixFileFilter("est");
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("fred"), false);
        
        try {
            new SuffixFileFilter((String) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testDirectory() throws Exception {
        IOFileFilter filter = new DirectoryFileFilter();

        assertFiltering(filter, new File("src/"), true);
        assertFiltering(filter, new File("src/java/"), true);

        assertFiltering(filter, new File("project.xml"), false);

        assertFiltering(filter, new File("imaginary"), false);
        assertFiltering(filter, new File("imaginary/"), false);

        assertFiltering(filter, new File("STATUS.html"), false);
    }

    public void testPrefix() throws Exception {
        IOFileFilter filter = new PrefixFileFilter(new String[] { "foo", "bar" });
        assertFiltering(filter, new File("foo.test"), true);
        assertFiltering(filter, new File("FOO.test"), false);  //case-sensitive
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("food/"), true);
        assertFiltering(filter, new File("barred\\"), true);
        assertFiltering(filter, new File("test"), false);
        assertFiltering(filter, new File("fo_o.test"), false);
        assertFiltering(filter, new File("abar.exe"), false);
        
        filter = new PrefixFileFilter("tes");
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("fred"), false);
        
        try {
            new PrefixFileFilter((String) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }
    
    public void testNameFilter() throws Exception {
        IOFileFilter filter = new NameFileFilter(new String[] { "foo", "bar" });
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("fred"), false);

        // repeat for a List
        java.util.ArrayList list = new java.util.ArrayList();
        list.add("foo");
        list.add("bar");
        filter = new NameFileFilter(list);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("fred"), false);

        filter = new NameFileFilter("foo");
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("FOO"), false); //case-sensitive
        assertFiltering(filter, new File("barfoo"), false);
        assertFiltering(filter, new File("foobar"), false);
        assertFiltering(filter, new File("fred"), false);
    }

    public void testTrue() throws Exception {
        IOFileFilter filter = FileFilterUtils.trueFileFilter();
        assertFiltering(filter, new File("foo.test"), true);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, null, true);
    }

    public void testFalse() throws Exception {
        IOFileFilter filter = FileFilterUtils.falseFileFilter();
        assertFiltering(filter, new File("foo.test"), false);
        assertFiltering(filter, new File("foo"), false);
        assertFiltering(filter, null, false);
    }

    public void testNot() throws Exception {
        IOFileFilter filter = FileFilterUtils.notFileFilter(FileFilterUtils.trueFileFilter());
        assertFiltering(filter, new File("foo.test"), false);
        assertFiltering(filter, new File("foo"), false);
        assertFiltering(filter, null, false);
        try {
            new NotFileFilter(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testAnd() throws Exception {
        IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        assertFiltering(new AndFileFilter(trueFilter, trueFilter), new File("foo.test"), true);
        assertFiltering(new AndFileFilter(trueFilter, falseFilter), new File("foo.test"), false);
        assertFiltering(new AndFileFilter(falseFilter, trueFilter), new File("foo.test"), false);
        assertFiltering(new AndFileFilter(falseFilter, falseFilter), new File("foo.test"), false);
        try {
            new AndFileFilter(falseFilter, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testOr() throws Exception {
        IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        assertFiltering(new OrFileFilter(trueFilter, trueFilter), new File("foo.test"), true);
        assertFiltering(new OrFileFilter(trueFilter, falseFilter), new File("foo.test"), true);
        assertFiltering(new OrFileFilter(falseFilter, trueFilter), new File("foo.test"), true);
        assertFiltering(new OrFileFilter(falseFilter, falseFilter), new File("foo.test"), false);
        try {
            new OrFileFilter(falseFilter, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

}
