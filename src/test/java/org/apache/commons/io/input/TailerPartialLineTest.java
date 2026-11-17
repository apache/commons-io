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
import java.util.stream.Stream;

import org.apache.commons.io.StandardLineSeparator;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TailerPartialLineTest {

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
        final Path file = directory.resolve("tailer.txt");
        Files.write(file, ("first" + separator.getString() + "partial").getBytes(StandardCharsets.UTF_8));
        final List<String> lines = new ArrayList<>();
        final TailerListenerAdapter listener = new TailerListenerAdapter() {
            private Tailer tailer;
            private int eofCount;

            @Override
            public void endOfFileReached() {
                final String append;
                switch (++eofCount) {
                case 1:
                    append = " middle";
                    break;
                case 2:
                    append = " end\n";
                    break;
                default:
                    tailer.close();
                    return;
                }
                try {
                    Files.write(file, append.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                } catch (final IOException e) {
                    fail(e);
                }
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
        assertEquals(Arrays.asList("first", "partial middle end"), lines);
    }
}
