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
import java.nio.channels.SeekableByteChannel;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.input.ProxyReader;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.io.output.ProxyWriter;

/**
 * A {@link SeekableByteChannel} filter which delegates to the wrapped {@link SeekableByteChannel}.
 * <p>
 * A {@code FilterSeekableByteChannel} wraps some other channel, which it uses as its basic source of data, possibly transforming the data along the way or
 * providing additional functionality. The class {@code FilterSeekableByteChannel} itself simply overrides methods of {@code SeekableByteChannel} with versions
 * that pass all requests to the wrapped channel. Subclasses of {@code FilterSeekableByteChannel} may of course override any methods declared or inherited by
 * {@code FilterSeekableByteChannel}, and may also provide additional fields and methods.
 * </p>
 * <p>
 * You construct s simple instance with the {@link FilterSeekableByteChannel#FilterSeekableByteChannel(SeekableByteChannel) Channel constructor} and more
 * advanced instances through the {@link Builder}.
 * </p>
 *
 * @param <C> the {@link SeekableByteChannel} type.
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
public class FilterSeekableByteChannel<C extends SeekableByteChannel> extends FilterByteChannel<C> implements SeekableByteChannel {

    /**
     * Builds instances of {@link FilterSeekableByteChannel} for subclasses.
     *
     * @param <F> The {@link FilterSeekableByteChannel} type.
     * @param <C> The {@link SeekableByteChannel} type wrapped by the FilterChannel.
     * @param <B> The builder type.
     */
    public abstract static class AbstractBuilder<F extends FilterSeekableByteChannel<C>, C extends SeekableByteChannel, B extends AbstractBuilder<F, C, B>>
            extends FilterByteChannel.AbstractBuilder<F, C, B> {

        /**
         * Constructs a new builder for {@link FilterSeekableByteChannel}.
         */
        public AbstractBuilder() {
            // empty
        }
    }

    /**
     * Builds instances of {@link FilterSeekableByteChannel}.
     */
    public static class Builder extends AbstractBuilder<FilterSeekableByteChannel<SeekableByteChannel>, SeekableByteChannel, Builder> {

        /**
         * Builds instances of {@link FilterSeekableByteChannel}.
         */
        protected Builder() {
            // empty
        }

        @Override
        public FilterSeekableByteChannel<SeekableByteChannel> get() throws IOException {
            return new FilterSeekableByteChannel<>(this);
        }
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder forSeekableByteChannel() {
        return new Builder();
    }

    FilterSeekableByteChannel(final Builder builder) throws IOException {
        super(builder);
    }

    /**
     * Constructs a new instance.
     *
     * @param channel The channel to wrap.
     */
    public FilterSeekableByteChannel(final C channel) {
        super(channel);
    }

    @Override
    public long position() throws IOException {
        return channel.position();
    }

    @Override
    public SeekableByteChannel position(final long newPosition) throws IOException {
        return channel.position(newPosition);
    }

    @Override
    public long size() throws IOException {
        return channel.size();
    }

    @Override
    public SeekableByteChannel truncate(final long size) throws IOException {
        return channel.truncate(size);
    }
}
