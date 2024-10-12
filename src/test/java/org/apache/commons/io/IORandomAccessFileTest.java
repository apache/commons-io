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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.build.AbstractOriginTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link IORandomAccessFile}.
 */
public class IORandomAccessFileTest {

    protected static final String FILE_NAME_RW = "target/" + AbstractOriginTest.class.getSimpleName() + ".txt";

    private File newFileFixture() throws IOException {
        final File file = new File(FILE_NAME_RW);
        FileUtils.touch(file);
        return file;
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    public void testFile(final RandomAccessFileMode mode) throws IOException {
        final File file = newFileFixture();
        final String modeStr = mode.getMode();
        try (IORandomAccessFile raf = new IORandomAccessFile(file, modeStr)) {
            assertEquals(file, raf.getFile());
            assertEquals(modeStr, raf.getMode());
        }
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    public void testString(final RandomAccessFileMode mode) throws IOException {
        final File file = newFileFixture();
        final String modeStr = mode.getMode();
        try (IORandomAccessFile raf = new IORandomAccessFile(FILE_NAME_RW, modeStr)) {
            assertEquals(file, raf.getFile());
            assertEquals(modeStr, raf.getMode());
        }
    }

    @Test
    public void testToString() throws IOException {
        final File file = newFileFixture();
        try (IORandomAccessFile raf = new IORandomAccessFile(FILE_NAME_RW, "r")) {
            assertEquals(file.toString(), raf.toString());
        }
    }
}
