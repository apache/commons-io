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

package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link Tailer} termination and reader cleanup for <a href="https://issues.apache.org/jira/browse/IO-889">IO-889</a>.
 */
public class TailerCloseTest {

    private enum CloseFailure {
        NONE, IO, UNCHECKED
    }

    private class TailerTestListener extends TailerListenerAdapter {

        @Override
        public void handle(final Exception ex) {
            super.handle(ex);
            result.completeExceptionally(ex);
        }

        @Override
        public void handle(final String line) {
            super.handle(line);
            result.complete(line);
        }
    }

    private static void await(final CountDownLatch latch) {
        assertTrue(assertDoesNotThrow(() -> latch.await(10, TimeUnit.SECONDS)));
    }

    private static Stream<Duration> largeTimeouts() {
        return Stream.of(Duration.ofNanos(Long.MAX_VALUE), Duration.ofNanos(Long.MAX_VALUE).plusNanos(1),
                Duration.ofSeconds(Long.MAX_VALUE), Duration.ofSeconds(Long.MAX_VALUE, 999_999_999));
    }

    private static Thread newDaemonThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable, "commons-io-tailer");
        thread.setDaemon(true);
        return thread;
    }

    private static Stream<Duration> nonPositiveTimeouts() {
        return Stream.of(Duration.ZERO, Duration.ofNanos(-1), Duration.ofSeconds(-1), Duration.ofSeconds(Long.MIN_VALUE));
    }

    private ExecutorService executorService;
    private Path path;
    private final CompletableFuture<String> result = new CompletableFuture<>();
    private final CountDownLatch release = new CountDownLatch(1);
    private Tailer tailer;
    private Thread thread;
    private Tailer.RandomAccessResourceBridge resource;

    private Tailer.Builder newBuilder() throws IOException {
        path = Files.createTempFile("TailerCloseTest", ".log");
        Files.write(path, "aaa\n".getBytes(StandardCharsets.UTF_8));
        return Tailer.builder().setPath(path).setCharset(StandardCharsets.UTF_8).setDelayDuration(Duration.ofMillis(1))
                .setTailerListener(new TailerTestListener());
    }

    @AfterEach
    void tearDown() throws Exception {
        release.countDown();
        if (tailer != null) {
            tailer.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        final boolean tailerTerminated;
        final boolean executorTerminated;
        try {
            tailerTerminated = tailer == null || tailer.awaitTermination(Duration.ofSeconds(10));
            if (thread != null) {
                thread.join(10000);
            }
            executorTerminated = executorService == null || executorService.awaitTermination(10, TimeUnit.SECONDS);
        } finally {
            try {
                if (resource != null) {
                    resource.close();
                }
            } finally {
                if (path != null) {
                    Files.deleteIfExists(path);
                }
            }
        }
        assertTrue(tailerTerminated, "Tailer did not finish cleanup");
        assertTrue(thread == null || !thread.isAlive(), "Manual tailer thread did not finish");
        assertTrue(executorTerminated, "Executor did not terminate");
    }

    @ParameterizedTest(name = "mode {0}: 0=builder, 1=executor, 2=manual")
    @ValueSource(ints = { 0, 1, 2 })
    void testAwaitCallback(final int mode) throws Exception {
        final Tailer.Builder builder = newBuilder().setTailerListener(new TailerTestListener() {
            @Override
            public void handle(final String line) {
                super.handle(line);
                await(release);
            }
        });
        if (mode == 1) {
            executorService = Executors.newSingleThreadExecutor(TailerCloseTest::newDaemonThread);
            builder.setExecutorService(executorService);
        }
        tailer = builder.setStartThread(mode != 2).get();
        if (mode == 2) {
            thread = newDaemonThread(tailer);
            thread.start();
        }
        assertEquals("aaa", result.get(10, TimeUnit.SECONDS));
        tailer.close();
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedException.class, () -> tailer.awaitTermination(Duration.ofSeconds(10)));
        } finally {
            Thread.interrupted();
        }
        assertFalse(tailer.awaitTermination(Duration.ofMillis(1)));
        release.countDown();
        assertTrue(tailer.awaitTermination(Duration.ofSeconds(10)));
        Files.delete(path);
        if (executorService != null) {
            assertEquals("available", executorService.submit(() -> "available").get(10, TimeUnit.SECONDS));
        }
    }

    @ParameterizedTest
    @EnumSource(CloseFailure.class)
    void testAwaitCleanup(final CloseFailure closeFailure) throws Exception {
        final Tailer.Tailable delegate = newBuilder().setStartThread(false).get().getTailable();
        final CountDownLatch closing = new CountDownLatch(1);
        final Tailer.Tailable controlled = new Tailer.Tailable() {
            @Override
            public Tailer.RandomAccessResourceBridge getRandomAccess(final String mode) throws FileNotFoundException {
                resource = delegate.getRandomAccess(mode);
                return new Tailer.RandomAccessResourceBridge() {
                    @Override
                    public void close() throws IOException {
                        closing.countDown();
                        await(release);
                        if (closeFailure == CloseFailure.IO) {
                            throw new IOException("close failure");
                        }
                        if (closeFailure == CloseFailure.UNCHECKED) {
                            throw new IllegalStateException("close failure");
                        }
                        resource.close();
                    }

                    @Override
                    public long getPointer() throws IOException {
                        return resource.getPointer();
                    }

                    @Override
                    public int read(final byte[] bytes) throws IOException {
                        return resource.read(bytes);
                    }

                    @Override
                    public void seek(final long position) throws IOException {
                        resource.seek(position);
                    }
                };
            }

            @Override
            public boolean isNewer(final FileTime time) throws IOException {
                return delegate.isNewer(time);
            }

            @Override
            public FileTime lastModifiedFileTime() throws IOException {
                return delegate.lastModifiedFileTime();
            }

            @Override
            public long size() throws IOException {
                return delegate.size();
            }
        };
        tailer = Tailer.builder().setTailable(controlled).setStartThread(false).setDelayDuration(Duration.ofMillis(1))
                .setTailerListener(new TailerTestListener() {
                    @Override
                    public void handle(final Exception exception) {
                        throw new IllegalStateException("cleanup listener", exception);
                    }
                }).get();
        executorService = Executors.newSingleThreadExecutor(TailerCloseTest::newDaemonThread);
        final Future<?> execution = executorService.submit(tailer);
        assertEquals("aaa", result.get(10, TimeUnit.SECONDS));
        tailer.close();
        await(closing);
        assertFalse(tailer.awaitTermination(Duration.ofMillis(1)));
        release.countDown();
        assertTrue(tailer.awaitTermination(Duration.ofSeconds(10)));
        if (closeFailure != CloseFailure.NONE) {
            assertInstanceOf(IllegalStateException.class, assertThrows(ExecutionException.class, () -> execution.get(10, TimeUnit.SECONDS)).getCause());
            assertEquals(4, resource.getPointer());
            resource.close();
        } else {
            execution.get(10, TimeUnit.SECONDS);
        }
        Files.delete(path);
    }

    @Test
    void testAwaitDoesNotStopTailer() throws Exception {
        final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        tailer = newBuilder().setTailerListener(new TailerTestListener() {
            @Override
            public void handle(final String line) {
                super.handle(line);
                lines.add(line);
            }
        }).get();
        assertEquals("aaa", lines.poll(10, TimeUnit.SECONDS));
        assertFalse(tailer.awaitTermination(Duration.ofMillis(1)));
        Files.write(path, "bbb\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        assertEquals("bbb", lines.poll(10, TimeUnit.SECONDS));
        tailer.close();
        assertTrue(tailer.awaitTermination(Duration.ofSeconds(10)));
        Files.delete(path);
    }

    @Test
    void testAwaitFromCallback() throws Exception {
        tailer = newBuilder().setTailerListener(new TailerListenerAdapter() {
            private Tailer callbackTailer;

            @Override
            public void handle(final String line) {
                callbackTailer.close();
                result.complete(assertThrows(IllegalStateException.class,
                        () -> callbackTailer.awaitTermination(Duration.ofSeconds(10))).getMessage());
            }

            @Override
            public void init(final Tailer instance) {
                callbackTailer = instance;
            }
        }).get();
        assertEquals("Cannot await termination from the tailer thread", result.get(10, TimeUnit.SECONDS));
        assertTrue(tailer.awaitTermination(Duration.ofSeconds(10)));
        Files.delete(path);
    }

    @ParameterizedTest
    @MethodSource("largeTimeouts")
    void testAwaitLargeTimeout(final Duration timeout) throws Exception {
        tailer = newBuilder().setTailerListener(new TailerTestListener() {
            @Override
            public void handle(final String line) {
                super.handle(line);
                await(release);
            }
        }).get();
        assertEquals("aaa", result.get(10, TimeUnit.SECONDS));
        tailer.close();
        final CountDownLatch waiting = new CountDownLatch(1);
        executorService = Executors.newSingleThreadExecutor(TailerCloseTest::newDaemonThread);
        final Future<Boolean> waiter = executorService.submit(() -> {
            waiting.countDown();
            return tailer.awaitTermination(timeout);
        });
        await(waiting);
        assertThrows(TimeoutException.class, () -> waiter.get(10, TimeUnit.MILLISECONDS));
        release.countDown();
        assertTrue(waiter.get(10, TimeUnit.SECONDS));
    }

    @Test
    void testAwaitMultipleWaiters() throws Exception {
        tailer = newBuilder().setTailerListener(new TailerTestListener() {
            @Override
            public void handle(final String line) {
                super.handle(line);
                await(release);
            }
        }).get();
        assertEquals("aaa", result.get(10, TimeUnit.SECONDS));
        final CountDownLatch waiting = new CountDownLatch(2);
        final ExecutorService waiters = Executors.newFixedThreadPool(2, TailerCloseTest::newDaemonThread);
        try {
            final Future<Boolean> first = waiters.submit(() -> {
                waiting.countDown();
                return tailer.awaitTermination(Duration.ofSeconds(10));
            });
            final Future<Boolean> second = waiters.submit(() -> {
                waiting.countDown();
                return tailer.awaitTermination(Duration.ofSeconds(10));
            });
            await(waiting);
            tailer.close();
            assertFalse(first.isDone());
            assertFalse(second.isDone());
            release.countDown();
            assertTrue(first.get(10, TimeUnit.SECONDS));
            assertTrue(second.get(10, TimeUnit.SECONDS));
        } finally {
            waiters.shutdownNow();
            assertTrue(waiters.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @ParameterizedTest
    @MethodSource("nonPositiveTimeouts")
    @Timeout(10)
    void testAwaitNeverStarted(final Duration timeout) throws Exception {
        tailer = newBuilder().setStartThread(false).get();
        assertThrows(NullPointerException.class, () -> tailer.awaitTermination(null));
        assertFalse(tailer.awaitTermination(timeout));
        tailer.close();
        assertFalse(tailer.awaitTermination(timeout));
        assertFalse(tailer.awaitTermination(Duration.ofNanos(1)));
        thread = newDaemonThread(tailer);
        thread.start();
        assertTrue(tailer.awaitTermination(Duration.ofSeconds(10)));
        assertTrue(tailer.awaitTermination(timeout));
        assertFalse(result.isDone());
    }

    @Test
    void testCloseTailer() throws Exception {
        executorService = Executors.newSingleThreadExecutor(TailerCloseTest::newDaemonThread);
        tailer = newBuilder().setExecutorService(executorService).get();
        assertEquals("aaa", result.get(10, TimeUnit.SECONDS));
        tailer.close();
        assertTrue(tailer.awaitTermination(Duration.ofSeconds(10)));
        Files.delete(path);
    }
}
