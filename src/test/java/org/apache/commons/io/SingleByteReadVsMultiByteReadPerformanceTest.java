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
package org.apache.commons.io;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.io.InputStream;
import java.io.BufferedInputStream;

/**
 *  Test to show that {@link InputStream#read(byte[])} is more performant than {@link BufferedInputStream#read()}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class SingleByteReadVsMultiByteReadPerformanceTest {
    private static final int TEST_DATA_FILE_SIZE = 8 * 1024 * 1024; // 8MB
    private File dataFile;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        dataFile = Files.createTempFile("SingleByteReadVsMultiByteReadPerformanceTest",
                UUID.randomUUID().toString()).toFile();

        System.out.printf("Writing %s bytes to %s.%n", TEST_DATA_FILE_SIZE, dataFile.getCanonicalPath());

        byte[] randomData = new byte[TEST_DATA_FILE_SIZE];
        Random random = new Random();
        random.nextBytes(randomData);
        FileUtils.writeByteArrayToFile(dataFile, randomData);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        System.out.printf("Deleting %s.%n", dataFile.getCanonicalPath());

        FileUtils.deleteQuietly(dataFile);
    }

    @Benchmark
    public void testReadSingleByte(Blackhole blackhole) throws IOException {
        int sum = 0;
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(dataFile))) {
            int readValue;
            while ((readValue = input.read()) != -1) {
                sum += readValue;
            }
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void testReadMultiBytes(Blackhole blackhole) throws IOException {
        byte sum = 0;
        try(FileInputStream input = new FileInputStream(dataFile)) {
            int readValue;
            byte[] buffer = new byte[8 * 1024]; // 8K read buffer
            while ((readValue = input.read(buffer)) != -1) {
                for(int i = 0; i < readValue; i++) {
                    sum += buffer[i];
                }
            }
        }

        blackhole.consume(sum);
    }
}
