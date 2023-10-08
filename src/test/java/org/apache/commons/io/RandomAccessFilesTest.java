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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link RandomAccessFiles}.
 */
public class RandomAccessFilesTest {

    private static final String FILE_NAME_RO_20 = "src/test/resources/org/apache/commons/io/test-file-20byteslength.bin";
    private static final String FILE_NAME_RO_0 = "src/test/resources/org/apache/commons/io/test-file-empty.bin";
    private static final String FILE_NAME_RO_0_BIS = "src/test/resources/org/apache/commons/io/test-file-empty2.bin";

    @Test
    public void testContentEquals() throws IOException {
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            assertEquals(raf1, raf1);
            assertTrue(RandomAccessFiles.contentEquals(raf1, raf1));
        }
        // as above, to make sure resources are OK
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            assertEquals(raf1, raf1);
            assertTrue(RandomAccessFiles.contentEquals(raf1, raf1));
        }
        // same 20 bytes
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20);
                RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            assertTrue(RandomAccessFiles.contentEquals(raf1, raf2));
        }
        // same empty file
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_0);
                RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_0)) {
            assertTrue(RandomAccessFiles.contentEquals(raf1, raf2));
            assertTrue(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf2), RandomAccessFiles.reset(raf1)));
        }
        // diff empty file
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_0);
                RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_0_BIS)) {
            assertTrue(RandomAccessFiles.contentEquals(raf1, raf2));
            assertTrue(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf2), RandomAccessFiles.reset(raf1)));
        }
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_0);
                RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            assertFalse(RandomAccessFiles.contentEquals(raf1, raf2));
            assertFalse(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf2), RandomAccessFiles.reset(raf1)));
        }
        //
        final Path bigFile1 = Files.createTempFile(getClass().getSimpleName(), "-1.bin");
        final Path bigFile2 = Files.createTempFile(getClass().getSimpleName(), "-2.bin");
        final Path bigFile3 = Files.createTempFile(getClass().getSimpleName(), "-3.bin");
        try {
            final int newLength = 1_000_000;
            final byte[] bytes1 = new byte[newLength];
            final byte[] bytes2 = new byte[newLength];
            Arrays.fill(bytes1, (byte) 1);
            Arrays.fill(bytes2, (byte) 2);
            Files.write(bigFile1, bytes1);
            Files.write(bigFile2, bytes2);
            try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(bigFile1);
                    RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(bigFile2)) {
                assertFalse(RandomAccessFiles.contentEquals(raf1, raf2));
                assertFalse(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf2), RandomAccessFiles.reset(raf1)));
                assertTrue(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf1), RandomAccessFiles.reset(raf1)));
            }
            // Make the last byte different
            final byte[] bytes3 = bytes1.clone();
            bytes3[bytes3.length - 1] = 9;
            Files.write(bigFile3, bytes3);
            try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(bigFile1);
                    RandomAccessFile raf3 = RandomAccessFileMode.READ_ONLY.create(bigFile3)) {
                assertFalse(RandomAccessFiles.contentEquals(raf1, raf3));
                assertFalse(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf3), RandomAccessFiles.reset(raf1)));
            }
        } finally {
            // Delete ASAP
            Files.deleteIfExists(bigFile1);
            Files.deleteIfExists(bigFile2);
            Files.deleteIfExists(bigFile3);
        }
    }

    @Test
    public void testRead() throws IOException {
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 0, 0);
            assertArrayEquals(new byte[] {}, buffer);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 1, 0);
            assertArrayEquals(new byte[] {}, buffer);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 0, 1);
            assertArrayEquals(new byte[] { '1' }, buffer);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 1, 1);
            assertArrayEquals(new byte[] { '2' }, buffer);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 0, 20);
            assertEquals(20, buffer.length);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO_20)) {
            assertThrows(IOException.class, () -> RandomAccessFiles.read(raf, 0, 21));
        }
    }
}
