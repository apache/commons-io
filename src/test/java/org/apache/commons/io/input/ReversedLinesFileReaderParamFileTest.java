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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.TestResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * Test checks symmetric behavior with BufferedReader.
 */
public class ReversedLinesFileReaderParamFileTest {

    private static final String UTF_16BE = StandardCharsets.ISO_8859_1.name();
    private static final String UTF_16LE = StandardCharsets.UTF_16LE.name();
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    private static final String ISO_8859_1 = StandardCharsets.ISO_8859_1.name();

    public static Stream<Arguments> testDataIntegrityWithBufferedReader() throws IOException, URISyntaxException {
        // Make a file using the default encoding.
        final Path sourcePath = TestResources.getPath("test-file-utf8-win-linebr.bin");
        final Path targetPath = Files.createTempFile("ReversedLinesFileReaderTestParamFile", ".bin");
        try (Reader input = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8);
            Writer output = Files.newBufferedWriter(targetPath, Charset.defaultCharset())) {
            IOUtils.copyLarge(input, output);
        }
        // All tests
        // @formatter:off
        return Stream.of(
                Arguments.of(targetPath.toAbsolutePath().toString(), null, null, false, false),
                Arguments.of("test-file-20byteslength.bin", ISO_8859_1, null, false, true),
                Arguments.of("test-file-iso8859-1-shortlines-win-linebr.bin", ISO_8859_1, null, false, true),
                Arguments.of("test-file-iso8859-1.bin", ISO_8859_1, null, false, true),
                Arguments.of("test-file-shiftjis.bin", "Shift_JIS", null, false, true),
                Arguments.of("test-file-utf16be.bin", UTF_16BE, null, false, true),
                Arguments.of("test-file-utf16le.bin", UTF_16LE, null, false, true),
                Arguments.of("test-file-utf8-cr-only.bin", UTF_8, null, false, true),
                Arguments.of("test-file-utf8-win-linebr.bin", UTF_8, null, false, true,
                Arguments.of("test-file-utf8-win-linebr.bin", UTF_8, 1, false, true),
                Arguments.of("test-file-utf8-win-linebr.bin", UTF_8, 2, false, true),
                Arguments.of("test-file-utf8-win-linebr.bin", UTF_8, 3, false, true),
                Arguments.of("test-file-utf8-win-linebr.bin", UTF_8, 4, false, true),
                Arguments.of("test-file-utf8.bin", UTF_8, null, false, true),
                Arguments.of("test-file-utf8.bin", UTF_8, null, true, true),
                Arguments.of("test-file-windows-31j.bin", "windows-31j", null, false, true),
                Arguments.of("test-file-gbk.bin", "gbk", null, false, true),
                Arguments.of("test-file-x-windows-949.bin", "x-windows-949", null, false, true),
                Arguments.of("test-file-x-windows-950.bin", "x-windows-950", null, false, true)));
        // @formatter:on
    }

    private void testDataIntegrityWithBufferedReader(final Path filePath, final FileSystem fileSystem, final Charset charset,
            final ReversedLinesFileReader reversedLinesFileReader) throws IOException {
        final List<String> allLines = Files.readAllLines(filePath, Charsets.toCharset(charset));
        final Stack<String> lineStack = new Stack<>();
        lineStack.addAll(allLines);
        // read in reverse order and compare with lines from stack
        reversedLinesFileReader.forEach(line -> assertEquals(lineStack.pop(), line));
        assertEquals(0, lineStack.size(), "Stack should be empty");
        IOUtils.close(fileSystem);
    }

    @ParameterizedTest(name = "{0}, encoding={1}, blockSize={2}, useNonDefaultFileSystem={3}, isResource={4}")
    @MethodSource
    public void testDataIntegrityWithBufferedReader(final String fileName, final String charsetName, final Integer blockSize,
            final boolean useNonDefaultFileSystem, final boolean isResource) throws IOException, URISyntaxException {

        Path filePath = isResource ? TestResources.getPath(fileName) : Paths.get(fileName);
        FileSystem fileSystem = null;
        if (useNonDefaultFileSystem) {
            fileSystem = Jimfs.newFileSystem(Configuration.unix());
            filePath = Files.copy(filePath, fileSystem.getPath("/" + fileName));
        }

        // We want to test null Charset in the ReversedLinesFileReader constructor.
        final Charset charset = charsetName != null ? Charset.forName(charsetName) : null;
        try (ReversedLinesFileReader reversedLinesFileReader = blockSize == null ? new ReversedLinesFileReader(filePath, charset)
                : new ReversedLinesFileReader(filePath, blockSize, charset)) {
            testDataIntegrityWithBufferedReader(filePath, fileSystem, charset, reversedLinesFileReader);
        }
        // @formatter:off
        try (ReversedLinesFileReader reversedLinesFileReader = ReversedLinesFileReader.builder()
                .setPath(filePath)
                .setBufferSize(blockSize)
                .setCharset(charset)
                .get()) {
            // @formatter:on
            testDataIntegrityWithBufferedReader(filePath, fileSystem, charset, reversedLinesFileReader);
        }
    }
}
