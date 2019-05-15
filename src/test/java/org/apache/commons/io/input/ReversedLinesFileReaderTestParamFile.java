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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test checks symmetric behaviour with  BufferedReader
 */
@RunWith(Parameterized.class)
public class ReversedLinesFileReaderTestParamFile {
    @Parameters(name = "{0}, encoding={1}, blockSize={2}, useNonDefaultFileSystem={3}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {"test-file-20byteslength.bin", "ISO_8859_1", null, false},
                {"test-file-iso8859-1-shortlines-win-linebr.bin", "ISO_8859_1", null, false},
                {"test-file-iso8859-1.bin", "ISO_8859_1", null, false},
                {"test-file-shiftjis.bin", "Shift_JIS", null, false},
                {"test-file-utf16be.bin", "UTF-16BE", null, false},
                {"test-file-utf16le.bin", "UTF-16LE", null, false},
                {"test-file-utf8-cr-only.bin", "UTF-8", null, false},
                {"test-file-utf8-win-linebr.bin", "UTF-8", null, false},
                {"test-file-utf8-win-linebr.bin", "UTF-8", 1, false},
                {"test-file-utf8-win-linebr.bin", "UTF-8", 2, false},
                {"test-file-utf8-win-linebr.bin", "UTF-8", 3, false},
                {"test-file-utf8-win-linebr.bin", "UTF-8", 4, false},
                {"test-file-utf8.bin", "UTF-8", null, false},
                {"test-file-utf8.bin", "UTF-8", null, true},
                {"test-file-windows-31j.bin", "windows-31j", null, false},
                {"test-file-gbk.bin", "gbk", null, false},
                {"test-file-x-windows-949.bin", "x-windows-949", null, false},
                {"test-file-x-windows-950.bin", "x-windows-950", null, false},
        });
    }

    private Path file;
    private FileSystem fileSystem;
    private ReversedLinesFileReader reversedLinesFileReader;
    private BufferedReader bufferedReader;

    private final String fileName;
    private final Charset encoding;
    private final Integer blockSize;
    private final boolean useNonDefaultFileSystem;

    public ReversedLinesFileReaderTestParamFile(final String fileName, final String encoding, final Integer blockSize, final boolean useNonDefaultFileSystem) {
        this.fileName = fileName;
        this.encoding = Charset.forName(encoding);
        this.blockSize = blockSize;
        this.useNonDefaultFileSystem = useNonDefaultFileSystem;
    }

    @Before
    public void prepareFile() throws URISyntaxException, IOException {
        file = Paths.get(getClass().getResource("/" + fileName).toURI());
        if (useNonDefaultFileSystem) {
            fileSystem = Jimfs.newFileSystem(Configuration.unix());
            file = Files.copy(file, fileSystem.getPath("/" + fileName));
        }
    }

    @Test
    public void testDataIntegrityWithBufferedReader() throws IOException {
        reversedLinesFileReader = blockSize == null
                ? new ReversedLinesFileReader(file, encoding)
                : new ReversedLinesFileReader(file, blockSize, encoding);

        final Stack<String> lineStack = new Stack<>();

        bufferedReader = Files.newBufferedReader(file, encoding);
        String line;

        // read all lines in normal order
        while ((line = bufferedReader.readLine()) != null) {
            lineStack.push(line);
        }

        // read in reverse order and compare with lines from stack
        while ((line = reversedLinesFileReader.readLine()) != null) {
            final String lineFromBufferedReader = lineStack.pop();
            assertEquals(lineFromBufferedReader, line);
        }
    }

    @After
    public void releaseResources() {
        try {
            bufferedReader.close();
        } catch (final Exception e) {
            // ignore
        }
        try {
            reversedLinesFileReader.close();
        } catch (final Exception e) {
            // ignore
        }
        try {
            fileSystem.close();
        } catch (final Exception e) {
            // ignore
        }
    }
}
