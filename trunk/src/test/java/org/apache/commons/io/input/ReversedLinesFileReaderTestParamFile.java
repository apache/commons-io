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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test checks symmetric behaviour with  BufferedReader
 */
@RunWith(Parameterized.class)
public class ReversedLinesFileReaderTestParamFile {

    @Parameters(name = "{0}, charset={1}")
    public static Collection<Object[]> blockSizes() {
        return Arrays.asList(new Object[][]{
                {"test-file-20byteslength.bin", "ISO_8859_1", null},
                {"test-file-iso8859-1-shortlines-win-linebr.bin", "ISO_8859_1", null},
                {"test-file-iso8859-1.bin", "ISO_8859_1", null},
                {"test-file-shiftjis.bin", "Shift_JIS", null},
                {"test-file-utf16be.bin", "UTF-16BE", null},
                {"test-file-utf16le.bin", "UTF-16LE", null},
                {"test-file-utf8-cr-only.bin", "UTF-8", null},
                {"test-file-utf8-win-linebr.bin", "UTF-8", null},
                {"test-file-utf8-win-linebr.bin", "UTF-8", 1},
                {"test-file-utf8-win-linebr.bin", "UTF-8", 2},
                {"test-file-utf8-win-linebr.bin", "UTF-8", 3},
                {"test-file-utf8-win-linebr.bin", "UTF-8", 4},
                {"test-file-utf8.bin", "UTF-8", null},
                {"test-file-windows-31j.bin", "windows-31j", null},
                {"test-file-gbk.bin", "gbk", null},
                {"test-file-x-windows-949.bin", "x-windows-949", null},
                {"test-file-x-windows-950.bin", "x-windows-950", null},
        });
    }

    private ReversedLinesFileReader reversedLinesFileReader;
    private BufferedReader bufferedReader;

    private final String fileName;
    private final String encoding;
    private final int buffSize;

    public ReversedLinesFileReaderTestParamFile(final String fileName, final String encoding, final Integer buffsize) {
        this.fileName = fileName;
        this.encoding = encoding;
        this.buffSize = buffsize == null ? 4096 : buffsize;
    }

    @Test
    public void testDataIntegrityWithBufferedReader() throws URISyntaxException, IOException {
        final File testFileIso = new File(this.getClass().getResource("/" + fileName).toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileIso, buffSize, encoding);

        final Stack<String> lineStack = new Stack<String>();

        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(testFileIso), encoding));
        String line = null;

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
    public void closeReader() {
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
    }


}
