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
package org.apache.commons.io.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.Uncheck;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilesUncheck}.
 *
 * These tests are simple and just makes sure we do can make the call without catching IOException.
 */
public class FilesUncheckTest {

    private static final FileAttribute<?>[] EMPTY_FILE_ATTRIBUTES_ARRAY = {};

    private static final Path FILE_PATH_A = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");

    private static final Path FILE_PATH_EMPTY = Paths.get("src/test/resources/org/apache/commons/io/test-file-empty.bin");

    private static final Path NEW_DIR_PATH = Paths.get("target/newdir");

    private static final Path NEW_FILE_PATH = Paths.get("target/file.txt");

    private static final Path NEW_FILE_PATH_LINK = Paths.get("target/to_another_file.txt");

    private static final String PREFIX = "prefix";

    private static final String SUFFIX = "suffix";

    private static final Path TARGET_PATH = Paths.get("target");

    @BeforeEach
    @AfterEach
    public void deleteFixtures() throws IOException {
        Files.deleteIfExists(NEW_FILE_PATH);
        Files.deleteIfExists(NEW_DIR_PATH);
        Files.deleteIfExists(NEW_FILE_PATH_LINK);
    }

    @Test
    public void testCopyInputStreamPathCopyOptionArray() {
        assertEquals(0, FilesUncheck.copy(new NullInputStream(), NEW_FILE_PATH, PathUtils.EMPTY_COPY_OPTIONS));
    }

    @Test
    public void testCopyPathOutputStream() {
        assertEquals(0, FilesUncheck.copy(FILE_PATH_EMPTY, NullOutputStream.INSTANCE));
    }

    @Test
    public void testCopyPathPathCopyOptionArray() {
        assertEquals(NEW_FILE_PATH, FilesUncheck.copy(FILE_PATH_EMPTY, NEW_FILE_PATH, PathUtils.EMPTY_COPY_OPTIONS));
    }

    @Test
    public void testCreateDirectories() {
        assertEquals(TARGET_PATH, FilesUncheck.createDirectories(TARGET_PATH, EMPTY_FILE_ATTRIBUTES_ARRAY));
    }

    @Test
    public void testCreateDirectory() {
        assertEquals(NEW_DIR_PATH, FilesUncheck.createDirectory(NEW_DIR_PATH, EMPTY_FILE_ATTRIBUTES_ARRAY));
    }

    @Test
    public void testCreateFile() {
        assertEquals(NEW_FILE_PATH, FilesUncheck.createFile(NEW_FILE_PATH, EMPTY_FILE_ATTRIBUTES_ARRAY));
    }

    @Test
    public void testCreateLink() {
        assertEquals(NEW_FILE_PATH_LINK, FilesUncheck.createLink(NEW_FILE_PATH_LINK, FILE_PATH_EMPTY));
    }

    @Test
    public void testCreateSymbolicLink() {
        // May cause: Caused by: java.nio.file.FileSystemException: A required privilege is not held by the client.
        assertEquals(NEW_FILE_PATH_LINK, FilesUncheck.createSymbolicLink(NEW_FILE_PATH_LINK, FILE_PATH_EMPTY));
    }

    @Test
    public void testCreateTempDirectoryPathStringFileAttributeOfQArray() {
        assertEquals(TARGET_PATH, FilesUncheck.createTempDirectory(TARGET_PATH, PREFIX, EMPTY_FILE_ATTRIBUTES_ARRAY).getParent());
    }

    @Test
    public void testCreateTempDirectoryStringFileAttributeOfQArray() {
        assertEquals(PathUtils.getTempDirectory(), FilesUncheck.createTempDirectory(PREFIX, EMPTY_FILE_ATTRIBUTES_ARRAY).getParent());
    }

    @Test
    public void testCreateTempFilePathStringStringFileAttributeOfQArray() {
        assertEquals(TARGET_PATH, FilesUncheck.createTempFile(TARGET_PATH, PREFIX, SUFFIX, EMPTY_FILE_ATTRIBUTES_ARRAY).getParent());
    }

    @Test
    public void testCreateTempFileStringStringFileAttributeOfQArray() {
        assertEquals(PathUtils.getTempDirectory(), FilesUncheck.createTempFile(PREFIX, SUFFIX, EMPTY_FILE_ATTRIBUTES_ARRAY).getParent());
    }

