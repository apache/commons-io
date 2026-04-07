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
import java.nio.channels.Channel;

import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.input.ProxyReader;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.io.output.ProxyWriter;

/**
 * A {@link Channel} filter which delegates to the wrapped {@link Channel}.
 *
 * @param <C> the {@link Channel} type.
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
public class FilterChannel<C extends Channel> implements Channel {

    /**
     * Builds instances of {@link FilterChannel} for subclasses.
     *
     * @param <F> The {@link FilterChannel} type.
     * @param <C> The {@link Channel} type wrapped by the FilterChannel.
     * @param <B> The builder type.
     */
    public abstract static class AbstractBuilder<F extends FilterChannel<C>, C extends Channel, B extends AbstractBuilder<F, C, B>>
            extends AbstractStreamBuilder<F, AbstractBuilder<F, C, B>> {

        /**
         * Constructs instance for subclasses.
         */
        protected AbstractBuilder() {
            // empty
        }
    }

    /**
     * Builds instances of {@link FilterChannel}.
     */
    public static class Builder extends AbstractBuilder<FilterChannel<Channel>, Channel, Builder> {

        /**
         * Builds instances of {@link FilterChannel}.
         */
        protected Builder() {
            // empty
        }

        @Override
        public FilterChannel<Channel> get() throws IOException {
            return new FilterChannel<>(this);
        }
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder forChannel() {
        return new Builder();
    }

    final C channel;

    /**
     * Constructs a new instance.
     *
     * @param builder The source builder.
     * @throws IOException if an I/O error occurs.
     */
    @SuppressWarnings("unchecked")
    FilterChannel(final AbstractBuilder<?, ?, ?> builder) throws IOException {
        channel = (C) builder.getChannel(Channel.class);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * Unwraps this instance by returning the underlying {@link Channel} of type {@code C}.
     * <p>
     * Use with caution.
     * </p>
     *
     * @return the underlying channel of type {@code C}.
     */
    public C unwrap() {
        return channel;
    }
}
