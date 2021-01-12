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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test cases for FileUtils.cleanDirectory() method.
 *
 * TODO Redo this test using
 * {@link Files#createSymbolicLink(java.nio.file.Path, java.nio.file.Path, java.nio.file.attribute.FileAttribute...)}.
 */
public class FileUtilsCleanDirectoryTestCase {

    @TempDir
    public File top;

    // -----------------------------------------------------------------------
    @Test
    public void testCleanEmpty() throws Exception {
        assertEquals(0, top.list().length);

        FileUtils.cleanDirectory(top);

        assertEquals(0, top.list().length);
    }

    @Test
    public void testDeletesRegular() throws Exception {
        FileUtils.touch(new File(top, "regular"));
        FileUtils.touch(new File(top, ".hidden"));

        assertEquals(2, top.list().length);

        FileUtils.cleanDirectory(top);

        assertEquals(0, top.list().length);
    }

    @Test
    public void testDeletesNested() throws Exception {
        final File nested = new File(top, "nested");

        assertTrue(nested.mkdirs());

        FileUtils.touch(new File(nested, "file"));

        assertEquals(1, top.list().length);

        FileUtils.cleanDirectory(top);

        assertEquals(0, top.list().length);
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void testThrowsOnNullList() throws Exception {
        // test wont work if we can't restrict permissions on the
        // directory, so skip it.
        assumeTrue(chmod(top, 0, false));

        try {
            // cleanDirectory calls forceDelete
            FileUtils.cleanDirectory(top);
            fail("expected IOException");
        } catch (final IOException e) {
            assertEquals("Failed to list contents of " + top.getAbsolutePath(), e.getMessage());
        } finally {
            chmod(top, 755, false);
        }
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void testThrowsOnCannotDeleteFile() throws Exception {
        final File file = new File(top, "restricted");
        FileUtils.touch(file);

        assumeTrue(chmod(top, 500, false));

        try {
            // cleanDirectory calls forceDelete
            FileUtils.cleanDirectory(top);
            fail("expected IOException");
        } catch (final IOException e) {
            final IOExceptionList list = (IOExceptionList) e;
            assertEquals("Cannot delete file: " + file.getAbsolutePath(), list.getCause(0).getMessage());
        } finally {
            chmod(top, 755, false);
        }
    }

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

}
