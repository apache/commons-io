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

package org.apache.commons.io.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IORunnable}.
 */
public class IORunnableTest {

    /**
     * Tests {@link IORunnable#run()}.
     *
     * @throws IOException thrown on test failure
     */
    @Test
    public void testAccept() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        final IORunnable runnable = () -> ref.set("A1");
        runnable.run();
        assertEquals("A1", ref.get());
    }

    @Test
    public void testAsRunnable() throws Exception {
        assertThrows(UncheckedIOException.class, () -> Executors.callable(TestConstants.THROWING_IO_RUNNABLE.asRunnable()).call());
        final IORunnable runnable = () -> Files.size(PathUtils.current());
        assertNull(Executors.callable(runnable.asRunnable()).call());
    }

    @SuppressWarnings("cast")
    @Test
    public void testNoop() throws IOException {
        assertTrue(IORunnable.noop() instanceof IORunnable);
        IORunnable.noop().run();
    }

}
