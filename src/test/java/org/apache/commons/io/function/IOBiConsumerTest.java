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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOBiConsumer}.
 */
public class IOBiConsumerTest {

    @Test
    public void testAccept() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOBiConsumer<String, Integer> biConsumer = (s, i) -> ref.set(s + i);
        biConsumer.accept("A", 1);
        assertEquals("A1", ref.get());
    }

    @Test
    public void testAndThen() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOBiConsumer<String, Integer> biConsumer1 = (s, i) -> ref.set(s + i);
        final IOBiConsumer<String, Integer> biConsumer2 = (s, i) -> ref.set(ref.get() + i + s);
        biConsumer1.andThen(biConsumer2).accept("B", 2);
        assertEquals("B22B", ref.get());
    }
}
