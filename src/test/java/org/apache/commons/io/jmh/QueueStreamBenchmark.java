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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.QueueInputStream;
import org.apache.commons.io.output.QueueOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Measures the amount of time to push 1 MiB to a {@link QueueOutputStream} and read it using a {@link QueueInputStream}
 */
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Group)
public class QueueStreamBenchmark {

    private static final int CAPACITY = 1024 * 1024;
    private static final int BUFFER_SIZE = 1024;

    private final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(CAPACITY);
    private final QueueInputStream inputStream = QueueInputStream.builder()
            .setBlockingQueue(queue)
            .get();
    private final QueueOutputStream outputStream = inputStream.newQueueOutputStream();

    private final byte[] input = RandomUtils.insecure().randomBytes(CAPACITY);
    private final byte[] output = new byte[BUFFER_SIZE];

    @Benchmark
    @Group("streams")
    public void output() throws Exception {
        int sent = 0;
        while (sent < CAPACITY) {
            final int len = Math.min(CAPACITY - sent, BUFFER_SIZE);
            outputStream.write(input, sent, len);
            sent += len;
        }
    }

    @Benchmark
    @Group("streams")
    public void input(Blackhole bh) throws Exception {
        int received = 0;
        while (received < CAPACITY) {
            final int len = inputStream.read(output, 0, BUFFER_SIZE);
            bh.consume(output);
            received += len;
        }
    }
}
