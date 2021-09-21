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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test cases for FileUtils.deleteDirectory() method.
 *
 */
public abstract class FileUtilsDeleteDirectoryBaseTestCase {
    @TempDir
    public File top;

    protected abstract boolean setupSymlink(final File res, final File link) throws Exception;

    @Test
    public void testDeleteDirWithASymlinkDir() throws Exception {

        final File realOuter = new File(top, "realouter");
        assertTrue(realOuter.mkdirs());

        final File realInner = new File(realOuter, "realinner");
        assertTrue(realInner.mkdirs());

        FileUtils.touch(new File(realInner, "file1"));
        assertEquals(1, realInner.list().length);

        final File randomDirectory = new File(top, "randomDir");
        assertTrue(randomDirectory.mkdirs());

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertEquals(1, randomDirectory.list().length);

        final File symlinkDirectory = new File(realOuter, "fakeinner");
        assertTrue(setupSymlink(randomDirectory, symlinkDirectory));

        assertEquals(1, symlinkDirectory.list().length);

        // assert contents of the real directory were removed including the symlink
        FileUtils.deleteDirectory(realOuter);
        assertEquals(1, top.list().length);

        // ensure that the contents of the symlink were NOT removed.
        assertEquals(1, randomDirectory.list().length, "Contents of sym link should not have been removed");
    }

    @Test
    public void testDeleteDirWithASymlinkDir2() throws Exception {

        final File realOuter = new File(top, "realouter");
        assertTrue(realOuter.mkdirs());

        final File realInner = new File(realOuter, "realinner");
        assertTrue(realInner.mkdirs());

        FileUtils.touch(new File(realInner, "file1"));
        assertEquals(1, realInner.list().length);

        final File randomDirectory = new File(top, "randomDir");
        assertTrue(randomDirectory.mkdirs());

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertEquals(1, randomDirectory.list().length);

        final File symlinkDirectory = new File(realOuter, "fakeinner");
        Files.createSymbolicLink(symlinkDirectory.toPath(), randomDirectory.toPath());

        assertEquals(1, symlinkDirectory.list().length);

        // assert contents of the real directory were removed including the symlink
        FileUtils.deleteDirectory(realOuter);
        assertEquals(1, top.list().length);

        // ensure that the contents of the symlink were NOT removed.
        assertEquals(1, randomDirectory.list().length, "Contents of sym link should not have been removed");
    }

    @Test
    public void testDeleteDirWithSymlinkFile() throws Exception {
        final File realOuter = new File(top, "realouter");
        assertTrue(realOuter.mkdirs());

        final File realInner = new File(realOuter, "realinner");
        assertTrue(realInner.mkdirs());

        final File realFile = new File(realInner, "file1");
        FileUtils.touch(realFile);

        assertEquals(1, realInner.list().length);

        final File randomFile = new File(top, "randomfile");
        FileUtils.touch(randomFile);

        final File symlinkFile = new File(realInner, "fakeinner");
        assertTrue(setupSymlink(randomFile, symlinkFile));

        assertEquals(2, realInner.list().length);
        assertEquals(2, top.list().length);

        // assert the real directory were removed including the symlink
        FileUtils.deleteDirectory(realOuter);
        assertEquals(1, top.list().length);

        // ensure that the contents of the symlink were NOT removed.
        assertTrue(randomFile.exists());
        assertFalse(symlinkFile.exists());
    }

    @Test
    public void testDeleteInvalidLinks() throws Exception {
        final File aFile = new File(top, "realParentDirA");
        assertTrue(aFile.mkdir());
        final File bFile = new File(aFile, "realChildDirB");
        assertTrue(bFile.mkdir());

        final File cFile = new File(top, "realParentDirC");
        assertTrue(cFile.mkdir());
        final File dFile = new File(cFile, "realChildDirD");
        assertTrue(dFile.mkdir());

        final File linkToC = new File(bFile, "linkToC");
        Files.createSymbolicLink(linkToC.toPath(), cFile.toPath());

        final File linkToB = new File(dFile, "linkToB");
        Files.createSymbolicLink(linkToB.toPath(), bFile.toPath());

        FileUtils.deleteDirectory(aFile);
        FileUtils.deleteDirectory(cFile);
        assertEquals(0, top.list().length);
    }

    @Test
    public void testDeleteParentSymlink() throws Exception {
        final File realParent = new File(top, "realparent");
        assertTrue(realParent.mkdirs());

        final File realInner = new File(realParent, "realinner");
        assertTrue(realInner.mkdirs());

        FileUtils.touch(new File(realInner, "file1"));
        assertEquals(1, realInner.list().length);

        final File randomDirectory = new File(top, "randomDir");
        assertTrue(randomDirectory.mkdirs());

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertEquals(1, randomDirectory.list().length);

        final File symlinkDirectory = new File(realParent, "fakeinner");
        assertTrue(setupSymlink(randomDirectory, symlinkDirectory));

        assertEquals(1, symlinkDirectory.list().length);

        final File symlinkParentDirectory = new File(top, "fakeouter");
        assertTrue(setupSymlink(realParent, symlinkParentDirectory));

        // assert only the symlink is deleted, but not followed
        FileUtils.deleteDirectory(symlinkParentDirectory);
        assertEquals(2, top.list().length);

        // ensure that the contents of the symlink were NOT removed.
        assertEquals(1, randomDirectory.list().length, "Contents of sym link should not have been removed");
    }

    @Test
    public void testDeleteParentSymlink2() throws Exception {
        final File realParent = new File(top, "realparent");
        assertTrue(realParent.mkdirs());

        final File realInner = new File(realParent, "realinner");
        assertTrue(realInner.mkdirs());

        FileUtils.touch(new File(realInner, "file1"));
        assertEquals(1, realInner.list().length);

        final File randomDirectory = new File(top, "randomDir");
        assertTrue(randomDirectory.mkdirs());

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertEquals(1, randomDirectory.list().length);

        final File symlinkDirectory = new File(realParent, "fakeinner");
        Files.createSymbolicLink(symlinkDirectory.toPath(), randomDirectory.toPath());

        assertEquals(1, symlinkDirectory.list().length);

        final File symlinkParentDirectory = new File(top, "fakeouter");
        Files.createSymbolicLink(symlinkParentDirectory.toPath(), realParent.toPath());

        // assert only the symlink is deleted, but not followed
        FileUtils.deleteDirectory(symlinkParentDirectory);
        assertEquals(2, top.list().length);

        // ensure that the contents of the symlink were NOT removed.
        assertEquals(1, randomDirectory.list().length, "Contents of sym link should not have been removed");
    }

    @Test
    public void testDeletesNested() throws Exception {
        final File nested = new File(top, "nested");
        assertTrue(nested.mkdirs());

        assertEquals(1, top.list().length);

        FileUtils.touch(new File(nested, "regular"));
        FileUtils.touch(new File(nested, ".hidden"));

        assertEquals(2, nested.list().length);

        FileUtils.deleteDirectory(nested);

        assertEquals(0, top.list().length);
    }

    @Test
    public void testDeletesRegular() throws Exception {
        final File nested = new File(top, "nested");
        assertTrue(nested.mkdirs());

        assertEquals(1, top.list().length);

        assertEquals(0, nested.list().length);

        FileUtils.deleteDirectory(nested);

        assertEquals(0, top.list().length);
    }

}
