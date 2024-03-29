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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link FileSystemUtils}.
 */
@SuppressWarnings("deprecation") // testing deprecated class
public class FileSystemUtilsTest {

    static char[] getIllegalFileNameChars() {
        return FileSystem.getCurrent().getIllegalFileNameChars();
    }

    @ParameterizedTest
    @MethodSource("getIllegalFileNameChars")
    public void testGetFreeSpace_IllegalFileName(final char illegalFileNameChar) throws Exception {
        assertThrows(IllegalArgumentException.class, () -> FileSystemUtils.freeSpace("\\ \"" + illegalFileNameChar));
    }

    @Test
    public void testGetFreeSpace_IllegalFileNames() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> FileSystemUtils.freeSpace("\\ \""));
    }

    @Test
    public void testGetFreeSpace_String() throws Exception {
        assertThrows(NullPointerException.class, () -> FileSystemUtils.freeSpace(null));
        assertThrows(IllegalArgumentException.class, () -> FileSystemUtils.freeSpace("this directory does not exist, at all."));
        // "" means current dir.
        assertTrue(FileSystemUtils.freeSpace("") > 0);
        assertTrue(FileSystemUtils.freeSpace("target") > 0);
        // files worked as well in previous versions.
        assertTrue(FileSystemUtils.freeSpace("pom.xml") > 0);
    }

    @Test
    public void testGetFreeSpaceKb() throws Exception {
        assertTrue(FileSystemUtils.freeSpaceKb() > 0);
    }

    @Test
    public void testGetFreeSpaceKb_long() throws Exception {
        assertTrue(FileSystemUtils.freeSpaceKb(0) > 0);
    }

    @Test
    public void testGetFreeSpaceKb_String() throws Exception {
        assertThrows(NullPointerException.class, () -> FileSystemUtils.freeSpaceKb(null));
        assertThrows(IllegalArgumentException.class, () -> FileSystemUtils.freeSpaceKb("this directory does not exist, at all."));
        // "" means current dir.
        assertTrue(FileSystemUtils.freeSpaceKb("") > 0);
        assertTrue(FileSystemUtils.freeSpaceKb("target") > 0);
        // files worked as well in previous versions.
        assertTrue(FileSystemUtils.freeSpaceKb("pom.xml") > 0);
    }

    @Test
    public void testGetFreeSpaceKb_String_long() throws Exception {
        assertThrows(NullPointerException.class, () -> FileSystemUtils.freeSpaceKb(null, 0));
        assertThrows(IllegalArgumentException.class, () -> FileSystemUtils.freeSpaceKb("this directory does not exist, at all.", 0));
        // "" means current dir.
        assertTrue(FileSystemUtils.freeSpaceKb("", 0) > 0);
        assertTrue(FileSystemUtils.freeSpaceKb("target", 0) > 0);
        // files worked as well in previous versions.
        assertTrue(FileSystemUtils.freeSpaceKb("pom.xml", 0) > 0);
    }

}
