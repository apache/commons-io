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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.file.AbstractTempDirTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Test cases for FileUtils.cleanDirectory() method.
 *
 * TODO Redo this test using
 * {@link Files#createSymbolicLink(java.nio.file.Path, java.nio.file.Path, java.nio.file.attribute.FileAttribute...)}.
 */
public class FileUtilsCleanDirectoryTest extends AbstractTempDirTest {

    /** Only runs on Linux. */
    private boolean chmod(final File file, final int mode, final boolean recurse) throws InterruptedException {
        final List<String> args = new ArrayList<>();
        args.add("chmod");

        if (recurse) {
            args.add("-R");
        }

        args.add(Integer.toString(mode));
        args.add(file.getAbsolutePath());

        final Process proc;

        try {
            proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
        } catch (final IOException e) {
            return false;
        }
        return proc.waitFor() == 0;
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void testCleanDirectoryToForceDelete() throws Exception {
        final File file = new File(tempDirFile, "restricted");
        FileUtils.touch(file);

        // 300 = owner: WE.
        // 500 = owner: RE.
        // 700 = owner: RWE.
        assumeTrue(chmod(tempDirFile, 700, false));

        // cleanDirectory calls forceDelete
        FileUtils.cleanDirectory(tempDirFile);
    }

    @Test
    public void testCleanEmpty() throws Exception {
        assertEquals(0, tempDirFile.list().length);

        FileUtils.cleanDirectory(tempDirFile);

        assertEquals(0, tempDirFile.list().length);
    }

    @Test
    public void testDeletesNested() throws Exception {
        final File nested = new File(tempDirFile, "nested");

        assertTrue(nested.mkdirs());

        FileUtils.touch(new File(nested, "file"));

        assertEquals(1, tempDirFile.list().length);

        FileUtils.cleanDirectory(tempDirFile);

        assertEquals(0, tempDirFile.list().length);
    }

    @Test
    public void testDeletesRegular() throws Exception {
        FileUtils.touch(new File(tempDirFile, "regular"));
        FileUtils.touch(new File(tempDirFile, ".hidden"));

        assertEquals(2, tempDirFile.list().length);

        FileUtils.cleanDirectory(tempDirFile);

        assertEquals(0, tempDirFile.list().length);
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void testThrowsOnNullList() throws Exception {
        // test wont work if we can't restrict permissions on the
        // directory, so skip it.
        assumeTrue(chmod(tempDirFile, 0, false));

        try {
            // cleanDirectory calls forceDelete
            FileUtils.cleanDirectory(tempDirFile);
            fail("expected IOException");
        } catch (final IOException e) {
            assertEquals("Unknown I/O error listing contents of directory: " + tempDirFile.getAbsolutePath(), e.getMessage());
        } finally {
            chmod(tempDirFile, 755, false);
        }
    }

}
