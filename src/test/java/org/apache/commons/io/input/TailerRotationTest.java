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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Regression coverage for temporarily missing replacement files during rotation.
 */
class TailerRotationTest {

    private static final class Bridge implements Tailer.RandomAccessResourceBridge {

        private int closeCalls;
        private boolean closed;
        private final byte[] content;
        private long pointer;

        Bridge(final String content) {
            this.content = content.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void close() {
            closed = true;
            closeCalls++;
        }

        @Override
        public long getPointer() {
            return pointer;
        }

        @Override
        public int read(final byte[] buffer) throws IOException {
            if (closed) {
                throw new IOException("read attempted on closed bridge");
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
            if (position < 0 || position > content.length) {
                throw new IOException("invalid bridge position " + position);
            }
            pointer = position;
        }
    }

    /**
     * Simulates one rotation: the path is absent for one open attempt, then the
     * replacement stays larger than the position read from the old inode.
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

    @Test
    void closesOldReaderWhenStoppedDuringRotationRetry() {
        final SingleRotationTailable tailable = new SingleRotationTailable(1);
        final AtomicReference<Exception> listenerException = new AtomicReference<>();
        final AtomicReference<Tailer> tailerReference = new AtomicReference<>();
        final Tailer tailer = Tailer.builder()
                .setStartThread(false)
                .setTailFromEnd(true)
                .setDelayDuration(Duration.ofMillis(1))
                .setTailable(tailable)
                .setTailerListener(new TailerListenerAdapter() {
                    @Override
                    public void fileNotFound() {
                        assertFalse(tailable.oldBridge.closed);
                        tailerReference.get().close();
                    }

                    @Override
                    public void handle(final Exception exception) {
                        listenerException.compareAndSet(null, exception);
                    }

                })
                .get();
        tailerReference.set(tailer);

        assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);

        assertEquals(2, tailable.openCalls.get());
        assertNull(listenerException.get());
        assertTrue(tailable.oldBridge.closed);
        assertFalse(tailable.newBridge.closed);
        assertEquals(1, tailable.oldBridge.closeCalls);
        assertEquals(0, tailable.newBridge.closeCalls);
        assertFalse(tailer.getRun());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void resumesRotationWithoutAnotherPathShrink(final int failedOpens) {
        final SingleRotationTailable tailable = new SingleRotationTailable(failedOpens);
        final List<String> lines = new ArrayList<>();
        final AtomicInteger missingFiles = new AtomicInteger();
        final AtomicInteger rotations = new AtomicInteger();
        final AtomicReference<Exception> listenerException = new AtomicReference<>();
        final AtomicReference<Tailer> tailerReference = new AtomicReference<>();
        final Tailer tailer = Tailer.builder()
                .setStartThread(false)
                .setTailFromEnd(true)
                .setDelayDuration(Duration.ofMillis(1))
                .setTailable(tailable)
                .setTailerListener(new TailerListenerAdapter() {
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
                })
                .get();
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
