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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

/**
 * Used to test FileFilterUtils.
 */
public class FileFilterTest extends AbstractFilterTest {

    @Test
    public void testAgeFilter() throws Exception {
        final File oldFile = new File(temporaryFolder, "old.txt");
        final Path oldPath = oldFile.toPath();
        final File reference = new File(temporaryFolder, "reference.txt");
        final File newFile = new File(temporaryFolder, "new.txt");
        final Path newPath = newFile.toPath();

        if (!oldFile.getParentFile().exists()) {
            fail("Cannot create file " + oldFile + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(oldFile.toPath()))) {
            TestUtils.generateTestData(output1, 0);
        }

        do {
            try {
                TestUtils.sleep(1000);
            } catch (final InterruptedException ie) {
                // ignore
            }
            if (!reference.getParentFile().exists()) {
                fail("Cannot create file " + reference + " as the parent directory does not exist");
            }
            try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(reference.toPath()))) {
                TestUtils.generateTestData(output, 0);
            }
        } while (equalsLastModified(oldFile, reference));

        final Date date = new Date();
        final long now = date.getTime();

        do {
            try {
                TestUtils.sleep(1000);
            } catch (final InterruptedException ie) {
                // ignore
            }
            if (!newFile.getParentFile().exists()) {
                fail("Cannot create file " + newFile + " as the parent directory does not exist");
            }
            try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(newFile.toPath()))) {
                TestUtils.generateTestData(output, 0);
            }
        } while (equalsLastModified(reference, newFile));

        final IOFileFilter filter1 = FileFilterUtils.ageFileFilter(now);
        final IOFileFilter filter2 = FileFilterUtils.ageFileFilter(now, true);
        final IOFileFilter filter3 = FileFilterUtils.ageFileFilter(now, false);
        final IOFileFilter filter4 = FileFilterUtils.ageFileFilter(date);
        final IOFileFilter filter5 = FileFilterUtils.ageFileFilter(date, true);
        final IOFileFilter filter6 = FileFilterUtils.ageFileFilter(date, false);
        final IOFileFilter filter7 = FileFilterUtils.ageFileFilter(reference);
        final IOFileFilter filter8 = FileFilterUtils.ageFileFilter(reference, true);
        final IOFileFilter filter9 = FileFilterUtils.ageFileFilter(reference, false);

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
        //
        assertFiltering(filter1, oldPath, true);
        assertFiltering(filter2, oldPath, true);
        assertFiltering(filter3, oldPath, false);
        assertFiltering(filter4, oldPath, true);
        assertFiltering(filter5, oldPath, true);
        assertFiltering(filter6, oldPath, false);
        assertFiltering(filter7, oldPath, true);
        assertFiltering(filter8, oldPath, true);
        assertFiltering(filter9, oldPath, false);
        assertFiltering(filter1, newPath, false);
        assertFiltering(filter2, newPath, false);
        assertFiltering(filter3, newPath, true);
        assertFiltering(filter4, newPath, false);
        assertFiltering(filter5, newPath, false);
        assertFiltering(filter6, newPath, true);
        assertFiltering(filter7, newPath, false);
        assertFiltering(filter8, newPath, false);
        assertFiltering(filter9, newPath, true);
    }

    @Test
    public void testAnd() {
        final IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        final IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        assertFiltering(trueFilter.and(trueFilter), new File("foo.test"), true);
        assertFiltering(trueFilter.and(falseFilter), new File("foo.test"), false);
        assertFiltering(falseFilter.and(trueFilter), new File("foo.test"), false);
        assertFiltering(falseFilter.and(trueFilter), new File("foo.test"), false);
    }

    @Test
    public void testAnd2() {
        final IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        final IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        assertFiltering(new AndFileFilter(trueFilter, trueFilter), new File("foo.test"), true);
        assertFiltering(new AndFileFilter(trueFilter, falseFilter), new File("foo.test"), false);
        assertFiltering(new AndFileFilter(falseFilter, trueFilter), new File("foo.test"), false);
        assertFiltering(new AndFileFilter(falseFilter, falseFilter), new File("foo.test"), false);

        final List<IOFileFilter> filters = new ArrayList<>();
        assertFiltering(new AndFileFilter(filters), new File("test"), false);
        assertFiltering(new AndFileFilter(), new File("test"), false);

        assertThrows(NullPointerException.class, () -> new AndFileFilter(falseFilter, null));
        assertThrows(NullPointerException.class, () -> new AndFileFilter(null, falseFilter));
        assertThrows(NullPointerException.class, () -> new AndFileFilter((List<IOFileFilter>) null));
    }

    @Test
    public void testAndArray() {
        final IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        final IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        assertFiltering(new AndFileFilter(trueFilter, trueFilter, trueFilter), new File("foo.test"), true);
        assertFiltering(new AndFileFilter(trueFilter, falseFilter, falseFilter), new File("foo.test"), false);
        assertFiltering(new AndFileFilter(falseFilter, trueFilter, trueFilter), new File("foo.test"), false);
        assertFiltering(new AndFileFilter(falseFilter, falseFilter, falseFilter), new File("foo.test"), false);

        final List<IOFileFilter> filters = new ArrayList<>();
        assertFiltering(new AndFileFilter(filters), new File("test"), false);
        assertFiltering(new AndFileFilter(), new File("test"), false);
    }

    @Test
    public void testCanExecute() throws Exception {
        assumeTrue(SystemUtils.IS_OS_WINDOWS);
        final File executableFile = File.createTempFile(getClass().getSimpleName(), ".temp");
        final Path executablePath = executableFile.toPath();
        try {
            try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(executableFile.toPath()))) {
                TestUtils.generateTestData(output, 32);
            }
            assertTrue(executableFile.setExecutable(true));
            assertFiltering(CanExecuteFileFilter.CAN_EXECUTE, executableFile, true);
            assertFiltering(CanExecuteFileFilter.CAN_EXECUTE, executablePath, true);
            executableFile.setExecutable(false);
            assertFiltering(CanExecuteFileFilter.CANNOT_EXECUTE, executableFile, false);
            assertFiltering(CanExecuteFileFilter.CANNOT_EXECUTE, executablePath, false);
        } finally {
            executableFile.delete();
        }
    }

    @Test
    public void testCanRead() throws Exception {
        final File readOnlyFile = new File(temporaryFolder, "read-only-file1.txt");
        final Path readOnlyPath = readOnlyFile.toPath();
        if (!readOnlyFile.getParentFile().exists()) {
            fail("Cannot create file " + readOnlyFile + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(readOnlyFile.toPath()))) {
            TestUtils.generateTestData(output, 32);
        }
        assertTrue(readOnlyFile.setReadOnly());
        assertFiltering(CanReadFileFilter.CAN_READ, readOnlyFile, true);
        assertFiltering(CanReadFileFilter.CAN_READ, readOnlyPath, true);
        assertFiltering(CanReadFileFilter.CANNOT_READ, readOnlyFile, false);
        assertFiltering(CanReadFileFilter.CANNOT_READ, readOnlyPath, false);
        assertFiltering(CanReadFileFilter.READ_ONLY, readOnlyFile, true);
        assertFiltering(CanReadFileFilter.READ_ONLY, readOnlyPath, true);
        readOnlyFile.delete();
    }

    @Test
    public void testCanWrite() throws Exception {
        final File readOnlyFile = new File(temporaryFolder, "read-only-file2.txt");
        final Path readOnlyPath = readOnlyFile.toPath();
        if (!readOnlyFile.getParentFile().exists()) {
            fail("Cannot create file " + readOnlyFile + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(readOnlyFile.toPath()))) {
            TestUtils.generateTestData(output, 32);
        }
        assertTrue(readOnlyFile.setReadOnly());
        assertFiltering(CanWriteFileFilter.CAN_WRITE, temporaryFolder, true);
        assertFiltering(CanWriteFileFilter.CANNOT_WRITE, temporaryFolder, false);
        assertFiltering(CanWriteFileFilter.CAN_WRITE, readOnlyFile, false);
        assertFiltering(CanWriteFileFilter.CAN_WRITE, readOnlyPath, false);
        assertFiltering(CanWriteFileFilter.CANNOT_WRITE, readOnlyFile, true);
        assertFiltering(CanWriteFileFilter.CANNOT_WRITE, readOnlyPath, true);
        readOnlyFile.delete();
    }

    @Test
    public void testDelegateFileFilter() {
        final OrFileFilter orFilter = new OrFileFilter();
        final File testFile = new File("test.txt");

        IOFileFilter filter = new DelegateFileFilter((FileFilter) orFilter);
        assertFiltering(filter, testFile, false);
        assertNotNull(filter.toString()); // TODO better test

        filter = new DelegateFileFilter((FilenameFilter) orFilter);
        assertFiltering(filter, testFile, false);
        assertNotNull(filter.toString()); // TODO better test

        assertThrows(IllegalArgumentException.class, () -> new DelegateFileFilter((FileFilter) null));
        assertThrows(IllegalArgumentException.class, () -> new DelegateFileFilter((FilenameFilter) null));
    }

    @Test
    public void testDelegation() { // TODO improve these tests
        assertNotNull(FileFilterUtils.asFileFilter((FileFilter) FalseFileFilter.INSTANCE));
        assertNotNull(FileFilterUtils.asFileFilter((FilenameFilter) FalseFileFilter.INSTANCE).toString());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedWildcard() {
        IOFileFilter filter = new WildcardFilter("*.txt");
        final List<String> patternList = Arrays.asList("*.txt", "*.xml", "*.gif");
        final IOFileFilter listFilter = new WildcardFilter(patternList);
        final File txtFile = new File("test.txt");
        final Path txtPath = txtFile.toPath();
        final File bmpFile = new File("test.bmp");
        final Path bmpPath = bmpFile.toPath();
        final File dirFile = new File("src/java");
        final Path dirPath = dirFile.toPath();

        assertFiltering(filter, new File("log.txt"), true);
//        assertFiltering(filter, new File("log.txt.bak"), false);
        assertFiltering(filter, new File("log.txt").toPath(), true);

        filter = new WildcardFilter("log?.txt");
        assertFiltering(filter, new File("log1.txt"), true);
        assertFiltering(filter, new File("log12.txt"), false);
        //
        assertFiltering(filter, new File("log1.txt").toPath(), true);
        assertFiltering(filter, new File("log12.txt").toPath(), false);

        filter = new WildcardFilter("open??.????04");
        assertFiltering(filter, new File("openAB.102504"), true);
        assertFiltering(filter, new File("openA.102504"), false);
        assertFiltering(filter, new File("openXY.123103"), false);
//        assertFiltering(filter, new File("openAB.102504.old"), false);
        //
        assertFiltering(filter, new File("openAB.102504").toPath(), true);
        assertFiltering(filter, new File("openA.102504").toPath(), false);
        assertFiltering(filter, new File("openXY.123103").toPath(), false);
//        assertFiltering(filter, new File("openAB.102504.old").toPath(), false);

        filter = new WildcardFilter("*.java", "*.class");
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.class"), true);
        assertFiltering(filter, new File("Test.jsp"), false);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("Test.class").toPath(), true);
        assertFiltering(filter, new File("Test.jsp").toPath(), false);

        assertFiltering(listFilter, new File("Test.txt"), true);
        assertFiltering(listFilter, new File("Test.xml"), true);
        assertFiltering(listFilter, new File("Test.gif"), true);
        assertFiltering(listFilter, new File("Test.bmp"), false);
        //
        assertFiltering(listFilter, new File("Test.txt").toPath(), true);
        assertFiltering(listFilter, new File("Test.xml").toPath(), true);
        assertFiltering(listFilter, new File("Test.gif").toPath(), true);
        assertFiltering(listFilter, new File("Test.bmp").toPath(), false);

        assertTrue(listFilter.accept(txtFile));
        assertFalse(listFilter.accept(bmpFile));
        assertFalse(listFilter.accept(dirFile));
        //
        assertEquals(FileVisitResult.CONTINUE, listFilter.accept(txtPath, null));
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(bmpPath, null));
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(dirPath, null));

        assertTrue(listFilter.accept(txtFile.getParentFile(), txtFile.getName()));
        assertFalse(listFilter.accept(bmpFile.getParentFile(), bmpFile.getName()));
        assertFalse(listFilter.accept(dirFile.getParentFile(), dirFile.getName()));
        //
        assertEquals(FileVisitResult.CONTINUE, listFilter.accept(txtPath, null));
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(bmpPath, null));
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(dirPath, null));

        assertThrows(IllegalArgumentException.class, () -> new WildcardFilter((String) null));
        assertThrows(IllegalArgumentException.class, () -> new WildcardFilter((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> new WildcardFilter((List<String>) null));
    }

    @Test
    public void testDirectory() {
        // XXX: This test presumes the current working dir is the base dir of the source checkout.
        final IOFileFilter filter = new DirectoryFileFilter();

        assertFiltering(filter, new File("src/"), true);
        assertFiltering(filter, new File("src/").toPath(), true);
        assertFiltering(filter, new File("src/main/java/"), true);
        assertFiltering(filter, new File("src/main/java/").toPath(), true);

        assertFiltering(filter, new File("pom.xml"), false);
        assertFiltering(filter, new File("pom.xml").toPath(), false);

        assertFiltering(filter, new File("imaginary"), false);
        assertFiltering(filter, new File("imaginary").toPath(), false);
        assertFiltering(filter, new File("imaginary/"), false);
        assertFiltering(filter, new File("imaginary/").toPath(), false);

        assertFiltering(filter, new File("LICENSE.txt"), false);
        assertFiltering(filter, new File("LICENSE.txt").toPath(), false);

        assertSame(DirectoryFileFilter.DIRECTORY, DirectoryFileFilter.INSTANCE);
    }

    @Test
    public void testEmpty() throws Exception {

        // Empty Dir
        final File emptyDirFile = new File(temporaryFolder, "empty-dir");
        final Path emptyDirPath = emptyDirFile.toPath();
        emptyDirFile.mkdirs();
        assertFiltering(EmptyFileFilter.EMPTY, emptyDirFile, true);
        assertFiltering(EmptyFileFilter.EMPTY, emptyDirPath, true);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyDirFile, false);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyDirPath, false);

        // Empty File
        final File emptyFile = new File(emptyDirFile, "empty-file.txt");
        final Path emptyPath = emptyFile.toPath();
        if (!emptyFile.getParentFile().exists()) {
            fail("Cannot create file " + emptyFile + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(emptyFile.toPath()))) {
            TestUtils.generateTestData(output1, 0);
        }
        assertFiltering(EmptyFileFilter.EMPTY, emptyFile, true);
        assertFiltering(EmptyFileFilter.EMPTY, emptyPath, true);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyFile, false);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyPath, false);

        // Not Empty Dir
        assertFiltering(EmptyFileFilter.EMPTY, emptyDirFile, false);
        assertFiltering(EmptyFileFilter.EMPTY, emptyDirPath, false);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyDirFile, true);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, emptyDirPath, true);

        // Not Empty File
        final File notEmptyFile = new File(emptyDirFile, "not-empty-file.txt");
        final Path notEmptyPath = notEmptyFile.toPath();
        if (!notEmptyFile.getParentFile().exists()) {
            fail("Cannot create file " + notEmptyFile + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(notEmptyFile.toPath()))) {
            TestUtils.generateTestData(output, 32);
        }
        assertFiltering(EmptyFileFilter.EMPTY, notEmptyFile, false);
        assertFiltering(EmptyFileFilter.EMPTY, notEmptyPath, false);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, notEmptyFile, true);
        assertFiltering(EmptyFileFilter.NOT_EMPTY, notEmptyPath, true);
        FileUtils.forceDelete(emptyDirFile);
    }

    @Test
    public void testEnsureTestCoverage() {
        assertNotNull(new FileFilterUtils()); // dummy for test coverage
    }

    @Test
    public void testFalse() {
        final IOFileFilter filter = FileFilterUtils.falseFileFilter();
        assertFiltering(filter, new File("foo.test"), false);
        assertFiltering(filter, new File("foo.test").toPath(), false);
        assertFiltering(filter, new File("foo"), false);
        assertFiltering(filter, new File("foo").toPath(), false);
        assertFiltering(filter, (File) null, false);
        assertFiltering(filter, (Path) null, false);
        assertSame(FalseFileFilter.FALSE, FalseFileFilter.INSTANCE);
        assertSame(TrueFileFilter.TRUE, FalseFileFilter.INSTANCE.negate());
        assertSame(TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE.negate());
        assertNotNull(FalseFileFilter.INSTANCE.toString());
    }

    @Test
    public void testFileEqualsFilter() {
        assertFooBarFileFiltering(
            new FileEqualsFileFilter(new File("foo")).or(new FileEqualsFileFilter(new File("bar"))));
    }

    @Test
    public void testFileFilterUtils_and() {
        final IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        final IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        assertFiltering(FileFilterUtils.and(trueFilter, trueFilter, trueFilter), new File("foo.test"), true);
        assertFiltering(FileFilterUtils.and(trueFilter, falseFilter, trueFilter), new File("foo.test"), false);
        assertFiltering(FileFilterUtils.and(falseFilter, trueFilter), new File("foo.test"), false);
        assertFiltering(FileFilterUtils.and(falseFilter, falseFilter), new File("foo.test"), false);
    }

    @Test
    public void testFileFilterUtils_or() {
        final IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        final IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        final File testFile = new File("foo.test");
        assertFiltering(FileFilterUtils.or(trueFilter, trueFilter), testFile, true);
        assertFiltering(FileFilterUtils.or(trueFilter, trueFilter, falseFilter), testFile, true);
        assertFiltering(FileFilterUtils.or(falseFilter, trueFilter), testFile, true);
        assertFiltering(FileFilterUtils.or(falseFilter, falseFilter, falseFilter), testFile, false);
    }

    @Test
    public void testFiles() {
        // XXX: This test presumes the current working dir is the base dir of the source checkout.
        final IOFileFilter filter = FileFileFilter.INSTANCE;

        assertFiltering(filter, new File("src/"), false);
        assertFiltering(filter, new File("src/").toPath(), false);
        assertFiltering(filter, new File("src/java/"), false);
        assertFiltering(filter, new File("src/java/").toPath(), false);

        assertFiltering(filter, new File("pom.xml"), true);
        assertFiltering(filter, new File("pom.xml").toPath(), true);

        assertFiltering(filter, new File("imaginary"), false);
        assertFiltering(filter, new File("imaginary").toPath(), false);
        assertFiltering(filter, new File("imaginary/"), false);
        assertFiltering(filter, new File("imaginary/").toPath(), false);

        assertFiltering(filter, new File("LICENSE.txt"), true);
        assertFiltering(filter, new File("LICENSE.txt").toPath(), true);
    }

    /*
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, java.lang.Iterable)} that tests that the method
     * properly filters files from the list.
     */
    @Test
    public void testFilterArray_fromList() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");
        final List<File> fileList = Arrays.asList(fileA, fileB);

        final IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        final File[] filtered = FileFilterUtils.filter(filter, fileList);

        assertEquals(1, filtered.length);
        assertEquals(fileA, filtered[0]);
    }

    /*
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, File...)} that tests that the method properly filters
     * files from the list.
     */
    @Test
    public void testFilterArray_IOFileFilter() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");

        final IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        final File[] filtered = FileFilterUtils.filter(filter, fileA, fileB);

        assertEquals(1, filtered.length);
        assertEquals(fileA, filtered[0]);
    }

    /*
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, File...)} that tests that the method properly filters
     * files from the list.
     */
    @Test
    public void testFilterArray_PathVisitorFileFilter_FileExistsNo() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");

        final IOFileFilter filter = new PathVisitorFileFilter(new NameFileFilter("A"));

        final File[] filtered = FileFilterUtils.filter(filter, fileA, fileB);

        assertEquals(1, filtered.length);
        assertEquals(fileA, filtered[0]);
    }

    /*
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, File...)} that tests that the method properly filters
     * files from the list.
     */
    @Test
    public void testFilterArray_PathVisitorFileFilter_FileExistsYes() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");
        FileUtils.write(fileA, "test", StandardCharsets.US_ASCII);

        final IOFileFilter filter = new PathVisitorFileFilter(new NameFileFilter("A"));

        final File[] filtered = FileFilterUtils.filter(filter, fileA, fileB);

        assertEquals(1, filtered.length);
        assertEquals(fileA, filtered[0]);
    }

    /*
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, File...)} that tests {@code null} parameters.
     */
    @Test
    public void testFilterFilesArrayNullParameters() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");
        assertThrows(IllegalArgumentException.class, () -> FileFilterUtils.filter(null, fileA, fileB));

        final IOFileFilter filter = FileFilterUtils.trueFileFilter();
        FileFilterUtils.filter(filter, fileA, null);

        final File[] filtered = FileFilterUtils.filter(filter, (File[]) null);
        assertEquals(0, filtered.length);
    }

    /*
     * Test method for {@link FileFilterUtils#filterList(IOFileFilter, java.lang.Iterable)} that tests that the method
     * properly filters files from the list.
     */
    @Test
    public void testFilterList() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");
        final List<File> fileList = Arrays.asList(fileA, fileB);

        final IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        final List<File> filteredList = FileFilterUtils.filterList(filter, fileList);

        assertTrue(filteredList.contains(fileA));
        assertFalse(filteredList.contains(fileB));
    }

    /*
     * Test method for {@link FileFilterUtils#filterList(IOFileFilter, File...)} that tests that the method properly
     * filters files from the list.
     */
    @Test
    public void testFilterList_fromArray() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");

        final IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        final List<File> filteredList = FileFilterUtils.filterList(filter, fileA, fileB);

        assertTrue(filteredList.contains(fileA));
        assertFalse(filteredList.contains(fileB));
    }

    /*
     * Test method for {@link FileFilterUtils#filterList(IOFileFilter, java.lang.Iterable)} that tests {@code null}
     * parameters and {@code null} elements in the provided list.
     */
    @Test
    public void testFilterListNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> FileFilterUtils.filterList(null, Collections.emptyList()));

        final IOFileFilter filter = FileFilterUtils.trueFileFilter();
        try {
            FileFilterUtils.filterList(filter, Collections.singletonList(null));
        } catch (final IllegalArgumentException iae) {
            // Test passes, exception thrown for list containing null
        }

        final List<File> filteredList = FileFilterUtils.filterList(filter, (List<File>) null);
        assertEquals(0, filteredList.size());
    }

    /*
     * Test method for {@link FileFilterUtils#filter(IOFileFilter, Path...)}.
     */
    @Test
    public void testFilterPathsArrayNullParameters() throws Exception {
        final Path fileA = TestUtils.newFile(temporaryFolder, "A").toPath();
        final Path fileB = TestUtils.newFile(temporaryFolder, "B").toPath();
        assertThrows(NullPointerException.class, () -> PathUtils.filter(null, fileA, fileB));

        final IOFileFilter filter = FileFilterUtils.trueFileFilter();
        PathUtils.filter(filter, fileA, null);

        final File[] filtered = FileFilterUtils.filter(filter, (File[]) null);
        assertEquals(0, filtered.length);
    }

    /*
     * Test method for {@link FileFilterUtils#filterSet(IOFileFilter, java.lang.Iterable)} that tests that the method
     * properly filters files from the set.
     */
    @Test
    public void testFilterSet() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");
        final Set<File> fileList = new HashSet<>(Arrays.asList(fileA, fileB));

        final IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        final Set<File> filteredSet = FileFilterUtils.filterSet(filter, fileList);

        assertTrue(filteredSet.contains(fileA));
        assertFalse(filteredSet.contains(fileB));
    }

    /*
     * Test method for {@link FileFilterUtils#filterSet(IOFileFilter, File...)} that tests that the method properly
     * filters files from the set.
     */
    @Test
    public void testFilterSet_fromArray() throws Exception {
        final File fileA = TestUtils.newFile(temporaryFolder, "A");
        final File fileB = TestUtils.newFile(temporaryFolder, "B");

        final IOFileFilter filter = FileFilterUtils.nameFileFilter("A");

        final Set<File> filteredSet = FileFilterUtils.filterSet(filter, fileA, fileB);

        assertTrue(filteredSet.contains(fileA));
        assertFalse(filteredSet.contains(fileB));
    }

    /*
     * Test method for {@link FileFilterUtils#filterSet(IOFileFilter, java.lang.Iterable)} that tests {@code null}
     * parameters and {@code null} elements in the provided set.
     */
    @Test
    public void testFilterSetNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> FileFilterUtils.filterSet(null, Collections.emptySet()));

        final IOFileFilter filter = FileFilterUtils.trueFileFilter();
        FileFilterUtils.filterSet(filter, new HashSet<>(Collections.singletonList(null)));

        final Set<File> filteredSet = FileFilterUtils.filterSet(filter, (Set<File>) null);
        assertEquals(0, filteredSet.size());
    }

    @Test
    public void testHidden() {
        final File hiddenDirFile = new File(SVN_DIR_NAME);
        final Path hiddenDirPath = hiddenDirFile.toPath();
        if (hiddenDirFile.exists()) {
            assertFiltering(HiddenFileFilter.HIDDEN, hiddenDirFile, hiddenDirFile.isHidden());
            assertFiltering(HiddenFileFilter.HIDDEN, hiddenDirPath, hiddenDirFile.isHidden());
            assertFiltering(HiddenFileFilter.VISIBLE, hiddenDirFile, !hiddenDirFile.isHidden());
            assertFiltering(HiddenFileFilter.VISIBLE, hiddenDirPath, !hiddenDirFile.isHidden());
        }
        final Path path = temporaryFolder.toPath();
        assertFiltering(HiddenFileFilter.HIDDEN, temporaryFolder, false);
        assertFiltering(HiddenFileFilter.HIDDEN, path, false);
        assertFiltering(HiddenFileFilter.VISIBLE, temporaryFolder, true);
        assertFiltering(HiddenFileFilter.VISIBLE, path, true);
    }

    @Test
    public void testMagicNumberFileFilterBytes() throws Exception {
        final byte[] classFileMagicNumber = {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
        final String xmlFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\">\n" + "<element>text</element>";

        final File classAFile = new File(temporaryFolder, "A.class");
        final Path classAPath = classAFile.toPath();
        final File xmlBFile = new File(temporaryFolder, "B.xml");
        final Path xmlBPath = xmlBFile.toPath();
        final File emptyFile = new File(temporaryFolder, "C.xml");
        final Path emptyPath = emptyFile.toPath();
        final File dirFile = new File(temporaryFolder, "D");
        final Path dirPath = dirFile.toPath();
        dirFile.mkdirs();

        try (final OutputStream classFileAStream = FileUtils.openOutputStream(classAFile)) {
            IOUtils.write(classFileMagicNumber, classFileAStream);
            TestUtils.generateTestData(classFileAStream, 32);
        }

        FileUtils.write(xmlBFile, xmlFileContent, StandardCharsets.UTF_8);
        FileUtils.touch(emptyFile);

        IOFileFilter filter = new MagicNumberFileFilter(classFileMagicNumber);

        assertFiltering(filter, classAFile, true);
        assertFiltering(filter, classAPath, true);
        assertFiltering(filter, xmlBFile, false);
        assertFiltering(filter, xmlBPath, false);
        assertFiltering(filter, emptyFile, false);
        assertFiltering(filter, emptyPath, false);
        assertFiltering(filter, dirFile, false);
        assertFiltering(filter, dirPath, false);

        filter = FileFilterUtils.magicNumberFileFilter(classFileMagicNumber);

        assertFiltering(filter, classAFile, true);
        assertFiltering(filter, classAPath, true);
        assertFiltering(filter, xmlBFile, false);
        assertFiltering(filter, xmlBPath, false);
        assertFiltering(filter, emptyFile, false);
        assertFiltering(filter, emptyPath, false);
        assertFiltering(filter, dirFile, false);
        assertFiltering(filter, dirPath, false);
    }

    @Test
    public void testMagicNumberFileFilterBytesOffset() throws Exception {
        final byte[] tarMagicNumber = {0x75, 0x73, 0x74, 0x61, 0x72};
        final long tarMagicNumberOffset = 257;

        final File tarFileA = new File(temporaryFolder, "A.tar");
        final File randomFileB = new File(temporaryFolder, "B.txt");
        final File dir = new File(temporaryFolder, "D");
        dir.mkdirs();

        try (final OutputStream tarFileAStream = FileUtils.openOutputStream(tarFileA)) {
            TestUtils.generateTestData(tarFileAStream, tarMagicNumberOffset);
            IOUtils.write(tarMagicNumber, tarFileAStream);
        }

        if (!randomFileB.getParentFile().exists()) {
            fail("Cannot create file " + randomFileB + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(randomFileB.toPath()))) {
            TestUtils.generateTestData(output, 2 * tarMagicNumberOffset);
        }

        IOFileFilter filter = new MagicNumberFileFilter(tarMagicNumber, tarMagicNumberOffset);

        assertFiltering(filter, tarFileA, true);
        assertFiltering(filter, randomFileB, false);
        assertFiltering(filter, dir, false);

        filter = FileFilterUtils.magicNumberFileFilter(tarMagicNumber, tarMagicNumberOffset);

        assertFiltering(filter, tarFileA, true);
        assertFiltering(filter, randomFileB, false);
        assertFiltering(filter, dir, false);
    }

    // -----------------------------------------------------------------------

    @Test
    public void testMagicNumberFileFilterString() throws Exception {
        final byte[] classFileMagicNumber = {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
        final String xmlFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\">\n" + "<element>text</element>";
        final String xmlMagicNumber = "<?xml version=\"1.0\"";

        final File classFileA = new File(temporaryFolder, "A.class");
        final File xmlFileB = new File(temporaryFolder, "B.xml");
        final File dir = new File(temporaryFolder, "D");
        dir.mkdirs();

        try (final OutputStream classFileAStream = FileUtils.openOutputStream(classFileA)) {
            IOUtils.write(classFileMagicNumber, classFileAStream);
            TestUtils.generateTestData(classFileAStream, 32);
        }

        FileUtils.write(xmlFileB, xmlFileContent, StandardCharsets.UTF_8);

        IOFileFilter filter = new MagicNumberFileFilter(xmlMagicNumber);

        assertFiltering(filter, classFileA, false);
        assertFiltering(filter, xmlFileB, true);
        assertFiltering(filter, dir, false);

        filter = FileFilterUtils.magicNumberFileFilter(xmlMagicNumber);

        assertFiltering(filter, classFileA, false);
        assertFiltering(filter, xmlFileB, true);
        assertFiltering(filter, dir, false);
    }

    @Test
    public void testMagicNumberFileFilterStringOffset() throws Exception {
        final String tarMagicNumber = "ustar";
        final long tarMagicNumberOffset = 257;

        final File tarFileA = new File(temporaryFolder, "A.tar");
        final File randomFileB = new File(temporaryFolder, "B.txt");
        final File dir = new File(temporaryFolder, "D");
        dir.mkdirs();

        try (final OutputStream tarFileAStream = FileUtils.openOutputStream(tarFileA)) {
            TestUtils.generateTestData(tarFileAStream, tarMagicNumberOffset);
            IOUtils.write(tarMagicNumber, tarFileAStream, StandardCharsets.UTF_8);
        }

        if (!randomFileB.getParentFile().exists()) {
            fail("Cannot create file " + randomFileB + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(randomFileB.toPath()))) {
            TestUtils.generateTestData(output, 2 * tarMagicNumberOffset);
        }

        IOFileFilter filter = new MagicNumberFileFilter(tarMagicNumber, tarMagicNumberOffset);

        assertFiltering(filter, tarFileA, true);
        assertFiltering(filter, randomFileB, false);
        assertFiltering(filter, dir, false);

        filter = FileFilterUtils.magicNumberFileFilter(tarMagicNumber, tarMagicNumberOffset);

        assertFiltering(filter, tarFileA, true);
        assertFiltering(filter, randomFileB, false);
        assertFiltering(filter, dir, false);
    }

    @Test
    public void testMagicNumberFileFilterValidation() {
        assertThrows(IllegalArgumentException.class, () -> new MagicNumberFileFilter((String) null, 0));
        assertThrows(IllegalArgumentException.class, () -> new MagicNumberFileFilter("0", -1));
        assertThrows(IllegalArgumentException.class, () -> new MagicNumberFileFilter("", 0));
        assertThrows(IllegalArgumentException.class, () -> new MagicNumberFileFilter((byte[]) null, 0));
        assertThrows(IllegalArgumentException.class, () -> new MagicNumberFileFilter(new byte[] {0}, -1));
        assertThrows(IllegalArgumentException.class, () -> new MagicNumberFileFilter(new byte[] {}, 0));
    }

    @Test
    public void testMakeCVSAware() throws Exception {
        final IOFileFilter filter1 = FileFilterUtils.makeCVSAware(null);
        final IOFileFilter filter2 = FileFilterUtils.makeCVSAware(FileFilterUtils.nameFileFilter("test-file1.txt"));

        File file = new File(temporaryFolder, "CVS");
        file.mkdirs();
        assertFiltering(filter1, file, false);
        assertFiltering(filter2, file, false);
        FileUtils.deleteDirectory(file);

        file = new File(temporaryFolder, "test-file1.txt");
        if (!file.getParentFile().exists()) {
            fail("Cannot create file " + file + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output2 = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            TestUtils.generateTestData(output2, 0);
        }
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, true);

        file = new File(temporaryFolder, "test-file2.log");
        if (!file.getParentFile().exists()) {
            fail("Cannot create file " + file + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            TestUtils.generateTestData(output1, 0);
        }
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, false);

        file = new File(temporaryFolder, "CVS");
        if (!file.getParentFile().exists()) {
            fail("Cannot create file " + file + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, false);
    }

    @Test
    public void testMakeDirectoryOnly() throws Exception {
        assertSame(DirectoryFileFilter.DIRECTORY, FileFilterUtils.makeDirectoryOnly(null));

        final IOFileFilter filter = FileFilterUtils.makeDirectoryOnly(FileFilterUtils.nameFileFilter("B"));

        final File fileA = new File(temporaryFolder, "A");
        final File fileB = new File(temporaryFolder, "B");

        fileA.mkdirs();
        fileB.mkdirs();

        assertFiltering(filter, fileA, false);
        assertFiltering(filter, fileB, true);

        FileUtils.deleteDirectory(fileA);
        FileUtils.deleteDirectory(fileB);

        if (!fileA.getParentFile().exists()) {
            fail("Cannot create file " + fileA + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(fileA.toPath()))) {
            TestUtils.generateTestData(output1, 32);
        }
        if (!fileB.getParentFile().exists()) {
            fail("Cannot create file " + fileB + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(fileB.toPath()))) {
            TestUtils.generateTestData(output, 32);
        }

        assertFiltering(filter, fileA, false);
        assertFiltering(filter, fileB, false);

        fileA.delete();
        fileB.delete();
    }

    // -----------------------------------------------------------------------
    @Test
    public void testMakeFileOnly() throws Exception {
        assertSame(FileFileFilter.INSTANCE, FileFilterUtils.makeFileOnly(null));

        final IOFileFilter filter = FileFilterUtils.makeFileOnly(FileFilterUtils.nameFileFilter("B"));

        final File fileA = new File(temporaryFolder, "A");
        final File fileB = new File(temporaryFolder, "B");

        fileA.mkdirs();
        fileB.mkdirs();

        assertFiltering(filter, fileA, false);
        assertFiltering(filter, fileB, false);

        FileUtils.deleteDirectory(fileA);
        FileUtils.deleteDirectory(fileB);

        if (!fileA.getParentFile().exists()) {
            fail("Cannot create file " + fileA + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(fileA.toPath()))) {
            TestUtils.generateTestData(output1, 32);
        }
        if (!fileB.getParentFile().exists()) {
            fail("Cannot create file " + fileB + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(fileB.toPath()))) {
            TestUtils.generateTestData(output, 32);
        }

        assertFiltering(filter, fileA, false);
        assertFiltering(filter, fileB, true);

        fileA.delete();
        fileB.delete();
    }

    @Test
    public void testMakeSVNAware() throws Exception {
        final IOFileFilter filter1 = FileFilterUtils.makeSVNAware(null);
        final IOFileFilter filter2 = FileFilterUtils.makeSVNAware(FileFilterUtils.nameFileFilter("test-file1.txt"));

        File file = new File(temporaryFolder, SVN_DIR_NAME);
        file.mkdirs();
        assertFiltering(filter1, file, false);
        assertFiltering(filter2, file, false);
        FileUtils.deleteDirectory(file);

        file = new File(temporaryFolder, "test-file1.txt");
        if (!file.getParentFile().exists()) {
            fail("Cannot create file " + file + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output2 = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            TestUtils.generateTestData(output2, 0);
        }
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, true);

        file = new File(temporaryFolder, "test-file2.log");
        if (!file.getParentFile().exists()) {
            fail("Cannot create file " + file + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            TestUtils.generateTestData(output1, 0);
        }
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, false);

        file = new File(temporaryFolder, SVN_DIR_NAME);
        if (!file.getParentFile().exists()) {
            fail("Cannot create file " + file + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }
        assertFiltering(filter1, file, true);
        assertFiltering(filter2, file, false);
    }

    @Test
    public void testNameFilter() {
        assertFooBarFileFiltering(new NameFileFilter("foo", "bar"));
    }

    @Test
    public void testNameFilterNullArgument() {
        final String test = null;
        try {
            new NameFileFilter(test);
            fail("constructing a NameFileFilter with a null String argument should fail.");
        } catch (final IllegalArgumentException ignore) {
        }

        try {
            FileFilterUtils.nameFileFilter(test, IOCase.INSENSITIVE);
            fail("constructing a NameFileFilter with a null String argument should fail.");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testNameFilterNullArrayArgument() {
        assertThrows(IllegalArgumentException.class, () -> new NameFileFilter((String[]) null));
    }

    @Test
    public void testNameFilterNullListArgument() {
        final List<String> test = null;
        assertThrows(IllegalArgumentException.class, () -> new NameFileFilter(test));
    }

    @Test
    public void testNegate() {
        final IOFileFilter filter = FileFilterUtils.notFileFilter(FileFilterUtils.trueFileFilter());
        assertFiltering(filter, new File("foo.test"), false);
        assertFiltering(filter, new File("foo"), false);
        assertFiltering(filter.negate(), new File("foo"), true);
        assertFiltering(filter, (File) null, false);
        assertThrows(IllegalArgumentException.class, () -> new NotFileFilter(null));
    }

    @Test
    public void testNullFilters() {
        try {
            FileFilterUtils.toList((IOFileFilter) null);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException ignore) {
            // expected
        }
        try {
            FileFilterUtils.toList(new IOFileFilter[] {null});
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException ignore) {
            // expected
        }
    }

    @Test
    public void testOr() {
        final IOFileFilter trueFilter = TrueFileFilter.INSTANCE;
        final IOFileFilter falseFilter = FalseFileFilter.INSTANCE;
        final File testFile = new File("foo.test");
        final Path testPath = testFile.toPath();
        assertFiltering(new OrFileFilter(trueFilter, trueFilter), testFile, true);
        assertFiltering(new OrFileFilter(trueFilter, falseFilter), testFile, true);
        assertFiltering(new OrFileFilter(falseFilter, trueFilter), testFile, true);
        assertFiltering(new OrFileFilter(falseFilter, falseFilter), testFile, false);
        assertFiltering(new OrFileFilter(), testFile, false);
        //
        assertFiltering(new OrFileFilter(trueFilter, trueFilter), testPath, true);
        assertFiltering(new OrFileFilter(trueFilter, falseFilter), testPath, true);
        assertFiltering(new OrFileFilter(falseFilter, trueFilter), testPath, true);
        assertFiltering(new OrFileFilter(falseFilter, falseFilter), testPath, false);
        assertFiltering(new OrFileFilter(), testPath, false);
        //
        assertFiltering(falseFilter.or(trueFilter), testPath, true);

        final List<IOFileFilter> filters = new ArrayList<>();
        filters.add(trueFilter);
        filters.add(falseFilter);

        final OrFileFilter orFilter = new OrFileFilter(filters);

        assertFiltering(orFilter, testFile, true);
        assertFiltering(orFilter, testPath, true);
        assertEquals(orFilter.getFileFilters(), filters);
        orFilter.removeFileFilter(trueFilter);
        assertFiltering(orFilter, testFile, false);
        assertFiltering(orFilter, testPath, false);
        orFilter.setFileFilters(filters);
        assertFiltering(orFilter, testFile, true);
        assertFiltering(orFilter, testPath, true);

        assertTrue(orFilter.accept(testFile.getParentFile(), testFile.getName()));
        assertEquals(FileVisitResult.CONTINUE, orFilter.accept(testPath, null));
        orFilter.removeFileFilter(trueFilter);
        assertFalse(orFilter.accept(testFile.getParentFile(), testFile.getName()));
        assertEquals(FileVisitResult.TERMINATE, orFilter.accept(testPath, null));

        assertThrows(NullPointerException.class, () -> new OrFileFilter(falseFilter, null));
    }

    @Test
    public void testPathEqualsFilter() {
        assertFooBarFileFiltering(
            new PathEqualsFileFilter(Paths.get("foo")).or(new PathEqualsFileFilter(Paths.get("bar"))));
    }

    @Test
    public void testPrefix() {
        IOFileFilter filter = new PrefixFileFilter("foo", "bar");
        final File testFile = new File("test");
        final Path testPath = testFile.toPath();
        final File fredFile = new File("fred");
        final Path fredPath = fredFile.toPath();

        assertFiltering(filter, new File("foo.test"), true);
        assertFiltering(filter, new File("FOO.test"), false); // case-sensitive
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("food/"), true);
        //
        assertFiltering(filter, new File("foo.test").toPath(), true);
        assertFiltering(filter, new File("FOO.test").toPath(), false); // case-sensitive
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("bar").toPath(), true);
        assertFiltering(filter, new File("food/").toPath(), true);

        filter = FileFilterUtils.prefixFileFilter("bar");
        assertFiltering(filter, new File("barred\\"), true);
        assertFiltering(filter, new File("test"), false);
        assertFiltering(filter, new File("fo_o.test"), false);
        assertFiltering(filter, new File("abar.exe"), false);
        //
        assertFiltering(filter, new File("barred\\").toPath(), true);
        assertFiltering(filter, new File("test").toPath(), false);
        assertFiltering(filter, new File("fo_o.test").toPath(), false);
        assertFiltering(filter, new File("abar.exe").toPath(), false);

        filter = new PrefixFileFilter("tes");
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("fred"), false);
        //
        assertFiltering(filter, new File("test").toPath(), true);
        assertFiltering(filter, new File("fred").toPath(), false);

        assertTrue(filter.accept(testFile.getParentFile(), testFile.getName()));
        assertFalse(filter.accept(fredFile.getParentFile(), fredFile.getName()));
        //
        assertEquals(FileVisitResult.CONTINUE, filter.accept(testPath, null));
        assertEquals(FileVisitResult.TERMINATE, filter.accept(fredPath, null));

        final List<String> prefixes = Arrays.asList("foo", "fre");
        final IOFileFilter listFilter = new PrefixFileFilter(prefixes);

        assertFalse(listFilter.accept(testFile.getParentFile(), testFile.getName()));
        assertTrue(listFilter.accept(fredFile.getParentFile(), fredFile.getName()));
        //
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(testPath, null));
        assertEquals(FileVisitResult.CONTINUE, listFilter.accept(fredPath, null));

        assertThrows(IllegalArgumentException.class, () -> new PrefixFileFilter((String) null));
        assertThrows(IllegalArgumentException.class, () -> new PrefixFileFilter((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> new PrefixFileFilter((List<String>) null));
    }

    @Test
    public void testPrefixCaseInsensitive() {

        IOFileFilter filter = new PrefixFileFilter(new String[] {"foo", "bar"}, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.test1"), true);
        assertFiltering(filter, new File("bar.test1"), true);
        assertFiltering(filter, new File("FOO.test1"), true); // case-sensitive
        assertFiltering(filter, new File("BAR.test1"), true); // case-sensitive

        filter = new PrefixFileFilter("bar", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.test2"), false);
        assertFiltering(filter, new File("bar.test2"), true);
        assertFiltering(filter, new File("FOO.test2"), false); // case-sensitive
        assertFiltering(filter, new File("BAR.test2"), true); // case-sensitive

        final List<String> prefixes = Arrays.asList("foo", "bar");
        filter = new PrefixFileFilter(prefixes, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.test3"), true);
        assertFiltering(filter, new File("bar.test3"), true);
        assertFiltering(filter, new File("FOO.test3"), true); // case-sensitive
        assertFiltering(filter, new File("BAR.test3"), true); // case-sensitive

        assertThrows(IllegalArgumentException.class, () -> new PrefixFileFilter((String) null, IOCase.INSENSITIVE));
        assertThrows(IllegalArgumentException.class, () -> new PrefixFileFilter((String[]) null, IOCase.INSENSITIVE));
        assertThrows(IllegalArgumentException.class, () -> new PrefixFileFilter((List<String>) null, IOCase.INSENSITIVE));
        // FileFilterUtils.prefixFileFilter(String, IOCase) tests
        filter = FileFilterUtils.prefixFileFilter("bar", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.test2"), false);
        assertFiltering(filter, new File("bar.test2"), true);
        assertFiltering(filter, new File("FOO.test2"), false); // case-sensitive
        assertFiltering(filter, new File("BAR.test2"), true); // case-sensitive

        assertThrows(IllegalArgumentException.class, () -> FileFilterUtils.prefixFileFilter(null, IOCase.INSENSITIVE));
    }

    @Test
    public void testSizeFilterOnFiles() throws Exception {
        final File smallFile = new File(temporaryFolder, "small.txt");
        if (!smallFile.getParentFile().exists()) {
            fail("Cannot create file " + smallFile + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(smallFile.toPath()))) {
            TestUtils.generateTestData(output1, 32);
        }
        final File largeFile = new File(temporaryFolder, "large.txt");
        if (!largeFile.getParentFile().exists()) {
            fail("Cannot create file " + largeFile + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(largeFile.toPath()))) {
            TestUtils.generateTestData(output, 128);
        }
        final IOFileFilter filter1 = FileFilterUtils.sizeFileFilter(64);
        final IOFileFilter filter2 = FileFilterUtils.sizeFileFilter(64, true);
        final IOFileFilter filter3 = FileFilterUtils.sizeFileFilter(64, false);

        assertFiltering(filter1, smallFile, false);
        assertFiltering(filter2, smallFile, false);
        assertFiltering(filter3, smallFile, true);
        assertFiltering(filter1, largeFile, true);
        assertFiltering(filter2, largeFile, true);
        assertFiltering(filter3, largeFile, false);

        // size range tests
        final IOFileFilter filter4 = FileFilterUtils.sizeRangeFileFilter(33, 127);
        final IOFileFilter filter5 = FileFilterUtils.sizeRangeFileFilter(32, 127);
        final IOFileFilter filter6 = FileFilterUtils.sizeRangeFileFilter(33, 128);
        final IOFileFilter filter7 = FileFilterUtils.sizeRangeFileFilter(31, 129);
        final IOFileFilter filter8 = FileFilterUtils.sizeRangeFileFilter(128, 128);

        assertFiltering(filter4, smallFile, false);
        assertFiltering(filter4, largeFile, false);
        assertFiltering(filter5, smallFile, true);
        assertFiltering(filter5, largeFile, false);
        assertFiltering(filter6, smallFile, false);
        assertFiltering(filter6, largeFile, true);
        assertFiltering(filter7, smallFile, true);
        assertFiltering(filter7, largeFile, true);
        assertFiltering(filter8, largeFile, true);

        assertThrows(IllegalArgumentException.class, () -> FileFilterUtils.sizeFileFilter(-1));
    }

    @Test
    public void testSizeFilterOnPaths() throws Exception {
        final Path smallFile = Paths.get(temporaryFolder.toString(), "small.txt");
        if (Files.notExists(smallFile.getParent())) {
            fail("Cannot create file " + smallFile + " as the parent directory does not exist");
        }
        try (OutputStream output = Files.newOutputStream(smallFile)) {
            TestUtils.generateTestData(output, 32);
        }
        final Path largeFile = Paths.get(temporaryFolder.toString(), "large.txt");
        if (Files.notExists(largeFile.getParent())) {
            fail("Cannot create file " + largeFile + " as the parent directory does not exist");
        }
        try (OutputStream output = Files.newOutputStream(largeFile)) {
            TestUtils.generateTestData(output, 128);
        }
        final IOFileFilter filter1 = FileFilterUtils.sizeFileFilter(64);
        final IOFileFilter filter2 = FileFilterUtils.sizeFileFilter(64, true);
        final IOFileFilter filter3 = FileFilterUtils.sizeFileFilter(64, false);

        assertFiltering(filter1, smallFile, false);
        assertFiltering(filter2, smallFile, false);
        assertFiltering(filter3, smallFile, true);
        assertFiltering(filter1, largeFile, true);
        assertFiltering(filter2, largeFile, true);
        assertFiltering(filter3, largeFile, false);

        // size range tests
        final IOFileFilter filter4 = FileFilterUtils.sizeRangeFileFilter(33, 127);
        final IOFileFilter filter5 = FileFilterUtils.sizeRangeFileFilter(32, 127);
        final IOFileFilter filter6 = FileFilterUtils.sizeRangeFileFilter(33, 128);
        final IOFileFilter filter7 = FileFilterUtils.sizeRangeFileFilter(31, 129);
        final IOFileFilter filter8 = FileFilterUtils.sizeRangeFileFilter(128, 128);

        assertFiltering(filter4, smallFile, false);
        assertFiltering(filter4, largeFile, false);
        assertFiltering(filter5, smallFile, true);
        assertFiltering(filter5, largeFile, false);
        assertFiltering(filter6, smallFile, false);
        assertFiltering(filter6, largeFile, true);
        assertFiltering(filter7, smallFile, true);
        assertFiltering(filter7, largeFile, true);
        assertFiltering(filter8, largeFile, true);

        assertThrows(IllegalArgumentException.class, () -> FileFilterUtils.sizeFileFilter(-1));
    }

    @Test
    public void testSuffix() {
        IOFileFilter filter = new SuffixFileFilter("tes", "est");
        final File testFile = new File("test");
        final Path testPath = testFile.toPath();
        final File fredFile = new File("fred");
        final Path fredPath = fredFile.toPath();
        //
        assertFiltering(filter, new File("fred.tes"), true);
        assertFiltering(filter, new File("fred.est"), true);
        assertFiltering(filter, new File("fred.EST"), false); // case-sensitive
        assertFiltering(filter, new File("fred.exe"), false);
        //
        assertFiltering(filter, new File("fred.tes").toPath(), true);
        assertFiltering(filter, new File("fred.est").toPath(), true);
        assertFiltering(filter, new File("fred.EST").toPath(), false); // case-sensitive
        assertFiltering(filter, new File("fred.exe").toPath(), false);

        filter = FileFilterUtils.or(FileFilterUtils.suffixFileFilter("tes"), FileFilterUtils.suffixFileFilter("est"));
        assertFiltering(filter, new File("fred"), false);
        assertFiltering(filter, new File(".tes"), true);
        assertFiltering(filter, new File("fred.test"), true);
        //
        assertFiltering(filter, new File("fred").toPath(), false);
        assertFiltering(filter, new File(".tes").toPath(), true);
        assertFiltering(filter, new File("fred.test").toPath(), true);

        filter = new SuffixFileFilter("est");
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("fred"), false);
        //
        assertFiltering(filter, new File("test").toPath(), true);
        assertFiltering(filter, new File("fred").toPath(), false);

        assertTrue(filter.accept(testFile.getParentFile(), testFile.getName()));
        assertFalse(filter.accept(fredFile.getParentFile(), fredFile.getName()));
        //
        assertEquals(FileVisitResult.CONTINUE, filter.accept(testPath, null));
        assertEquals(FileVisitResult.TERMINATE, filter.accept(fredPath, null));

        final List<String> prefixes = Arrays.asList("ood", "red");
        final IOFileFilter listFilter = new SuffixFileFilter(prefixes);

        assertFalse(listFilter.accept(testFile.getParentFile(), testFile.getName()));
        assertTrue(listFilter.accept(fredFile.getParentFile(), fredFile.getName()));
        //
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(testPath, null));
        assertEquals(FileVisitResult.CONTINUE, listFilter.accept(fredPath, null));

        assertThrows(IllegalArgumentException.class, () -> new SuffixFileFilter((String) null));
        assertThrows(IllegalArgumentException.class, () -> new SuffixFileFilter((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> new SuffixFileFilter((List<String>) null));
    }

    @Test
    public void testSuffixCaseInsensitive() {

        IOFileFilter filter = new SuffixFileFilter(new String[] {"tes", "est"}, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo.tes"), true);
        assertFiltering(filter, new File("foo.est"), true);
        assertFiltering(filter, new File("foo.EST"), true); // case-sensitive
        assertFiltering(filter, new File("foo.TES"), true); // case-sensitive
        assertFiltering(filter, new File("foo.exe"), false);

        filter = new SuffixFileFilter("est", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("TEST"), true);

        final List<String> suffixes = Arrays.asList("tes", "est");
        filter = new SuffixFileFilter(suffixes, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("bar.tes"), true);
        assertFiltering(filter, new File("bar.est"), true);
        assertFiltering(filter, new File("bar.EST"), true); // case-sensitive
        assertFiltering(filter, new File("bar.TES"), true); // case-sensitive
        assertFiltering(filter, new File("bar.exe"), false);

        assertThrows(IllegalArgumentException.class, () -> new SuffixFileFilter((String) null, IOCase.INSENSITIVE));
        assertThrows(IllegalArgumentException.class, () -> new SuffixFileFilter((String[]) null, IOCase.INSENSITIVE));
        assertThrows(IllegalArgumentException.class, () -> new SuffixFileFilter((List<String>) null, IOCase.INSENSITIVE));

        // FileFilterUtils.suffixFileFilter(String, IOCase) tests
        filter = FileFilterUtils.suffixFileFilter("est", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("test"), true);
        assertFiltering(filter, new File("TEST"), true);

        assertThrows(IllegalArgumentException.class, () -> FileFilterUtils.suffixFileFilter(null, IOCase.INSENSITIVE));
    }

    @Test
    public void testTrue() {
        final IOFileFilter filter = FileFilterUtils.trueFileFilter();
        assertFiltering(filter, new File("foo.test"), true);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, (File) null, true);
        //
        assertFiltering(filter, new File("foo.test").toPath(), true);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, (Path) null, true);
        //
        assertSame(TrueFileFilter.TRUE, TrueFileFilter.INSTANCE);
        assertSame(FalseFileFilter.FALSE, TrueFileFilter.INSTANCE.negate());
        assertSame(FalseFileFilter.INSTANCE, TrueFileFilter.INSTANCE.negate());
        assertNotNull(TrueFileFilter.INSTANCE.toString());
    }

}
