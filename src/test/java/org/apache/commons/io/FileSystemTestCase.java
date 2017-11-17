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

import org.junit.Assert;
import org.junit.Test;

public class FileSystemTestCase {

    @Test
    public void testSorted() {
        for (final FileSystem fs : FileSystem.values()) {
            final char[] chars = fs.getIllegalFileNameChars();
            for (int i = 0; i < chars.length - 1; i++) {
                Assert.assertTrue(fs.name(), chars[i] < chars[i + 1]);
            }
        }
    }

    @Test
    public void testToLegalFileNameWindows() {
        final FileSystem fs = FileSystem.WINDOWS;
        final char replacement = '-';
        for (char i = 0; i < 32; i++) {
            Assert.assertEquals(replacement, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        final char[] illegal = new char[] { '<', '>', ':', '"', '/', '\\', '|', '?', '*' };
        for (char i = 0; i < illegal.length; i++) {
            Assert.assertEquals(replacement, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        for (char i = 'a'; i < 'z'; i++) {
            Assert.assertEquals(i, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        for (char i = 'A'; i < 'Z'; i++) {
            Assert.assertEquals(i, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        for (char i = '0'; i < '9'; i++) {
            Assert.assertEquals(i, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
    }

    @Test
    public void testIsLegalName() {
        for (final FileSystem fs : FileSystem.values()) {
            Assert.assertFalse(fs.name(), fs.isLegalFileName("")); // Empty is always illegal
            Assert.assertFalse(fs.name(), fs.isLegalFileName(null)); // null is always illegal
            Assert.assertFalse(fs.name(), fs.isLegalFileName("\0")); // Assume NUL is always illegal
            Assert.assertTrue(fs.name(), fs.isLegalFileName("0")); // Assume simple name always legal
            for (final String candidate : fs.getReservedFileNames()) {
                // Reserved file names are not legal
                Assert.assertFalse(fs.isLegalFileName(candidate));
            }
        }
    }

    @Test
    public void testIsReservedFileName() {
        for (final FileSystem fs : FileSystem.values()) {
            for (final String candidate : fs.getReservedFileNames()) {
                Assert.assertTrue(fs.isReservedFileName(candidate));
            }
        }
    }

    @Test
    public void testReplacementWithNUL() {
        for (final FileSystem fs : FileSystem.values()) {
            try {
                fs.toLegalFileName("Test", '\0'); // Assume NUL is always illegal
            } catch (final IllegalArgumentException iae) {
                Assert.assertTrue(iae.getMessage(), iae.getMessage().startsWith("The replacement character '\\0'"));
            }
        }
    }
}
