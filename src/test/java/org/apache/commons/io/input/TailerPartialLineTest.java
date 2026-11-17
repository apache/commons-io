/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.io.StandardLineSeparator;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Timeout(10)
class TailerPartialLineTest {

    private static void assertLinesAcrossAppends(final Path directory, final int bufferSize, final boolean reOpen,
            final byte[][] chunks, final String[][] expectedLines) throws IOException {
        final Path file = directory.resolve("tailer.txt");
        Files.write(file, chunks[0]);
        final List<String> lines = new ArrayList<>();
        final AtomicInteger eofCount = new AtomicInteger();
        final TailerListenerAdapter listener = new TailerListenerAdapter() {
            private Tailer tailer;

            @Override
            public void endOfFileReached() {
                final int poll = eofCount.getAndIncrement();
                assertEquals(Arrays.asList(expectedLines[poll]), lines, "Lines after poll " + (poll + 1));
                if (poll + 1 == chunks.length) {
                    tailer.close();
                    return;
                }
                try {
                    Files.write(file, chunks[poll + 1], StandardOpenOption.APPEND);
                } catch (final IOException e) {
                    fail(e);
                }
            }

            @Override
            public void fileNotFound() {
                fail("File unexpectedly missing: " + file);
            }

            @Override
            public void handle(final Exception e) {
                fail(e);
            }

            @Override
            public void handle(final String line) {
                lines.add(line);
            }

            @Override
            public void init(final Tailer tailer) {
                this.tailer = tailer;
            }
        };
        try (Tailer tailer = Tailer.builder().setPath(file).setCharset(StandardCharsets.UTF_8).setBufferSize(bufferSize)
                .setTailerListener(listener).setStartThread(false).setDelayDuration(Duration.ZERO).setReOpen(reOpen).get()) {
            tailer.run();
        }
        assertEquals(chunks.length, eofCount.get(), "All scripted polls must complete");
        assertEquals(Arrays.asList(expectedLines[expectedLines.length - 1]), lines);
    }

    private static void assertLinesAcrossAppends(final Path directory, final int bufferSize, final boolean reOpen,
            final String[] chunks, final String[][] expectedLines) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen,
                Arrays.stream(chunks).map(chunk -> chunk.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new), expectedLines);
    }

    static Stream<Arguments> buffersAndReOpen() {
        final Stream.Builder<Arguments> arguments = Stream.builder();
        for (final int bufferSize : new int[] {1, 2, 3, 16}) {
            for (final boolean reOpen : new boolean[] {false, true}) {
                arguments.add(Arguments.of(bufferSize, reOpen));
            }
        }
        return arguments.build();
    }

    static Stream<Arguments> lineEndingsAndBuffers() {
        final Stream.Builder<Arguments> arguments = Stream.builder();
        for (final StandardLineSeparator separator : StandardLineSeparator.values()) {
            for (final int bufferSize : new int[] {1, 2, 3, 16}) {
                for (final boolean reOpen : new boolean[] {false, true}) {
                    arguments.add(Arguments.of(separator, bufferSize, reOpen));
                }
            }
        }
        return arguments.build();
    }

    @ParameterizedTest(name = "separator={0}, bufferSize={1}, reOpen={2}")
    @MethodSource("lineEndingsAndBuffers")
    void preservesPartialLineAcrossAppends(final StandardLineSeparator separator, final int bufferSize, final boolean reOpen,
            @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen,
                new String[] {"first" + separator.getString() + "partial", " middle", " end\n"},
                new String[][] {{"first"}, {"first"}, {"first", "partial middle end"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testCompleteLineBeforePartialLine(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new String[] {"a\rb\nc", "d\n"},
                new String[][] {{"a", "b"}, {"a", "b", "cd"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testConsecutiveCarriageReturns(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new String[] {"a\r\rb", "c\n"},
                new String[][] {{"a\r"}, {"a\r", "bc"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testEmptyLineBeforePartialLine(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new String[] {"\rb", "\n"}, new String[][] {{""}, {"", "b"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testPartialLineAfterCarriageReturnAtEndOfFile(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new String[] {"a\r", "b", "c\n"}, new String[][] {{}, {"a"}, {"a", "bc"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testPartialLineAfterCrLfSplitAcrossAppends(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new String[] {"a\r", "\nb", "c\n"}, new String[][] {{}, {"a"}, {"a", "bc"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testPartialLineEndingWithCarriageReturn(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new String[] {"a\rb", "c\rd", "e\n"},
                new String[][] {{"a"}, {"a", "bc"}, {"a", "bc", "de"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testPartialLineStartingWithUtf8Character(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new String[] {"a\r\u00e9", "x\n"}, new String[][] {{"a"}, {"a", "\u00e9x"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testPartialLineWithUtf8CharacterSplitAcrossAppends(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        final byte[] firstChunk = {'a', '\r', (byte) 0xc3};
        final byte[] secondChunk = {(byte) 0xa9, '\n'};
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new byte[][] {firstChunk, secondChunk}, new String[][] {{"a"}, {"a", "\u00e9"}});
    }

    @ParameterizedTest(name = "bufferSize={0}, reOpen={1}")
    @MethodSource("buffersAndReOpen")
    void testSingleBytePartialLine(final int bufferSize, final boolean reOpen, @TempDir final Path directory) throws IOException {
        assertLinesAcrossAppends(directory, bufferSize, reOpen, new String[] {"a\rb", "c\n"}, new String[][] {{"a"}, {"a", "bc"}});
    }
}
