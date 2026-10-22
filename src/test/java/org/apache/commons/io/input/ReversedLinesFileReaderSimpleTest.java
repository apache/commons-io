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
package org.apache.commons.io.input;

import static org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest.assertEqualsAndNoLineBreaks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.TestResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ReversedLinesFileReaderSimpleTest {

    private static String longLine(final Charset charset, final int offset) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < 2_000; i++) {
            result.append(Integer.toHexString(i + offset));
            result.append(charset == StandardCharsets.US_ASCII ? (char) ('a' + i % 26) : (char) ('\u4e00' + i % 100));
        }
        return result.toString();
    }

    static Stream<Arguments> longLineConfigurations() {
        return ReversedLinesFileReaderParamBlockSizeTest.blockSizes().boxed().flatMap(blockSize ->
                Stream.of(StandardCharsets.US_ASCII, StandardCharsets.UTF_8, StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE).flatMap(charset ->
                        Stream.of("\r", "\n", "\r\n").flatMap(separator ->
                                Stream.of(false, true).map(trailingSeparator -> Arguments.of(blockSize, charset, separator, trailingSeparator)))));
    }

    private static String separatorPrefix(final int blockSize, final Charset charset, final String line, final String separator) {
        final int separatorLength = separator.getBytes(charset).length;
        final int characterLength = "a".getBytes(charset).length;
        int offset = line.getBytes(charset).length % blockSize;
        final StringBuilder prefix = new StringBuilder();
        for (int padding = 0; padding < blockSize; padding++) {
            if (offset + separatorLength > blockSize) {
                return prefix.toString();
            }
            prefix.append('a');
            offset = (offset + characterLength) % blockSize;
        }
        // Single-byte separators, and UTF-16 characters with even block sizes, cannot always be split.
        return "";
    }

    @TempDir
    private Path tempDirectory;

    /*
     * Tests IO-639.
     */
    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    void testEmptyFirstLine(final int blockSize) throws Exception {
        final File testFileEmptyFirstLine = TestResources.getFile("/empty-first-line.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFileEmptyFirstLine,
                blockSize, StandardCharsets.US_ASCII.name())) {
            assertEqualsAndNoLineBreaks("test2", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("test1", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("", reversedLinesFileReader.readLine());
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    void testFileSizeIsExactMultipleOfBlockSize(final int blockSize) throws URISyntaxException, IOException {
        final File testFile20Bytes = TestResources.getFile("/test-file-20byteslength.bin");
        try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize,
                StandardCharsets.ISO_8859_1.name())) {
            assertEqualsAndNoLineBreaks("987654321", reversedLinesFileReader.readLine());
            assertEqualsAndNoLineBreaks("123456789", reversedLinesFileReader.readLine());
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    void testLineCount(final int blockSize) throws URISyntaxException, IOException {
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

    /**
     * Reads a single long line intact, both with and without a preceding line.
     */
    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    void testLongLineSpanningManyBlocks(final int blockSize) throws IOException {
        final Path testFile = tempDirectory.resolve("long-line.txt");
        final StringBuilder longLine = new StringBuilder(10_000);
        for (int i = 0; i < 1_000; i++) {
            longLine.append("0123456789");
        }
        final String first = "first";
        final String last = longLine.toString();
        Files.write(testFile, (first + "\n" + last).getBytes(StandardCharsets.US_ASCII));
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setPath(testFile)
                .setBufferSize(blockSize)
                .setCharset(StandardCharsets.US_ASCII)
                .get()) {
            assertEquals(last, reader.readLine());
            assertEquals(first, reader.readLine());
            assertNull(reader.readLine());
        }
        // Newline-free file: the whole content is one line accumulated across every block.
        Files.write(testFile, last.getBytes(StandardCharsets.US_ASCII));
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setPath(testFile)
                .setBufferSize(blockSize)
                .setCharset(StandardCharsets.US_ASCII)
                .get()) {
            assertEquals(last, reader.readLine());
            assertNull(reader.readLine());
        }
    }

    /**
     * Reads distinct long lines with empty lines, split separators, different encodings, and an optional trailing separator.
     */
    @ParameterizedTest
    @MethodSource("longLineConfigurations")
    void testMultipleLongLines(final int blockSize, final Charset charset, final String separator, final boolean trailingSeparator) throws IOException {
        final Path testFile = tempDirectory.resolve("multiple-long-lines.txt");
        final String first = longLine(charset, 0);
        final String last = longLine(charset, 4_000);
        // Place a separator across a block boundary where possible, then also exercise the neighboring offsets.
        final String boundaryPrefix = separatorPrefix(blockSize, charset, first, separator);
        for (int padding = 0; padding < 4; padding++) {
            final String prefix = boundaryPrefix + "abcd".substring(0, padding);
            final String emptyLine = trailingSeparator ? separator : "";
            final String content = prefix + first + separator + emptyLine + last + emptyLine;
            Files.write(testFile, content.getBytes(charset));
            try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                    .setPath(testFile)
                    .setBufferSize(blockSize)
                    .setCharset(charset)
                    .get()) {
                assertEquals(last, reader.readLine());
                if (trailingSeparator) {
                    assertEquals("", reader.readLine());
                }
                assertEquals(prefix + first, reader.readLine());
                assertNull(reader.readLine());
            }
        }
    }

    /**
     * Rejects oversized virtual lines before allocating their output arrays.
     */
    @ParameterizedTest
    @ValueSource(longs = { (long) Integer.MAX_VALUE - 7, (long) Integer.MAX_VALUE + 1 })
    void testOversizedLine(final long oversizedLength) throws Exception {
        final Path testFile = Files.createFile(tempDirectory.resolve("oversized-line.txt"));
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder().setPath(testFile).get()) {
            // Exercise the guards with synthetic lengths instead of allocating a multi-gigabyte file or array.
            final Field currentFilePart = ReversedLinesFileReader.class.getDeclaredField("currentFilePart");
            currentFilePart.setAccessible(true);
            final Object part = currentFilePart.get(reader);
            final Method copyVirtual = part.getClass().getDeclaredMethod("copyVirtual", long.class, long.class);
            copyVirtual.setAccessible(true);
            final InvocationTargetException copyException = assertThrows(InvocationTargetException.class,
                    () -> copyVirtual.invoke(part, 0L, oversizedLength - 1));
            assertEquals(IllegalStateException.class, copyException.getCause().getClass());
            assertEquals("Line too long: " + oversizedLength + " bytes exceed the maximum array size", copyException.getCause().getMessage());

            final Field leftOverLength = part.getClass().getDeclaredField("leftOverLength");
            leftOverLength.setAccessible(true);
            leftOverLength.setLong(part, oversizedLength);
            final Method joinLeftOver = part.getClass().getDeclaredMethod("joinLeftOver");
            joinLeftOver.setAccessible(true);
            final InvocationTargetException joinException = assertThrows(InvocationTargetException.class, () -> joinLeftOver.invoke(part));
            assertEquals(IllegalStateException.class, joinException.getCause().getClass());
            assertEquals(copyException.getCause().getMessage(), joinException.getCause().getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    void testToString(final int blockSize) throws URISyntaxException, IOException {
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
    void testUnsupportedEncodingBig5() throws URISyntaxException {
        final File testFileEncodingBig5 = TestResources.getFile("/test-file-empty.bin");
        assertThrows(UnsupportedEncodingException.class,
            () -> new ReversedLinesFileReader(testFileEncodingBig5, IOUtils.DEFAULT_BUFFER_SIZE, "Big5").close());
    }

    @Test
    void testUnsupportedEncodingUTF16() throws URISyntaxException {
        final File testFileEmpty = TestResources.getFile("/test-file-empty.bin");
        assertThrows(UnsupportedEncodingException.class,
            () -> new ReversedLinesFileReader(testFileEmpty, IOUtils.DEFAULT_BUFFER_SIZE, StandardCharsets.UTF_16.name()).close());
    }
}
