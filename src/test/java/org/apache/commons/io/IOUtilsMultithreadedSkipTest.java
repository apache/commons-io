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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * See Jira ticket IO-802.
 */
public class IOUtilsMultithreadedSkipTest {

    private static final String FIXTURE = "TIKA-4065.bin";
    long seed = 1;
    private final ThreadLocal<byte[]> threadLocal = ThreadLocal.withInitial(() -> new byte[4096]);

    private int[] generateExpected(final InputStream is, final int[] skips) throws IOException {
        final int[] testBytes = new int[skips.length];
        for (int i = 0; i < skips.length; i++) {
            try {
                IOUtils.skipFully(is, skips[i]);
                testBytes[i] = is.read();
            } catch (final EOFException e) {
                testBytes[i] = -1;
            }
        }
        return testBytes;
    }

    private int[] generateSkips(final byte[] bytes, final int numSkips, final Random random) {
        final int[] skips = new int[numSkips];
        for (int i = 0; i < skips.length; i++) {
            skips[i] = random.nextInt(bytes.length / numSkips) + bytes.length / 10;
        }
        return skips;
    }

    private InputStream inflate(final byte[] deflated) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(new InflaterInputStream(new ByteArrayInputStream(deflated), new Inflater(true)), bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    @BeforeEach
    public void setUp() {
        // Not the best random we can use but good enough here.
        seed = new Random().nextLong();
    }

    private void testSkipFullyOnInflaterInputStream(final Supplier<byte[]> baSupplier) throws Exception {
        final long thisSeed = seed;
        // thisSeed = -727624427837034313l;
        final Random random = new Random(thisSeed);
        final byte[] bytes;
        try (InputStream inputStream = getClass().getResourceAsStream(FIXTURE)) {
            bytes = IOUtils.toByteArray(inputStream);
        }
        final int numSkips = random.nextInt(bytes.length) / 100 + 1;

        final int[] skips = generateSkips(bytes, numSkips, random);
        final int[] expected;
        try (InputStream inflate = inflate(bytes)) {
            expected = generateExpected(inflate, skips);
        }

        final int numThreads = 2;
        final int iterations = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        final ExecutorCompletionService<Integer> executorCompletionService = new ExecutorCompletionService<>(executorService);

        for (int i = 0; i < numThreads; i++) {
            executorCompletionService.submit(() -> {
                for (int iteration = 0; iteration < iterations; iteration++) {
                    try (InputStream is = new InflaterInputStream(new ByteArrayInputStream(bytes), new Inflater(true))) {
                        for (int skipIndex = 0; skipIndex < skips.length; skipIndex++) {
                            try {
                                IOUtils.skipFully(is, skips[skipIndex], baSupplier);
                                final int c = is.read();
                                assertEquals(expected[skipIndex], c, "failed on seed=" + seed + " iteration=" + iteration);
                            } catch (final EOFException e) {
                                assertEquals(expected[skipIndex], is.read(), "failed on " + "seed=" + seed + " iteration=" + iteration);
                            }
                        }
                    }
                }
                return 1;
            });
        }

        int finished = 0;
        while (finished < numThreads) {
            // blocking
            final Future<Integer> future = executorCompletionService.take();
            try {
                future.get();
            } catch (final Exception e) {
                // printStackTrace() for simpler debugging
                e.printStackTrace();
                fail("failed on seed=" + seed);
            }
            finished++;
        }
    }

    @Test
    public void testSkipFullyOnInflaterInputStream_New_bytes() throws Exception {
        testSkipFullyOnInflaterInputStream(() -> new byte[4096]);
    }

    @Test
    public void testSkipFullyOnInflaterInputStream_ThreadLocal() throws Exception {
        testSkipFullyOnInflaterInputStream(threadLocal::get);
    }

}
