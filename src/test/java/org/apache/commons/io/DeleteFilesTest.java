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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.spi.FileSystemProvider;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

public class DeleteFilesTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCleanEmpty() throws IOException {
        Path root = temporaryFolder.getRoot().toPath();
        assertDirectoryEmpty(root);

        DeleteFiles.getDefault().cleanDirectory(root);

        assertDirectoryEmpty(root);
    }

    @Test
    public void testForceDeleteFile() throws IOException {
        File file = temporaryFolder.newFile();
        FileUtils.touch(file);

        DeleteFiles.getDefault().forceDelete(file.toPath());

        assertFalse(file.exists());
    }

    @Test
    public void testForceDeleteLockedFile() throws IOException {
        String filename = "/var/lock/apache.lock";
        File file = mock(File.class);
        Path path = mock(Path.class);
        FileSystem fs = mock(FileSystem.class);
        FileSystemProvider fsProvider = mock(FileSystemProvider.class);
        PosixFileAttributes attributes = mock(PosixFileAttributes.class);

        given(file.getPath()).willReturn(filename);
        given(file.toPath()).willReturn(path);
        given(path.toString()).willReturn(filename);
        given(path.toFile()).willReturn(file);
        given(path.getFileSystem()).willReturn(fs);
        given(fs.provider()).willReturn(fsProvider);
        willThrow(new FileSystemException(filename)).given(fsProvider).delete(path);
        given(fsProvider.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)).willReturn(attributes);
        given(fsProvider.readAttributes(path, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS)).willReturn(attributes);
        given(attributes.isDirectory()).willReturn(false);

        expectedException.expectMessage("Unable to delete '" + filename + "'. Tried 1 time.");
        expectedException.expect(CompositeIOException.class);
        DeleteFiles.getDefault().forceDelete(path);
    }

    @Test
    public void testForceDeleteReadOnlyFileWithRetryOverridingFileAttributes() throws IOException {
        File folder = temporaryFolder.newFolder();
        File file = new File(folder, "file.tmp");
        FileUtils.touch(file);
        assertTrue("Unable to make file read-only: " + file, file.setWritable(false));

        DeleteFiles deleteFiles = DeleteFiles.newConfig().setRetryOverridingFileAttributes(true).build();
        deleteFiles.forceDelete(file.toPath());

        assertFalse("Unable to delete file: " + file, file.exists());
    }

    @Test
    public void testDeleteQuietlyFileDoesNotExist() throws IOException {
        File folder = temporaryFolder.newFolder();
        File file = new File(folder, "invalid.file");
        assertFalse(file.exists());

        assertFalse(DeleteFiles.getDefault().deleteQuietly(file.toPath()));
    }

    @Test
    public void testCleanDirectory() throws IOException {
        File dir = temporaryFolder.newFolder();
        File d1 = new File(dir, "d1");
        File d2 = new File(dir, "d2");
        File f1 = new File(dir, "f1");
        File d1f1 = new File(d1, "d1f1");
        File d2f2 = new File(d2, "d1f2");
        assertTrue(d1.mkdirs());
        assertTrue(d2.mkdirs());
        FileUtils.touch(f1);
        FileUtils.touch(d1f1);
        FileUtils.touch(d2f2);

        DeleteFiles.getDefault().cleanDirectory(dir.toPath());

        assertTrue(dir.exists());
        assertFalse(d1.exists());
        assertFalse(d2.exists());
        assertFalse(f1.exists());
    }

    private static void assertDirectoryEmpty(final Path directory) throws IOException {
        try (final DirectoryStream<Path> children = Files.newDirectoryStream(directory)) {
            assertFalse(directory + " is not empty", children.iterator().hasNext());
        }
    }

}