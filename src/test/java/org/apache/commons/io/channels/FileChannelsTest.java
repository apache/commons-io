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
package org.apache.commons.io.channels;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link FileChannels}.
 */
public class FileChannelsTest extends AbstractTempDirTest {

    private static final int SMALL_BUFFER_SIZE = 1024;
    private static final String CONTENT = StringUtils.repeat("x", SMALL_BUFFER_SIZE);

    public static int[] getBufferSizes() {
        // 1 and 2 are unusual and slow edge cases, but edge cases nonetheless.
        return new int[] { 1, 2, IOUtils.DEFAULT_BUFFER_SIZE / 10, IOUtils.DEFAULT_BUFFER_SIZE, IOUtils.DEFAULT_BUFFER_SIZE * 10 };
    }

    private static byte reverse(final byte b) {
        return (byte) (~b & 0xff);
    }

    private boolean isEmpty(final File empty) {
        return empty.length() == 0;
    }


    private FileChannel reset(final FileChannel fc) throws IOException {
        return fc.position(0);
    }

    @ParameterizedTest()
    @MethodSource("getBufferSizes")
    public void testContentEquals(final int bufferSize) throws IOException {
        final Path bigFile1 = Files.createTempFile(getClass().getSimpleName(), "-1.bin");
        final Path bigFile2 = Files.createTempFile(getClass().getSimpleName(), "-2.bin");
        final Path bigFile3 = Files.createTempFile(getClass().getSimpleName(), "-3.bin");
        try {
            // This length must match any restriction from the Surefire configuration.
            final int newLength = 5_000_000;
            final byte[] bytes1 = new byte[newLength];
            final byte[] bytes2 = new byte[newLength];
            // Make sure bytes1 and bytes2 are different despite the shuffle
            ArrayUtils.shuffle(bytes1);
            bytes1[0] = 1;
            ArrayUtils.shuffle(bytes2);
            bytes2[0] = 2;
            Files.write(bigFile1, bytes1);
            Files.write(bigFile2, bytes2);
            try (FileChannel fc1 = FileChannel.open(bigFile1); FileChannel fc2 = FileChannel.open(bigFile2)) {
                assertFalse(FileChannels.contentEquals(fc1, fc2, bufferSize));
                assertFalse(FileChannels.contentEquals(reset(fc2), reset(fc1), bufferSize));
                assertTrue(FileChannels.contentEquals(reset(fc1), reset(fc1), bufferSize));
            }
            // Make the LAST byte different.
            byte[] bytes3 = bytes1.clone();
            final int last = bytes3.length - 1;
            bytes3[last] = reverse(bytes3[last]);
            Files.write(bigFile3, bytes3);
            try (FileChannel fc1 = FileChannel.open(bigFile1); FileChannel fc3 = FileChannel.open(bigFile3)) {
                assertFalse(FileChannels.contentEquals(fc1, fc3, bufferSize));
                assertFalse(FileChannels.contentEquals(reset(fc3), reset(fc1), bufferSize));
                // Test just the last byte
                fc1.position(last);
                fc3.position(last);
                assertFalse(FileChannels.contentEquals(fc1, fc3, bufferSize));
            }
            // Make the LAST byte equal.
            bytes3 = bytes1.clone();
            Files.write(bigFile3, bytes3);
            try (FileChannel fc1 = FileChannel.open(bigFile1); FileChannel fc3 = FileChannel.open(bigFile3)) {
                // Test just the last byte
                fc1.position(last);
                fc3.position(last);
                assertTrue(FileChannels.contentEquals(fc1, fc3, bufferSize));
            }
            // Make a byte in the middle different
            bytes3 = bytes1.clone();
            final int middle = bytes3.length / 2;
            bytes3[middle] = reverse(bytes3[middle]);
            Files.write(bigFile3, bytes3);
            try (FileChannel fc1 = FileChannel.open(bigFile1); FileChannel fc3 = FileChannel.open(bigFile3)) {
                assertFalse(FileChannels.contentEquals(fc1, fc3, bufferSize));
                assertFalse(FileChannels.contentEquals(reset(fc3), reset(fc1), bufferSize));
            }
        } finally {
            // Delete ASAP
            Files.deleteIfExists(bigFile1);
            Files.deleteIfExists(bigFile2);
            Files.deleteIfExists(bigFile3);
        }
    }

