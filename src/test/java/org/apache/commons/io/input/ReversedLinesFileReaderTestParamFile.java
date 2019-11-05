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

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Stack;
import java.util.stream.Stream;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test checks symmetric behavior with  BufferedReader
 */
public class ReversedLinesFileReaderTestParamFile {
    public static Stream<Arguments> testDataIntegrityWithBufferedReader() {
        return Stream.of(
                Arguments.of("test-file-20byteslength.bin", "ISO_8859_1", null, false),
                Arguments.of("test-file-iso8859-1-shortlines-win-linebr.bin", "ISO_8859_1", null, false),
                Arguments.of("test-file-iso8859-1.bin", "ISO_8859_1", null, false),
                Arguments.of("test-file-shiftjis.bin", "Shift_JIS", null, false),
                Arguments.of("test-file-utf16be.bin", "UTF-16BE", null, false),
                Arguments.of("test-file-utf16le.bin", "UTF-16LE", null, false),
                Arguments.of("test-file-utf8-cr-only.bin", "UTF-8", null, false),
                Arguments.of("test-file-utf8-win-linebr.bin", "UTF-8", null, false),
                Arguments.of("test-file-utf8-win-linebr.bin", "UTF-8", 1, false),
                Arguments.of("test-file-utf8-win-linebr.bin", "UTF-8", 2, false),
                Arguments.of("test-file-utf8-win-linebr.bin", "UTF-8", 3, false),
                Arguments.of("test-file-utf8-win-linebr.bin", "UTF-8", 4, false),
                Arguments.of("test-file-utf8.bin", "UTF-8", null, false),
                Arguments.of("test-file-utf8.bin", "UTF-8", null, true),
                Arguments.of("test-file-windows-31j.bin", "windows-31j", null, false),
                Arguments.of("test-file-gbk.bin", "gbk", null, false),
                Arguments.of("test-file-x-windows-949.bin", "x-windows-949", null, false),
                Arguments.of("test-file-x-windows-950.bin", "x-windows-950", null, false)
        );
    }

    @ParameterizedTest(name = "{0}, encoding={1}, blockSize={2}, useNonDefaultFileSystem={3}")
    @MethodSource
    public void testDataIntegrityWithBufferedReader
            (final String fileName, final String encodingName, final Integer blockSize, final boolean useNonDefaultFileSystem)
            throws IOException, URISyntaxException {

        Path file = Paths.get(getClass().getResource("/" + fileName).toURI());
        FileSystem fileSystem = null;
        if (useNonDefaultFileSystem) {
            fileSystem = Jimfs.newFileSystem(Configuration.unix());
            file = Files.copy(file, fileSystem.getPath("/" + fileName));
        }

        Charset encoding = Charset.forName(encodingName);
        try (ReversedLinesFileReader reversedLinesFileReader = blockSize == null
                ? new ReversedLinesFileReader(file, encoding)
                : new ReversedLinesFileReader(file, blockSize, encoding)) {

            final Stack<String> lineStack = new Stack<>();
            String line;

            try (BufferedReader bufferedReader = Files.newBufferedReader(file, encoding)) {
                // read all lines in normal order
                while ((line = bufferedReader.readLine()) != null) {
                    lineStack.push(line);
                }
            }

            // read in reverse order and compare with lines from stack
            while ((line = reversedLinesFileReader.readLine()) != null) {
                final String lineFromBufferedReader = lineStack.pop();
                assertEquals(lineFromBufferedReader, line);
            }

            if (fileSystem != null) {
                fileSystem.close();
            }
        }
    }
}
