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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.io.file.Counters.PathCounters;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.TempDirectory;
import org.apache.commons.io.file.TempFile;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link FileUtils}.
 */
@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"}) // unit tests include tests of many deprecated methods
public class FileUtilsTest extends AbstractTempDirTest {

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

    private static final Path DIR_SIZE_1 = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1");

    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    /** Test data. */
    private static final long DATE3 = 1_000_000_002_000L;

    /** Test data. */
    private static final long DATE2 = 1_000_000_001_000L;

    /** Test data. */
    private static final long DATE1 = 1_000_000_000_000L;

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

    private File testFile1;
    private File testFile2;
    private long testFile1Size;
    private long testFile2Size;

    private void assertContentMatchesAfterCopyURLToFileFor(final String resourceName, final File destination) throws IOException {
        FileUtils.copyURLToFile(getClass().getResource(resourceName), destination);

        try (InputStream fis = Files.newInputStream(destination.toPath());
             InputStream expected = getClass().getResourceAsStream(resourceName)) {
            assertTrue(IOUtils.contentEquals(expected, fis), "Content is not equal.");
        }
    }

    private void backDateFile10Minutes(final File testFile) throws IOException {
        final long mins10 = 1000 * 60 * 10;
        final long lastModified1 = getLastModifiedMillis(testFile);
        assertTrue(setLastModifiedMillis(testFile, lastModified1 - mins10));
        // ensure it was changed
        assertNotEquals(getLastModifiedMillis(testFile), lastModified1, "Should have changed source date");
    }

    private void consumeRemaining(final Iterator<File> iterator) {
        if (iterator != null) {
            iterator.forEachRemaining(e -> {
                // noop
            });
        }
    }

    private Path createCircularOsSymbolicLink(final String linkName, final String targetName) throws IOException {
        return Files.createSymbolicLink(Paths.get(linkName), Paths.get(targetName));
    }

    /**
     * May throw java.nio.file.FileSystemException: C:\Users\...\FileUtilsTestCase\cycle: A required privilege is not held
     * by the client. On Windows, you are fine if you run a terminal with admin karma.
     */
    private void createCircularSymbolicLink(final File file) throws IOException {
        assertTrue(file.exists());
        final String linkName = file + "/cycle";
        final String targetName = file + "/..";
        assertTrue(file.exists());
        final Path linkPath = Paths.get(linkName);
        assertFalse(Files.exists(linkPath));
        final Path targetPath = Paths.get(targetName);
        assertTrue(Files.exists(targetPath));
        try {
            // May throw java.nio.file.FileSystemException: C:\Users\...\FileUtilsTestCase\cycle: A required privilege is not held by the client.
            // On Windows, you are fine if you run a terminal with admin karma.
            Files.createSymbolicLink(linkPath, targetPath);
        } catch (final UnsupportedOperationException e) {
            createCircularOsSymbolicLink(linkName, targetName);
        }
        // Sanity check:
        assertTrue(Files.isSymbolicLink(linkPath), () -> "Expected a symbolic link here: " + linkName);
    }

