/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Regression coverage for temporarily missing replacement files during rotation.
 */
class TailerRotationTest {

    private static final class Bridge implements Tailer.RandomAccessResourceBridge {

        private int closeCalls;
        private boolean closed;
        private IOException closeException;
        private IOException readException;
        private final byte[] content;
        private long pointer;

        Bridge(final String content) {
            this.content = content.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            closeCalls++;
            if (closeException != null) {
                throw closeException;
            }
        }

        @Override
        public long getPointer() throws IOException {
            if (closed) {
                throw new IOException("getPointer attempted on closed bridge");
            }
            return pointer;
        }

        @Override
        public int read(final byte[] buffer) throws IOException {
            if (closed) {
                throw new IOException("read attempted on closed bridge");
            }
            if (readException != null) {
                throw readException;
            }
            if (pointer >= content.length) {
                return -1;
            }
            final int count = (int) Math.min(buffer.length, content.length - pointer);
            System.arraycopy(content, (int) pointer, buffer, 0, count);
            pointer += count;
            return count;
        }

        @Override
        public void seek(final long position) throws IOException {
            if (closed || position < 0) {
                throw new IOException("invalid bridge position " + position);
            }
            pointer = position;
        }
    }

    /**
     * Simulates two rotations to empty files without any write in between (IO-591), followed by a write to the second replacement.
     */
    private static final class DoubleRotationTailable implements Tailer.Tailable {

        private final Bridge firstBridge = new Bridge("first\n");
        private final Bridge secondBridge = new Bridge("");
        private final List<Bridge> replacementBridges = new ArrayList<>();
        private final AtomicInteger openCalls = new AtomicInteger();
        private final AtomicInteger sizeCalls = new AtomicInteger();
        private final AtomicReference<Tailer> tailer = new AtomicReference<>();

        @Override
        public Tailer.RandomAccessResourceBridge getRandomAccess(final String mode) throws FileNotFoundException {
            final int attempt = openCalls.incrementAndGet();
            if (attempt == 1) {
                return firstBridge;
            }
            if (attempt == 2) {
                return secondBridge;
            }
            if (attempt > 10) {
                throw new AssertionError("open budget exceeded");
            }
            final Bridge bridge = new Bridge("after\n");
            replacementBridges.add(bridge);
            return bridge;
        }

        @Override
        public boolean isNewer(final FileTime previous) {
            return false;
        }

        @Override
        public FileTime lastModifiedFileTime() {
            return FileTime.fromMillis(0);
        }

        @Override
        public long size() {
            final int call = sizeCalls.incrementAndGet();
            if (call == 1) {
                return 6; // "first\n" is available in the first file.
            }
            if (call == 2 || call == 3) {
                return 0; // The first rotation is visible, the second one (to another empty file) is not.
            }
            if (call > 30) {
                // Stop instead of failing here, so that a tailer stuck on a stale reader fails on the line assertion.
                tailer.get().close();
            }
            return 6; // "after\n" was written to the second replacement.
        }
    }

    /**
     * Simulates a tailable that never rotates: each open returns a new reader over the same content, and the size is fixed.
     */
    private static final class FixedTailable implements Tailer.Tailable {

        private final String content;
        private final long size;
        private final AtomicInteger openCalls = new AtomicInteger();
        private final AtomicInteger sizeCalls = new AtomicInteger();
        private final AtomicReference<Tailer> tailer = new AtomicReference<>();

        FixedTailable(final String content, final long size) {
            this.content = content;
            this.size = size;
        }

        @Override
        public Tailer.RandomAccessResourceBridge getRandomAccess(final String mode) throws FileNotFoundException {
            if (openCalls.incrementAndGet() > 30) {
                throw new AssertionError("open budget exceeded");
            }
            return new Bridge(content);
        }

        @Override
        public boolean isNewer(final FileTime previous) {
            return false;
        }

        @Override
        public FileTime lastModifiedFileTime() {
            return FileTime.fromMillis(0);
        }

        @Override
        public long size() {
            if (sizeCalls.incrementAndGet() > 10) {
                tailer.get().close();
            }
            return size;
        }
    }

    /**
     * Simulates one rotation with a configurable number of failed opens, followed by a replacement larger than the old read position.
     */
    private static final class SingleRotationTailable implements Tailer.Tailable {

        private final int failedOpens;
        private final Bridge newBridge = new Bridge("new\nmore\n");
        private final Bridge oldBridge = new Bridge("old\nlate\n");
        private final AtomicInteger openCalls = new AtomicInteger();
        private final AtomicInteger sizeCalls = new AtomicInteger();

