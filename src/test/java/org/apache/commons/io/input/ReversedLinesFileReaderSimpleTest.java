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
package org.apache.commons.io.input;

import static org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest.assertEqualsAndNoLineBreaks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.TestResources;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ReversedLinesFileReaderSimpleTest {

    /*
     * Tests IO-639.
     */
    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    @Disabled
    public void testEmptyFirstLine(final int blockSize) throws Exception {
        final File testFileEmptyFirstLine = TestResources.getFile("/empty-first-line.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFileEmptyFirstLine, 10, StandardCharsets.US_ASCII.name())) {
            assertEqualsAndNoLineBreaks("test2", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("test1", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("", reversedLinesFileReader.readLine());
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    public void testFileSizeIsExactMultipleOfBlockSize(final int blockSize) throws URISyntaxException, IOException {
        final File testFile20Bytes = TestResources.getFile("/test-file-20byteslength.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize,
                StandardCharsets.ISO_8859_1.name())) {
            assertEqualsAndNoLineBreaks("987654321", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("123456789", reversedLinesFileReader.readLine());
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    public void testLineCount(final int blockSize) throws URISyntaxException, IOException {
        final File testFile20Bytes = TestResources.getFile("/test-file-20byteslength.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize,
                StandardCharsets.ISO_8859_1.name())) {
            assertThrows(IllegalArgumentException.class, () -> reversedLinesFileReader.readLines(-1));
            assertTrue(reversedLinesFileReader.readLines(0).isEmpty());
            final List<String> lines = reversedLinesFileReader.readLines(2);
            assertEqualsAndNoLineBreaks("987654321", lines.get(0));
            assertEqualsAndNoLineBreaks("123456789", lines.get(1));
            assertTrue(reversedLinesFileReader.readLines(0).isEmpty());
            assertTrue(reversedLinesFileReader.readLines(10000).isEmpty());
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    public void testToString(final int blockSize) throws URISyntaxException, IOException {
        final File testFile20Bytes = TestResources.getFile("/test-file-20byteslength.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize,
                StandardCharsets.ISO_8859_1.name())) {
            assertThrows(IllegalArgumentException.class, () -> reversedLinesFileReader.toString(-1));
            assertTrue(reversedLinesFileReader.readLines(0).isEmpty());
            final String lines = reversedLinesFileReader.toString(2);
            assertEquals("123456789" + System.lineSeparator() + "987654321" + System.lineSeparator(), lines);
            assertTrue(reversedLinesFileReader.toString(0).isEmpty());
            assertTrue(reversedLinesFileReader.toString(10000).isEmpty());
        }
    }

    @Test
    public void testUnsupportedEncodingBig5() throws URISyntaxException {
        final File testFileEncodingBig5 = TestResources.getFile("/test-file-empty.bin");
        assertThrows(UnsupportedEncodingException.class,
            () -> new ReversedLinesFileReader(testFileEncodingBig5, IOUtils.DEFAULT_BUFFER_SIZE, "Big5").close());
    }

    @Test
    public void testUnsupportedEncodingUTF16() throws URISyntaxException {
        final File testFileEmpty = TestResources.getFile("/test-file-empty.bin");
        assertThrows(UnsupportedEncodingException.class,
            () -> new ReversedLinesFileReader(testFileEmpty, IOUtils.DEFAULT_BUFFER_SIZE, StandardCharsets.UTF_16.name()).close());
    }
}