    private void createFilesForTestCopyDirectory(final File grandParentDir, final File parentDir, final File childDir) throws IOException {
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

    private ImmutablePair<Path, Path> createTempSymbolicLinkedRelativeDir() throws IOException {
        final Path targetDir = tempDirPath.resolve("subdir");
        final Path symLinkedDir = tempDirPath.resolve("symlinked-dir");
        Files.createDirectory(targetDir);
        Files.createSymbolicLink(symLinkedDir, targetDir);
        return ImmutablePair.of(symLinkedDir, targetDir);
    }

    private Set<String> getFilePathSet(final List<File> files) {
        return files.stream().map(f -> {
            try {
                return f.getCanonicalPath();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toSet());
    }

    private long getLastModifiedMillis(final File file) throws IOException {
        return FileUtils.lastModified(file);
    }

    private String getName() {
        return this.getClass().getSimpleName();
    }

    private void iterateFilesAndDirs(final File dir, final IOFileFilter fileFilter,
        final IOFileFilter dirFilter, final Collection<File> expectedFilesAndDirs) {
        final Iterator<File> iterator = FileUtils.iterateFilesAndDirs(dir, fileFilter, dirFilter);
        int filesCount = 0;
        try {
            final List<File> actualFiles = new ArrayList<>();
            while (iterator.hasNext()) {
                filesCount++;
                final File file = iterator.next();
                actualFiles.add(file);
                assertTrue(expectedFilesAndDirs.contains(file),
                    () -> "Unexpected directory/file " + file + ", expected one of " + expectedFilesAndDirs);
            }
            assertEquals(expectedFilesAndDirs.size(), filesCount, actualFiles::toString);
        } finally {
            // MUST consume until the end in order to close the underlying stream.
            consumeRemaining(iterator);
        }
    }

    private void openOutputStream_noParent(final boolean createFile) throws Exception {
        final File file = new File("test.txt");
        assertNull(file.getParentFile());
        try {
            if (createFile) {
                TestUtils.createLineFileUtf8(file, new String[]{"Hello"});
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
    }

    @BeforeEach
    public void setUp() throws Exception {
        testFile1 = new File(tempDirFile, "file1-test.txt");
        testFile2 = new File(tempDirFile, "file1a-test.txt");
        testFile1Size = testFile1.length();
        testFile2Size = testFile2.length();
        if (!testFile1.getParentFile().exists()) {
            fail("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output3 =
                new BufferedOutputStream(Files.newOutputStream(testFile1.toPath()))) {
            TestUtils.generateTestData(output3, testFile1Size);
        }
        if (!testFile2.getParentFile().exists()) {
            fail("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output2 =
                new BufferedOutputStream(Files.newOutputStream(testFile2.toPath()))) {
            TestUtils.generateTestData(output2, testFile2Size);
        }
        FileUtils.deleteDirectory(tempDirFile);
        tempDirFile.mkdirs();
        if (!testFile1.getParentFile().exists()) {
            fail("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output1 =
                new BufferedOutputStream(Files.newOutputStream(testFile1.toPath()))) {
            TestUtils.generateTestData(output1, testFile1Size);
        }
        if (!testFile2.getParentFile().exists()) {
            fail("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testFile2.toPath()))) {
            TestUtils.generateTestData(output, testFile2Size);
        }
    }

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
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 2 - 1));
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
    public void testByteCountToDisplaySizeNumber() {
        assertEquals("0 bytes", FileUtils.byteCountToDisplaySize(Integer.valueOf(0)));
        assertEquals("1 bytes", FileUtils.byteCountToDisplaySize(Integer.valueOf(1)));
        assertEquals("1023 bytes", FileUtils.byteCountToDisplaySize(Integer.valueOf(1023)));
        assertEquals("1 KB", FileUtils.byteCountToDisplaySize(Integer.valueOf(1024)));
        assertEquals("1 KB", FileUtils.byteCountToDisplaySize(Integer.valueOf(1025)));
        assertEquals("1023 KB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024 * 1023)));
        assertEquals("1 MB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024 * 1024)));
        assertEquals("1 MB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024 * 1025)));
        assertEquals("1023 MB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024 * 1024 * 1023)));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024 * 1024 * 1024)));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024 * 1024 * 1025)));
        assertEquals("2 GB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024L * 1024 * 1024 * 2)));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024L * 1024 * 1024 * 2 - 1)));
        assertEquals("1 TB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024L * 1024 * 1024 * 1024)));
        assertEquals("1 PB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024L * 1024 * 1024 * 1024 * 1024)));
        assertEquals("1 EB", FileUtils.byteCountToDisplaySize(Long.valueOf(1024L * 1024 * 1024 * 1024 * 1024 * 1024)));
        assertEquals("7 EB", FileUtils.byteCountToDisplaySize(Long.valueOf(Long.MAX_VALUE)));
        // Other MAX_VALUEs
        assertEquals("63 KB", FileUtils.byteCountToDisplaySize(Integer.valueOf(Character.MAX_VALUE)));
        assertEquals("31 KB", FileUtils.byteCountToDisplaySize(Short.valueOf(Short.MAX_VALUE)));
        assertEquals("1 GB", FileUtils.byteCountToDisplaySize(Integer.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    public void testChecksum() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(tempDirFile, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, StandardCharsets.US_ASCII.name());

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

    @Test
    public void testChecksumCRC32() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(tempDirFile, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, StandardCharsets.US_ASCII.name());

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
        final File file1 = new File(tempDirFile, "checksum-test.txt");
        FileUtils.writeStringToFile(file1, text1, StandardCharsets.US_ASCII.name());

        // create a second test file
        final String text2 = "To be or not to be - Shakespeare";
        final File file2 = new File(tempDirFile, "checksum-test2.txt");
        FileUtils.writeStringToFile(file2, text2, StandardCharsets.US_ASCII.name());

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
    public void testChecksumOnDirectory() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.checksum(FileUtils.current(), new CRC32()));
    }

    @Test
    public void testChecksumOnNullChecksum() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(tempDirFile, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, StandardCharsets.US_ASCII.name());
        assertThrows(NullPointerException.class, () -> FileUtils.checksum(file, null));
    }

    @Test
    public void testChecksumOnNullFile() {
        assertThrows(NullPointerException.class, () -> FileUtils.checksum(null, new CRC32()));
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

    @Test
    public void testContentEquals() throws Exception {
        // Non-existent files
        final File file = new File(tempDirFile, getName());
        final File file2 = new File(tempDirFile, getName() + "2");
        assertTrue(FileUtils.contentEquals(null, null));
        assertFalse(FileUtils.contentEquals(null, file));
        assertFalse(FileUtils.contentEquals(file, null));
        // both don't exist
        assertTrue(FileUtils.contentEquals(file, file));
        assertTrue(FileUtils.contentEquals(file, file2));
        assertTrue(FileUtils.contentEquals(file2, file2));
        assertTrue(FileUtils.contentEquals(file2, file));
        // Directories
        assertThrows(IllegalArgumentException.class, () -> FileUtils.contentEquals(tempDirFile, tempDirFile));
        // Different files
        final File objFile1 = new File(tempDirFile, getName() + ".object");
        FileUtils.copyURLToFile(getClass().getResource("/java/lang/Object.class"), objFile1);
        final File objFile1b = new File(tempDirFile, getName() + ".object2");
        FileUtils.copyURLToFile(getClass().getResource("/java/lang/Object.class"), objFile1b);
        final File objFile2 = new File(tempDirFile, getName() + ".collection");
        FileUtils.copyURLToFile(getClass().getResource("/java/util/Collection.class"), objFile2);
        // equals to other
        assertFalse(FileUtils.contentEquals(objFile1, objFile2));
        assertFalse(FileUtils.contentEquals(objFile1b, objFile2));
        assertTrue(FileUtils.contentEquals(objFile1, objFile1b));
        // equals to self
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
        final File file1 = new File(tempDirFile, getName());
        final File file2 = new File(tempDirFile, getName() + "2");
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
            () -> FileUtils.contentEqualsIgnoreEOL(tempDirFile, tempDirFile, null));

        // Different files
        final File tfile1 = new File(tempDirFile, getName() + ".txt1");
        FileUtils.write(tfile1, "123\r");

        final File tfile2 = new File(tempDirFile, getName() + ".txt2");
        FileUtils.write(tfile2, "123\n");

        final File tfile3 = new File(tempDirFile, getName() + ".collection");
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

    /**
     * See what happens when copyDirectory copies a directory that is a symlink
     * to another directory containing non-symlinked files.
     * This is a characterization test to explore current behavior, and arguably
     * represents a bug. This behavior, and the test, is likely to change
     * and should not be relied on.
     */
    @Test
    public void testCopyDir_SymbolicLink() throws Exception {
        // Make a directory
        final File realDirectory = new File(tempDirFile, "real_directory");
        realDirectory.mkdir();
        final File content = new File(realDirectory, "hello.txt");
        FileUtils.writeStringToFile(content, "HELLO WORLD", "UTF8");

        // Make a symlink to the directory
        final Path linkPath = tempDirFile.toPath().resolve("link_to_directory");
        Files.createSymbolicLink(linkPath, realDirectory.toPath());

        // Now copy symlink
        final File destination = new File(tempDirFile, "destination");

        // Is the copy a symlink or an actual directory?
        FileUtils.copyDirectory(linkPath.toFile(), destination);

        // delete the original file so that if we can read the bytes from the
        // copied directory it's definitely been copied, not linked.
        assumeTrue(content.delete());

        assertFalse(Files.isSymbolicLink(destination.toPath()));
        final File copied_content = new File(destination, "hello.txt");
        final String actual = FileUtils.readFileToString(copied_content, "UTF8");
        assertEquals("HELLO WORLD", actual);
    }

    @Test
    public void testCopyDir_SymbolicLinkCycle() throws Exception {
        // Make a directory
        final File topDirectory = new File(tempDirFile, "topDirectory");
        topDirectory.mkdir();
        final File content = new File(topDirectory, "hello.txt");
        FileUtils.writeStringToFile(content, "HELLO WORLD", "UTF8");
        final File childDirectory = new File(topDirectory, "child_directory");
        childDirectory.mkdir();

        // Make a symlink to the top directory
        final Path linkPath = childDirectory.toPath().resolve("link_to_top");
        Files.createSymbolicLink(linkPath, topDirectory.toPath());

        // Now copy symlink
        final File destination = new File(tempDirFile, "destination");
        FileUtils.copyDirectory(linkPath.toFile(), destination);

        // delete the original file so that if we can read the bytes from the
        // copied directory it's definitely been copied, not linked.
        assumeTrue(content.delete());

        assertFalse(Files.isSymbolicLink(destination.toPath()));
        final File copied_content = new File(destination, "hello.txt");
        final String actual = FileUtils.readFileToString(copied_content, "UTF8");
        assertEquals("HELLO WORLD", actual);

        final File[] copied = destination.listFiles();
        assertEquals(2, copied.length);
    }

    /**
     * Tests IO-807.
     */
    @Test
    public void testCopyDirectory_brokenSymbolicLink() throws IOException {
        // Make a file
        final File sourceDirectory = new File(tempDirFile, "source_directory");
        sourceDirectory.mkdir();
        final File targetFile = new File(sourceDirectory, "hello.txt");
        FileUtils.writeStringToFile(targetFile, "HELLO WORLD", "UTF8");

        // Make a symlink to the file
        final Path targetPath = targetFile.toPath();
        final Path linkPath = sourceDirectory.toPath().resolve("linkfile");
        Files.createSymbolicLink(linkPath, targetPath);
        assumeTrue(Files.isSymbolicLink(linkPath), () -> "Expected a symlink here: " + linkPath);
        assumeTrue(Files.exists(linkPath));
        assumeTrue(Files.exists(linkPath, LinkOption.NOFOLLOW_LINKS));

        // Delete the file file to break the symlink
        assumeTrue(targetFile.delete());
        assumeFalse(Files.exists(linkPath));
        assumeTrue(Files.exists(linkPath, LinkOption.NOFOLLOW_LINKS));

        // Now copy sourceDirectory, including the broken link, to another directory
        final File destination = new File(tempDirFile, "destination");
        FileUtils.copyDirectory(sourceDirectory, destination);
        assertTrue(destination.exists());
        final Path copiedBrokenSymlink = new File(destination, "linkfile").toPath();

        // test for the existence of the copied symbolic link as a link
        assertTrue(Files.isSymbolicLink(copiedBrokenSymlink));

        // shouldn't be able to read through to the source of the link.
        // If we can, then the link points somewhere other than the deleted file
        assertFalse(Files.exists(copiedBrokenSymlink));
    }

    @Test
    public void testCopyDirectory_SymbolicLink() throws IOException {
        // Make a file
        final File sourceDirectory = new File(tempDirFile, "source_directory");
        sourceDirectory.mkdir();
        final File targetFile = new File(sourceDirectory, "hello.txt");
        FileUtils.writeStringToFile(targetFile, "HELLO WORLD", "UTF8");

        // Make a symlink to the file
        final Path targetPath = targetFile.toPath();
        final Path linkPath = sourceDirectory.toPath().resolve("linkfile");
        Files.createSymbolicLink(linkPath, targetPath);
        assumeTrue(Files.isSymbolicLink(linkPath), () -> "Expected a symlink here: " + linkPath);
        assumeTrue(Files.exists(linkPath));
        assumeTrue(Files.exists(linkPath, LinkOption.NOFOLLOW_LINKS));

        // Now copy sourceDirectory to another directory
        final File destination = new File(tempDirFile, "destination");
        FileUtils.copyDirectory(sourceDirectory, destination);
        assertTrue(destination.exists());
        final Path copiedSymlink = new File(destination, "linkfile").toPath();

        // test for the existence of the copied symbolic link as a link
        assertTrue(Files.isSymbolicLink(copiedSymlink));
        assertTrue(Files.exists(copiedSymlink));
    }

    /**
     * Test what happens when copyDirectory copies a directory that contains a symlink
     * to a file outside the copied directory.
     */
    @Test
    public void testCopyDirectory_SymbolicLinkExternalFile() throws Exception {
        // make a file
        final File content = new File(tempDirFile, "hello.txt");
        FileUtils.writeStringToFile(content, "HELLO WORLD", "UTF8");

        // Make a directory
        final File realDirectory = new File(tempDirFile, "real_directory");
        realDirectory.mkdir();

        // Make a symlink to the file
        final Path linkPath = realDirectory.toPath().resolve("link_to_file");
        Files.createSymbolicLink(linkPath, content.toPath());

        // Now copy the directory
        final File destination = new File(tempDirFile, "destination");
        FileUtils.copyDirectory(realDirectory, destination);

        // test that the copied directory contains a link to the original file
        final File copiedLink = new File(destination, "link_to_file");
        assertTrue(Files.isSymbolicLink(copiedLink.toPath()));
        final String actual = FileUtils.readFileToString(copiedLink, "UTF8");
        assertEquals("HELLO WORLD", actual);

        final Path source = Files.readSymbolicLink(copiedLink.toPath());
        assertEquals(content.toPath(), source);
    }

    @Test
    public void testCopyDirectoryExceptions() {
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
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(tempDirFile, tempDirFile));
        //
        // IOException
        assertThrows(IOException.class, () -> FileUtils.copyDirectory(new File("doesnt-exist"), new File("a")));
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(tempDirFile, testFile1));
    }

    @Test
    public void testCopyDirectoryFiltered() throws IOException {
        final File grandParentDir = new File(tempDirFile, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        final NameFileFilter filter = new NameFileFilter("parent", "child", "file3.txt");
        final File destDir = new File(tempDirFile, "copydest");

        FileUtils.copyDirectory(grandParentDir, destDir, filter);
        final List<File> files = LIST_WALKER.list(destDir);
        assertEquals(3, files.size());
        assertEquals("parent", files.get(0).getName());
        assertEquals("child", files.get(1).getName());
        assertEquals("file3.txt", files.get(2).getName());
    }

    @Test
    public void testCopyDirectoryPreserveDates() throws Exception {
        final File source = new File(tempDirFile, "source");
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

        final File target = new File(tempDirFile, "target");
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

        // Test permission of copied file match destination folder
        if (!SystemUtils.IS_OS_WINDOWS) {
            final Set<PosixFilePermission> parentPerms = Files.getPosixFilePermissions(target.getParentFile().toPath());
            final Set<PosixFilePermission> targetPerms = Files.getPosixFilePermissions(target.toPath());
            assertEquals(parentPerms, targetPerms);
        } else {
            final AclFileAttributeView parentView = Files.getFileAttributeView(target.getParentFile().toPath(), AclFileAttributeView.class);
            final AclFileAttributeView targetView = Files.getFileAttributeView(target.toPath(), AclFileAttributeView.class);
            assertEquals(parentView.getAcl(), targetView.getAcl());
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

    /** Tests IO-141 */
    @Test
    public void testCopyDirectoryToChild() throws IOException {
        final File grandParentDir = new File(tempDirFile, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        final long expectedCount = LIST_WALKER.list(grandParentDir).size() + LIST_WALKER.list(parentDir).size();
        final long expectedSize = FileUtils.sizeOfDirectory(grandParentDir) + FileUtils.sizeOfDirectory(parentDir);
        FileUtils.copyDirectory(parentDir, childDir);
        assertEquals(expectedCount, LIST_WALKER.list(grandParentDir).size());
        assertEquals(expectedSize, FileUtils.sizeOfDirectory(grandParentDir));
        assertTrue(expectedCount > 0, "Count > 0");
        assertTrue(expectedSize > 0, "Size > 0");
    }

    @Test
    public void testCopyDirectoryToDirectory_NonExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            fail("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (OutputStream output1 = new BufferedOutputStream(Files.newOutputStream(testFile1.toPath()))) {
            TestUtils.generateTestData(output1, 1234);
        }
        if (!testFile2.getParentFile().exists()) {
            fail("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile2.toPath()))) {
            TestUtils.generateTestData(output, 4321);
        }
        final File srcDir = tempDirFile;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(FileUtils.getTempDirectoryPath(), "tmp-FileUtilsTestCase");
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

    @Test
    public void testCopyDirectoryToExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            fail("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (OutputStream output1 = new BufferedOutputStream(Files.newOutputStream(testFile1.toPath()))) {
            TestUtils.generateTestData(output1, 1234);
        }
        if (!testFile2.getParentFile().exists()) {
            fail("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile2.toPath()))) {
            TestUtils.generateTestData(output, 4321);
        }
        final File srcDir = tempDirFile;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(SystemProperties.getJavaIoTmpdir(), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        destDir.mkdirs();

        FileUtils.copyDirectory(srcDir, destDir);

        final long srcSize = FileUtils.sizeOfDirectory(srcDir);
        assertTrue(srcSize > 0, "Size > 0");
        assertEquals(srcSize, FileUtils.sizeOfDirectory(destDir));
        assertTrue(new File(destDir, "sub/A.txt").exists());
    }

    /** Test IO-141 */
    @Test
    public void testCopyDirectoryToGrandChild() throws IOException {
        final File grandParentDir = new File(tempDirFile, "grandparent");
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

    /** Tests IO-217 FileUtils.copyDirectoryToDirectory makes infinite loops */
    @Test
    public void testCopyDirectoryToItself() throws Exception {
        final File dir = new File(tempDirFile, "itself");
        dir.mkdirs();
        FileUtils.copyDirectoryToDirectory(dir, dir);
        assertEquals(1, LIST_WALKER.list(dir).size());
    }

    @Test
    public void testCopyDirectoryToNonExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            fail("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (OutputStream output1 = new BufferedOutputStream(Files.newOutputStream(testFile1.toPath()))) {
            TestUtils.generateTestData(output1, 1234);
        }
        if (!testFile2.getParentFile().exists()) {
            fail("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile2.toPath()));) {
            TestUtils.generateTestData(output, 4321);
        }
        final File srcDir = tempDirFile;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(FileUtils.getTempDirectoryPath(), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        assertTrue(destDir.exists(), "Check exists");
        final long sizeOfSrcDirectory = FileUtils.sizeOfDirectory(srcDir);
        assertTrue(sizeOfSrcDirectory > 0, "Size > 0");
        assertEquals(sizeOfSrcDirectory, FileUtils.sizeOfDirectory(destDir), "Check size");
        assertTrue(new File(destDir, "sub/A.txt").exists());
        FileUtils.deleteDirectory(destDir);
    }

    /**
     * Test for https://github.com/apache/commons-io/pull/371. The dir name 'par' is a substring of
     * the dir name 'parent' which is the parent of the 'parent/child' dir.
     */
    @Test
    public void testCopyDirectoryWithPotentialFalsePartialMatch() throws IOException {
        final File grandParentDir = new File(tempDirFile, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File parDir = new File(grandParentDir, "par");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parDir, childDir);

        final List<File> initFiles = LIST_WALKER.list(grandParentDir);
        final List<File> parFiles = LIST_WALKER.list(parDir);
        final long expectedCount = initFiles.size() + parFiles.size();
        final long expectedSize = FileUtils.sizeOfDirectory(grandParentDir) + FileUtils.sizeOfDirectory(parDir);
        FileUtils.copyDirectory(parDir, childDir);
        final List<File> latestFiles = LIST_WALKER.list(grandParentDir);
        assertEquals(expectedCount, latestFiles.size());
        assertEquals(expectedSize, FileUtils.sizeOfDirectory(grandParentDir));
        assertTrue(expectedCount > 0, "Count > 0");
        assertTrue(expectedSize > 0, "Size > 0");
        final Set<String> initFilePaths = getFilePathSet(initFiles);
        final Set<String> newFilePaths = getFilePathSet(latestFiles);
        newFilePaths.removeAll(initFilePaths);
        assertEquals(parFiles.size(), newFilePaths.size());
    }

    @Test
    public void testCopyFile_SymbolicLink() throws Exception {
        // Make a file
        final File sourceDirectory = new File(tempDirFile, "source_directory");
        sourceDirectory.mkdir();
        final File targetFile = new File(sourceDirectory, "hello.txt");
        FileUtils.writeStringToFile(targetFile, "HELLO WORLD", "UTF8");

        // Make a symlink to the file
        final Path targetPath = targetFile.toPath();
        final Path linkPath = sourceDirectory.toPath().resolve("linkfile");
        Files.createSymbolicLink(linkPath, targetPath);

        // Now copy symlink to another directory
        final File destination = new File(tempDirFile, "destination");
        FileUtils.copyFile(linkPath.toFile(), destination);
        assertFalse(Files.isSymbolicLink(destination.toPath()));
        final String contents = FileUtils.readFileToString(destination, StandardCharsets.UTF_8);
        assertEquals("HELLO WORLD", contents);
    }

    @Test
    public void testCopyFile1() throws Exception {
        final File destination = new File(tempDirFile, "copy1.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        FileUtils.copyFile(testFile1, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile1Size, destination.length(), "Check Full copy");
        assertEquals(getLastModifiedMillis(testFile1), getLastModifiedMillis(destination), "Check last modified date preserved");
    }

    @Test
    public void testCopyFile1ToDir() throws Exception {
        final File directory = new File(tempDirFile, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        backDateFile10Minutes(testFile1);

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile1Size, destination.length(), "Check Full copy");
        assertEquals(FileUtils.lastModified(testFile1), FileUtils.lastModified(destination), "Check last modified date preserved");

        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyFileToDirectory(destination, directory),
            "Should not be able to copy a file into the same directory as itself");
    }

    @Test
    public void testCopyFile2() throws Exception {
        final File destination = new File(tempDirFile, "copy2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        FileUtils.copyFile(testFile1, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile2Size, destination.length(), "Check Full copy");
        assertEquals(getLastModifiedMillis(testFile1) , getLastModifiedMillis(destination), "Check last modified date preserved");
    }

    @Test
    public void testCopyFile2ToDir() throws Exception {
        final File directory = new File(tempDirFile, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        backDateFile10Minutes(testFile1);

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile2Size, destination.length(), "Check Full copy");
        assertEquals(FileUtils.lastModified(testFile1), FileUtils.lastModified(destination), "Check last modified date preserved");
    }

    @Test
    public void testCopyFile2WithoutFileDatePreservation() throws Exception {
        final File destFile = new File(tempDirFile, "copy2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        // destination file time should not be less than this (allowing for granularity)
        final long nowMillis = System.currentTimeMillis() - 1000L;
        // On Windows, the last modified time is copied by default.
        FileUtils.copyFile(testFile1, destFile, false);
        assertTrue(destFile.exists(), "Check Exist");
        assertEquals(testFile1Size, destFile.length(), "Check Full copy");
        final long destLastModMillis = getLastModifiedMillis(destFile);
        final long unexpectedMillis = getLastModifiedMillis(testFile1);
        if (!SystemUtils.IS_OS_WINDOWS) {
            final long deltaMillis = destLastModMillis - unexpectedMillis;
            assertNotEquals(unexpectedMillis, destLastModMillis,
                "Check last modified date not same as input, delta " + deltaMillis);
            assertTrue(destLastModMillis > nowMillis,
                destLastModMillis + " > " + nowMillis + " (delta " + deltaMillis + ")");
        }
    }

    @Test
    @Disabled
    public void testCopyFileLarge() throws Exception {

        final File largeFile = new File(tempDirFile, "large.txt");
        final File destination = new File(tempDirFile, "copylarge.txt");

        if (!largeFile.getParentFile().exists()) {
            fail("Cannot create file " + largeFile
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(largeFile.toPath()))) {
            TestUtils.generateTestData(output, FileUtils.ONE_GB);
        }
        FileUtils.copyFile(largeFile, destination);

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
    public void testCopyFileToReadOnlyDirectory() throws Exception {
        final File directory = new File(tempDirFile, "readonly");
        if (!directory.exists()) {
            assumeTrue(directory.mkdirs());
        }
        assumeTrue(directory.setWritable(false));

        assertThrows(IOException.class, () -> FileUtils.copyFileToDirectory(testFile1, directory),
            "Should not be able to copy a file into a readonly directory");
    }

    @Test
    public void testCopyToDirectoryWithDirectory() throws IOException {
        final File destDirectory = new File(tempDirFile, "destination");
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }

        // Create a test directory
        final File inputDirectory = new File(tempDirFile, "input");
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
        final File directory = new File(tempDirFile, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        FileUtils.copyToDirectory(testFile1, directory);
        assertTrue(destination.exists(), "Check Exists");
        assertEquals(testFile1Size, destination.length(), "Check Full Copy");
    }

    @Test
    public void testCopyToDirectoryWithFileSourceDoesNotExist() {
        assertThrows(IOException.class,
                () -> FileUtils.copyToDirectory(new File(tempDirFile, "doesNotExists"), tempDirFile));
    }

    @Test
    public void testCopyToDirectoryWithFileSourceIsNull() {
        assertThrows(NullPointerException.class, () -> FileUtils.copyToDirectory((File) null, tempDirFile));
    }

    @Test
    public void testCopyToDirectoryWithIterable() throws IOException {
        final File directory = new File(tempDirFile, "subdir");
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
        assertThrows(IOException.class, () -> FileUtils.copyToDirectory(Collections.singleton(new File(tempDirFile, "doesNotExists")), tempDirFile));
    }

    @Test
    public void testCopyToDirectoryWithIterableSourceIsNull() {
        assertThrows(NullPointerException.class, () -> FileUtils.copyToDirectory((List<File>) null, tempDirFile));
    }

    @Test
    public void testCopyToSelf() throws Exception {
        final File destination = new File(tempDirFile, "copy3.txt");
        //Prepare a test file
        FileUtils.copyFile(testFile1, destination);
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyFile(destination, destination));
    }

    @Test
    public void testCopyURLToFile() throws Exception {
        // Creates file
        final File file = new File(tempDirFile, getName());
        assertContentMatchesAfterCopyURLToFileFor("/java/lang/Object.class", file);
        //TODO Maybe test copy to itself like for copyFile()
    }

    @Test
    public void testCopyURLToFileCreatesParentDirs() throws Exception {
        final File file = managedTempDirPath.resolve("subdir").resolve(getName()).toFile();
        assertContentMatchesAfterCopyURLToFileFor("/java/lang/Object.class", file);
    }

    @Test
    public void testCopyURLToFileReplacesExisting() throws Exception {
        final File file = new File(tempDirFile, getName());
        assertContentMatchesAfterCopyURLToFileFor("/java/lang/Object.class", file);
        assertContentMatchesAfterCopyURLToFileFor("/java/lang/String.class", file);
    }

    @Test
    public void testCopyURLToFileWithTimeout() throws Exception {
        // Creates file
        final File file = new File(tempDirFile, "testCopyURLToFileWithTimeout");

        // Loads resource
        final String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile(getClass().getResource(resourceName), file, 500, 500);

        // Tests that resource was copied correctly
        try (InputStream fis = Files.newInputStream(file.toPath());
             InputStream resStream = getClass().getResourceAsStream(resourceName);) {
            assertTrue(IOUtils.contentEquals(resStream, fis), "Content is not equal.");
        }
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @Test
    public void testCountFolders1FileSize0() {
        assertEquals(0, FileUtils.sizeOfDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0").toFile()));
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    public void testCountFolders1FileSize1() {
        assertEquals(1, FileUtils.sizeOfDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1").toFile()));
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @Test
    public void testCountFolders2FileSize2() {
        assertEquals(2, FileUtils.sizeOfDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2").toFile()));
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @Test
    public void testCountFolders2FileSize4() {
        assertEquals(8, FileUtils.sizeOfDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-4").toFile()));
    }

    @Test
    public void testCreateParentDirectories() throws IOException {
        // If a directory already exists, nothing happens.
        FileUtils.createParentDirectories(FileUtils.current());
        // null is a noop
        FileUtils.createParentDirectories(null);
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
    public void testDelete() throws Exception {
        assertEquals(testFile1, FileUtils.delete(testFile1));
        assertThrows(IOException.class, () -> FileUtils.delete(new File("does not exist.nope")));
    }

    @Test
    public void testDeleteDirectoryFailsOnFile() {
        // Fail request to delete a directory for a file
        assertThrows(IllegalArgumentException.class, () -> FileUtils.deleteDirectory(testFile1));
    }

    @Test
    public void testDeleteDirectoryNoopIfAbsent() {
        // Noop on non-existent entry
        assertDoesNotThrow(() -> FileUtils.deleteDirectory(new File("does not exist.nope")));
    }

    @Test
    public void testDeleteDirectorySymbolicLink() throws IOException {
        final Path symlinkedDir = createTempSymbolicLinkedRelativeDir().getLeft();
        FileUtils.deleteDirectory(symlinkedDir.toFile());
        assertFalse(Files.exists(symlinkedDir));
    }

    @Test
    public void testDeleteDirectorySymbolicLinkAbsent() throws IOException {
        final ImmutablePair<Path, Path> pair = createTempSymbolicLinkedRelativeDir();
        final Path symlinkedDir = pair.getLeft();
        final Path targetDir = pair.getRight();
        assertTrue(Files.exists(symlinkedDir), symlinkedDir::toString);
        Files.delete(symlinkedDir);
        assertTrue(Files.exists(targetDir), targetDir::toString);
        assertFalse(Files.exists(symlinkedDir), symlinkedDir::toString);
        // actual test
        FileUtils.deleteDirectory(symlinkedDir.toFile());
        assertFalse(Files.exists(symlinkedDir), symlinkedDir::toString);
    }

    @Test
    public void testDeleteDirectorySymbolicLinkAbsentDeepTarget() throws IOException {
        final ImmutablePair<Path, Path> pair = createTempSymbolicLinkedRelativeDir();
        final Path symLinkedDir = pair.getLeft();
        final Path targetDir = pair.getRight();
        // more setup
        final Path targetDir2 = targetDir.resolve("subdir2");
        final Path symLinkedDir2 = targetDir.resolve("symlinked-dir2");
        Files.createDirectory(targetDir2);
        Files.createSymbolicLink(symLinkedDir2, targetDir2);
        assertTrue(Files.exists(symLinkedDir2), symLinkedDir2::toString);
        // remove target directory, keeping symbolic link
        Files.delete(targetDir2);
        assertFalse(Files.exists(targetDir2), targetDir2::toString);
        assertFalse(Files.exists(symLinkedDir2), symLinkedDir2::toString);
        // actual test
        FileUtils.deleteDirectory(targetDir.toFile());
        assertFalse(Files.exists(targetDir), targetDir::toString);
    }

    @Test
    public void testDeleteDirectorySymbolicLinkAbsentTarget() throws IOException {
        final ImmutablePair<Path, Path> pair = createTempSymbolicLinkedRelativeDir();
        final Path symlinkedDir = pair.getLeft();
        final Path targetDir = pair.getRight();
        assertTrue(Files.exists(symlinkedDir), symlinkedDir::toString);
        // remove target directory, keeping symbolic link
        Files.delete(targetDir);
        assertFalse(Files.exists(targetDir), targetDir::toString);
        assertFalse(Files.exists(symlinkedDir), symlinkedDir::toString);
        // actual test
        FileUtils.deleteDirectory(symlinkedDir.toFile());
        assertFalse(Files.exists(symlinkedDir), symlinkedDir::toString);
    }

    @Test
    public void testDeleteQuietlyDir() throws IOException {
        final File testDirectory = new File(tempDirFile, "testDeleteQuietlyDir");
        final File testFile = new File(testDirectory, "testDeleteQuietlyFile");
        testDirectory.mkdirs();
        if (!testFile.getParentFile().exists()) {
            fail("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }

        assertTrue(testDirectory.exists());
        assertTrue(testFile.exists());
        FileUtils.deleteQuietly(testDirectory);
        assertFalse(testDirectory.exists(), "Check No Exist");
        assertFalse(testFile.exists(), "Check No Exist");
    }

    @Test
    public void testDeleteQuietlyFile() throws IOException {
        final File testFile = new File(tempDirFile, "testDeleteQuietlyFile");
        if (!testFile.getParentFile().exists()) {
            fail("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }

        assertTrue(testFile.exists());
        FileUtils.deleteQuietly(testFile);
        assertFalse(testFile.exists(), "Check No Exist");
    }

    @Test
    public void testDeleteQuietlyForNull() {
        FileUtils.deleteQuietly(null);
    }

    @Test
    public void testDeleteQuietlyNonExistent() {
        final File testFile = new File("testDeleteQuietlyNonExistent");
        assertFalse(testFile.exists());
        FileUtils.deleteQuietly(testFile);
    }

    /**
     * Tests the FileUtils implementation.
     */
    @Test
    public void testFileUtils() throws Exception {
        // Loads file from classpath
        final File file1 = new File(tempDirFile, "test.txt");
        final String fileName = file1.getAbsolutePath();

        //Create test file on-the-fly (used to be in CVS)
        try (OutputStream out = Files.newOutputStream(file1.toPath())) {
            out.write("This is a test".getBytes(StandardCharsets.UTF_8));
        }

        final File file2 = new File(tempDirFile, "test2.txt");

        FileUtils.writeStringToFile(file2, fileName, UTF_8);
        assertTrue(file2.exists());
        assertTrue(file2.length() > 0);

        final String file2contents = FileUtils.readFileToString(file2, UTF_8);
        assertEquals(fileName, file2contents, "Second file's contents correct");

        assertTrue(file2.delete());

        final String contents = FileUtils.readFileToString(new File(fileName), UTF_8);
        assertEquals("This is a test", contents, "FileUtils.fileRead()");

    }

    @Test
    public void testForceDeleteAFile1() throws Exception {
        final File destination = new File(tempDirFile, "copy1.txt");
        destination.createNewFile();
        assertTrue(destination.exists(), "Copy1.txt doesn't exist to delete");
        FileUtils.forceDelete(destination);
        assertFalse(destination.exists(), "Check No Exist");
    }

    @Test
    public void testForceDeleteAFile2() throws Exception {
        final File destination = new File(tempDirFile, "copy2.txt");
        destination.createNewFile();
        assertTrue(destination.exists(), "Copy2.txt doesn't exist to delete");
        FileUtils.forceDelete(destination);
        assertFalse(destination.exists(), "Check No Exist");
    }

    @Test
    public void testForceDeleteAFileDoesNotExist() {
        final File destination = new File(tempDirFile, "no_such_file");
        assertFalse(destination.exists(), "Check No Exist");
        assertThrowsExactly(FileNotFoundException.class, () -> FileUtils.forceDelete(destination));

    }

    @Test
    public void testForceDeleteDir() throws Exception {
        final File testDirectory = tempDirFile;
        assertTrue(testDirectory.exists(), "TestDirectory must exist");
        FileUtils.forceDelete(testDirectory);
        assertFalse(testDirectory.exists(), "TestDirectory must not exist");
    }

    /**
     * TODO Passes on macOS, fails on Linux and Windows with AccessDeniedException.
     */
    @Test
    @EnabledOnOs(value = OS.MAC)
    public void testForceDeleteReadOnlyDirectory() throws Exception {
        try (TempDirectory destDir = TempDirectory.create("dir-");
                TempFile destination = TempFile.create(destDir, "test-", ".txt")) {
            // sanity check structure
            assertTrue(Files.isDirectory(destDir.get()));
            assertEquals(destDir.get(), destination.get().getParent());
            // sanity check attributes
            final File file = destination.toFile();
            assertTrue(file.setReadOnly());
            assertTrue(file.canRead());
            assertFalse(file.canWrite());
            // sanity check that File.delete() deletes a read-only directory.
            final PathCounters delete = destDir.delete();
            assertEquals(1, delete.getDirectoryCounter().get());
            assertEquals(1, delete.getFileCounter().get());
            assertFalse(file.exists());
            assertFalse(destDir.exists());
        }
        try (TempDirectory destDir = TempDirectory.create("dir-");
                TempFile destination = TempFile.create(destDir, "test-", ".txt")) {
            // sanity check structure
            assertTrue(Files.isDirectory(destDir.get()));
            assertEquals(destDir.get(), destination.get().getParent());
            // sanity check attributes
            final File dir = destDir.toFile();
            // real test
            assertTrue(dir.setReadOnly());
            assertTrue(dir.canRead());
            assertFalse(dir.canWrite());
            assertTrue(dir.exists(), "File doesn't exist to delete");
            // TODO Passes on macOS, fails on Linux and Windows with AccessDeniedException.
            FileUtils.forceDelete(dir);
            assertFalse(destination.exists(), "Check deletion");
            assertFalse(dir.exists(), "Check deletion");
            assertFalse(destDir.exists(), "Check deletion");
        }
    }

    @Test
    public void testForceDeleteReadOnlyFile() throws Exception {
        try (TempFile destination = TempFile.create("test-", ".txt")) {
            final File file = destination.toFile();
            assertTrue(file.setReadOnly());
            assertTrue(file.canRead());
            assertFalse(file.canWrite());
            // sanity check that File.delete() deletes read-only files.
            assertTrue(file.delete());
        }
        try (TempFile destination = TempFile.create("test-", ".txt")) {
            final File file = destination.toFile();
            // real test
            assertTrue(file.setReadOnly());
            assertTrue(file.canRead());
            assertFalse(file.canWrite());
            assertTrue(file.exists(), "File doesn't exist to delete");
            FileUtils.forceDelete(file);
            assertFalse(file.exists(), "Check deletion");
        }
    }

    /**
     * TODO Passes on macOS, fails on Linux and Windows with AccessDeniedException.
     */
    @Test
    @EnabledOnOs(value = OS.MAC)
    public void testForceDeleteUnwritableDirectory() throws Exception {
        try (TempDirectory destDir = TempDirectory.create("dir-");
                TempFile file = TempFile.create(destDir, "test-", ".txt")) {
            // sanity check structure
            assertTrue(Files.isDirectory(destDir.get()));
            assertEquals(destDir.get(), file.get().getParent());
            // sanity check attributes
            final File dir = destDir.toFile();
            assertTrue(dir.canWrite());
            // Windows: setWritable(false) returns false.
            assertTrue(dir.setWritable(false), () -> "setWritable(false) on " + dir);
            assertFalse(dir.canWrite());
            assertTrue(dir.canRead());
            // sanity check that File.delete() cannot delete non-empty directories.
            assertFalse(dir.delete());
            // delete underlying file.
            assertFalse(file.toFile().delete());
            // reset attribute so we can delete file and auto-close/delete dir.
            assertTrue(dir.setWritable(true));
            assertTrue(file.toFile().delete());
        }
        try (TempDirectory destDir = TempDirectory.create("dir-");
                TempFile file = TempFile.create(destDir, "test-", ".txt")) {
            // sanity check structure
            assertTrue(Files.isDirectory(destDir.get()));
            assertEquals(destDir.get(), file.get().getParent());
            // sanity check attributes
            final File dir = destDir.toFile();
            assertTrue(dir.canWrite());
            assertTrue(dir.setWritable(false));
            assertFalse(dir.canWrite());
            assertTrue(dir.canRead());
            // sanity check that File.delete() cannot delete non-empty directories.
            assertFalse(dir.delete());
            // test
            // Linux: Fails with AccessDeniedException.
            FileUtils.forceDelete(dir);
            assertFalse(file.exists());
            assertFalse(dir.exists());
        }
    }

    @Test
    public void testForceDeleteUnwritableFile() throws Exception {
        try (TempFile destination = TempFile.create("test-", ".txt")) {
            final File file = destination.toFile();
            assertTrue(file.canWrite());
            assertTrue(file.setWritable(false));
            assertFalse(file.canWrite());
            assertTrue(file.canRead());
            // sanity check that File.delete() deletes unwritable files.
            assertTrue(file.delete());
        }
        try (TempFile destination = TempFile.create("test-", ".txt")) {
            final File file = destination.toFile();
            // real test
            assertTrue(file.canWrite());
            assertTrue(file.setWritable(false));
            assertFalse(file.canWrite());
            assertTrue(file.canRead());
            assertTrue(file.exists(), "File doesn't exist to delete");
            FileUtils.forceDelete(file);
            assertFalse(file.exists(), "Check deletion");
        }
    }

    @Test
    public void testForceMkdir() throws Exception {
        // Tests with existing directory
        FileUtils.forceMkdir(tempDirFile);

        // Creates test file
        final File testFile = new File(tempDirFile, getName());
        testFile.createNewFile();
        assertTrue(testFile.exists(), "Test file does not exist.");

        // Tests with existing file
        assertThrows(IOException.class, () -> FileUtils.forceMkdir(testFile));

        testFile.delete();

        // Tests with non-existent directory
        FileUtils.forceMkdir(testFile);
        assertTrue(testFile.exists(), "Directory was not created.");

        // noop
        FileUtils.forceMkdir(null);
    }

    @Test
    public void testForceMkdirParent() throws Exception {
        // Tests with existing directory
        assertTrue(tempDirFile.exists());
        final File testParentDir = new File(tempDirFile, "testForceMkdirParent");
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

    @Test
    public void testGetFile() {
        final File expected_A = new File("src");
        final File expected_B = new File(expected_A, "main");
        final File expected_C = new File(expected_B, "java");
        assertEquals(expected_A, FileUtils.getFile("src"), "A");
        assertEquals(expected_B, FileUtils.getFile("src", "main"), "B");
        assertEquals(expected_C, FileUtils.getFile("src", "main", "java"), "C");
        assertThrows(NullPointerException.class, () -> FileUtils.getFile((String[]) null));

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
        assertThrows(NullPointerException.class, () -> FileUtils.getFile(parent, (String[]) null));
        assertThrows(NullPointerException.class, () -> FileUtils.getFile((File) null, "src"));
    }

    @Test
    public void testGetTempDirectory() {
        final File tempDirectory = new File(FileUtils.getTempDirectoryPath());
        assertEquals(tempDirectory, FileUtils.getTempDirectory());
    }

    @Test
    public void testGetTempDirectoryPath() {
        assertEquals(SystemProperties.getJavaIoTmpdir(), FileUtils.getTempDirectoryPath());
    }

    @Test
    public void testGetUserDirectory() {
        final File userDirectory = new File(SystemProperties.getUserHome());
        assertEquals(userDirectory, FileUtils.getUserDirectory());
    }

    @Test
    public void testGetUserDirectoryPath() {
        assertEquals(SystemProperties.getUserHome(), FileUtils.getUserDirectoryPath());
    }

    @Test
    public void testIO276() throws Exception {
        final File dir = new File("target", "IO276");
        Files.deleteIfExists(dir.toPath());
        assertTrue(dir.mkdirs(), dir + " should not be present");
        final File file = new File(dir, "IO276.txt");
        assertTrue(file.createNewFile(), file + " should not be present");
        FileUtils.forceDeleteOnExit(dir);
        // If this does not work, test will fail next time (assuming target is not cleaned)
    }

    @Test
    public void testIO300() {
        final File testDirectory = tempDirFile;
        final File src = new File(testDirectory, "dir1");
        final File dest = new File(src, "dir2");
        assertTrue(dest.mkdirs());
        assertTrue(src.exists());
        assertThrows(IOException.class, () -> FileUtils.moveDirectoryToDirectory(src, dest, false));
        assertTrue(src.exists());
    }

    @Test
    public void testIO575() throws IOException {
        final Path sourceDir = Files.createTempDirectory("source-dir");
        final String fileName = "some-file";
        final Path sourceFile = Files.createFile(sourceDir.resolve(fileName));

        assertEquals(SystemUtils.IS_OS_WINDOWS, sourceFile.toFile().canExecute());

        sourceFile.toFile().setExecutable(true);

        assertTrue(sourceFile.toFile().canExecute());

        final Path destDir = Files.createTempDirectory("some-empty-destination");

        FileUtils.copyDirectory(sourceDir.toFile(), destDir.toFile());

        final Path destFile = destDir.resolve(fileName);

        assertTrue(destFile.toFile().exists());
        assertTrue(destFile.toFile().canExecute());
    }

    @Test
    public void testIsDirectory() throws IOException {
        assertFalse(FileUtils.isDirectory(null));

        assertTrue(FileUtils.isDirectory(tempDirFile));
        assertFalse(FileUtils.isDirectory(testFile1));

        final File tempDirAsFile;
        try (TempDirectory tempDir = TempDirectory.create(getClass().getCanonicalName())) {
            tempDirAsFile = tempDir.toFile();
            assertTrue(FileUtils.isDirectory(tempDirAsFile));
        }
        assertFalse(FileUtils.isDirectory(tempDirAsFile));
    }

    @Test
    public void testIsEmptyDirectory() throws IOException {
        try (TempDirectory tempDir = TempDirectory.create(getClass().getCanonicalName())) {
            final File tempDirAsFile = tempDir.toFile();
            Assertions.assertTrue(FileUtils.isEmptyDirectory(tempDirAsFile));
        }
        Assertions.assertFalse(FileUtils.isEmptyDirectory(DIR_SIZE_1.toFile()));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L})
    public void testIsFileNewerOlder(final long millis) throws Exception {
        // Files
        final File oldFile = new File(tempDirFile, "FileUtils-old.txt");
        final File refFile = new File(tempDirFile, "FileUtils-reference.txt");
        final File newFile = new File(tempDirFile, "FileUtils-new.txt");
        final File invalidFile = new File(tempDirFile, "FileUtils-invalid-file.txt");
        // Paths
        final Path oldPath = oldFile.toPath();
        final Path refPath = refFile.toPath();
        final Path newPath = newFile.toPath();
        // FileTimes
        // TODO What is wrong with Java 8 on macOS? Or is this a macOS file system issue?
        final long actualMillis = SystemUtils.IS_OS_MAC && SystemUtils.IS_JAVA_1_8 ? millis + 1000 : millis;
        final FileTime oldFileTime = FileTime.from(actualMillis * 1, TimeUnit.MILLISECONDS);
        final FileTime refFileTime = FileTime.from(actualMillis * 2, TimeUnit.MILLISECONDS);
        final FileTime testFileTime = FileTime.from(actualMillis * 3, TimeUnit.MILLISECONDS);
        final FileTime newFileTime = FileTime.from(actualMillis * 4, TimeUnit.MILLISECONDS);

        // Create fixtures
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(oldPath))) {
            TestUtils.generateTestData(output, 0);
        }
        Files.setLastModifiedTime(oldPath, oldFileTime);

        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(refPath))) {
            TestUtils.generateTestData(output, 0);
        }
        Files.setLastModifiedTime(refPath, refFileTime);

        final Date date = new Date(testFileTime.toMillis());
        final long now = date.getTime();
        final Instant instant = date.toInstant();
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        final OffsetDateTime offsetDateTime = zonedDateTime.toOffsetDateTime();
        final LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        final LocalDate localDate = zonedDateTime.toLocalDate();
        final LocalDate localDatePlusDay = localDate.plusDays(1);
        final LocalTime localTime0 = LocalTime.MIDNIGHT;
        final OffsetTime offsetTime0 = OffsetTime.of(localTime0, ZoneOffset.UTC);

        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(newPath))) {
            TestUtils.generateTestData(output, 0);
        }
        Files.setLastModifiedTime(newPath, newFileTime);

        // Test
        assertFalse(FileUtils.isFileNewer(oldFile, refFile), "Old File - Newer - File");
        assertFalse(FileUtils.isFileNewer(oldFile, date), "Old File - Newer - Date");
        assertFalse(FileUtils.isFileNewer(oldFile, now), "Old File - Newer - Mili");
        assertFalse(FileUtils.isFileNewer(oldFile, instant), "Old File - Newer - Instant");
        assertFalse(FileUtils.isFileNewer(oldFile, zonedDateTime), "Old File - Newer - ZonedDateTime");
        assertFalse(FileUtils.isFileNewer(oldFile, offsetDateTime), "Old File - Newer - OffsetDateTime");
        assertFalse(FileUtils.isFileNewer(oldFile, localDateTime), "Old File - Newer - LocalDateTime");
        assertFalse(FileUtils.isFileNewer(oldFile, localDateTime, ZoneId.systemDefault()), "Old File - Newer - LocalDateTime,ZoneId");
        assertFalse(FileUtils.isFileNewer(oldFile, localDate), "Old File - Newer - LocalDate");
        assertTrue(FileUtils.isFileNewer(oldFile, localDate, localTime0), "Old File - Newer - LocalDate,LocalTime");
        assertTrue(FileUtils.isFileNewer(oldFile, localDate, offsetTime0), "Old File - Newer - LocalDate,OffsetTime");
        assertFalse(FileUtils.isFileNewer(oldFile, localDatePlusDay), "Old File - Newer - LocalDate plus one day");
        assertFalse(FileUtils.isFileNewer(oldFile, localDatePlusDay, localTime0), "Old File - Newer - LocalDate plus one day,LocalTime");
        assertFalse(FileUtils.isFileNewer(oldFile, localDatePlusDay, offsetTime0), "Old File - Newer - LocalDate plus one day,OffsetTime");

        assertTrue(FileUtils.isFileNewer(newFile, refFile), "New File - Newer - File");
        assertTrue(FileUtils.isFileNewer(newFile, date), "New File - Newer - Date");
        assertTrue(FileUtils.isFileNewer(newFile, now), "New File - Newer - Mili");
        assertTrue(FileUtils.isFileNewer(newFile, instant), "New File - Newer - Instant");
        assertTrue(FileUtils.isFileNewer(newFile, zonedDateTime), "New File - Newer - ZonedDateTime");
        assertTrue(FileUtils.isFileNewer(newFile, offsetDateTime), "New File - Newer - OffsetDateTime");
        assertTrue(FileUtils.isFileNewer(newFile, localDateTime), "New File - Newer - LocalDateTime");
        assertTrue(FileUtils.isFileNewer(newFile, localDateTime, ZoneId.systemDefault()), "New File - Newer - LocalDateTime,ZoneId");
        assertFalse(FileUtils.isFileNewer(newFile, localDate), "New File - Newer - LocalDate");
        assertTrue(FileUtils.isFileNewer(newFile, localDate, localTime0), "New File - Newer - LocalDate,LocalTime");
        assertTrue(FileUtils.isFileNewer(newFile, localDate, offsetTime0), "New File - Newer - LocalDate,OffsetTime");
        assertFalse(FileUtils.isFileNewer(newFile, localDatePlusDay), "New File - Newer - LocalDate plus one day");
        assertFalse(FileUtils.isFileNewer(newFile, localDatePlusDay, localTime0), "New File - Newer - LocalDate plus one day,LocalTime");
        assertFalse(FileUtils.isFileNewer(newFile, localDatePlusDay, offsetTime0), "New File - Newer - LocalDate plus one day,OffsetTime");
        assertFalse(FileUtils.isFileNewer(invalidFile, refFile), "Illegal - Newer - File");
        assertThrows(UncheckedIOException.class, () -> FileUtils.isFileNewer(newFile, invalidFile));

        // Test isFileOlder()
        assertTrue(FileUtils.isFileOlder(oldFile, refFile), "Old File - Older - File");
        assertTrue(FileUtils.isFileOlder(oldFile, date), "Old File - Older - Date");
        assertTrue(FileUtils.isFileOlder(oldFile, now), "Old File - Older - Mili");
        assertTrue(FileUtils.isFileOlder(oldFile, instant), "Old File - Older - Instant");
        assertTrue(FileUtils.isFileOlder(oldFile, zonedDateTime), "Old File - Older - ZonedDateTime");
        assertTrue(FileUtils.isFileOlder(oldFile, offsetDateTime), "Old File - Older - OffsetDateTime");
        assertTrue(FileUtils.isFileOlder(oldFile, localDateTime), "Old File - Older - LocalDateTime");
        assertTrue(FileUtils.isFileOlder(oldFile, localDateTime, ZoneId.systemDefault()), "Old File - Older - LocalDateTime,LocalTime");
        assertTrue(FileUtils.isFileOlder(oldFile, localDate), "Old File - Older - LocalDate");
        assertFalse(FileUtils.isFileOlder(oldFile, localDate, localTime0), "Old File - Older - LocalDate,LocalTime");
        assertFalse(FileUtils.isFileOlder(oldFile, localDate, offsetTime0), "Old File - Older - LocalDate,OffsetTime");
        assertTrue(FileUtils.isFileOlder(oldFile, localDatePlusDay), "Old File - Older - LocalDate plus one day");
        assertTrue(FileUtils.isFileOlder(oldFile, localDatePlusDay, localTime0), "Old File - Older - LocalDate plus one day,LocalTime");
        assertTrue(FileUtils.isFileOlder(oldFile, localDatePlusDay, offsetTime0), "Old File - Older - LocalDate plus one day,OffsetTime");

        assertFalse(FileUtils.isFileOlder(newFile, refFile), "New File - Older - File");
        assertFalse(FileUtils.isFileOlder(newFile, date), "New File - Older - Date");
        assertFalse(FileUtils.isFileOlder(newFile, now), "New File - Older - Mili");
        assertFalse(FileUtils.isFileOlder(newFile, instant), "New File - Older - Instant");
        assertFalse(FileUtils.isFileOlder(newFile, zonedDateTime), "New File - Older - ZonedDateTime");
        assertFalse(FileUtils.isFileOlder(newFile, offsetDateTime), "New File - Older - OffsetDateTime");
        assertFalse(FileUtils.isFileOlder(newFile, localDateTime), "New File - Older - LocalDateTime");
        assertFalse(FileUtils.isFileOlder(newFile, localDateTime, ZoneId.systemDefault()), "New File - Older - LocalDateTime,ZoneId");
        assertTrue(FileUtils.isFileOlder(newFile, localDate), "New File - Older - LocalDate");
        assertFalse(FileUtils.isFileOlder(newFile, localDate, localTime0), "New File - Older - LocalDate,LocalTime");
        assertFalse(FileUtils.isFileOlder(newFile, localDate, offsetTime0), "New File - Older - LocalDate,OffsetTime");
        assertTrue(FileUtils.isFileOlder(newFile, localDatePlusDay), "New File - Older - LocalDate plus one day");
        assertTrue(FileUtils.isFileOlder(newFile, localDatePlusDay, localTime0), "New File - Older - LocalDate plus one day,LocalTime");
        assertTrue(FileUtils.isFileOlder(newFile, localDatePlusDay, offsetTime0), "New File - Older - LocalDate plus one day,OffsetTime");

        assertFalse(FileUtils.isFileOlder(invalidFile, refFile), "Illegal - Older - File");
        assertThrows(UncheckedIOException.class, () -> FileUtils.isFileOlder(newFile, invalidFile));

        // Null File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(null, now));

        // Null reference File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(oldFile, (File) null));

        // Invalid reference File
        assertThrows(UncheckedIOException.class, () -> FileUtils.isFileNewer(oldFile, invalidFile));

        // Null reference Date
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(oldFile, (Date) null));

        // Test isFileOlder() exceptions
        // Null File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(null, now));

        // Null reference File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(oldFile, (File) null));

        // Null reference Date
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(oldFile, (Date) null));

        // Invalid reference File
        assertThrows(UncheckedIOException.class, () -> FileUtils.isFileOlder(oldFile, invalidFile));
    }

    @Test
    public void testIsRegularFile() throws IOException {
        assertFalse(FileUtils.isRegularFile(null));

        assertFalse(FileUtils.isRegularFile(tempDirFile));
        assertTrue(FileUtils.isRegularFile(testFile1));

        Files.delete(testFile1.toPath());
        assertFalse(FileUtils.isRegularFile(testFile1));
    }

    @Test
    public void testIterateFiles() throws Exception {
        final File srcDir = tempDirFile;
        final File subDir = new File(srcDir, "list_test");
        final File subSubDir = new File(subDir, "subSubDir");
        final File notSubSubDir = new File(subDir, "notSubSubDir");
        assertTrue(subDir.mkdir());
        assertTrue(subSubDir.mkdir());
        assertTrue(notSubSubDir.mkdir());
        Iterator<File> iterator = null;
        try {
            // Need list to be appendable
            final List<String> expectedFileNames = new ArrayList<>(
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

            final WildcardFileFilter allFilesFileFilter = WildcardFileFilter.builder().setWildcards("*.*").get();
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
        final File srcDir = tempDirFile;
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
        final WildcardFileFilter fileFilterAllFiles =  WildcardFileFilter.builder().setWildcards("*.*").get();
        final WildcardFileFilter fileFilterAllDirs = WildcardFileFilter.builder().setWildcards("*").get();
        final WildcardFileFilter fileFilterExtTxt = WildcardFileFilter.builder().setWildcards("*.txt").get();
        try {
            try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(someFile.toPath()))) {
                TestUtils.generateTestData(output, 100);
            }
            //
            // "*.*" and "*"
            Collection<File> expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile, subDir3, subDir4);
            iterateFilesAndDirs(subDir1, fileFilterAllFiles, fileFilterAllDirs, expectedFilesAndDirs);
            //
            // "*.txt" and "*"
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
    public void testIterateFilesOnlyNoDirs() throws IOException {
        final File directory = tempDirFile;
        assertTrue(new File(directory, "TEST").mkdir());
        assertTrue(new File(directory, "test.txt").createNewFile());

        final IOFileFilter filter = WildcardFileFilter.builder().setWildcards("*").setIoCase(IOCase.INSENSITIVE).get();
        FileUtils.iterateFiles(directory, filter, null).forEachRemaining(file -> assertFalse(file.isDirectory(), file::getAbsolutePath));
    }

    @Test
    public void testListFiles() throws Exception {
        final File srcDir = tempDirFile;
        final File subDir = new File(srcDir, "list_test");
        final File subDir2 = new File(subDir, "subdir");
        subDir.mkdir();
        subDir2.mkdir();
        try {

            final String[] expectedFileNames = { "a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt" };
            final int[] fileSizes = { 123, 234, 345, 456, 678, 789 };

            for (int i = 0; i < expectedFileNames.length; ++i) {
                final File theFile = new File(subDir, expectedFileNames[i]);
                if (!theFile.getParentFile().exists()) {
                    fail("Cannot create file " + theFile + " as the parent directory does not exist");
                }
                try (BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(theFile.toPath()))) {
                    TestUtils.generateTestData(output, fileSizes[i]);
                }
            }

            // @formatter:off
            final Collection<File> actualFiles = FileUtils.listFiles(subDir,
                    WildcardFileFilter.builder().setWildcards("*.*").get(),
                    WildcardFileFilter.builder().setWildcards("*").get());
            // @formatter:on

            final int count = actualFiles.size();
            final Object[] fileObjs = actualFiles.toArray();

            assertEquals(expectedFileNames.length, actualFiles.size(), actualFiles::toString);

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

            assertEquals(foundFileNames.size(), expectedFileNames.length, foundFileNames::toString);
        } finally {
            subDir.delete();
        }
    }

    @Test
    public void testListFilesOnlyNoDirs() throws IOException {
        final File directory = tempDirFile;
        assertTrue(new File(directory, "TEST").mkdir());
        assertTrue(new File(directory, "test.txt").createNewFile());

        final IOFileFilter filter = WildcardFileFilter.builder().setWildcards("*").setIoCase(IOCase.INSENSITIVE).get();
        for (final File file : FileUtils.listFiles(directory, filter, null)) {
            assertFalse(file.isDirectory(), file::getAbsolutePath);
        }
    }

    @Test
    public void testListFilesWithDirs() throws IOException {
        final File srcDir = tempDirFile;

        final File subDir1 = new File(srcDir, "subdir");
        final File subDir2 = new File(subDir1, "subdir2");
        subDir1.mkdir();
        subDir2.mkdir();
        try {
            final File someFile = new File(subDir2, "a.txt");
            if (!someFile.getParentFile().exists()) {
                fail("Cannot create file " + someFile + " as the parent directory does not exist");
            }
            try (BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(someFile.toPath()))) {
                TestUtils.generateTestData(output, 100);
            }

            final File subDir3 = new File(subDir2, "subdir3");
            subDir3.mkdir();

            // @formatter:off
            final Collection<File> files = FileUtils.listFilesAndDirs(subDir1,
                    WildcardFileFilter.builder().setWildcards("*.*").get(),
                    WildcardFileFilter.builder().setWildcards("*").get());
            // @formatter:on

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

        final File dir = tempDirFile;
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
            fail("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }
        final File destination = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destination);

        // Move the directory
        FileUtils.moveDirectory(src, destination);

        // Check results
        assertTrue(destination.exists(), "Check Exist");
        assertFalse(src.exists(), "Original deleted");
        final File movedDir = new File(destination, testDir.getName());
        final File movedFile = new File(movedDir, testFile.getName());
        assertTrue(movedDir.exists(), "Check dir moved");
        assertTrue(movedFile.exists(), "Check file moved");
    }

    @Test
    public void testMoveDirectory_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectory(null, new File("foo")));
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectory(new File("foo"), null));
        assertThrows(FileNotFoundException.class, () -> FileUtils.moveDirectory(new File("non-existent"), new File("foo")));

        final File testFile = new File(tempDirFile, "testMoveDirectoryFile");
        if (!testFile.getParentFile().exists()) {
            fail("Cannot create file " + testFile + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveDirectory(testFile, new File("foo")));
        final File testSrcFile = new File(tempDirFile, "testMoveDirectorySource");
        final File testDestFile = new File(tempDirFile, "testMoveDirectoryDest");
        testSrcFile.mkdir();
        testDestFile.mkdir();
        assertThrows(FileExistsException.class, () -> FileUtils.moveDirectory(testSrcFile, testDestFile),
            "Expected FileExistsException when dest already exists");

    }

    @Test
    public void testMoveDirectory_Rename() throws Exception {
        final File dir = tempDirFile;
        final File src = new File(dir, "testMoveDirectory1Source");
        final File testDir = new File(src, "foo");
        final File testFile = new File(testDir, "bar");
        testDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            fail("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }
        final File destination = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destination);

        // Move the directory
        FileUtils.moveDirectory(src, destination);

        // Check results
        assertTrue(destination.exists(), "Check Exist");
        assertFalse(src.exists(), "Original deleted");
        final File movedDir = new File(destination, testDir.getName());
        final File movedFile = new File(movedDir, testFile.getName());
        assertTrue(movedDir.exists(), "Check dir moved");
        assertTrue(movedFile.exists(), "Check file moved");
    }

    @Test
    public void testMoveDirectoryToDirectory() throws Exception {
        final File dir = tempDirFile;
        final File src = new File(dir, "testMoveDirectory1Source");
        final File testChildDir = new File(src, "foo");
        final File testFile = new File(testChildDir, "bar");
        testChildDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            fail("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }
        final File destDir = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destDir);
        assertFalse(destDir.exists(), "Check Exist before");

        // Move the directory
        FileUtils.moveDirectoryToDirectory(src, destDir, true);

        // Check results
        assertTrue(destDir.exists(), "Check Exist after");
        assertFalse(src.exists(), "Original deleted");
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
        final File testFile1 = new File(tempDirFile, "testMoveFileFile1");
        final File testFile2 = new File(tempDirFile, "testMoveFileFile2");
        if (!testFile1.getParentFile().exists()) {
            fail("Cannot create file " + testFile1 + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(testFile1.toPath()))) {
            TestUtils.generateTestData(output1, 0);
        }
        if (!testFile2.getParentFile().exists()) {
            fail("Cannot create file " + testFile2 + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile2.toPath()))) {
            TestUtils.generateTestData(output, 0);
        }
        assertThrows(IOException.class, () -> FileUtils.moveDirectoryToDirectory(testFile1, testFile2, true));

        final File nonExistent = new File(tempDirFile, "testMoveFileNonExistent");
        assertThrows(IOException.class, () -> FileUtils.moveDirectoryToDirectory(testFile1, nonExistent, false));
    }

    @Test
    public void testMoveFile_CopyDelete() throws Exception {
        final File destination = new File(tempDirFile, "move2.txt");
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
        assertFalse(src.exists(), "Original deleted");
    }

    @Test
    public void testMoveFile_CopyDelete_Failed() {
        final File destination = new File(tempDirFile, "move3.txt");
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
        assertFalse(destination.exists(), "Check Rollback");
        assertTrue(src.exists(), "Original exists");
    }

    @Test
    public void testMoveFile_CopyDelete_WithFileDatePreservation() throws Exception {
        final File destination = new File(tempDirFile, "move2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        final File src = new File(testFile1.getAbsolutePath()) {
            private static final long serialVersionUID = 1L;

            // Force renameTo to fail, as if destination is on another
            // filesystem
            @Override
            public boolean renameTo(final File f) {
                return false;
            }
        };
        final long expected = getLastModifiedMillis(testFile1);

        FileUtils.moveFile(src, destination, StandardCopyOption.COPY_ATTRIBUTES);
        assertTrue(destination.exists(), "Check Exist");
        assertFalse(src.exists(), "Original deleted");

        final long destLastMod = getLastModifiedMillis(destination);
        final long delta = destLastMod - expected;
        assertEquals(expected, destLastMod, "Check last modified date same as input, delta " + delta);
    }

    @Test
    public void testMoveFile_CopyDelete_WithoutFileDatePreservation() throws Exception {
        final File destination = new File(tempDirFile, "move2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        // destination file time should not be less than this (allowing for granularity)
        final long nowMillis = System.currentTimeMillis() - 1000L;

        final File src = new File(testFile1.getAbsolutePath()) {
            private static final long serialVersionUID = 1L;

            // Force renameTo to fail, as if destination is on another
            // filesystem
            @Override
            public boolean renameTo(final File f) {
                return false;
            }
        };
        final long unexpectedMillis = getLastModifiedMillis(testFile1);

        FileUtils.moveFile(src, destination, PathUtils.EMPTY_COPY_OPTIONS);
        assertTrue(destination.exists(), "Check Exist");
        assertFalse(src.exists(), "Original deleted");

        // On Windows, the last modified time is copied by default.
        if (!SystemUtils.IS_OS_WINDOWS) {
            final long destLastModMillis = getLastModifiedMillis(destination);
            final long deltaMillis = destLastModMillis - unexpectedMillis;
            assertNotEquals(unexpectedMillis, destLastModMillis,
                "Check last modified date not same as input, delta " + deltaMillis);
            assertTrue(destLastModMillis > nowMillis,
                destLastModMillis + " > " + nowMillis + " (delta " + deltaMillis + ")");
        }
    }

    @Test
    public void testMoveFile_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveFile(null, new File("foo")));
        assertThrows(NullPointerException.class, () -> FileUtils.moveFile(new File("foo"), null));
        assertThrows(FileNotFoundException.class, () -> FileUtils.moveFile(new File("non-existent"), new File("foo")));
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveFile(tempDirFile, new File("foo")));
        final File testSourceFile = new File(tempDirFile, "testMoveFileSource");
        final File testDestFile = new File(tempDirFile, "testMoveFileSource");
        if (!testSourceFile.getParentFile().exists()) {
            fail("Cannot create file " + testSourceFile + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(testSourceFile.toPath()));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        assertTrue(testDestFile.getParentFile().exists(), () -> "Cannot create file " + testDestFile + " as the parent directory does not exist");
        final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(testDestFile.toPath()));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        assertThrows(FileExistsException.class, () -> FileUtils.moveFile(testSourceFile, testDestFile),
            "Expected FileExistsException when dest already exists");
    }

    @Test
    public void testMoveFile_Rename() throws Exception {
        final File destination = new File(tempDirFile, "move1.txt");

        FileUtils.moveFile(testFile1, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertFalse(testFile1.exists(), "Original deleted");
    }

    @Test
    public void testMoveFileToDirectory() throws Exception {
        final File destDir = new File(tempDirFile, "moveFileDestDir");
        final File movedFile = new File(destDir, testFile1.getName());
        assertFalse(destDir.exists(), "Check Exist before");
        assertFalse(movedFile.exists(), "Check Exist before");

        FileUtils.moveFileToDirectory(testFile1, destDir, true);
        assertTrue(movedFile.exists(), "Check Exist after");
        assertFalse(testFile1.exists(), "Original deleted");
    }

    @Test
    public void testMoveFileToDirectory_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveFileToDirectory(null, new File("foo"), true));
        assertThrows(NullPointerException.class, () -> FileUtils.moveFileToDirectory(new File("foo"), null, true));
        final File testFile1 = new File(tempDirFile, "testMoveFileFile1");
        final File testFile2 = new File(tempDirFile, "testMoveFileFile2");
        if (!testFile1.getParentFile().exists()) {
            fail("Cannot create file " + testFile1 + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output1 = new BufferedOutputStream(Files.newOutputStream(testFile1.toPath()))) {
            TestUtils.generateTestData(output1, 0);
        }
        if (!testFile2.getParentFile().exists()) {
            fail("Cannot create file " + testFile2 + " as the parent directory does not exist");
        }
        final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(testFile2.toPath()));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveFileToDirectory(testFile1, testFile2, true));

        final File nonExistent = new File(tempDirFile, "testMoveFileNonExistent");
        assertThrows(IOException.class, () -> FileUtils.moveFileToDirectory(testFile1, nonExistent, false));
    }

    @Test
    public void testMoveToDirectory() throws Exception {
        final File destDir = new File(tempDirFile, "testMoveToDirectoryDestDir");
        final File testDir = new File(tempDirFile, "testMoveToDirectoryTestDir");
        final File testFile = new File(tempDirFile, "testMoveToDirectoryTestFile");
        testDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            fail("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testFile.toPath()));
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
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectoryToDirectory(null, new File("foo"), true));
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectoryToDirectory(new File("foo"), null, true));
        final File nonExistent = new File(tempDirFile, "non-existent");
        final File destDir = new File(tempDirFile, "MoveToDirectoryDestDir");
        assertThrows(IOException.class, () -> FileUtils.moveToDirectory(nonExistent, destDir, true), "Expected IOException when source does not exist");

    }

    @Test
    public void testOpenInputStream_exists() throws Exception {
        final File file = new File(tempDirFile, "test.txt");
        TestUtils.createLineFileUtf8(file, new String[]{"Hello"});
        try (FileInputStream in = FileUtils.openInputStream(file)) {
            assertEquals('H', in.read());
        }
    }

    @Test
    public void testOpenInputStream_existsButIsDirectory() {
        final File directory = new File(tempDirFile, "subdir");
        directory.mkdirs();
        assertThrows(IOException.class, () -> FileUtils.openInputStream(directory));
    }

    @Test
    public void testOpenInputStream_notExists() {
        final File directory = new File(tempDirFile, "test.txt");
        assertThrows(IOException.class, () -> FileUtils.openInputStream(directory));
    }

    @Test
    public void testOpenOutputStream_exists() throws Exception {
        final File file = new File(tempDirFile, "test.txt");
        TestUtils.createLineFileUtf8(file, new String[]{"Hello"});
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            out.write(0);
        }
        assertTrue(file.exists());
    }

    @Test
    public void testOpenOutputStream_existsButIsDirectory() {
        final File directory = new File(tempDirFile, "subdir");
        directory.mkdirs();
        assertThrows(IllegalArgumentException.class, () -> FileUtils.openOutputStream(directory));
    }

    /**
     * Requires admin privileges on Windows.
     *
     * @throws Exception For example java.nio.file.FileSystemException:
     *                   C:\Users\you\AppData\Local\Temp\junit2324629522183300191\FileUtilsTest8613879743106252609\symlinked-dir: A required privilege is
     *                   not held by the client.
     */
    @Test
    public void testOpenOutputStream_intoExistingSymlinkedDir() throws Exception {
        final Path symlinkedDir = createTempSymbolicLinkedRelativeDir().getLeft();
        final File file = symlinkedDir.resolve("test.txt").toFile();
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            out.write(0);
        }
        assertTrue(file.exists());
    }

    @Test
    public void testOpenOutputStream_noParentCreateFile() throws Exception {
        openOutputStream_noParent(true);
    }

    @Test
    public void testOpenOutputStream_noParentNoFile() throws Exception {
        openOutputStream_noParent(false);
    }

    @Test
    public void testOpenOutputStream_notExists() throws Exception {
        final File file = new File(tempDirFile, "a/test.txt");
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            out.write(0);
        }
        assertTrue(file.exists());
    }

    @Test
    public void testOpenOutputStream_notExistsCannotCreate() {
        // according to Wikipedia, most filing systems have a 256 limit on filename
        final String longStr =
                "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz";  // 300 chars
        final File file = new File(tempDirFile, "a/" + longStr + "/test.txt");
        assertThrows(IOException.class, () -> FileUtils.openOutputStream(file));
    }

    @Test
    public void testReadFileToByteArray() throws Exception {
        final File file = new File(tempDirFile, "read.txt");
        Files.write(file.toPath(), new byte[] {11, 21, 31});

        final byte[] data = FileUtils.readFileToByteArray(file);
        assertEquals(3, data.length);
        assertEquals(11, data[0]);
        assertEquals(21, data[1]);
        assertEquals(31, data[2]);
    }

    @Test
    public void testReadFileToByteArray_Errors() {
        assertThrows(NullPointerException.class, () -> FileUtils.readFileToByteArray(null));
        assertThrows(IOException.class, () -> FileUtils.readFileToByteArray(new File("non-exsistent")));
        assertThrows(IOException.class, () -> FileUtils.readFileToByteArray(tempDirFile));
    }

    @Test
    @EnabledIf("isPosixFilePermissionsSupported")
    public void testReadFileToByteArray_IOExceptionOnPosixFileSystem() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "cant-read.txt");
        TestUtils.createFile(file, 100);
        Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("---------"));

        assertThrows(IOException.class, () -> FileUtils.readFileToByteArray(file));
    }

    @Test
    public void testReadFileToString_Errors() {
        assertThrows(NullPointerException.class, () -> FileUtils.readFileToString(null));
        assertThrows(IOException.class, () -> FileUtils.readFileToString(new File("non-exsistent")));
        assertThrows(IOException.class, () -> FileUtils.readFileToString(tempDirFile));
        assertThrows(UnsupportedCharsetException.class, () -> FileUtils.readFileToString(tempDirFile, "unsupported-charset"));
    }

    @Test
    @EnabledIf("isPosixFilePermissionsSupported")
    public void testReadFileToString_IOExceptionOnPosixFileSystem() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "cant-read.txt");
        TestUtils.createFile(file, 100);
        Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("---------"));

        assertThrows(IOException.class, () -> FileUtils.readFileToString(file));
    }

    @Test
    public void testReadFileToStringWithDefaultEncoding() throws Exception {
        final File file = new File(tempDirFile, "read.obj");
        // Don't use non-ASCII in this test fixture because this test uses the default platform encoding.
        final String fixture = "Hello 1234";
        Files.write(file.toPath(), fixture.getBytes());
        assertEquals(fixture, FileUtils.readFileToString(file));
    }

    @Test
    public void testReadFileToStringWithEncoding() throws Exception {
        final File file = new File(tempDirFile, "read.obj");
        final byte[] text = "Hello \u1234".getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(), text);

        final String data = FileUtils.readFileToString(file, "UTF8");
        assertEquals("Hello \u1234", data);
    }

    @Test
    public void testReadLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        try {
            final String[] data = {"hello", "\u1234", "", "this is", "some text"};
            TestUtils.createLineFileUtf8(file, data);

            final List<String> lines = FileUtils.readLines(file, UTF_8);
            assertEquals(Arrays.asList(data), lines);
        } finally {
            TestUtils.deleteFile(file);
        }
    }

    @Test
    public void testReadLines_Errors() {
        assertThrows(NullPointerException.class, () -> FileUtils.readLines(null));
        assertThrows(IOException.class, () -> FileUtils.readLines(new File("non-exsistent")));
        assertThrows(IOException.class, () -> FileUtils.readLines(tempDirFile));
        assertThrows(UnsupportedCharsetException.class, () -> FileUtils.readLines(tempDirFile, "unsupported-charset"));
    }

    @Test
    @EnabledIf("isPosixFilePermissionsSupported")
    public void testReadLines_IOExceptionOnPosixFileSystem() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "cant-read.txt");
        TestUtils.createFile(file, 100);
        Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("---------"));

        assertThrows(IOException.class, () -> FileUtils.readLines(file));
    }

    @Test
    public void testSizeOf() throws Exception {
        final File file = new File(tempDirFile, getName());

        // Null argument
        assertThrows(NullPointerException.class, () -> FileUtils.sizeOf(null));

        // Non-existent file
        assertThrows(IllegalArgumentException.class, () -> FileUtils.sizeOf(file));

        // Creates file
        file.createNewFile();

        // New file
        assertEquals(0, FileUtils.sizeOf(file));
        file.delete();

        // Existing file
        assertEquals(testFile1Size, FileUtils.sizeOf(testFile1), "Unexpected files size");

        // Existing directory
        assertEquals(TEST_DIRECTORY_SIZE, FileUtils.sizeOf(tempDirFile), "Unexpected directory size");
    }

    @Test
    public void testSizeOfAsBigInteger() throws Exception {
        final File file = new File(tempDirFile, getName());

        // Null argument
        assertThrows(NullPointerException.class, () -> FileUtils.sizeOfAsBigInteger(null));
        // Non-existent file
        assertThrows(IllegalArgumentException.class, () -> FileUtils.sizeOfAsBigInteger(file));

        // Creates file
        file.createNewFile();

        // New file
        assertEquals(BigInteger.ZERO, FileUtils.sizeOfAsBigInteger(file));
        file.delete();

        // Existing file
        assertEquals(BigInteger.valueOf(testFile1Size), FileUtils.sizeOfAsBigInteger(testFile1),
                "Unexpected files size");

        // Existing directory
        assertEquals(TEST_DIRECTORY_SIZE_BI, FileUtils.sizeOfAsBigInteger(tempDirFile),
                "Unexpected directory size");
    }

    /**
     * Requires admin privileges on Windows.
     *
     * @throws Exception For example java.nio.file.FileSystemException:
     *                   C:\Users\you\AppData\Local\Temp\junit2324629522183300191\FileUtilsTest8613879743106252609\symlinked-dir: A required privilege is
     *                   not held by the client.
     */
    @Test
    public void testSizeOfDirectory() throws Exception {
        final File file = new File(tempDirFile, getName());

        // Null argument
        assertThrows(NullPointerException.class, () -> FileUtils.sizeOfDirectory(null));
        // Non-existent file
        assertThrows(IllegalArgumentException.class, () -> FileUtils.sizeOfAsBigInteger(file));

        // Creates file
        file.createNewFile();

        // Existing file
        assertThrows(IllegalArgumentException.class, () -> FileUtils.sizeOfDirectory(file));

        // Existing directory
        file.delete();
        file.mkdir();

        // Create a cyclic symlink
        createCircularSymbolicLink(file);

        assertEquals(TEST_DIRECTORY_SIZE, FileUtils.sizeOfDirectory(file), "Unexpected directory size");
    }

    /**
     * Requires admin privileges on Windows.
     *
     * @throws Exception For example java.nio.file.FileSystemException:
     *                   C:\Users\you\AppData\Local\Temp\junit2324629522183300191\FileUtilsTest8613879743106252609\symlinked-dir: A required privilege is
     *                   not held by the client.
     */
    @Test
    public void testSizeOfDirectoryAsBigInteger() throws Exception {
        final File file = new File(tempDirFile, getName());

        // Null argument
        assertThrows(NullPointerException.class, () -> FileUtils.sizeOfDirectoryAsBigInteger(null));
        // Non-existent file
        assertThrows(UncheckedIOException.class, () -> FileUtils.sizeOfDirectoryAsBigInteger(file));

        // Creates file
        file.createNewFile();

        // Existing file
        assertThrows(IllegalArgumentException.class, () -> FileUtils.sizeOfDirectoryAsBigInteger(file));

        // Existing directory
        file.delete();
        file.mkdir();

        createCircularSymbolicLink(file);

        assertEquals(TEST_DIRECTORY_SIZE_BI, FileUtils.sizeOfDirectoryAsBigInteger(file), "Unexpected directory size");

        // Existing directory which size is greater than zero
        file.delete();
        file.mkdir();

        final File nonEmptyFile = new File(file, "non-emptyFile" + System.nanoTime());
        assertTrue(nonEmptyFile.getParentFile().exists(), () -> "Cannot create file " + nonEmptyFile + " as the parent directory does not exist");
        final OutputStream output = new BufferedOutputStream(Files.newOutputStream(nonEmptyFile.toPath()));
        try {
            TestUtils.generateTestData(output, TEST_DIRECTORY_SIZE_GT_ZERO_BI.longValue());
        } finally {
            IOUtils.closeQuietly(output);
        }

        assertEquals(TEST_DIRECTORY_SIZE_GT_ZERO_BI, FileUtils.sizeOfDirectoryAsBigInteger(file), "Unexpected directory size");

        nonEmptyFile.delete();
        file.delete();
    }

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
        assertNull(FileUtils.toFile(null));
        assertNull(FileUtils.toFile(new URL("http://jakarta.apache.org")));
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
        final URL[] urls = {
                new URL("file", null, "file1.txt"),
                new URL("file", null, "file2.txt"),
        };
        final File[] files = FileUtils.toFiles(urls);

        assertEquals(urls.length, files.length);
        assertTrue(files[0].toString().contains("file1.txt"), "File: " + files[0]);
        assertTrue(files[1].toString().contains("file2.txt"), "File: " + files[1]);
    }

    @Test
    public void testToFiles2() throws Exception {
        final URL[] urls = {
                new URL("file", null, "file1.txt"),
                null,
        };
        final File[] files = FileUtils.toFiles(urls);

        assertEquals(urls.length, files.length);
        assertTrue(files[0].toString().contains("file1.txt"), "File: " + files[0]);
        assertNull(files[1], "File: " + files[1]);
    }

    @Test
    public void testToFiles3() throws Exception {
        final URL[] urls = null;
        final File[] files = FileUtils.toFiles(urls);

        assertEquals(0, files.length);
    }

    @Test
    public void testToFiles3a() throws Exception {
        final URL[] urls = {}; // empty array
        final File[] files = FileUtils.toFiles(urls);

        assertEquals(0, files.length);
    }

    @Test
    public void testToFiles4() throws Exception {
        final URL[] urls = {
                new URL("file", null, "file1.txt"),
                new URL("http", "jakarta.apache.org", "file1.txt"),
        };
        assertThrows(IllegalArgumentException.class, () -> FileUtils.toFiles(urls));
    }

    @Test
    public void testToFileUtf8() throws Exception {
        final URL url = new URL("file", null, "/home/%C3%A4%C3%B6%C3%BC%C3%9F");
        final File file = FileUtils.toFile(url);
        assertTrue(file.toString().contains("\u00E4\u00F6\u00FC\u00DF"));
    }

    @Test
    public void testTouch() throws IOException {
        assertThrows(NullPointerException.class, () -> FileUtils.touch(null));

        final File file = new File(tempDirFile, "touch.txt");
        if (file.exists()) {
            file.delete();
        }
        assertFalse(file.exists(), "Bad test: test file still exists");
        FileUtils.touch(file);
        assertTrue(file.exists(), "FileUtils.touch() created file");
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            assertEquals(0, file.length(), "Created empty file.");
            out.write(0);
        }
        assertEquals(1, file.length(), "Wrote one byte to file");
        final long y2k = new GregorianCalendar(2000, 0, 1).getTime().getTime();
        final boolean res = setLastModifiedMillis(file, y2k);  // 0L fails on Win98
        assertTrue(res, "Bad test: set lastModified failed");
        assertEquals(y2k, getLastModifiedMillis(file), "Bad test: set lastModified set incorrect value");
        final long nowMillis = System.currentTimeMillis();
        FileUtils.touch(file);
        assertEquals(1, file.length(), "FileUtils.touch() didn't empty the file.");
        assertNotEquals(y2k, getLastModifiedMillis(file), "FileUtils.touch() changed lastModified");
        final int delta = 3000;
        assertTrue(getLastModifiedMillis(file) >= nowMillis - delta, "FileUtils.touch() changed lastModified to more than now-3s");
        assertTrue(getLastModifiedMillis(file) <= nowMillis + delta, "FileUtils.touch() changed lastModified to less than now+3s");
    }

    @Test
    public void testTouchDirDoesNotExist() throws Exception {
        final File file = new File("target/does-not-exist", "touchme.txt");
        final File parentDir = file.getParentFile();
        file.delete();
        parentDir.delete();
        assertFalse(parentDir.exists());
        assertFalse(file.exists());
        FileUtils.touch(file);
        assertTrue(parentDir.exists());
        assertTrue(file.exists());
    }

    @Test
    public void testToURLs1() throws Exception {
        final File[] files = {
                new File(tempDirFile, "file1.txt"),
                new File(tempDirFile, "file2.txt"),
                new File(tempDirFile, "test file.txt"),
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
    public void testToURLs2() {
        final File[] files = {
            new File(tempDirFile, "file1.txt"),
            null,
        };
        assertThrows(NullPointerException.class, () -> FileUtils.toURLs(files),
                "Can't convert null URL");
    }

    @Test
    public void testToURLs3() {
        final File[] files = null;
        assertThrows(NullPointerException.class, () -> FileUtils.toURLs(files),
                "Can't convert null list");
    }

    @Test
    public void testToURLs3a() throws Exception {
        final File[] files = {}; // empty array
        final URL[] urls = FileUtils.toURLs(files);

        assertEquals(0, urls.length);
    }

    @Test
    public void testWrite_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWrite_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteByteArrayToFile() throws Exception {
        final File file = new File(tempDirFile, "write.obj");
        final byte[] data = {11, 21, 31};
        FileUtils.writeByteArrayToFile(file, data);
        TestUtils.assertEqualContent(data, file);
    }

    @Test
    public void testWriteByteArrayToFile_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeByteArrayToFile(file, "this is brand new data".getBytes(), false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteByteArrayToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeByteArrayToFile(file, "this is brand new data".getBytes(), true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength() throws Exception {
        final File file = new File(tempDirFile, "write.obj");
        final byte[] data = {11, 21, 32, 41, 51};
        final byte[] writtenData = new byte[3];
        System.arraycopy(data, 1, writtenData, 0, 3);
        FileUtils.writeByteArrayToFile(file, data, 1, 3);
        TestUtils.assertEqualContent(writtenData, file);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength_WithAppendOptionTrue_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final byte[] data = "SKIP_THIS_this is brand new data_AND_SKIP_THIS".getBytes(StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(file, data, 10, 22, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final byte[] data = "SKIP_THIS_this is brand new data_AND_SKIP_THIS".getBytes(StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(file, data, 10, 22, true);

        final String expected = "This line was there before you..." + "this is brand new data";
        final String actual = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteCharSequence1() throws Exception {
        final File file = new File(tempDirFile, "write.txt");
        FileUtils.write(file, "Hello \u1234", "UTF8");
        final byte[] text = "Hello \u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteCharSequence2() throws Exception {
        final File file = new File(tempDirFile, "write.txt");
        FileUtils.write(file, "Hello \u1234", (String) null);
        final byte[] text = "Hello \u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteLines_3arg_nullSeparator() throws Exception {
        final Object[] data = {
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeLines(file, StandardCharsets.US_ASCII.name(), list);

        final String expected = "hello" + System.lineSeparator() + "world" + System.lineSeparator() +
                System.lineSeparator() + "this is" + System.lineSeparator() +
                System.lineSeparator() + "some text" + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file, StandardCharsets.US_ASCII.name());
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_3argsWithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...", StandardCharsets.UTF_8);

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
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...", StandardCharsets.UTF_8);

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
        final Object[] data = {
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeLines(file, StandardCharsets.US_ASCII.name(), list, "*");

        final String expected = "hello*world**this is**some text*";
        final String actual = FileUtils.readFileToString(file, StandardCharsets.US_ASCII.name());
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_4arg_nullSeparator() throws Exception {
        final Object[] data = {
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeLines(file, StandardCharsets.US_ASCII.name(), list, null);

        final String expected = "hello" + System.lineSeparator() + "world" + System.lineSeparator() +
                System.lineSeparator() + "this is" + System.lineSeparator() +
                System.lineSeparator() + "some text" + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file, StandardCharsets.US_ASCII.name());
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_4arg_Writer_nullData() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeLines(file, StandardCharsets.US_ASCII.name(), null, "*");

        assertEquals(0, file.length(), "Sizes differ");
    }

    @Test
    public void testWriteLines_4argsWithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
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
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
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
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
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
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
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
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
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
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
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
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteStringToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteStringToFileIntoNonExistentSubdir() throws Exception {
        final File file = new File(tempDirFile, "subdir/write.txt");
        FileUtils.writeStringToFile(file, "Hello \u1234", (Charset) null);
        final byte[] text = "Hello \u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    /**
     * Requires admin privileges on Windows.
     *
     * @throws Exception For example java.nio.file.FileSystemException:
     *                   C:\Users\you\AppData\Local\Temp\junit2324629522183300191\FileUtilsTest8613879743106252609\symlinked-dir: A required privilege is
     *                   not held by the client.
     */
    @Test
    public void testWriteStringToFileIntoSymlinkedDir() throws Exception {
        final Path symlinkDir = createTempSymbolicLinkedRelativeDir().getLeft();
        final File file = symlinkDir.resolve("file").toFile();
        FileUtils.writeStringToFile(file, "Hello \u1234", StandardCharsets.UTF_8);
        final byte[] text = "Hello \u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFileWithCharset() throws Exception {
        final File file = new File(tempDirFile, "write.txt");
        FileUtils.writeStringToFile(file, "Hello \u1234", "UTF8");
        final byte[] text = "Hello \u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFileWithEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", (String) null, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteStringToFileWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", (String) null, true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteStringToFileWithNullCharset() throws Exception {
        final File file = new File(tempDirFile, "write.txt");
        FileUtils.writeStringToFile(file, "Hello \u1234", (Charset) null);
        final byte[] text = "Hello \u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFileWithNullStringCharset() throws Exception {
        final File file = new File(tempDirFile, "write.txt");
        FileUtils.writeStringToFile(file, "Hello \u1234", (String) null);
        final byte[] text = "Hello \u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteWithEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...", StandardCharsets.UTF_8);

        FileUtils.write(file, "this is brand new data", (String) null, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(tempDirFile, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...", StandardCharsets.UTF_8);

        FileUtils.write(file, "this is brand new data", (String) null, true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

}
