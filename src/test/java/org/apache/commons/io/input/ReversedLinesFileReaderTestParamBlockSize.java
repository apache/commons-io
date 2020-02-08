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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ReversedLinesFileReaderTestParamBlockSize {

    private static final String UTF_8 = "UTF-8";
    private static final String ISO_8859_1 = "ISO-8859-1";

    private static final int[] BLOCK_SIZES = {1, 3, 8, 256, 4096};

    // small and uneven block sizes are not used in reality but are good to show that the algorithm is solid
    public static IntStream blockSizes() {
        return IntStream.of(1, 3, 8, 256, 4096);
    }

    private ReversedLinesFileReader reversedLinesFileReader;

    // Strings are escaped in constants to avoid java source encoding issues (source file enc is UTF-8):

    // "A Test Line. Special chars: Ã„Ã¤ÃœÃ¼Ã–Ã¶ÃŸ ÃƒÃ¡Ã©Ã­Ã¯Ã§Ã±Ã‚ Â©ÂµÂ¥Â£Â±Â²Â®"
    private static final String TEST_LINE = "A Test Line. Special chars: \u00C4\u00E4\u00DC\u00FC\u00D6\u00F6\u00DF \u00C3\u00E1\u00E9\u00ED\u00EF\u00E7\u00F1\u00C2 \u00A9\u00B5\u00A5\u00A3\u00B1\u00B2\u00AE";
    // Hiragana letters: ã��ã�‚ã�ƒã�„ã�…
    private static final String TEST_LINE_SHIFT_JIS1 = "Hiragana letters: \u3041\u3042\u3043\u3044\u3045";
    // Kanji letters: æ˜Žè¼¸å­�äº¬
    private static final String TEST_LINE_SHIFT_JIS2 = "Kanji letters: \u660E\u8F38\u5B50\u4EAC";
    // windows-31j characters
    private static final String TEST_LINE_WINDOWS_31J_1 = "\u3041\u3042\u3043\u3044\u3045";
    private static final String TEST_LINE_WINDOWS_31J_2 = "\u660E\u8F38\u5B50\u4EAC";
    // gbk characters (Simplified Chinese)
    private static final String TEST_LINE_GBK_1 = "\u660E\u8F38\u5B50\u4EAC";
    private static final String TEST_LINE_GBK_2 = "\u7B80\u4F53\u4E2D\u6587";
    // x-windows-949 characters (Korean)
    private static final String TEST_LINE_X_WINDOWS_949_1 = "\uD55C\uAD6D\uC5B4";
    private static final String TEST_LINE_X_WINDOWS_949_2 = "\uB300\uD55C\uBBFC\uAD6D";
    // x-windows-950 characters (Traditional Chinese)
    private static final String TEST_LINE_X_WINDOWS_950_1 = "\u660E\u8F38\u5B50\u4EAC";
    private static final String TEST_LINE_X_WINDOWS_950_2 = "\u7E41\u9AD4\u4E2D\u6587";


    @AfterEach
    public void closeReader() {
        try {
            reversedLinesFileReader.close();
        } catch (final Exception e) {
            // ignore
        }
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testIsoFileDefaults(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileIso = new File(this.getClass().getResource("/test-file-iso8859-1.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileIso, testParamBlockSize, ISO_8859_1);
        assertFileWithShrinkingTestLines(reversedLinesFileReader);
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testUTF8FileWindowsBreaks(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileIso = new File(this.getClass().getResource("/test-file-utf8-win-linebr.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileIso, testParamBlockSize, UTF_8);
        assertFileWithShrinkingTestLines(reversedLinesFileReader);
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testUTF8FileCRBreaks(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileIso = new File(this.getClass().getResource("/test-file-utf8-cr-only.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileIso, testParamBlockSize, UTF_8);
        assertFileWithShrinkingTestLines(reversedLinesFileReader);
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testUTF8File(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileIso = new File(this.getClass().getResource("/test-file-utf8.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileIso, testParamBlockSize, UTF_8);
        assertFileWithShrinkingTestLines(reversedLinesFileReader);
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testEmptyFile(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileEmpty = new File(this.getClass().getResource("/test-file-empty.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileEmpty, testParamBlockSize, UTF_8);
        assertNull(reversedLinesFileReader.readLine());
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testUTF16BEFile(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileUTF16BE = new File(this.getClass().getResource("/test-file-utf16be.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileUTF16BE, testParamBlockSize, "UTF-16BE");
        assertFileWithShrinkingTestLines(reversedLinesFileReader);
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testUTF16LEFile(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileUTF16LE = new File(this.getClass().getResource("/test-file-utf16le.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileUTF16LE, testParamBlockSize, "UTF-16LE");
        assertFileWithShrinkingTestLines(reversedLinesFileReader);
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testShiftJISFile(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileShiftJIS = new File(this.getClass().getResource("/test-file-shiftjis.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileShiftJIS, testParamBlockSize, "Shift_JIS");
        assertEqualsAndNoLineBreaks(TEST_LINE_SHIFT_JIS2, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(TEST_LINE_SHIFT_JIS1, reversedLinesFileReader.readLine());
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testWindows31jFile(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileWindows31J = new File(this.getClass().getResource("/test-file-windows-31j.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileWindows31J, testParamBlockSize, "windows-31j");
        assertEqualsAndNoLineBreaks(TEST_LINE_WINDOWS_31J_2, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(TEST_LINE_WINDOWS_31J_1, reversedLinesFileReader.readLine());
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testGBK(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileGBK = new File(this.getClass().getResource("/test-file-gbk.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileGBK, testParamBlockSize, "GBK");
        assertEqualsAndNoLineBreaks(TEST_LINE_GBK_2, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(TEST_LINE_GBK_1, reversedLinesFileReader.readLine());
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testxWindows949File(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFilexWindows949 = new File(this.getClass().getResource("/test-file-x-windows-949.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFilexWindows949, testParamBlockSize, "x-windows-949");
        assertEqualsAndNoLineBreaks(TEST_LINE_X_WINDOWS_949_2, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(TEST_LINE_X_WINDOWS_949_1, reversedLinesFileReader.readLine());
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testxWindows950File(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFilexWindows950 = new File(this.getClass().getResource("/test-file-x-windows-950.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFilexWindows950, testParamBlockSize, "x-windows-950");
        assertEqualsAndNoLineBreaks(TEST_LINE_X_WINDOWS_950_2, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(TEST_LINE_X_WINDOWS_950_1, reversedLinesFileReader.readLine());
    }

    @Test
    public void testFileSizeIsExactMultipleOfBlockSize() throws URISyntaxException, IOException {
        final int blockSize = 10;
        final File testFile20Bytes = new File(this.getClass().getResource("/test-file-20byteslength.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFile20Bytes, blockSize, ISO_8859_1);
        final String testLine = "123456789";
        assertEqualsAndNoLineBreaks(testLine, reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks(testLine, reversedLinesFileReader.readLine());
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testUTF8FileWindowsBreaksSmallBlockSize2VerifyBlockSpanningNewLines(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileUtf8 = new File(this.getClass().getResource("/test-file-utf8-win-linebr.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileUtf8, testParamBlockSize, UTF_8);
        assertFileWithShrinkingTestLines(reversedLinesFileReader);
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testIsoFileManyWindowsBreaksSmallBlockSize2VerifyBlockSpanningNewLines(int testParamBlockSize) throws URISyntaxException, IOException {
        final File testFileIso = new File(this.getClass().getResource("/test-file-iso8859-1-shortlines-win-linebr.bin").toURI());
        reversedLinesFileReader = new ReversedLinesFileReader(testFileIso, testParamBlockSize, ISO_8859_1);

        for (int i = 3; i > 0; i--) {
            for (int j = 1; j <= 3; j++) {
                assertEqualsAndNoLineBreaks("", reversedLinesFileReader.readLine());
            }
            assertEqualsAndNoLineBreaks("" + i, reversedLinesFileReader.readLine());
        }
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testUnsupportedEncodingUTF16(int testParamBlockSize) throws URISyntaxException {
        final File testFileEmpty = new File(this.getClass().getResource("/test-file-empty.bin").toURI());
        assertThrows(UnsupportedEncodingException.class,
                () -> new ReversedLinesFileReader(testFileEmpty, testParamBlockSize, "UTF-16").close());
    }

    @ParameterizedTest(name = "BlockSize={0}")
    @MethodSource("blockSizes")
    public void testUnsupportedEncodingBig5(int testParamBlockSize) throws URISyntaxException {
        final File testFileEncodingBig5 = new File(this.getClass().getResource("/test-file-empty.bin").toURI());
        assertThrows(UnsupportedEncodingException.class,
                () -> new ReversedLinesFileReader(testFileEncodingBig5, testParamBlockSize, "Big5").close());
    }

    private void assertFileWithShrinkingTestLines(final ReversedLinesFileReader reversedLinesFileReader) throws IOException {
        String line = null;
        int lineCount = 0;
        while ((line = reversedLinesFileReader.readLine()) != null) {
            lineCount++;
            assertEqualsAndNoLineBreaks("Line " + lineCount + " is not matching", TEST_LINE.substring(0, lineCount), line);
        }
    }

    static void assertEqualsAndNoLineBreaks(final String msg, final String expected, final String actual) {
        if (actual != null) {
            assertFalse(actual.contains("\n"), "Line contains \\n: line=" + actual);
            assertFalse(actual.contains("\r"), "Line contains \\r: line=" + actual);
        }
        assertEquals(expected, actual, msg);
    }

    static void assertEqualsAndNoLineBreaks(final String expected, final String actual) {
        assertEqualsAndNoLineBreaks(null, expected, actual);
    }
}
