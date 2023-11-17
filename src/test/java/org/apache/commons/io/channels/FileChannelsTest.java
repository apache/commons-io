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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FileChannels}.
 */
public class FileChannelsTest extends AbstractTempDirTest {

    private static final int BUFFER_SIZE = 1024;
    private static final String CONTENT = StringUtils.repeat("x", BUFFER_SIZE);

    private boolean isEmpty(final File empty) {
        return empty.length() == 0;
    }

    private void testContentEquals(final String content1, final String content2) throws IOException {
        assertTrue(FileChannels.contentEquals(null, null, BUFFER_SIZE));

        // Prepare test files with same size but different content
        // (first 3 bytes are different, followed by a large amount of equal content)
        final File file1 = new File(tempDirFile, "test1.txt");
        final File file2 = new File(tempDirFile, "test2.txt");
        FileUtils.writeStringToFile(file1, content1, US_ASCII);
        FileUtils.writeStringToFile(file2, content2, US_ASCII);

        // File checksums are different
        assertNotEquals(FileUtils.checksumCRC32(file1), FileUtils.checksumCRC32(file2));

        try (FileInputStream stream1 = new FileInputStream(file1);
                FileInputStream stream2 = new FileInputStream(file2);
                FileChannel channel1 = stream1.getChannel();
                FileChannel channel2 = stream2.getChannel()) {
            assertFalse(FileChannels.contentEquals(channel1, channel2, BUFFER_SIZE));
        }
        try (FileInputStream stream1 = new FileInputStream(file1);
                FileInputStream stream2 = new FileInputStream(file2);
                FileChannel channel1 = stream1.getChannel();
                FileChannel channel2 = stream2.getChannel()) {
            assertTrue(FileChannels.contentEquals(channel1, channel1, BUFFER_SIZE));
            assertTrue(FileChannels.contentEquals(channel2, channel2, BUFFER_SIZE));
        }
    }

    @Test
    public void testContentEqualsDifferentPostfix() throws IOException {
        testContentEquals(CONTENT + "ABC", CONTENT + "XYZ");
    }

    @Test
    public void testContentEqualsDifferentPrefix() throws IOException {
        testContentEquals("ABC" + CONTENT, "XYZ" + CONTENT);
    }

    @Test
    public void testContentEqualsEmpty() throws IOException {
        assertTrue(FileChannels.contentEquals(null, null, BUFFER_SIZE));

        final File empty = new File(tempDirFile, "empty.txt");
        final File notEmpty = new File(tempDirFile, "not-empty.txt");
        FileUtils.writeStringToFile(empty, StringUtils.EMPTY, US_ASCII);
        FileUtils.writeStringToFile(notEmpty, "X", US_ASCII);
        assertTrue(isEmpty(empty));
        assertFalse(isEmpty(notEmpty));

        // File checksums are different
        assertNotEquals(FileUtils.checksumCRC32(empty), FileUtils.checksumCRC32(notEmpty));

        try (FileInputStream streamEmpty = new FileInputStream(empty);
                FileInputStream streamNotEmpty = new FileInputStream(notEmpty);
                FileChannel channelEmpty = streamEmpty.getChannel();
                FileChannel channelNotEmpty = streamNotEmpty.getChannel()) {
            assertFalse(FileChannels.contentEquals(channelEmpty, channelNotEmpty, BUFFER_SIZE));
            assertFalse(FileChannels.contentEquals(null, channelNotEmpty, BUFFER_SIZE));
            assertFalse(FileChannels.contentEquals(channelNotEmpty, null, BUFFER_SIZE));
            assertTrue(FileChannels.contentEquals(channelEmpty, channelEmpty, BUFFER_SIZE));
            assertTrue(FileChannels.contentEquals(null, channelEmpty, BUFFER_SIZE));
            assertTrue(FileChannels.contentEquals(channelEmpty, null, BUFFER_SIZE));
            assertTrue(FileChannels.contentEquals(channelNotEmpty, channelNotEmpty, BUFFER_SIZE));
        }
    }

}
