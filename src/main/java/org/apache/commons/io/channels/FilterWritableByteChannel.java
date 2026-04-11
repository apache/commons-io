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
import java.nio.channels.WritableByteChannel;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.input.ProxyReader;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.io.output.ProxyWriter;

/**
 * A {@link WritableByteChannel} filter which delegates to the wrapped {@link WritableByteChannel}.
 * <p>
 * A {@code FilterWritableByteChannel} wraps some other channel, which it uses as its basic source of data, possibly transforming the data along the way or
 * providing additional functionality. The class {@code FilterWritableByteChannel} itself simply overrides methods of {@code WritableByteChannel} with versions
 * that pass all requests to the wrapped channel. Subclasses of {@code FilterWritableByteChannel} may of course override any methods declared or inherited by
 * {@code WritableByteChannel}, and may also provide additional fields and methods.
 * </p>
 * <p>
 * You construct s simple instance with the {@link FilterWritableByteChannel#FilterWritableByteChannel(WritableByteChannel) Channel constructor} and more
 * advanced instances through the {@link Builder}.
 * </p>
 *
 * @param <C> the {@link WritableByteChannel} type.
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
public class FilterWritableByteChannel<C extends WritableByteChannel> extends FilterChannel<C> implements WritableByteChannel {

    /**
     * Builds instances of {@link FilterWritableByteChannel} for subclasses.
     *
     * @param <F> The {@link FilterWritableByteChannel} type.
     * @param <C> The {@link WritableByteChannel} type wrapped by the FilterChannel.
     * @param <B> The builder type.
     */
    public abstract static class AbstractBuilder<F extends FilterWritableByteChannel<C>, C extends WritableByteChannel, B extends AbstractBuilder<F, C, B>>
            extends FilterChannel.AbstractBuilder<F, C, B> {

        /**
         * Constructs a new builder for {@link FilterWritableByteChannel}.
         */
        public AbstractBuilder() {
            // empty
        }
    }

    /**
     * Builds instances of {@link FilterByteChannel}.
     */
    public static class Builder extends AbstractBuilder<FilterWritableByteChannel<WritableByteChannel>, WritableByteChannel, Builder> {

        /**
         * Builds instances of {@link FilterByteChannel}.
         */
        protected Builder() {
            // empty
        }

        @Override
        public FilterWritableByteChannel<WritableByteChannel> get() throws IOException {
            return new FilterWritableByteChannel<>(this);
        }
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder forWritableByteChannel() {
        return new Builder();
    }

    FilterWritableByteChannel(final Builder builder) throws IOException {
        super(builder);
    }

    /**
     * Constructs a new instance.
     *
     * @param channel The channel to wrap.
     */
    public FilterWritableByteChannel(final C channel) {
        super(channel);
    }

    @Override
    public int write(final ByteBuffer src) throws IOException {
        return channel.write(src);
    }
}
