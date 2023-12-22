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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.RandomAccessFiles;
import org.apache.commons.io.file.PathUtils;
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
 * Test different implementations of {@link PathUtils#fileContentEquals(Path, Path)}.
 *
 * <pre>
 * Benchmark                                                                Mode  Cnt    Score   Error  Units
 * PathUtilsContentEqualsBenchmark.testCurrent_fileContentEquals            avgt    5    4.538 ▒  1.010  ms/op
 * PathUtilsContentEqualsBenchmark.testCurrent_fileContentEquals_Blackhole  avgt    5  110.627 ▒ 30.317  ms/op
 * PathUtilsContentEqualsBenchmark.testProposal_contentEquals               avgt    5    1.812 ▒  0.634  ms/op
 * PathUtilsContentEqualsBenchmark.testProposal_contentEquals_Blackhole     avgt    5   43.521 ▒  6.762  ms/op
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "-server" })
public class PathUtilsContentEqualsBenchmark {

    private static final Path bigFile1;
    private static final Path bigFile2;

    static {
        // Set up test fixtures
        try {
            bigFile1 = Files.createTempFile(PathUtilsContentEqualsBenchmark.class.getSimpleName(), "-1.bin");
            bigFile2 = Files.createTempFile(PathUtilsContentEqualsBenchmark.class.getSimpleName(), "-2.bin");
            final int newLength = 1_000_000;
            final byte[] bytes1 = new byte[newLength];
            Arrays.fill(bytes1, (byte) 1);
            Files.write(bigFile1, bytes1);
            Files.copy(bigFile1, bigFile2, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean newFileContentEquals(final Path path1, final Path path2) throws IOException {
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(path1);
                RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(path2)) {
            return RandomAccessFiles.contentEquals(raf1, raf2);
        }
    }

    @Benchmark
    public boolean[] testCurrent_fileContentEquals() throws IOException {
        final boolean[] res = new boolean[1];
        res[0] = PathUtils.fileContentEquals(bigFile1, bigFile2);
        return res;
    }

    @Benchmark
    public void testCurrent_fileContentEquals_Blackhole(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                blackhole.consume(PathUtils.fileContentEquals(bigFile1, bigFile2));
            }
        }
    }

    @Benchmark
    public boolean[] testProposal_contentEquals() throws IOException {
        final boolean[] res = new boolean[1];
        res[0] = newFileContentEquals(bigFile1, bigFile2);
        return res;
    }

    @Benchmark
    public void testProposal_contentEquals_Blackhole(final Blackhole blackhole) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                blackhole.consume(newFileContentEquals(bigFile1, bigFile2));
            }
        }
    }

}
