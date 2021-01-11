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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This is used to test FileUtils for correctness.
 *
 * @see FileUtils
 */
@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"}) // unit tests include tests of many deprecated methods
public class FileUtilsTestCase {

    /**
     * DirectoryWalker implementation that recursively lists all files and directories.
     */
    static class ListDirectoryWalker extends DirectoryWalker<File> {

        ListDirectoryWalker() {
        }

        @Override
        protected void handleDirectoryStart(final File directory, final int depth, final Collection<File> results) throws IOException {
            // Add all directories except the starting directory
            if (depth > 0) {
                results.add(directory);
            }
        }

        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> results) throws IOException {
            results.add(file);
        }

        List<File> list(final File startDirectory) throws IOException {
            final ArrayList<File> files = new ArrayList<>();
            walk(startDirectory, files);
            return files;
        }
    }

    // Test helper class to pretend a file is shorter than it is
    private static class ShorterFile extends File {
        private static final long serialVersionUID = 1L;

        public ShorterFile(final String pathname) {
            super(pathname);
        }

        @Override
        public long length() {
            return super.length() - 1;
        }
    }

    /** Test data. */
    private static final long DATE3 = 1000000002000L;

    /** Test data. */
    private static final long DATE2 = 1000000001000L;

    /** Test data. */
    private static final long DATE1 = 1000000000000L;

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;

    /**
     * Size of test directory.
     */
    private static final BigInteger TEST_DIRECTORY_SIZE_BI = BigInteger.ZERO;

    /**
     * Size (greater of zero) of test file.
     */
    private static final BigInteger TEST_DIRECTORY_SIZE_GT_ZERO_BI = BigInteger.valueOf(100);

    /**
     * List files recursively
     */
    private static final ListDirectoryWalker LIST_WALKER = new ListDirectoryWalker();
    @TempDir
    public File temporaryFolder;

    /**
     * Delay in milliseconds to make sure test for "last modified date" are accurate
     */
    //private static final int LAST_MODIFIED_DELAY = 600;

    private File testFile1;
    private File testFile2;

    private long testFile1Size;

    private long testFile2Size;

    private void backDateFile10Minutes(final File testFile) throws IOException {
        final long mins10 = 1000 * 60 * 10;
        final long lastModified1 = getLastModifiedMillis(testFile);
        assertTrue(setLastModifiedMillis(testFile, lastModified1 - mins10));
        // ensure it was changed
        assertNotEquals(getLastModifiedMillis(testFile), lastModified1, "Should have changed source date");
    }

    private void consumeRemaining(final Iterator<File> iterator) {
        if (iterator != null) {
            iterator.forEachRemaining(e -> {});
        }
    }

    private void createCircularSymLink(final File file) throws IOException {
        if (!FilenameUtils.isSystemWindows()) {
            Runtime.getRuntime()
                    .exec("ln -s " + file + "/.. " + file + "/cycle");
        } else {
            try {
                Runtime.getRuntime()
                        .exec("mklink /D " + file + "/cycle" + file + "/.. ");
            } catch (final IOException ioe) { // So that tests run in FAT filesystems
                //don't fail
            }
        }
    }

    private void createFilesForTestCopyDirectory(final File grandParentDir, final File parentDir, final File childDir) throws Exception {
        final File childDir2 = new File(parentDir, "child2");
        final File grandChildDir = new File(childDir, "grandChild");
        final File grandChild2Dir = new File(childDir2, "grandChild2");
        final File file1 = new File(grandParentDir, "file1.txt");
        final File file2 = new File(parentDir, "file2.txt");
        final File file3 = new File(childDir, "file3.txt");
        final File file4 = new File(childDir2, "file4.txt");
        final File file5 = new File(grandChildDir, "file5.txt");
        final File file6 = new File(grandChild2Dir, "file6.txt");
        FileUtils.deleteDirectory(grandParentDir);
        grandChildDir.mkdirs();
        grandChild2Dir.mkdirs();
        FileUtils.writeStringToFile(file1, "File 1 in grandparent", "UTF8");
        FileUtils.writeStringToFile(file2, "File 2 in parent", "UTF8");
        FileUtils.writeStringToFile(file3, "File 3 in child", "UTF8");
        FileUtils.writeStringToFile(file4, "File 4 in child2", "UTF8");
        FileUtils.writeStringToFile(file5, "File 5 in grandChild", "UTF8");
        FileUtils.writeStringToFile(file6, "File 6 in grandChild2", "UTF8");
    }

    private long getLastModifiedMillis(final File file) throws IOException {
        return file.lastModified();
        //https://bugs.openjdk.java.net/browse/JDK-8177809
        //return Files.getLastModifiedTime(file.toPath()).toMillis();
    }

    private String getName() {
        return this.getClass().getSimpleName();
    }

    private void iterateFilesAndDirs(final File dir, final IOFileFilter fileFilter,
        final IOFileFilter dirFilter, final Collection<File> expectedFilesAndDirs) {
        final Iterator<File> iterator;
        int filesCount = 0;
        iterator = FileUtils.iterateFilesAndDirs(dir, fileFilter, dirFilter);
        try {
            final List<File> actualFiles = new ArrayList<>();
            while (iterator.hasNext()) {
                filesCount++;
                final File file = iterator.next();
                actualFiles.add(file);
                assertTrue(expectedFilesAndDirs.contains(file),
                    () -> "Unexpected directory/file " + file + ", expected one of " + expectedFilesAndDirs);
            }
            assertEquals(expectedFilesAndDirs.size(), filesCount, () -> actualFiles.toString());
        } finally {
            // MUST consume until the end in order to close the underlying stream.
            consumeRemaining(iterator);
        }
    }

    //-----------------------------------------------------------------------
    void openOutputStream_noParent(final boolean createFile) throws Exception {
        final File file = new File("test.txt");
        assertNull(file.getParentFile());
        try {
            if (createFile) {
                TestUtils.createLineBasedFile(file, new String[]{"Hello"});
            }
            try (FileOutputStream out = FileUtils.openOutputStream(file)) {
                out.write(0);
            }
            assertTrue(file.exists());
        } finally {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    private boolean setLastModifiedMillis(final File testFile, final long millis) {
        return testFile.setLastModified(millis);
//        try {
//            Files.setLastModifiedTime(testFile.toPath(), FileTime.fromMillis(millis));
//        } catch (IOException e) {
//            return false;
//        }
//        return true;
    }

    @BeforeEach
    public void setUp() throws Exception {
        testFile1 = new File(temporaryFolder, "file1-test.txt");
        testFile2 = new File(temporaryFolder, "file1a-test.txt");

        testFile1Size = testFile1.length();
        testFile2Size = testFile2.length();
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output3 =
                new BufferedOutputStream(new FileOutputStream(testFile1))) {
            TestUtils.generateTestData(output3, testFile1Size);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output2 =
                new BufferedOutputStream(new FileOutputStream(testFile2))) {
            TestUtils.generateTestData(output2, testFile2Size);
        }
        FileUtils.deleteDirectory(temporaryFolder);
        temporaryFolder.mkdirs();
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1))) {
            TestUtils.generateTestData(output1, testFile1Size);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2))) {
            TestUtils.generateTestData(output, testFile2Size);
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_openInputStream_exists() throws Exception {
        final File file = new File(temporaryFolder, "test.txt");
        TestUtils.createLineBasedFile(file, new String[]{"Hello"});
        try (FileInputStream in = FileUtils.openInputStream(file)) {
            assertEquals('H', in.read());
        }
    }

    @Test
    public void test_openInputStream_existsButIsDirectory() throws Exception {
        final File directory = new File(temporaryFolder, "subdir");
        directory.mkdirs();
        assertThrows(IOException.class, () -> FileUtils.openInputStream(directory));
    }

    @Test
    public void test_openInputStream_notExists() throws Exception {
        final File directory = new File(temporaryFolder, "test.txt");
        try (FileInputStream in = FileUtils.openInputStream(directory)) {
            fail();
        } catch (final IOException ioe) {
            // expected
        }
    }

    @Test
    public void test_openOutputStream_exists() throws Exception {
        final File file = new File(temporaryFolder, "test.txt");
        TestUtils.createLineBasedFile(file, new String[]{"Hello"});
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            out.write(0);
        }
        assertTrue(file.exists());
    }

    @Test
    public void test_openOutputStream_existsButIsDirectory() throws Exception {
        final File directory = new File(temporaryFolder, "subdir");
        directory.mkdirs();
        assertThrows(IllegalArgumentException.class, () -> FileUtils.openOutputStream(directory));
    }

    @Test
    public void test_openOutputStream_noParentCreateFile() throws Exception {
        openOutputStream_noParent(true);
    }

    @Test
    public void test_openOutputStream_noParentNoFile() throws Exception {
        openOutputStream_noParent(false);
    }

    @Test
    public void test_openOutputStream_notExists() throws Exception {
        final File file = new File(temporaryFolder, "a/test.txt");
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            out.write(0);
        }
        assertTrue(file.exists());
    }

    @Test
    public void test_openOutputStream_notExistsCannotCreate() throws Exception {
        // according to Wikipedia, most filing systems have a 256 limit on filename
        final String longStr =
                "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz";  // 300 chars
        final File file = new File(temporaryFolder, "a/" + longStr + "/test.txt");
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            fail();
        } catch (final IOException ioe) {
            // expected
        }
    }

    //-----------------------------------------------------------------------
    // byteCountToDisplaySize
    @Test
    public void testByteCountToDisplaySizeBigInteger() {
        final BigInteger b1023 = BigInteger.valueOf(1023);
        final BigInteger b1025 = BigInteger.valueOf(1025);
        final BigInteger KB1 = BigInteger.valueOf(1024);
        final BigInteger MB1 = KB1.multiply(KB1);
        final BigInteger GB1 = MB1.multiply(KB1);
        final BigInteger GB2 = GB1.add(GB1);
        final BigInteger TB1 = GB1.multiply(KB1);
        final BigInteger PB1 = TB1.multiply(KB1);
        final BigInteger EB1 = PB1.multiply(KB1);
        assertEquals("0 bytes", FileUtils.byteCountToDisplaySize(BigInteger.ZERO));
        assertEquals("1 bytes", FileUtils.byteCountToDisplaySize(BigInteger.ONE));
        assertEquals("1023 bytes", FileUtils.byteCountToDisplaySize(b1023));
        assertEquals("1 KB", FileUtils.byteCountToDisplaySize(KB1));
        assertEquals("1 KB", FileUtils.byteCountToDisplaySize(b1025));
        assertEquals("1023 KB", FileUtils.byteCountToDisplaySize(MB1.subtract(BigInteger.ONE)));
        assertEquals("1 MB", FileUtils.byteCountToDisplaySize(MB1));
        assertEquals("1 MB", FileUtils.byteCountToDisplaySize(MB1.add(BigInteger.ONE)));
        assertEquals("1023 MB", FileUtils.byteCountToDisplaySize(GB1.subtract(BigInteger.ONE)));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(GB1));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(GB1.add(BigInteger.ONE)));
        assertEquals("2 GB", FileUtils.byteCountToDisplaySize(GB2));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(GB2.subtract(BigInteger.ONE)));
        assertEquals("1 TB", FileUtils.byteCountToDisplaySize(TB1));
        assertEquals("1 PB", FileUtils.byteCountToDisplaySize(PB1));
        assertEquals("1 EB", FileUtils.byteCountToDisplaySize(EB1));
        assertEquals("7 EB", FileUtils.byteCountToDisplaySize(Long.MAX_VALUE));
        // Other MAX_VALUEs
        assertEquals("63 KB", FileUtils.byteCountToDisplaySize(BigInteger.valueOf(Character.MAX_VALUE)));
        assertEquals("31 KB", FileUtils.byteCountToDisplaySize(BigInteger.valueOf(Short.MAX_VALUE)));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(BigInteger.valueOf(Integer.MAX_VALUE)));
    }

    @SuppressWarnings("NumericOverflow")
    @Test
    public void testByteCountToDisplaySizeLong() {
        assertEquals("0 bytes", FileUtils.byteCountToDisplaySize(0));
        assertEquals("1 bytes", FileUtils.byteCountToDisplaySize(1));
        assertEquals("1023 bytes", FileUtils.byteCountToDisplaySize(1023));
        assertEquals("1 KB", FileUtils.byteCountToDisplaySize(1024));
        assertEquals("1 KB", FileUtils.byteCountToDisplaySize(1025));
        assertEquals("1023 KB", FileUtils.byteCountToDisplaySize(1024 * 1023));
        assertEquals("1 MB", FileUtils.byteCountToDisplaySize(1024 * 1024));
        assertEquals("1 MB", FileUtils.byteCountToDisplaySize(1024 * 1025));
        assertEquals("1023 MB", FileUtils.byteCountToDisplaySize(1024 * 1024 * 1023));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(1024 * 1024 * 1024));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(1024 * 1024 * 1025));
        assertEquals("2 GB", FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 2));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(1024 * 1024 * 1024 * 2 - 1));
        assertEquals("1 TB", FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 1024));
        assertEquals("1 PB", FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 1024 * 1024));
        assertEquals("1 EB", FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 1024 * 1024 * 1024));
        assertEquals("7 EB", FileUtils.byteCountToDisplaySize(Long.MAX_VALUE));
        // Other MAX_VALUEs
        assertEquals("63 KB", FileUtils.byteCountToDisplaySize(Character.MAX_VALUE));
        assertEquals("31 KB", FileUtils.byteCountToDisplaySize(Short.MAX_VALUE));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(Integer.MAX_VALUE));
    }

    @Test
    public void testChecksum() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(temporaryFolder, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");

        // compute the expected checksum
        final Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text.getBytes(StandardCharsets.US_ASCII), 0, text.length());
        final long expectedValue = expectedChecksum.getValue();

        // compute the checksum of the file
        final Checksum testChecksum = new CRC32();
        final Checksum resultChecksum = FileUtils.checksum(file, testChecksum);
        final long resultValue = resultChecksum.getValue();

        assertSame(testChecksum, resultChecksum);
        assertEquals(expectedValue, resultValue);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testChecksumCRC32() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(temporaryFolder, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");

        // compute the expected checksum
        final Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text.getBytes(StandardCharsets.US_ASCII), 0, text.length());
        final long expectedValue = expectedChecksum.getValue();

        // compute the checksum of the file
        final long resultValue = FileUtils.checksumCRC32(file);

        assertEquals(expectedValue, resultValue);
    }

    @Test
    public void testChecksumDouble() throws Exception {
        // create a test file
        final String text1 = "Imagination is more important than knowledge - Einstein";
        final File file1 = new File(temporaryFolder, "checksum-test.txt");
        FileUtils.writeStringToFile(file1, text1, "US-ASCII");

        // create a second test file
        final String text2 = "To be or not to be - Shakespeare";
        final File file2 = new File(temporaryFolder, "checksum-test2.txt");
        FileUtils.writeStringToFile(file2, text2, "US-ASCII");

        // compute the expected checksum
        final Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text1.getBytes(StandardCharsets.US_ASCII), 0, text1.length());
        expectedChecksum.update(text2.getBytes(StandardCharsets.US_ASCII), 0, text2.length());
        final long expectedValue = expectedChecksum.getValue();

        // compute the checksum of the file
        final Checksum testChecksum = new CRC32();
        FileUtils.checksum(file1, testChecksum);
        FileUtils.checksum(file2, testChecksum);
        final long resultValue = testChecksum.getValue();

        assertEquals(expectedValue, resultValue);
    }

    @Test
    public void testChecksumOnDirectory() throws Exception {
        try {
            FileUtils.checksum(new File("."), new CRC32());
            fail();
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testChecksumOnNullChecksum() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(temporaryFolder, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");
        try {
            FileUtils.checksum(file, null);
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testChecksumOnNullFile() throws Exception {
        try {
            FileUtils.checksum(null, new CRC32());
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
    }

    // Compare sizes of a directory tree using long and BigInteger methods
    @Test
    public void testCompareSizeOf() {
        final File start = new File("src/test/java");
        final long sizeLong1 = FileUtils.sizeOf(start);
        final BigInteger sizeBig = FileUtils.sizeOfAsBigInteger(start);
        final long sizeLong2 = FileUtils.sizeOf(start);
        assertEquals(sizeLong1, sizeLong2, "Size should not change");
        assertEquals(sizeLong1, sizeBig.longValue(), "longSize should equal BigSize");
    }

    // toFiles

    @Test
    public void testContentEquals() throws Exception {
        // Non-existent files
        final File file = new File(temporaryFolder, getName());
        final File file2 = new File(temporaryFolder, getName() + "2");
        assertTrue(FileUtils.contentEquals(null, null));
        assertFalse(FileUtils.contentEquals(null, file));
        assertFalse(FileUtils.contentEquals(file, null));
        // both don't  exist
        assertTrue(FileUtils.contentEquals(file, file));
        assertTrue(FileUtils.contentEquals(file, file2));
        assertTrue(FileUtils.contentEquals(file2, file2));
        assertTrue(FileUtils.contentEquals(file2, file));

        // Directories
        assertThrows(IllegalArgumentException.class, () -> FileUtils.contentEquals(temporaryFolder, temporaryFolder));

        // Different files
        final File objFile1 =
                new File(temporaryFolder, getName() + ".object");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(
                getClass().getResource("/java/lang/Object.class"),
                objFile1);

        final File objFile1b =
                new File(temporaryFolder, getName() + ".object2");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(
                getClass().getResource("/java/lang/Object.class"),
                objFile1b);

        final File objFile2 =
                new File(temporaryFolder, getName() + ".collection");
        objFile2.deleteOnExit();
        FileUtils.copyURLToFile(
                getClass().getResource("/java/util/Collection.class"),
                objFile2);

        assertFalse(FileUtils.contentEquals(objFile1, objFile2));
        assertFalse(FileUtils.contentEquals(objFile1b, objFile2));
        assertTrue(FileUtils.contentEquals(objFile1, objFile1b));

        assertTrue(FileUtils.contentEquals(objFile1, objFile1));
        assertTrue(FileUtils.contentEquals(objFile1b, objFile1b));
        assertTrue(FileUtils.contentEquals(objFile2, objFile2));

        // Equal files
        file.createNewFile();
        file2.createNewFile();
        assertTrue(FileUtils.contentEquals(file, file));
        assertTrue(FileUtils.contentEquals(file, file2));
    }

    @Test
    public void testContentEqualsIgnoreEOL() throws Exception {
        // Non-existent files
        final File file1 = new File(temporaryFolder, getName());
        final File file2 = new File(temporaryFolder, getName() + "2");
        assertTrue(FileUtils.contentEqualsIgnoreEOL(null, null, null));
        assertFalse(FileUtils.contentEqualsIgnoreEOL(null, file1, null));
        assertFalse(FileUtils.contentEqualsIgnoreEOL(file1, null, null));
        // both don't  exist
        assertTrue(FileUtils.contentEqualsIgnoreEOL(file1, file1, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(file1, file2, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(file2, file2, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(file2, file1, null));

        // Directories
        assertThrows(IllegalArgumentException.class,
            () -> FileUtils.contentEqualsIgnoreEOL(temporaryFolder, temporaryFolder, null));

        // Different files
        final File tfile1 = new File(temporaryFolder, getName() + ".txt1");
        tfile1.deleteOnExit();
        FileUtils.write(tfile1, "123\r");

        final File tfile2 = new File(temporaryFolder, getName() + ".txt2");
        tfile1.deleteOnExit();
        FileUtils.write(tfile2, "123\n");

        final File tfile3 = new File(temporaryFolder, getName() + ".collection");
        tfile3.deleteOnExit();
        FileUtils.write(tfile3, "123\r\n2");

        assertTrue(FileUtils.contentEqualsIgnoreEOL(tfile1, tfile1, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(tfile2, tfile2, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(tfile3, tfile3, null));

        assertTrue(FileUtils.contentEqualsIgnoreEOL(tfile1, tfile2, null));
        assertFalse(FileUtils.contentEqualsIgnoreEOL(tfile1, tfile3, null));
        assertFalse(FileUtils.contentEqualsIgnoreEOL(tfile2, tfile3, null));

        final URL urlCR = getClass().getResource("FileUtilsTestDataCR.dat");
        assertNotNull(urlCR);
        final File cr = new File(urlCR.toURI());
        assertTrue(cr.exists());

        final URL urlCRLF = getClass().getResource("FileUtilsTestDataCRLF.dat");
        assertNotNull(urlCRLF);
        final File crlf = new File(urlCRLF.toURI());
        assertTrue(crlf.exists());

        final URL urlLF = getClass().getResource("FileUtilsTestDataLF.dat");
        assertNotNull(urlLF);
        final File lf = new File(urlLF.toURI());
        assertTrue(lf.exists());

        assertTrue(FileUtils.contentEqualsIgnoreEOL(cr, cr, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(crlf, crlf, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(lf, lf, null));

        assertTrue(FileUtils.contentEqualsIgnoreEOL(cr, crlf, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(cr, lf, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(crlf, lf, null));

        // Check the files behave OK when EOL is not ignored
        assertTrue(FileUtils.contentEquals(cr, cr));
        assertTrue(FileUtils.contentEquals(crlf, crlf));
        assertTrue(FileUtils.contentEquals(lf, lf));

        assertFalse(FileUtils.contentEquals(cr, crlf));
        assertFalse(FileUtils.contentEquals(cr, lf));
        assertFalse(FileUtils.contentEquals(crlf, lf));

        // Equal files
        file1.createNewFile();
        file2.createNewFile();
        assertTrue(FileUtils.contentEqualsIgnoreEOL(file1, file1, null));
        assertTrue(FileUtils.contentEqualsIgnoreEOL(file1, file2, null));
    }

    @Test
    public void testCopyDirectoryExceptions() throws Exception {
        //
        // NullPointerException
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, null));
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, testFile1));
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(testFile1, null));
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, new File("a")));
        //
        // IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(testFile1, new File("a")));
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(testFile1, new File("a")));
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(temporaryFolder, temporaryFolder));
        //
        // IOException
        assertThrows(IOException.class, () -> FileUtils.copyDirectory(new File("doesnt-exist"), new File("a")));
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(temporaryFolder, testFile1));
    }

    @Test
    public void testCopyDirectoryFiltered() throws Exception {
        final File grandParentDir = new File(temporaryFolder, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        final NameFileFilter filter = new NameFileFilter(new String[]{"parent", "child", "file3.txt"});
        final File destDir = new File(temporaryFolder, "copydest");

        FileUtils.copyDirectory(grandParentDir, destDir, filter);
        final List<File> files = LIST_WALKER.list(destDir);
        assertEquals(3, files.size());
        assertEquals("parent", files.get(0).getName());
        assertEquals("child", files.get(1).getName());
        assertEquals("file3.txt", files.get(2).getName());
    }

    @Test
    public void testCopyDirectoryPreserveDates() throws Exception {
        final File source = new File(temporaryFolder, "source");
        final File sourceDirectory = new File(source, "directory");
        final File sourceFile = new File(sourceDirectory, "hello.txt");

        // Prepare source data
        source.mkdirs();
        sourceDirectory.mkdir();
        FileUtils.writeStringToFile(sourceFile, "HELLO WORLD", "UTF8");
        // Set dates in reverse order to avoid overwriting previous values
        // Also, use full seconds (arguments are in ms) close to today
        // but still highly unlikely to occur in the real world
        assertTrue(setLastModifiedMillis(sourceFile, DATE3));
        assertTrue(setLastModifiedMillis(sourceDirectory, DATE2));
        assertTrue(setLastModifiedMillis(source, DATE1));

        final File target = new File(temporaryFolder, "target");
        final File targetDirectory = new File(target, "directory");
        final File targetFile = new File(targetDirectory, "hello.txt");

        // Test with preserveFileDate disabled
        // On Windows, the last modified time is copied by default.
        FileUtils.copyDirectory(source, target, false);
        assertNotEquals(DATE1, getLastModifiedMillis(target));
        assertNotEquals(DATE2, getLastModifiedMillis(targetDirectory));
        if (!SystemUtils.IS_OS_WINDOWS) {
            assertNotEquals(DATE3, getLastModifiedMillis(targetFile));
        }
        FileUtils.deleteDirectory(target);

        // Test with preserveFileDate enabled
        FileUtils.copyDirectory(source, target, true);
        assertEquals(DATE1, getLastModifiedMillis(target));
        assertEquals(DATE2, getLastModifiedMillis(targetDirectory));
        assertEquals(DATE3, getLastModifiedMillis(targetFile));
        FileUtils.deleteDirectory(target);

        // also if the target directory already exists (IO-190)
        target.mkdirs();
        FileUtils.copyDirectory(source, target, true);
        assertEquals(DATE1, getLastModifiedMillis(target));
        assertEquals(DATE2, getLastModifiedMillis(targetDirectory));
        assertEquals(DATE3, getLastModifiedMillis(targetFile));
        FileUtils.deleteDirectory(target);

        // also if the target subdirectory already exists (IO-190)
        targetDirectory.mkdirs();
        FileUtils.copyDirectory(source, target, true);
        assertEquals(DATE1, getLastModifiedMillis(target));
        assertEquals(DATE2, getLastModifiedMillis(targetDirectory));
        assertEquals(DATE3, getLastModifiedMillis(targetFile));
        FileUtils.deleteDirectory(target);
    }

    // toURLs

    /* Test for IO-141 */
    @Test
    public void testCopyDirectoryToChild() throws Exception {
        final File grandParentDir = new File(temporaryFolder, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        final long expectedCount = LIST_WALKER.list(grandParentDir).size() +
                LIST_WALKER.list(parentDir).size();
        final long expectedSize = FileUtils.sizeOfDirectory(grandParentDir) +
                FileUtils.sizeOfDirectory(parentDir);
        FileUtils.copyDirectory(parentDir, childDir);
        assertEquals(expectedCount, LIST_WALKER.list(grandParentDir).size());
        assertEquals(expectedSize, FileUtils.sizeOfDirectory(grandParentDir));
        assertTrue(expectedCount > 0, "Count > 0");
        assertTrue(expectedSize > 0, "Size > 0");
    }

//   @Test public void testToURLs2() throws Exception {
//        File[] files = new File[] {
//            new File(temporaryFolder, "file1.txt"),
//            null,
//        };
//        URL[] urls = FileUtils.toURLs(files);
//
//        assertEquals(files.length, urls.length);
//        assertTrue(urls[0].toExternalForm().startsWith("file:"));
//        assertTrue(urls[0].toExternalForm().indexOf("file1.txt") > 0);
//        assertEquals(null, urls[1]);
//    }
//
//   @Test public void testToURLs3() throws Exception {
//        File[] files = null;
//        URL[] urls = FileUtils.toURLs(files);
//
//        assertEquals(0, urls.length);
//    }

    @Test
    public void testCopyDirectoryToDirectory_NonExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 1234);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 4321);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        final File actualDestDir = new File(destDir, srcDir.getName());

        FileUtils.copyDirectoryToDirectory(srcDir, destDir);

        assertTrue(destDir.exists(), "Check exists");
        assertTrue(actualDestDir.exists(), "Check exists");
        final long srcSize = FileUtils.sizeOfDirectory(srcDir);
        assertTrue(srcSize > 0, "Size > 0");
        assertEquals(srcSize, FileUtils.sizeOfDirectory(actualDestDir), "Check size");
        assertTrue(new File(actualDestDir, "sub/A.txt").exists());
        FileUtils.deleteDirectory(destDir);
    }

    // contentEquals

    @Test
    public void testCopyDirectoryToExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 1234);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 4321);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        destDir.mkdirs();

        FileUtils.copyDirectory(srcDir, destDir);

        final long srcSize = FileUtils.sizeOfDirectory(srcDir);
        assertTrue(srcSize > 0, "Size > 0");
        assertEquals(srcSize, FileUtils.sizeOfDirectory(destDir));
        assertTrue(new File(destDir, "sub/A.txt").exists());
    }

    /* Test for IO-141 */
    @Test
    public void testCopyDirectoryToGrandChild() throws Exception {
        final File grandParentDir = new File(temporaryFolder, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        final long expectedCount = LIST_WALKER.list(grandParentDir).size() * 2;
        final long expectedSize = FileUtils.sizeOfDirectory(grandParentDir) * 2;
        FileUtils.copyDirectory(grandParentDir, childDir);
        assertEquals(expectedCount, LIST_WALKER.list(grandParentDir).size());
        assertEquals(expectedSize, FileUtils.sizeOfDirectory(grandParentDir));
        assertTrue(expectedSize > 0, "Size > 0");
    }

    // copyURLToFile

    /* Test for IO-217 FileUtils.copyDirectoryToDirectory makes infinite loops */
    @Test
    public void testCopyDirectoryToItself() throws Exception {
        final File dir = new File(temporaryFolder, "itself");
        dir.mkdirs();
        FileUtils.copyDirectoryToDirectory(dir, dir);
        assertEquals(1, LIST_WALKER.list(dir).size());
    }

    @Test
    public void testCopyDirectoryToNonExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 1234);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 4321);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        assertTrue(destDir.exists(), "Check exists");
        final long sizeOfSrcDirectory = FileUtils.sizeOfDirectory(srcDir);
        assertTrue(sizeOfSrcDirectory > 0, "Size > 0");
        assertEquals(sizeOfSrcDirectory, FileUtils.sizeOfDirectory(destDir), "Check size");
        assertTrue(new File(destDir, "sub/A.txt").exists());
        FileUtils.deleteDirectory(destDir);
    }

    // forceMkdir

    @Test
    public void testCopyFile1() throws Exception {
        final File destination = new File(temporaryFolder, "copy1.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        FileUtils.copyFile(testFile1, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile1Size, destination.length(), "Check Full copy");
        assertEquals(getLastModifiedMillis(testFile1), getLastModifiedMillis(destination), "Check last modified date preserved");
    }

    @Test
    public void testCopyFile1ToDir() throws Exception {
        final File directory = new File(temporaryFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        backDateFile10Minutes(testFile1);

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile1Size, destination.length(), "Check Full copy");
        assertEquals(testFile1.lastModified(), destination.lastModified(), "Check last modified date preserved");

        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyFileToDirectory(destination, directory),
            "Should not be able to copy a file into the same directory as itself");
    }

    // sizeOfDirectory

    @Test
    public void testCopyFile2() throws Exception {
        final File destination = new File(temporaryFolder, "copy2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        FileUtils.copyFile(testFile1, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile2Size, destination.length(), "Check Full copy");
        assertEquals(getLastModifiedMillis(testFile1) , getLastModifiedMillis(destination), "Check last modified date preserved");
    }

    @Test
    public void testCopyFile2ToDir() throws Exception {
        final File directory = new File(temporaryFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        backDateFile10Minutes(testFile1);

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile2Size, destination.length(), "Check Full copy");
        assertEquals(testFile1.lastModified(), destination.lastModified(), "Check last modified date preserved");
    }

    @Test
    public void testCopyFile2WithoutFileDatePreservation() throws Exception {
        final File destFile = new File(temporaryFolder, "copy2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        // destination file time should not be less than this (allowing for granularity)
        final long now = System.currentTimeMillis() - 1000L;
        // On Windows, the last modified time is copied by default.
        FileUtils.copyFile(testFile1, destFile, false);
        assertTrue(destFile.exists(), "Check Exist");
        assertEquals(testFile1Size, destFile.length(), "Check Full copy");
        final long destLastMod = getLastModifiedMillis(destFile);
        final long unexpected = getLastModifiedMillis(testFile1);
        if (!SystemUtils.IS_OS_WINDOWS) {
            final long delta = destLastMod - unexpected;
            assertNotEquals(unexpected, destLastMod, "Check last modified date not same as input, delta " + delta);
            assertTrue(destLastMod > now, destLastMod + " > " + now + " (delta " + delta + ")");
        }
    }

    @Test
    @Disabled
    public void testCopyFileLarge() throws Exception {

        final File largeFile = new File(temporaryFolder, "large.txt");
        final File destination = new File(temporaryFolder, "copylarge.txt");

        System.out.println("START:   " + new java.util.Date());
        if (!largeFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + largeFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(largeFile));
        try {
            TestUtils.generateTestData(output, FileUtils.ONE_GB);
        } finally {
            IOUtils.closeQuietly(output);
        }
        System.out.println("CREATED: " + new java.util.Date());
        FileUtils.copyFile(largeFile, destination);
        System.out.println("COPIED:  " + new java.util.Date());

        assertTrue(destination.exists(), "Check Exist");
        assertEquals(largeFile.length(), destination.length(), "Check Full copy");
    }

    @Test
    public void testCopyFileToOutputStream() throws Exception {
        final ByteArrayOutputStream destination = new ByteArrayOutputStream();
        FileUtils.copyFile(testFile1, destination);
        assertEquals(testFile1Size, destination.size(), "Check Full copy size");
        final byte[] expected = FileUtils.readFileToByteArray(testFile1);
        assertArrayEquals(expected, destination.toByteArray(), "Check Full copy");
    }

    @Test
    public void testCopyToDirectoryWithDirectory() throws IOException {
        final File destDirectory = new File(temporaryFolder, "destination");
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }

        // Create a test directory
        final File inputDirectory = new File(temporaryFolder, "input");
        if (!inputDirectory.exists()) {
            inputDirectory.mkdirs();
        }
        final File outputDirDestination = new File(destDirectory, inputDirectory.getName());
        FileUtils.copyToDirectory(testFile1, inputDirectory);
        final File destFile1 = new File(outputDirDestination, testFile1.getName());
        FileUtils.copyToDirectory(testFile2, inputDirectory);
        final File destFile2 = new File(outputDirDestination, testFile2.getName());

        FileUtils.copyToDirectory(inputDirectory, destDirectory);

        // Check the directory was created
        assertTrue(outputDirDestination.exists(), "Check Exists");
        assertTrue(outputDirDestination.isDirectory(), "Check Directory");

        // Check each file
        assertTrue(destFile1.exists(), "Check Exists");
        assertEquals(testFile1Size, destFile1.length(), "Check Full Copy");
        assertTrue(destFile2.exists(), "Check Exists");
        assertEquals(testFile2Size, destFile2.length(), "Check Full Copy");
    }

    @Test
    public void testCopyToDirectoryWithFile() throws IOException {
        final File directory = new File(temporaryFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        FileUtils.copyToDirectory(testFile1, directory);
        assertTrue(destination.exists(), "Check Exists");
        assertEquals(testFile1Size, destination.length(), "Check Full Copy");
    }

    // copyFile

    @Test
    public void testCopyToDirectoryWithFileSourceDoesNotExist() {
        assertThrows(IOException.class,
                () -> FileUtils.copyToDirectory(new File(temporaryFolder, "doesNotExists"), temporaryFolder));
    }

    @Test
    public void testCopyToDirectoryWithFileSourceIsNull() {
        assertThrows(NullPointerException.class, () -> FileUtils.copyToDirectory((File) null, temporaryFolder));
    }

    @Test
    public void testCopyToDirectoryWithIterable() throws IOException {
        final File directory = new File(temporaryFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        final List<File> input = new ArrayList<>();
        input.add(testFile1);
        input.add(testFile2);

        final File destFile1 = new File(directory, testFile1.getName());
        final File destFile2 = new File(directory, testFile2.getName());

        FileUtils.copyToDirectory(input, directory);
        // Check each file
        assertTrue(destFile1.exists(), "Check Exists");
        assertEquals(testFile1Size, destFile1.length(), "Check Full Copy");
        assertTrue(destFile2.exists(), "Check Exists");
        assertEquals(testFile2Size, destFile2.length(), "Check Full Copy");
    }

    @Test
    public void testCopyToDirectoryWithIterableSourceDoesNotExist() {
        assertThrows(IOException.class,
                () -> FileUtils.copyToDirectory(Collections.singleton(new File(temporaryFolder, "doesNotExists")),
                        temporaryFolder));
    }

    @Test
    public void testCopyToDirectoryWithIterableSourceIsNull() {
        assertThrows(NullPointerException.class, () -> FileUtils.copyToDirectory((List<File>) null, temporaryFolder));
    }

    @Test
    public void testCopyToSelf() throws Exception {
        final File destination = new File(temporaryFolder, "copy3.txt");
        //Prepare a test file
        FileUtils.copyFile(testFile1, destination);
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyFile(destination, destination));
    }

    @Test
    public void testCopyURLToFile() throws Exception {
        // Creates file
        final File file = new File(temporaryFolder, getName());
        file.deleteOnExit();

        // Loads resource
        final String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile(getClass().getResource(resourceName), file);

        // Tests that resuorce was copied correctly
        try (FileInputStream fis = new FileInputStream(file)) {
            assertTrue(IOUtils.contentEquals(getClass().getResourceAsStream(resourceName), fis),
                    "Content is not equal.");
        }
        //TODO Maybe test copy to itself like for copyFile()
    }

    @Test
    public void testCopyURLToFileWithTimeout() throws Exception {
        // Creates file
        final File file = new File(temporaryFolder, "testCopyURLToFileWithTimeout");
        file.deleteOnExit();

        // Loads resource
        final String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile(getClass().getResource(resourceName), file, 500, 500);

        // Tests that resuorce was copied correctly
        try (FileInputStream fis = new FileInputStream(file)) {
            assertTrue(IOUtils.contentEquals(getClass().getResourceAsStream(resourceName), fis),
                    "Content is not equal.");
        }
        //TODO Maybe test copy to itself like for copyFile()
    }

    @Test
    public void testDecodeUrl() {
        assertEquals("", FileUtils.decodeUrl(""));
        assertEquals("foo", FileUtils.decodeUrl("foo"));
        assertEquals("+", FileUtils.decodeUrl("+"));
        assertEquals("% ", FileUtils.decodeUrl("%25%20"));
        assertEquals("%20", FileUtils.decodeUrl("%2520"));
        assertEquals("jar:file:/C:/dir/sub dir/1.0/foo-1.0.jar!/org/Bar.class", FileUtils
                .decodeUrl("jar:file:/C:/dir/sub%20dir/1.0/foo-1.0.jar!/org/Bar.class"));
    }

    @Test
    public void testDecodeUrlEncodingUtf8() {
        assertEquals("\u00E4\u00F6\u00FC\u00DF", FileUtils.decodeUrl("%C3%A4%C3%B6%C3%BC%C3%9F"));
    }

    @Test
    public void testDecodeUrlLenient() {
        assertEquals(" ", FileUtils.decodeUrl(" "));
        assertEquals("\u00E4\u00F6\u00FC\u00DF", FileUtils.decodeUrl("\u00E4\u00F6\u00FC\u00DF"));
        assertEquals("%", FileUtils.decodeUrl("%"));
        assertEquals("% ", FileUtils.decodeUrl("%%20"));
        assertEquals("%2", FileUtils.decodeUrl("%2"));
        assertEquals("%2G", FileUtils.decodeUrl("%2G"));
    }

    @Test
    public void testDecodeUrlNullSafe() {
        assertNull(FileUtils.decodeUrl(null));
    }

    @Test
    public void testDeleteDirectoryWithNonDirectory() throws Exception {
        try {
            FileUtils.deleteDirectory(testFile1);
            fail();
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testDelete() throws Exception {
        assertEquals(testFile1, FileUtils.delete(testFile1));
        assertThrows(IOException.class, () -> FileUtils.delete(new File("does not exist.nope")));
    }

    @Test
    public void testDeleteQuietlyDir() throws IOException {
        final File testDirectory = new File(temporaryFolder, "testDeleteQuietlyDir");
        final File testFile = new File(testDirectory, "testDeleteQuietlyFile");
        testDirectory.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }

        assertTrue(testDirectory.exists());
        assertTrue(testFile.exists());
        FileUtils.deleteQuietly(testDirectory);
        assertFalse(testDirectory.exists(), "Check No Exist");
        assertFalse(testFile.exists(), "Check No Exist");
    }

    @Test
    public void testDeleteQuietlyFile() throws IOException {
        final File testFile = new File(temporaryFolder, "testDeleteQuietlyFile");
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }

        assertTrue(testFile.exists());
        FileUtils.deleteQuietly(testFile);
        assertFalse(testFile.exists(), "Check No Exist");
    }

    @Test
    public void testDeleteQuietlyForNull() {
        try {
            FileUtils.deleteQuietly(null);
        } catch (final Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testDeleteQuietlyNonExistent() {
        final File testFile = new File("testDeleteQuietlyNonExistent");
        assertFalse(testFile.exists());

        try {
            FileUtils.deleteQuietly(testFile);
        } catch (final Exception ex) {
            fail(ex.getMessage());
        }
    }

    // copyToDirectory

    /*
     *  Test the FileUtils implementation.
     */
    @Test
    public void testFileUtils() throws Exception {
        // Loads file from classpath
        final File file1 = new File(temporaryFolder, "test.txt");
        final String filename = file1.getAbsolutePath();

        //Create test file on-the-fly (used to be in CVS)
        try (OutputStream out = new FileOutputStream(file1)) {
            out.write("This is a test".getBytes(StandardCharsets.UTF_8));
        }

        final File file2 = new File(temporaryFolder, "test2.txt");

        FileUtils.writeStringToFile(file2, filename, "UTF-8");
        assertTrue(file2.exists());
        assertTrue(file2.length() > 0);

        final String file2contents = FileUtils.readFileToString(file2, "UTF-8");
        assertEquals(filename, file2contents, "Second file's contents correct");

        assertTrue(file2.delete());

        final String contents = FileUtils.readFileToString(new File(filename), "UTF-8");
        assertEquals("This is a test", contents, "FileUtils.fileRead()");

    }

    @Test
    public void testForceDeleteAFile1() throws Exception {
        final File destination = new File(temporaryFolder, "copy1.txt");
        destination.createNewFile();
        assertTrue(destination.exists(), "Copy1.txt doesn't exist to delete");
        FileUtils.forceDelete(destination);
        assertTrue(!destination.exists(), "Check No Exist");
    }

    @Test
    public void testForceDeleteAFile2() throws Exception {
        final File destination = new File(temporaryFolder, "copy2.txt");
        destination.createNewFile();
        assertTrue(destination.exists(), "Copy2.txt doesn't exist to delete");
        FileUtils.forceDelete(destination);
        assertTrue(!destination.exists(), "Check No Exist");
    }

    @Test
    public void testForceDeleteAFile3() throws Exception {
        final File destination = new File(temporaryFolder, "no_such_file");
        assertTrue(!destination.exists(), "Check No Exist");
        try {
            FileUtils.forceDelete(destination);
            fail("Should generate FileNotFoundException");
        } catch (final FileNotFoundException ignored) {
        }
    }

    @Test
    public void testForceDeleteDir() throws Exception {
        final File testDirectory = temporaryFolder;
        assertTrue(testDirectory.exists(), "TestDirectory must exist");
        FileUtils.forceDelete(testDirectory);
        assertFalse(testDirectory.exists(), "TestDirectory must not exist");
    }

    @Test
    public void testForceDeleteReadOnlyFile() throws Exception {
        File destination = File.createTempFile("test-", ".txt");
        assertTrue(destination.setReadOnly());
        assertTrue(destination.canRead());
        assertFalse(destination.canWrite());
        // sanity check that File.delete() in deletes read-only files.
        assertTrue(destination.delete());
        destination = File.createTempFile("test-", ".txt");
        // real test
        assertTrue(destination.setReadOnly());
        assertTrue(destination.canRead());
        assertFalse(destination.canWrite());
        assertTrue(destination.exists(), "File doesn't exist to delete");
        FileUtils.forceDelete(destination);
        assertTrue(!destination.exists(), "Check deletion");
    }

    @Test
    public void testForceMkdir() throws Exception {
        // Tests with existing directory
        FileUtils.forceMkdir(temporaryFolder);

        // Creates test file
        final File testFile = new File(temporaryFolder, getName());
        testFile.deleteOnExit();
        testFile.createNewFile();
        assertTrue(testFile.exists(), "Test file does not exist.");

        // Tests with existing file
        assertThrows(IOException.class, () -> FileUtils.forceMkdir(testFile));

        testFile.delete();

        // Tests with non-existent directory
        FileUtils.forceMkdir(testFile);
        assertTrue(testFile.exists(), "Directory was not created.");
    }

    @Test
    public void testForceMkdirParent() throws Exception {
        // Tests with existing directory
        assertTrue(temporaryFolder.exists());
        final File testParentDir = new File(temporaryFolder, "testForceMkdirParent");
        testParentDir.delete();
        assertFalse(testParentDir.exists());
        final File testFile = new File(testParentDir, "test.txt");
        assertFalse(testParentDir.exists());
        assertFalse(testFile.exists());
        // Create
        FileUtils.forceMkdirParent(testFile);
        assertTrue(testParentDir.exists());
        assertFalse(testFile.exists());
        // Again
        FileUtils.forceMkdirParent(testFile);
        assertTrue(testParentDir.exists());
        assertFalse(testFile.exists());
    }

    // forceDelete

    //-----------------------------------------------------------------------
    @Test
    public void testGetFile() {
        final File expected_A = new File("src");
        final File expected_B = new File(expected_A, "main");
        final File expected_C = new File(expected_B, "java");
        assertEquals(expected_A, FileUtils.getFile("src"), "A");
        assertEquals(expected_B, FileUtils.getFile("src", "main"), "B");
        assertEquals(expected_C, FileUtils.getFile("src", "main", "java"), "C");
        try {
            FileUtils.getFile((String[]) null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetFile_Parent() {
        final File parent = new File("parent");
        final File expected_A = new File(parent, "src");
        final File expected_B = new File(expected_A, "main");
        final File expected_C = new File(expected_B, "java");
        assertEquals(expected_A, FileUtils.getFile(parent, "src"), "A");
        assertEquals(expected_B, FileUtils.getFile(parent, "src", "main"), "B");
        assertEquals(expected_C, FileUtils.getFile(parent, "src", "main", "java"), "C");
        try {
            FileUtils.getFile(parent, (String[]) null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException e) {
            // expected
        }
        try {
            FileUtils.getFile((File) null, "src");
            fail("Expected NullPointerException");
        } catch (final NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetTempDirectory() {
        final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        assertEquals(tempDirectory, FileUtils.getTempDirectory());
    }

    // copyFileToDirectory

    @Test
    public void testGetTempDirectoryPath() {
        assertEquals(System.getProperty("java.io.tmpdir"),
                FileUtils.getTempDirectoryPath());
    }

    @Test
    public void testGetUserDirectory() {
        final File userDirectory = new File(System.getProperty("user.home"));
        assertEquals(userDirectory, FileUtils.getUserDirectory());
    }

    // forceDelete

    @Test
    public void testGetUserDirectoryPath() {
        assertEquals(System.getProperty("user.home"),
                FileUtils.getUserDirectoryPath());
    }

    // This test relies on FileUtils.copyFile using File.length to check the output size
    @Test
    public void testIncorrectOutputSize() throws Exception {
        final File inFile = new File("pom.xml");
        final File outFile = new ShorterFile("target/pom.tmp"); // it will report a shorter file
        try {
            FileUtils.copyFile(inFile, outFile);
            fail("Expected IOException");
        } catch (final Exception e) {
            final String msg = e.toString();
            assertTrue(msg.contains("Failed to copy full contents"), msg);
        } finally {
            outFile.delete(); // tidy up
        }
    }

    @Test
    public void testIO276() throws Exception {
        final File dir = new File("target", "IO276");
        assertTrue(dir.mkdirs(), dir + " should not be present");
        final File file = new File(dir, "IO276.txt");
        assertTrue(file.createNewFile(), file + " should not be present");
        FileUtils.forceDeleteOnExit(dir);
        // If this does not work, test will fail next time (assuming target is not cleaned)
    }

    @Test
    public void testIO300() throws Exception {
        final File testDirectory = temporaryFolder;
        final File src = new File(testDirectory, "dir1");
        final File dest = new File(src, "dir2");
        assertTrue(dest.mkdirs());
        assertTrue(src.exists());
        try {
            FileUtils.moveDirectoryToDirectory(src, dest, false);
            fail("expected IOException");
        } catch (final IOException ioe) {
            // expected
        }
        assertTrue(src.exists());
    }

    // isFileNewer / isFileOlder
    @Test
    public void testIsFileNewerOlder() throws Exception {
        final File reference = new File(temporaryFolder, "FileUtils-reference.txt");
        final File oldFile = new File(temporaryFolder, "FileUtils-old.txt");
        final File newFile = new File(temporaryFolder, "FileUtils-new.txt");
        final File invalidFile = new File(temporaryFolder, "FileUtils-invalid-file.txt");

        // Create Files
        if (!oldFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + oldFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(oldFile));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }

        do {
            try {
                TestUtils.sleep(1000);
            } catch (final InterruptedException ie) {
                // ignore
            }
            if (!reference.getParentFile().exists()) {
                throw new IOException("Cannot create file " + reference
                        + " as the parent directory does not exist");
            }
            final BufferedOutputStream output =
                    new BufferedOutputStream(new FileOutputStream(reference));
            try {
                TestUtils.generateTestData(output, 0);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } while (getLastModifiedMillis(oldFile) == getLastModifiedMillis(reference));

        final Date date = new Date();
        final long now = date.getTime();
        final Instant instant = date.toInstant();
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        final LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        final LocalDate localDate = zonedDateTime.toLocalDate();
        final LocalDate localDatePlusDay = localDate.plusDays(1);
        final LocalTime localTime = LocalTime.ofSecondOfDay(0);

        do {
            try {
                TestUtils.sleep(1000);
            } catch (final InterruptedException ie) {
                // ignore
            }
            if (!newFile.getParentFile().exists()) {
                throw new IOException("Cannot create file " + newFile
                        + " as the parent directory does not exist");
            }
            final BufferedOutputStream output =
                    new BufferedOutputStream(new FileOutputStream(newFile));
            try {
                TestUtils.generateTestData(output, 0);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } while (getLastModifiedMillis(reference) == getLastModifiedMillis(newFile));

        // Test isFileNewer()
        assertFalse(FileUtils.isFileNewer(oldFile, reference), "Old File - Newer - File");
        assertFalse(FileUtils.isFileNewer(oldFile, date), "Old File - Newer - Date");
        assertFalse(FileUtils.isFileNewer(oldFile, now), "Old File - Newer - Mili");
        assertFalse(FileUtils.isFileNewer(oldFile, instant), "Old File - Newer - Instant");
        assertFalse(FileUtils.isFileNewer(oldFile, zonedDateTime), "Old File - Newer - ZonedDateTime");
        assertFalse(FileUtils.isFileNewer(oldFile, localDateTime), "Old File - Newer - LocalDateTime");
        assertFalse(FileUtils.isFileNewer(oldFile, localDateTime, ZoneId.systemDefault()), "Old File - Newer - LocalDateTime,ZoneId");
        assertFalse(FileUtils.isFileNewer(oldFile, localDate), "Old File - Newer - LocalDate");
        assertTrue(FileUtils.isFileNewer(oldFile, localDate, localTime), "Old File - Newer - LocalDate,ZoneId");
        assertFalse(FileUtils.isFileNewer(oldFile, localDatePlusDay), "Old File - Newer - LocalDate plus one day");
        assertFalse(FileUtils.isFileNewer(oldFile, localDatePlusDay, localTime), "Old File - Newer - LocalDate plus one day,ZoneId");

        assertTrue(FileUtils.isFileNewer(newFile, reference), "New File - Newer - File");
        assertTrue(FileUtils.isFileNewer(newFile, date), "New File - Newer - Date");
        assertTrue(FileUtils.isFileNewer(newFile, now), "New File - Newer - Mili");
        assertTrue(FileUtils.isFileNewer(newFile, instant), "New File - Newer - Instant");
        assertTrue(FileUtils.isFileNewer(newFile, zonedDateTime), "New File - Newer - ZonedDateTime");
        assertTrue(FileUtils.isFileNewer(newFile, localDateTime), "New File - Newer - LocalDateTime");
        assertTrue(FileUtils.isFileNewer(newFile, localDateTime, ZoneId.systemDefault()), "New File - Newer - LocalDateTime,ZoneId");
        assertFalse(FileUtils.isFileNewer(newFile, localDate), "New File - Newer - LocalDate");
        assertTrue(FileUtils.isFileNewer(newFile, localDate, localTime), "New File - Newer - LocalDate,ZoneId");
        assertFalse(FileUtils.isFileNewer(newFile, localDatePlusDay), "New File - Newer - LocalDate plus one day");
        assertFalse(FileUtils.isFileNewer(newFile, localDatePlusDay, localTime), "New File - Newer - LocalDate plus one day,ZoneId");
        assertFalse(FileUtils.isFileNewer(invalidFile, reference), "Invalid - Newer - File");
        final String invalidFileName = invalidFile.getName();
        try {
            FileUtils.isFileNewer(newFile, invalidFile);
            fail("Should have cause IllegalArgumentException");
        } catch (final IllegalArgumentException iae) {
            final String message = iae.getMessage();
            assertTrue(message.contains(invalidFileName), "Message should contain: " + invalidFileName + " but was: " + message);
        }

        // Test isFileOlder()
        assertTrue(FileUtils.isFileOlder(oldFile, reference), "Old File - Older - File");
        assertTrue(FileUtils.isFileOlder(oldFile, date), "Old File - Older - Date");
        assertTrue(FileUtils.isFileOlder(oldFile, now), "Old File - Older - Mili");
        assertTrue(FileUtils.isFileOlder(oldFile, instant), "Old File - Older - Instant");
        assertTrue(FileUtils.isFileOlder(oldFile, zonedDateTime), "Old File - Older - ZonedDateTime");
        assertTrue(FileUtils.isFileOlder(oldFile, localDateTime), "Old File - Older - LocalDateTime");
        assertTrue(FileUtils.isFileOlder(oldFile, localDateTime, ZoneId.systemDefault()), "Old File - Older - LocalDateTime,LocalTime");
        assertTrue(FileUtils.isFileOlder(oldFile, localDate), "Old File - Older - LocalDate");
        assertFalse(FileUtils.isFileOlder(oldFile, localDate, localTime), "Old File - Older - LocalDate,ZoneId");
        assertTrue(FileUtils.isFileOlder(oldFile, localDatePlusDay), "Old File - Older - LocalDate plus one day");
        assertTrue(FileUtils.isFileOlder(oldFile, localDatePlusDay, localTime), "Old File - Older - LocalDate plus one day,LocalTime");

        assertFalse(FileUtils.isFileOlder(newFile, reference), "New File - Older - File");
        assertFalse(FileUtils.isFileOlder(newFile, date), "New File - Older - Date");
        assertFalse(FileUtils.isFileOlder(newFile, now), "New File - Older - Mili");
        assertFalse(FileUtils.isFileOlder(newFile, instant), "New File - Older - Instant");
        assertFalse(FileUtils.isFileOlder(newFile, zonedDateTime), "New File - Older - ZonedDateTime");
        assertFalse(FileUtils.isFileOlder(newFile, localDateTime), "New File - Older - LocalDateTime");
        assertFalse(FileUtils.isFileOlder(newFile, localDateTime, ZoneId.systemDefault()), "New File - Older - LocalDateTime,ZoneId");
        assertTrue(FileUtils.isFileOlder(newFile, localDate), "New File - Older - LocalDate");
        assertFalse(FileUtils.isFileOlder(newFile, localDate, localTime), "New File - Older - LocalDate,LocalTime");
        assertTrue(FileUtils.isFileOlder(newFile, localDatePlusDay), "New File - Older - LocalDate plus one day");
        assertTrue(FileUtils.isFileOlder(newFile, localDatePlusDay, localTime), "New File - Older - LocalDate plus one day,LocalTime");

        assertFalse(FileUtils.isFileOlder(invalidFile, reference), "Invalid - Older - File");
        assertThrows(IllegalArgumentException.class, () -> FileUtils.isFileOlder(newFile, invalidFile));
        try {
            FileUtils.isFileOlder(newFile, invalidFile);
            fail("Should have cause IllegalArgumentException");
        } catch (final IllegalArgumentException iae) {
            final String message = iae.getMessage();
            assertTrue(message.contains(invalidFileName), "Message should contain: " + invalidFileName + " but was: " + message);
        }


        // ----- Test isFileNewer() exceptions -----
        // Null File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(null, now));

        // Null reference File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(oldFile, (File) null));

        // Invalid reference File
        assertThrows(IllegalArgumentException.class, () -> FileUtils.isFileNewer(oldFile, invalidFile));

        // Null reference Date
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(oldFile, (Date) null));

        // ----- Test isFileOlder() exceptions -----
        // Null File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(null, now));

        // Null reference File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(oldFile, (File) null));

        // Null reference Date
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(oldFile, (Date) null));

        // Invalid reference File
        assertThrows(IllegalArgumentException.class, () -> FileUtils.isFileOlder(oldFile, invalidFile));
    }

    @Test
    public void testIterateFiles() throws Exception {
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "list_test");
        final File subSubDir = new File(subDir, "subSubDir");
        final File notSubSubDir = new File(subDir, "notSubSubDir");
        assertTrue(subDir.mkdir());
        assertTrue(subSubDir.mkdir());
        assertTrue(notSubSubDir.mkdir());
        Iterator<File> iterator = null;
        try {
            // Need list to be appendable
            final List<String> expectedFileNames = new ArrayList(
                Arrays.asList("a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt"));
            final int[] fileSizes = {123, 234, 345, 456, 678, 789};
            assertEquals(expectedFileNames.size(), fileSizes.length);
            Collections.sort(expectedFileNames);
            Arrays.sort(fileSizes);
            for (int i = 0; i < fileSizes.length; ++i) {
                TestUtils.generateTestData(new File(subDir, expectedFileNames.get(i)), fileSizes[i]);
            }
            //
            final String subSubFileName = "z.txt";
            TestUtils.generateTestData(new File(subSubDir, subSubFileName), 1);
            expectedFileNames.add(subSubFileName);
            //
            final String notSubSubFileName = "not.txt";
            TestUtils.generateTestData(new File(notSubSubDir, notSubSubFileName), 1);

            final WildcardFileFilter allFilesFileFilter = new WildcardFileFilter("*.*");
            final NameFileFilter dirFilter = new NameFileFilter("subSubDir");
            iterator = FileUtils.iterateFiles(subDir, allFilesFileFilter, dirFilter);

            final Map<String, String> matchedFileNames = new HashMap<>();
            final List<String> actualFileNames = new ArrayList<>();

            while (iterator.hasNext()) {
                boolean found = false;
                final String fileName = iterator.next().getName();
                actualFileNames.add(fileName);

                for (int j = 0; !found && j < expectedFileNames.size(); ++j) {
                    final String expectedFileName = expectedFileNames.get(j);
                    if (expectedFileName.equals(fileName)) {
                        matchedFileNames.put(expectedFileName, expectedFileName);
                        found = true;
                    }
                }
            }
            assertEquals(expectedFileNames.size(), matchedFileNames.size());
            Collections.sort(actualFileNames);
            assertEquals(expectedFileNames, actualFileNames);
        } finally {
            consumeRemaining(iterator);
            notSubSubDir.delete();
            subSubDir.delete();
            subDir.delete();
        }
    }

    @Test
    public void testIterateFilesAndDirs() throws IOException {
        final File srcDir = temporaryFolder;
        // temporaryFolder/srcDir
        // - subdir1
        // -- subdir2
        // --- a.txt
        // --- subdir3
        // --- subdir4
        final File subDir1 = new File(srcDir, "subdir1");
        final File subDir2 = new File(subDir1, "subdir2");
        final File subDir3 = new File(subDir2, "subdir3");
        final File subDir4 = new File(subDir2, "subdir4");
        assertTrue(subDir1.mkdir());
        assertTrue(subDir2.mkdir());
        assertTrue(subDir3.mkdir());
        assertTrue(subDir4.mkdir());
        final File someFile = new File(subDir2, "a.txt");
        final WildcardFileFilter fileFilterAllFiles = new WildcardFileFilter("*.*");
        final WildcardFileFilter fileFilterAllDirs = new WildcardFileFilter("*");
        final WildcardFileFilter fileFilterExtTxt = new WildcardFileFilter("*.txt");
        try {
            try (final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(someFile))) {
                TestUtils.generateTestData(output, 100);
            }
            //
            // "*.*" and "*"
            Collection<File> expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile, subDir3, subDir4);
            iterateFilesAndDirs(subDir1, fileFilterAllFiles, fileFilterAllDirs, expectedFilesAndDirs);
            //
            // "*.txt" and "*"
            final int filesCount;
            expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile, subDir3, subDir4);
            iterateFilesAndDirs(subDir1, fileFilterExtTxt, fileFilterAllDirs, expectedFilesAndDirs);
            //
            // "*.*" and "subdir2"
            expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile);
            iterateFilesAndDirs(subDir1, fileFilterAllFiles, new NameFileFilter("subdir2"), expectedFilesAndDirs);
            //
            // "*.txt" and "subdir2"
            expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile);
            iterateFilesAndDirs(subDir1, fileFilterExtTxt, new NameFileFilter("subdir2"), expectedFilesAndDirs);
        } finally {
            someFile.delete();
            subDir4.delete();
            subDir3.delete();
            subDir2.delete();
            subDir1.delete();
        }
    }

    @Test
    public void testListFiles() throws Exception {
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "list_test");
        final File subDir2 = new File(subDir, "subdir");
        subDir.mkdir();
        subDir2.mkdir();
        try {

            final String[] expectedFileNames = {"a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt"};
            final int[] fileSizes = {123, 234, 345, 456, 678, 789};

            for (int i = 0; i < expectedFileNames.length; ++i) {
                final File theFile = new File(subDir, expectedFileNames[i]);
                if (!theFile.getParentFile().exists()) {
                    throw new IOException("Cannot create file " + theFile + " as the parent directory does not exist");
                }
                final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(theFile));
                try {
                    TestUtils.generateTestData(output, fileSizes[i]);
                } finally {
                    IOUtils.closeQuietly(output);
                }
            }

            final Collection<File> actualFiles = FileUtils.listFiles(subDir, new WildcardFileFilter("*.*"),
                new WildcardFileFilter("*"));

            final int count = actualFiles.size();
            final Object[] fileObjs = actualFiles.toArray();

            assertEquals(expectedFileNames.length, actualFiles.size(), () -> actualFiles.toString());

            final Map<String, String> foundFileNames = new HashMap<>();

            for (int i = 0; i < count; ++i) {
                boolean found = false;
                for (int j = 0; !found && j < expectedFileNames.length; ++j) {
                    if (expectedFileNames[j].equals(((File) fileObjs[i]).getName())) {
                        foundFileNames.put(expectedFileNames[j], expectedFileNames[j]);
                        found = true;
                    }
                }
            }

            assertEquals(foundFileNames.size(), expectedFileNames.length, () -> foundFileNames.toString());
        } finally {
            subDir.delete();
        }
    }

    @Test
    public void testListFilesWithDirs() throws IOException {
        final File srcDir = temporaryFolder;

        final File subDir1 = new File(srcDir, "subdir");
        final File subDir2 = new File(subDir1, "subdir2");
        subDir1.mkdir();
        subDir2.mkdir();
        try {
            final File someFile = new File(subDir2, "a.txt");
            if (!someFile.getParentFile().exists()) {
                throw new IOException("Cannot create file " + someFile + " as the parent directory does not exist");
            }
            final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(someFile));
            try {
                TestUtils.generateTestData(output, 100);
            } finally {
                IOUtils.closeQuietly(output);
            }

            final File subDir3 = new File(subDir2, "subdir3");
            subDir3.mkdir();

            final Collection<File> files = FileUtils.listFilesAndDirs(subDir1, new WildcardFileFilter("*.*"),
                new WildcardFileFilter("*"));

            assertEquals(4, files.size());
            assertTrue(files.contains(subDir1), "Should contain the directory.");
            assertTrue(files.contains(subDir2), "Should contain the directory.");
            assertTrue(files.contains(someFile), "Should contain the file.");
            assertTrue(files.contains(subDir3), "Should contain the directory.");
        } finally {
            subDir1.delete();
        }
    }

    @Test
    public void testMoveDirectory_CopyDelete() throws Exception {

        final File dir = temporaryFolder;
        final File src = new File(dir, "testMoveDirectory2Source") {
            private static final long serialVersionUID = 1L;

            // Force renameTo to fail
            @Override
            public boolean renameTo(final File dest) {
                return false;
            }
        };
        final File testDir = new File(src, "foo");
        final File testFile = new File(testDir, "bar");
        testDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File destination = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destination);

        // Move the directory
        FileUtils.moveDirectory(src, destination);

        // Check results
        assertTrue(destination.exists(), "Check Exist");
        assertTrue(!src.exists(), "Original deleted");
        final File movedDir = new File(destination, testDir.getName());
        final File movedFile = new File(movedDir, testFile.getName());
        assertTrue(movedDir.exists(), "Check dir moved");
        assertTrue(movedFile.exists(), "Check file moved");
    }

    @Test
    public void testMoveDirectory_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectory(null, new File("foo")));
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectory(new File("foo"), null));
        try {
            FileUtils.moveDirectory(new File("nonexistant"), new File("foo"));
            fail("Expected FileNotFoundException for source");
        } catch (final FileNotFoundException e) {
            // expected
        }
        final File testFile = new File(temporaryFolder, "testMoveDirectoryFile");
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveDirectory(testFile, new File("foo")));
        final File testSrcFile = new File(temporaryFolder, "testMoveDirectorySource");
        final File testDestFile = new File(temporaryFolder, "testMoveDirectoryDest");
        testSrcFile.mkdir();
        testDestFile.mkdir();
        try {
            FileUtils.moveDirectory(testSrcFile, testDestFile);
            fail("Expected FileExistsException when dest already exists");
        } catch (final FileExistsException e) {
            // expected
        }
    }

    @Test
    public void testMoveDirectory_Rename() throws Exception {
        final File dir = temporaryFolder;
        final File src = new File(dir, "testMoveDirectory1Source");
        final File testDir = new File(src, "foo");
        final File testFile = new File(testDir, "bar");
        testDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File destination = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destination);

        // Move the directory
        FileUtils.moveDirectory(src, destination);

        // Check results
        assertTrue(destination.exists(), "Check Exist");
        assertTrue(!src.exists(), "Original deleted");
        final File movedDir = new File(destination, testDir.getName());
        final File movedFile = new File(movedDir, testFile.getName());
        assertTrue(movedDir.exists(), "Check dir moved");
        assertTrue(movedFile.exists(), "Check file moved");
    }

    @Test
    public void testMoveDirectoryToDirectory() throws Exception {
        final File dir = temporaryFolder;
        final File src = new File(dir, "testMoveDirectory1Source");
        final File testChildDir = new File(src, "foo");
        final File testFile = new File(testChildDir, "bar");
        testChildDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File destDir = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destDir);
        assertFalse(destDir.exists(), "Check Exist before");

        // Move the directory
        FileUtils.moveDirectoryToDirectory(src, destDir, true);

        // Check results
        assertTrue(destDir.exists(), "Check Exist after");
        assertTrue(!src.exists(), "Original deleted");
        final File movedDir = new File(destDir, src.getName());
        final File movedChildDir = new File(movedDir, testChildDir.getName());
        final File movedFile = new File(movedChildDir, testFile.getName());
        assertTrue(movedDir.exists(), "Check dir moved");
        assertTrue(movedChildDir.exists(), "Check child dir moved");
        assertTrue(movedFile.exists(), "Check file moved");
    }

    @Test
    public void testMoveDirectoryToDirectory_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectoryToDirectory(null, new File("foo"), true));
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectoryToDirectory(new File("foo"), null, true));
        final File testFile1 = new File(temporaryFolder, "testMoveFileFile1");
        final File testFile2 = new File(temporaryFolder, "testMoveFileFile2");
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1 + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 = new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2 + " as the parent directory does not exist");
        }
        final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        try {
            FileUtils.moveDirectoryToDirectory(testFile1, testFile2, true);
            fail("Expected IOException when dest not a directory");
        } catch (final IOException e) {
            // expected
        }

        final File nonexistant = new File(temporaryFolder, "testMoveFileNonExistant");
        try {
            FileUtils.moveDirectoryToDirectory(testFile1, nonexistant, false);
            fail("Expected IOException when dest does not exist and create=false");
        } catch (final IOException e) {
            // expected
        }
    }

    @Test
    public void testMoveFile_CopyDelete() throws Exception {
        final File destination = new File(temporaryFolder, "move2.txt");
        final File src = new File(testFile1.getAbsolutePath()) {
            private static final long serialVersionUID = 1L;

            // Force renameTo to fail, as if destination is on another
            // filesystem
            @Override
            public boolean renameTo(final File f) {
                return false;
            }
        };
        FileUtils.moveFile(src, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertTrue(!src.exists(), "Original deleted");
    }

    @Test
    public void testMoveFile_CopyDelete_Failed() throws Exception {
        final File destination = new File(temporaryFolder, "move3.txt");
        final File src = new File(testFile1.getAbsolutePath()) {
            private static final long serialVersionUID = 1L;

            // Force delete failure
            @Override
            public boolean delete() {
                return false;
            }

            // Force renameTo to fail, as if destination is on another
            // filesystem
            @Override
            public boolean renameTo(final File f) {
                return false;
            }

        };
        assertThrows(IOException.class, () -> FileUtils.moveFile(src, destination));
        // expected
        assertTrue(!destination.exists(), "Check Rollback");
        assertTrue(src.exists(), "Original exists");
    }

    @Test
    public void testMoveFile_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveFile(null, new File("foo")));
        assertThrows(NullPointerException.class, () -> FileUtils.moveFile(new File("foo"), null));
        try {
            FileUtils.moveFile(new File("nonexistant"), new File("foo"));
            fail("Expected FileNotFoundException for source");
        } catch (final FileNotFoundException e) {
            // expected
        }
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveFile(temporaryFolder, new File("foo")));
        final File testSourceFile = new File(temporaryFolder, "testMoveFileSource");
        final File testDestFile = new File(temporaryFolder, "testMoveFileSource");
        if (!testSourceFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testSourceFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testSourceFile));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testDestFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testDestFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testDestFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        try {
            FileUtils.moveFile(testSourceFile, testDestFile);
            fail("Expected FileExistsException when dest already exists");
        } catch (final FileExistsException e) {
            // expected
        }
    }

    @Test
    public void testMoveFile_Rename() throws Exception {
        final File destination = new File(temporaryFolder, "move1.txt");

        FileUtils.moveFile(testFile1, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertTrue(!testFile1.exists(), "Original deleted");
    }

    @Test
    public void testMoveFileToDirectory() throws Exception {
        final File destDir = new File(temporaryFolder, "moveFileDestDir");
        final File movedFile = new File(destDir, testFile1.getName());
        assertFalse(destDir.exists(), "Check Exist before");
        assertFalse(movedFile.exists(), "Check Exist before");

        FileUtils.moveFileToDirectory(testFile1, destDir, true);
        assertTrue(movedFile.exists(), "Check Exist after");
        assertTrue(!testFile1.exists(), "Original deleted");
    }

    @Test
    public void testMoveFileToDirectory_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveFileToDirectory(null, new File("foo"), true));
        assertThrows(NullPointerException.class, () -> FileUtils.moveFileToDirectory(new File("foo"), null, true));
        final File testFile1 = new File(temporaryFolder, "testMoveFileFile1");
        final File testFile2 = new File(temporaryFolder, "testMoveFileFile2");
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveFileToDirectory(testFile1, testFile2, true));

        final File nonexistant = new File(temporaryFolder, "testMoveFileNonExistant");
        try {
            FileUtils.moveFileToDirectory(testFile1, nonexistant, false);
            fail("Expected IOException when dest does not exist and create=false");
        } catch (final IOException e) {
            // expected
        }
    }

    @Test
    public void testMoveToDirectory() throws Exception {
        final File destDir = new File(temporaryFolder, "testMoveToDirectoryDestDir");
        final File testDir = new File(temporaryFolder, "testMoveToDirectoryTestDir");
        final File testFile = new File(temporaryFolder, "testMoveToDirectoryTestFile");
        testDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File movedFile = new File(destDir, testFile.getName());
        final File movedDir = new File(destDir, testFile.getName());

        assertFalse(movedFile.exists(), "Check File Doesnt exist");
        assertFalse(movedDir.exists(), "Check Dir Doesnt exist");

        // Test moving a file
        FileUtils.moveToDirectory(testFile, destDir, true);
        assertTrue(movedFile.exists(), "Check File exists");
        assertFalse(testFile.exists(), "Check Original File doesn't exist");

        // Test moving a directory
        FileUtils.moveToDirectory(testDir, destDir, true);
        assertTrue(movedDir.exists(), "Check Dir exists");
        assertFalse(testDir.exists(), "Check Original Dir doesn't exist");
    }

    @Test
    public void testMoveToDirectory_Errors() throws Exception {
        try {
            FileUtils.moveDirectoryToDirectory(null, new File("foo"), true);
            fail("Expected NullPointerException when source is null");
        } catch (final NullPointerException e) {
            // expected
        }
        try {
            FileUtils.moveDirectoryToDirectory(new File("foo"), null, true);
            fail("Expected NullPointerException when destination is null");
        } catch (final NullPointerException e) {
            // expected
        }
        final File nonexistant = new File(temporaryFolder, "nonexistant");
        final File destDir = new File(temporaryFolder, "MoveToDirectoryDestDir");
        try {
            FileUtils.moveToDirectory(nonexistant, destDir, true);
            fail("Expected IOException when source does not exist");
        } catch (final IOException e) {
            // expected
        }
    }

    @Test
    public void testReadFileToByteArray() throws Exception {
        final File file = new File(temporaryFolder, "read.txt");
        final FileOutputStream out = new FileOutputStream(file);
        out.write(11);
        out.write(21);
        out.write(31);
        out.close();

        final byte[] data = FileUtils.readFileToByteArray(file);
        assertEquals(3, data.length);
        assertEquals(11, data[0]);
        assertEquals(21, data[1]);
        assertEquals(31, data[2]);
    }

    @Test
    public void testReadFileToStringWithDefaultEncoding() throws Exception {
        final File file = new File(temporaryFolder, "read.obj");
        final FileOutputStream out = new FileOutputStream(file);
        final byte[] text = "Hello /u1234".getBytes();
        out.write(text);
        out.close();

        final String data = FileUtils.readFileToString(file);
        assertEquals("Hello /u1234", data);
    }

    @Test
    public void testReadFileToStringWithEncoding() throws Exception {
        final File file = new File(temporaryFolder, "read.obj");
        final FileOutputStream out = new FileOutputStream(file);
        final byte[] text = "Hello /u1234".getBytes(StandardCharsets.UTF_8);
        out.write(text);
        out.close();

        final String data = FileUtils.readFileToString(file, "UTF8");
        assertEquals("Hello /u1234", data);
    }

    @Test
    public void testReadLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        try {
            final String[] data = new String[]{"hello", "/u1234", "", "this is", "some text"};
            TestUtils.createLineBasedFile(file, data);

            final List<String> lines = FileUtils.readLines(file, "UTF-8");
            assertEquals(Arrays.asList(data), lines);
        } finally {
            TestUtils.deleteFile(file);
        }
    }

    @Test
    public void testSizeOf() throws Exception {
        final File file = new File(temporaryFolder, getName());

        // Null argument
        try {
            FileUtils.sizeOf(null);
            fail("Exception expected.");
        } catch (final NullPointerException ignore) {
        }

        // Non-existent file
        try {
            FileUtils.sizeOf(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // New file
        assertEquals(0, FileUtils.sizeOf(file));
        file.delete();

        // Existing file
        assertEquals(testFile1Size, FileUtils.sizeOf(testFile1), "Unexpected files size");

        // Existing directory
        assertEquals(TEST_DIRECTORY_SIZE, FileUtils.sizeOf(temporaryFolder), "Unexpected directory size");
    }

    @Test
    public void testSizeOfAsBigInteger() throws Exception {
        final File file = new File(temporaryFolder, getName());

        // Null argument
        try {
            FileUtils.sizeOfAsBigInteger(null);
            fail("Exception expected.");
        } catch (final NullPointerException ignore) {
        }

        // Non-existent file
        try {
            FileUtils.sizeOfAsBigInteger(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // New file
        assertEquals(BigInteger.ZERO, FileUtils.sizeOfAsBigInteger(file));
        file.delete();

        // Existing file
        assertEquals(BigInteger.valueOf(testFile1Size), FileUtils.sizeOfAsBigInteger(testFile1),
                "Unexpected files size");

        // Existing directory
        assertEquals(TEST_DIRECTORY_SIZE_BI, FileUtils.sizeOfAsBigInteger(temporaryFolder),
                "Unexpected directory size");
    }


    @Test
    public void testSizeOfDirectory() throws Exception {
        final File file = new File(temporaryFolder, getName());

        // Non-existent file
        try {
            FileUtils.sizeOfDirectory(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Creates file
        file.createNewFile();

        // Existing file
        try {
            FileUtils.sizeOfDirectory(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Existing directory
        file.delete();
        file.mkdir();

        // Create a cyclic symlink
        this.createCircularSymLink(file);

        assertEquals(TEST_DIRECTORY_SIZE, FileUtils.sizeOfDirectory(file), "Unexpected directory size");
    }

    @Test
    public void testSizeOfDirectoryAsBigInteger() throws Exception {
        final File file = new File(temporaryFolder, getName());

        // Non-existent file
        try {
            FileUtils.sizeOfDirectoryAsBigInteger(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // Existing file
        try {
            FileUtils.sizeOfDirectoryAsBigInteger(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Existing directory
        file.delete();
        file.mkdir();

        this.createCircularSymLink(file);

        assertEquals(TEST_DIRECTORY_SIZE_BI, FileUtils.sizeOfDirectoryAsBigInteger(file), "Unexpected directory size");

        // Existing directory which size is greater than zero
        file.delete();
        file.mkdir();

        final File nonEmptyFile = new File(file, "nonEmptyFile" + System.nanoTime());
        if (!nonEmptyFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + nonEmptyFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(nonEmptyFile));
        try {
            TestUtils.generateTestData(output, TEST_DIRECTORY_SIZE_GT_ZERO_BI.longValue());
        } finally {
            IOUtils.closeQuietly(output);
        }
        nonEmptyFile.deleteOnExit();

        assertEquals(TEST_DIRECTORY_SIZE_GT_ZERO_BI, FileUtils.sizeOfDirectoryAsBigInteger(file),
                "Unexpected directory size");

        nonEmptyFile.delete();
        file.delete();
    }

    //-----------------------------------------------------------------------
    @Test
    public void testToFile1() throws Exception {
        final URL url = new URL("file", null, "a/b/c/file.txt");
        final File file = FileUtils.toFile(url);
        assertTrue(file.toString().contains("file.txt"));
    }

    @Test
    public void testToFile2() throws Exception {
        final URL url = new URL("file", null, "a/b/c/file%20n%61me%2520.tx%74");
        final File file = FileUtils.toFile(url);
        assertTrue(file.toString().contains("file name%20.txt"));
    }

    @Test
    public void testToFile3() throws Exception {
        assertEquals(null, FileUtils.toFile(null));
        assertEquals(null, FileUtils.toFile(new URL("http://jakarta.apache.org")));
    }

    @Test
    public void testToFile4() throws Exception {
        final URL url = new URL("file", null, "a/b/c/file%%20%me.txt%");
        final File file = FileUtils.toFile(url);
        assertTrue(file.toString().contains("file% %me.txt%"));
    }

    /* IO-252 */
    @Test
    public void testToFile5() throws Exception {
        final URL url = new URL("file", null, "both%20are%20100%20%25%20true");
        final File file = FileUtils.toFile(url);
        assertEquals("both are 100 % true", file.toString());
    }

    @Test
    public void testToFiles1() throws Exception {
        final URL[] urls = new URL[]{
                new URL("file", null, "file1.txt"),
                new URL("file", null, "file2.txt"),
        };
        final File[] files = FileUtils.toFiles(urls);

        assertEquals(urls.length, files.length);
        assertEquals(true, files[0].toString().contains("file1.txt"), "File: " + files[0]);
        assertEquals(true, files[1].toString().contains("file2.txt"), "File: " + files[1]);
    }

    @Test
    public void testToFiles2() throws Exception {
        final URL[] urls = new URL[]{
                new URL("file", null, "file1.txt"),
                null,
        };
        final File[] files = FileUtils.toFiles(urls);

        assertEquals(urls.length, files.length);
        assertEquals(true, files[0].toString().contains("file1.txt"), "File: " + files[0]);
        assertEquals(null, files[1], "File: " + files[1]);
    }

    @Test
    public void testToFiles3() throws Exception {
        final URL[] urls = null;
        final File[] files = FileUtils.toFiles(urls);

        assertEquals(0, files.length);
    }

    @Test
    public void testToFiles3a() throws Exception {
        final URL[] urls = new URL[0]; // empty array
        final File[] files = FileUtils.toFiles(urls);

        assertEquals(0, files.length);
    }

    @Test
    public void testToFiles4() throws Exception {
        final URL[] urls = new URL[]{
                new URL("file", null, "file1.txt"),
                new URL("http", "jakarta.apache.org", "file1.txt"),
        };
        try {
            FileUtils.toFiles(urls);
            fail();
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testToFileUtf8() throws Exception {
        final URL url = new URL("file", null, "/home/%C3%A4%C3%B6%C3%BC%C3%9F");
        final File file = FileUtils.toFile(url);
        assertTrue(file.toString().contains("\u00E4\u00F6\u00FC\u00DF"));
    }

    @Test
    public void testTouch() throws IOException {
        final File file = new File(temporaryFolder, "touch.txt");
        if (file.exists()) {
            file.delete();
        }
        assertTrue(!file.exists(), "Bad test: test file still exists");
        FileUtils.touch(file);
        assertTrue(file.exists(), "FileUtils.touch() created file");
        final FileOutputStream out = new FileOutputStream(file);
        assertEquals(0, file.length(), "Created empty file.");
        out.write(0);
        out.close();
        assertEquals(1, file.length(), "Wrote one byte to file");
        final long y2k = new GregorianCalendar(2000, 0, 1).getTime().getTime();
        final boolean res = setLastModifiedMillis(file, y2k);  // 0L fails on Win98
        assertEquals(true, res, "Bad test: set lastModified failed");
        assertEquals(y2k, getLastModifiedMillis(file), "Bad test: set lastModified set incorrect value");
        final long now = System.currentTimeMillis();
        FileUtils.touch(file);
        assertEquals(1, file.length(), "FileUtils.touch() didn't empty the file.");
        assertEquals(false, y2k == getLastModifiedMillis(file), "FileUtils.touch() changed lastModified");
        assertEquals(true, getLastModifiedMillis(file) >= now - 3000, "FileUtils.touch() changed lastModified to more than now-3s");
        assertEquals(true, getLastModifiedMillis(file) <= now + 3000, "FileUtils.touch() changed lastModified to less than now+3s");
    }

    @Test
    public void testToURLs1() throws Exception {
        final File[] files = new File[]{
                new File(temporaryFolder, "file1.txt"),
                new File(temporaryFolder, "file2.txt"),
                new File(temporaryFolder, "test file.txt"),
        };
        final URL[] urls = FileUtils.toURLs(files);

        assertEquals(files.length, urls.length);
        assertTrue(urls[0].toExternalForm().startsWith("file:"));
        assertTrue(urls[0].toExternalForm().contains("file1.txt"));
        assertTrue(urls[1].toExternalForm().startsWith("file:"));
        assertTrue(urls[1].toExternalForm().contains("file2.txt"));

        // Test escaped char
        assertTrue(urls[2].toExternalForm().startsWith("file:"));
        assertTrue(urls[2].toExternalForm().contains("test%20file.txt"));
    }

    @Test
    public void testToURLs3a() throws Exception {
        final File[] files = new File[0]; // empty array
        final URL[] urls = FileUtils.toURLs(files);

        assertEquals(0, urls.length);
    }

    @Test
    public void testWrite_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWrite_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteByteArrayToFile() throws Exception {
        final File file = new File(temporaryFolder, "write.obj");
        final byte[] data = new byte[]{11, 21, 31};
        FileUtils.writeByteArrayToFile(file, data);
        TestUtils.assertEqualContent(data, file);
    }

    @Test
    public void testWriteByteArrayToFile_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeByteArrayToFile(file, "this is brand new data".getBytes(), false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteByteArrayToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeByteArrayToFile(file, "this is brand new data".getBytes(), true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength() throws Exception {
        final File file = new File(temporaryFolder, "write.obj");
        final byte[] data = new byte[]{11, 21, 32, 41, 51};
        final byte[] writtenData = new byte[3];
        System.arraycopy(data, 1, writtenData, 0, 3);
        FileUtils.writeByteArrayToFile(file, data, 1, 3);
        TestUtils.assertEqualContent(writtenData, file);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength_WithAppendOptionTrue_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final byte[] data = "SKIP_THIS_this is brand new data_AND_SKIP_THIS".getBytes(StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(file, data, 10, 22, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final byte[] data = "SKIP_THIS_this is brand new data_AND_SKIP_THIS".getBytes(StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(file, data, 10, 22, true);

        final String expected = "This line was there before you..." + "this is brand new data";
        final String actual = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteCharSequence1() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.write(file, "Hello /u1234", "UTF8");
        final byte[] text = "Hello /u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteCharSequence2() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.write(file, "Hello /u1234", (String) null);
        final byte[] text = "Hello /u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteLines_3arg_nullSeparator() throws Exception {
        final Object[] data = new Object[]{
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list);

        final String expected = "hello" + System.lineSeparator() + "world" + System.lineSeparator() +
                System.lineSeparator() + "this is" + System.lineSeparator() +
                System.lineSeparator() + "some text" + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_3argsWithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, linesToAppend, false);

        final String expected = "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_3argsWithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, linesToAppend, true);

        final String expected = "This line was there before you..."
                + "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_4arg() throws Exception {
        final Object[] data = new Object[]{
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list, "*");

        final String expected = "hello*world**this is**some text*";
        final String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_4arg_nullSeparator() throws Exception {
        final Object[] data = new Object[]{
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list, null);

        final String expected = "hello" + System.lineSeparator() + "world" + System.lineSeparator() +
                System.lineSeparator() + "this is" + System.lineSeparator() +
                System.lineSeparator() + "some text" + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_4arg_Writer_nullData() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeLines(file, "US-ASCII", null, "*");

        assertEquals(0, file.length(), "Sizes differ");
    }

    @Test
    public void testWriteLines_4argsWithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, linesToAppend, null, false);

        final String expected = "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }


    @Test
    public void testWriteLines_4argsWithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, linesToAppend, null, true);

        final String expected = "This line was there before you..."
                + "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_5argsWithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, null, linesToAppend, null, false);

        final String expected = "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_5argsWithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, null, linesToAppend, null, true);

        final String expected = "This line was there before you..."
                + "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLinesEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, null, linesToAppend, false);

        final String expected = "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLinesEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, null, linesToAppend, true);

        final String expected = "This line was there before you..."
                + "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteStringToFile_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteStringToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteStringToFile1() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.writeStringToFile(file, "Hello /u1234", "UTF8");
        final byte[] text = "Hello /u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFile2() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.writeStringToFile(file, "Hello /u1234", (String) null);
        final byte[] text = "Hello /u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFile3() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.writeStringToFile(file, "Hello /u1234", (Charset) null);
        final byte[] text = "Hello /u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFileWithEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", (String) null, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteStringToFileWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", (String) null, true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteWithEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", (String) null, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", (String) null, true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

}
