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
package org.apache.commons.io.performance;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.buffer.UnsynchronizedBufferedInputStream;
import org.apache.commons.io.input.buffer.UnsynchronizedBufferedReader;
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
 * Test to show whether using BitSet for removeAll() methods is faster than using HashSet.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server"})
public class IOUtilsContentEqualsPerformanceTest {
    static String[] STRINGS = new String[1];

    static {
        STRINGS[0] = StringUtils.repeat("ab", 1 << 24);
    }

    public void readAll(InputStream inputStream, Blackhole blackhole) throws IOException {
        while (true) {
            int nowI = inputStream.read();
            // do nothing
            blackhole.consume(nowI);
            if (nowI != -1) {
                break;
            }
        }
    }

    @Benchmark
    public void testBufferedInputStreamReadAll(Blackhole blackhole) throws IOException {
        try (
                InputStream inputStream = IOUtils.toInputStream(STRINGS[0]);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)
        ) {
            readAll(bufferedInputStream, blackhole);
        }
    }

    @Benchmark
    public void testUnsynchronizedBufferedInputStreamReadAll(Blackhole blackhole) throws IOException {
        try (
                InputStream inputStream = IOUtils.toInputStream(STRINGS[0]);
                UnsynchronizedBufferedInputStream bufferedInputStream =
                        new UnsynchronizedBufferedInputStream(inputStream)
        ) {
            readAll(bufferedInputStream, blackhole);
        }
    }

    public void readAll(Reader reader, Blackhole blackhole) throws IOException {
        while (true) {
            int nowI = reader.read();
            // do nothing
            blackhole.consume(nowI);
            if (nowI != -1) {
                break;
            }
        }
    }

    @Benchmark
    public void testBufferedReaderReadAll(Blackhole blackhole) throws IOException {
        try (
                StringReader inputReader = new StringReader(STRINGS[0]);
                BufferedReader bufferedReader = new BufferedReader(inputReader)
        ) {
            readAll(bufferedReader, blackhole);
        }
    }

    @Benchmark
    public void testUnsynchronizedBufferedReaderReadAll(Blackhole blackhole) throws IOException {
        try (
                StringReader inputReader = new StringReader(STRINGS[0]);
                UnsynchronizedBufferedReader bufferedReader = new UnsynchronizedBufferedReader(inputReader)
        ) {
            readAll(bufferedReader, blackhole);
        }
    }

    public void readAllUsingReadToArray(InputStream inputStream, Blackhole blackhole) throws IOException {
        byte[] bytes = new byte[1];
        while (true) {
            int nowI = inputStream.read(bytes, 0, 1);
            // do nothing
            blackhole.consume(nowI);
            if (nowI != -1) {
                break;
            }
        }
    }

    @Benchmark
    public void testBufferedInputStreamReadAllUsingReadToArray(Blackhole blackhole) throws IOException {
        try (
                InputStream inputStream = IOUtils.toInputStream(STRINGS[0]);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)
        ) {
            readAllUsingReadToArray(bufferedInputStream, blackhole);
        }
    }

    @Benchmark
    public void testUnsynchronizedBufferedInputStreamReadAllUsingReadToArray(Blackhole blackhole) throws IOException {
        try (
                InputStream inputStream = IOUtils.toInputStream(STRINGS[0]);
                UnsynchronizedBufferedInputStream bufferedInputStream =
                        new UnsynchronizedBufferedInputStream(inputStream)
        ) {
            readAllUsingReadToArray(bufferedInputStream, blackhole);
        }
    }

    public void readAllUsingReadToArray(Reader reader, Blackhole blackhole) throws IOException {
        char[] bytes = new char[1];
        while (true) {
            int nowI = reader.read(bytes, 0, 1);
            // do nothing
            blackhole.consume(nowI);
            if (nowI != -1) {
                break;
            }
        }
    }

    @Benchmark
    public void testBufferedReaderReadAllUsingReadToArray(Blackhole blackhole) throws IOException {
        try (
                StringReader inputReader = new StringReader(STRINGS[0]);
                BufferedReader bufferedReader = new BufferedReader(inputReader)
        ) {
            readAllUsingReadToArray(bufferedReader, blackhole);
        }
    }

    @Benchmark
    public void testUnsynchronizedBufferedReaderReadAllUsingReadToArray(Blackhole blackhole) throws IOException {
        try (
                StringReader inputReader = new StringReader(STRINGS[0]);
                UnsynchronizedBufferedReader bufferedReader = new UnsynchronizedBufferedReader(inputReader)
        ) {
            readAllUsingReadToArray(bufferedReader, blackhole);
        }
    }

}