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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link RandomAccessFiles}.
 */
public class RandomAccessFilesTest {

    protected static final String FILE_RES_RO = "/org/apache/commons/io/test-file-20byteslength.bin";
    protected static final String FILE_NAME_RO = "src/test/resources" + FILE_RES_RO;

    @Test
    public void testRead() throws IOException {
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 0, 0);
            assertArrayEquals(new byte[] {}, buffer);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 1, 0);
            assertArrayEquals(new byte[] {}, buffer);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 0, 1);
            assertArrayEquals(new byte[] { '1' }, buffer);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 1, 1);
            assertArrayEquals(new byte[] { '2' }, buffer);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO)) {
            final byte[] buffer = RandomAccessFiles.read(raf, 0, 20);
            assertEquals(20, buffer.length);
        }
        try (final RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(FILE_NAME_RO)) {
            assertThrows(IOException.class, () -> RandomAccessFiles.read(raf, 0, 21));
        }
    }
}
