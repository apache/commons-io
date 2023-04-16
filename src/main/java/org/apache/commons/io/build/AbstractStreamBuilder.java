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

package org.apache.commons.io.build;

import java.nio.charset.Charset;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

/**
 * Abstracts building a typed instance of {@code T}.
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @since 2.12.0
 */
public abstract class AbstractStreamBuilder<T, B extends AbstractStreamBuilder<T, B>> extends AbstractOriginSupplier<T, B> {

    protected static int checkBufferSize(final int initialBufferSize) {
        if (initialBufferSize < 0) {
            throw new IllegalArgumentException("Initial buffer size must be at least 0.");
        }
        return initialBufferSize;
    }

    /**
     * The buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    private int bufferSize = IOUtils.DEFAULT_BUFFER_SIZE;

    /**
     * The buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    private int bufferSizeDefault = IOUtils.DEFAULT_BUFFER_SIZE;

    /**
     * The Charset, defaults to {@link Charset#defaultCharset()}.
     */
    private Charset charset = Charset.defaultCharset();

    /**
     * The Charset, defaults to {@link Charset#defaultCharset()}.
     */
    private Charset charsetDefault = Charset.defaultCharset();

    /**
     * Gets the buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     *
     * @return the buffer size, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    protected int getBufferSize() {
        return bufferSize;
    }

    /**
     * Gets the buffer size default, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     *
     * @return the buffer size default, defaults to {@link IOUtils#DEFAULT_BUFFER_SIZE} ({@value IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    protected int getBufferSizeDefault() {
        return bufferSizeDefault;
    }

    /**
     * Gets the Charset, defaults to {@link Charset#defaultCharset()}.
     *
     * @return the Charset, defaults to {@link Charset#defaultCharset()}.
     */
    protected Charset getCharset() {
        return charset;
    }

    /**
     * Gets the Charset default, defaults to {@link Charset#defaultCharset()}.
     *
     * @return the Charset default, defaults to {@link Charset#defaultCharset()}.
     */
    protected Charset getCharsetDefault() {
        return charsetDefault;
    }

    /**
     * Sets the buffer size.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSize the buffer size.
     * @return this.
     */
    public B setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize >= 0 ? bufferSize : bufferSizeDefault;
        return asThis();
    }

    /**
     * Sets the buffer size.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSize the buffer size, null resets to the default.
     * @return this.
     */
    public B setBufferSize(final Integer bufferSize) {
        setBufferSize(bufferSize != null ? bufferSize : bufferSizeDefault);
        return asThis();
    }

    /**
     * Sets the buffer size for subclasses to initialize.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param bufferSizeDefault the buffer size, null resets to the default.
     * @return this.
     */
    protected B setBufferSizeDefault(final int bufferSizeDefault) {
        this.bufferSizeDefault = bufferSizeDefault;
        return asThis();
    }

    /**
     * Sets the Charset.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param charset the Charset, null resets to the default.
     * @return this.
     */
    public B setCharset(final Charset charset) {
        this.charset = Charsets.toCharset(charset, charsetDefault);
        return asThis();
    }

    /**
     * Sets the Charset.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param charset the Charset name, null resets to the default.
     * @return this.
     */
    public B setCharset(final String charset) {
        return setCharset(Charsets.toCharset(charset, charsetDefault));
    }

    /**
     * Sets the Charset default for subclasses to initialize.
     * <p>
     * Subclasses may ignore this setting.
     * </p>
     *
     * @param defaultCharset the Charset name, null resets to the default.
     * @return this.
     */
    protected B setCharsetDefault(final Charset defaultCharset) {
        this.charsetDefault = defaultCharset;
        return asThis();
    }
}
