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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Used to test FileFilterUtils.
 */
public class FileFilterTestCase extends FileBasedTestCase {

    /**
     * The subversion directory name.
     */
    static final String SVN_DIR_NAME = ".svn";
    
    private static final boolean WINDOWS = File.separatorChar == '\\';

    public FileFilterTestCase(String name) {
        super(name);
    }

    @Override
    public void setUp() {
        getTestDirectory().mkdirs();
    }

    @Override
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(getTestDirectory());
    }

    public void assertFiltering(IOFileFilter filter, File file, boolean expected) throws Exception {
        // Note. This only tests the (File, String) version if the parent of
        //       the File passed in is not null
        assertEquals(
            "Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file,
            expected, filter.accept(file));

        if (file != null && file.getParentFile() != null) {
            assertEquals(
                "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file,
                expected, filter.accept(file.getParentFile(), file.getName()));
        } else if (file == null) {
            assertEquals(
                "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for null",
                expected, filter.accept(file));
        }
        assertNotNull(filter.toString());
    }

    public void testSuffix() throws Exception {
        IOFileFilter filter = new SuffixFileFilter(new String[] { "tes", "est" });
        File testFile = new File( "test" );
        File fredFile = new File( "fred" );
        assertFiltering(filter, new File("fred.tes"), true);
        assertFiltering(filter, new File("fred.est"), true);
        assertFiltering(filter, new File("fred.EST"), false); //case-sensitive
        assertFiltering(filter, new File("fred.exe"), false);

        filter = FileFilterUtils.or( 
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

        List<String> prefixes = Arrays.asList( new String[] { "ood", "red" } );
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
            new SuffixFileFilter((List<String>) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
}

    public void testSuffixCaseInsensitive() throws Exception {

        IOFileFilter filter = new SuffixFileFilter(new String[] { "tes", "est" }, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.tes"), true);
        assertFiltering(filter, new File("foo.est"), true);
        assertFiltering(filter, new File("foo.EST"), true); //case-sensitive
        assertFiltering(filter, new File("foo.TES"), true); //case-sensitive
        assertFiltering(filter, new File("foo.exe"), false);

        filter = new SuffixFileFilter("est", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("TEST"), true);

        List<String> suffixes = Arrays.asList( new String[] { "tes", "est" } );
        filter = new SuffixFileFilter(suffixes, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("bar.tes"), true);
        assertFiltering(filter, new File("bar.est"), true);
        assertFiltering(filter, new File("bar.EST"), true); //case-sensitive
        assertFiltering(filter, new File("bar.TES"), true); //case-sensitive
        assertFiltering(filter, new File("bar.exe"), false);

        try {
            new SuffixFileFilter((String) null, IOCase.INSENSITIVE);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new SuffixFileFilter((String[]) null, IOCase.INSENSITIVE);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new SuffixFileFilter((List<String>) null, IOCase.INSENSITIVE);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        // FileFilterUtils.suffixFileFilter(String, IOCase) tests
        filter = FileFilterUtils.suffixFileFilter("est", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("TEST"), true);
        
        try {
            FileFilterUtils.suffixFileFilter((String) null, IOCase.INSENSITIVE);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testDirectory() throws Exception {
        // XXX: This test presumes the current working dir is the base dir of the source checkout.
        IOFileFilter filter = new DirectoryFileFilter();

        assertFiltering(filter, new File("src/"), true);
        assertFiltering(filter, new File("src/main/java/"), true);

        assertFiltering(filter, new File("pom.xml"), false);

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
        
        assertFiltering(filter, new File("pom.xml"), true);
        
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

        List<String> prefixes = Arrays.asList( new String[] { "foo", "fre" } );
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
            new PrefixFileFilter((List<String>) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testPrefixCaseInsensitive() throws Exception {

        IOFileFilter filter = new PrefixFileFilter(new String[] { "foo", "bar" }, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.test1"), true);
        assertFiltering(filter, new File("bar.test1"), true);
        assertFiltering(filter, new File("FOO.test1"), true);  //case-sensitive
        assertFiltering(filter, new File("BAR.test1"), true);  //case-sensitive

        filter = new PrefixFileFilter("bar", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.test2"), false);
        assertFiltering(filter, new File("bar.test2"), true);
        assertFiltering(filter, new File("FOO.test2"), false); //case-sensitive
        assertFiltering(filter, new File("BAR.test2"), true);  //case-sensitive

        List<String> prefixes = Arrays.asList( new String[] { "foo", "bar" } );
        filter = new PrefixFileFilter(prefixes, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.test3"), true);
        assertFiltering(filter, new File("bar.test3"), true);
        assertFiltering(filter, new File("FOO.test3"), true);  //case-sensitive
        assertFiltering(filter, new File("BAR.test3"), true);  //case-sensitive

        try {
            new PrefixFileFilter((String) null, IOCase.INSENSITIVE);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new PrefixFileFilter((String[]) null, IOCase.INSENSITIVE);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new PrefixFileFilter((List<String>) null, IOCase.INSENSITIVE);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        // FileFilterUtils.prefixFileFilter(String, IOCase) tests
        filter = FileFilterUtils.prefixFileFilter("bar", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.test2"), false);
        assertFiltering(filter, new File("bar.test2"), true);
        assertFiltering(filter, new File("FOO.test2"), false); //case-sensitive
        assertFiltering(filter, new File("BAR.test2"), true);  //case-sensitive
        
        try {
            FileFilterUtils.prefixFileFilter((String) null, IOCase.INSENSITIVE);
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
        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
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
        
        // FileFilterUtils.nameFileFilter(String, IOCase) tests
        filter = FileFilterUtils.nameFileFilter("foo", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("FOO"), true); //case-insensitive
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
        
        try {
            FileFilterUtils.nameFileFilter(test, IOCase.INSENSITIVE);
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
        List<String> test = null;
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

        List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
        assertFiltering( new AndFileFilter( filters ), new File( "test" ), false );
        assertFiltering( new AndFileFilter(), new File( "test" ), false );
        
        try {
            new AndFileFilter(falseFilter, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new AndFileFilter(null, falseFilter);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        AndFileFilter f = new AndFileFilter((List<IOFileFilter>) null);
        assertTrue(f.getFileFilters().isEmpty());
        
        assertNotNull(f.toString()); // TODO better tests
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
        
        List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
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
        
        OrFileFilter f = new OrFileFilter((List<IOFileFilter>) null);
        assertTrue(f.getFileFilters().isEmpty());
    }
    public void testFileFilterUtils_and() throws Exception {
        IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        assertFiltering(FileFilterUtils.and(trueFilter, trueFilter, trueFilter), new File("foo.test"), true);
        assertFiltering(FileFilterUtils.and(trueFilter, falseFilter, trueFilter), new File("foo.test"), false);
        assertFiltering(FileFilterUtils.and(falseFilter, trueFilter), new File("foo.test"), false);
        assertFiltering(FileFilterUtils.and(falseFilter, falseFilter), new File("foo.test"), false);
    }

    public void testFileFilterUtils_or() throws Exception {
        IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        File testFile = new File( "foo.test" );
        assertFiltering(FileFilterUtils.or(trueFilter, trueFilter), testFile, true);
        assertFiltering(FileFilterUtils.or(trueFilter, trueFilter, falseFilter), testFile, true);
        assertFiltering(FileFilterUtils.or(falseFilter, trueFilter), testFile, true);
        assertFiltering(FileFilterUtils.or(falseFilter, falseFilter, falseFilter), testFile, false);
    }

    @SuppressWarnings("deprecation")
    public void testDeprecatedWildcard() throws Exception {
        IOFileFilter filter = new WildcardFilter("*.txt");
        List<String> patternList = Arrays.asList( new String[] { "*.txt", "*.xml", "*.gif" } );
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
            new WildcardFilter((List<String>) null);
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
        
        List<String> patternList = Arrays.asList( new String[] { "*.txt", "*.xml", "*.gif" } );
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
            new WildcardFileFilter((List<String>) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    public void testDelegateFileFilter() throws Exception {
        OrFileFilter orFilter = new OrFileFilter();
        File testFile = new File( "test.txt" );

        IOFileFilter filter = new DelegateFileFilter((FileFilter) orFilter);
        assertFiltering( filter, testFile, false );
        assertNotNull(filter.toString()); // TODO better test

        filter = new DelegateFileFilter((FilenameFilter) orFilter);
        assertFiltering( filter, testFile, false );
        assertNotNull(filter.toString()); // TODO better test

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
    
    //-----------------------------------------------------------------------
    
    public void testMagicNumberFileFilterBytes() throws Exception {
        byte[] classFileMagicNumber = 
            new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
        String xmlFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\">\n" +
            "<element>text</element>";
        
        File classFileA = new File(getTestDirectory(), "A.class");
        File xmlFileB = new File(getTestDirectory(), "B.xml");
        File emptyFile = new File(getTestDirectory(), "C.xml");
        File dir = new File(getTestDirectory(), "D");
        dir.mkdirs();
        
        OutputStream classFileAStream = FileUtils.openOutputStream(classFileA);
        IOUtils.write(classFileMagicNumber, classFileAStream);
        generateTestData(classFileAStream, 32);
        classFileAStream.close();
        
        FileUtils.write(xmlFileB, xmlFileContent);
        FileUtils.touch(emptyFile);
        
        IOFileFilter filter = new MagicNumberFileFilter(classFileMagicNumber);
        
        assertFiltering(filter, classFileA, true);
        assertFiltering(filter, xmlFileB, false);
        assertFiltering(filter, emptyFile, false);
        assertFiltering(filter, dir, false);
        
        
        filter = FileFilterUtils.magicNumberFileFilter(classFileMagicNumber);
        
        assertFiltering(filter, classFileA, true);
        assertFiltering(filter, xmlFileB, false);
        assertFiltering(filter, emptyFile, false);
        assertFiltering(filter, dir, false);
    }
    
    public void testMagicNumberFileFilterBytesOffset() throws Exception {
        byte[] tarMagicNumber = new byte[] {0x75, 0x73, 0x74, 0x61, 0x72};
        long tarMagicNumberOffset = 257;
        
        File tarFileA = new File(getTestDirectory(), "A.tar");
        File randomFileB = new File(getTestDirectory(), "B.txt");
        File dir = new File(getTestDirectory(), "D");
        dir.mkdirs();
        
        OutputStream tarFileAStream = FileUtils.openOutputStream(tarFileA);
        generateTestData(tarFileAStream, tarMagicNumberOffset);
        IOUtils.write(tarMagicNumber, tarFileAStream);
        tarFileAStream.close();
        
        createFile(randomFileB, 2 * tarMagicNumberOffset);
        
        IOFileFilter filter = 
            new MagicNumberFileFilter(tarMagicNumber, tarMagicNumberOffset);
        
        assertFiltering(filter, tarFileA, true);
        assertFiltering(filter, randomFileB, false);
        assertFiltering(filter, dir, false);
        
        filter = FileFilterUtils.magicNumberFileFilter(tarMagicNumber, 
                tarMagicNumberOffset);
        
        assertFiltering(filter, tarFileA, true);
        assertFiltering(filter, randomFileB, false);
        assertFiltering(filter, dir, false);
    }
    
    public void testMagicNumberFileFilterString() throws Exception {
        byte[] classFileMagicNumber = 
            new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
        String xmlFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\">\n" +
            "<element>text</element>";
        String xmlMagicNumber = "<?xml version=\"1.0\"";
        
        File classFileA = new File(getTestDirectory(), "A.class");
        File xmlFileB = new File(getTestDirectory(), "B.xml");
        File dir = new File(getTestDirectory(), "D");
        dir.mkdirs();
        
        OutputStream classFileAStream = FileUtils.openOutputStream(classFileA);
        IOUtils.write(classFileMagicNumber, classFileAStream);
        generateTestData(classFileAStream, 32);
        classFileAStream.close();
        
        FileUtils.write(xmlFileB, xmlFileContent);
        
        IOFileFilter filter = new MagicNumberFileFilter(xmlMagicNumber);
        
        assertFiltering(filter, classFileA, false);
        assertFiltering(filter, xmlFileB, true);
        assertFiltering(filter, dir, false);
        
        filter = FileFilterUtils.magicNumberFileFilter(xmlMagicNumber);
        
        assertFiltering(filter, classFileA, false);
        assertFiltering(filter, xmlFileB, true);
        assertFiltering(filter, dir, false);
    }
    
    public void testMagicNumberFileFilterStringOffset() throws Exception {
        String tarMagicNumber = "ustar";
        long tarMagicNumberOffset = 257;
        
        File tarFileA = new File(getTestDirectory(), "A.tar");
        File randomFileB = new File(getTestDirectory(), "B.txt");
        File dir = new File(getTestDirectory(), "D");
        dir.mkdirs();
        
        OutputStream tarFileAStream = FileUtils.openOutputStream(tarFileA);
        generateTestData(tarFileAStream, tarMagicNumberOffset);
        IOUtils.write(tarMagicNumber, tarFileAStream);
        tarFileAStream.close();

        createFile(randomFileB, 2 * tarMagicNumberOffset);
        
        IOFileFilter filter = 
            new MagicNumberFileFilter(tarMagicNumber, tarMagicNumberOffset);
        
        assertFiltering(filter, tarFileA, true);
        assertFiltering(filter, randomFileB, false);
        assertFiltering(filter, dir, false);
        
        filter = FileFilterUtils.magicNumberFileFilter(tarMagicNumber, 
                tarMagicNumberOffset);
        
        assertFiltering(filter, tarFileA, true);
        assertFiltering(filter, randomFileB, false);
        assertFiltering(filter, dir, false);
    }

    public void testMagicNumberFileFilterValidation() {
        try {
            new MagicNumberFileFilter((String)null, 0);
            fail();
        } catch (IllegalArgumentException iae) {
            // expected
        }
        try {
            new MagicNumberFileFilter("0", -1);
            fail();
        } catch (IllegalArgumentException iae) {
            // expected
        }
        try {
            new MagicNumberFileFilter("", 0);
            fail();
        } catch (IllegalArgumentException iae) {
            // expected
        }
        try {
            new MagicNumberFileFilter((byte[])null, 0);
            fail();
        } catch (IllegalArgumentException iae) {
            // expected
        }
        try {
            new MagicNumberFileFilter(new byte[]{0}, -1);
            fail();
        } catch (IllegalArgumentException iae) {
            // expected
        }
        try {
            new MagicNumberFileFilter(new byte[]{}, 0);
            fail();
        } catch (IllegalArgumentException iae) {
            // expected
        }
}

    /**
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, File...)}
     * that tests that the method properly filters files from the list.
     */
    public void testFilterArray() throws Exception {
        File fileA = newFile("A");
        File fileB = newFile("B");

        IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        File[] filtered = FileFilterUtils.filter(filter, fileA, fileB);

        assertEquals(1, filtered.length);
        assertEquals(fileA, filtered[0]);
    }

    /**
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, java.lang.Iterable)}
     * that tests that the method properly filters files from the list.
     */
    public void testFilterArray_fromList() throws Exception {
        File fileA = newFile("A");
        File fileB = newFile("B");
        List<File> fileList = Arrays.asList(fileA, fileB);

        IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        File[] filtered = FileFilterUtils.filter(filter, fileList);

        assertEquals(1, filtered.length);
        assertEquals(fileA, filtered[0]);
    }

    /**
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, File...)}
     * that tests {@code null} parameters and {@code null} elements
     * in the provided list.
     */
    public void testFilterArrayNullParameters() throws Exception {
        File fileA = newFile("A");
        File fileB = newFile("B");
        try {
            FileFilterUtils.filter(null, fileA, fileB); 
            fail();
        } catch (IllegalArgumentException iae) {
            // Test passes, exception thrown for null filter
        }

        IOFileFilter filter = FileFilterUtils.trueFileFilter();
        try {
            FileFilterUtils.filter(filter, fileA, null); 
            fail();
        } catch (IllegalArgumentException iae) {
            // Test passes, exception thrown for list containing null
        }

        File[] filtered = FileFilterUtils.filter(filter, (File[])null); 
        assertEquals(0, filtered.length);
    }

    /**
     * Test method for {@link FileFilterUtils#filterList(IOFileFilter, java.lang.Iterable)}
     * that tests that the method properly filters files from the list.
     */
    public void testFilterList() throws Exception {
        File fileA = newFile("A");
        File fileB = newFile("B");
        List<File> fileList = Arrays.asList(fileA, fileB);

        IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        List<File> filteredList = FileFilterUtils.filterList(filter, fileList);

        assertTrue(filteredList.contains(fileA));
        assertFalse(filteredList.contains(fileB));
    }

    /**
     * Test method for {@link FileFilterUtils#filterList(IOFileFilter, File...)}
     * that tests that the method properly filters files from the list.
     */
    public void testFilterList_fromArray() throws Exception {
        File fileA = newFile("A");
        File fileB = newFile("B");

        IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        List<File> filteredList = FileFilterUtils.filterList(filter, fileA, fileB);

        assertTrue(filteredList.contains(fileA));
        assertFalse(filteredList.contains(fileB));
    }

    /**
     * Test method for {@link FileFilterUtils#filterList(IOFileFilter, java.lang.Iterable)}
     * that tests {@code null} parameters and {@code null} elements
     * in the provided list.
     */
    public void testFilterListNullParameters() {
        try {
            FileFilterUtils.filterList(null, Collections.<File>emptyList()); 
            fail();
        } catch (IllegalArgumentException iae) {
            // Test passes, exception thrown for null filter
        }

        IOFileFilter filter = FileFilterUtils.trueFileFilter();
        try {
            FileFilterUtils.filterList(filter, Arrays.<File>asList((File) null)); 
            fail();
        } catch (IllegalArgumentException iae) {
            // Test passes, exception thrown for list containing null
        }

        List<File> filteredList = FileFilterUtils.filterList(filter, (List<File>)null); 
        assertEquals(0, filteredList.size());
    }
 
    /**
     * Test method for {@link FileFilterUtils#filterSet(IOFileFilter, java.lang.Iterable)}
     * that tests that the method properly filters files from the set.
     */
    public void testFilterSet() throws Exception {
        File fileA = newFile("A");
        File fileB = newFile("B");
        Set<File> fileList = new HashSet<File>(Arrays.asList(fileA, fileB));

        IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        Set<File> filteredSet = FileFilterUtils.filterSet(filter, fileList);

        assertTrue(filteredSet.contains(fileA));
        assertFalse(filteredSet.contains(fileB));
    }
    
    /**
     * Test method for {@link FileFilterUtils#filterSet(IOFileFilter, File...)}
     * that tests that the method properly filters files from the set.
     */
    public void testFilterSet_fromArray() throws Exception {
        File fileA = newFile("A");
        File fileB = newFile("B");

        IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        Set<File> filteredSet = FileFilterUtils.filterSet(filter, fileA, fileB);

        assertTrue(filteredSet.contains(fileA));
        assertFalse(filteredSet.contains(fileB));
    }

    /**
     * Test method for {@link FileFilterUtils#filterSet(IOFileFilter, java.lang.Iterable)}
     * that tests {@code null} parameters and {@code null} elements
     * in the provided set.
     */
   public void testFilterSetNullParameters() {
        try {
            FileFilterUtils.filterSet(null, Collections.<File>emptySet()); 
            fail();
        } catch (IllegalArgumentException iae) {
            // Test passes, exception thrown for null filter
        }

        IOFileFilter filter = FileFilterUtils.trueFileFilter();
        try {
            FileFilterUtils.filterSet(filter, new HashSet<File>(Arrays.<File>asList((File) null))); 
            fail();
        } catch (IllegalArgumentException iae) {
            // Test passes, exception thrown for set containing null
        }

        Set<File> filteredSet = FileFilterUtils.filterSet(filter, (Set<File>)null); 
        assertEquals(0, filteredSet.size());
    }

       public void testEnsureTestCoverage() {
           assertNotNull(new FileFilterUtils()); // dummy for test coverage
       }
       
       public void testNullFilters() {
           try {
               FileFilterUtils.toList((IOFileFilter)null);
               fail("Expected IllegalArgumentException");
           } catch (IllegalArgumentException expected) {
               // expected
           }
           try {
               FileFilterUtils.toList(new IOFileFilter[]{null});
               fail("Expected IllegalArgumentException");
           } catch (IllegalArgumentException expected) {
            // expected
           }
       }
       
       public void testDelegation() { // TODO improve these tests
           assertNotNull(FileFilterUtils.asFileFilter((FileFilter)FalseFileFilter.INSTANCE));
           assertNotNull(FileFilterUtils.asFileFilter((FilenameFilter)FalseFileFilter.INSTANCE).toString());
       }
}
