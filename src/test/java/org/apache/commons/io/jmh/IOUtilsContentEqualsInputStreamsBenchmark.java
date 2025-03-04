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

import static org.apache.commons.io.IOUtils.DEFAULT_BUFFER_SIZE;
import static org.apache.commons.io.IOUtils.EOF;
import static org.apache.commons.io.IOUtils.buffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.channels.FileChannels;
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
 * Test different implementations of {@link IOUtils#contentEquals(InputStream, InputStream)}.
 *
 * <pre>{@code
Benchmark                                                          Mode  Cnt           Score           Error  Units
IOUtilsContentEqualsInputStreamsBenchmark.testFileChannels         avgt    5       65105.350 ±      2655.812  ns/op
IOUtilsContentEqualsInputStreamsBenchmark.testFileCurrent          avgt    5       75452.987 ±       260.088  ns/op
IOUtilsContentEqualsInputStreamsBenchmark.testFilePr118            avgt    5       74346.141 ±      2138.149  ns/op
IOUtilsContentEqualsInputStreamsBenchmark.testFileRelease_2_8_0    avgt    5      157246.303 ±       215.369  ns/op
IOUtilsContentEqualsInputStreamsBenchmark.testStringCurrent        avgt    5   623344988.622 ± 574407721.150  ns/op
IOUtilsContentEqualsInputStreamsBenchmark.testStringFileChannels   avgt    5   132847058.786 ±   1760007.730  ns/op
IOUtilsContentEqualsInputStreamsBenchmark.testStringPr118          avgt    5   459079096.521 ± 512827244.936  ns/op
IOUtilsContentEqualsInputStreamsBenchmark.testStringRelease_2_8_0  avgt    5  2555773583.300 ±  18112219.764  ns/op

[INFO] Finished at: 2025-03-03T21:11:56-05:00
 * }</pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server"})
public class IOUtilsContentEqualsInputStreamsBenchmark {

    private static final String TEST_PATH_A = "/org/apache/commons/io/testfileBOM.xml";
    private static final String TEST_PATH_16K_A = "/org/apache/commons/io/abitmorethan16k.txt";
    private static final String TEST_PATH_16K_A_COPY = "/org/apache/commons/io/abitmorethan16kcopy.txt";
    private static final String TEST_PATH_B = "/org/apache/commons/io/testfileNoBOM.xml";
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    static String[] STRINGS = new String[5];

    static {
        STRINGS[0] = StringUtils.repeat("ab", 1 << 24);
        STRINGS[1] = STRINGS[0] + 'c';
        STRINGS[2] = STRINGS[0] + 'd';
        STRINGS[3] = StringUtils.repeat("ab\rab\n", 1 << 24);
        STRINGS[4] = StringUtils.repeat("ab\r\nab\r", 1 << 24);
    }

    static String SPECIAL_CASE_STRING_0 = StringUtils.repeat(StringUtils.repeat("ab", 1 << 24) + '\n', 2);
    static String SPECIAL_CASE_STRING_1 = StringUtils.repeat(StringUtils.repeat("cd", 1 << 24) + '\n', 2);

    @SuppressWarnings("resource")
    public static boolean contentEquals_release_2_8_0(final InputStream input1, final InputStream input2)
        throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null ^ input2 == null) {
            return false;
        }
        final BufferedInputStream bufferedInput1 = buffer(input1);
        final BufferedInputStream bufferedInput2 = buffer(input2);
        int ch = bufferedInput1.read();
        while (EOF != ch) {
            final int ch2 = bufferedInput2.read();
            if (ch != ch2) {
                return false;
            }
            ch = bufferedInput1.read();
        }
        return bufferedInput2.read() == EOF;

    }

    public static boolean contentEqualsFileChannels(final InputStream input1, final InputStream input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }
        return FileChannels.contentEquals(Channels.newChannel(input1), Channels.newChannel(input2), IOUtils.DEFAULT_BUFFER_SIZE);
    }

    public static boolean contentEqualsPr118(final InputStream input1, final InputStream input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }

        final byte[] array1 = new byte[DEFAULT_BUFFER_SIZE];
        final byte[] array2 = new byte[DEFAULT_BUFFER_SIZE];
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

    @Benchmark
    public boolean[] testFileChannels() throws IOException {
        final boolean[] res = new boolean[3];
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_B)) {
            res[0] = contentEqualsFileChannels(input1, input1);
        }
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_A)) {
            res[1] = contentEqualsFileChannels(input1, input2);
        }
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_16K_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_16K_A_COPY)) {
            res[2] = contentEqualsFileChannels(input1, input2);
        }
        return res;
    }

    @Benchmark
    public boolean[] testFileCurrent() throws IOException {
        final boolean[] res = new boolean[3];
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_B)) {
            res[0] = IOUtils.contentEquals(input1, input1);
        }
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_A);) {
            res[1] = IOUtils.contentEquals(input1, input2);
        }
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_16K_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_16K_A_COPY);) {
            res[2] = IOUtils.contentEquals(input1, input2);
        }
        return res;
    }

    @Benchmark
    public boolean[] testFilePr118() throws IOException {
        final boolean[] res = new boolean[3];
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_B)) {
            res[0] = contentEqualsPr118(input1, input1);
        }
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_A)) {
            res[1] = contentEqualsPr118(input1, input2);
        }
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_16K_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_16K_A_COPY)) {
            res[2] = contentEqualsPr118(input1, input2);
        }
        return res;
    }

    @Benchmark
    public boolean[] testFileRelease_2_8_0() throws IOException {
        final boolean[] res = new boolean[3];
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_B)) {
            res[0] = contentEquals_release_2_8_0(input1, input1);
        }
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_A);) {
            res[1] = contentEquals_release_2_8_0(input1, input2);
        }
        try (InputStream input1 = getClass().getResourceAsStream(TEST_PATH_16K_A);
            InputStream input2 = getClass().getResourceAsStream(TEST_PATH_16K_A_COPY)) {
            res[2] = contentEquals_release_2_8_0(input1, input2);
        }
        return res;
    }

    @Benchmark
    public void testStringCurrent(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (InputStream inputReader1 = IOUtils.toInputStream(STRINGS[i], DEFAULT_CHARSET);
                    InputStream inputReader2 = IOUtils.toInputStream(STRINGS[j], DEFAULT_CHARSET)) {
                    blackhole.consume(IOUtils.contentEquals(inputReader1, inputReader2));
                }
            }
        }
    }

    @Benchmark
    public void testStringFileChannels(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (InputStream input1 = IOUtils.toInputStream(STRINGS[i], DEFAULT_CHARSET);
                    InputStream input2 = IOUtils.toInputStream(STRINGS[j], DEFAULT_CHARSET)) {
                    blackhole.consume(contentEqualsFileChannels(input1, input2));
                }
            }
        }
    }

    @Benchmark
    public void testStringPr118(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (InputStream input1 = IOUtils.toInputStream(STRINGS[i], DEFAULT_CHARSET);
                    InputStream input2 = IOUtils.toInputStream(STRINGS[j], DEFAULT_CHARSET)) {
                    blackhole.consume(contentEqualsPr118(input1, input2));
                }
            }
        }
    }

    @Benchmark
    public void testStringRelease_2_8_0(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                try (InputStream input1 = IOUtils.toInputStream(STRINGS[i], DEFAULT_CHARSET);
                    InputStream input2 = IOUtils.toInputStream(STRINGS[j], DEFAULT_CHARSET)) {
                    blackhole.consume(contentEquals_release_2_8_0(input1, input2));
                }
            }
        }
    }

}
