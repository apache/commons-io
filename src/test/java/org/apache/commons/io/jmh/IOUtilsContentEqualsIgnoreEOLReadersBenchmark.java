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

package org.apache.commons.io.jmh;

import static org.apache.commons.io.IOUtils.EOF;
import static org.apache.commons.io.IOUtils.toBufferedReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.buffer.LineEndUnifiedBufferedReader;
import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Test different implementations of {@link IOUtils#contentEqualsIgnoreEOL(Reader, Reader)}.
 *
 * <pre>
 * IOUtilsContentEqualsIgnoreEOLReadersBenchmark.testFileCurrent                                      avgt    5      510173.062 ▒      4124.634  ns/op
 * IOUtilsContentEqualsIgnoreEOLReadersBenchmark.testFilePr118                                        avgt    5      513733.905 ▒      6157.818  ns/op
 * IOUtilsContentEqualsIgnoreEOLReadersBenchmark.testFileRelease_2_8_0                                avgt    5      498785.100 ▒      9845.248  ns/op
 * IOUtilsContentEqualsIgnoreEOLReadersBenchmark.testStringCurrent                                    avgt    5  1708154223.333 ▒ 104024141.073  ns/op
 * IOUtilsContentEqualsIgnoreEOLReadersBenchmark.testStringPr118                                      avgt    5  1714266053.333 ▒  44126767.233  ns/op
 * IOUtilsContentEqualsIgnoreEOLReadersBenchmark.testStringRelease_2_8_0                              avgt    5  4237073486.667 ▒ 217596541.348  ns/op
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server"})
public class IOUtilsContentEqualsIgnoreEOLReadersBenchmark {

    private static final int STRING_LEN = 1 << 24;
    private static final String TEST_PATH_A = "/org/apache/commons/io/testfileBOM.xml";
    private static final String TEST_PATH_16K_A = "/org/apache/commons/io/abitmorethan16k.txt";
    private static final String TEST_PATH_16K_A_COPY = "/org/apache/commons/io/abitmorethan16kcopy.txt";
    private static final String TEST_PATH_B = "/org/apache/commons/io/testfileNoBOM.xml";
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    static String[] STRINGS = new String[5];

    static {
        STRINGS[0] = StringUtils.repeat("ab", STRING_LEN);
        STRINGS[1] = STRINGS[0] + 'c';
        STRINGS[2] = STRINGS[0] + 'd';
        STRINGS[3] = StringUtils.repeat("ab\rab\n", STRING_LEN);
        STRINGS[4] = StringUtils.repeat("ab\r\nab\r", STRING_LEN);
    }

    static String SPECIAL_CASE_STRING_0 = StringUtils.repeat(StringUtils.repeat("ab", STRING_LEN) + '\n', 2);
    static String SPECIAL_CASE_STRING_1 = StringUtils.repeat(StringUtils.repeat("cd", STRING_LEN) + '\n', 2);

    @SuppressWarnings("resource")
    public static boolean contentEqualsIgnoreEOL_release_2_8_0(final Reader reader1, final Reader reader2) throws IOException {
        if (reader1 == reader2) {
            return true;
        }
        if (reader1 == null ^ reader2 == null) {
            return false;
        }
        final BufferedReader br1 = toBufferedReader(reader1);
        final BufferedReader br2 = toBufferedReader(reader2);

        String line1 = br1.readLine();
        String line2 = br2.readLine();
        while (line1 != null && line1.equals(line2)) {
            line1 = br1.readLine();
            line2 = br2.readLine();
        }
        return Objects.equals(line1, line2);
    }

    public static boolean contentEqualsIgnoreEOLPr118(final Reader reader1, final Reader reader2)
            throws IOException {
        if (reader1 == reader2) {
            return true;
        }
        if (reader1 == null ^ reader2 == null) {
            return false;
        }

        final LineEndUnifiedBufferedReader bufferedInput1;
        if (reader1 instanceof LineEndUnifiedBufferedReader) {
            bufferedInput1 = (LineEndUnifiedBufferedReader) reader1;
        } else {
            bufferedInput1 = new LineEndUnifiedBufferedReader(reader1);
        }

        final LineEndUnifiedBufferedReader bufferedInput2;
        if (reader2 instanceof LineEndUnifiedBufferedReader) {
            bufferedInput2 = (LineEndUnifiedBufferedReader) reader2;
        } else {
            bufferedInput2 = new LineEndUnifiedBufferedReader(reader2);
        }

        /*
         * We use this variable to mark if last char be '\n'.
         * Because "a" and "a\n" is thought contentEqualsIgnoreEOL,
         * but "\n" and "\n\n" is thought not contentEqualsIgnoreEOL.
         */
        boolean justNewLine = true;

        int currentChar1;
        int currentChar2;

        while (true) {
            currentChar1 = bufferedInput1.peek();
            currentChar2 = bufferedInput2.peek();

            if (currentChar1 == EOF) {
                if (currentChar2 == EOF) {
                    return true;
                } else {
                    if (!justNewLine) {
                        return inputOnlyHaveCRLForEOF(bufferedInput2, currentChar2);
                    }
                    return false;
                }
            } else if (currentChar2 == EOF) {
                if (!justNewLine) {
                    return inputOnlyHaveCRLForEOF(bufferedInput1, currentChar1);
                }
                return false;
            }
            if (currentChar1 != currentChar2) {
                return false;
            }
            justNewLine = currentChar1 == '\n';
            bufferedInput1.eat();
            bufferedInput2.eat();
        }
    }

    /**
     * private function used only in contentEqualsIgnoreEOL.
     * used in contentEqualsIgnoreEOL to detect whether a input only have CRLF or EOF.
     * @param input input reader
     * @param currentChar current peek char of input
     * @return true/false
     * @throws IOException by input.read(), not me.
     * @see #contentEqualsIgnoreEOL(Reader, Reader)
     */
    private static boolean inputOnlyHaveCRLForEOF(LineEndUnifiedBufferedReader input, int currentChar) throws IOException {

        /*
         * logically there should be some code like
         *
         *  if (char1 == EOF) {
         *      return true;
         *  }
         *
         * here.
         *
         * But actually, if this input's read() is EOF, then we will not invoke this function at all.
         * So the check is deleted.
         *
         * You can go contentEqualsIgnoreEOL for details.
         */

        if (currentChar == '\n') {
            input.eat();
            return input.read() == EOF;
        }
        return false;
    }


    @Benchmark
    public boolean[] testFileCurrent() throws IOException {
        final boolean[] res = new boolean[3];
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_B), DEFAULT_CHARSET)) {
            res[0] = IOUtils.contentEqualsIgnoreEOL(input1, input1);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET)) {
            res[1] = IOUtils.contentEqualsIgnoreEOL(input1, input2);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A_COPY),
                DEFAULT_CHARSET)) {
            res[2] = IOUtils.contentEqualsIgnoreEOL(input1, input2);
        }
        return res;
    }

    @Benchmark
    public boolean[] testFilePr118() throws IOException {
        final boolean[] res = new boolean[3];
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_B), DEFAULT_CHARSET)) {
            res[0] = contentEqualsIgnoreEOLPr118(input1, input1);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET)) {
            res[1] = contentEqualsIgnoreEOLPr118(input1, input2);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A));
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A_COPY))) {
            res[2] = contentEqualsIgnoreEOLPr118(input1, input2);
        }
        return res;
    }

    @Benchmark
    public boolean[] testFileRelease_2_8_0() throws IOException {
        final boolean[] res = new boolean[3];
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_B), DEFAULT_CHARSET)) {
            res[0] = contentEqualsIgnoreEOL_release_2_8_0(input1, input1);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET)) {
            res[1] = contentEqualsIgnoreEOL_release_2_8_0(input1, input2);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A_COPY),
                DEFAULT_CHARSET)) {
            res[2] = contentEqualsIgnoreEOL_release_2_8_0(input1, input2);
        }
        return res;
    }

    @Benchmark
    public void testStringCurrent(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (StringReader input1 = new StringReader(STRINGS[i]);
                    StringReader input2 = new StringReader(STRINGS[j])) {
                    blackhole.consume(IOUtils.contentEqualsIgnoreEOL(input1, input2));
                }
            }
        }
    }

    @Benchmark
    public void testStringPr118(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (StringReader input1 = new StringReader(STRINGS[i]);
                    StringReader input2 = new StringReader(STRINGS[j])) {
                    blackhole.consume(contentEqualsIgnoreEOLPr118(input1, input2));
                }
            }
        }
    }

    @Benchmark
    public void testStringRelease_2_8_0(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (StringReader input1 = new StringReader(STRINGS[i]);
                    StringReader input2 = new StringReader(STRINGS[j])) {
                    blackhole.consume(contentEqualsIgnoreEOL_release_2_8_0(input1, input2));
                }
            }
        }
    }

}
