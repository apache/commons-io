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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.testtools.FileBasedTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for FileUtils.cleanDirectory() method.
 *
 * @version $Id$
 */
public class FileUtilsCleanDirectoryTestCase extends FileBasedTestCase {
    final File top = getLocalTestDirectory();

    private File getLocalTestDirectory() {
        return new File(getTestDirectory(), "list-files");
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        top.mkdirs();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        chmod(top, 775, true);
        FileUtils.deleteDirectory(top);
        FileUtils.deleteDirectory(getTestDirectory());
    }

    //-----------------------------------------------------------------------
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

    @Test
    public void testThrowsOnNullList() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")  ||  !chmod(top, 0, false)) {
            // test wont work if we can't restrict permissions on the
            // directory, so skip it.
            return;
        }

        try {
            FileUtils.cleanDirectory(top);
            fail("expected IOException");
        } catch (final IOException e) {
            assertEquals("Failed to list contents of " +
                    top.getAbsolutePath(), e.getMessage());
        }
    }

    @Test
    public void testThrowsOnCannotDeleteFile() throws Exception {
        final File file = new File(top, "restricted");
        FileUtils.touch(file);

        if (System.getProperty("os.name").startsWith("Win")  ||  !chmod(top, 500, false)) {
            // test wont work if we can't restrict permissions on the
            // directory, so skip it.
            return;
        }

        try {
            FileUtils.cleanDirectory(top);
            fail("expected IOException");
        } catch (final IOException e) {
            assertEquals("Unable to delete file: " +
                    file.getAbsolutePath(), e.getMessage());
        }
    }

    private boolean chmod(final File file, final int mode, final boolean recurse)
            throws InterruptedException {
        // TODO: Refactor this to FileSystemUtils
        final List<String> args = new ArrayList<>();
        args.add("chmod");

        if (recurse) {
            args.add("-R");
        }

        args.add(Integer.toString(mode));
        args.add(file.getAbsolutePath());

        Process proc;

        try {
            proc = Runtime.getRuntime().exec(
                    args.toArray(new String[args.size()]));
        } catch (final IOException e) {
            return false;
        }
        final int result = proc.waitFor();
        return result == 0;
    }

}
