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
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Used to test FileFilterUtils.
 */
public class FileFilterTestCase extends FileBasedTestCase {

    /**
     * The subversion directory name.
     */
    static final String SVN_DIR_NAME = ".svn";
    
    private static final boolean WINDOWS = (File.separatorChar == '\\');

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
        getTestDirectory().mkdirs();
    }

    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(getTestDirectory());
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
        File testFile = new File( "test" );
        File fredFile = new File( "fred" );
        assertFiltering(filter, new File("fred.tes"), true);
        assertFiltering(filter, new File("fred.est"), true);
        assertFiltering(filter, new File("fred.EST"), false); //case-sensitive
        assertFiltering(filter, new File("fred.exe"), false);

        filter = FileFilterUtils.orFileFilter( 
        			FileFilterUtils.suffixFileFilter( "tes" ),
        			FileFilterUtils.suffixFileFilter( "est" ) );
        assertFiltering(filter, new File("fred"), false);
        assertFiltering(filter, new File(".tes"), true);
        assertFiltering(filter, new File("fred.test"), true);

        filter = new SuffixFileFilter("est");
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("fred"), false);

        assertTrue( filter.accept( testFile.getParentFile(), testFile.getName() ) );
        assertTrue( !filter.accept( fredFile.getParentFile(), fredFile.getName() ) );

        List prefixes = Arrays.asList( new String[] { "ood", "red" } );
        IOFileFilter listFilter = new SuffixFileFilter( prefixes );

        assertTrue( !listFilter.accept( testFile.getParentFile(), testFile.getName() ) );
        assertTrue( listFilter.accept( fredFile.getParentFile(), fredFile.getName() ) );

        try {
            new SuffixFileFilter((String) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new SuffixFileFilter((String[]) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new SuffixFileFilter((List) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
}

    public void testDirectory() throws Exception {
        // XXX: This test presumes the current working dir is the base dir of the source checkout.
        IOFileFilter filter = new DirectoryFileFilter();

        assertFiltering(filter, new File("src/"), true);
        assertFiltering(filter, new File("src/java/"), true);

        assertFiltering(filter, new File("project.xml"), false);

        assertFiltering(filter, new File("imaginary"), false);
        assertFiltering(filter, new File("imaginary/"), false);

        assertFiltering(filter, new File("LICENSE.txt"), false);
        
        assertSame(DirectoryFileFilter.DIRECTORY, DirectoryFileFilter.INSTANCE);
    }

    public void testFiles() throws Exception {
        // XXX: This test presumes the current working dir is the base dir of the source checkout.
        IOFileFilter filter = FileFileFilter.FILE;
        
        assertFiltering(filter, new File("src/"), false);
        assertFiltering(filter, new File("src/java/"), false);
        
        assertFiltering(filter, new File("project.xml"), true);
        
        assertFiltering(filter, new File("imaginary"), false);
        assertFiltering(filter, new File("imaginary/"), false);
        
        assertFiltering(filter, new File("LICENSE.txt"), true);
    }

    public void testPrefix() throws Exception {
        IOFileFilter filter = new PrefixFileFilter(new String[] { "foo", "bar" });
        File testFile = new File( "test" );
        File fredFile = new File( "fred" );

        assertFiltering(filter, new File("foo.test"), true);
        assertFiltering(filter, new File("FOO.test"), false);  //case-sensitive
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("food/"), true);

        filter = FileFilterUtils.prefixFileFilter( "bar" );
        assertFiltering(filter, new File("barred\\"), true);
        assertFiltering(filter, new File("test"), false);
        assertFiltering(filter, new File("fo_o.test"), false);
        assertFiltering(filter, new File("abar.exe"), false);

        filter = new PrefixFileFilter("tes");
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("fred"), false);

        assertTrue( filter.accept( testFile.getParentFile(), testFile.getName() ) );
        assertTrue( !filter.accept( fredFile.getParentFile(), fredFile.getName() ) );

        List prefixes = Arrays.asList( new String[] { "foo", "fre" } );
        IOFileFilter listFilter = new PrefixFileFilter( prefixes );

        assertTrue( !listFilter.accept( testFile.getParentFile(), testFile.getName() ) );
        assertTrue( listFilter.accept( fredFile.getParentFile(), fredFile.getName() ) );
        
        
        try {
            new PrefixFileFilter((String) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new PrefixFileFilter((String[]) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new PrefixFileFilter((List) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testNameFilter() throws Exception {
        IOFileFilter filter = new NameFileFilter(new String[] { "foo", "bar" });
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("fred"), false);
        
        filter = new NameFileFilter(new String[] { "foo", "bar" }, IOCase.SENSITIVE);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("FOO"), false);
        assertFiltering(filter, new File("BAR"), false);
        
        filter = new NameFileFilter(new String[] { "foo", "bar" }, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("FOO"), true);
        assertFiltering(filter, new File("BAR"), true);
        
        filter = new NameFileFilter(new String[] { "foo", "bar" }, IOCase.SYSTEM);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("FOO"), WINDOWS);
        assertFiltering(filter, new File("BAR"), WINDOWS);
        
        filter = new NameFileFilter(new String[] { "foo", "bar" }, (IOCase) null);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("FOO"), false);
        assertFiltering(filter, new File("BAR"), false);

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

    public void testNameFilterNullArgument() throws Exception {
    	String test = null;
    	try {
    		new NameFileFilter(test);
    		fail( "constructing a NameFileFilter with a null String argument should fail.");
    	} catch( IllegalArgumentException iae ) {
    	}
    }

    public void testNameFilterNullArrayArgument() throws Exception {
    	String[] test = null;
    	try {
    		new NameFileFilter(test);
    		fail( "constructing a NameFileFilter with a null String[] argument should fail.");
    	} catch( IllegalArgumentException iae ) {
    	}
    }

    public void testNameFilterNullListArgument() throws Exception {
    	List test = null;
    	try {
    		new NameFileFilter(test);
    		fail( "constructing a NameFileFilter with a null List argument should fail.");
    	} catch( IllegalArgumentException iae ) {
    	}
    }

    public void testTrue() throws Exception {
        IOFileFilter filter = FileFilterUtils.trueFileFilter();
        assertFiltering(filter, new File("foo.test"), true);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, null, true);
        assertSame(TrueFileFilter.TRUE, TrueFileFilter.INSTANCE);
    }

    public void testFalse() throws Exception {
        IOFileFilter filter = FileFilterUtils.falseFileFilter();
        assertFiltering(filter, new File("foo.test"), false);
        assertFiltering(filter, new File("foo"), false);
        assertFiltering(filter, null, false);
        assertSame(FalseFileFilter.FALSE, FalseFileFilter.INSTANCE);
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

        List filters = new ArrayList();
        assertFiltering( new AndFileFilter( filters ), new File( "test" ), false );
        assertFiltering( new AndFileFilter(), new File( "test" ), false );
        
        try {
            new AndFileFilter(falseFilter, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        AndFileFilter f = new AndFileFilter((List) null);
        assertEquals(true, f.getFileFilters().isEmpty());
    }

    public void testOr() throws Exception {
        IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        File testFile = new File( "foo.test" );
        assertFiltering(new OrFileFilter(trueFilter, trueFilter), testFile, true);
        assertFiltering(new OrFileFilter(trueFilter, falseFilter), testFile, true);
        assertFiltering(new OrFileFilter(falseFilter, trueFilter), testFile, true);
        assertFiltering(new OrFileFilter(falseFilter, falseFilter), testFile, false);
        assertFiltering(new OrFileFilter(), testFile, false);
        
        List filters = new ArrayList();
        filters.add( trueFilter );
        filters.add( falseFilter );

        OrFileFilter orFilter = new OrFileFilter( filters );

        assertFiltering(orFilter, testFile, true);
        assertEquals( orFilter.getFileFilters(), filters );
        orFilter.removeFileFilter( trueFilter );
        assertFiltering(orFilter, testFile, false);
        orFilter.setFileFilters( filters );
        assertFiltering(orFilter, testFile, true);
        
        assertTrue( orFilter.accept( testFile.getParentFile(), testFile.getName() ) );
        orFilter.removeFileFilter( trueFilter );
        assertTrue( !orFilter.accept( testFile.getParentFile(), testFile.getName() ) );
        
        try {
            new OrFileFilter(falseFilter, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        OrFileFilter f = new OrFileFilter((List) null);
        assertEquals(true, f.getFileFilters().isEmpty());
    }

    public void testDeprecatedWildcard() throws Exception {
        IOFileFilter filter = new WildcardFilter("*.txt");
        List patternList = Arrays.asList( new String[] { "*.txt", "*.xml", "*.gif" } );
        IOFileFilter listFilter = new WildcardFilter( patternList );
        File txtFile = new File( "test.txt" );
        File bmpFile = new File( "test.bmp" );
        File dir = new File( "src/java" );
        
        assertFiltering(filter, new File("log.txt"), true);
//        assertFiltering(filter, new File("log.txt.bak"), false);

        filter = new WildcardFilter("log?.txt");
        assertFiltering(filter, new File("log1.txt"), true);
        assertFiltering(filter, new File("log12.txt"), false);

        filter = new WildcardFilter("open??.????04");
        assertFiltering(filter, new File("openAB.102504"), true);
        assertFiltering(filter, new File("openA.102504"), false);
        assertFiltering(filter, new File("openXY.123103"), false);
//        assertFiltering(filter, new File("openAB.102504.old"), false);

        filter = new WildcardFilter(new String[] {"*.java", "*.class"});
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.class"), true);
        assertFiltering(filter, new File("Test.jsp"), false);

        assertFiltering(listFilter, new File("Test.txt"), true);
        assertFiltering(listFilter, new File("Test.xml"), true);
        assertFiltering(listFilter, new File("Test.gif"), true);
        assertFiltering(listFilter, new File("Test.bmp"), false);

        assertTrue( listFilter.accept( txtFile ) );
        assertTrue( !listFilter.accept( bmpFile ) );
        assertTrue( !listFilter.accept( dir ) );

        assertTrue( listFilter.accept( txtFile.getParentFile(), txtFile.getName() ) );
        assertTrue( !listFilter.accept( bmpFile.getParentFile(), bmpFile.getName() ) );
        assertTrue( !listFilter.accept( dir.getParentFile(), dir.getName() ) );

        try {
            new WildcardFilter((String) null);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            new WildcardFilter((String[]) null);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            new WildcardFilter((List) null);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testWildcard() throws Exception {
        IOFileFilter filter = new WildcardFileFilter("*.txt");
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), false);
        
        filter = new WildcardFileFilter("*.txt", IOCase.SENSITIVE);
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), false);
        
        filter = new WildcardFileFilter("*.txt", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), true);
        
        filter = new WildcardFileFilter("*.txt", IOCase.SYSTEM);
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), WINDOWS);
        
        filter = new WildcardFileFilter("*.txt", (IOCase) null);
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), false);
        
        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"});
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.class"), true);
        assertFiltering(filter, new File("Test.jsp"), false);
        
        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"}, IOCase.SENSITIVE);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.JAVA"), false);
        
        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"}, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.JAVA"), true);
        
        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"}, IOCase.SYSTEM);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.JAVA"), WINDOWS);
        
        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"}, (IOCase) null);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.JAVA"), false);
        
        List patternList = Arrays.asList( new String[] { "*.txt", "*.xml", "*.gif" } );
        IOFileFilter listFilter = new WildcardFileFilter( patternList );
        assertFiltering(listFilter, new File("Test.txt"), true);
        assertFiltering(listFilter, new File("Test.xml"), true);
        assertFiltering(listFilter, new File("Test.gif"), true);
        assertFiltering(listFilter, new File("Test.bmp"), false);
        
        File txtFile = new File( "test.txt" );
        File bmpFile = new File( "test.bmp" );
        File dir = new File( "src/java" );
        assertTrue( listFilter.accept( txtFile ) );
        assertTrue( !listFilter.accept( bmpFile ) );
        assertTrue( !listFilter.accept( dir ) );
        
        assertTrue( listFilter.accept( txtFile.getParentFile(), txtFile.getName() ) );
        assertTrue( !listFilter.accept( bmpFile.getParentFile(), bmpFile.getName() ) );
        assertTrue( !listFilter.accept( dir.getParentFile(), dir.getName() ) );
        
        try {
            new WildcardFileFilter((String) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new WildcardFileFilter((String[]) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new WildcardFileFilter((List) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    public void testDelegateFileFilter() throws Exception {
    	OrFileFilter orFilter = new OrFileFilter();
    	File testFile = new File( "test.txt" );

    	IOFileFilter filter = new DelegateFileFilter((FileFilter) orFilter);
    	assertFiltering( filter, testFile, false );

    	filter = new DelegateFileFilter((FilenameFilter) orFilter);
    	assertFiltering( filter, testFile, false );

    	try {
    		new DelegateFileFilter((FileFilter) null);
    		fail();
    	} catch( IllegalArgumentException iae ) {
    	}

    	try {
    		new DelegateFileFilter((FilenameFilter) null);
    		fail();
    	} catch( IllegalArgumentException iae ) {
    	}

    }

    public void testMakeCVSAware() throws Exception {
        IOFileFilter filter1 = FileFilterUtils.makeCVSAware(null);
        IOFileFilter filter2 = FileFilterUtils.makeCVSAware(FileFilterUtils
            .nameFileFilter("test-file1.txt"));

        File file = new File(getTestDirectory(), "CVS");
        file.mkdirs();
        assertFiltering(filter1, file, false);
        assertFiltering(filter2, file, false);
        FileUtils.deleteDirectory(file);

        file = new File(getTestDirectory(), "test-file1.txt");
        createFile(file, 0);
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, true);

        file = new File(getTestDirectory(), "test-file2.log");
        createFile(file, 0);
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, false);

        file = new File(getTestDirectory(), "CVS");
        createFile(file, 0);
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, false);
    }
         
    public void testMakeSVNAware() throws Exception {
        IOFileFilter filter1 = FileFilterUtils.makeSVNAware(null);
        IOFileFilter filter2 = FileFilterUtils.makeSVNAware(FileFilterUtils
            .nameFileFilter("test-file1.txt"));

        File file = new File(getTestDirectory(), SVN_DIR_NAME);
        file.mkdirs();
        assertFiltering(filter1, file, false);
        assertFiltering(filter2, file, false);
        FileUtils.deleteDirectory(file);

        file = new File(getTestDirectory(), "test-file1.txt");
        createFile(file, 0);
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, true);

        file = new File(getTestDirectory(), "test-file2.log");
        createFile(file, 0);
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, false);

        file = new File(getTestDirectory(), SVN_DIR_NAME);
        createFile(file, 0);
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, false);
    }

    public void testAgeFilter() throws Exception {
        File oldFile = new File(getTestDirectory(), "old.txt");
        File reference = new File(getTestDirectory(), "reference.txt");
        File newFile = new File(getTestDirectory(), "new.txt");

        createFile(oldFile, 0);

        do {
            try { 
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                // ignore
            }
            createFile(reference, 0);
        } while( oldFile.lastModified() == reference.lastModified() );

        Date date = new Date();
        long now = date.getTime();

        do {
            try { 
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                // ignore
            }
            createFile(newFile, 0);
        } while( reference.lastModified() == newFile.lastModified() );

        IOFileFilter filter1 = FileFilterUtils.ageFileFilter(now);
        IOFileFilter filter2 = FileFilterUtils.ageFileFilter(now, true);
        IOFileFilter filter3 = FileFilterUtils.ageFileFilter(now, false);
        IOFileFilter filter4 = FileFilterUtils.ageFileFilter(date);
        IOFileFilter filter5 = FileFilterUtils.ageFileFilter(date, true);
        IOFileFilter filter6 = FileFilterUtils.ageFileFilter(date, false);
        IOFileFilter filter7 = FileFilterUtils.ageFileFilter(reference);
        IOFileFilter filter8 = FileFilterUtils.ageFileFilter(reference, true);
        IOFileFilter filter9 = FileFilterUtils.ageFileFilter(reference, false);

        assertFiltering(filter1, oldFile, true);
        assertFiltering(filter2, oldFile, true);
        assertFiltering(filter3, oldFile, false);
        assertFiltering(filter4, oldFile, true);
        assertFiltering(filter5, oldFile, true);
        assertFiltering(filter6, oldFile, false);
        assertFiltering(filter7, oldFile, true);
        assertFiltering(filter8, oldFile, true);
        assertFiltering(filter9, oldFile, false);
        assertFiltering(filter1, newFile, false);
        assertFiltering(filter2, newFile, false);
        assertFiltering(filter3, newFile, true);
        assertFiltering(filter4, newFile, false);
        assertFiltering(filter5, newFile, false);
        assertFiltering(filter6, newFile, true);
        assertFiltering(filter7, newFile, false);
        assertFiltering(filter8, newFile, false);
        assertFiltering(filter9, newFile, true);
    }

    public void testSizeFilter() throws Exception {
        File smallFile = new File(getTestDirectory(), "small.txt");
        createFile(smallFile, 32);
        File largeFile = new File(getTestDirectory(), "large.txt");
        createFile(largeFile, 128);
        IOFileFilter filter1 = FileFilterUtils.sizeFileFilter(64);
        IOFileFilter filter2 = FileFilterUtils.sizeFileFilter(64, true);
        IOFileFilter filter3 = FileFilterUtils.sizeFileFilter(64, false);

        assertFiltering(filter1, smallFile, false);
        assertFiltering(filter2, smallFile, false);
        assertFiltering(filter3, smallFile, true);
        assertFiltering(filter1, largeFile, true);
        assertFiltering(filter2, largeFile, true);
        assertFiltering(filter3, largeFile, false);

        // size range tests
        IOFileFilter filter4 = FileFilterUtils.sizeRangeFileFilter(33, 127);
        IOFileFilter filter5 = FileFilterUtils.sizeRangeFileFilter(32, 127);
        IOFileFilter filter6 = FileFilterUtils.sizeRangeFileFilter(33, 128);
        IOFileFilter filter7 = FileFilterUtils.sizeRangeFileFilter(31, 129);
        IOFileFilter filter8 = FileFilterUtils.sizeRangeFileFilter(128, 128);

        assertFiltering(filter4, smallFile, false);
        assertFiltering(filter4, largeFile, false);
        assertFiltering(filter5, smallFile, true);
        assertFiltering(filter5, largeFile, false);
        assertFiltering(filter6, smallFile, false);
        assertFiltering(filter6, largeFile, true);
        assertFiltering(filter7, smallFile, true);
        assertFiltering(filter7, largeFile, true);
        assertFiltering(filter8, largeFile, true);

        try {
            FileFilterUtils.sizeFileFilter(-1);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testHidden() throws Exception {
        File hiddenDir = new File(SVN_DIR_NAME);
        if (hiddenDir.exists()) {
            assertFiltering(HiddenFileFilter.HIDDEN,  hiddenDir, hiddenDir.isHidden());
            assertFiltering(HiddenFileFilter.VISIBLE, hiddenDir, !hiddenDir.isHidden());
        }
        assertFiltering(HiddenFileFilter.HIDDEN,  getTestDirectory(), false);
        assertFiltering(HiddenFileFilter.VISIBLE, getTestDirectory(), true);
    }

    public void testCanRead() throws Exception {
        File readOnlyFile = new File(getTestDirectory(), "read-only-file1.txt");
        createFile(readOnlyFile, 32);
        readOnlyFile.setReadOnly();
        assertFiltering(CanReadFileFilter.CAN_READ,  readOnlyFile, true);
        assertFiltering(CanReadFileFilter.CANNOT_READ,  readOnlyFile, false);
        assertFiltering(CanReadFileFilter.READ_ONLY, readOnlyFile, true);
        readOnlyFile.delete();
    }

    public void testCanWrite() throws Exception {
        File readOnlyFile = new File(getTestDirectory(), "read-only-file2.txt");
        createFile(readOnlyFile, 32);
        readOnlyFile.setReadOnly();
        assertFiltering(CanWriteFileFilter.CAN_WRITE,    getTestDirectory(), true);
        assertFiltering(CanWriteFileFilter.CANNOT_WRITE, getTestDirectory(), false);
        assertFiltering(CanWriteFileFilter.CAN_WRITE,    readOnlyFile, false);
        assertFiltering(CanWriteFileFilter.CANNOT_WRITE, readOnlyFile, true);
        readOnlyFile.delete();
    }

    public void testEmpty() throws Exception {

        // Empty Dir        
        File emptyDir  = new File(getTestDirectory(), "empty-dir");
        emptyDir.mkdirs();
        assertFiltering(EmptyFileFilter.EMPTY, emptyDir, true);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyDir, false);

        // Empty File
        File emptyFile = new File(emptyDir, "empty-file.txt");
        createFile(emptyFile, 0);
        assertFiltering(EmptyFileFilter.EMPTY, emptyFile, true);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyFile, false);

        // Not Empty Dir
        assertFiltering(EmptyFileFilter.EMPTY, emptyDir, false);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyDir, true);

        // Not Empty File
        File notEmptyFile = new File(emptyDir, "not-empty-file.txt");
        createFile(notEmptyFile, 32);
        assertFiltering(EmptyFileFilter.EMPTY, notEmptyFile, false);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, notEmptyFile, true);
        FileUtils.forceDelete(emptyDir);
    }

    //-----------------------------------------------------------------------
    public void testMakeDirectoryOnly() throws Exception {
        assertSame(DirectoryFileFilter.DIRECTORY, FileFilterUtils.makeDirectoryOnly(null));
        
        IOFileFilter filter = FileFilterUtils.makeDirectoryOnly(
                FileFilterUtils.nameFileFilter("B"));
        
        File fileA = new File(getTestDirectory(), "A");
        File fileB = new File(getTestDirectory(), "B");
        
        fileA.mkdirs();
        fileB.mkdirs();
        
        assertFiltering(filter, fileA, false);
        assertFiltering(filter, fileB, true);
        
        FileUtils.deleteDirectory(fileA);
        FileUtils.deleteDirectory(fileB);
        
        createFile(fileA, 32);
        createFile(fileB, 32);
        
        assertFiltering(filter, fileA, false);
        assertFiltering(filter, fileB, false);
        
        fileA.delete();
        fileB.delete();
    }
         
    //-----------------------------------------------------------------------
    public void testMakeFileOnly() throws Exception {
        assertSame(FileFileFilter.FILE, FileFilterUtils.makeFileOnly(null));
        
        IOFileFilter filter = FileFilterUtils.makeFileOnly(
                FileFilterUtils.nameFileFilter("B"));
        
        File fileA = new File(getTestDirectory(), "A");
        File fileB = new File(getTestDirectory(), "B");
        
        fileA.mkdirs();
        fileB.mkdirs();
        
        assertFiltering(filter, fileA, false);
        assertFiltering(filter, fileB, false);
        
        FileUtils.deleteDirectory(fileA);
        FileUtils.deleteDirectory(fileB);
        
        createFile(fileA, 32);
        createFile(fileB, 32);
        
        assertFiltering(filter, fileA, false);
        assertFiltering(filter, fileB, true);
        
        fileA.delete();
        fileB.delete();
    }
         
}
