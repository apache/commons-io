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

package org.apache.commons.io.channels;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.input.ProxyReader;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.io.output.ProxyWriter;

/**
 * A {@link ByteChannel} filter which delegates to the wrapped {@link ByteChannel}.
 * <p>
 * A {@code FilterByteChannel} wraps some other channel, which it uses as its basic source of data, possibly transforming the data along the way or providing
 * additional functionality. The class {@code FilterByteChannel} itself simply overrides methods of {@code ByteChannel} with versions that pass all requests to
 * the wrapped channel. Subclasses of {@code FilterByteChannel} may of course override any methods declared or inherited by {@code FilterByteChannel}, and may
 * also provide additional fields and methods.
 * </p>
 * <p>
 * You construct s simple instance with the {@link FilterByteChannel#FilterByteChannel(ByteChannel) channel constructor} and more advanced instances through the
 * {@link Builder}.
 * </p>
 *
 * @param <C> the {@link ByteChannel} type.
 * @see FilterInputStream
 * @see FilterOutputStream
 * @see FilterReader
 * @see FilterWritableByteChannel
 * @see ProxyInputStream
 * @see ProxyOutputStream
 * @see ProxyReader
 * @see ProxyWriter
 * @since 2.22.0
 */
public class FilterByteChannel<C extends ByteChannel> extends FilterChannel<C> implements ByteChannel {

    /**
     * Builds instances of {@link FilterByteChannel} for subclasses.
     *
     * @param <F> The {@link FilterByteChannel} type.
     * @param <C> The {@link ByteChannel} type wrapped by the FilterChannel.
     * @param <B> The builder type.
     */
    public abstract static class AbstractBuilder<F extends FilterByteChannel<C>, C extends ByteChannel, B extends AbstractBuilder<F, C, B>>
            extends FilterChannel.AbstractBuilder<F, C, B> {

        /**
         * Constructs a new builder for {@link FilterByteChannel}.
         */
        protected AbstractBuilder() {
            // empty
        }
    }

    /**
     * Builds instances of {@link FilterByteChannel}.
     */
    public static class Builder extends AbstractBuilder<FilterByteChannel<ByteChannel>, ByteChannel, Builder> {

        /**
         * Builds instances of {@link FilterByteChannel}.
         */
        protected Builder() {
            // empty
        }

        @Override
        public FilterByteChannel<ByteChannel> get() throws IOException {
            return new FilterByteChannel<>(this);
        }
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder forByteChannel() {
        return new Builder();
    }

    FilterByteChannel(final AbstractBuilder<?, ?, ?> builder) throws IOException {
        super(builder);
    }

    /**
     * Constructs a new instance.
     *
     * @param byteChannel The channel to wrap.
     */
    public FilterByteChannel(final C byteChannel) {
        super(byteChannel);
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        return channel.read(dst);
    }

    @Override
    public int write(final ByteBuffer src) throws IOException {
        return channel.write(src);
    }
}
