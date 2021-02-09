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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsTest extends TestArguments {

    private static final String PATH_FIXTURE = "NOTICE.txt";

    /**
     * A temporary directory managed by JUnit.
     */
    @TempDir
    public Path tempDir;

    @Test
    public void testCopyFile() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            final Path sourceFile = Paths
                .get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
            final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDir);
            assertTrue(Files.exists(targetFile));
            assertEquals(Files.size(sourceFile), Files.size(targetFile));
        } finally {
            PathUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCreateDirectoriesAlreadyExists() throws IOException {
        assertEquals(tempDir.getParent(), PathUtils.createParentDirectories(tempDir));
    }

    @Test
    public void testCreateDirectoriesNew() throws IOException {
        assertEquals(tempDir, PathUtils.createParentDirectories(tempDir.resolve("child")));
    }

    @Test
    public void testIsDirectory() throws IOException {
        assertFalse(PathUtils.isDirectory(null));

        assertTrue(PathUtils.isDirectory(tempDir));
        final Path testFile1 = Files.createTempFile(tempDir, "prefix", null);
        assertFalse(PathUtils.isDirectory(testFile1));

        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        Files.delete(tempDir);
        assertFalse(PathUtils.isDirectory(tempDir));
    }

    @Test
    public void testIsRegularFile() throws IOException {
        assertFalse(PathUtils.isRegularFile(null));

        assertFalse(PathUtils.isRegularFile(tempDir));
        final Path testFile1 = Files.createTempFile(tempDir, "prefix", null);
        assertTrue(PathUtils.isRegularFile(testFile1));

        Files.delete(testFile1);
        assertFalse(PathUtils.isRegularFile(testFile1));
    }

    @Test
    public void testNewDirectoryStream() throws Exception {
        final PathFilter pathFilter = new NameFileFilter(PATH_FIXTURE);
        try (final DirectoryStream<Path> stream = PathUtils.newDirectoryStream(PathUtils.current(), pathFilter)) {
            final Iterator<Path> iterator = stream.iterator();
            final Path path = iterator.next();
            assertEquals(PATH_FIXTURE, path.getFileName().toString());
            assertFalse(iterator.hasNext());
        }
    }
    
	@Test
	public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePath() throws IOException {
		final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName()).toAbsolutePath();
		try {
			final Path archivePath = Paths.get("src/test/resources/org/apache/commons/io/test.jar");
			try (final FileSystem archive = FileSystems.newFileSystem(archivePath, null)) {
				// relative jar -> absolute dir
				Path sourceDir = archive.getPath("dir1");
				PathUtils.copyDirectory(sourceDir, tempDir);
				assertTrue(Files.exists(tempDir.resolve("f1")));
				
				// absolute jar -> absolute dir
				sourceDir = archive.getPath("/next");
				PathUtils.copyDirectory(sourceDir, tempDir);
				assertTrue(Files.exists(tempDir.resolve("dir")));
			}
		} finally {
			PathUtils.deleteDirectory(tempDir);
		}
	}
	
	@Test
	public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePathReverse() throws IOException {
		final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
		try {
			final Path archivePath = tempDir.resolve("test.jar");
			final URI uri = URI.create("jar:file:" + archivePath.toAbsolutePath().toString());
			final Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			try (final FileSystem archive = FileSystems.newFileSystem(uri, env, null)) {
				// absolute dir -> relative jar
				Path targetDir = archive.getPath("target");
				Files.createDirectory(targetDir);
				Path sourceDir= Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2").toAbsolutePath();
				PathUtils.copyDirectory(sourceDir, targetDir);
				assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
				
				// absolute dir -> absolute jar
				targetDir = archive.getPath("/");
				PathUtils.copyDirectory(sourceDir, targetDir);
				assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
			}
		} finally {
			PathUtils.deleteDirectory(tempDir);
		}
	}
	
	@Test
	public void testCopyDirectoryForDifferentFilesystemsWithRelativePath() throws IOException {
		Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
		final Path cwd = Paths.get("").toAbsolutePath();
		tempDir = cwd.relativize(tempDir);
		try {
			final Path archivePath = Paths.get("src/test/resources/org/apache/commons/io/test.jar");
			try (final FileSystem archive = FileSystems.newFileSystem(archivePath, null)) {
				// relative jar -> relative dir
				Path sourceDir = archive.getPath("next");
				PathUtils.copyDirectory(sourceDir, tempDir);
				assertTrue(Files.exists(tempDir.resolve("dir")));
				
				// absolute jar -> relative dir
				sourceDir = archive.getPath("/dir1");
				PathUtils.copyDirectory(sourceDir, tempDir);
				assertTrue(Files.exists(tempDir.resolve("f1")));
			}
		} finally {
			PathUtils.deleteDirectory(tempDir);
		}
	}
	
	@Test
	public void testCopyDirectoryForDifferentFilesystemsWithRelativePathReverse() throws IOException {
		final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
		try {
			final Path archivePath = tempDir.resolve("test.jar");
			final URI uri = URI.create("jar:file:" + archivePath.toAbsolutePath().toString());
			final Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			try (final FileSystem archive = FileSystems.newFileSystem(uri, env, null)) {
				// relative dir -> relative jar
				Path targetDir = archive.getPath("target");
				Files.createDirectory(targetDir);
				final Path sourceDir= Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2");
				PathUtils.copyDirectory(sourceDir, targetDir);
				assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
				
				// relative dir -> absolute jar
				targetDir = archive.getPath("/");
				PathUtils.copyDirectory(sourceDir, targetDir);
				assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
			}
		} finally {
			PathUtils.deleteDirectory(tempDir);
		}
	}

}
