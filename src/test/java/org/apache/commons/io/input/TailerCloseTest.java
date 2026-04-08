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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests {@link Tailer} for <a href=https://issues.apache.org/jira/browse/IO-889">IO-889</a>.
 */
public class TailerCloseTest {

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

    private static Thread newDaemonThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable, "commons-io-tailer");
        thread.setDaemon(true);
        return thread;
    }

    private ExecutorService executorService;
    private Path path;
    private final CompletableFuture<String> result = new CompletableFuture<>();

    @AfterEach
    public void tearDown() throws Exception {
        // wait for the tailer task and delete log file if previous delete failed
        if (executorService != null) {
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        }
        if (path != null && Files.exists(path)) {
            Files.delete(path);
        }
    }

    @DisabledOnOs(value = OS.WINDOWS)
    @Test
    public void testCloseTailer() throws Exception {
        path = Files.createTempFile("TailerTestFile", ".log");
        // same as default Tailer executor
        executorService = Executors.newSingleThreadExecutor(TailerCloseTest::newDaemonThread);
        try (
        // @formatter:off
            Tailer tailer = Tailer
                    .builder()
                    .setExecutorService(executorService)
                    .setTailerListener(new TailerTestListener())
                    .setFile(path.toFile())
                    .get()) {
            // @formatter:on
            Files.write(path, "aaa\n".getBytes());
            // wait for the background thread to open the file
            assertEquals("aaa", result.get(60, TimeUnit.SECONDS));
        }
        // delete file after closing Tailer
        Files.delete(path);
    }
}