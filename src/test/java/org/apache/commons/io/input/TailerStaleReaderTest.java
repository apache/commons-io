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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks stale-reader recovery and delayed visibility of appended data (IO-591).
 *
 * <p>
 * These tests use an in-memory tailable so that changes happen at known points in Tailer's execution, without relying on file system timing. A
 * {@link Resource} holds readable bytes, a {@link Bridge} represents an open reader with its own position, and {@link ScriptedTailable} supplies the
 * current size and new readers. {@link Result} records the lines, errors, and notifications delivered to the listener.
 * </p>
 *
 * <p>
 * Changing a resource's content makes the new bytes visible to every reader sharing that resource. Assigning a different resource to the tailable affects
 * its size and future readers, but existing readers retain their old resource. This can model either a rotated file or different cached views of the same
 * file. A different resource object therefore does not, by itself, mean that the test expects a rotation notification.
 * </p>
 *
 * <p>
 * Calls to {@link ScriptedTailable#size()} advance the scenario. Callbacks on size checks, opens, reads, and closes make writes or failures happen in a
 * repeatable order. Modification times stay fixed to keep timestamp-based replay out of the scenarios. The run helper disables automatic thread creation
 * and polling delays, invokes {@link Tailer#run()} under a timeout, and checks termination and one close attempt per opened reader, even when closing fails.
 * </p>
 *
 * <p>
 * The main assertions distinguish recovery from replay: a reader left on an empty rotated file must resume delivering lines, while a reader with a stale
 * view of an appended file must resume at its existing position without delivering earlier lines again.
 * </p>
 */
class TailerStaleReaderTest {

    /**
     * Models an open reader, including its position, visible content, and optional failures or callbacks.
     *
     * <p>
     * The read callbacks are cleared before invocation so each installed callback runs once; a callback may install another for a later read. In particular,
     * {@code onEof} runs after the EOF decision: that read still returns {@code -1} even if the callback makes more bytes visible. This models an append
     * becoming visible between an EOF result and the caller's next operation.
     * </p>
     *
     * <p>
     * Reads, seeks, and position lookups after a close attempt fail an assertion. Close attempts are counted before any configured exception is thrown,
     * allowing the run helper to check cleanup even when closing reports an error.
     * </p>
     */
    private static final class Bridge implements Tailer.RandomAccessResourceBridge {

        private Resource resource;
        private int pointer;
        private int closeCalls;
        private IOException readFailure;
        private IOException seekFailure;
        private IOException closeFailure;
        private Runnable onEof;
        private Runnable beforeRead;
        private Runnable onClose;

        Bridge(final Resource resource) {
            this.resource = resource;
        }

        @Override
        public void close() throws IOException {
            closeCalls++;
            if (onClose != null) {
                onClose.run();
            }
            if (closeFailure != null) {
                throw closeFailure;
            }
        }

        @Override
        public long getPointer() {
            assertEquals(0, closeCalls, "getPointer on a closed reader");
            return pointer;
        }

        @Override
        public int read(final byte[] buffer) throws IOException {
            assertEquals(0, closeCalls, "read on a closed reader");
            if (readFailure != null) {
                throw readFailure;
            }
            if (beforeRead != null) {
                final Runnable action = beforeRead;
                beforeRead = null;
                action.run();
            }
            if (pointer >= resource.content.length) {
                if (onEof != null) {
                    final Runnable action = onEof;
                    onEof = null;
                    action.run();
                }
                return -1;
            }
            final int count = Math.min(buffer.length, resource.content.length - pointer);
            System.arraycopy(resource.content, pointer, buffer, 0, count);
            pointer += count;
            return count;
        }

        @Override
        public void seek(final long position) throws IOException {
            assertEquals(0, closeCalls, "seek on a closed reader");
            if (seekFailure != null) {
                throw seekFailure;
            }
            pointer = Math.toIntExact(position);
        }
    }

    /**
     * Selects the operation on a newly opened reader that will throw an exception.
     */
    private enum RefreshOperation {
        SEEK, READ, CLOSE
    }

    /**
     * Holds one mutable view of UTF-8 content, which may be shared by several readers.
     */
    private static final class Resource {

        private byte[] content;

        Resource(final String content) {
            setContent(content);
        }

        void setContent(final String content) {
            this.content = content.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Collects listener events in delivery order and counts rotation and missing-file notifications.
     */
    private static final class Result extends TailerListenerAdapter {

        private final List<String> lines = new ArrayList<>();
        private final List<Exception> errors = new ArrayList<>();
        private int rotations;
        private int missingFiles;

        @Override
        public void fileNotFound() {
            missingFiles++;
        }

        @Override
        public void fileRotated() {
            rotations++;
        }

        @Override
        public void handle(final Exception exception) {
            errors.add(exception);
        }

        @Override
        public void handle(final String line) {
            lines.add(line);
        }
    }

    /**
     * Supplies the current resource and advances a scenario through callbacks from Tailer's own operations.
     *
     * <p>
     * {@code onPoll} runs on each size lookup, including the initial lookup when tailing from the end. {@code reportedSize} can make metadata advertise more
     * bytes than readers can see. {@code onOpen} runs after a successful open and can configure the new reader; the open counter also includes failed attempts.
     * </p>
     *
     * <p>
     * After twelve size lookups, the next lookup requests shutdown so a tailer that never recovers fails the line assertions rather than running forever.
     * A separate open limit catches repeated opens that do not return to size checks. The run helper's timeout is the final guard against a stuck test.
     * </p>
     */
    private static final class ScriptedTailable implements Tailer.Tailable {

        private final List<Bridge> readers = new ArrayList<>();
        private Resource resource = new Resource("first\n");
        private Tailer tailer;
        private IntConsumer onPoll;
        private Consumer<Bridge> onOpen;
        private int polls;
        private int opens;
        private int failedOpen;
        private int failedOpensUntil;
        private long reportedSize = -1;

        @Override
        public Tailer.RandomAccessResourceBridge getRandomAccess(final String mode) throws FileNotFoundException {
            assertTrue(++opens < 50, "open budget exceeded");
            if (opens == failedOpen || opens > 1 && opens <= failedOpensUntil) {
                throw new FileNotFoundException("temporarily inaccessible");
            }
            final Bridge bridge = new Bridge(resource);
            readers.add(bridge);
            if (onOpen != null) {
                onOpen.accept(bridge);
            }
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
            if (++polls > 12) {
                tailer.close();
            }
            if (onPoll != null) {
                onPoll.accept(polls);
            }
            return reportedSize < 0 ? resource.content.length : reportedSize;
        }
    }

    /**
     * Selects when the shared view of an append becomes readable in
     * {@link TailerStaleReaderTest#testDelayedVisibilityDoesNotReplay(Visibility)}.
     *
     * <p>
     * {@code EOF}, {@code OPEN}, and {@code READ} publish the append during the original reader's EOF result, the next open, or the new reader's first read.
     * {@code CLOSE} and {@code SECOND_EOF} give the new reader the appended bytes immediately and schedule an update of the original view on close or another
     * EOF result. These last cases expose why checking the original reader again cannot reliably establish whether the file was replaced.
     * </p>
     */
    private enum Visibility {
        EOF, OPEN, READ, CLOSE, SECOND_EOF
    }

    /**
     * Runs a scenario from the beginning with a 4096-byte buffer and without periodic reopening or touch suppression.
     *
     * @param tailable the scenario to run.
     * @return the recorded listener events.
     * @throws InterruptedException if waiting for termination is interrupted.
     */
    private static Result run(final ScriptedTailable tailable) throws InterruptedException {
        return run(tailable, new Result(), false, false, false, 4096);
    }

    /**
     * Runs Tailer's loop with the supplied settings and checks shutdown and reader cleanup before returning its listener events.
     *
     * <p>
     * The builder does not start a background thread. The timeout executes {@link Tailer#run()} with a two-second limit; all scenario callbacks execute as
     * part of that loop. The scripted size limit normally stops the loop. Termination is checked before try-with-resources closes the tailer, so automatic
     * cleanup cannot hide a failure to stop. Every successfully opened reader must receive exactly one close attempt.
     * </p>
     *
     * @param tailable the scenario to run.
     * @param result the listener that records the outcome.
     * @param tailFromEnd whether to skip the initial content.
     * @param reOpen whether to reopen the reader between polls.
     * @param ignoreTouch whether to suppress replay caused by modification-time changes.
     * @param bufferSize the read buffer size in bytes.
     * @return the supplied listener with the recorded events.
     * @throws InterruptedException if waiting for termination is interrupted.
     */
    private static Result run(final ScriptedTailable tailable, final Result result, final boolean tailFromEnd, final boolean reOpen,
            final boolean ignoreTouch, final int bufferSize) throws InterruptedException {
        try (Tailer tailer = Tailer.builder().setTailable(tailable).setTailerListener(result).setCharset(StandardCharsets.UTF_8).setStartThread(false)
                .setTailFromEnd(tailFromEnd).setReOpen(reOpen).setIgnoreTouch(ignoreTouch).setBufferSize(bufferSize).setDelayDuration(Duration.ZERO).get()) {
            tailable.tailer = tailer;
            assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);
            assertFalse(tailer.getRun());
            assertTrue(tailer.awaitTermination(Duration.ZERO));
        }
        for (final Bridge reader : tailable.readers) {
            assertEquals(1, reader.closeCalls, "Each opened reader must be closed exactly once");
        }
        return result;
    }

    /**
     * Creates a tailable whose original reader stays at EOF after the first complete line, while new readers can see an append.
     *
     * <p>
     * The first size lookup exposes {@code "first\n"}. The second switches the tailable's view to {@code "first\nafter\n"}, leaving the original reader
     * on the six-byte view. Recovery must seek a new reader to byte six and deliver only {@code "after"}; starting at zero would duplicate {@code "first"}.
     * </p>
     *
     * @return the configured tailable.
     */
    private static ScriptedTailable staleAtNonzeroPosition() {
        final ScriptedTailable tailable = new ScriptedTailable();
        tailable.onPoll = poll -> {
            if (poll == 2) {
                tailable.resource = new Resource("first\nafter\n");
            }
        };
        return tailable;
    }

    /**
     * Checks recovery when the original reader's view never catches up with the append visible to a fresh reader.
     *
     * <p>
     * Unlike the delayed-visibility cases, there is no later update to the original resource. Recovery must use the fresh reader at the saved offset;
     * waiting on the original reader loses the appended line, and resetting the fresh reader to zero duplicates the first line.
     * </p>
     *
     * @throws Exception if the scenario cannot complete.
     */
    @Test
    void testAppendVisibleOnlyToFreshHandleDoesNotReplay() throws Exception {
        // The original handle retains a stale view of the same file even after a new handle sees the append.
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        final Result result = run(tailable);
        assertEquals(Arrays.asList("first", "after"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(0, result.rotations);
    }

    /**
     * Checks that an append becoming visible during recovery does not cause earlier lines to be replayed.
     *
     * <p>
     * Tailer first consumes {@code "first\n"}, leaving its position at byte six. The second size lookup reports twelve bytes before the original reader can
     * see the append. The selected callback controls when {@code "after\n"} becomes visible. A new reader may see it while the original reader still returns
     * EOF, including during a second EOF check. None of these differences proves rotation, so the expected output is exactly {@code [first, after]} with no
     * rotation notification.
     * </p>
     *
     * <p>
     * The third size lookup also publishes the append. This lets an implementation that never refreshes recover on its next poll, so this test specifically
     * catches replay rather than requiring a particular reopening strategy. A one-byte buffer also checks that probing does not consume a byte of the line.
     * </p>
     *
     * @param visibility the point at which the append becomes visible.
     * @throws Exception if the scenario cannot complete.
     */
    @ParameterizedTest
    @EnumSource(Visibility.class)
    void testDelayedVisibilityDoesNotReplay(final Visibility visibility) throws Exception {
        final ScriptedTailable tailable = new ScriptedTailable();
        final Resource shared = tailable.resource;
        final Runnable publish = () -> shared.setContent("first\nafter\n");
        tailable.onPoll = poll -> {
            if (poll == 2) {
                tailable.reportedSize = 12;
                if (visibility == Visibility.EOF) {
                    tailable.readers.get(0).onEof = publish;
                } else if (visibility == Visibility.SECOND_EOF) {
                    // The append becomes visible only after another EOF from the original handle.
                    tailable.readers.get(0).onEof = () -> tailable.readers.get(0).onEof = publish;
                }
            } else if (poll == 3) {
                // The original implementation also sees the append on its next poll, without refreshing the reader.
                publish.run();
            }
        };
        tailable.onOpen = reader -> {
            if (tailable.opens == 2) {
                if (visibility == Visibility.OPEN) {
                    publish.run();
                } else if (visibility == Visibility.READ) {
                    reader.beforeRead = publish;
                } else if (visibility == Visibility.SECOND_EOF) {
                    reader.resource = new Resource("first\nafter\n");
                } else if (visibility == Visibility.CLOSE) {
                    // A new handle can see the append before the original handle's cached view is refreshed.
                    reader.resource = new Resource("first\nafter\n");
                    reader.onClose = publish;
                }
            }
        };
        final Result result = run(tailable, new Result(), false, false, true, 1);
        assertEquals(Arrays.asList("first", "after"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(0, result.rotations);
    }

    /**
     * Checks recovery after two replacements with empty files and no write between them.
     *
     * <p>
     * Size lookups two and three each install a new empty resource. The first replacement shrinks the length below the old position and causes one rotation
     * notification. The second replacement leaves both length and position at zero, so length alone cannot reveal the change. Lookup four writes
     * {@code "after\n"} to the latest resource. A reader still attached to the earlier empty resource must refresh and deliver that line without reporting
     * another rotation merely because the readers see different data.
     * </p>
     *
     * <p>
     * The cases combine startup position, periodic reopening, touch suppression, and small or large buffers. When starting from the end, the initial size
     * lookup consumes step one and {@code "first"} is deliberately omitted from the expected lines.
     * </p>
     *
     * @param tailFromEnd whether to skip the initial line.
     * @param reOpen whether to reopen between polls.
     * @param ignoreTouch whether to suppress replay caused by modification-time changes.
     * @param bufferSize the read buffer size in bytes.
     * @throws Exception if the scenario cannot complete.
     */
    @ParameterizedTest
    @CsvSource({ "false,false,false,1", "false,false,true,1", "false,true,false,1", "false,true,true,1",
        "true,false,false,1", "true,false,true,1", "true,true,false,1", "true,true,true,1",
        "false,false,false,4096", "false,false,true,4096", "false,true,false,4096", "false,true,true,4096",
        "true,false,false,4096", "true,false,true,4096", "true,true,false,4096", "true,true,true,4096" })
    void testDoubleEmptyRotation(final boolean tailFromEnd, final boolean reOpen, final boolean ignoreTouch, final int bufferSize) throws Exception {
        final ScriptedTailable tailable = new ScriptedTailable();
        tailable.onPoll = poll -> {
            if (poll == 2 || poll == 3) {
                tailable.resource = new Resource("");
            } else if (poll == 4) {
                tailable.resource.setContent("after\n");
            }
        };
        final Result result = run(tailable, new Result(), tailFromEnd, reOpen, ignoreTouch, bufferSize);
        assertEquals(tailFromEnd ? Arrays.asList("after") : Arrays.asList("first", "after"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(1, result.rotations);
    }

    /**
     * Checks that a newly opened reader is still closed if retiring the original reader fails before the new reader can deliver any lines.
     *
     * @param fileNotFound whether the close failure is a FileNotFoundException, which must not be mistaken for a failed open.
     * @throws Exception if the scenario cannot complete.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testOriginalCloseFailureClosesFreshReader(final boolean fileNotFound) throws Exception {
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        final IOException failure = fileNotFound ? new FileNotFoundException("close failed") : new IOException("close failed");
        tailable.onOpen = reader -> {
            if (tailable.opens == 2) {
                tailable.readers.get(0).closeFailure = failure;
            }
        };
        final Result result = run(tailable);
        assertEquals(Arrays.asList("first"), result.lines);
        assertEquals(Arrays.asList(failure), result.errors);
        assertEquals(0, result.rotations);
    }

    /**
     * Checks that data on the original reader remains usable even when every refresh attempt fails.
     *
     * <p>
     * The reported size advances on lookup two, but opening another reader always fails within the test's poll limit. Lookup four appends to the resource
     * already held by the original reader. That reader must deliver the new line, and it must remain the only successfully opened reader.
     * </p>
     *
     * @throws Exception if the scenario cannot complete.
     */
    @Test
    void testPermanentRefreshOpenFailureRetainsOriginalReader() throws Exception {
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        tailable.failedOpensUntil = 40;
        // A failed refresh must still allow subsequent data on the existing handle to be delivered.
        tailable.onPoll = poll -> {
            if (poll == 2) {
                tailable.reportedSize = 12;
            } else if (poll == 4) {
                tailable.resource.setContent("first\nafter\n");
            }
        };
        final Result result = run(tailable);
        assertEquals(Arrays.asList("first", "after"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(0, result.rotations);
        assertEquals(1, tailable.readers.size());
    }

    /**
     * Checks failures on the fresh reader after it has opened successfully.
     *
     * <p>
     * Seek and read failures prevent the appended line from being delivered. A close failure occurs during shutdown, after that line has been delivered.
     * Both ordinary I/O errors and FileNotFoundException must reach the listener: only a failure to open the fresh reader may be treated as an optional
     * refresh that could not proceed. The shared run helper also checks that neither reader receives an extra close attempt.
     * </p>
     *
     * @param operation the operation on the fresh reader that fails.
     * @param fileNotFound whether to use FileNotFoundException instead of a plain IOException.
     * @throws Exception if the scenario cannot complete.
     */
    @ParameterizedTest
    @CsvSource({ "SEEK,false", "READ,false", "CLOSE,false", "SEEK,true", "READ,true", "CLOSE,true" })
    void testRefreshFailure(final RefreshOperation operation, final boolean fileNotFound) throws Exception {
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        final IOException failure = fileNotFound ? new FileNotFoundException("refresh failed") : new IOException("refresh failed");
        tailable.onOpen = reader -> {
            if (tailable.opens == 2) {
                switch (operation) {
                case SEEK:
                    reader.seekFailure = failure;
                    break;
                case READ:
                    reader.readFailure = failure;
                    break;
                case CLOSE:
                    reader.closeFailure = failure;
                    break;
                default:
                    throw new AssertionError(operation);
                }
            }
        };
        final Result result = run(tailable);
        assertEquals(operation == RefreshOperation.CLOSE ? Arrays.asList("first", "after") : Arrays.asList("first"), result.lines);
        assertEquals(Arrays.asList(failure), result.errors);
        assertEquals(0, result.rotations);
    }

    /**
     * Checks that a failed first refresh leaves the original reader available until the next poll can open a fresh reader at the saved offset.
     *
     * @throws Exception if the scenario cannot complete.
     */
    @Test
    void testRefreshOpenFailureIsRetried() throws Exception {
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        tailable.failedOpen = 2;
        final Result result = run(tailable);
        assertEquals(Arrays.asList("first", "after"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(0, result.rotations);
        assertEquals(0, result.missingFiles);
        assertEquals(3, tailable.opens);
    }

    /**
     * Checks that refreshing after the first six bytes resumes at byte six with either a one-byte buffer or a buffer large enough for both lines.
     *
     * @param bufferSize the read buffer size in bytes.
     * @throws Exception if the scenario cannot complete.
     */
    @ParameterizedTest
    @ValueSource(ints = { 1, 4096 })
    void testRefreshPreservesNonzeroPosition(final int bufferSize) throws Exception {
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        final Result result = run(tailable, new Result(), false, false, false, bufferSize);
        assertEquals(Arrays.asList("first", "after"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(0, result.rotations);
        assertEquals(2, tailable.opens);
    }

    /**
     * Checks that a fresh reader's read failure is reported first, followed by its close failure during cleanup.
     *
     * @throws Exception if the scenario cannot complete.
     */
    @Test
    void testRefreshReadAndCloseFailures() throws Exception {
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        final IOException readFailure = new IOException("read failed");
        final IOException closeFailure = new IOException("close failed");
        tailable.onOpen = reader -> {
            if (tailable.opens == 2) {
                reader.readFailure = readFailure;
                reader.closeFailure = closeFailure;
            }
        };
        final Result result = run(tailable);
        assertEquals(Arrays.asList(readFailure, closeFailure), result.errors);
        assertEquals(Arrays.asList("first"), result.lines);
    }

    /**
     * Checks that failed open attempts two through four preserve the original reader and offset until attempt five succeeds.
     *
     * @throws Exception if the scenario cannot complete.
     */
    @Test
    void testRepeatedRefreshOpenFailuresRetainOriginalReader() throws Exception {
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        tailable.failedOpensUntil = 4;
        final Result result = run(tailable);
        assertEquals(Arrays.asList("first", "after"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(0, result.rotations);
        assertEquals(5, tailable.opens);
    }

    /**
     * Checks that metadata advertising twelve bytes while all readers still see six does not cause a reset or rotation notification.
     *
     * <p>
     * Size lookup four makes the append visible to all readers sharing the resource. The earlier line must not be replayed while waiting for that update.
     * </p>
     *
     * @throws Exception if the scenario cannot complete.
     */
    @Test
    void testSizeAheadOfReadableDataAtNonzeroPosition() throws Exception {
        final ScriptedTailable tailable = new ScriptedTailable();
        tailable.onPoll = poll -> {
            if (poll == 2) {
                tailable.reportedSize = 12;
            } else if (poll == 4) {
                tailable.resource.setContent("first\nafter\n");
            }
        };
        final Result result = run(tailable);
        assertEquals(Arrays.asList("first", "after"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(0, result.rotations);
    }

    /**
     * Checks shutdown requested from the fresh reader's open callback, before that open returns to Tailer.
     *
     * <p>
     * Both readers must be closed, and the fresh reader must not deliver the appended line after the stop request.
     * </p>
     *
     * @throws Exception if the scenario cannot complete.
     */
    @Test
    void testStopDuringRefreshClosesReaders() throws Exception {
        final ScriptedTailable tailable = staleAtNonzeroPosition();
        tailable.onOpen = reader -> {
            if (tailable.opens == 2) {
                tailable.tailer.close();
            }
        };
        final Result result = run(tailable);
        assertEquals(Arrays.asList("first"), result.lines);
        assertEquals(Collections.emptyList(), result.errors);
        assertEquals(0, result.rotations);
        assertEquals(2, tailable.opens);
    }
}
