/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.build.AbstractOriginTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link IORandomAccessFile}.
 */
class IORandomAccessFileTest {

    protected static final String FILE_NAME_RW = "target/" + AbstractOriginTest.class.getSimpleName() + ".txt";

    private File newFileFixture() throws IOException {
        final File file = new File(FILE_NAME_RW);
        FileUtils.touch(file);
        return file;
    }

    /**
     * Tests {@link IORandomAccessFile#clear()} by writing known non-zero bytes to the file, calling {@code clear()},
     * and verifying that every byte in the file is {@code 0}.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testClear() throws IOException {
        final File file = newFileFixture();
        final byte[] originalData = { 1, 2, 3, 4, 5, 6, 7, 8 };
        // Write non-zero data into the file first
        Files.write(file.toPath(), originalData);
        try (IORandomAccessFile raf = new IORandomAccessFile(file, "rw")) {
            assertEquals(originalData.length, raf.length());
            // clear() should overwrite every byte with 0 and return 'this'
            @SuppressWarnings("resource")
            final IORandomAccessFile echoRaf = raf.clear();
            assertSame(raf, echoRaf);
            // Seek back to the start and read the contents
            raf.seek(0);
            final byte[] result = new byte[originalData.length];
            raf.readFully(result);
            assertArrayEquals(new byte[originalData.length], result, "All bytes should be 0 after clear()");
        }
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    void testFile(final RandomAccessFileMode mode) throws IOException {
        final File file = newFileFixture();
        final String modeStr = mode.getMode();
        try (IORandomAccessFile raf = new IORandomAccessFile(file, modeStr)) {
            assertEquals(file, raf.getFile());
            assertEquals(modeStr, raf.getMode());
        }
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    void testString(final RandomAccessFileMode mode) throws IOException {
        final File file = newFileFixture();
        final String modeStr = mode.getMode();
        try (IORandomAccessFile raf = new IORandomAccessFile(FILE_NAME_RW, modeStr)) {
            assertEquals(file, raf.getFile());
            assertEquals(modeStr, raf.getMode());
        }
    }

    @Test
    void testToString() throws IOException {
        final File file = newFileFixture();
        try (IORandomAccessFile raf = new IORandomAccessFile(FILE_NAME_RW, "r")) {
            assertEquals(file.toString(), raf.toString());
        }
    }
}
