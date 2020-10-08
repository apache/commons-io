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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test cases for FileUtils.cleanDirectory() method that involve symlinks.
 * &amp; FileUtils.isSymlink(File file)
 */
public class FileUtilsCleanSymlinksTestCase {

    @TempDir
    public File top;

    @Test
    public void testCleanDirWithSymlinkFile() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // cant create symlinks in windows.
            return;
        }

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

        // assert contents of the real directory were removed including the symlink
        FileUtils.cleanDirectory(realOuter);
        assertEquals(0, realOuter.list().length);

        // ensure that the contents of the symlink were NOT removed.
        assertTrue(randomFile.exists());
        assertFalse(symlinkFile.exists());
    }


    @Test
    public void testCleanDirWithASymlinkDir() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // cant create symlinks in windows.
            return;
        }

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
        FileUtils.cleanDirectory(realOuter);
        assertEquals(0, realOuter.list().length);

        // ensure that the contents of the symlink were NOT removed.
        assertEquals(1, randomDirectory.list().length, "Contents of sym link should not have been removed");
    }

    @Test
    public void testCleanDirWithParentSymlinks() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // cant create symlinks in windows.
            return;
        }

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

        // assert contents of the real directory were removed including the symlink
        FileUtils.cleanDirectory(symlinkParentDirectory);// should clean the contents of this but not recurse into other links
        assertEquals(0, symlinkParentDirectory.list().length);
        assertEquals(0, realParent.list().length);

        // ensure that the contents of the symlink were NOT removed.
        assertEquals(1, randomDirectory.list().length, "Contents of sym link should not have been removed");
    }

    @Test
    public void testStillClearsIfGivenDirectoryIsASymlink() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // cant create symlinks in windows.
            return;
        }

        final File randomDirectory = new File(top, "randomDir");
        assertTrue(randomDirectory.mkdirs());

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertEquals(1, randomDirectory.list().length);

        final File symlinkDirectory = new File(top, "fakeDir");
        assertTrue(setupSymlink(randomDirectory, symlinkDirectory));

        FileUtils.cleanDirectory(symlinkDirectory);
        assertEquals(0, symlinkDirectory.list().length);
        assertEquals(0, randomDirectory.list().length);
    }


    @Test
    public void testIdentifiesSymlinkDir() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // cant create symlinks in windows.
            return;
        }

        final File randomDirectory = new File(top, "randomDir");
        assertTrue(randomDirectory.mkdirs());

        final File symlinkDirectory = new File(top, "fakeDir");
        assertTrue(setupSymlink(randomDirectory, symlinkDirectory));

        assertTrue(FileUtils.isSymlink(symlinkDirectory));
        assertFalse(FileUtils.isSymlink(randomDirectory));
    }

    @Test
    public void testIdentifiesSymlinkFile() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // cant create symlinks in windows.
            return;
        }

        final File randomFile = new File(top, "randomfile");
        FileUtils.touch(randomFile);

        final File symlinkFile = new File(top, "fakeinner");
        assertTrue(setupSymlink(randomFile, symlinkFile));

        assertTrue(FileUtils.isSymlink(symlinkFile));
        assertFalse(FileUtils.isSymlink(randomFile));
    }

    @Test
    public void testIdentifiesBrokenSymlinkFile() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // cant create symlinks in windows.
            return;
        }

        final File noexistFile = new File(top, "noexist");
        final File symlinkFile = new File(top, "fakeinner");
        final File badSymlinkInPathFile = new File(symlinkFile, "fakeinner");
        final File noexistParentFile = new File("noexist", "file");

        assertTrue(setupSymlink(noexistFile, symlinkFile));

        assertTrue(FileUtils.isSymlink(symlinkFile));
        assertFalse(FileUtils.isSymlink(noexistFile));
        assertFalse(FileUtils.isSymlink(noexistParentFile));
        assertFalse(FileUtils.isSymlink(badSymlinkInPathFile));
    }

    @Test
    public void testCorrectlyIdentifySymlinkWithParentSymLink() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // cant create symlinks in windows.
            return;
        }

        final File realParent = new File(top, "realparent");
        assertTrue(realParent.mkdirs());

        final File symlinkParentDirectory = new File(top, "fakeparent");
        assertTrue(setupSymlink(realParent, symlinkParentDirectory));

        final File realChild = new File(symlinkParentDirectory, "realChild");
        assertTrue(realChild.mkdirs());

        final File symlinkChild = new File(symlinkParentDirectory, "fakeChild");
        assertTrue(setupSymlink(realChild, symlinkChild));

        assertTrue(FileUtils.isSymlink(symlinkChild));
        assertFalse(FileUtils.isSymlink(realChild));
    }

    private boolean setupSymlink(final File res, final File link) throws Exception {
        // create symlink
        final List<String> args = new ArrayList<>();
        args.add("ln");
        args.add("-s");

        args.add(res.getAbsolutePath());
        args.add(link.getAbsolutePath());

        Process proc;

        proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
        return proc.waitFor() == 0;
    }

}
