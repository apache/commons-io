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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link RandomAccessFileMode}.
 */
public class RandomAccessFileModeTest {

    /**
     * Temporary directory.
     */
    @TempDir
    public Path tempDir;

    private byte[] read(final RandomAccessFile randomAccessFile) throws IOException {
        return RandomAccessFiles.read(randomAccessFile, 0, (int) randomAccessFile.length());
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    public void testCreateFile(final RandomAccessFileMode randomAccessFileMode) throws IOException {
        final byte[] expected = "Foo".getBytes(StandardCharsets.US_ASCII);
        final Path fixture = Files.write(tempDir.resolve("test.txt"), expected);
        try (RandomAccessFile randomAccessFile = randomAccessFileMode.create(fixture.toFile())) {
            assertArrayEquals(expected, read(randomAccessFile));
        }
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    public void testCreatePath(final RandomAccessFileMode randomAccessFileMode) throws IOException {
        final byte[] expected = "Foo".getBytes(StandardCharsets.US_ASCII);
        final Path fixture = Files.write(tempDir.resolve("test.txt"), expected);
        try (RandomAccessFile randomAccessFile = randomAccessFileMode.create(fixture)) {
            assertArrayEquals(expected, read(randomAccessFile));
        }
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    public void testCreateString(final RandomAccessFileMode randomAccessFileMode) throws IOException {
        final byte[] expected = "Foo".getBytes(StandardCharsets.US_ASCII);
        final Path fixture = Files.write(tempDir.resolve("test.txt"), expected);
        try (RandomAccessFile randomAccessFile = randomAccessFileMode.create(fixture.toString())) {
            assertArrayEquals(expected, read(randomAccessFile));
        }
    }

    @Test
    public void testToString() {
        assertEquals("r", RandomAccessFileMode.READ_ONLY.toString());
        assertEquals("rw", RandomAccessFileMode.READ_WRITE.toString());
        assertEquals("rws", RandomAccessFileMode.READ_WRITE_SYNC_ALL.toString());
        assertEquals("rwd", RandomAccessFileMode.READ_WRITE_SYNC_CONTENT.toString());
    }
}
