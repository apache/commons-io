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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link RandomAccessFileMode}.
 */
public class RandomAccessFileModeTest {

    private static final byte[] BYTES_FIXTURE = "Foo".getBytes(StandardCharsets.US_ASCII);

    private static final String FIXTURE = "test.txt";

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
        final byte[] expected = BYTES_FIXTURE;
        final Path fixture = writeFixture(expected);
        try (RandomAccessFile randomAccessFile = randomAccessFileMode.create(fixture.toFile())) {
            assertArrayEquals(expected, read(randomAccessFile));
        }
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    public void testCreatePath(final RandomAccessFileMode randomAccessFileMode) throws IOException {
        final byte[] expected = BYTES_FIXTURE;
        final Path fixture = writeFixture(expected);
        randomAccessFileMode.accept(fixture, raf -> assertArrayEquals(expected, read(raf)));
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    public void testCreateString(final RandomAccessFileMode randomAccessFileMode) throws IOException {
        final byte[] expected = BYTES_FIXTURE;
        final Path fixture = writeFixture(expected);
        try (RandomAccessFile randomAccessFile = randomAccessFileMode.create(fixture.toString())) {
            assertArrayEquals(expected, read(randomAccessFile));
        }
    }

    @Test
    public void testGetMode() {
        assertEquals("r", RandomAccessFileMode.READ_ONLY.getMode());
        assertEquals("rw", RandomAccessFileMode.READ_WRITE.getMode());
        assertEquals("rwd", RandomAccessFileMode.READ_WRITE_SYNC_CONTENT.getMode());
        assertEquals("rws", RandomAccessFileMode.READ_WRITE_SYNC_ALL.getMode());
    }

    @Test
    public void testImplies() {
        assertTrue(RandomAccessFileMode.READ_WRITE_SYNC_ALL.implies(RandomAccessFileMode.READ_WRITE_SYNC_CONTENT));
        assertTrue(RandomAccessFileMode.READ_WRITE_SYNC_CONTENT.implies(RandomAccessFileMode.READ_WRITE));
        assertTrue(RandomAccessFileMode.READ_WRITE.implies(RandomAccessFileMode.READ_ONLY));
        assertFalse(RandomAccessFileMode.READ_ONLY.implies(RandomAccessFileMode.READ_WRITE_SYNC_ALL));
    }

    @ParameterizedTest
    @EnumSource(RandomAccessFileMode.class)
    public void testIoString(final RandomAccessFileMode randomAccessFileMode) throws IOException {
        final byte[] expected = BYTES_FIXTURE;
        final Path fixture = writeFixture(expected);
        try (IORandomAccessFile randomAccessFile = randomAccessFileMode.io(fixture.toString())) {
            assertArrayEquals(expected, read(randomAccessFile));
        }
    }

    /**
     * Tests the standard {@link Enum#toString()} behavior.
     */
    @Test
    public void testToString() {
        assertEquals("READ_ONLY", RandomAccessFileMode.READ_ONLY.toString());
        assertEquals("READ_WRITE", RandomAccessFileMode.READ_WRITE.toString());
        assertEquals("READ_WRITE_SYNC_ALL", RandomAccessFileMode.READ_WRITE_SYNC_ALL.toString());
        assertEquals("READ_WRITE_SYNC_CONTENT", RandomAccessFileMode.READ_WRITE_SYNC_CONTENT.toString());
    }

    @ParameterizedTest
    @EnumSource(LinkOption.class)
    public void testValueOf(final LinkOption option) {
        assertTrue(RandomAccessFileMode.valueOf(option).implies(RandomAccessFileMode.READ_ONLY));
    }

    @ParameterizedTest
    @EnumSource(StandardOpenOption.class)
    public void testValueOf(final StandardOpenOption option) {
        assertTrue(RandomAccessFileMode.valueOf(option).implies(RandomAccessFileMode.READ_ONLY));
    }

    @Test
    public void testValueOfMode() {
        assertEquals(RandomAccessFileMode.READ_ONLY, RandomAccessFileMode.valueOfMode("r"));
        assertEquals(RandomAccessFileMode.READ_WRITE, RandomAccessFileMode.valueOfMode("rw"));
        assertEquals(RandomAccessFileMode.READ_WRITE_SYNC_CONTENT, RandomAccessFileMode.valueOfMode("rwd"));
        assertEquals(RandomAccessFileMode.READ_WRITE_SYNC_ALL, RandomAccessFileMode.valueOfMode("rws"));
    }

    @Test
    public void testValueOfOpenOptions() {
        // READ_ONLY
        assertEquals(RandomAccessFileMode.READ_ONLY, RandomAccessFileMode.valueOf(StandardOpenOption.READ));
        // READ_WRITE
        assertEquals(RandomAccessFileMode.READ_WRITE, RandomAccessFileMode.valueOf(StandardOpenOption.WRITE));
        assertEquals(RandomAccessFileMode.READ_WRITE, RandomAccessFileMode.valueOf(StandardOpenOption.READ, StandardOpenOption.WRITE));
        // READ_WRITE_SYNC_CONTENT
        assertEquals(RandomAccessFileMode.READ_WRITE_SYNC_CONTENT, RandomAccessFileMode.valueOf(StandardOpenOption.DSYNC));
        assertEquals(RandomAccessFileMode.READ_WRITE_SYNC_CONTENT, RandomAccessFileMode.valueOf(StandardOpenOption.WRITE, StandardOpenOption.DSYNC));
        assertEquals(RandomAccessFileMode.READ_WRITE_SYNC_CONTENT,
                RandomAccessFileMode.valueOf(StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC));
        // READ_WRITE_SYNC_ALL
        assertEquals(RandomAccessFileMode.READ_WRITE_SYNC_ALL, RandomAccessFileMode.valueOf(StandardOpenOption.SYNC));
        assertEquals(RandomAccessFileMode.READ_WRITE_SYNC_ALL, RandomAccessFileMode.valueOf(StandardOpenOption.READ, StandardOpenOption.SYNC));
        assertEquals(RandomAccessFileMode.READ_WRITE_SYNC_ALL,
                RandomAccessFileMode.valueOf(StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.SYNC));
    }

    private Path writeFixture(final byte[] bytes) throws IOException {
        return Files.write(tempDir.resolve(FIXTURE), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
