/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.io.serialization;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test base class that keeps track of Closeable objects and cleans them up.
 */
public abstract class AbstractCloseableListTest {
    private final List<Closeable> closeableList = new ArrayList<>();

    @AfterEach
    public void cleanup() {
        for (final Closeable c : closeableList) {
            try {
                c.close();
            } catch (final IOException ignored) {
                // ignore
            }
        }
    }

    /**
     * Adds a Closeable to close after each test.
     *
     * @param <T> The Closeable type
     * @param t The Closeable.
     * @return The Closeable.
     */
    protected <T extends Closeable> T closeAfterEachTest(final T t) {
        closeableList.add(t);
        return t;
    }

    @BeforeEach
    public void setup() {
        closeableList.clear();
    }
}