    private void testContentEquals(final String content1, final String content2, final int bufferSize) throws IOException {
        assertTrue(FileChannels.contentEquals(null, null, bufferSize));
        // Prepare test files with same size but different content
        // (first 3 bytes are different, followed by a large amount of equal content)
        final File file1 = new File(tempDirFile, "test1.txt");
        final File file2 = new File(tempDirFile, "test2.txt");
        FileUtils.writeStringToFile(file1, content1, US_ASCII);
        FileUtils.writeStringToFile(file2, content2, US_ASCII);
        // File checksums are different
        assertNotEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2));
        try (FileInputStream in1 = new FileInputStream(file1);
                FileInputStream in2 = new FileInputStream(file2);
                FileChannel channel1 = in1.getChannel();
                FileChannel channel2 = in2.getChannel()) {
            assertFalse(FileChannels.contentEquals(channel1, channel2, bufferSize));
        }
        try (FileInputStream in1 = new FileInputStream(file1);
                FileInputStream in2 = new FileInputStream(file2);
                FileChannel channel1 = in1.getChannel();
                FileChannel channel2 = in2.getChannel()) {
            assertTrue(FileChannels.contentEquals(channel1, channel1, bufferSize));
            assertTrue(FileChannels.contentEquals(channel2, channel2, bufferSize));
        }
    }

    @ParameterizedTest()
    @MethodSource("getBufferSizes")
    public void testContentEqualsDifferentPostfix(final int bufferSize) throws IOException {
        testContentEquals(CONTENT + "ABC", CONTENT + "XYZ", bufferSize);
    }

    @ParameterizedTest()
    @MethodSource("getBufferSizes")
    public void testContentEqualsDifferentPrefix(final int bufferSize) throws IOException {
        testContentEquals("ABC" + CONTENT, "XYZ" + CONTENT, bufferSize);
    }

    @ParameterizedTest()
    @MethodSource("getBufferSizes")
    public void testContentEqualsEmpty(final int bufferSize) throws IOException {
        assertTrue(FileChannels.contentEquals(null, null, bufferSize));
        // setup fixtures
        final File empty = new File(tempDirFile, "empty.txt");
        final File notEmpty = new File(tempDirFile, "not-empty.txt");
        FileUtils.writeStringToFile(empty, StringUtils.EMPTY, US_ASCII);
        FileUtils.writeStringToFile(notEmpty, "X", US_ASCII);
        assertTrue(isEmpty(empty));
        assertFalse(isEmpty(notEmpty));
        // File checksums are different
        assertNotEquals(FileUtils.checksumCRC32(empty), FileUtils.checksumCRC32(notEmpty));
        try (FileInputStream inEmpty = new FileInputStream(empty);
                FileInputStream inNotEmpty = new FileInputStream(notEmpty);
                FileChannel channelEmpty = inEmpty.getChannel();
                FileChannel channelNotEmpty = inNotEmpty.getChannel()) {
            assertFalse(FileChannels.contentEquals(channelEmpty, channelNotEmpty, bufferSize));
            assertFalse(FileChannels.contentEquals(null, channelNotEmpty, bufferSize));
            assertFalse(FileChannels.contentEquals(channelNotEmpty, null, bufferSize));
            assertTrue(FileChannels.contentEquals(channelEmpty, channelEmpty, bufferSize));
            assertTrue(FileChannels.contentEquals(null, channelEmpty, bufferSize));
            assertTrue(FileChannels.contentEquals(channelEmpty, null, bufferSize));
            assertTrue(FileChannels.contentEquals(channelNotEmpty, channelNotEmpty, bufferSize));
        }
    }
}