        SingleRotationTailable(final int failedOpens) {
            this.failedOpens = failedOpens;
        }

        @Override
        public Tailer.RandomAccessResourceBridge getRandomAccess(final String mode) throws FileNotFoundException {
            final int attempt = openCalls.incrementAndGet();
            if (attempt == 1) {
                return oldBridge;
            }
            if (attempt <= failedOpens + 1) {
                throw new FileNotFoundException("replacement temporarily absent");
            }
            if (attempt == failedOpens + 2) {
                return newBridge;
            }
            throw new AssertionError("open budget exceeded");
        }

        @Override
        public boolean isNewer(final FileTime previous) {
            return false;
        }

        @Override
        public FileTime lastModifiedFileTime() {
            return FileTime.fromMillis(0);
        }

        @Override
        public long size() {
            final int call = sizeCalls.incrementAndGet();
            if (call == 1) {
                return 4; // "old\n" was already consumed through the old reader.
            }
            if (call == 2) {
                return 0; // Detect one rotation before the replacement grows.
            }
            if (call > 30) {
                throw new AssertionError("loop budget exceeded");
            }
            return 9; // replacement is present and never shrinks again.
        }
    }

    private static void runFixedTailable(final FixedTailable tailable, final List<String> lines, final AtomicInteger rotations) {
        final List<Exception> errors = new ArrayList<>();
        final TailerListener listener = new TailerListenerAdapter() {

            @Override
            public void fileRotated() {
                rotations.incrementAndGet();
            }

            @Override
            public void handle(final Exception exception) {
                errors.add(exception);
            }

            @Override
            public void handle(final String line) {
                lines.add(line);
            }
        };
        try (Tailer tailer = Tailer.builder().setTailable(tailable).setTailerListener(listener).setCharset(StandardCharsets.UTF_8).setStartThread(false)
                .setDelayDuration(Duration.ZERO).get()) {
            tailable.tailer.set(tailer);
            assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);
            assertFalse(tailer.getRun());
        }
        assertEquals(new ArrayList<>(), errors);
    }

    @Test
    void testCloseFailureListenerThrows() throws InterruptedException {
        final SingleRotationTailable tailable = new SingleRotationTailable(0);
        final IOException readFailure = new IOException("read failed");
        final IOException closeFailure = new IOException("close failed");
        final IllegalStateException listenerFailure = new IllegalStateException("listener failed");
        tailable.newBridge.readException = readFailure;
        tailable.newBridge.closeException = closeFailure;
        final List<Exception> errors = new ArrayList<>();
        final TailerListener listener = new TailerListenerAdapter() {

            @Override
            public void handle(final Exception exception) {
                errors.add(exception);
                if (exception == closeFailure) {
                    throw listenerFailure;
                }
            }
        };
        try (Tailer tailer = Tailer.builder().setTailable(tailable).setTailerListener(listener).setCharset(StandardCharsets.UTF_8).setTailFromEnd(true)
                .setStartThread(false).setDelayDuration(Duration.ZERO).get()) {
            assertSame(listenerFailure, assertThrows(IllegalStateException.class, tailer::run));
            // Check before try-with-resources closes the Tailer, so automatic cleanup cannot hide a running flag left set by run().
            assertFalse(tailer.getRun());
            assertTrue(tailer.awaitTermination(Duration.ZERO));
        }
        assertEquals(Arrays.asList(readFailure, closeFailure), errors);
        assertEquals(1, tailable.oldBridge.closeCalls);
        assertEquals(1, tailable.newBridge.closeCalls);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testClosesOldReaderWhenStoppedDuringRotationRetry(final boolean failClose) {
        final SingleRotationTailable tailable = new SingleRotationTailable(1);
        final IOException closeException = failClose ? new IOException("close failed during shutdown") : null;
        tailable.oldBridge.closeException = closeException;
        final AtomicReference<Exception> listenerException = new AtomicReference<>();
        final AtomicReference<Tailer> tailerReference = new AtomicReference<>();
        try (Tailer tailer = Tailer.builder().setCharset(StandardCharsets.UTF_8).setStartThread(false).setTailFromEnd(true)
                .setDelayDuration(Duration.ofMillis(1)).setTailable(tailable).setTailerListener(new TailerListenerAdapter() {

                    @Override
                    public void fileNotFound() {
                        assertFalse(tailable.oldBridge.closed);
                        tailerReference.get().close();
                    }

                    @Override
                    public void handle(final Exception exception) {
                        listenerException.compareAndSet(null, exception);
                    }
                }).get()) {
            tailerReference.set(tailer);
            assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);
            assertEquals(2, tailable.openCalls.get());
            assertEquals(closeException, listenerException.get());
            assertTrue(tailable.oldBridge.closed);
            assertFalse(tailable.newBridge.closed);
            assertEquals(1, tailable.oldBridge.closeCalls);
            assertEquals(0, tailable.newBridge.closeCalls);
            assertFalse(tailer.getRun());
        }
    }

    @Test
    void testInterruptDuringRotationRetry() {
        final SingleRotationTailable tailable = new SingleRotationTailable(1);
        final List<Exception> errors = new ArrayList<>();
        final AtomicBoolean interrupted = new AtomicBoolean();
        final TailerListener listener = new TailerListenerAdapter() {

            @Override
            public void fileNotFound() {
                assertFalse(tailable.oldBridge.closed);
                Thread.currentThread().interrupt();
            }

            @Override
            public void handle(final Exception exception) {
                errors.add(exception);
            }
        };
        try (Tailer tailer = Tailer.builder().setTailable(tailable).setTailerListener(listener).setCharset(StandardCharsets.UTF_8).setTailFromEnd(true)
                .setStartThread(false).setDelayDuration(Duration.ofMillis(1)).get()) {
            assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
                tailer.run();
                interrupted.set(Thread.interrupted());
            });
            assertFalse(tailer.getRun());
        }
        assertTrue(interrupted.get());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0) instanceof InterruptedException);
        assertEquals(1, tailable.oldBridge.closeCalls);
        assertEquals(0, tailable.newBridge.closeCalls);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testOldReaderFailureClosesBothReaders(final boolean failClose) {
        final SingleRotationTailable tailable = new SingleRotationTailable(0);
        final IOException failure = new IOException("old reader failure");
        if (failClose) {
            tailable.oldBridge.closeException = failure;
        } else {
            tailable.oldBridge.readException = failure;
        }
        final List<String> lines = new ArrayList<>();
        final List<Exception> errors = new ArrayList<>();
        final TailerListener listener = new TailerListenerAdapter() {

            private Tailer tailer;

            @Override
            public void handle(final Exception exception) {
                errors.add(exception);
            }

            @Override
            public void handle(final String line) {
                lines.add(line);
                if ("more".equals(line)) {
                    tailer.close();
                }
            }

            @Override
            public void init(final Tailer tailer) {
                this.tailer = tailer;
            }
        };
        try (Tailer tailer = Tailer.builder().setTailable(tailable).setTailerListener(listener).setCharset(StandardCharsets.UTF_8).setTailFromEnd(true)
                .setStartThread(false).setDelayDuration(Duration.ZERO).get()) {
            assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);
            assertFalse(tailer.getRun());
        }
        assertEquals(Arrays.asList(failure), errors);
        assertEquals(failClose ? Arrays.asList("late") : Arrays.asList("new", "more"), lines);
        assertEquals(1, tailable.oldBridge.closeCalls);
        assertEquals(1, tailable.newBridge.closeCalls);
    }

    /**
     * Tests that a trailing partial line, which the reader re-reads on each pass, is not mistaken for a rotation.
     */
    @Test
    void testPartialLineIsNotRotation() {
        final FixedTailable tailable = new FixedTailable("first\npartial", 13);
        final List<String> lines = new ArrayList<>();
        final AtomicInteger rotations = new AtomicInteger();
        runFixedTailable(tailable, lines, rotations);
        assertEquals(Arrays.asList("first"), lines);
        assertEquals(0, rotations.get());
        assertEquals(1, tailable.openCalls.get());
    }

    @ParameterizedTest
    @CsvSource({ "0,false,1", "1,false,1", "2,false,1", "0,true,1", "1,true,1", "2,true,1", "0,false,4096", "1,false,4096", "2,false,4096", "0,true,4096",
            "1,true,4096", "2,true,4096" })
    void testResumesRotationWithoutAnotherPathShrink(final int failedOpens, final boolean reOpen, final int bufferSize) {
        final SingleRotationTailable tailable = new SingleRotationTailable(failedOpens);
        final List<String> lines = new ArrayList<>();
        final AtomicInteger missingFiles = new AtomicInteger();
        final AtomicInteger rotations = new AtomicInteger();
        final AtomicReference<Exception> listenerException = new AtomicReference<>();
        final AtomicReference<Tailer> tailerReference = new AtomicReference<>();
        try (Tailer tailer = Tailer.builder().setCharset(StandardCharsets.UTF_8).setStartThread(false).setReOpen(reOpen).setBufferSize(bufferSize)
                .setTailFromEnd(true).setDelayDuration(Duration.ofMillis(1)).setTailable(tailable).setTailerListener(new TailerListenerAdapter() {

                    @Override
                    public void fileNotFound() {
                        missingFiles.incrementAndGet();
                    }

                    @Override
                    public void fileRotated() {
                        rotations.incrementAndGet();
                    }

                    @Override
                    public void handle(final Exception exception) {
                        listenerException.compareAndSet(null, exception);
                    }

                    @Override
                    public void handle(final String line) {
                        lines.add(line);
                        if ("more".equals(line)) {
                            tailerReference.get().close();
                        }
                    }
                }).get()) {
            tailerReference.set(tailer);
            assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);
            assertEquals(Arrays.asList("late", "new", "more"), lines);
            assertEquals(failedOpens, missingFiles.get());
            assertEquals(1, rotations.get());
            assertEquals(failedOpens + 2, tailable.openCalls.get());
            assertNull(listenerException.get());
            assertTrue(tailable.oldBridge.closed);
            assertTrue(tailable.newBridge.closed);
            assertEquals(1, tailable.oldBridge.closeCalls);
            assertEquals(1, tailable.newBridge.closeCalls);
            assertFalse(tailer.getRun());
        }
    }

    /**
     * Tests that lines written after two rotations to empty files are not lost (IO-591).
     */
    @Test
    void testRotationToEmptyFileTwiceWithoutWrites() {
        final DoubleRotationTailable tailable = new DoubleRotationTailable();
        final List<String> lines = new ArrayList<>();
        final List<Exception> errors = new ArrayList<>();
        final AtomicInteger rotations = new AtomicInteger();
        final TailerListener listener = new TailerListenerAdapter() {

            @Override
            public void fileRotated() {
                rotations.incrementAndGet();
            }

            @Override
            public void handle(final Exception exception) {
                errors.add(exception);
            }

            @Override
            public void handle(final String line) {
                lines.add(line);
                if ("after".equals(line)) {
                    tailable.tailer.get().close();
                }
            }
        };
        try (Tailer tailer = Tailer.builder().setTailable(tailable).setTailerListener(listener).setCharset(StandardCharsets.UTF_8).setStartThread(false)
                .setDelayDuration(Duration.ZERO).get()) {
            tailable.tailer.set(tailer);
            assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);
            assertFalse(tailer.getRun());
        }
        assertEquals(Arrays.asList("first", "after"), lines);
        assertEquals(new ArrayList<>(), errors);
        assertEquals(2, rotations.get());
        assertEquals(1, tailable.firstBridge.closeCalls);
        assertEquals(1, tailable.secondBridge.closeCalls);
        // One reader checks the replacement, and one reads it.
        assertEquals(2, tailable.replacementBridges.size());
        for (final Bridge bridge : tailable.replacementBridges) {
            assertEquals(1, bridge.closeCalls);
        }
    }

    /**
     * Tests that a size ahead of the readable data, for example on a caching network file system, is not mistaken for a rotation.
     */
    @Test
    void testSizeAheadOfDataIsNotRotation() {
        final FixedTailable tailable = new FixedTailable("", 1);
        final List<String> lines = new ArrayList<>();
        final AtomicInteger rotations = new AtomicInteger();
        runFixedTailable(tailable, lines, rotations);
        assertEquals(new ArrayList<>(), lines);
        assertEquals(0, rotations.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testStopWhileDrainingOldReader(final boolean listenerThrows) {
        final SingleRotationTailable tailable = new SingleRotationTailable(0);
        final RuntimeException failure = new IllegalStateException("listener failure");
        final List<String> lines = new ArrayList<>();
        final List<Exception> errors = new ArrayList<>();
        final TailerListener listener = new TailerListenerAdapter() {

            private Tailer tailer;

            @Override
            public void handle(final Exception exception) {
                errors.add(exception);
            }

            @Override
            public void handle(final String line) {
                lines.add(line);
                if (listenerThrows) {
                    throw failure;
                }
                tailer.close();
            }

            @Override
            public void init(final Tailer tailer) {
                this.tailer = tailer;
            }
        };
        try (Tailer tailer = Tailer.builder().setTailable(tailable).setTailerListener(listener).setCharset(StandardCharsets.UTF_8).setTailFromEnd(true)
                .setStartThread(false).setDelayDuration(Duration.ZERO).get()) {
            assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);
        }
        assertEquals(Arrays.asList("late"), lines);
        assertEquals(listenerThrows ? Arrays.asList(failure) : new ArrayList<>(), errors);
        assertEquals(1, tailable.oldBridge.closeCalls);
        assertEquals(1, tailable.newBridge.closeCalls);
    }
}
