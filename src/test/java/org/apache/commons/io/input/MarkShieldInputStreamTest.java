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
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class MarkShieldInputStreamTest {

    private static final class MarkTestableInputStream extends ProxyInputStream {
        int markcount;
        int readLimit;

        public MarkTestableInputStream(final InputStream in) {
            super(in);
        }

        @SuppressWarnings("sync-override")
        @Override
        public void mark(final int readLimit) {
            // record that `mark` was called
            this.markcount++;
            this.readLimit = readLimit;

            // invoke on super
            super.mark(readLimit);
        }
    }

    @Test
    public void markIsNoOpWhenUnderlyingDoesNotSupport() throws IOException {
        try (MarkTestableInputStream in = new MarkTestableInputStream(new NullInputStream(64, false, false));
             final MarkShieldInputStream msis = new MarkShieldInputStream(in)) {

            msis.mark(1024);

            assertEquals(0, in.markcount);
            assertEquals(0, in.readLimit);
        }
    }

    @Test
    public void markIsNoOpWhenUnderlyingSupports() throws IOException {
        try (MarkTestableInputStream in = new MarkTestableInputStream(new NullInputStream(64, true, false));
             final MarkShieldInputStream msis = new MarkShieldInputStream(in)) {

            msis.mark(1024);

            assertEquals(0, in.markcount);
            assertEquals(0, in.readLimit);
        }
    }

    @Test
    public void markSupportedIsFalseWhenUnderlyingFalse() throws IOException {
        // test wrapping an underlying stream which does NOT support marking
        try (InputStream is = new NullInputStream(64, false, false)) {
            assertFalse(is.markSupported());

            try (MarkShieldInputStream msis = new MarkShieldInputStream(is)) {
                assertFalse(msis.markSupported());
            }
        }
    }

    @Test
    public void markSupportedIsFalseWhenUnderlyingTrue() throws IOException {
        // test wrapping an underlying stream which supports marking
        try (InputStream is = new NullInputStream(64, true, false)) {
            assertTrue(is.markSupported());

            try (MarkShieldInputStream msis = new MarkShieldInputStream(is)) {
                assertFalse(msis.markSupported());
            }
        }
    }

    @Test
    public void resetThrowsExceptionWhenUnderlyingDoesNotSupport() throws IOException {
        // test wrapping an underlying stream which does NOT support marking
        try (MarkShieldInputStream msis = new MarkShieldInputStream(
                new NullInputStream(64, false, false))) {
            assertThrows(UnsupportedOperationException.class, msis::reset);
        }
    }

    @Test
    public void resetThrowsExceptionWhenUnderlyingSupports() throws IOException {
        // test wrapping an underlying stream which supports marking
        try (MarkShieldInputStream msis = new MarkShieldInputStream(
                new NullInputStream(64, true, false))) {
            assertThrows(UnsupportedOperationException.class, msis::reset);
        }
    }
}
