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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/**
 * Measures reading a newline-free file with increasing line lengths to expose super-linear processing costs.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ReversedLinesFileReaderBenchmark {

    /**
     * The number of bytes in the line.
     */
    @Param({ "262144", "1048576", "4194304" })
    public int lineLength;

    private Path path;

    /**
     * Reads the last line using a fixed block size.
     *
     * @return The line read.
     * @throws IOException if an I/O error occurs.
     */
    @Benchmark
    public String readLine() throws IOException {
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setPath(path)
                .setBufferSize(4096)
                .setCharset(StandardCharsets.US_ASCII)
                .get()) {
            return reader.readLine();
        }
    }

    /**
     * Creates the input file outside the measured operation.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Setup
    public void setup() throws IOException {
        path = Files.createTempFile(getClass().getSimpleName(), ".txt");
        final byte[] data = new byte[lineLength];
        Arrays.fill(data, (byte) 'a');
        Files.write(path, data);
    }

    /**
     * Deletes the input file.
     *
     * @throws IOException if an I/O error occurs.
     */
    @TearDown
    public void tearDown() throws IOException {
        Files.deleteIfExists(path);
    }
}
