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

package org.apache.commons.io.jmh;

import static org.apache.commons.io.IOUtils.DEFAULT_BUFFER_SIZE;
import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
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
 * Test different implementations of {@link IOUtils#contentEquals(Reader, Reader)}.
 *
 * <pre>
 * RESULTS:
 * Benchmark                                                            Mode  Cnt          Score         Error  Units
 * IOUtilsContentEqualsReadersBenchmark_2_22_0.testFileCurrent          avgt    5     105274.452 ±    1466.048  ns/op
 * IOUtilsContentEqualsReadersBenchmark_2_22_0.testFileRelease2_22_0    avgt    5     107500.847 ±    1752.422  ns/op
 * IOUtilsContentEqualsReadersBenchmark_2_22_0.testFile_2_21_0          avgt    5     115720.416 ±    1209.652  ns/op
 * IOUtilsContentEqualsReadersBenchmark_2_22_0.testStringCurrent        avgt    5  113330719.330 ± 1187191.151  ns/op
 * IOUtilsContentEqualsReadersBenchmark_2_22_0.testStringRelease2_22_0  avgt    5  110389392.582 ±  785367.455  ns/op
 * IOUtilsContentEqualsReadersBenchmark_2_22_0.testString_2_21_0        avgt    5  284939866.619 ± 9969793.485  ns/op
 *
 * Run: mvn clean test -P benchmark -Dbenchmark=IOUtilsContentEqualsReadersBenchmark_2_22_0
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server"})
public class IOUtilsContentEqualsReadersBenchmark_2_22_0 {

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

    public static boolean contentEquals_2_21_0(final Reader input1, final Reader input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }
        final char[] array1 = new char[DEFAULT_BUFFER_SIZE];
        final char[] array2 = new char[DEFAULT_BUFFER_SIZE];
        int pos1;
        int pos2;
        int count1;
        int count2;
        while (true) {
            pos1 = 0;
            pos2 = 0;
            for (int index = 0; index < DEFAULT_BUFFER_SIZE; index++) {
                if (pos1 == index) {
                    do {
                        count1 = input1.read(array1, pos1, DEFAULT_BUFFER_SIZE - pos1);
                    } while (count1 == 0);
                    if (count1 == EOF) {
                        return pos2 == index && input2.read() == EOF;
                    }
                    pos1 += count1;
                }
                if (pos2 == index) {
                    do {
                        count2 = input2.read(array2, pos2, DEFAULT_BUFFER_SIZE - pos2);
                    } while (count2 == 0);
                    if (count2 == EOF) {
                        return pos1 == index && input1.read() == EOF;
                    }
                    pos2 += count2;
                }
                if (array1[index] != array2[index]) {
                    return false;
                }
            }
        }
    }

    /**
     * Version 2.22.0 (December 2025).
     */
    public static boolean contentEqualsRelease2_22_0(final Reader input1, final Reader input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }
        final char[] array1 = new char[DEFAULT_BUFFER_SIZE];
        final char[] array2 = new char[DEFAULT_BUFFER_SIZE];
        int read1;
        int read2;
        while (true) {
            read1 = input1.read(array1, 0, DEFAULT_BUFFER_SIZE);
            read2 = input2.read(array2, 0, DEFAULT_BUFFER_SIZE);
            // If both read EOF here, they're equal.
            if (read1 == EOF && read2 == EOF) {
                return true;
            }
            // If only one read EOF or different amounts, they're not equal.
            if (read1 != read2) {
                return false;
            }
            // Compare the buffers - bulk comparison is faster than character-by-character
            for (int i = 0; i < read1; i++) {
                if (array1[i] != array2[i]) {
                    return false;
                }
            }
        }
    }

    @Benchmark
    public boolean[] testFile_2_21_0() throws IOException {
        final boolean[] res = new boolean[3];
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_B), DEFAULT_CHARSET)) {
            res[0] = contentEquals_2_21_0(input1, input1);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET)) {
            res[1] = contentEquals_2_21_0(input1, input2);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A));
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A_COPY))) {
            res[2] = contentEquals_2_21_0(input1, input2);
        }
        return res;
    }

    @Benchmark
    public boolean[] testFileCurrent() throws IOException {
        final boolean[] res = new boolean[3];
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_B), DEFAULT_CHARSET)) {
            res[0] = IOUtils.contentEquals(input1, input1);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET)) {
            res[1] = IOUtils.contentEquals(input1, input2);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A_COPY),
                DEFAULT_CHARSET)) {
            res[2] = IOUtils.contentEquals(input1, input2);
        }
        return res;
    }

    @Benchmark
    public boolean[] testFileRelease2_22_0() throws IOException {
        final boolean[] res = new boolean[3];
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_B), DEFAULT_CHARSET)) {
            res[0] = contentEqualsRelease2_22_0(input1, input1);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_A), DEFAULT_CHARSET)) {
            res[1] = contentEqualsRelease2_22_0(input1, input2);
        }
        try (Reader input1 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A), DEFAULT_CHARSET);
            Reader input2 = new InputStreamReader(getClass().getResourceAsStream(TEST_PATH_16K_A_COPY),
                DEFAULT_CHARSET)) {
            res[2] = contentEqualsRelease2_22_0(input1, input2);
        }
        return res;
    }

    @Benchmark
    public void testString_2_21_0(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (StringReader input1 = new StringReader(STRINGS[i]);
                    StringReader input2 = new StringReader(STRINGS[j])) {
                    blackhole.consume(contentEquals_2_21_0(input1, input2));
                }
            }
        }
    }

    @Benchmark
    public void testStringCurrent(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (StringReader input1 = new StringReader(STRINGS[i]);
                    StringReader input2 = new StringReader(STRINGS[j])) {
                    blackhole.consume(IOUtils.contentEquals(input1, input2));
                }
            }
        }
    }

    @Benchmark
    public void testStringRelease2_22_0(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (StringReader input1 = new StringReader(STRINGS[i]);
                    StringReader input2 = new StringReader(STRINGS[j])) {
                    blackhole.consume(contentEqualsRelease2_22_0(input1, input2));
                }
            }
        }
    }

}
