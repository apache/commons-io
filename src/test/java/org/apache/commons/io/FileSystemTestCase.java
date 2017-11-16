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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class FileSystemTestCase {

    @Test
    public void testToLegalFileNameWindows() {
        FileSystem fs = FileSystem.WINDOWS;
        char replacement = '-';
        for (char i = 0; i < 32; i++) {
            Assert.assertEquals(replacement, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        char[] illegal = new char[] { '<', '>', ':', '"', '/', '\\', '|', '?', '*' };
        Arrays.sort(illegal);
        System.out.println(Arrays.toString(illegal));
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
}
