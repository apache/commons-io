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

import static org.apache.commons.io.input.ReversedLinesFileReaderTestParamBlockSize.assertEqualsAndNoLineBreaks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.TestResources;
import org.junit.jupiter.api.Test;

public class ReversedLinesFileReaderTestSimple {

    @Test
    public void testFileSizeIsExactMultipleOfBlockSize() throws URISyntaxException, IOException {
        final int blockSize = 10;
        final File testFile20Bytes = TestResources.getFile("/test-file-20byteslength.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize,
            "ISO-8859-1")) {
            assertEqualsAndNoLineBreaks("987654321", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("123456789", reversedLinesFileReader.readLine());
        }
    }

    @Test
    public void testLineCount() throws URISyntaxException, IOException {
        final int blockSize = 10;
        final File testFile20Bytes = TestResources.getFile("/test-file-20byteslength.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize,
            "ISO-8859-1")) {
            assertThrows(IllegalArgumentException.class, () -> reversedLinesFileReader.readLines(-1));
            assertTrue(reversedLinesFileReader.readLines(0).isEmpty());
            final List<String> lines = reversedLinesFileReader.readLines(2);
            assertEqualsAndNoLineBreaks("987654321", lines.get(0));
            assertEqualsAndNoLineBreaks("123456789", lines.get(1));
            assertTrue(reversedLinesFileReader.readLines(0).isEmpty());
            assertTrue(reversedLinesFileReader.readLines(10000).isEmpty());
        }
    }

    @Test
    public void testToString() throws URISyntaxException, IOException {
        final int blockSize = 10;
        final File testFile20Bytes = TestResources.getFile("/test-file-20byteslength.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize,
            "ISO-8859-1")) {
            assertThrows(IllegalArgumentException.class, () -> reversedLinesFileReader.toString(-1));
            assertTrue(reversedLinesFileReader.readLines(0).isEmpty());
            final String lines = reversedLinesFileReader.toString(2);
            assertEquals("123456789" + System.lineSeparator() + "987654321" + System.lineSeparator(), lines);
            assertTrue(reversedLinesFileReader.toString(0).isEmpty());
            assertTrue(reversedLinesFileReader.toString(10000).isEmpty());
        }
    }

    @Test
    public void testUnsupportedEncodingUTF16() throws URISyntaxException {
        final File testFileEmpty = TestResources.getFile("/test-file-empty.bin");
        assertThrows(UnsupportedEncodingException.class,
            () -> new ReversedLinesFileReader(testFileEmpty, IOUtils.DEFAULT_BUFFER_SIZE, "UTF-16").close());
    }

    @Test
    public void testUnsupportedEncodingBig5() throws URISyntaxException {
        final File testFileEncodingBig5 = TestResources.getFile("/test-file-empty.bin");
        assertThrows(UnsupportedEncodingException.class,
            () -> new ReversedLinesFileReader(testFileEncodingBig5, IOUtils.DEFAULT_BUFFER_SIZE, "Big5").close());
    }

}
