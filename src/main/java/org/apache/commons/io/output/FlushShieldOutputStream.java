/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.io.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Re-implements {@link FilterOutputStream#flush()} to do nothing.
 *
 * @since 2.22.0
 */
public final class FlushShieldOutputStream extends ProxyOutputStream {

    // @formatter:off
    /**
     * Builds a new {@link FlushShieldOutputStream}.
     *
     * <p>
     * Using File IO:
     * </p>
     * <pre>{@code
     * FlushShieldOutputStream s = FlushShieldOutputStream.builder()
     *   .setPath("over/there.out")
     *   .setBufferSize(8192)
     *   .get();
     * }
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * FlushShieldOutputStream s = FlushShieldOutputStream.builder()
     *   .setPath("over/there.out")
     *   .setBufferSize(8192)
     *   .get();
     * }
     * </pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<FlushShieldOutputStream, Builder> {

        /**
         * Constructs a new builder of {@link FlushShieldOutputStream}.
         */
        public Builder() {
            // empty
        }

        /**
         * Builds a new {@link FlushShieldOutputStream}.
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link OutputStream}.
         * @throws IOException                   if an I/O error occurs converting to an {@link OutputStream} using {@link #getOutputStream()}.
         * @see #getOutputStream()
         * @see #getBufferSize()
         * @see #getUnchecked()
         */
        @Override
        public FlushShieldOutputStream get() throws IOException {
            return new FlushShieldOutputStream(this);
        }

    }

    /**
     * Constructs a new builder of {@link FlushShieldOutputStream}.
     *
     * @return a new builder of {@link FlushShieldOutputStream}.
     */
    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("resource") // caller closes.
    private FlushShieldOutputStream(final Builder builder) throws IOException {
        super(builder.getOutputStream());
    }

    /**
     * Constructs a {@code FlushShieldOutputStream} filter for the specified underlying output stream.
     *
     * @param out the underlying output stream to be assigned to the field {@code this.out} for later use, or {@code null} if this instance is to be created
     *            without an underlying stream.
     */
    public FlushShieldOutputStream(final OutputStream out) {
        super(out);
    }

    @Override
    public void flush() throws IOException {
        // shield: do nothing.
    }
}
