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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.function.IOUnaryOperator;

/**
 * Proxy stream that prevents the underlying input stream from being closed.
 * <p>
 * This class is typically used in cases where an input stream needs to be
 * passed to a component that wants to explicitly close the stream even if more
 * input would still be available to other components.
 * </p>
 *
 * @since 1.4
 */
public class CloseShieldInputStream extends ProxyInputStream {

    /**
     * Constructs a new builder for {@link CloseShieldInputStream}.
     *
     * @since 2.22.0
     */
    public static class Builder extends AbstractBuilder<CloseShieldInputStream, Builder> {

        private IOUnaryOperator<InputStream> onClose = is -> ClosedInputStream.INSTANCE;

        /**
         * Constructs a new instance.
         */
        public Builder() {
            // empty
        }

        @Override
        public CloseShieldInputStream get() throws IOException {
            return new CloseShieldInputStream(this);
        }

        /**
         * Sets the {@code onClose} function. By default, replaces the underlying input stream when {@link #close()} is called.
         *
         * @param onClose the onClose function.
         * @return {@code this} instance.
         */
        public Builder setOnClose(final IOUnaryOperator<InputStream> onClose) {
            this.onClose = onClose;
            return asThis();
        }

    }

    /**
     * Constructs a new builder for {@link CloseShieldInputStream}.
     *
     * @return the new builder.
     * @since 2.22.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs a proxy that only shields {@link System#in} from closing.
     *
     * @param inputStream the candidate input stream.
     * @return the given stream or a proxy on {@link System#in}.
     * @since 2.17.0
     */
    public static InputStream systemIn(final InputStream inputStream) {
        return inputStream == System.in ? wrap(inputStream) : inputStream;
    }

    /**
     * Constructs a proxy that shields the given input stream from being closed.
     *
     * @param inputStream the input stream to wrap.
     * @return the created proxy.
     * @since 2.9.0
     */
    public static CloseShieldInputStream wrap(final InputStream inputStream) {
        return new CloseShieldInputStream(inputStream);
    }

    private final IOUnaryOperator<InputStream> onClose;

    private CloseShieldInputStream(final Builder builder) throws IOException {
        super(builder.getInputStream());
        this.onClose = builder.onClose;
    }

    /**
     * Constructs a proxy that shields the given input stream from being closed.
     *
     * @param inputStream underlying input stream.
     * @deprecated Using this constructor prevents IDEs from warning if the
     *             underlying input stream is never closed. Use
     *             {@link #wrap(InputStream)} instead.
     */
    @Deprecated
    public CloseShieldInputStream(final InputStream inputStream) {
        super(inputStream);
        this.onClose = builder().onClose;
    }

    /**
     * Applies the {@code onClose} function to the underlying input stream, replacing it with the result.
     * <p>
     * By default, replaces the underlying input stream with a {@link ClosedInputStream} sentinel. The original input stream will remain open, but this proxy
     * will appear closed.
     * </p>
     *
     * @throws IOException Thrown by the {@code onClose} function.
     */
    @Override
    public void close() throws IOException {
        in = onClose.apply(in);
    }

}
