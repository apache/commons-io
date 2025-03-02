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
import java.nio.file.Paths;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link RandomAccessFiles}.
 */
public class RandomAccessFilesTest {

    private static final Path PATH_RO_20 = Paths.get("src/test/resources/org/apache/commons/io/test-file-20byteslength.bin");
    private static final Path PATH_RO_0 = Paths.get("src/test/resources/org/apache/commons/io/test-file-empty.bin");
    private static final Path PATH_RO_0_BIS = Paths.get("src/test/resources/org/apache/commons/io/test-file-empty2.bin");

    private static byte reverse(final byte b) {
        return (byte) (~b & 0xff);
    }

    @ParameterizedTest()
    @EnumSource(value = RandomAccessFileMode.class)
    public void testContentEquals(final RandomAccessFileMode mode) throws IOException {
        mode.accept(PATH_RO_20, raf -> {
            assertEquals(raf, raf);
            assertTrue(RandomAccessFiles.contentEquals(raf, raf));
        });
        // as above, to make sure resources are OK
        mode.accept(PATH_RO_20, raf -> {
            assertEquals(raf, raf);
            assertTrue(RandomAccessFiles.contentEquals(raf, raf));
        });
        // same 20 bytes, 2 RAFs
        try (RandomAccessFile raf1 = mode.create(PATH_RO_20);
                RandomAccessFile raf2 = mode.create(PATH_RO_20)) {
            assertTrue(RandomAccessFiles.contentEquals(raf1, raf2));
        }
        // as above, nested
        mode.accept(PATH_RO_20, raf1 -> mode.accept(PATH_RO_20, raf2 -> assertTrue(RandomAccessFiles.contentEquals(raf1, raf2))));
        // same empty file
        try (RandomAccessFile raf1 = mode.create(PATH_RO_0);
                RandomAccessFile raf2 = mode.create(PATH_RO_0)) {
            assertTrue(RandomAccessFiles.contentEquals(raf1, raf2));
            assertTrue(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf2), RandomAccessFiles.reset(raf1)));
        }
        // diff empty file
        try (RandomAccessFile raf1 = mode.create(PATH_RO_0);
                RandomAccessFile raf2 = mode.create(PATH_RO_0_BIS)) {
            assertTrue(RandomAccessFiles.contentEquals(raf1, raf2));
            assertTrue(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf2), RandomAccessFiles.reset(raf1)));
        }
        try (RandomAccessFile raf1 = mode.create(PATH_RO_0);
                RandomAccessFile raf2 = mode.create(PATH_RO_20)) {
            assertFalse(RandomAccessFiles.contentEquals(raf1, raf2));
            assertFalse(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf2), RandomAccessFiles.reset(raf1)));
        }
        //
        final Path bigFile1 = Files.createTempFile(getClass().getSimpleName(), "-1.bin");
        final Path bigFile2 = Files.createTempFile(getClass().getSimpleName(), "-2.bin");
        final Path bigFile3 = Files.createTempFile(getClass().getSimpleName(), "-3.bin");
        try {
            // This length must match any restriction from the Surefire configuration.
            final int newLength = 2_000_000;
            final byte[] bytes1 = new byte[newLength];
            final byte[] bytes2 = new byte[newLength];
            // Make sure bytes1 and bytes2 are different despite the shuffle
            ArrayUtils.shuffle(bytes1);
            bytes1[0] = 1;
            ArrayUtils.shuffle(bytes2);
            bytes2[0] = 2;
            Files.write(bigFile1, bytes1);
            Files.write(bigFile2, bytes2);
            try (RandomAccessFile raf1 = mode.create(bigFile1);
                    RandomAccessFile raf2 = mode.create(bigFile2)) {
                assertFalse(RandomAccessFiles.contentEquals(raf1, raf2));
                assertFalse(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf2), RandomAccessFiles.reset(raf1)));
                assertTrue(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf1), RandomAccessFiles.reset(raf1)));
            }
            // Make the LAST byte different.
            byte[] bytes3 = bytes1.clone();
            final int last = bytes3.length - 1;
            bytes3[last] = reverse(bytes3[last]);
            Files.write(bigFile3, bytes3);
            try (RandomAccessFile raf1 = mode.create(bigFile1);
                    RandomAccessFile raf3 = mode.create(bigFile3)) {
                assertFalse(RandomAccessFiles.contentEquals(raf1, raf3));
                assertFalse(RandomAccessFiles.contentEquals(RandomAccessFiles.reset(raf3), RandomAccessFiles.reset(raf1)));
            }
            // Make a byte in the middle different
            bytes3 = bytes1.clone();
            final int middle = bytes3.length / 2;
            bytes3[middle] = reverse(bytes3[middle]);
            Files.write(bigFile3, bytes3);
            try (RandomAccessFile raf1 = mode.create(bigFile1);
                    RandomAccessFile raf3 = mode.create(bigFile3)) {
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

    @ParameterizedTest()
    @EnumSource(value = RandomAccessFileMode.class)
    public void testRead(final RandomAccessFileMode mode) throws IOException {
        mode.accept(PATH_RO_20, raf -> assertArrayEquals(new byte[] {}, RandomAccessFiles.read(raf, 0, 0)));
        mode.accept(PATH_RO_20, raf -> assertArrayEquals(new byte[] {}, RandomAccessFiles.read(raf, 1, 0)));
        mode.accept(PATH_RO_20, raf -> assertArrayEquals(new byte[] { '1' }, RandomAccessFiles.read(raf, 0, 1)));
        mode.accept(PATH_RO_20, raf -> assertArrayEquals(new byte[] { '2' }, RandomAccessFiles.read(raf, 1, 1)));
        mode.accept(PATH_RO_20, raf -> assertEquals(20, RandomAccessFiles.read(raf, 0, 20).length));
        mode.accept(PATH_RO_20, raf -> assertThrows(IOException.class, () -> RandomAccessFiles.read(raf, 0, 21)));
    }
}