    @Test
    public void testDelete() {
        assertThrows(UncheckedIOException.class, () -> FilesUncheck.delete(NEW_FILE_PATH));
    }

    @Test
    public void testDeleteIfExists() {
        assertFalse(FilesUncheck.deleteIfExists(NEW_FILE_PATH));
    }

    @Test
    public void testFind() {
        try (Stream<Path> find = FilesUncheck.find(FILE_PATH_EMPTY, 0, (t, u) -> false)) {
            assertNotNull(find);
        }
    }

    @Test
    public void testGetAttribute() {
        assertEquals(0L, FilesUncheck.getAttribute(FILE_PATH_EMPTY, "basic:size", LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void testGetFileStore() {
        assertNotNull(FilesUncheck.getFileStore(FILE_PATH_EMPTY));
    }

    @Test
    public void testGetLastModifiedTime() {
        assertTrue(0 < FilesUncheck.getLastModifiedTime(FILE_PATH_EMPTY, LinkOption.NOFOLLOW_LINKS).toMillis());
    }

    @Test
    public void testGetOwner() {
        assertNotNull(FilesUncheck.getOwner(FILE_PATH_EMPTY, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void testGetPosixFilePermissions() {
        assumeTrue(PathUtils.isPosix(FILE_PATH_EMPTY, LinkOption.NOFOLLOW_LINKS));
        assertNotNull(FilesUncheck.getPosixFilePermissions(FILE_PATH_EMPTY, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void testIsHidden() {
        assertFalse(FilesUncheck.isHidden(FILE_PATH_EMPTY));
    }

    @Test
    public void testIsSameFile() {
        assertTrue(FilesUncheck.isSameFile(FILE_PATH_EMPTY, FILE_PATH_EMPTY));
    }

    @Test
    public void testLinesPath() {
        try (Stream<String> stream = FilesUncheck.lines(FILE_PATH_EMPTY)) {
            assertEquals(0, stream.count());
        }
    }

    @Test
    public void testLinesPathCharset() {
        try (Stream<String> stream = FilesUncheck.lines(FILE_PATH_EMPTY, StandardCharsets.UTF_8)) {
            assertEquals(0, stream.count());
        }
    }

    @Test
    public void testList() {
        try (Stream<Path> stream = FilesUncheck.list(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0"))) {
            assertEquals(1, stream.count());
        }
    }

    @Test
    public void testMove() {
        final Path tempFile1 = FilesUncheck.createTempFile(PREFIX, SUFFIX);
        final Path tempFile2 = FilesUncheck.createTempFile(PREFIX, SUFFIX);
        assertEquals(tempFile2, FilesUncheck.move(tempFile1, tempFile2, StandardCopyOption.REPLACE_EXISTING));
        FilesUncheck.delete(tempFile2);
    }

    @Test
    public void testNewBufferedReaderPath() {
        Uncheck.run(() -> {
            try (BufferedReader reader = FilesUncheck.newBufferedReader(FILE_PATH_EMPTY)) {
                IOUtils.consume(reader);
            }
        });
    }

    @Test
    public void testNewBufferedReaderPathCharset() {
        Uncheck.run(() -> {
            try (BufferedReader reader = FilesUncheck.newBufferedReader(FILE_PATH_EMPTY, StandardCharsets.UTF_8)) {
                IOUtils.consume(reader);
            }
        });
    }

    @Test
    public void testNewBufferedWriterPathCharsetOpenOptionArray() {
        final Path tempPath = FilesUncheck.createTempFile(PREFIX, SUFFIX);
        Uncheck.run(() -> {
            try (BufferedWriter writer = FilesUncheck.newBufferedWriter(tempPath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.append("test");
            }
        });
        assertEquals("test", FilesUncheck.readAllLines(tempPath, StandardCharsets.UTF_8).get(0));
    }

    @Test
    public void testNewBufferedWriterPathOpenOptionArray() {
        final Path tempPath = FilesUncheck.createTempFile(PREFIX, SUFFIX);
        Uncheck.run(() -> {
            try (BufferedWriter writer = FilesUncheck.newBufferedWriter(tempPath, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.append("test");
            }
        });
        assertEquals("test", FilesUncheck.readAllLines(tempPath).get(0));
    }

    @Test
    public void testNewByteChannelPathOpenOptionArray() {
        assertEquals(0, Uncheck.getAsLong(() -> {
            try (SeekableByteChannel c = FilesUncheck.newByteChannel(FILE_PATH_EMPTY, StandardOpenOption.READ)) {
                return c.size();
            }
        }));
    }

    @Test
    public void testNewByteChannelPathSetOfQextendsOpenOptionFileAttributeOfQArray() {
        final Set<OpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.READ);
        assertEquals(0, Uncheck.getAsLong(() -> {
            try (SeekableByteChannel c = FilesUncheck.newByteChannel(FILE_PATH_EMPTY, options, EMPTY_FILE_ATTRIBUTES_ARRAY)) {
                return c.size();
            }
        }));
    }

    @Test
    public void testNewDirectoryStreamPath() {
        Uncheck.run(() -> {
            try (DirectoryStream<Path> directoryStream = FilesUncheck.newDirectoryStream(TARGET_PATH)) {
                directoryStream.forEach(e -> assertEquals(TARGET_PATH, e.getParent()));
            }
        });
    }

    @Test
    public void testNewDirectoryStreamPathFilterOfQsuperPath() {
        Uncheck.run(() -> {
            try (DirectoryStream<Path> directoryStream = FilesUncheck.newDirectoryStream(TARGET_PATH, e -> true)) {
                directoryStream.forEach(e -> assertEquals(TARGET_PATH, e.getParent()));
            }
        });
    }

    @Test
    public void testNewDirectoryStreamPathString() {
        Uncheck.run(() -> {
            try (DirectoryStream<Path> directoryStream = FilesUncheck.newDirectoryStream(TARGET_PATH, "*.xml")) {
                directoryStream.forEach(e -> assertEquals(TARGET_PATH, e.getParent()));
            }
        });
    }

    @Test
    public void testNewInputStream() {
        assertEquals(0, Uncheck.getAsInt(() -> {
            try (InputStream in = FilesUncheck.newInputStream(FILE_PATH_EMPTY, StandardOpenOption.READ)) {
                return in.available();
            }
        }));
    }

    @Test
    public void testNewOutputStream() {
        final Path tempPath = FilesUncheck.createTempFile(PREFIX, SUFFIX);
        Uncheck.run(() -> {
            try (OutputStream stream = FilesUncheck.newOutputStream(tempPath, StandardOpenOption.TRUNCATE_EXISTING)) {
                stream.write("test".getBytes());
            }
        });
        assertEquals("test", FilesUncheck.readAllLines(tempPath).get(0));
    }

    @Test
    public void testProbeContentType() {
        // Empty file:
        String probeContentType = FilesUncheck.probeContentType(FILE_PATH_EMPTY);
        // Empirical: probeContentType is null on Windows
        // Empirical: probeContentType is "text/plain" on Ubuntu
        // Empirical: probeContentType is ? on macOS
        //
        // BOM file:
        probeContentType = FilesUncheck.probeContentType(Paths.get("src/test/resources/org/apache/commons/io/testfileBOM.xml"));
        // Empirical: probeContentType is "text/plain" on Windows
        // Empirical: probeContentType is "application/plain" on Ubuntu
        // Empirical: probeContentType is ? on macOS
    }

    @Test
    public void testReadAllBytes() {
        assertArrayEquals(ArrayUtils.EMPTY_BYTE_ARRAY, FilesUncheck.readAllBytes(FILE_PATH_EMPTY));
        assertArrayEquals(new byte[] {'a'}, FilesUncheck.readAllBytes(FILE_PATH_A));
    }

    @Test
    public void testReadAllLinesPath() {
        assertEquals(Collections.emptyList(), FilesUncheck.readAllLines(FILE_PATH_EMPTY));
        assertEquals(Arrays.asList("a"), FilesUncheck.readAllLines(FILE_PATH_A));
    }

    @Test
    public void testReadAllLinesPathCharset() {
        assertEquals(Collections.emptyList(), FilesUncheck.readAllLines(FILE_PATH_EMPTY, StandardCharsets.UTF_8));
        assertEquals(Arrays.asList("a"), FilesUncheck.readAllLines(FILE_PATH_A, StandardCharsets.UTF_8));
    }

    @Test
    public void testReadAttributesPathClassOfALinkOptionArray() {
        assertNotNull(FilesUncheck.readAttributes(FILE_PATH_EMPTY, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void testReadAttributesPathStringLinkOptionArray() {
        assertNotNull(FilesUncheck.readAttributes(FILE_PATH_EMPTY, "basic:lastModifiedTime", LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void testReadSymbolicLink() {
        assertThrows(UncheckedIOException.class, () -> FilesUncheck.readSymbolicLink(NEW_FILE_PATH_LINK));
    }

    @Test
    public void testSetAttribute() {
        final FileTime ft = FilesUncheck.getLastModifiedTime(FILE_PATH_EMPTY);
        assertEquals(FILE_PATH_EMPTY, FilesUncheck.setAttribute(FILE_PATH_EMPTY, "basic:lastModifiedTime", ft, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void testSetLastModifiedTime() {
        final FileTime ft = FilesUncheck.getLastModifiedTime(FILE_PATH_EMPTY);
        assertEquals(FILE_PATH_EMPTY, FilesUncheck.setLastModifiedTime(FILE_PATH_EMPTY, ft));
    }

    @Test
    public void testSetOwner() {
        final UserPrincipal owner = FilesUncheck.getOwner(FILE_PATH_EMPTY, LinkOption.NOFOLLOW_LINKS);
        assertEquals(FILE_PATH_EMPTY, FilesUncheck.setOwner(FILE_PATH_EMPTY, owner));
    }

    @Test
    public void testSetPosixFilePermissions() {
        assumeTrue(PathUtils.isPosix(FILE_PATH_EMPTY, LinkOption.NOFOLLOW_LINKS));
        final Set<PosixFilePermission> posixFilePermissions = FilesUncheck.getPosixFilePermissions(FILE_PATH_EMPTY, LinkOption.NOFOLLOW_LINKS);
        assertEquals(FILE_PATH_EMPTY, FilesUncheck.setPosixFilePermissions(FILE_PATH_EMPTY, posixFilePermissions));
    }

    @Test
    public void testSize() {
        assertEquals(0, FilesUncheck.size(FILE_PATH_EMPTY));
        assertEquals(1, FilesUncheck.size(FILE_PATH_A));
    }

    @Test
    public void testWalkFileTreePathFileVisitorOfQsuperPath() {
        assertEquals(TARGET_PATH, FilesUncheck.walkFileTree(TARGET_PATH, NoopPathVisitor.INSTANCE));
    }

    @Test
    public void testWalkFileTreePathSetOfFileVisitOptionIntFileVisitorOfQsuperPath() {
        assertEquals(TARGET_PATH, FilesUncheck.walkFileTree(TARGET_PATH, new HashSet<>(), 1, NoopPathVisitor.INSTANCE));
    }

    @Test
    public void testWalkPathFileVisitOptionArray() {
        try (Stream<Path> stream = FilesUncheck.walk(TARGET_PATH, FileVisitOption.FOLLOW_LINKS)) {
            assertTrue(0 < stream.count());
        }
    }

    @Test
    public void testWalkPathIntFileVisitOptionArray() {
        try (Stream<Path> stream = FilesUncheck.walk(TARGET_PATH, 0, FileVisitOption.FOLLOW_LINKS)) {
            assertEquals(1, stream.count());
        }
    }

    @Test
    public void testWritePathByteArrayOpenOptionArray() {
        final Path tempFile = FilesUncheck.createTempFile(PREFIX, SUFFIX);
        assertEquals(tempFile, FilesUncheck.write(tempFile, "test".getBytes(), StandardOpenOption.TRUNCATE_EXISTING));
        FilesUncheck.delete(tempFile);
    }

    @Test
    public void testWritePathIterableOfQextendsCharSequenceCharsetOpenOptionArray() {
        final Path tempFile = FilesUncheck.createTempFile(PREFIX, SUFFIX);
        assertEquals(tempFile, FilesUncheck.write(tempFile, Arrays.asList("test"), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING));
        FilesUncheck.delete(tempFile);
    }

    @Test
    public void testWritePathIterableOfQextendsCharSequenceOpenOptionArray() {
        final Path tempFile = FilesUncheck.createTempFile(PREFIX, SUFFIX);
        assertEquals(tempFile, FilesUncheck.write(tempFile, Arrays.asList("test"), StandardOpenOption.TRUNCATE_EXISTING));
        FilesUncheck.delete(tempFile);
    }

}
