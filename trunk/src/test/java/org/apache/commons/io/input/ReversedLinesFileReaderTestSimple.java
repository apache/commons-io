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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Test;


public class ReversedLinesFileReaderTestSimple {

    private ReversedLinesFileReader reversedLinesFileReader;

    @After
    public void closeReader() {
        try {
            reversedLinesFileReader.close();
        } catch(final Exception e) {
            // ignore
        }
    }

    @Test
    public void testFileSizeIsExactMultipleOfBlockSize() throws URISyntaxException, IOException {
        final int blockSize = 10;
        final File testFile20Bytes = new File(this.getClass().getResource("/test-file-20byteslength.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize, "ISO-8859-1");
        final String testLine = "123456789";
        assertEqualsAndNoLineBreaks(testLine, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(testLine, reversedLinesFileReader.readLine());
    }

    @Test(expected=UnsupportedEncodingException.class)
    public void testUnsupportedEncodingUTF16() throws URISyntaxException, IOException {
        final File testFileEmpty = new File(this.getClass().getResource("/test-file-empty.bin").toURI());
        new ReversedLinesFileReader(testFileEmpty, 4096, "UTF-16").close();
    }

    @Test(expected=UnsupportedEncodingException.class)
    public void testUnsupportedEncodingBig5() throws URISyntaxException, IOException {
        final File testFileEncodingBig5 = new File(this.getClass().getResource("/test-file-empty.bin").toURI());
        new ReversedLinesFileReader(testFileEncodingBig5, 4096, "Big5").close();
    }



}
