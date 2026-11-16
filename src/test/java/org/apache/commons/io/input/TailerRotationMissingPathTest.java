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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks pending rotation retries with the built-in path implementation.
 */
class TailerRotationMissingPathTest {

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testReplacementRemainsAbsentAcrossPolls(@TempDir final Path directory) throws Exception {
        final Path path = directory.resolve("current.log");
        Files.write(path, "old\n".getBytes(StandardCharsets.UTF_8));
        final List<String> lines = new ArrayList<>();
        final AtomicInteger missingFiles = new AtomicInteger();
        final AtomicInteger rotations = new AtomicInteger();
        final AtomicReference<Exception> error = new AtomicReference<>();
        final TailerListener listener = new TailerListenerAdapter() {

            private Tailer tailer;

            @Override
            public void fileNotFound() {
                if (missingFiles.incrementAndGet() == 2) {
                    assertDoesNotThrow(() -> Files.write(path, "new\nmore\n".getBytes(StandardCharsets.UTF_8)));
                }
            }

            @Override
            public void fileRotated() {
                rotations.incrementAndGet();
                // Force the replacement to disappear after size detects rotation and before open.
                assertDoesNotThrow(() -> Files.delete(path));
            }

            @Override
            public void handle(final Exception exception) {
                error.compareAndSet(null, exception);
            }

            @Override
            public void handle(final String line) {
                lines.add(line);
                if ("old".equals(line)) {
                    assertDoesNotThrow(() -> {
                        Files.move(path, directory.resolve("old.log"));
                        Files.createFile(path);
                    });
                } else if ("more".equals(line)) {
                    tailer.close();
                }
            }

            @Override
            public void init(final Tailer tailer) {
                this.tailer = tailer;
            }
        };
        try (Tailer tailer = Tailer.builder().setPath(path).setTailerListener(listener).setCharset(StandardCharsets.UTF_8).setStartThread(false)
                .setDelayDuration(Duration.ofMillis(1)).get()) {
            assertTimeoutPreemptively(Duration.ofSeconds(2), tailer::run);
        }
        assertNull(error.get(), () -> "Tailer stopped after " + missingFiles.get() + " missing-file callbacks: " + error.get());
        assertEquals(Arrays.asList("old", "new", "more"), lines);
        assertEquals(2, missingFiles.get());
        assertEquals(1, rotations.get());
    }
}
